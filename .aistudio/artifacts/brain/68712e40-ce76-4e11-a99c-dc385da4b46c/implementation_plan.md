# Two Hearts Study — 10 Power Features & Font Color Overhaul

Two Hearts Study is receiving 10 purpose-built collaborative features tailored for Admin (Zaid) and Surbie, paired with a complete typography and font color overhaul featuring high-contrast deep warm plum and rose gold on crisp soft cream to guarantee effortless readability and zero eye strain.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following directions have been confirmed and are locked in for implementation:

- **Confirmed Balance**: Balanced mix of academic tools for Surbie (NEET preparation, doubt clearing, flashcards, exam countdown) and romantic administrative tools for Admin (instant hugs/nudges, surprise mystery gifts, challenge quests, live status).
- **Confirmed Typography & Palette**: Deep warm plum (`#360F1F` / `#4A152B`) headings and charcoal plum (`#23161C`) body text on crisp soft cream surfaces (`#FCF8F5` / `#FFFDFB`) with rose gold (`#C87556` / `#D48372`) accents and gleaming borders (`#EAD5CE`).
- **All 10 Features Persisted Locally & Cloud-Synced**: Full offline persistence via `TwoHeartsRepository` with SharedPreferences JSON serialization, plus automatic upstream/downstream sync with Supabase when online.

---

## 1. Overview & Core Concept

### What It Does
Two Hearts Study evolves from a syllabus tracker into a study companion ecosystem. Surbie gains daily study momentum with formula flashcards, a dedicated doubt diary, exam countdown pacing, and mood check-ins. Admin gains real-time encouragement tools including one-tap virtual hugs, study nudges, surprise mystery bounties, and custom study quests.

### Target Audience & Persona
- **Surbie (Student)**: Preparing for NEET/medical entrance exams; requires structured focus, fast recall, confidence boosters, and clear progress metrics.
- **Admin (Zaid)**: Mentor and partner; needs transparent visibility into study progress, the ability to support emotionally and academically, assign rewarding challenges, and celebrate milestones together.

### Key Value
Removes the isolation of intense exam preparation by turning study goals into shared victories, while ensuring that all text, formulas, and progress metrics are crystal-clear and legible.

---

## 2. User Experience & Visual Design

### The 10 New Features

#### Academic & Study Features (for Surbie & Shared Review)
1. **Flashcard Recall Decks**:
   - Flip cards with question/formula on the front and answer/derivation on the back.
   - Tagged by subject (Physics, Chemistry, Biology) and chapter.
   - Spaced-repetition confidence ratings: "Need Review", "Good", "Mastered" (+5 points per mastered card).
2. **Doubt Diary & Academic Help Hub**:
   - Surbie logs confusing questions or tricky concept doubts with subject tag, chapter link, priority, and notes.
   - Admin can view, comment with explanations or tips, and mark doubts as "Resolved Together" (+20 points).
3. **Exam Countdown & Daily Pace Calculator**:
   - Live visual countdown to NEET 2026 and custom mock tests.
   - Dynamic pace indicator showing how many chapters remain and target chapters per week.
   - Daily motivational quote card that refreshes each morning.
4. **Daily Mood & Energy Check-in**:
   - Quick 1-tap check-in before or after study sessions (Energetic, Motivated, Neutral, Tired, Overwhelmed).
   - Streak of positive check-ins; Admin sees Surbie's mood badge and can tailor encouragement accordingly.
5. **High-Yield NEET Chapter Priority & Quick Filter**:
   - Chapter cards tagged with "High Yield", "Medium Yield", "Foundational".
   - Fast instant search bar and status filters ("Needs Attention", "In Progress", "Completed").

#### Romantic & Support Features (for Admin & Surbie)
6. **Instant Virtual Hugs & Live Study Nudges**:
   - Admin can tap one-button quick actions: "Sending a Warm Hug 💖", "Drink Water! 💧", "You've Got This Superstar! ⭐", "Take a 5-min Stretch 🧘‍♀️".
   - App displays an animated celebratory banner on Surbie's dashboard with haptic sound/vibration.
7. **Surprise Mystery Gift Box & Bounty Dropper**:
   - Admin can drop a "Mystery Gift Box" onto Surbie's screen with a custom love note and unlockable surprise points (+50 to +250 points) or custom reward coupon (e.g., "Late night ice cream treat").
   - Unwrapping animation with confetti when Surbie taps to open.
8. **Admin Study Quests & Weekend Challenges**:
   - Admin creates custom timed quests (e.g., "Complete 2 Biology Genetics chapters before Sunday 8 PM").
   - Includes progress tracking and automatic bounty payout upon completion.
9. **Interactive Study Ambience & Lofi Soundscapes**:
   - Sound mixer in the focus timer with customizable audio streams (Gentle Rain, Cozy Cafe, Soft White Noise, Fireplace crackle).
   - Audio visualizer animation while the timer is running.
10. **Weekly Study Report Card & Milestones Certificate**:
    - Beautiful summary card showing weekly focus hours, chapters conquered, points earned, and streak records.
    - Generates an aesthetic digital certificate of achievement signed by Admin with a one-tap screenshot/share layout.

### Visual Identity & Font Color Overhaul

#### Color Palette
| Token | Hex Value | Role & Usage |
|---|---|---|
| `PlumHeader` | `#360F1F` | Display titles, screen headers, major numbers (Highest contrast: 14:1 against cream) |
| `PlumPrimary` | `#5A1A33` | Primary buttons, active tab indicators, major navigation pills |
| `TextCharcoal` | `#23161C` | Primary body text, chapter titles, routine details (Crisp readability) |
| `TextSubtlePlum` | `#664E57` | Secondary text, timestamps, subtitles (Never washed out; passes WCAG AAA) |
| `RoseGoldAccent` | `#C87556` | Highlights, flame streak icons, star ratings, key actions |
| `RoseGoldSoft` | `#F9ECE8` | Pill backgrounds, active badge containers |
| `RoseCreamBg` | `#FCF8F5` | Main app background, warm, soothing to the eyes during long study sprints |
| `SurfaceCard` | `#FFFDFB` | Card surfaces with subtle border `#EAD5CE` and soft elevation |
| `GoldBounty` | `#D99018` | Points, trophies, mystery boxes, milestone achievements |

#### Typography Hierarchy
- **Display & Large Titles**: 24–28sp Bold in `PlumHeader`, tightened letter spacing (-0.5sp) for editorial polish.
- **Card Headings & Section Headers**: 18–20sp SemiBold in `PlumHeader`.
- **Primary Body**: 15–16sp Medium/Regular in `TextCharcoal` with 22sp line height for optimal legibility.
- **Captions & Metadata**: 12–13sp SemiBold in `TextSubtlePlum`, avoiding light-gray low-contrast text.

---

## 3. Key Product Decisions & Trade-Offs

### 1. Unified Modular Navigation Architecture
- **Decision**: Keep the intuitive bottom navigation while expanding the navigation bar with dedicated tabs and sub-sheets:
  - **Dashboard**: Daily targets, countdown, mood check-in, recent routine, instant hugs banner.
  - **Curriculum**: High-yield syllabus, chapter notes, search & filter.
  - **Flashcards & Doubts**: Spaced recall deck and doubt diary.
  - **Focus & Ambience**: Pomodoro timer with soundscape mixer and lofi ambience.
  - **Quests & Love Hub**: Admin quests, mystery gifts, love notes, reward store, weekly report card.
- **Why**: Prevents UI clutter while making every single one of the 10 features easily discoverable in 1 tap.

### 2. Dual Local-First Persistence & Supabase Integration
- **Decision**: All flashcards, doubts, mood logs, mystery gifts, quests, and nudges will be serialized into the existing robust `TwoHeartsRepository` SharedPreferences storage engine with real-time reactive `StateFlow`s, and synchronized with Supabase tables when credentials are provided.
- **Why**: Zero chance of data loss if the user exits the app or studies offline.

---

## 4. Technical Architecture & Data Strategy

### System Component Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                          MainActivity (Scaffold)                       │
│    TopBar (Points Pill, Role Toggle, Admin Nudge, Settings)            │
│    Navigation: Dashboard | Syllabus | Flashcards | Focus | Quests      │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
    ┌───────────────────────────────┼───────────────────────────────┐
    ▼                               ▼                               ▼
┌───────────────────────┐ ┌───────────────────────┐ ┌───────────────────────┐
│  Academic Study Hub   │ │   Admin & Love Hub    │ │  Focus & Wellness     │
│  - Flashcard Decks    │ │  - Virtual Hugs & Nudge│ │  - Ambience Sound Mixer│
│  - Doubt Diary Log    │ │  - Mystery Gift Boxes │ │  - Mood & Energy Log  │
│  - Exam Countdown     │ │  - Study Quests/Bounty│ │  - Weekly Report Card │
│  - High-Yield Filters │ │  - Love Notes & Store │ │  - Milestone Badges   │
└───────────┬───────────┘ └───────────┬───────────┘ └───────────┬───────────┘
            │                         │                         │
            └─────────────────────────┼─────────────────────────┘
                                      ▼
                        ┌───────────────────────────┐
                        │   TwoHeartsRepository     │
                        │   (Single Source of Truth)│
                        └─────────────┬─────────────┘
                                      │
            ┌─────────────────────────┴─────────────────────────┐
            ▼                                                   ▼
┌───────────────────────────┐                       ┌───────────────────────┐
│ Local SharedPreferences   │                       │ Supabase Cloud Sync   │
│ - Instant offline save    │                       │ - Rest API v1         │
│ - Zero data loss on exit  │                       │ - Multi-device sync   │
└───────────────────────────┘                       └───────────────────────┘
```

### Data Models to Add
- `Flashcard(id, subjectId, chapterId, frontQuestion, backAnswer, masteryLevel, lastReviewedAt)`
- `DoubtEntry(id, subjectId, chapterTitle, questionText, adminSolution, isResolved, createdAt)`
- `ExamTarget(id, title, targetDate, totalSyllabusCount, completedCount)`
- `MoodCheckIn(id, moodEmoji, moodTitle, energyLevel, timestamp, adminReaction)`
- `AdminNudge(id, type, message, senderName, timestamp, isSeen)`
- `MysteryGift(id, title, message, pointsAmount, perkDescription, isClaimed)`
- `StudyQuest(id, title, description, targetType, targetCount, progress, pointsBounty, deadline, isCompleted)`
- `WeeklyReport(totalHours, chaptersCompleted, focusSessionsCount, currentStreak, topSubject)`

---

## Verification & Testing Plan

1. **Compilation & Syntax**: Build with `compile_applet` ensuring all models, composables, and repository extensions compile without warnings.
2. **Robolectric JVM Unit Tests**:
   - Test flashcard mastery state updates and point awards.
   - Test doubt logging, resolution, and persistence.
   - Test mystery gift claiming and wallet balance updates.
   - Test state persistence across simulated repository restart.
3. **Contrast & Theme Verification**: Audit all text elements against light and dark theme surfaces to guarantee WCAG AAA contrast for primary text.
