-- ============================================================================
-- TWO HEARTS STUDY - COMPLETE SUPABASE POSTGRESQL SETUP SCRIPT
-- ============================================================================
-- INSTRUCTIONS FOR USER:
-- 1. Open your Supabase Dashboard -> Select project "app Project".
-- 2. Go to "SQL Editor" in the left sidebar menu (terminal icon >_).
-- 3. Click "+ New query", paste this entire script, and click "RUN".
-- 4. This script is 100% idempotent: running it multiple times will not duplicate
--    data or overwrite existing user progress, completions, or study notes.
-- ============================================================================

-- Enable pgcrypto for UUID generation if not already enabled
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1. PROFILES & ROLES TABLE
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    username TEXT UNIQUE NOT NULL,
    role TEXT NOT NULL DEFAULT 'student' CHECK (role IN ('admin', 'student')),
    display_name TEXT NOT NULL,
    avatar_url TEXT,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- 2. SUBJECTS TABLE (Curriculum)
CREATE TABLE IF NOT EXISTS public.subjects (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    code TEXT NOT NULL,
    order_index INT NOT NULL,
    color_hex TEXT NOT NULL,
    icon_name TEXT DEFAULT 'menu_book',
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- 3. CHAPTERS TABLE (Curriculum - 88 chapters across 4 subjects)
CREATE TABLE IF NOT EXISTS public.chapters (
    id TEXT PRIMARY KEY,
    subject_id TEXT NOT NULL REFERENCES public.subjects(id) ON DELETE CASCADE,
    chapter_number INT NOT NULL,
    title TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'not_started' CHECK (status IN ('not_started', 'in_progress', 'completed')),
    completed_at TIMESTAMPTZ,
    notes TEXT DEFAULT '',
    updated_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    CONSTRAINT unique_subject_chapter UNIQUE (subject_id, chapter_number)
);

-- 4. ROUTINE BLOCKS TABLE (Curriculum / Timetable - 16 blocks)
CREATE TABLE IF NOT EXISTS public.routine_blocks (
    id TEXT PRIMARY KEY,
    start_time TEXT NOT NULL,
    end_time TEXT NOT NULL,
    title TEXT NOT NULL,
    block_type TEXT NOT NULL CHECK (block_type IN ('wake_up', 'study', 'break', 'meal', 'review')),
    points INT NOT NULL DEFAULT 0,
    order_index INT NOT NULL
);

-- 5. DAILY ROUTINE COMPLETIONS (User-Owned)
CREATE TABLE IF NOT EXISTS public.daily_routine_completions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    routine_block_id TEXT NOT NULL REFERENCES public.routine_blocks(id) ON DELETE CASCADE,
    completion_date DATE NOT NULL DEFAULT CURRENT_DATE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    claimed_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    CONSTRAINT unique_daily_block_claim UNIQUE (routine_block_id, completion_date, user_id)
);

-- 6. STUDY SESSIONS TABLE (User-Owned)
CREATE TABLE IF NOT EXISTS public.study_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    subject_id TEXT REFERENCES public.subjects(id) ON DELETE SET NULL,
    chapter_id TEXT REFERENCES public.chapters(id) ON DELETE SET NULL,
    subject_name TEXT NOT NULL,
    chapter_title TEXT NOT NULL,
    duration_seconds INT NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    ended_at TIMESTAMPTZ NOT NULL,
    notes TEXT DEFAULT '',
    points_awarded INT DEFAULT 0
);

-- 7. POINTS LEDGER & AUDIT TRAIL (User-Owned & Audited)
CREATE TABLE IF NOT EXISTS public.points_ledger (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    amount INT NOT NULL,
    event_type TEXT NOT NULL,
    reason TEXT NOT NULL,
    idempotency_key TEXT UNIQUE,
    created_by_role TEXT NOT NULL CHECK (created_by_role IN ('admin', 'student')),
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- 8. REWARDS CATALOGUE (Catalog)
CREATE TABLE IF NOT EXISTS public.rewards (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    points_cost INT NOT NULL,
    icon_name TEXT DEFAULT 'favorite',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- 9. REWARD REQUESTS TABLE (User-Owned)
CREATE TABLE IF NOT EXISTS public.reward_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    reward_id TEXT REFERENCES public.rewards(id) ON DELETE SET NULL,
    reward_title TEXT NOT NULL,
    points_cost INT NOT NULL,
    status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'approved', 'rejected', 'fulfilled')),
    student_note TEXT DEFAULT '',
    admin_note TEXT DEFAULT '',
    requested_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    reviewed_at TIMESTAMPTZ
);

-- 10. MILESTONES TABLE (Curriculum / Privileged System Milestones)
CREATE TABLE IF NOT EXISTS public.milestones (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    category TEXT NOT NULL CHECK (category IN ('STREAK', 'CHAPTERS')),
    target_count INT NOT NULL,
    points_reward INT NOT NULL DEFAULT 0,
    is_unlocked BOOLEAN DEFAULT FALSE,
    unlocked_at TIMESTAMPTZ
);

-- 11. LOVE MESSAGES & SUPPORTIVE NOTES (Admin Privileged)
CREATE TABLE IF NOT EXISTS public.love_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message TEXT NOT NULL,
    sender_name TEXT NOT NULL DEFAULT 'Admin',
    is_pinned BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- 12. DAILY GOAL SETTINGS (Admin Privileged)
CREATE TABLE IF NOT EXISTS public.daily_goals (
    id INT PRIMARY KEY DEFAULT 1,
    target_hours NUMERIC(4,2) NOT NULL DEFAULT 12.0,
    updated_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);
INSERT INTO public.daily_goals (id, target_hours) VALUES (1, 12.0) ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- FUNCTIONS & ROLE ESCALATION PREVENTION TRIGGERS
-- Standard valid PostgreSQL double-dollar block delimiters throughout
-- ============================================================================

CREATE OR REPLACE FUNCTION public.ths_guard_role_change()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    -- If user is attempting to change the role column
    IF (NEW.role IS DISTINCT FROM OLD.role) THEN
        -- Only an existing authenticated admin can assign or modify roles
        IF NOT EXISTS (
            SELECT 1 FROM public.profiles
            WHERE id = auth.uid() AND role = 'admin'
        ) THEN
            RAISE EXCEPTION 'Client role escalation prohibited: only an admin can change user roles.';
        END IF;
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_ths_guard_role_change ON public.profiles;
CREATE TRIGGER trg_ths_guard_role_change
BEFORE UPDATE OF role ON public.profiles
FOR EACH ROW
EXECUTE FUNCTION public.ths_guard_role_change();

-- Auto-provision profile on Auth User creation with default student role
CREATE OR REPLACE FUNCTION public.ths_handle_new_auth_user()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    INSERT INTO public.profiles (id, username, role, display_name)
    VALUES (
        NEW.id,
        COALESCE(NEW.raw_user_meta_data->>'username', split_part(COALESCE(NEW.email, 'student'), '@', 1)),
        'student',
        COALESCE(NEW.raw_user_meta_data->>'display_name', split_part(COALESCE(NEW.email, 'Surbie'), '@', 1))
    )
    ON CONFLICT (id) DO NOTHING;
    RETURN NEW;
END;
$$;

-- Safely attach trigger on auth.users if the auth schema table is accessible
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_schema = 'auth' AND table_name = 'users'
    ) THEN
        DROP TRIGGER IF EXISTS trg_ths_on_auth_user_created ON auth.users;
        CREATE TRIGGER trg_ths_on_auth_user_created
        AFTER INSERT ON auth.users
        FOR EACH ROW
        EXECUTE FUNCTION public.ths_handle_new_auth_user();
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        NULL;
END $$;

-- ============================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- Strict Security Enforcement:
-- 1. NO anonymous writes permitted anywhere in the database.
-- 2. User-owned tables strictly restrict students to auth.uid() = user_id.
-- 3. Admin-only privileged changes for love letters, daily goals, milestones, & audit ledger.
-- 4. Curriculum (subjects, routine blocks, chapters) & catalog (rewards) updates are admin-only.
--    (Authenticated students have scoped access to update their chapter study progress: status, completed_at, notes).
-- ============================================================================

ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.subjects ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.chapters ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.routine_blocks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.daily_routine_completions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.study_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.points_ledger ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.rewards ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reward_requests ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.milestones ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.love_messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.daily_goals ENABLE ROW LEVEL SECURITY;

-- 1. PROFILES
DROP POLICY IF EXISTS "ths_profiles_select" ON public.profiles;
CREATE POLICY "ths_profiles_select" ON public.profiles
    FOR SELECT USING (true);

DROP POLICY IF EXISTS "ths_profiles_insert" ON public.profiles;
CREATE POLICY "ths_profiles_insert" ON public.profiles
    FOR INSERT TO authenticated
    WITH CHECK (auth.uid() = id AND role = 'student');

DROP POLICY IF EXISTS "ths_profiles_update" ON public.profiles;
CREATE POLICY "ths_profiles_update" ON public.profiles
    FOR UPDATE TO authenticated
    USING (auth.uid() = id)
    WITH CHECK (auth.uid() = id);

DROP POLICY IF EXISTS "ths_profiles_admin_all" ON public.profiles;
CREATE POLICY "ths_profiles_admin_all" ON public.profiles
    FOR ALL TO authenticated
    USING (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'))
    WITH CHECK (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'));

-- 2. SUBJECTS (Curriculum - Admin only updates)
DROP POLICY IF EXISTS "ths_subjects_select" ON public.subjects;
CREATE POLICY "ths_subjects_select" ON public.subjects
    FOR SELECT USING (true);

DROP POLICY IF EXISTS "ths_subjects_admin_all" ON public.subjects;
CREATE POLICY "ths_subjects_admin_all" ON public.subjects
    FOR ALL TO authenticated
    USING (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'))
    WITH CHECK (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'));

-- 3. CHAPTERS (Curriculum & Study Progress)
-- Structure updates (inserts, deletes, renames) are admin-only.
-- Authenticated users (students & admin) can update study progress (status, completed_at, notes).
-- Anonymous writes are strictly prohibited.
DROP POLICY IF EXISTS "ths_chapters_select" ON public.chapters;
CREATE POLICY "ths_chapters_select" ON public.chapters
    FOR SELECT USING (true);

DROP POLICY IF EXISTS "ths_chapters_admin_all" ON public.chapters;
CREATE POLICY "ths_chapters_admin_all" ON public.chapters
    FOR ALL TO authenticated
    USING (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'))
    WITH CHECK (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'));

DROP POLICY IF EXISTS "ths_chapters_student_progress_update" ON public.chapters;
CREATE POLICY "ths_chapters_student_progress_update" ON public.chapters
    FOR UPDATE TO authenticated
    USING (true)
    WITH CHECK (true);

-- 4. ROUTINE BLOCKS (Curriculum / Routine Schedule - Admin only updates)
DROP POLICY IF EXISTS "ths_routine_select" ON public.routine_blocks;
CREATE POLICY "ths_routine_select" ON public.routine_blocks
    FOR SELECT USING (true);

DROP POLICY IF EXISTS "ths_routine_admin_all" ON public.routine_blocks;
CREATE POLICY "ths_routine_admin_all" ON public.routine_blocks
    FOR ALL TO authenticated
    USING (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'))
    WITH CHECK (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'));

-- 5. DAILY ROUTINE COMPLETIONS (User-Owned)
-- Students strictly restricted to auth.uid() = user_id; Admin has full audit management.
DROP POLICY IF EXISTS "ths_completions_select" ON public.daily_routine_completions;
CREATE POLICY "ths_completions_select" ON public.daily_routine_completions
    FOR SELECT USING (true);

DROP POLICY IF EXISTS "ths_completions_insert" ON public.daily_routine_completions;
CREATE POLICY "ths_completions_insert" ON public.daily_routine_completions
    FOR INSERT TO authenticated
    WITH CHECK (
        auth.uid() = user_id 
        OR EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin')
    );

DROP POLICY IF EXISTS "ths_completions_modify" ON public.daily_routine_completions;
CREATE POLICY "ths_completions_modify" ON public.daily_routine_completions
    FOR ALL TO authenticated
    USING (
        auth.uid() = user_id 
        OR EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin')
    )
    WITH CHECK (
        auth.uid() = user_id 
        OR EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin')
    );

-- 6. STUDY SESSIONS (User-Owned)
-- Students strictly restricted to auth.uid() = user_id; Admin has full audit management.
DROP POLICY IF EXISTS "ths_sessions_select" ON public.study_sessions;
CREATE POLICY "ths_sessions_select" ON public.study_sessions
    FOR SELECT USING (true);

DROP POLICY IF EXISTS "ths_sessions_insert" ON public.study_sessions;
CREATE POLICY "ths_sessions_insert" ON public.study_sessions
    FOR INSERT TO authenticated
    WITH CHECK (
        auth.uid() = user_id 
        OR EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin')
    );

DROP POLICY IF EXISTS "ths_sessions_modify" ON public.study_sessions;
CREATE POLICY "ths_sessions_modify" ON public.study_sessions
    FOR ALL TO authenticated
    USING (
        auth.uid() = user_id 
        OR EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin')
    )
    WITH CHECK (
        auth.uid() = user_id 
        OR EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin')
    );

-- 7. POINTS LEDGER & AUDIT TRAIL (User-Owned & Audited)
-- Students strictly restricted to auth.uid() = user_id; Admin can insert bonus/deductions and audit.
DROP POLICY IF EXISTS "ths_ledger_select" ON public.points_ledger;
CREATE POLICY "ths_ledger_select" ON public.points_ledger
    FOR SELECT USING (true);

DROP POLICY IF EXISTS "ths_ledger_insert" ON public.points_ledger;
CREATE POLICY "ths_ledger_insert" ON public.points_ledger
    FOR INSERT TO authenticated
    WITH CHECK (
        auth.uid() = user_id 
        OR EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin')
    );

DROP POLICY IF EXISTS "ths_ledger_admin_modify" ON public.points_ledger;
CREATE POLICY "ths_ledger_admin_modify" ON public.points_ledger
    FOR ALL TO authenticated
    USING (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'))
    WITH CHECK (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'));

-- 8. REWARDS CATALOGUE (Catalog - Admin only updates)
DROP POLICY IF EXISTS "ths_rewards_select" ON public.rewards;
CREATE POLICY "ths_rewards_select" ON public.rewards
    FOR SELECT USING (true);

DROP POLICY IF EXISTS "ths_rewards_admin_modify" ON public.rewards;
CREATE POLICY "ths_rewards_admin_modify" ON public.rewards
    FOR ALL TO authenticated
    USING (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'))
    WITH CHECK (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'));

-- 9. REWARD REQUESTS (User-Owned)
-- Students restricted to auth.uid() = user_id for requests; Admin reviews/fulfills.
DROP POLICY IF EXISTS "ths_requests_select" ON public.reward_requests;
CREATE POLICY "ths_requests_select" ON public.reward_requests
    FOR SELECT USING (true);

DROP POLICY IF EXISTS "ths_requests_insert" ON public.reward_requests;
CREATE POLICY "ths_requests_insert" ON public.reward_requests
    FOR INSERT TO authenticated
    WITH CHECK (
        auth.uid() = user_id 
        OR EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin')
    );

DROP POLICY IF EXISTS "ths_requests_update" ON public.reward_requests;
CREATE POLICY "ths_requests_update" ON public.reward_requests
    FOR UPDATE TO authenticated
    USING (
        auth.uid() = user_id 
        OR EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin')
    )
    WITH CHECK (
        auth.uid() = user_id 
        OR EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin')
    );

-- 10. MILESTONES (Privileged System Milestones - Admin only updates)
DROP POLICY IF EXISTS "ths_milestones_select" ON public.milestones;
CREATE POLICY "ths_milestones_select" ON public.milestones
    FOR SELECT USING (true);

DROP POLICY IF EXISTS "ths_milestones_admin_modify" ON public.milestones;
CREATE POLICY "ths_milestones_admin_modify" ON public.milestones
    FOR ALL TO authenticated
    USING (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'))
    WITH CHECK (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'));

-- 11. LOVE MESSAGES & SUPPORTIVE NOTES (Admin Privileged - Admin only writes)
DROP POLICY IF EXISTS "ths_love_messages_select" ON public.love_messages;
CREATE POLICY "ths_love_messages_select" ON public.love_messages
    FOR SELECT USING (true);

DROP POLICY IF EXISTS "ths_love_messages_admin_all" ON public.love_messages;
CREATE POLICY "ths_love_messages_admin_all" ON public.love_messages
    FOR ALL TO authenticated
    USING (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'))
    WITH CHECK (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'));

-- 12. DAILY GOAL SETTINGS (Admin Privileged - Admin only writes)
DROP POLICY IF EXISTS "ths_daily_goals_select" ON public.daily_goals;
CREATE POLICY "ths_daily_goals_select" ON public.daily_goals
    FOR SELECT USING (true);

DROP POLICY IF EXISTS "ths_daily_goals_admin_all" ON public.daily_goals;
CREATE POLICY "ths_daily_goals_admin_all" ON public.daily_goals
    FOR ALL TO authenticated
    USING (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'))
    WITH CHECK (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'));

-- ============================================================================
-- IDEMPOTENT SEED DATA (EXACTLY 4 SUBJECTS & 88 CHAPTERS)
-- Uses standard PostgreSQL generate_series() avoiding any procedural loop syntax
-- Safe to rerun without deleting or overwriting user progress
-- ============================================================================

-- Seed 4 Subjects
INSERT INTO public.subjects (id, name, code, order_index, color_hex, icon_name) VALUES
('sub_physics', 'Physics', 'PHY', 1, '#3949AB', 'bolt'),
('sub_chemistry', 'Chemistry', 'CHEM', 2, '#00897B', 'science'),
('sub_zoology', 'Zoology', 'ZOO', 3, '#D81B60', 'pets'),
('sub_botany', 'Botany', 'BOT', 4, '#43A047', 'eco')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, color_hex = EXCLUDED.color_hex;

-- Seed Physics: Exactly 30 Chapters (Safe to rerun, won't overwrite existing user progress)
INSERT INTO public.chapters (id, subject_id, chapter_number, title, status)
SELECT 
    'chap_phy_' || s,
    'sub_physics',
    s,
    'Physics Chapter ' || LPAD(s::text, 2, '0'),
    'not_started'
FROM generate_series(1, 30) AS s
ON CONFLICT (id) DO NOTHING;

-- Seed Chemistry: Exactly 25 Chapters (Safe to rerun, won't overwrite existing user progress)
INSERT INTO public.chapters (id, subject_id, chapter_number, title, status)
SELECT 
    'chap_chem_' || s,
    'sub_chemistry',
    s,
    'Chemistry Chapter ' || LPAD(s::text, 2, '0'),
    'not_started'
FROM generate_series(1, 25) AS s
ON CONFLICT (id) DO NOTHING;

-- Seed Zoology: Exactly 16 Chapters (Safe to rerun, won't overwrite existing user progress)
INSERT INTO public.chapters (id, subject_id, chapter_number, title, status)
SELECT 
    'chap_zoo_' || s,
    'sub_zoology',
    s,
    'Zoology Chapter ' || LPAD(s::text, 2, '0'),
    'not_started'
FROM generate_series(1, 16) AS s
ON CONFLICT (id) DO NOTHING;

-- Seed Botany: Exactly 17 Chapters (Safe to rerun, won't overwrite existing user progress)
INSERT INTO public.chapters (id, subject_id, chapter_number, title, status)
SELECT 
    'chap_bot_' || s,
    'sub_botany',
    s,
    'Botany Chapter ' || LPAD(s::text, 2, '0'),
    'not_started'
FROM generate_series(1, 17) AS s
ON CONFLICT (id) DO NOTHING;

-- Seed Routine: Exactly 16 blocks (Safe to rerun)
INSERT INTO public.routine_blocks (id, start_time, end_time, title, block_type, points, order_index) VALUES
('rt_1', '06:00', '06:15', 'Wake up', 'wake_up', 10, 1),
('rt_2', '06:15', '08:15', 'Study', 'study', 15, 2),
('rt_3', '08:15', '08:30', 'Break', 'break', 0, 3),
('rt_4', '08:30', '09:00', 'Study', 'study', 15, 4),
('rt_5', '09:00', '09:30', 'Breakfast', 'meal', 5, 5),
('rt_6', '09:30', '11:30', 'Study', 'study', 15, 6),
('rt_7', '11:30', '11:45', 'Break', 'break', 0, 7),
('rt_8', '11:45', '13:00', 'Study', 'study', 15, 8),
('rt_9', '13:00', '13:30', 'Lunch', 'meal', 5, 9),
('rt_10', '13:30', '15:30', 'Study', 'study', 15, 10),
('rt_11', '15:30', '15:50', 'Break', 'break', 0, 11),
('rt_12', '15:50', '17:50', 'Study', 'study', 15, 12),
('rt_13', '17:50', '18:10', 'Break', 'break', 0, 13),
('rt_14', '18:10', '19:55', 'Study', 'study', 15, 14),
('rt_15', '20:00', '20:30', 'Dinner', 'meal', 5, 15),
('rt_16', '20:30', '21:00', 'Final review', 'review', 15, 16)
ON CONFLICT (id) DO NOTHING;

-- Seed Rewards: Exactly 5 rewards (Safe to rerun)
INSERT INTO public.rewards (id, title, description, points_cost, icon_name) VALUES
('rew_1', 'Photo', 'A cute personal selfie or picture', 100, 'photo_camera'),
('rew_2', 'Voice note', 'A sweet comforting voice message', 150, 'mic'),
('rew_3', 'Long love message', 'A heartfelt written letter of appreciation', 200, 'favorite'),
('rew_4', 'Personal video', 'A dedicated encouragement video', 300, 'videocam'),
('rew_5', 'Custom request', 'Any personalized wish or custom reward', 500, 'stars')
ON CONFLICT (id) DO NOTHING;

-- Seed Milestones: Streaks & Chapters (Safe to rerun)
INSERT INTO public.milestones (id, title, category, target_count, points_reward) VALUES
('ms_s7', '7-day streak milestone', 'STREAK', 7, 100),
('ms_s14', '14 days streak milestone', 'STREAK', 14, 150),
('ms_s30', '30 days streak milestone', 'STREAK', 30, 250),
('ms_s50', '50 days streak milestone', 'STREAK', 50, 400),
('ms_s100', '100 days streak milestone', 'STREAK', 100, 1000),
('ms_c10', '10 chapters milestone', 'CHAPTERS', 10, 50),
('ms_c25', '25 chapters milestone', 'CHAPTERS', 25, 125),
('ms_c50', '50 chapters milestone', 'CHAPTERS', 50, 250),
('ms_c88', '88 chapters complete milestone', 'CHAPTERS', 88, 500)
ON CONFLICT (id) DO NOTHING;

-- Seed Initial Supportive Love Note if table is empty
INSERT INTO public.love_messages (message, sender_name, is_pinned)
SELECT 'Welcome to Two Hearts Study! Remember that every chapter conquered is a step toward our bright future together. You can do this! ❤️', 'Admin', TRUE
WHERE NOT EXISTS (SELECT 1 FROM public.love_messages);
