package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TwoHeartsRepository
import com.example.model.MilestoneCategory
import com.example.ui.theme.*

@Composable
fun WalletScreen(
    repository: TwoHeartsRepository,
    onNavigateToRewards: () -> Unit
) {
    val totalPoints by repository.totalPoints.collectAsState()
    val pointsHistory by repository.pointsHistory.collectAsState()
    val milestones by repository.milestones.collectAsState()
    val streakDays by repository.currentStreakDays.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: History, 1: Milestones, 2: Rules

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Points Wallet Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CreamSurface),
                border = BorderStroke(1.5.dp, BlackBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Two Hearts Points Wallet",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = BlackText
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$totalPoints pts",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BlackText
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(CreamSurfaceVariant)
                                .border(1.5.dp, BlackBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = PurpleLogo,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Streak: $streakDays Days • Ready to redeem",
                            style = MaterialTheme.typography.bodySmall,
                            color = BlackText
                        )

                        Button(
                            onClick = onNavigateToRewards,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BlackBorder),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BlackBorder,
                                contentColor = CreamBackground
                            )
                        ) {
                            Text(
                                text = "Rewards Catalogue 🎁",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CreamBackground
                                )
                            )
                        }
                    }
                }
            }
        }

        // Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CreamBackground,
                contentColor = BlackText,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = BlackBorder
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Audit History", fontWeight = FontWeight.SemiBold, color = BlackText) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Milestones", fontWeight = FontWeight.SemiBold, color = BlackText) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Points Rules", fontWeight = FontWeight.SemiBold, color = BlackText) }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                // Audit History
                items(pointsHistory, key = { it.id }) { tx ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CreamSurface),
                        border = BorderStroke(1.dp, BlackBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(CreamSurfaceVariant)
                                        .border(1.dp, BlackBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (tx.amount >= 0) Icons.Default.Add else Icons.Default.Remove,
                                        contentDescription = null,
                                        tint = BlackText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = tx.reason,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = BlackText
                                    )
                                    Text(
                                        text = "${tx.timestamp} • by ${tx.createdByRole.displayName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BlackText
                                    )
                                }
                            }

                            Text(
                                text = if (tx.amount > 0) "+${tx.amount}" else "${tx.amount}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BlackText
                                )
                            )
                        }
                    }
                }
            }

            1 -> {
                // Milestones (Streak & Chapters)
                item {
                    Text(
                        text = "Streak Milestones",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = BlackText)
                    )
                }
                items(milestones.filter { it.category == MilestoneCategory.STREAK }) { ms ->
                    MilestoneCard(ms)
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Chapter Completion Milestones (88 Total)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = BlackText)
                    )
                }
                items(milestones.filter { it.category == MilestoneCategory.CHAPTERS }) { ms ->
                    MilestoneCard(ms)
                }
            }

            2 -> {
                // Default Points System Rules
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CreamSurface),
                        border = BorderStroke(1.5.dp, BlackBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Standard Configured Rewards",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = BlackText)
                            )
                            HorizontalDivider(color = BlackBorder)

                            RuleRow("Wake up on schedule (06:00)", "+10 pts")
                            RuleRow("Healthy Breakfast", "+5 pts")
                            RuleRow("Nourishing Lunch", "+5 pts")
                            RuleRow("Cozy Dinner", "+5 pts")
                            RuleRow("Completed study routine block", "+15 pts")
                            RuleRow("Each focused study hour (via timer)", "+10 pts")
                            RuleRow("Full 16-block routine completed in a day", "+50 pts")
                            RuleRow("7-Day Streak milestone unlock", "+100 pts")

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Note: Points are idempotently credited. Admin can also award bonus points or deduct points with an explicit audit reason.",
                                style = MaterialTheme.typography.bodySmall,
                                color = BlackText
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MilestoneCard(milestone: com.example.model.Milestone) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (milestone.isUnlocked) CreamSurfaceVariant else CreamSurface
        ),
        border = BorderStroke(1.5.dp, BlackBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (milestone.isUnlocked) BlackBorder else CreamSurfaceVariant)
                        .border(1.dp, BlackBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (milestone.isUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (milestone.isUnlocked) CreamBackground else BlackText,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = milestone.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = BlackText
                    )
                    Text(
                        text = if (milestone.isUnlocked) "Unlocked: ${milestone.unlockedAt}" else "Target: ${milestone.targetCount} count",
                        style = MaterialTheme.typography.bodySmall,
                        color = BlackText
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(50.dp),
                color = if (milestone.isUnlocked) BlackBorder else CreamSurfaceVariant,
                border = BorderStroke(1.dp, BlackBorder)
            ) {
                Text(
                    text = "+${milestone.pointsReward} pts",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (milestone.isUnlocked) CreamBackground else BlackText
                    )
                )
            }
        }
    }
}

@Composable
fun RuleRow(event: String, reward: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = event, style = MaterialTheme.typography.bodyMedium, color = BlackText)
        Text(text = reward, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = BlackText))
    }
}
