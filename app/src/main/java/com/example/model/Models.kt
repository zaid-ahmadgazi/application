package com.example.model

enum class UserRole(val displayName: String) {
    ADMIN("Admin"),
    STUDENT("Surbie (Student)")
}

data class UserProfile(
    val id: String,
    val username: String,
    val role: UserRole,
    val displayName: String,
    val totalPoints: Int = 0
)

enum class ChapterStatus(val label: String) {
    NOT_STARTED("Not Started"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed")
}

data class Subject(
    val id: String,
    val name: String,
    val code: String,
    val orderIndex: Int,
    val colorHex: String,
    val iconName: String = "menu_book"
)

data class Chapter(
    val id: String,
    val subjectId: String,
    val chapterNumber: Int,
    val title: String,
    val status: ChapterStatus = ChapterStatus.NOT_STARTED,
    val completedAt: String? = null,
    val notes: String = "",
    val updatedAt: String = "",
    val yieldLevel: String = "High" // "High", "Medium", "Foundational"
)

enum class BlockType(val label: String) {
    WAKE_UP("Wake Up"),
    STUDY("Focused Study"),
    BREAK("Relax & Break"),
    MEAL("Nourishment"),
    REVIEW("Final Review")
}

data class RoutineBlock(
    val id: String,
    val startTime: String,
    val endTime: String,
    val title: String,
    val blockType: BlockType,
    val points: Int,
    val orderIndex: Int
)

data class StudySession(
    val id: String,
    val userId: String,
    val subjectId: String?,
    val chapterId: String?,
    val subjectName: String,
    val chapterTitle: String,
    val durationSeconds: Long,
    val startedAt: String,
    val endedAt: String,
    val notes: String = "",
    val pointsAwarded: Int = 0
)

data class PointsTransaction(
    val id: String,
    val userId: String,
    val amount: Int,
    val eventType: String,
    val reason: String,
    val idempotencyKey: String,
    val createdByRole: UserRole,
    val timestamp: String
)

data class Reward(
    val id: String,
    val title: String,
    val description: String,
    val pointsCost: Int,
    val iconName: String = "favorite",
    val isActive: Boolean = true
)

enum class RewardStatus(val label: String) {
    PENDING("Pending Review"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    FULFILLED("Fulfilled ❤️")
}

data class RewardRequest(
    val id: String,
    val rewardId: String,
    val rewardTitle: String,
    val pointsCost: Int,
    val status: RewardStatus,
    val studentNote: String,
    val adminNote: String = "",
    val requestedAt: String,
    val reviewedAt: String? = null
)

enum class MilestoneCategory {
    STREAK,
    CHAPTERS
}

data class Milestone(
    val id: String,
    val title: String,
    val category: MilestoneCategory,
    val targetCount: Int,
    val pointsReward: Int,
    val isUnlocked: Boolean = false,
    val unlockedAt: String? = null
)

data class LoveMessage(
    val id: String,
    val message: String,
    val senderName: String,
    val timestamp: String,
    val isPinned: Boolean = false
)

enum class SyncState {
    CONNECTED,
    LOCAL_CACHE,
    SYNCING,
    ERROR
}

// -------------------------------------------------------------
// NEW 10 POWER FEATURES DATA MODELS
// -------------------------------------------------------------

enum class FlashcardMastery(val label: String) {
    NEED_REVIEW("Need Review"),
    GOOD("Good"),
    MASTERED("Mastered 🏆")
}

data class Flashcard(
    val id: String,
    val subjectId: String,
    val chapterTitle: String,
    val frontQuestion: String,
    val backAnswer: String,
    val mastery: FlashcardMastery = FlashcardMastery.NEED_REVIEW,
    val lastReviewedAt: String = ""
)

data class DoubtEntry(
    val id: String,
    val subjectId: String,
    val subjectName: String,
    val chapterTitle: String,
    val questionText: String,
    val adminSolution: String = "",
    val isResolved: Boolean = false,
    val createdAt: String,
    val resolvedAt: String? = null
)

data class ExamTarget(
    val id: String,
    val title: String,
    val examDateStr: String,
    val daysRemaining: Int,
    val targetChaptersPerWeek: Int,
    val motivationalQuote: String
)

data class MoodCheckIn(
    val id: String,
    val emoji: String,
    val moodLabel: String,
    val energyPercent: Int, // 0 - 100
    val date: String,
    val adminHeartReaction: Boolean = false
)

data class AdminNudge(
    val id: String,
    val type: String, // "HUG", "WATER", "CHEER", "STRETCH"
    val message: String,
    val senderName: String,
    val timestamp: String,
    val isAcknowledged: Boolean = false
)

data class MysteryGift(
    val id: String,
    val title: String,
    val message: String,
    val pointsAmount: Int,
    val perkDescription: String,
    val isClaimed: Boolean = false,
    val claimedAt: String? = null
)

data class StudyQuest(
    val id: String,
    val title: String,
    val description: String,
    val targetType: String,
    val targetGoal: Int,
    val currentProgress: Int = 0,
    val pointsBounty: Int,
    val deadline: String,
    val isCompleted: Boolean = false
)

data class SoundscapeTrack(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconName: String,
    val isPlaying: Boolean = false,
    val volume: Float = 0.5f
)

data class WeeklyReportSummary(
    val totalMinutes: Long,
    val chaptersCompleted: Int,
    val sessionsCount: Int,
    val pointsEarned: Int,
    val currentStreak: Int,
    val topSubject: String,
    val certificateDate: String
)
