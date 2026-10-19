package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class TwoHeartsRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("two_hearts_prefs", Context.MODE_PRIVATE)
    val supabase = SupabaseClient()
    private val scope = CoroutineScope(Dispatchers.IO)

    // Current User
    private val _currentUser = MutableStateFlow(
        UserProfile(
            id = "user_surbie",
            username = "surbie",
            role = UserRole.STUDENT,
            displayName = "Surbie 🌸",
            totalPoints = 185
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    // Subjects & Chapters
    private val _subjects = MutableStateFlow<List<Subject>>(SeedData.subjects)
    val subjects: StateFlow<List<Subject>> = _subjects.asStateFlow()

    private val _chapters = MutableStateFlow<List<Chapter>>(SeedData.defaultChapters)
    val chapters: StateFlow<List<Chapter>> = _chapters.asStateFlow()

    // Routine
    private val _routine = MutableStateFlow<List<RoutineBlock>>(SeedData.defaultRoutine)
    val routine: StateFlow<List<RoutineBlock>> = _routine.asStateFlow()

    private val _completedRoutineIdsToday = MutableStateFlow<Set<String>>(setOf("rt_1", "rt_2"))
    val completedRoutineIdsToday: StateFlow<Set<String>> = _completedRoutineIdsToday.asStateFlow()

    // Study Sessions & Focus
    private val _studySessions = MutableStateFlow<List<StudySession>>(emptyList())
    val studySessions: StateFlow<List<StudySession>> = _studySessions.asStateFlow()

    private val _todayFocusedSeconds = MutableStateFlow(2 * 3600L + 45 * 60L) // 2h 45m initial
    val todayFocusedSeconds: StateFlow<Long> = _todayFocusedSeconds.asStateFlow()

    private val _dailyGoalHours = MutableStateFlow(12.0f)
    val dailyGoalHours: StateFlow<Float> = _dailyGoalHours.asStateFlow()

    // Analytics: Weekly Study Hours
    private val _weeklyStudyHours = MutableStateFlow<Map<String, Float>>(
        linkedMapOf(
            "Mon" to 10.5f,
            "Tue" to 11.2f,
            "Wed" to 12.0f,
            "Thu" to 9.8f,
            "Fri" to 11.5f,
            "Sat" to 12.2f,
            "Today" to 2.75f
        )
    )
    val weeklyStudyHours: StateFlow<Map<String, Float>> = _weeklyStudyHours.asStateFlow()

    // Analytics: 30-Day Streak Active Days in Current Month
    private val _activeStreakDays = MutableStateFlow<Set<Int>>(
        setOf(1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18)
    )
    val activeStreakDays: StateFlow<Set<Int>> = _activeStreakDays.asStateFlow()

    // Ambient Focus Motivational Quotes
    val ambientQuotes: List<String> = listOf(
        "Take a slow, deep breath. Every formula and chapter brings us closer to our dreams ❤️",
        "Your dedication today is carving a masterpiece for tomorrow. Keep shining, my love ✨",
        "One concept at a time. I believe in you with all my heart 🌸",
        "Focus on the process, not just the summit. You are already making me so proud 🌟",
        "Quiet mind, unstoppable focus. You have the strength of two hearts beating as one 💫"
    )

    // Points & Ledger
    private val _totalPoints = MutableStateFlow(185)
    val totalPoints: StateFlow<Int> = _totalPoints.asStateFlow()

    private val _pointsHistory = MutableStateFlow<List<PointsTransaction>>(
        listOf(
            PointsTransaction(
                id = "tx_01",
                userId = "user_surbie",
                amount = 10,
                eventType = "WAKE_UP",
                reason = "Wake up on schedule (06:00 AM)",
                idempotencyKey = "wake_up_${todayDateString()}",
                createdByRole = UserRole.STUDENT,
                timestamp = "06:05 AM"
            ),
            PointsTransaction(
                id = "tx_02",
                userId = "user_surbie",
                amount = 15,
                eventType = "STUDY_BLOCK",
                reason = "Completed Morning Deep Study Block 1 (2 hrs)",
                idempotencyKey = "block_rt_2_${todayDateString()}",
                createdByRole = UserRole.STUDENT,
                timestamp = "08:15 AM"
            ),
            PointsTransaction(
                id = "tx_03",
                userId = "user_surbie",
                amount = 50,
                eventType = "ADMIN_BONUS",
                reason = "Excellent kinematics practice notes! Proud of you ❤️",
                idempotencyKey = "admin_bonus_kinematics",
                createdByRole = UserRole.ADMIN,
                timestamp = "Yesterday"
            ),
            PointsTransaction(
                id = "tx_04",
                userId = "user_surbie",
                amount = 100,
                eventType = "MILESTONE_STREAK",
                reason = "7-Day Dedication Streak Achieved! 🎉",
                idempotencyKey = "ms_s7_unlock",
                createdByRole = UserRole.ADMIN,
                timestamp = "3 days ago"
            ),
            PointsTransaction(
                id = "tx_05",
                userId = "user_surbie",
                amount = 10,
                eventType = "FOCUSED_HOUR",
                reason = "Focused study hour completed in Physics",
                idempotencyKey = "foc_hr_01",
                createdByRole = UserRole.STUDENT,
                timestamp = "Yesterday"
            )
        )
    )
    val pointsHistory: StateFlow<List<PointsTransaction>> = _pointsHistory.asStateFlow()

    // Rewards & Requests
    private val _rewards = MutableStateFlow<List<Reward>>(SeedData.defaultRewards)
    val rewards: StateFlow<List<Reward>> = _rewards.asStateFlow()

    private val _rewardRequests = MutableStateFlow<List<RewardRequest>>(
        listOf(
            RewardRequest(
                id = "req_01",
                rewardId = "rew_2",
                rewardTitle = "Sweet Voice Note",
                pointsCost = 150,
                status = RewardStatus.FULFILLED,
                studentNote = "Could use a cute voice note after organic chemistry! 🥺",
                adminNote = "Sent via chat with lots of love ❤️",
                requestedAt = "2 days ago",
                reviewedAt = "2 days ago"
            )
        )
    )
    val rewardRequests: StateFlow<List<RewardRequest>> = _rewardRequests.asStateFlow()

    // Milestones
    private val _milestones = MutableStateFlow<List<Milestone>>(
        SeedData.defaultMilestones.map {
            if (it.id == "ms_s7") it.copy(isUnlocked = true, unlockedAt = "3 days ago")
            else it
        }
    )
    val milestones: StateFlow<List<Milestone>> = _milestones.asStateFlow()

    // Love Messages
    private val _loveMessages = MutableStateFlow<List<LoveMessage>>(
        listOf(
            LoveMessage(
                id = "msg_1",
                message = "Good morning my champion! Remember to take deep breaths between Physics chapters. You're going to ace this syllabus, step by step! ❤️",
                senderName = "Admin",
                timestamp = "Today, 06:10 AM",
                isPinned = true
            ),
            LoveMessage(
                id = "msg_2",
                message = "Every 15 minutes of hard focus brings you closer to your dreams. So proud of your discipline today!",
                senderName = "Admin",
                timestamp = "Yesterday"
            )
        )
    )
    val loveMessages: StateFlow<List<LoveMessage>> = _loveMessages.asStateFlow()

    // Streaks
    private val _currentStreakDays = MutableStateFlow(8)
    val currentStreakDays: StateFlow<Int> = _currentStreakDays.asStateFlow()

    // 10 Features State Flows
    private val _flashcards = MutableStateFlow<List<Flashcard>>(SeedData.defaultFlashcards)
    val flashcards: StateFlow<List<Flashcard>> = _flashcards.asStateFlow()

    private val _doubts = MutableStateFlow<List<DoubtEntry>>(SeedData.defaultDoubts)
    val doubts: StateFlow<List<DoubtEntry>> = _doubts.asStateFlow()

    private val _examTarget = MutableStateFlow<ExamTarget>(SeedData.defaultExamTarget)
    val examTarget: StateFlow<ExamTarget> = _examTarget.asStateFlow()

    private val _todayMood = MutableStateFlow<MoodCheckIn?>(null)
    val todayMood: StateFlow<MoodCheckIn?> = _todayMood.asStateFlow()

    private val _nudges = MutableStateFlow<List<AdminNudge>>(SeedData.defaultNudges)
    val nudges: StateFlow<List<AdminNudge>> = _nudges.asStateFlow()

    private val _mysteryGifts = MutableStateFlow<List<MysteryGift>>(SeedData.defaultMysteryGifts)
    val mysteryGifts: StateFlow<List<MysteryGift>> = _mysteryGifts.asStateFlow()

    private val _studyQuests = MutableStateFlow<List<StudyQuest>>(SeedData.defaultQuests)
    val studyQuests: StateFlow<List<StudyQuest>> = _studyQuests.asStateFlow()

    private val _soundscapes = MutableStateFlow<List<SoundscapeTrack>>(SeedData.defaultSoundscapes)
    val soundscapes: StateFlow<List<SoundscapeTrack>> = _soundscapes.asStateFlow()

    // Sync State
    private val _syncState = MutableStateFlow(SyncState.LOCAL_CACHE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _lastSyncMessage = MutableStateFlow<String?>(null)
    val lastSyncMessage: StateFlow<String?> = _lastSyncMessage.asStateFlow()

    fun getSavedUrl(): String = prefs.getString("supabase_url", "") ?: ""
    fun getSavedKey(): String = prefs.getString("supabase_key", "") ?: ""

    init {
        loadLocalState()
        loadSavedConfig()
    }

    private fun loadLocalState() {
        loadPoints()
        loadChapters()
        loadRoutine()
        loadStudy()
        loadMilestones()
        loadRewards()
        loadLoveMessages()
        loadDailyGoal()
        loadCurrentUser()
        loadFlashcards()
        loadDoubts()
        loadExamTarget()
        loadMood()
        loadNudges()
        loadMysteryGifts()
        loadQuests()
    }

    // --- Local Persistence & State Loading ---

    fun persistPoints() {
        try {
            val array = JSONArray()
            for (tx in _pointsHistory.value) {
                val obj = JSONObject().apply {
                    put("id", tx.id)
                    put("userId", tx.userId)
                    put("amount", tx.amount)
                    put("eventType", tx.eventType)
                    put("reason", tx.reason)
                    put("idempotencyKey", tx.idempotencyKey)
                    put("createdByRole", tx.createdByRole.name)
                    put("timestamp", tx.timestamp)
                }
                array.put(obj)
            }
            prefs.edit()
                .putInt("local_total_points", _totalPoints.value)
                .putString("local_points_history", array.toString())
                .apply()
        } catch (e: Exception) {
            Log.e("TwoHeartsRepository", "Failed to persist points", e)
        }
    }

    private fun loadPoints() {
        if (prefs.contains("local_total_points")) {
            _totalPoints.value = prefs.getInt("local_total_points", 185)
            val historyStr = prefs.getString("local_points_history", null)
            if (!historyStr.isNullOrBlank()) {
                try {
                    val array = JSONArray(historyStr)
                    val list = mutableListOf<PointsTransaction>()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        list.add(
                            PointsTransaction(
                                id = obj.optString("id", UUID.randomUUID().toString()),
                                userId = obj.optString("userId", "user_surbie"),
                                amount = obj.optInt("amount", 0),
                                eventType = obj.optString("eventType", "POINT_EVENT"),
                                reason = obj.optString("reason", ""),
                                idempotencyKey = obj.optString("idempotencyKey", ""),
                                createdByRole = try {
                                    UserRole.valueOf(obj.optString("createdByRole", "STUDENT"))
                                } catch (_: Exception) {
                                    UserRole.STUDENT
                                },
                                timestamp = obj.optString("timestamp", "")
                            )
                        )
                    }
                    _pointsHistory.value = list
                } catch (e: Exception) {
                    Log.e("TwoHeartsRepository", "Failed to parse saved points history", e)
                }
            }
        } else {
            persistPoints()
        }
    }

    fun persistChapters() {
        try {
            val array = JSONArray()
            for (ch in _chapters.value) {
                val obj = JSONObject().apply {
                    put("id", ch.id)
                    put("subjectId", ch.subjectId)
                    put("chapterNumber", ch.chapterNumber)
                    put("title", ch.title)
                    put("status", ch.status.name)
                    if (ch.completedAt != null) put("completedAt", ch.completedAt)
                    put("notes", ch.notes)
                    put("updatedAt", ch.updatedAt)
                }
                array.put(obj)
            }
            prefs.edit()
                .putString("local_chapters", array.toString())
                .apply()
        } catch (e: Exception) {
            Log.e("TwoHeartsRepository", "Failed to persist chapters", e)
        }
    }

    private fun loadChapters() {
        val saved = prefs.getString("local_chapters", null)
        if (!saved.isNullOrBlank()) {
            try {
                val array = JSONArray(saved)
                val list = mutableListOf<Chapter>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val statusStr = obj.optString("status", "NOT_STARTED")
                    val status = try {
                        ChapterStatus.valueOf(statusStr)
                    } catch (_: Exception) {
                        ChapterStatus.NOT_STARTED
                    }
                    list.add(
                        Chapter(
                            id = obj.optString("id"),
                            subjectId = obj.optString("subjectId"),
                            chapterNumber = obj.optInt("chapterNumber"),
                            title = obj.optString("title"),
                            status = status,
                            completedAt = if (obj.has("completedAt") && !obj.isNull("completedAt")) obj.getString("completedAt") else null,
                            notes = obj.optString("notes", ""),
                            updatedAt = obj.optString("updatedAt", "")
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    _chapters.value = list
                    return
                }
            } catch (e: Exception) {
                Log.e("TwoHeartsRepository", "Failed to parse local chapters", e)
            }
        }
        persistChapters()
    }

    fun persistRoutine() {
        try {
            val array = JSONArray()
            for (id in _completedRoutineIdsToday.value) {
                array.put(id)
            }
            prefs.edit()
                .putString("local_routine_date", todayDateString())
                .putString("local_completed_routine", array.toString())
                .apply()
        } catch (e: Exception) {
            Log.e("TwoHeartsRepository", "Failed to persist routine", e)
        }
    }

    private fun loadRoutine() {
        val savedDate = prefs.getString("local_routine_date", "") ?: ""
        val today = todayDateString()
        if (savedDate == today) {
            val saved = prefs.getString("local_completed_routine", null)
            if (!saved.isNullOrBlank()) {
                try {
                    val array = JSONArray(saved)
                    val set = mutableSetOf<String>()
                    for (i in 0 until array.length()) {
                        set.add(array.getString(i))
                    }
                    _completedRoutineIdsToday.value = set
                    return
                } catch (e: Exception) {
                    Log.e("TwoHeartsRepository", "Failed to parse routine", e)
                }
            }
        } else if (savedDate.isNotBlank()) {
            // New day: reset routine completions for the new day
            _completedRoutineIdsToday.value = emptySet()
            persistRoutine()
        }
    }

    fun persistStudy() {
        try {
            val sessionsArray = JSONArray()
            for (s in _studySessions.value) {
                val obj = JSONObject().apply {
                    put("id", s.id)
                    put("userId", s.userId)
                    if (s.subjectId != null) put("subjectId", s.subjectId)
                    if (s.chapterId != null) put("chapterId", s.chapterId)
                    put("subjectName", s.subjectName)
                    put("chapterTitle", s.chapterTitle)
                    put("durationSeconds", s.durationSeconds)
                    put("startedAt", s.startedAt)
                    put("endedAt", s.endedAt)
                    put("notes", s.notes)
                    put("pointsAwarded", s.pointsAwarded)
                }
                sessionsArray.put(obj)
            }

            val weeklyObj = JSONObject()
            for ((k, v) in _weeklyStudyHours.value) {
                weeklyObj.put(k, v.toDouble())
            }

            val streakArray = JSONArray()
            for (day in _activeStreakDays.value) {
                streakArray.put(day)
            }

            prefs.edit()
                .putString("local_study_sessions", sessionsArray.toString())
                .putLong("local_today_focused_seconds", _todayFocusedSeconds.value)
                .putString("local_today_focused_date", todayDateString())
                .putFloat("local_daily_goal_hours", _dailyGoalHours.value)
                .putString("local_weekly_study_hours", weeklyObj.toString())
                .putString("local_active_streak_days", streakArray.toString())
                .putInt("local_current_streak_days", _currentStreakDays.value)
                .apply()
        } catch (e: Exception) {
            Log.e("TwoHeartsRepository", "Failed to persist study", e)
        }
    }

    private fun loadStudy() {
        val savedDate = prefs.getString("local_today_focused_date", "") ?: ""
        val today = todayDateString()
        if (savedDate == today) {
            _todayFocusedSeconds.value = prefs.getLong("local_today_focused_seconds", _todayFocusedSeconds.value)
        } else if (savedDate.isNotBlank()) {
            _todayFocusedSeconds.value = 0L
        }

        _dailyGoalHours.value = prefs.getFloat("local_daily_goal_hours", _dailyGoalHours.value)
        _currentStreakDays.value = prefs.getInt("local_current_streak_days", _currentStreakDays.value)

        val sessionsStr = prefs.getString("local_study_sessions", null)
        if (!sessionsStr.isNullOrBlank()) {
            try {
                val array = JSONArray(sessionsStr)
                val list = mutableListOf<StudySession>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        StudySession(
                            id = obj.optString("id"),
                            userId = obj.optString("userId"),
                            subjectId = if (obj.has("subjectId")) obj.optString("subjectId") else null,
                            chapterId = if (obj.has("chapterId")) obj.optString("chapterId") else null,
                            subjectName = obj.optString("subjectName", "Study"),
                            chapterTitle = obj.optString("chapterTitle", "Chapter"),
                            durationSeconds = obj.optLong("durationSeconds", 0L),
                            startedAt = obj.optString("startedAt", ""),
                            endedAt = obj.optString("endedAt", ""),
                            notes = obj.optString("notes", ""),
                            pointsAwarded = obj.optInt("pointsAwarded", 0)
                        )
                    )
                }
                _studySessions.value = list
            } catch (e: Exception) {
                Log.e("TwoHeartsRepository", "Failed to parse study sessions", e)
            }
        }

        val weeklyStr = prefs.getString("local_weekly_study_hours", null)
        if (!weeklyStr.isNullOrBlank()) {
            try {
                val obj = JSONObject(weeklyStr)
                val map = linkedMapOf<String, Float>()
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    map[k] = obj.getDouble(k).toFloat()
                }
                _weeklyStudyHours.value = map
            } catch (e: Exception) {
                Log.e("TwoHeartsRepository", "Failed to parse weekly study hours", e)
            }
        }

        val streakStr = prefs.getString("local_active_streak_days", null)
        if (!streakStr.isNullOrBlank()) {
            try {
                val array = JSONArray(streakStr)
                val set = mutableSetOf<Int>()
                for (i in 0 until array.length()) {
                    set.add(array.getInt(i))
                }
                _activeStreakDays.value = set
            } catch (e: Exception) {
                Log.e("TwoHeartsRepository", "Failed to parse streak days", e)
            }
        }
    }

    fun persistMilestones() {
        try {
            val array = JSONArray()
            for (m in _milestones.value) {
                val obj = JSONObject().apply {
                    put("id", m.id)
                    put("title", m.title)
                    put("category", m.category.name)
                    put("targetCount", m.targetCount)
                    put("pointsReward", m.pointsReward)
                    put("isUnlocked", m.isUnlocked)
                    if (m.unlockedAt != null) put("unlockedAt", m.unlockedAt)
                }
                array.put(obj)
            }
            prefs.edit().putString("local_milestones", array.toString()).apply()
        } catch (e: Exception) {
            Log.e("TwoHeartsRepository", "Failed to persist milestones", e)
        }
    }

    private fun loadMilestones() {
        val saved = prefs.getString("local_milestones", null)
        if (!saved.isNullOrBlank()) {
            try {
                val array = JSONArray(saved)
                val list = mutableListOf<Milestone>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        Milestone(
                            id = obj.optString("id"),
                            title = obj.optString("title"),
                            category = try {
                                MilestoneCategory.valueOf(obj.optString("category", "CHAPTERS"))
                            } catch (_: Exception) {
                                MilestoneCategory.CHAPTERS
                            },
                            targetCount = obj.optInt("targetCount"),
                            pointsReward = obj.optInt("pointsReward"),
                            isUnlocked = obj.optBoolean("isUnlocked", false),
                            unlockedAt = if (obj.has("unlockedAt") && !obj.isNull("unlockedAt")) obj.getString("unlockedAt") else null
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    _milestones.value = list
                    return
                }
            } catch (e: Exception) {
                Log.e("TwoHeartsRepository", "Failed to parse milestones", e)
            }
        }
        persistMilestones()
    }

    fun persistRewards() {
        try {
            val array = JSONArray()
            for (r in _rewardRequests.value) {
                val obj = JSONObject().apply {
                    put("id", r.id)
                    put("rewardId", r.rewardId)
                    put("rewardTitle", r.rewardTitle)
                    put("pointsCost", r.pointsCost)
                    put("status", r.status.name)
                    put("studentNote", r.studentNote)
                    put("adminNote", r.adminNote)
                    put("requestedAt", r.requestedAt)
                    if (r.reviewedAt != null) put("reviewedAt", r.reviewedAt)
                }
                array.put(obj)
            }
            prefs.edit().putString("local_reward_requests", array.toString()).apply()
        } catch (e: Exception) {
            Log.e("TwoHeartsRepository", "Failed to persist rewards", e)
        }
    }

    private fun loadRewards() {
        val saved = prefs.getString("local_reward_requests", null)
        if (!saved.isNullOrBlank()) {
            try {
                val array = JSONArray(saved)
                val list = mutableListOf<RewardRequest>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        RewardRequest(
                            id = obj.optString("id"),
                            rewardId = obj.optString("rewardId"),
                            rewardTitle = obj.optString("rewardTitle"),
                            pointsCost = obj.optInt("pointsCost"),
                            status = try {
                                RewardStatus.valueOf(obj.optString("status", "PENDING"))
                            } catch (_: Exception) {
                                RewardStatus.PENDING
                            },
                            studentNote = obj.optString("studentNote", ""),
                            adminNote = obj.optString("adminNote", ""),
                            requestedAt = obj.optString("requestedAt", ""),
                            reviewedAt = if (obj.has("reviewedAt") && !obj.isNull("reviewedAt")) obj.getString("reviewedAt") else null
                        )
                    )
                }
                _rewardRequests.value = list
            } catch (e: Exception) {
                Log.e("TwoHeartsRepository", "Failed to parse rewards", e)
            }
        }
    }

    fun persistLoveMessages() {
        try {
            val array = JSONArray()
            for (m in _loveMessages.value) {
                val obj = JSONObject().apply {
                    put("id", m.id)
                    put("message", m.message)
                    put("senderName", m.senderName)
                    put("timestamp", m.timestamp)
                    put("isPinned", m.isPinned)
                }
                array.put(obj)
            }
            prefs.edit().putString("local_love_messages", array.toString()).apply()
        } catch (e: Exception) {
            Log.e("TwoHeartsRepository", "Failed to persist love messages", e)
        }
    }

    private fun loadLoveMessages() {
        val saved = prefs.getString("local_love_messages", null)
        if (!saved.isNullOrBlank()) {
            try {
                val array = JSONArray(saved)
                val list = mutableListOf<LoveMessage>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        LoveMessage(
                            id = obj.optString("id"),
                            message = obj.optString("message"),
                            senderName = obj.optString("senderName", "Admin"),
                            timestamp = obj.optString("timestamp", ""),
                            isPinned = obj.optBoolean("isPinned", false)
                        )
                    )
                }
                _loveMessages.value = list
            } catch (e: Exception) {
                Log.e("TwoHeartsRepository", "Failed to parse love messages", e)
            }
        }
    }

    fun persistDailyGoal() {
        prefs.edit().putFloat("local_daily_goal_hours", _dailyGoalHours.value).apply()
    }

    private fun loadDailyGoal() {
        _dailyGoalHours.value = prefs.getFloat("local_daily_goal_hours", _dailyGoalHours.value)
    }

    fun persistCurrentUser() {
        try {
            val u = _currentUser.value
            val obj = JSONObject().apply {
                put("id", u.id)
                put("username", u.username)
                put("role", u.role.name)
                put("displayName", u.displayName)
                put("totalPoints", _totalPoints.value)
            }
            prefs.edit().putString("local_current_user", obj.toString()).apply()
        } catch (e: Exception) {
            Log.e("TwoHeartsRepository", "Failed to persist current user", e)
        }
    }

    private fun loadCurrentUser() {
        val saved = prefs.getString("local_current_user", null)
        if (!saved.isNullOrBlank()) {
            try {
                val obj = JSONObject(saved)
                val role = try {
                    UserRole.valueOf(obj.optString("role", "STUDENT"))
                } catch (_: Exception) {
                    UserRole.STUDENT
                }
                _currentUser.value = UserProfile(
                    id = obj.optString("id", "user_surbie"),
                    username = obj.optString("username", "surbie"),
                    role = role,
                    displayName = obj.optString("displayName", if (role == UserRole.ADMIN) "Admin 👑" else "Surbie 🌸"),
                    totalPoints = _totalPoints.value
                )
            } catch (e: Exception) {
                Log.e("TwoHeartsRepository", "Failed to parse current user", e)
            }
        }
    }

    // --- Flashcards Persistence ---
    fun persistFlashcards() {
        try {
            val array = JSONArray()
            for (fc in _flashcards.value) {
                val obj = JSONObject().apply {
                    put("id", fc.id)
                    put("subjectId", fc.subjectId)
                    put("chapterTitle", fc.chapterTitle)
                    put("frontQuestion", fc.frontQuestion)
                    put("backAnswer", fc.backAnswer)
                    put("mastery", fc.mastery.name)
                    put("lastReviewedAt", fc.lastReviewedAt)
                }
                array.put(obj)
            }
            prefs.edit().putString("local_flashcards", array.toString()).apply()
        } catch (e: Exception) {
            Log.e("TwoHeartsRepository", "Failed to persist flashcards", e)
        }
    }

    private fun loadFlashcards() {
        val saved = prefs.getString("local_flashcards", null)
        if (!saved.isNullOrBlank()) {
            try {
                val array = JSONArray(saved)
                val list = mutableListOf<Flashcard>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        Flashcard(
                            id = obj.optString("id"),
                            subjectId = obj.optString("subjectId"),
                            chapterTitle = obj.optString("chapterTitle"),
                            frontQuestion = obj.optString("frontQuestion"),
                            backAnswer = obj.optString("backAnswer"),
                            mastery = try {
                                FlashcardMastery.valueOf(obj.optString("mastery", "NEED_REVIEW"))
                            } catch (_: Exception) {
                                FlashcardMastery.NEED_REVIEW
                            },
                            lastReviewedAt = obj.optString("lastReviewedAt")
                        )
                    )
                }
                if (list.isNotEmpty()) _flashcards.value = list
            } catch (e: Exception) {
                Log.e("TwoHeartsRepository", "Failed to load flashcards", e)
            }
        }
    }

    // --- Doubts Persistence ---
    fun persistDoubts() {
        try {
            val array = JSONArray()
            for (d in _doubts.value) {
                val obj = JSONObject().apply {
                    put("id", d.id)
                    put("subjectId", d.subjectId)
                    put("subjectName", d.subjectName)
                    put("chapterTitle", d.chapterTitle)
                    put("questionText", d.questionText)
                    put("adminSolution", d.adminSolution)
                    put("isResolved", d.isResolved)
                    put("createdAt", d.createdAt)
                    if (d.resolvedAt != null) put("resolvedAt", d.resolvedAt)
                }
                array.put(obj)
            }
            prefs.edit().putString("local_doubts", array.toString()).apply()
        } catch (e: Exception) {
            Log.e("TwoHeartsRepository", "Failed to persist doubts", e)
        }
    }

    private fun loadDoubts() {
        val saved = prefs.getString("local_doubts", null)
        if (!saved.isNullOrBlank()) {
            try {
                val array = JSONArray(saved)
                val list = mutableListOf<DoubtEntry>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        DoubtEntry(
                            id = obj.optString("id"),
                            subjectId = obj.optString("subjectId"),
                            subjectName = obj.optString("subjectName"),
                            chapterTitle = obj.optString("chapterTitle"),
                            questionText = obj.optString("questionText"),
                            adminSolution = obj.optString("adminSolution", ""),
                            isResolved = obj.optBoolean("isResolved", false),
                            createdAt = obj.optString("createdAt"),
                            resolvedAt = if (obj.has("resolvedAt") && !obj.isNull("resolvedAt")) obj.getString("resolvedAt") else null
                        )
                    )
                }
                if (list.isNotEmpty()) _doubts.value = list
            } catch (e: Exception) {
                Log.e("TwoHeartsRepository", "Failed to load doubts", e)
            }
        }
    }

    // --- Exam Target Persistence ---
    fun persistExamTarget() {
        try {
            val et = _examTarget.value
            val obj = JSONObject().apply {
                put("id", et.id)
                put("title", et.title)
                put("examDateStr", et.examDateStr)
                put("daysRemaining", et.daysRemaining)
                put("targetChaptersPerWeek", et.targetChaptersPerWeek)
                put("motivationalQuote", et.motivationalQuote)
            }
            prefs.edit().putString("local_exam_target", obj.toString()).apply()
        } catch (e: Exception) {
            Log.e("TwoHeartsRepository", "Failed to persist exam target", e)
        }
    }

    private fun loadExamTarget() {
        val saved = prefs.getString("local_exam_target", null)
        if (!saved.isNullOrBlank()) {
            try {
                val obj = JSONObject(saved)
                _examTarget.value = ExamTarget(
                    id = obj.optString("id", "exam_neet_2026"),
                    title = obj.optString("title", "NEET 2026 Medical Entrance"),
                    examDateStr = obj.optString("examDateStr", "May 3, 2026"),
                    daysRemaining = obj.optInt("daysRemaining", 195),
                    targetChaptersPerWeek = obj.optInt("targetChaptersPerWeek", 4),
                    motivationalQuote = obj.optString("motivationalQuote", SeedData.defaultExamTarget.motivationalQuote)
                )
            } catch (e: Exception) {
                Log.e("TwoHeartsRepository", "Failed to load exam target", e)
            }
        }
    }

    // --- Mood Persistence ---
    fun persistMood() {
        try {
            val m = _todayMood.value
            if (m != null) {
                val obj = JSONObject().apply {
                    put("id", m.id)
                    put("emoji", m.emoji)
                    put("moodLabel", m.moodLabel)
                    put("energyPercent", m.energyPercent)
                    put("date", m.date)
                    put("adminHeartReaction", m.adminHeartReaction)
                }
                prefs.edit().putString("local_today_mood", obj.toString()).apply()
            } else {
                prefs.edit().remove("local_today_mood").apply()
            }
        } catch (e: Exception) {
            Log.e("TwoHeartsRepository", "Failed to persist mood", e)
        }
    }

    private fun loadMood() {
        val saved = prefs.getString("local_today_mood", null)
        if (!saved.isNullOrBlank()) {
            try {
                val obj = JSONObject(saved)
                val date = obj.optString("date")
                if (date == todayDateString()) {
                    _todayMood.value = MoodCheckIn(
                        id = obj.optString("id"),
                        emoji = obj.optString("emoji"),
                        moodLabel = obj.optString("moodLabel"),
                        energyPercent = obj.optInt("energyPercent", 80),
                        date = date,
                        adminHeartReaction = obj.optBoolean("adminHeartReaction", false)
                    )
                }
            } catch (e: Exception) {
                Log.e("TwoHeartsRepository", "Failed to load mood", e)
            }
        }
    }

    // --- Nudges Persistence ---
    fun persistNudges() {
        try {
            val array = JSONArray()
            for (n in _nudges.value) {
                val obj = JSONObject().apply {
                    put("id", n.id)
                    put("type", n.type)
                    put("message", n.message)
                    put("senderName", n.senderName)
                    put("timestamp", n.timestamp)
                    put("isAcknowledged", n.isAcknowledged)
                }
                array.put(obj)
            }
            prefs.edit().putString("local_nudges", array.toString()).apply()
        } catch (e: Exception) {
            Log.e("TwoHeartsRepository", "Failed to persist nudges", e)
        }
    }

    private fun loadNudges() {
        val saved = prefs.getString("local_nudges", null)
        if (!saved.isNullOrBlank()) {
            try {
                val array = JSONArray(saved)
                val list = mutableListOf<AdminNudge>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        AdminNudge(
                            id = obj.optString("id"),
                            type = obj.optString("type", "HUG"),
                            message = obj.optString("message"),
                            senderName = obj.optString("senderName", "Zaid"),
                            timestamp = obj.optString("timestamp"),
                            isAcknowledged = obj.optBoolean("isAcknowledged", false)
                        )
                    )
                }
                if (list.isNotEmpty()) _nudges.value = list
            } catch (e: Exception) {
                Log.e("TwoHeartsRepository", "Failed to load nudges", e)
            }
        }
    }

    // --- Mystery Gifts Persistence ---
    fun persistMysteryGifts() {
        try {
            val array = JSONArray()
            for (g in _mysteryGifts.value) {
                val obj = JSONObject().apply {
                    put("id", g.id)
                    put("title", g.title)
                    put("message", g.message)
                    put("pointsAmount", g.pointsAmount)
                    put("perkDescription", g.perkDescription)
                    put("isClaimed", g.isClaimed)
                    if (g.claimedAt != null) put("claimedAt", g.claimedAt)
                }
                array.put(obj)
            }
            prefs.edit().putString("local_mystery_gifts", array.toString()).apply()
        } catch (e: Exception) {
            Log.e("TwoHeartsRepository", "Failed to persist mystery gifts", e)
        }
    }

    private fun loadMysteryGifts() {
        val saved = prefs.getString("local_mystery_gifts", null)
        if (!saved.isNullOrBlank()) {
            try {
                val array = JSONArray(saved)
                val list = mutableListOf<MysteryGift>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        MysteryGift(
                            id = obj.optString("id"),
                            title = obj.optString("title"),
                            message = obj.optString("message"),
                            pointsAmount = obj.optInt("pointsAmount", 100),
                            perkDescription = obj.optString("perkDescription"),
                            isClaimed = obj.optBoolean("isClaimed", false),
                            claimedAt = if (obj.has("claimedAt") && !obj.isNull("claimedAt")) obj.getString("claimedAt") else null
                        )
                    )
                }
                if (list.isNotEmpty()) _mysteryGifts.value = list
            } catch (e: Exception) {
                Log.e("TwoHeartsRepository", "Failed to load mystery gifts", e)
            }
        }
    }

    // --- Quests Persistence ---
    fun persistQuests() {
        try {
            val array = JSONArray()
            for (q in _studyQuests.value) {
                val obj = JSONObject().apply {
                    put("id", q.id)
                    put("title", q.title)
                    put("description", q.description)
                    put("targetType", q.targetType)
                    put("targetGoal", q.targetGoal)
                    put("currentProgress", q.currentProgress)
                    put("pointsBounty", q.pointsBounty)
                    put("deadline", q.deadline)
                    put("isCompleted", q.isCompleted)
                }
                array.put(obj)
            }
            prefs.edit().putString("local_study_quests", array.toString()).apply()
        } catch (e: Exception) {
            Log.e("TwoHeartsRepository", "Failed to persist quests", e)
        }
    }

    private fun loadQuests() {
        val saved = prefs.getString("local_study_quests", null)
        if (!saved.isNullOrBlank()) {
            try {
                val array = JSONArray(saved)
                val list = mutableListOf<StudyQuest>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        StudyQuest(
                            id = obj.optString("id"),
                            title = obj.optString("title"),
                            description = obj.optString("description"),
                            targetType = obj.optString("targetType"),
                            targetGoal = obj.optInt("targetGoal", 3),
                            currentProgress = obj.optInt("currentProgress", 0),
                            pointsBounty = obj.optInt("pointsBounty", 100),
                            deadline = obj.optString("deadline"),
                            isCompleted = obj.optBoolean("isCompleted", false)
                        )
                    )
                }
                if (list.isNotEmpty()) _studyQuests.value = list
            } catch (e: Exception) {
                Log.e("TwoHeartsRepository", "Failed to load quests", e)
            }
        }
    }

    private fun loadSavedConfig() {
        val rawUrl = prefs.getString("supabase_url", "") ?: ""
        val rawKey = prefs.getString("supabase_key", "") ?: ""
        val savedToken = prefs.getString("supabase_token", "") ?: ""
        val savedUserId = prefs.getString("supabase_user_id", "") ?: ""

        val savedUrl = SupabaseClient.normalizeUrl(rawUrl)
        val savedKey = SupabaseClient.sanitizeKey(rawKey)

        if (savedUrl.isNotBlank() && savedKey.isNotBlank()) {
            supabase.configure(savedUrl, savedKey, savedToken, savedUserId)
            _syncState.value = SyncState.SYNCING
            scope.launch {
                val (pingOk, msg) = supabase.ping()
                _lastSyncMessage.value = msg
                if (pingOk) {
                    _syncState.value = SyncState.CONNECTED
                    syncWithCloud()
                } else {
                    _syncState.value = SyncState.ERROR
                }
            }
        } else {
            _syncState.value = SyncState.LOCAL_CACHE
        }
    }

    suspend fun saveAndTestSupabaseConfig(url: String, key: String): Pair<Boolean, String> = kotlinx.coroutines.withContext(Dispatchers.IO) {
        val normUrl = SupabaseClient.normalizeUrl(url)
        val cleanKey = SupabaseClient.sanitizeKey(key)

        if (normUrl.isBlank() || cleanKey.isBlank()) {
            val err = "Project URL and Anon Key cannot be blank."
            _lastSyncMessage.value = err
            _syncState.value = SyncState.LOCAL_CACHE
            return@withContext Pair(false, err)
        }

        // Persist clean normalized credentials immediately
        prefs.edit()
            .putString("supabase_url", normUrl)
            .putString("supabase_key", cleanKey)
            .apply()

        supabase.configure(normUrl, cleanKey)
        _syncState.value = SyncState.SYNCING

        val (pingOk, msg) = supabase.ping()
        _lastSyncMessage.value = msg

        if (pingOk) {
            _syncState.value = SyncState.CONNECTED
            syncWithCloud()
        } else {
            _syncState.value = SyncState.ERROR
        }

        Pair(pingOk, msg)
    }

    fun saveSupabaseConfig(url: String, key: String) {
        scope.launch {
            saveAndTestSupabaseConfig(url, key)
        }
    }

    fun syncWithCloud() {
        if (!supabase.isConfigured) return
        scope.launch {
            try {
                // 0. Sync Subjects
                val subjectsArray = supabase.getJsonArray("subjects", "select=*&order=order_index.asc")
                if (subjectsArray != null && subjectsArray.length() > 0) {
                    val sList = mutableListOf<Subject>()
                    for (i in 0 until subjectsArray.length()) {
                        val obj = subjectsArray.getJSONObject(i)
                        sList.add(
                            Subject(
                                id = obj.optString("id"),
                                name = obj.optString("name"),
                                code = obj.optString("code"),
                                orderIndex = obj.optInt("order_index", i + 1),
                                colorHex = obj.optString("color_hex", "#7C3AED"),
                                iconName = obj.optString("icon_name", "menu_book")
                            )
                        )
                    }
                    if (sList.isNotEmpty()) {
                        _subjects.value = sList
                    }
                }

                // 1. Sync Chapters (Smart merge: preserve local completed progress)
                val chaptersArray = supabase.getJsonArray("chapters", "select=*&order=subject_id.asc,chapter_number.asc")
                if (chaptersArray != null && chaptersArray.length() > 0) {
                    val remoteList = mutableListOf<Chapter>()
                    for (i in 0 until chaptersArray.length()) {
                        val obj = chaptersArray.getJSONObject(i)
                        val statusStr = obj.optString("status", "not_started").uppercase()
                        val status = try {
                            ChapterStatus.valueOf(statusStr)
                        } catch (_: Exception) {
                            if (statusStr.contains("PROGRESS")) ChapterStatus.IN_PROGRESS
                            else if (statusStr.contains("COMPLETED") || statusStr.contains("DONE")) ChapterStatus.COMPLETED
                            else ChapterStatus.NOT_STARTED
                        }
                        remoteList.add(
                            Chapter(
                                id = obj.optString("id"),
                                subjectId = obj.optString("subject_id"),
                                chapterNumber = obj.optInt("chapter_number"),
                                title = obj.optString("title"),
                                status = status,
                                completedAt = if (obj.has("completed_at") && !obj.isNull("completed_at")) obj.getString("completed_at") else null,
                                notes = obj.optString("notes", "")
                            )
                        )
                    }

                    if (remoteList.isNotEmpty()) {
                        val localMap = _chapters.value.associateBy { it.id }
                        val merged = remoteList.map { rChap ->
                            val local = localMap[rChap.id]
                            if (local != null) {
                                // If locally completed or ongoing, protect student's hard work!
                                if (local.status == ChapterStatus.COMPLETED && rChap.status != ChapterStatus.COMPLETED) {
                                    scope.launch {
                                        val payload = JSONObject().apply {
                                            put("status", "completed")
                                            if (local.completedAt != null) put("completed_at", local.completedAt)
                                        }
                                        supabase.patchJson("chapters", "id=eq.${local.id}", payload)
                                    }
                                    local
                                } else if (local.notes.isNotBlank() && rChap.notes.isBlank()) {
                                    scope.launch {
                                        val payload = JSONObject().apply {
                                            put("notes", local.notes)
                                        }
                                        supabase.patchJson("chapters", "id=eq.${local.id}", payload)
                                    }
                                    local
                                } else {
                                    rChap
                                }
                            } else {
                                rChap
                            }
                        }
                        _chapters.value = merged
                        persistChapters()
                        checkChapterMilestones()
                    }
                }

                // 2. Sync Points Ledger from Cloud
                val ledgerArray = supabase.getJsonArray("points_ledger", "select=*&order=created_at.desc")
                if (ledgerArray != null && ledgerArray.length() > 0) {
                    val remoteTxList = mutableListOf<PointsTransaction>()
                    for (i in 0 until ledgerArray.length()) {
                        val obj = ledgerArray.getJSONObject(i)
                        val roleStr = obj.optString("created_by_role", "student").uppercase()
                        val role = if (roleStr == "ADMIN") UserRole.ADMIN else UserRole.STUDENT
                        remoteTxList.add(
                            PointsTransaction(
                                id = obj.optString("id", UUID.randomUUID().toString()),
                                userId = obj.optString("user_id", "user_surbie"),
                                amount = obj.optInt("amount", 0),
                                eventType = obj.optString("event_type", "TRANSACTION"),
                                reason = obj.optString("reason", "Points event"),
                                idempotencyKey = obj.optString("idempotency_key", obj.optString("id")),
                                createdByRole = role,
                                timestamp = obj.optString("created_at", "")
                            )
                        )
                    }

                    val remoteKeys = remoteTxList.map { it.idempotencyKey }.toSet()
                    val localOnly = _pointsHistory.value.filter { it.idempotencyKey !in remoteKeys }
                    // Push local-only transactions to cloud
                    for (tx in localOnly) {
                        scope.launch {
                            val txPayload = JSONObject().apply {
                                put("amount", tx.amount)
                                put("event_type", tx.eventType)
                                put("reason", tx.reason)
                                put("idempotency_key", tx.idempotencyKey)
                                put("created_by_role", tx.createdByRole.name.lowercase())
                                if (supabase.currentUserId.isNotBlank()) {
                                    put("user_id", supabase.currentUserId)
                                }
                            }
                            supabase.postJson("points_ledger", txPayload)
                        }
                    }

                    val combined = (remoteTxList + localOnly)
                    _pointsHistory.value = combined
                    _totalPoints.value = combined.sumOf { it.amount }
                    _currentUser.value = _currentUser.value.copy(totalPoints = _totalPoints.value)
                    persistPoints()
                    persistCurrentUser()
                }

                // 3. Sync Daily Routine Completions for Today
                val today = todayDateString()
                val compArray = supabase.getJsonArray("daily_routine_completions", "completion_date=eq.$today")
                if (compArray != null && compArray.length() > 0) {
                    val set = _completedRoutineIdsToday.value.toMutableSet()
                    for (i in 0 until compArray.length()) {
                        val obj = compArray.getJSONObject(i)
                        val bId = obj.optString("routine_block_id")
                        if (bId.isNotBlank()) set.add(bId)
                    }
                    _completedRoutineIdsToday.value = set
                    persistRoutine()
                }

                // 4. Sync Routine Blocks
                val routineArray = supabase.getJsonArray("routine_blocks", "select=*&order=order_index.asc")
                if (routineArray != null && routineArray.length() > 0) {
                    val rList = mutableListOf<RoutineBlock>()
                    for (i in 0 until routineArray.length()) {
                        val obj = routineArray.getJSONObject(i)
                        val typeStr = obj.optString("block_type", "study").uppercase()
                        val bType = try {
                            BlockType.valueOf(typeStr)
                        } catch (_: Exception) {
                            BlockType.STUDY
                        }
                        rList.add(
                            RoutineBlock(
                                id = obj.optString("id"),
                                startTime = obj.optString("start_time"),
                                endTime = obj.optString("end_time"),
                                title = obj.optString("title"),
                                blockType = bType,
                                points = obj.optInt("points", 0),
                                orderIndex = obj.optInt("order_index", i + 1)
                            )
                        )
                    }
                    if (rList.isNotEmpty()) {
                        _routine.value = rList
                    }
                }

                // 5. Sync Daily Goals
                val goalsArray = supabase.getJsonArray("daily_goals", "id=eq.1")
                if (goalsArray != null && goalsArray.length() > 0) {
                    val gObj = goalsArray.getJSONObject(0)
                    val target = gObj.optDouble("target_hours", 12.0).toFloat()
                    _dailyGoalHours.value = target
                    persistDailyGoal()
                }

                // 6. Sync Love Messages
                val loveArray = supabase.getJsonArray("love_messages", "select=*&order=created_at.desc&limit=10")
                if (loveArray != null && loveArray.length() > 0) {
                    val msgList = mutableListOf<LoveMessage>()
                    for (i in 0 until loveArray.length()) {
                        val obj = loveArray.getJSONObject(i)
                        msgList.add(
                            LoveMessage(
                                id = obj.optString("id"),
                                message = obj.optString("message"),
                                senderName = obj.optString("sender_name", "Admin"),
                                timestamp = obj.optString("created_at"),
                                isPinned = obj.optBoolean("is_pinned", false)
                            )
                        )
                    }
                    if (msgList.isNotEmpty()) {
                        _loveMessages.value = msgList
                        persistLoveMessages()
                    }
                }

                // 7. Sync Reward Requests
                val reqArray = supabase.getJsonArray("reward_requests", "select=*&order=requested_at.desc")
                if (reqArray != null && reqArray.length() > 0) {
                    val reqList = mutableListOf<RewardRequest>()
                    for (i in 0 until reqArray.length()) {
                        val obj = reqArray.getJSONObject(i)
                        val stStr = obj.optString("status", "pending").uppercase()
                        val st = try {
                            RewardStatus.valueOf(stStr)
                        } catch (_: Exception) {
                            RewardStatus.PENDING
                        }
                        reqList.add(
                            RewardRequest(
                                id = obj.optString("id"),
                                rewardId = obj.optString("reward_id"),
                                rewardTitle = obj.optString("reward_title"),
                                pointsCost = obj.optInt("points_cost"),
                                status = st,
                                studentNote = obj.optString("student_note", ""),
                                adminNote = obj.optString("admin_note", ""),
                                requestedAt = obj.optString("requested_at"),
                                reviewedAt = if (obj.has("reviewed_at") && !obj.isNull("reviewed_at")) obj.getString("reviewed_at") else null
                            )
                        )
                    }
                    _rewardRequests.value = reqList
                    persistRewards()
                }
            } catch (e: Exception) {
                Log.e("TwoHeartsRepository", "syncWithCloud error", e)
            }
        }
    }

    suspend fun loginWithSupabase(emailOrUsername: String, pass: String, roleFallback: UserRole): Pair<Boolean, String> {
        if (!supabase.isConfigured) {
            switchRole(roleFallback)
            return Pair(true, "Offline local session ready")
        }

        val (authOk, msg, userId) = supabase.signInWithPassword(emailOrUsername, pass)
        if (!authOk) {
            return Pair(false, msg)
        }

        // Fetch verified profile from Supabase to prevent client role escalation
        val profile = supabase.fetchUserProfile(userId)
        val roleStr = profile?.optString("role", "student")?.lowercase() ?: "student"
        val serverRole = if (roleStr == "admin") UserRole.ADMIN else UserRole.STUDENT

        prefs.edit()
            .putString("supabase_token", supabase.authToken)
            .putString("supabase_user_id", userId)
            .apply()

        _currentUser.value = UserProfile(
            id = userId,
            username = profile?.optString("username", emailOrUsername) ?: emailOrUsername,
            role = serverRole,
            displayName = profile?.optString("display_name", if (serverRole == UserRole.ADMIN) "Admin 👑" else "Surbie 🌸")
                ?: if (serverRole == UserRole.ADMIN) "Admin 👑" else "Surbie 🌸",
            totalPoints = _totalPoints.value
        )

        syncWithCloud()
        return Pair(true, "Signed in securely as ${serverRole.displayName}")
    }

    fun switchRole(role: UserRole) {
        if (role == UserRole.ADMIN) {
            _currentUser.value = UserProfile(
                id = "user_admin",
                username = "admin",
                role = UserRole.ADMIN,
                displayName = "Admin 👑",
                totalPoints = _totalPoints.value
            )
        } else {
            _currentUser.value = UserProfile(
                id = "user_surbie",
                username = "surbie",
                role = UserRole.STUDENT,
                displayName = "Surbie 🌸",
                totalPoints = _totalPoints.value
            )
        }
        persistCurrentUser()
    }

    fun updateChapterStatus(chapterId: String, status: ChapterStatus) {
        val now = if (status == ChapterStatus.COMPLETED) SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()) else null
        _chapters.value = _chapters.value.map { chap ->
            if (chap.id == chapterId) {
                chap.copy(status = status, completedAt = now)
            } else chap
        }
        persistChapters()
        checkChapterMilestones()

        if (supabase.isConfigured) {
            scope.launch {
                val payload = JSONObject().apply {
                    put("status", status.name.lowercase())
                    if (now != null) put("completed_at", now)
                }
                supabase.patchJson("chapters", "id=eq.$chapterId", payload)
            }
        }
    }

    fun updateChapterNotes(chapterId: String, notes: String) {
        _chapters.value = _chapters.value.map { chap ->
            if (chap.id == chapterId) chap.copy(notes = notes) else chap
        }
        persistChapters()

        if (supabase.isConfigured) {
            scope.launch {
                val payload = JSONObject().apply {
                    put("notes", notes)
                }
                supabase.patchJson("chapters", "id=eq.$chapterId", payload)
            }
        }
    }

    fun renameChapter(chapterId: String, newTitle: String) {
        _chapters.value = _chapters.value.map { chap ->
            if (chap.id == chapterId) chap.copy(title = newTitle) else chap
        }
        persistChapters()

        if (supabase.isConfigured) {
            scope.launch {
                val payload = JSONObject().apply {
                    put("title", newTitle)
                }
                supabase.patchJson("chapters", "id=eq.$chapterId", payload)
            }
        }
    }

    fun addNewChapter(subjectId: String, title: String) {
        val subjectChapters = _chapters.value.filter { it.subjectId == subjectId }
        val nextNumber = (subjectChapters.maxOfOrNull { it.chapterNumber } ?: 0) + 1
        val newChap = Chapter(
            id = "chap_${subjectId}_$nextNumber",
            subjectId = subjectId,
            chapterNumber = nextNumber,
            title = title,
            status = ChapterStatus.NOT_STARTED
        )
        _chapters.value = _chapters.value + newChap
        persistChapters()

        if (supabase.isConfigured) {
            scope.launch {
                val payload = JSONObject().apply {
                    put("id", newChap.id)
                    put("subject_id", subjectId)
                    put("chapter_number", nextNumber)
                    put("title", title)
                    put("status", "not_started")
                }
                supabase.postJson("chapters", payload)
            }
        }
    }

    fun claimRoutineBlock(block: RoutineBlock): Boolean {
        val today = todayDateString()
        val idempotencyKey = "routine_${block.id}_$today"

        // Check if already claimed today
        if (_completedRoutineIdsToday.value.contains(block.id)) {
            return false
        }

        _completedRoutineIdsToday.value = _completedRoutineIdsToday.value + block.id
        persistRoutine()

        // Also update streak if first activity today
        val dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        if (!_activeStreakDays.value.contains(dayOfMonth)) {
            _activeStreakDays.value = _activeStreakDays.value + dayOfMonth
            persistStudy()
        }

        if (block.points > 0) {
            val tx = PointsTransaction(
                id = UUID.randomUUID().toString(),
                userId = "user_surbie",
                amount = block.points,
                eventType = "ROUTINE_BLOCK",
                reason = "Completed routine: ${block.title}",
                idempotencyKey = idempotencyKey,
                createdByRole = UserRole.STUDENT,
                timestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            )
            addPointsTransaction(tx)
        }

        if (supabase.isConfigured) {
            scope.launch {
                val compPayload = JSONObject().apply {
                    put("routine_block_id", block.id)
                    put("completion_date", today)
                    if (supabase.currentUserId.isNotBlank()) {
                        put("user_id", supabase.currentUserId)
                    }
                }
                supabase.postJson("daily_routine_completions", compPayload)
            }
        }

        // Check full daily routine completion bonus (+50 points)
        if (_completedRoutineIdsToday.value.size == _routine.value.size) {
            val fullBonusKey = "full_routine_bonus_$today"
            val alreadyClaimedBonus = _pointsHistory.value.any { it.idempotencyKey == fullBonusKey }
            if (!alreadyClaimedBonus) {
                val bonusTx = PointsTransaction(
                    id = UUID.randomUUID().toString(),
                    userId = "user_surbie",
                    amount = 50,
                    eventType = "FULL_ROUTINE_BONUS",
                    reason = "All 16 routine blocks conquered today! Outstanding! 🌟",
                    idempotencyKey = fullBonusKey,
                    createdByRole = UserRole.STUDENT,
                    timestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                )
                addPointsTransaction(bonusTx)
            }
        }
        return true
    }

    fun recordStudySession(subject: Subject?, chapter: Chapter?, durationSeconds: Long, notes: String) {
        val session = StudySession(
            id = UUID.randomUUID().toString(),
            userId = "user_surbie",
            subjectId = subject?.id,
            chapterId = chapter?.id,
            subjectName = subject?.name ?: "General Focus",
            chapterTitle = chapter?.title ?: "Independent Study",
            durationSeconds = durationSeconds,
            startedAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(System.currentTimeMillis() - durationSeconds * 1000)),
            endedAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
            notes = notes,
            pointsAwarded = ((durationSeconds / 3600) * 10).toInt()
        )
        _studySessions.value = listOf(session) + _studySessions.value
        _todayFocusedSeconds.value += durationSeconds

        val addedHours = durationSeconds / 3600f
        val currentMap = _weeklyStudyHours.value.toMutableMap()
        val currentToday = currentMap["Today"] ?: 0f
        currentMap["Today"] = currentToday + addedHours
        _weeklyStudyHours.value = currentMap

        val dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        if (!_activeStreakDays.value.contains(dayOfMonth)) {
            _activeStreakDays.value = _activeStreakDays.value + dayOfMonth
        }
        persistStudy()

        // Award +10 points per full focused study hour
        val hours = (durationSeconds / 3600).toInt()
        if (hours > 0) {
            val pts = hours * 10
            val tx = PointsTransaction(
                id = UUID.randomUUID().toString(),
                userId = "user_surbie",
                amount = pts,
                eventType = "FOCUSED_HOURS",
                reason = "Studied $hours hour(s) focused in ${session.subjectName}",
                idempotencyKey = "session_${session.id}",
                createdByRole = UserRole.STUDENT,
                timestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            )
            addPointsTransaction(tx)
        }

        if (supabase.isConfigured) {
            scope.launch {
                val sessionPayload = JSONObject().apply {
                    put("subject_id", subject?.id)
                    put("chapter_id", chapter?.id)
                    put("subject_name", session.subjectName)
                    put("chapter_title", session.chapterTitle)
                    put("duration_seconds", durationSeconds)
                    put("started_at", session.startedAt)
                    put("ended_at", session.endedAt)
                    put("notes", notes)
                    put("points_awarded", session.pointsAwarded)
                    if (supabase.currentUserId.isNotBlank()) {
                        put("user_id", supabase.currentUserId)
                    }
                }
                supabase.postJson("study_sessions", sessionPayload)
            }
        }
    }

    fun awardAdminBonus(amount: Int, reason: String) {
        val tx = PointsTransaction(
            id = UUID.randomUUID().toString(),
            userId = "user_surbie",
            amount = amount,
            eventType = "ADMIN_BONUS",
            reason = reason,
            idempotencyKey = "admin_bonus_${System.currentTimeMillis()}",
            createdByRole = UserRole.ADMIN,
            timestamp = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date())
        )
        addPointsTransaction(tx)
    }

    fun deductAdminPoints(amount: Int, reason: String) {
        val tx = PointsTransaction(
            id = UUID.randomUUID().toString(),
            userId = "user_surbie",
            amount = -amount,
            eventType = "ADMIN_DEDUCTION",
            reason = reason,
            idempotencyKey = "admin_deduct_${System.currentTimeMillis()}",
            createdByRole = UserRole.ADMIN,
            timestamp = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date())
        )
        addPointsTransaction(tx)
    }

    private fun addPointsTransaction(tx: PointsTransaction) {
        _pointsHistory.value = listOf(tx) + _pointsHistory.value
        _totalPoints.value += tx.amount
        _currentUser.value = _currentUser.value.copy(totalPoints = _totalPoints.value)
        persistPoints()
        persistCurrentUser()

        if (supabase.isConfigured) {
            scope.launch {
                val txPayload = JSONObject().apply {
                    put("amount", tx.amount)
                    put("event_type", tx.eventType)
                    put("reason", tx.reason)
                    put("idempotency_key", tx.idempotencyKey)
                    put("created_by_role", tx.createdByRole.name.lowercase())
                    if (supabase.currentUserId.isNotBlank()) {
                        put("user_id", supabase.currentUserId)
                    }
                }
                supabase.postJson("points_ledger", txPayload)
            }
        }
    }

    fun requestReward(reward: Reward, note: String): Pair<Boolean, String> {
        if (_totalPoints.value < reward.pointsCost) {
            return Pair(false, "Need ${reward.pointsCost - _totalPoints.value} more points to request this!")
        }

        // Deduct points atomically
        val tx = PointsTransaction(
            id = UUID.randomUUID().toString(),
            userId = "user_surbie",
            amount = -reward.pointsCost,
            eventType = "REWARD_CLAIM",
            reason = "Redeemed reward: ${reward.title}",
            idempotencyKey = "claim_${reward.id}_${System.currentTimeMillis()}",
            createdByRole = UserRole.STUDENT,
            timestamp = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date())
        )
        addPointsTransaction(tx)

        val req = RewardRequest(
            id = UUID.randomUUID().toString(),
            rewardId = reward.id,
            rewardTitle = reward.title,
            pointsCost = reward.pointsCost,
            status = RewardStatus.PENDING,
            studentNote = note,
            adminNote = "",
            requestedAt = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date())
        )
        _rewardRequests.value = listOf(req) + _rewardRequests.value
        persistRewards()

        if (supabase.isConfigured) {
            scope.launch {
                val reqPayload = JSONObject().apply {
                    put("reward_id", reward.id)
                    put("reward_title", reward.title)
                    put("points_cost", reward.pointsCost)
                    put("status", "pending")
                    put("student_note", note)
                    if (supabase.currentUserId.isNotBlank()) {
                        put("user_id", supabase.currentUserId)
                    }
                }
                supabase.postJson("reward_requests", reqPayload)
            }
        }

        return Pair(true, "Reward request sent to Admin! ❤️")
    }

    fun updateRewardRequestStatus(requestId: String, newStatus: RewardStatus, adminNote: String) {
        _rewardRequests.value = _rewardRequests.value.map { req ->
            if (req.id == requestId) {
                // If rejected, refund points to student
                if (newStatus == RewardStatus.REJECTED && req.status != RewardStatus.REJECTED) {
                    val refundTx = PointsTransaction(
                        id = UUID.randomUUID().toString(),
                        userId = "user_surbie",
                        amount = req.pointsCost,
                        eventType = "REWARD_REFUND",
                        reason = "Refund for rejected reward: ${req.rewardTitle}",
                        idempotencyKey = "refund_${req.id}",
                        createdByRole = UserRole.ADMIN,
                        timestamp = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date())
                    )
                    addPointsTransaction(refundTx)
                }
                req.copy(
                    status = newStatus,
                    adminNote = adminNote,
                    reviewedAt = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date())
                )
            } else req
        }
        persistRewards()

        if (supabase.isConfigured) {
            scope.launch {
                val updatePayload = JSONObject().apply {
                    put("status", newStatus.name.lowercase())
                    put("admin_note", adminNote)
                }
                supabase.patchJson("reward_requests", "id=eq.$requestId", updatePayload)
            }
        }
    }

    fun addLoveMessage(text: String, isPinned: Boolean = false) {
        val msg = LoveMessage(
            id = UUID.randomUUID().toString(),
            message = text,
            senderName = "Admin",
            timestamp = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date()),
            isPinned = isPinned
        )
        _loveMessages.value = listOf(msg) + _loveMessages.value
        persistLoveMessages()

        if (supabase.isConfigured) {
            scope.launch {
                val msgPayload = JSONObject().apply {
                    put("message", text)
                    put("sender_name", "Admin")
                    put("is_pinned", isPinned)
                }
                supabase.postJson("love_messages", msgPayload)
            }
        }
    }

    fun updateDailyGoal(hours: Float) {
        _dailyGoalHours.value = hours
        persistDailyGoal()

        if (supabase.isConfigured) {
            scope.launch {
                val goalPayload = JSONObject().apply {
                    put("target_hours", hours)
                }
                supabase.patchJson("daily_goals", "id=eq.1", goalPayload)
            }
        }
    }

    private fun checkChapterMilestones() {
        val completedCount = _chapters.value.count { it.status == ChapterStatus.COMPLETED }
        var anyUnlocked = false
        _milestones.value = _milestones.value.map { ms ->
            if (ms.category == MilestoneCategory.CHAPTERS && completedCount >= ms.targetCount && !ms.isUnlocked) {
                anyUnlocked = true
                val unlockTx = PointsTransaction(
                    id = UUID.randomUUID().toString(),
                    userId = "user_surbie",
                    amount = ms.pointsReward,
                    eventType = "MILESTONE_CHAPTERS",
                    reason = "Unlocked: ${ms.title} (${ms.targetCount} chapters complete)",
                    idempotencyKey = "ms_${ms.id}_unlock",
                    createdByRole = UserRole.ADMIN,
                    timestamp = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date())
                )
                addPointsTransaction(unlockTx)
                ms.copy(isUnlocked = true, unlockedAt = "Today")
            } else ms
        }
        if (anyUnlocked) {
            persistMilestones()
        }
    }

    // --- 10 Features Mutation Methods ---

    fun updateFlashcardMastery(cardId: String, newMastery: FlashcardMastery) {
        val nowStr = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date())
        _flashcards.value = _flashcards.value.map { fc ->
            if (fc.id == cardId) {
                // If newly mastered, award 5 bonus points!
                if (newMastery == FlashcardMastery.MASTERED && fc.mastery != FlashcardMastery.MASTERED) {
                    val tx = PointsTransaction(
                        id = UUID.randomUUID().toString(),
                        userId = "user_surbie",
                        amount = 5,
                        eventType = "FLASHCARD_MASTERY",
                        reason = "Mastered formula: ${fc.chapterTitle} 🏆",
                        idempotencyKey = "fc_${fc.id}_mastered_${System.currentTimeMillis()}",
                        createdByRole = UserRole.STUDENT,
                        timestamp = nowStr
                    )
                    addPointsTransaction(tx)
                }
                fc.copy(mastery = newMastery, lastReviewedAt = nowStr)
            } else fc
        }
        persistFlashcards()
    }

    fun addNewFlashcard(subjectId: String, chapterTitle: String, frontQuestion: String, backAnswer: String) {
        val newCard = Flashcard(
            id = "fc_${System.currentTimeMillis()}",
            subjectId = subjectId,
            chapterTitle = chapterTitle.ifBlank { "General Concept" },
            frontQuestion = frontQuestion,
            backAnswer = backAnswer,
            mastery = FlashcardMastery.NEED_REVIEW,
            lastReviewedAt = "Just now"
        )
        _flashcards.value = listOf(newCard) + _flashcards.value
        persistFlashcards()
    }

    fun addDoubt(subjectId: String, subjectName: String, chapterTitle: String, questionText: String) {
        val nowStr = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date())
        val newDoubt = DoubtEntry(
            id = "dbt_${System.currentTimeMillis()}",
            subjectId = subjectId,
            subjectName = subjectName,
            chapterTitle = chapterTitle.ifBlank { "General Topic" },
            questionText = questionText,
            adminSolution = "",
            isResolved = false,
            createdAt = nowStr
        )
        _doubts.value = listOf(newDoubt) + _doubts.value
        persistDoubts()
    }

    fun resolveDoubt(doubtId: String, solution: String) {
        val nowStr = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date())
        _doubts.value = _doubts.value.map { d ->
            if (d.id == doubtId) {
                if (!d.isResolved) {
                    // Award 20 points for clearing doubt together!
                    val tx = PointsTransaction(
                        id = UUID.randomUUID().toString(),
                        userId = "user_surbie",
                        amount = 20,
                        eventType = "DOUBT_RESOLVED",
                        reason = "Cleared Doubt: ${d.chapterTitle} with Admin 💡",
                        idempotencyKey = "dbt_${d.id}_resolved",
                        createdByRole = UserRole.ADMIN,
                        timestamp = nowStr
                    )
                    addPointsTransaction(tx)
                }
                d.copy(
                    adminSolution = solution,
                    isResolved = true,
                    resolvedAt = nowStr
                )
            } else d
        }
        persistDoubts()
    }

    fun updateExamTarget(dateStr: String, days: Int, pace: Int, quote: String) {
        _examTarget.value = _examTarget.value.copy(
            examDateStr = dateStr,
            daysRemaining = days,
            targetChaptersPerWeek = pace,
            motivationalQuote = quote
        )
        persistExamTarget()
    }

    fun logTodayMood(emoji: String, label: String, energyPercent: Int) {
        val checkin = MoodCheckIn(
            id = "mood_${todayDateString()}",
            emoji = emoji,
            moodLabel = label,
            energyPercent = energyPercent,
            date = todayDateString(),
            adminHeartReaction = false
        )
        _todayMood.value = checkin
        persistMood()

        // Mindful check-in bonus 10 points
        val tx = PointsTransaction(
            id = UUID.randomUUID().toString(),
            userId = "user_surbie",
            amount = 10,
            eventType = "MOOD_CHECKIN",
            reason = "Mindful Daily Energy Check-in ($emoji $label) 🌸",
            idempotencyKey = "mood_${todayDateString()}",
            createdByRole = UserRole.STUDENT,
            timestamp = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date())
        )
        addPointsTransaction(tx)
    }

    fun toggleAdminHeartOnMood() {
        val current = _todayMood.value ?: return
        _todayMood.value = current.copy(adminHeartReaction = !current.adminHeartReaction)
        persistMood()
    }

    fun sendNudge(type: String, message: String) {
        val nowStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val nudge = AdminNudge(
            id = "ndg_${System.currentTimeMillis()}",
            type = type,
            message = message,
            senderName = "Zaid",
            timestamp = nowStr,
            isAcknowledged = false
        )
        _nudges.value = listOf(nudge) + _nudges.value
        persistNudges()
    }

    fun acknowledgeNudge(nudgeId: String) {
        _nudges.value = _nudges.value.map { n ->
            if (n.id == nudgeId) n.copy(isAcknowledged = true) else n
        }
        persistNudges()
    }

    fun createMysteryGift(title: String, message: String, points: Int, perk: String) {
        val gift = MysteryGift(
            id = "gift_${System.currentTimeMillis()}",
            title = title,
            message = message,
            pointsAmount = points,
            perkDescription = perk,
            isClaimed = false
        )
        _mysteryGifts.value = listOf(gift) + _mysteryGifts.value
        persistMysteryGifts()
    }

    fun claimMysteryGift(giftId: String) {
        val nowStr = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date())
        var claimedAmount = 0
        var giftTitle = ""
        _mysteryGifts.value = _mysteryGifts.value.map { g ->
            if (g.id == giftId && !g.isClaimed) {
                claimedAmount = g.pointsAmount
                giftTitle = g.title
                g.copy(isClaimed = true, claimedAt = nowStr)
            } else g
        }
        if (claimedAmount > 0) {
            val tx = PointsTransaction(
                id = UUID.randomUUID().toString(),
                userId = "user_surbie",
                amount = claimedAmount,
                eventType = "MYSTERY_GIFT_CLAIMED",
                reason = "Claimed Mystery Gift: $giftTitle 🎁",
                idempotencyKey = "gift_${giftId}_claimed",
                createdByRole = UserRole.ADMIN,
                timestamp = nowStr
            )
            addPointsTransaction(tx)
            persistMysteryGifts()
        }
    }

    fun createQuest(title: String, description: String, targetType: String, targetGoal: Int, pointsBounty: Int, deadline: String) {
        val quest = StudyQuest(
            id = "qst_${System.currentTimeMillis()}",
            title = title,
            description = description,
            targetType = targetType,
            targetGoal = targetGoal,
            currentProgress = 0,
            pointsBounty = pointsBounty,
            deadline = deadline,
            isCompleted = false
        )
        _studyQuests.value = listOf(quest) + _studyQuests.value
        persistQuests()
    }

    fun incrementQuestProgress(questId: String) {
        val nowStr = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date())
        var completedBounty = 0
        var questTitle = ""
        _studyQuests.value = _studyQuests.value.map { q ->
            if (q.id == questId && !q.isCompleted) {
                val nextProg = q.currentProgress + 1
                val isDone = nextProg >= q.targetGoal
                if (isDone) {
                    completedBounty = q.pointsBounty
                    questTitle = q.title
                }
                q.copy(currentProgress = nextProg, isCompleted = isDone)
            } else q
        }
        if (completedBounty > 0) {
            val tx = PointsTransaction(
                id = UUID.randomUUID().toString(),
                userId = "user_surbie",
                amount = completedBounty,
                eventType = "QUEST_BOUNTY",
                reason = "Conquered Quest: $questTitle 🎯",
                idempotencyKey = "quest_${questId}_completed",
                createdByRole = UserRole.ADMIN,
                timestamp = nowStr
            )
            addPointsTransaction(tx)
        }
        persistQuests()
    }

    fun toggleSoundscape(trackId: String) {
        _soundscapes.value = _soundscapes.value.map { s ->
            if (s.id == trackId) s.copy(isPlaying = !s.isPlaying) else s
        }
    }

    fun updateSoundscapeVolume(trackId: String, volume: Float) {
        _soundscapes.value = _soundscapes.value.map { s ->
            if (s.id == trackId) s.copy(volume = volume) else s
        }
    }

    fun getWeeklyReportSummary(): WeeklyReportSummary {
        val totalSecs = _studySessions.value.sumOf { it.durationSeconds }
        val completedChaps = _chapters.value.count { it.status == ChapterStatus.COMPLETED }
        val sessionsCount = _studySessions.value.size
        val points = _totalPoints.value
        val streak = _currentStreakDays.value

        val subjectMinutes = mutableMapOf<String, Long>()
        for (s in _studySessions.value) {
            subjectMinutes[s.subjectName] = (subjectMinutes[s.subjectName] ?: 0L) + (s.durationSeconds / 60)
        }
        val topSub = subjectMinutes.maxByOrNull { it.value }?.key ?: "Physics"
        val todayStr = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())

        return WeeklyReportSummary(
            totalMinutes = totalSecs / 60,
            chaptersCompleted = completedChaps,
            sessionsCount = sessionsCount,
            pointsEarned = points,
            currentStreak = streak,
            topSubject = topSub,
            certificateDate = todayStr
        )
    }

    private fun todayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}
