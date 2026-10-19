package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.model.BlockType
import com.example.model.RoutineBlock
import com.example.model.UserRole
import com.example.ui.theme.*

@Composable
fun RoutineScreen(
    repository: TwoHeartsRepository,
    onStartTimer: () -> Unit
) {
    val routine by repository.routine.collectAsState()
    val completedBlocks by repository.completedRoutineIdsToday.collectAsState()
    val goalHours by repository.dailyGoalHours.collectAsState()
    val currentUser by repository.currentUser.collectAsState()

    var showSnack by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Daily Routine Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CreamSurface),
                border = BorderStroke(1.5.dp, BlackBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Synchronized Study Routine",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = BlackText
                        )
                    )
                    Text(
                        text = "16 Configured blocks • ${String.format("%.0f", goalHours)} hours daily focus target",
                        style = MaterialTheme.typography.bodySmall,
                        color = BlackText
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val completionRatio = completedBlocks.size.toFloat() / routine.size.coerceAtLeast(1)
                    LinearProgressIndicator(
                        progress = { completionRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .border(1.dp, BlackBorder, RoundedCornerShape(5.dp)),
                        color = BlackBorder,
                        trackColor = CreamSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${completedBlocks.size} of ${routine.size} Blocks Claimed",
                            style = MaterialTheme.typography.labelSmall,
                            color = BlackText
                        )
                        if (completedBlocks.size == routine.size) {
                            Text(
                                text = "Full Routine Bonus Claimed (+50 pts) 🎉",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = BlackText
                            )
                        } else {
                            Text(
                                text = "+50 pts bonus on completion",
                                style = MaterialTheme.typography.labelSmall,
                                color = BlackText
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Today's Blocks",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = BlackText
                ),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // List of all 16 routine blocks
        items(routine) { block ->
            val isClaimed = completedBlocks.contains(block.id)
            val icon = when (block.blockType) {
                BlockType.WAKE_UP -> Icons.Default.WbSunny
                BlockType.STUDY -> Icons.Default.MenuBook
                BlockType.BREAK -> Icons.Default.Coffee
                BlockType.MEAL -> Icons.Default.Restaurant
                BlockType.REVIEW -> Icons.Default.CheckCircle
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isClaimed) CreamSurfaceVariant else CreamSurface
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // Type Icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CreamSurfaceVariant)
                                .border(1.dp, BlackBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isClaimed) Icons.Default.Check else icon,
                                contentDescription = null,
                                tint = BlackText,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${block.startTime} – ${block.endTime}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = BlackText
                                )
                                Text(
                                    text = "• ${block.blockType.label}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BlackText
                                )
                            }
                            Text(
                                text = block.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = BlackText
                                )
                            )
                        }
                    }

                    // Claim / Done Button
                    if (isClaimed) {
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = BlackBorder,
                            border = BorderStroke(1.dp, BlackBorder)
                        ) {
                            Text(
                                text = "Claimed ✓",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CreamBackground
                                )
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                val success = repository.claimRoutineBlock(block)
                                if (success) {
                                    showSnack = "Completed ${block.title}! +${block.points} pts awarded ❤️"
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, BlackBorder),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BlackBorder,
                                contentColor = CreamBackground
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (block.points > 0) "+${block.points} pts" else "Complete",
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
    }
}
