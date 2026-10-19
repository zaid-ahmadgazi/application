package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TwoHeartsRepository
import com.example.model.ChapterStatus
import com.example.model.MysteryGift
import com.example.model.RoutineBlock
import com.example.model.Subject
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun StudentDashboardScreen(
    repository: TwoHeartsRepository,
    onNavigateToTimer: () -> Unit,
    onLaunchAmbientTimer: () -> Unit,
    onNavigateToRoutine: () -> Unit,
    onNavigateToChapters: (Subject?) -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToFlashcards: () -> Unit = {},
    onNavigateToDoubts: () -> Unit = {}
) {
    val totalPoints by repository.totalPoints.collectAsState()
    val focusedSeconds by repository.todayFocusedSeconds.collectAsState()
    val goalHours by repository.dailyGoalHours.collectAsState()
    val streakDays by repository.currentStreakDays.collectAsState()
    val chapters by repository.chapters.collectAsState()
    val subjects by repository.subjects.collectAsState()
    val routine by repository.routine.collectAsState()
    val completedBlocks by repository.completedRoutineIdsToday.collectAsState()
    val loveMessages by repository.loveMessages.collectAsState()
    val weeklyHours by repository.weeklyStudyHours.collectAsState()
    val activeCalendarDays by repository.activeStreakDays.collectAsState()

    // 10 Features States
    val nudges by repository.nudges.collectAsState()
    val mysteryGifts by repository.mysteryGifts.collectAsState()
    val examTarget by repository.examTarget.collectAsState()
    val todayMood by repository.todayMood.collectAsState()
    val quests by repository.studyQuests.collectAsState()

    val unacknowledgedNudge = nudges.firstOrNull { !it.isAcknowledged }
    val unclaimedGift = mysteryGifts.firstOrNull { !it.isClaimed }

    var showReportDialog by remember { mutableStateOf(false) }
    var viewingGift by remember { mutableStateOf<MysteryGift?>(null) }

    val focusedHours = focusedSeconds / 3600f
    val goalProgress = (focusedHours / goalHours).coerceIn(0f, 1f)
    val completedChaptersCount = chapters.count { it.status == ChapterStatus.COMPLETED }
    val latestLoveMessage = loveMessages.firstOrNull { it.isPinned } ?: loveMessages.firstOrNull()

    // Analytics Tab (0: Weekly Bar, 1: Streak Calendar, 2: Subjects Breakdown)
    var analyticsTab by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Instant Virtual Hug / Nudge Banner (Feature 6)
        if (unacknowledgedNudge != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CreamSurfaceVariant,
                    border = BorderStroke(1.5.dp, BlackBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(PurpleLogo)
                                .border(1.dp, BlackBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (unacknowledgedNudge.type) {
                                    "HUG" -> Icons.Default.Favorite
                                    "WATER" -> Icons.Default.WaterDrop
                                    "CHEER" -> Icons.Default.Star
                                    else -> Icons.Default.SelfImprovement
                                },
                                contentDescription = null,
                                tint = CreamBackground,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Nudge from ${unacknowledgedNudge.senderName} ❤️",
                                style = MaterialTheme.typography.labelMedium,
                                color = BlackText,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = unacknowledgedNudge.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = BlackText,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = { repository.acknowledgeNudge(unacknowledgedNudge.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BlackBorder,
                                contentColor = CreamBackground
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, BlackBorder),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Hug Back ❤️", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CreamBackground)
                        }
                    }
                }
            }
        }

        // 2. Surprise Mystery Gift Box Banner (Feature 7)
        if (unclaimedGift != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CreamSurfaceVariant,
                    border = BorderStroke(1.5.dp, BlackBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewingGift = unclaimedGift }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(CreamSurface)
                                .border(1.5.dp, BlackBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = null,
                                tint = PurpleLogo,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SURPRISE GIFT FROM ZAID! 🎁",
                                style = MaterialTheme.typography.labelSmall,
                                color = BlackText,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = unclaimedGift.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = BlackText,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tap to unwrap surprise points & special perk!",
                                style = MaterialTheme.typography.bodySmall,
                                color = BlackText
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = BlackText
                        )
                    }
                }
            }
        }

        // 3. Exam Countdown & Pace Calculator Card (Feature 3)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CreamSurface),
                border = BorderStroke(1.5.dp, BlackBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = BlackText,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = examTarget.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = BlackText,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CreamSurfaceVariant,
                            border = BorderStroke(1.dp, BlackBorder)
                        ) {
                            Text(
                                text = "${examTarget.daysRemaining} Days Left ⏳",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = BlackText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Target Pace: ${examTarget.targetChaptersPerWeek} chaps / week",
                            style = MaterialTheme.typography.bodySmall,
                            color = BlackText,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Exam Date: ${examTarget.examDateStr}",
                            style = MaterialTheme.typography.bodySmall,
                            color = BlackText,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CreamSurfaceVariant,
                        border = BorderStroke(1.dp, BlackBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "\"${examTarget.motivationalQuote}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = BlackText,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        // 4. Daily Mood & Energy Check-in (Feature 4)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CreamSurface),
                border = BorderStroke(1.5.dp, BlackBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Mindful Check-in 🌸",
                            style = MaterialTheme.typography.titleMedium,
                            color = BlackText,
                            fontWeight = FontWeight.Bold
                        )

                        if (todayMood != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CreamSurfaceVariant,
                                border = BorderStroke(1.dp, BlackBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (todayMood!!.adminHeartReaction) "Zaid Loved this ❤️" else "Logged Today ✓",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BlackText,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (todayMood == null) {
                        Text(
                            text = "How are you feeling before today's study sprints? (+10 bonus pts)",
                            style = MaterialTheme.typography.bodySmall,
                            color = BlackText
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MoodButton("⚡", "Energetic", 95) { repository.logTodayMood("⚡", "Energetic", 95) }
                            MoodButton("🌸", "Motivated", 85) { repository.logTodayMood("🌸", "Motivated", 85) }
                            MoodButton("☕", "Steady", 70) { repository.logTodayMood("☕", "Steady", 70) }
                            MoodButton("😴", "Tired", 50) { repository.logTodayMood("😴", "Tired", 50) }
                            MoodButton("🥺", "Overwhelmed", 35) { repository.logTodayMood("🥺", "Overwhelmed", 35) }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = todayMood!!.emoji,
                                    fontSize = 28.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = todayMood!!.moodLabel,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = BlackText,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Energy Battery: ${todayMood!!.energyPercent}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BlackText
                                    )
                                }
                            }

                            LinearProgressIndicator(
                                progress = { todayMood!!.energyPercent / 100f },
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .border(1.dp, BlackBorder, RoundedCornerShape(5.dp)),
                                color = BlackBorder,
                                trackColor = CreamSurface
                            )
                        }
                    }
                }
            }
        }

        // 5. Quick Study Tools Bar (Flashcards, Doubts, Certificate)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedCard(
                    onClick = onNavigateToFlashcards,
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = CreamSurface),
                    border = BorderStroke(1.5.dp, BlackBorder),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("nav_flashcards_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Style, contentDescription = null, tint = BlackText, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Flashcards", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = BlackText)
                        Text("Active Recall", style = MaterialTheme.typography.labelSmall, color = BlackText)
                    }
                }

                OutlinedCard(
                    onClick = onNavigateToDoubts,
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = CreamSurface),
                    border = BorderStroke(1.5.dp, BlackBorder),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("nav_doubts_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = BlackText, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Doubt Diary", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = BlackText)
                        Text("Solve Together", style = MaterialTheme.typography.labelSmall, color = BlackText)
                    }
                }

                OutlinedCard(
                    onClick = { showReportDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = CreamSurface),
                    border = BorderStroke(1.5.dp, BlackBorder),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("nav_certificate_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = BlackText, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Report Card", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = BlackText)
                        Text("Certificate", style = MaterialTheme.typography.labelSmall, color = BlackText)
                    }
                }
            }
        }

        // 6. Active Study Quests (Feature 8)
        if (quests.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CreamSurface),
                    border = BorderStroke(1.5.dp, BlackBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Flag, contentDescription = null, tint = BlackText, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Active Study Quests & Bounties", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BlackText)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        quests.forEach { quest ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CreamSurfaceVariant,
                                border = BorderStroke(1.dp, BlackBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = quest.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = BlackText
                                        )
                                        Text(
                                            text = quest.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = BlackText
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Progress: ${quest.currentProgress} / ${quest.targetGoal} • Deadline: ${quest.deadline}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = BlackText
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (quest.isCompleted) BlackBorder else CreamSurface,
                                        border = BorderStroke(1.dp, BlackBorder)
                                    ) {
                                        Text(
                                            text = if (quest.isCompleted) "Claimed ✓" else "+${quest.pointsBounty} pts",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (quest.isCompleted) CreamBackground else BlackText,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Pinned Supportive Love Message Banner
        if (latestLoveMessage != null) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = CreamSurfaceVariant,
                    borderColor = BlackBorder
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(PurpleLogo)
                                .border(1.dp, BlackBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Love Message",
                                tint = CreamBackground,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Love Note from ${latestLoveMessage.senderName}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = BlackText
                                    )
                                )
                                Text(
                                    text = latestLoveMessage.timestamp,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BlackText
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = latestLoveMessage.message,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = BlackText,
                                    lineHeight = 20.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Daily Focused Study Goal Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = CreamSurface,
                borderColor = BlackBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Today's Focused Study Goal",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = BlackText
                            )
                        )
                        Text(
                            text = "Target: ${String.format("%.1f", goalHours)} hrs / day",
                            style = MaterialTheme.typography.bodySmall,
                            color = BlackText
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = CreamSurfaceVariant,
                        border = BorderStroke(1.dp, BlackBorder)
                    ) {
                        Text(
                            text = "${(goalProgress * 100).toInt()}% Done",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = BlackText
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LinearProgressIndicator(
                    progress = { goalProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, BlackBorder, RoundedCornerShape(6.dp)),
                    color = BlackBorder,
                    trackColor = CreamSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Achieved: ${String.format("%.2f", focusedHours)} hrs",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = BlackText
                    )
                    val remaining = (goalHours - focusedHours).coerceAtLeast(0f)
                    Text(
                        text = "Remaining: ${String.format("%.2f", remaining)} hrs",
                        style = MaterialTheme.typography.bodySmall,
                        color = BlackText
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Ambient Focus Mode CTA
                Button(
                    onClick = onLaunchAmbientTimer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, BlackBorder),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlackBorder,
                        contentColor = CreamBackground
                    )
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp), tint = CreamBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Enter Fullscreen Ambient Focus ✨",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CreamBackground
                        )
                    )
                }
            }
        }

        // Quick Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Points Balance",
                    value = "$totalPoints pts",
                    subtitle = "Tap to view wallet",
                    icon = Icons.Default.Favorite,
                    iconTint = PurpleLogo,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Study Streak",
                    value = "$streakDays Days",
                    subtitle = "Active dedication",
                    icon = Icons.Default.LocalFireDepartment,
                    iconTint = BlackText,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Interactive Analytics Section
        item {
            SectionHeader(title = "Study Analytics & Progress")
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CreamSurfaceVariant)
                    .border(1.dp, BlackBorder, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp)),
                    color = if (analyticsTab == 0) BlackBorder else Color.Transparent,
                    onClick = { analyticsTab = 0 }
                ) {
                    Text(
                        text = "Weekly Bar",
                        modifier = Modifier.padding(vertical = 8.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (analyticsTab == 0) CreamBackground else BlackText
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp)),
                    color = if (analyticsTab == 1) BlackBorder else Color.Transparent,
                    onClick = { analyticsTab = 1 }
                ) {
                    Text(
                        text = "Streak Calendar",
                        modifier = Modifier.padding(vertical = 8.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (analyticsTab == 1) CreamBackground else BlackText
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp)),
                    color = if (analyticsTab == 2) BlackBorder else Color.Transparent,
                    onClick = { analyticsTab = 2 }
                ) {
                    Text(
                        text = "88 Chapters",
                        modifier = Modifier.padding(vertical = 8.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (analyticsTab == 2) CreamBackground else BlackText
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        item {
            when (analyticsTab) {
                0 -> {
                    WeeklyStudyChart(
                        weeklyHours = weeklyHours,
                        targetGoalHours = goalHours,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                1 -> {
                    StreakCalendarView(
                        activeDays = activeCalendarDays,
                        currentStreakDays = streakDays,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                2 -> {
                    SubjectProgressDistribution(
                        subjects = subjects,
                        chapters = chapters,
                        onSelectSubject = { sub -> onNavigateToChapters(sub) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Today's Routine Blocks
        item {
            SectionHeader(
                title = "Today's Study Routine (${completedBlocks.size}/${routine.size} Claimed)",
                actionText = "Full Schedule",
                onActionClick = onNavigateToRoutine
            )
        }

        items(routine.take(4)) { block ->
            val isCompleted = completedBlocks.contains(block.id)
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = if (isCompleted) CreamSurfaceVariant else CreamSurface,
                borderColor = BlackBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CreamSurfaceVariant,
                            border = BorderStroke(1.dp, BlackBorder)
                        ) {
                            Box(
                                modifier = Modifier.size(36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isCompleted) Icons.Default.Check else Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = BlackText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "${block.startTime} - ${block.endTime}",
                                style = MaterialTheme.typography.labelSmall,
                                color = BlackText
                            )
                            Text(
                                text = block.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = BlackText
                            )
                        }
                    }

                    if (isCompleted) {
                        Text(
                            text = "Claimed ✓",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = BlackText
                            )
                        )
                    } else {
                        Button(
                            onClick = { repository.claimRoutineBlock(block) },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BlackBorder),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BlackBorder,
                                contentColor = CreamBackground
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (block.points > 0) "+${block.points} pts" else "Done",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CreamBackground
                                )
                            )
                        }
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onNavigateToRoutine,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, BlackBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BlackText),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("View All 16 Routine Blocks", color = BlackText)
            }
        }
    }

    // Dialog for Weekly Report Card & Certificate
    if (showReportDialog) {
        val summary = repository.getWeeklyReportSummary()
        WeeklyReportDialog(
            summary = summary,
            onDismiss = { showReportDialog = false }
        )
    }

    // Dialog for Mystery Gift Unwrapping
    if (viewingGift != null) {
        AlertDialog(
            onDismissRequest = { viewingGift = null },
            title = {
                Text(
                    text = "Unwrap Mystery Gift! 🎁✨",
                    style = MaterialTheme.typography.titleLarge,
                    color = BlackText,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = viewingGift!!.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = BlackText,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "\"${viewingGift!!.message}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BlackText
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CreamSurfaceVariant,
                        border = BorderStroke(1.dp, BlackBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Bonus Bounty: +${viewingGift!!.pointsAmount} Points ⭐",
                                style = MaterialTheme.typography.titleSmall,
                                color = BlackText,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Perk: ${viewingGift!!.perkDescription}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BlackText
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.claimMysteryGift(viewingGift!!.id)
                        viewingGift = null
                    },
                    border = BorderStroke(1.dp, BlackBorder),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlackBorder,
                        contentColor = CreamBackground
                    )
                ) {
                    Text("Claim & Add to Points ❤️", fontWeight = FontWeight.Bold, color = CreamBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewingGift = null }) {
                    Text("Close", color = BlackText)
                }
            },
            containerColor = CreamSurface
        )
    }
}

@Composable
fun MoodButton(
    emoji: String,
    label: String,
    energy: Int,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CreamSurface,
        border = BorderStroke(1.dp, BlackBorder),
        onClick = onClick,
        modifier = Modifier.size(56.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = BlackText)
        }
    }
}
