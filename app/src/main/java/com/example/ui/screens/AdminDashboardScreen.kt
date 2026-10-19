package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TwoHeartsRepository
import com.example.model.ChapterStatus
import com.example.model.DoubtEntry
import com.example.model.RewardStatus
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.WeeklyStudyChart
import com.example.ui.theme.*

@Composable
fun AdminDashboardScreen(
    repository: TwoHeartsRepository,
    onNavigateToChapters: () -> Unit,
    onNavigateToRoutine: () -> Unit,
    onNavigateToRewards: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToDoubts: () -> Unit = {},
    onNavigateToFlashcards: () -> Unit = {}
) {
    val totalPoints by repository.totalPoints.collectAsState()
    val focusedSeconds by repository.todayFocusedSeconds.collectAsState()
    val goalHours by repository.dailyGoalHours.collectAsState()
    val streakDays by repository.currentStreakDays.collectAsState()
    val chapters by repository.chapters.collectAsState()
    val rewardRequests by repository.rewardRequests.collectAsState()
    val weeklyHours by repository.weeklyStudyHours.collectAsState()

    // 10 Features State
    val todayMood by repository.todayMood.collectAsState()
    val doubts by repository.doubts.collectAsState()
    val quests by repository.studyQuests.collectAsState()
    val nudges by repository.nudges.collectAsState()

    val pendingRequests = rewardRequests.filter { it.status == RewardStatus.PENDING }
    val pendingDoubts = doubts.filter { !it.isResolved }
    val completedChaptersCount = chapters.count { it.status == ChapterStatus.COMPLETED }
    val focusedHours = focusedSeconds / 3600f

    // Dialog state for awarding bonus
    var showBonusDialog by remember { mutableStateOf(false) }
    var bonusAmount by remember { mutableStateOf("50") }
    var bonusReason by remember { mutableStateOf("") }

    // Dialog state for deducting points
    var showDeductDialog by remember { mutableStateOf(false) }
    var deductAmount by remember { mutableStateOf("20") }
    var deductReason by remember { mutableStateOf("") }

    // Dialog state for sending love note
    var showLoveNoteDialog by remember { mutableStateOf(false) }
    var loveNoteText by remember { mutableStateOf("") }

    // Dialog state for editing daily goal
    var showGoalDialog by remember { mutableStateOf(false) }
    var goalInput by remember { mutableStateOf(goalHours.toString()) }

    // Dialog state for Mystery Gift
    var showMysteryGiftDialog by remember { mutableStateOf(false) }
    var giftTitle by remember { mutableStateOf("Special Surprise For My Hardworking Girl 🎁") }
    var giftMessage by remember { mutableStateOf("You are doing incredible Surbie! So proud of you ❤️") }
    var giftPoints by remember { mutableStateOf("150") }
    var giftPerk by remember { mutableStateOf("Late night ice cream treat & study pass 🍨") }

    // Dialog state for Study Quest
    var showQuestDialog by remember { mutableStateOf(false) }
    var questTitle by remember { mutableStateOf("High-Yield Sprint") }
    var questDesc by remember { mutableStateOf("Conquer 2 Physics chapters before tonight") }
    var questTarget by remember { mutableStateOf("2") }
    var questBounty by remember { mutableStateOf("100") }
    var questDeadline by remember { mutableStateOf("Tonight, 10:00 PM") }

    // Solving Doubt Dialog
    var solvingDoubt by remember { mutableStateOf<DoubtEntry?>(null) }
    var nudgeToast by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Admin Greeting & Quick Status Card
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
                            text = "Admin Control Hub 👑",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = BlackText
                            )
                        )
                        Text(
                            text = "Monitoring Surbie's study journey with love & care",
                            style = MaterialTheme.typography.bodySmall,
                            color = BlackText
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(CreamSurfaceVariant)
                            .border(1.5.dp, BlackBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = PurpleLogo,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Surbie's Balance", style = MaterialTheme.typography.labelSmall, color = BlackText)
                        Text("$totalPoints pts", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = BlackText))
                    }
                    Column {
                        Text("Today's Focus", style = MaterialTheme.typography.labelSmall, color = BlackText)
                        Text("${String.format("%.1f", focusedHours)} / ${String.format("%.0f", goalHours)}h", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = BlackText))
                    }
                    Column {
                        Text("Active Streak", style = MaterialTheme.typography.labelSmall, color = BlackText)
                        Text("$streakDays Days", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = BlackText))
                    }
                }
            }
        }

        // Feature 6: Instant Nudges & Virtual Hugs
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CreamSurface),
                border = BorderStroke(1.5.dp, BlackBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Instant Nudges & Virtual Hugs 💖",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BlackText
                    )
                    Text(
                        text = "Send real-time alerts directly to Surbie's study screen",
                        style = MaterialTheme.typography.bodySmall,
                        color = BlackText
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                repository.sendNudge("HUG", "Zaid sent you a tight warm hug! You are doing amazing Surbie! 💖")
                                nudgeToast = "Virtual hug sent to Surbie!"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, BlackBorder),
                            colors = ButtonDefaults.buttonColors(containerColor = BlackBorder, contentColor = CreamBackground)
                        ) {
                            Text("Send Hug 💖", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CreamBackground)
                        }

                        Button(
                            onClick = {
                                repository.sendNudge("WATER", "Hydration check! Drink some water and relax your shoulders 💧")
                                nudgeToast = "Water reminder sent!"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, BlackBorder),
                            colors = ButtonDefaults.buttonColors(containerColor = CreamSurfaceVariant, contentColor = BlackText)
                        ) {
                            Text("Water 💧", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlackText)
                        }

                        Button(
                            onClick = {
                                repository.sendNudge("CHEER", "You are a future doctor! Keep conquering those chapters superstar ⭐")
                                nudgeToast = "Cheer sent!"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, BlackBorder),
                            colors = ButtonDefaults.buttonColors(containerColor = CreamSurfaceVariant, contentColor = BlackText)
                        ) {
                            Text("Cheer ⭐", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlackText)
                        }
                    }

                    if (nudgeToast != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = nudgeToast!!,
                            style = MaterialTheme.typography.labelSmall,
                            color = BlackText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Surbie's Mood Check-in Status
        if (todayMood != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CreamSurfaceVariant,
                    border = BorderStroke(1.5.dp, BlackBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = todayMood!!.emoji, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Surbie's Mood: ${todayMood!!.moodLabel}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BlackText
                                )
                                Text(
                                    text = "Energy Battery: ${todayMood!!.energyPercent}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BlackText
                                )
                            }
                        }

                        Button(
                            onClick = { repository.toggleAdminHeartOnMood() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (todayMood!!.adminHeartReaction) BlackBorder else CreamSurface,
                                contentColor = if (todayMood!!.adminHeartReaction) CreamBackground else BlackText
                            ),
                            border = BorderStroke(1.dp, BlackBorder),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (todayMood!!.adminHeartReaction) "Loved ❤️" else "Send Heart ❤️",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (todayMood!!.adminHeartReaction) CreamBackground else BlackText
                            )
                        }
                    }
                }
            }
        }

        // Features 7 & 8: Mystery Gifts & Study Quests CTAs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedCard(
                    onClick = { showMysteryGiftDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = CreamSurface),
                    border = BorderStroke(1.5.dp, BlackBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = BlackText, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Drop Mystery Gift", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = BlackText)
                        Text("+Points & Perk", style = MaterialTheme.typography.labelSmall, color = BlackText)
                    }
                }

                OutlinedCard(
                    onClick = { showQuestDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = CreamSurface),
                    border = BorderStroke(1.5.dp, BlackBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Flag, contentDescription = null, tint = BlackText, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Set Study Quest", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = BlackText)
                        Text("Challenge & Bounty", style = MaterialTheme.typography.labelSmall, color = BlackText)
                    }
                }
            }
        }

        // Pending Doubts Alert
        if (pendingDoubts.isNotEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CreamSurfaceVariant,
                    border = BorderStroke(1.5.dp, BlackBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = BlackText)
                            Column {
                                Text(
                                    text = "${pendingDoubts.size} Academic Doubt(s) from Surbie",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BlackText
                                )
                                Text(
                                    text = "Topic: ${pendingDoubts.first().chapterTitle}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BlackText
                                )
                            }
                        }

                        Button(
                            onClick = { solvingDoubt = pendingDoubts.first() },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BlackBorder),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BlackBorder,
                                contentColor = CreamBackground
                            )
                        ) {
                            Text("Solve & Award", color = CreamBackground)
                        }
                    }
                }
            }
        }

        // Action Buttons Row (Award & Deduct)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showBonusDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, BlackBorder),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlackBorder,
                        contentColor = CreamBackground
                    )
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp), tint = CreamBackground)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Award Points", color = CreamBackground)
                }

                Button(
                    onClick = { showDeductDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, BlackBorder),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CreamSurfaceVariant,
                        contentColor = BlackText
                    )
                ) {
                    Icon(Icons.Default.RemoveCircle, contentDescription = null, modifier = Modifier.size(18.dp), tint = BlackText)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Deduct Points", color = BlackText)
                }
            }
        }

        // Send Love Note CTA
        item {
            Button(
                onClick = { showLoveNoteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, BlackBorder),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CreamSurface,
                    contentColor = BlackText
                )
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(18.dp), tint = PurpleLogo)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pin New Love Note & Encouragement ❤️", fontWeight = FontWeight.Bold, color = BlackText)
            }
        }

        // Weekly Study Analytics
        item {
            SectionHeader(title = "Surbie's Weekly Study Hours")
        }

        item {
            WeeklyStudyChart(
                weeklyHours = weeklyHours,
                targetGoalHours = goalHours,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Subject Breakdown
        item {
            SectionHeader(
                title = "Subject Syllabus Overview ($completedChaptersCount/88 Done)",
                actionText = "Manage",
                onActionClick = onNavigateToChapters
            )
        }

        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = CreamSurface,
                borderColor = BlackBorder
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val subjects = repository.subjects.collectAsState().value
                    subjects.forEach { sub ->
                        val subChapters = chapters.filter { it.subjectId == sub.id }
                        val done = subChapters.count { it.status == ChapterStatus.COMPLETED }
                        val prog = if (subChapters.isNotEmpty()) done.toFloat() / subChapters.size else 0f

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${sub.name} (${sub.code})",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = BlackText
                                )
                                Text(
                                    text = "$done / ${subChapters.size} Chapters (${(prog * 100).toInt()}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BlackText
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { prog },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(1.dp, BlackBorder, RoundedCornerShape(4.dp)),
                                color = BlackBorder,
                                trackColor = CreamSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Routine & Daily Goal Settings
        item {
            SectionHeader(
                title = "Study Schedule & Routine (16 Blocks)",
                actionText = "Adjust",
                onActionClick = onNavigateToRoutine
            )
        }

        item {
            Button(
                onClick = { showGoalDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, BlackBorder),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CreamSurface,
                    contentColor = BlackText
                )
            ) {
                Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp), tint = BlackText)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Set Daily Focus Target (${String.format("%.1f", goalHours)} Hours)", color = BlackText)
            }
        }
    }

    // Award Points Dialog
    if (showBonusDialog) {
        AlertDialog(
            onDismissRequest = { showBonusDialog = false },
            title = { Text("Award Points to Surbie 🎁", fontWeight = FontWeight.Bold, color = BlackText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add bonus points directly with a loving message.", style = MaterialTheme.typography.bodySmall, color = BlackText)
                    OutlinedTextField(
                        value = bonusAmount,
                        onValueChange = { bonusAmount = it },
                        label = { Text("Points (e.g. 50)", color = BlackText) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = bonusReason,
                        onValueChange = { bonusReason = it },
                        label = { Text("Reason / Note", color = BlackText) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pts = bonusAmount.toIntOrNull() ?: 0
                        if (pts > 0) {
                            repository.awardBonusPoints(pts, bonusReason.ifBlank { "Admin Bonus Award ❤️" })
                            showBonusDialog = false
                            bonusReason = ""
                        }
                    },
                    border = BorderStroke(1.dp, BlackBorder),
                    colors = ButtonDefaults.buttonColors(containerColor = BlackBorder, contentColor = CreamBackground)
                ) {
                    Text("Award Points", color = CreamBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBonusDialog = false }) { Text("Cancel", color = BlackText) }
            },
            containerColor = CreamSurface
        )
    }

    // Deduct Points Dialog
    if (showDeductDialog) {
        AlertDialog(
            onDismissRequest = { showDeductDialog = false },
            title = { Text("Deduct Points ⚠️", fontWeight = FontWeight.Bold, color = BlackText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Apply a mild penalty for missed routine blocks or targets.", style = MaterialTheme.typography.bodySmall, color = BlackText)
                    OutlinedTextField(
                        value = deductAmount,
                        onValueChange = { deductAmount = it },
                        label = { Text("Points to Deduct", color = BlackText) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = deductReason,
                        onValueChange = { deductReason = it },
                        label = { Text("Reason (e.g. Skipped Physics Sprint)", color = BlackText) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pts = deductAmount.toIntOrNull() ?: 0
                        if (pts > 0) {
                            repository.deductPoints(pts, deductReason.ifBlank { "Point Deduction" })
                            showDeductDialog = false
                            deductReason = ""
                        }
                    },
                    border = BorderStroke(1.dp, BlackBorder),
                    colors = ButtonDefaults.buttonColors(containerColor = BlackBorder, contentColor = CreamBackground)
                ) {
                    Text("Confirm Deduction", color = CreamBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeductDialog = false }) { Text("Cancel", color = BlackText) }
            },
            containerColor = CreamSurface
        )
    }

    // Send Love Note Dialog
    if (showLoveNoteDialog) {
        AlertDialog(
            onDismissRequest = { showLoveNoteDialog = false },
            title = { Text("Pin a Love Note ❤️", fontWeight = FontWeight.Bold, color = BlackText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("This note will appear pinned on Surbie's home screen.", style = MaterialTheme.typography.bodySmall, color = BlackText)
                    OutlinedTextField(
                        value = loveNoteText,
                        onValueChange = { loveNoteText = it },
                        label = { Text("Message", color = BlackText) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (loveNoteText.isNotBlank()) {
                            repository.sendLoveMessage(loveNoteText, isPinned = true)
                            showLoveNoteDialog = false
                            loveNoteText = ""
                        }
                    },
                    border = BorderStroke(1.dp, BlackBorder),
                    colors = ButtonDefaults.buttonColors(containerColor = BlackBorder, contentColor = CreamBackground)
                ) {
                    Text("Pin Note", color = CreamBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLoveNoteDialog = false }) { Text("Cancel", color = BlackText) }
            },
            containerColor = CreamSurface
        )
    }

    // Edit Daily Goal Dialog
    if (showGoalDialog) {
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("Set Daily Study Goal ⏱️", fontWeight = FontWeight.Bold, color = BlackText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Adjust daily required focus hours for Surbie.", style = MaterialTheme.typography.bodySmall, color = BlackText)
                    OutlinedTextField(
                        value = goalInput,
                        onValueChange = { goalInput = it },
                        label = { Text("Hours per day (e.g. 10.0)", color = BlackText) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val hrs = goalInput.toFloatOrNull() ?: goalHours
                        repository.setDailyGoalHours(hrs)
                        showGoalDialog = false
                    },
                    border = BorderStroke(1.dp, BlackBorder),
                    colors = ButtonDefaults.buttonColors(containerColor = BlackBorder, contentColor = CreamBackground)
                ) {
                    Text("Save Goal", color = CreamBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) { Text("Cancel", color = BlackText) }
            },
            containerColor = CreamSurface
        )
    }

    // Mystery Gift Dialog
    if (showMysteryGiftDialog) {
        AlertDialog(
            onDismissRequest = { showMysteryGiftDialog = false },
            title = { Text("Drop Mystery Gift Box 🎁", fontWeight = FontWeight.Bold, color = BlackText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = giftTitle,
                        onValueChange = { giftTitle = it },
                        label = { Text("Gift Box Title", color = BlackText) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = giftMessage,
                        onValueChange = { giftMessage = it },
                        label = { Text("Love Note Inside", color = BlackText) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    OutlinedTextField(
                        value = giftPoints,
                        onValueChange = { giftPoints = it },
                        label = { Text("Bonus Points (e.g. 150)", color = BlackText) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = giftPerk,
                        onValueChange = { giftPerk = it },
                        label = { Text("Special Perk / Date Promise", color = BlackText) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pts = giftPoints.toIntOrNull() ?: 100
                        if (giftTitle.isNotBlank()) {
                            repository.createMysteryGift(giftTitle, giftMessage, pts, giftPerk)
                            showMysteryGiftDialog = false
                        }
                    },
                    border = BorderStroke(1.dp, BlackBorder),
                    colors = ButtonDefaults.buttonColors(containerColor = BlackBorder, contentColor = CreamBackground)
                ) {
                    Text("Drop Gift Box ❤️", color = CreamBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMysteryGiftDialog = false }) { Text("Cancel", color = BlackText) }
            },
            containerColor = CreamSurface
        )
    }

    // Study Quest Dialog
    if (showQuestDialog) {
        AlertDialog(
            onDismissRequest = { showQuestDialog = false },
            title = { Text("Create Study Quest 🎯", fontWeight = FontWeight.Bold, color = BlackText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = questTitle,
                        onValueChange = { questTitle = it },
                        label = { Text("Quest Title", color = BlackText) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = questDesc,
                        onValueChange = { questDesc = it },
                        label = { Text("Task Description", color = BlackText) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = questTarget,
                            onValueChange = { questTarget = it },
                            label = { Text("Goal Count", color = BlackText) },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = questBounty,
                            onValueChange = { questBounty = it },
                            label = { Text("Points Bounty", color = BlackText) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = questDeadline,
                        onValueChange = { questDeadline = it },
                        label = { Text("Deadline", color = BlackText) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = questTarget.toIntOrNull() ?: 2
                        val bounty = questBounty.toIntOrNull() ?: 100
                        if (questTitle.isNotBlank()) {
                            repository.createQuest(questTitle, questDesc, "STUDY", target, bounty, questDeadline)
                            showQuestDialog = false
                        }
                    },
                    border = BorderStroke(1.dp, BlackBorder),
                    colors = ButtonDefaults.buttonColors(containerColor = BlackBorder, contentColor = CreamBackground)
                ) {
                    Text("Launch Quest 🚀", color = CreamBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuestDialog = false }) { Text("Cancel", color = BlackText) }
            },
            containerColor = CreamSurface
        )
    }

    // Solve Doubt Dialog
    if (solvingDoubt != null) {
        SolveDoubtDialog(
            doubt = solvingDoubt!!,
            onDismiss = { solvingDoubt = null },
            onResolve = { solution ->
                repository.resolveDoubt(solvingDoubt!!.id, solution)
                solvingDoubt = null
            }
        )
    }
}
