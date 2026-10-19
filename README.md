# Two Hearts Study ❤️📚
### Private, Supportive Couple Study & Goal Tracking System

**Two Hearts Study** is a dedicated Android application designed for two people: **Admin** and **Surbie (Student)**. It combines disciplined academic tracking across an 88-chapter syllabus with romantic encouragement, synchronized daily routines, a focused study timer, an audited points ledger, and custom milestones & rewards.

---

## 🌟 Key Features

1. **Exact 88-Chapter Curriculum**:
   - **Physics**: 30 chapters (`Physics Chapter 01` to `30`)
   - **Chemistry**: 25 chapters (`Chemistry Chapter 01` to `25`)
   - **Zoology**: 16 chapters (`Zoology Chapter 01` to `16`)
   - **Botany**: 17 chapters (`Botany Chapter 01` to `17`)
   - Status tracking (`Not Started`, `In Progress`, `Completed`), formulas/notes notebook, and search filters.

2. **16-Block Synchronized Routine**:
   - `06:00` Wake up (+10 pts)
   - `06:15–08:15` Study (+15 pts)
   - `08:15–08:30` Break
   - `08:30–09:00` Study (+15 pts)
   - `09:00–09:30` Breakfast (+5 pts)
   - `09:30–11:30` Study (+15 pts)
   - `11:30–11:45` Break
   - `11:45–13:00` Study (+15 pts)
   - `13:00–13:30` Lunch (+5 pts)
   - `13:30–15:30` Study (+15 pts)
   - `15:30–15:50` Break
   - `15:50–17:50` Study (+15 pts)
   - `17:50–18:10` Break
   - `18:10–19:55` Study (+15 pts)
   - `20:00–20:30` Dinner (+5 pts)
   - `20:30–21:00` Final review (+15 pts)
   - Configurable **12.0 hours daily focused study goal** and full routine completion bonus (+50 pts).

3. **Focused Study Timer**:
   - Start, Pause, Resume, Stop controls.
   - Chapter and subject tagging.
   - Session notes compilation.
   - Automatically credits **+10 points for each full focused hour studied**.

4. **Points Ledger & Milestones**:
   - Streak Milestones: `7 days (+100)`, `14 days (+150)`, `30 days (+250)`, `50 days (+400)`, `100 days (+1000)`.
   - Chapter Milestones: `10 chapters (+50)`, `25 chapters (+125)`, `50 chapters (+250)`, `88 chapters (+500)`.
   - Admin audit trail: Award bonus points or deduct points with mandatory recorded reason.

5. **Romantic Rewards Catalogue**:
   - Photo (100 pts)
   - Voice note (150 pts)
   - Long love message (200 pts)
   - Personal video (300 pts)
   - Custom request (500 pts)
   - Student submits requests; Admin approves or fulfills with notes (or rejects with automatic point refund).

6. **Supportive Love Notes**:
   - Admin can send daily pinned love letters and cheer notes to Surbie's dashboard.

---

## 🚀 Supabase Cloud Database Setup (Step-by-Step for Beginners)

You already created your Supabase project named `app Project` and have the SQL Editor open. Here is how to complete the setup in under 2 minutes:

### Step 1: Run the Database Setup Script
1. In your **Supabase Dashboard**, click on **SQL Editor** in the left sidebar menu (terminal icon `>_`).
2. Click **"+ New query"** at the top.
3. Open the file `supabase_setup.sql` in this project.
4. Copy its entire content, paste it into the Supabase SQL Editor, and click the green **"RUN"** button.
5. You will see `Success. No rows returned`. All 12 tables, Row Level Security (RLS) policies, all 88 chapters, 16 routine blocks, and default rewards are now generated!

### Step 2: Get your Project URL and Public Anon Key
1. In Supabase, click on the **Project Settings (gear icon ⚙️)** at the bottom of the left sidebar.
2. Under Project Settings, click on **Data API** (or **API**).
3. Find:
   - **Project URL**: (e.g., `https://xyzabcdefghijklm.supabase.co`)
   - **Project API Keys**: Copy the **`anon` `public`** key (starts with `eyJhbGciOi...`).
   *(Never share or use the service_role key).*

### Step 3: Connect the App
1. Open the Two Hearts Study app on your phone.
2. Tap the **Settings icon (⚙️)** in the top right.
3. Paste your **Project URL** and **Anon Public Key**.
4. Tap **"Save & Test"**.
5. The badge will turn green: `🟢 Cloud Connected & Synced`!

---

## 📱 Building the APK on Windows Laptop

If you export this project to your Windows laptop:

1. Install [Android Studio](https://developer.android.com/studio) or Java JDK 17+.
2. Open the project folder in Android Studio or Command Prompt (`cmd`).
3. To build the debug APK:
   ```cmd
   gradlew assembleDebug
   ```
4. Your installable APK will be at:
   ```
   app\build\outputs\apk\debug\app-debug.apk
   ```
5. Transfer `app-debug.apk` to both your phone and Surbie's phone and tap to install!
