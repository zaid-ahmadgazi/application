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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TwoHeartsRepository
import com.example.model.Reward
import com.example.model.RewardRequest
import com.example.model.RewardStatus
import com.example.model.UserRole
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

@Composable
fun RewardsScreen(
    repository: TwoHeartsRepository
) {
    val rewards by repository.rewards.collectAsState()
    val rewardRequests by repository.rewardRequests.collectAsState()
    val totalPoints by repository.totalPoints.collectAsState()
    val currentUser by repository.currentUser.collectAsState()

    var selectedRewardToClaim by remember { mutableStateOf<Reward?>(null) }
    var studentNoteInput by remember { mutableStateOf("") }
    var snackMessage by remember { mutableStateOf<String?>(null) }

    // Admin review dialog
    var selectedRequestToReview by remember { mutableStateOf<RewardRequest?>(null) }
    var adminReviewNote by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Balance Header Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, BlackBorder),
                colors = CardDefaults.cardColors(containerColor = CreamSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Current Balance",
                            style = MaterialTheme.typography.labelMedium,
                            color = BlackText
                        )
                        Text(
                            text = "$totalPoints points",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = BlackText
                            )
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = CreamSurfaceVariant,
                        border = BorderStroke(1.dp, BlackBorder)
                    ) {
                        Text(
                            text = if (currentUser.role == UserRole.ADMIN) "Admin Review Mode" else "Student Redemption",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = BlackText
                            )
                        )
                    }
                }
            }
        }

        // Active / Recent Requests
        if (rewardRequests.isNotEmpty()) {
            item {
                SectionHeader(title = "Reward Requests & Fulfillments")
            }

            items(rewardRequests, key = { it.id }) { req ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, BlackBorder),
                    colors = CardDefaults.cardColors(containerColor = CreamSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stars,
                                    contentDescription = null,
                                    tint = PurpleLogo
                                )
                                Column {
                                    Text(
                                        text = req.rewardTitle,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = BlackText
                                        )
                                    )
                                    Text(
                                        text = "${req.pointsCost} pts • Requested ${req.requestedAt}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BlackMuted
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(50.dp),
                                color = CreamSurfaceVariant,
                                border = BorderStroke(1.dp, BlackBorder)
                            ) {
                                Text(
                                    text = req.status.label,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = BlackText
                                    )
                                )
                            }
                        }

                        if (req.studentNote.isNotBlank()) {
                            Text(
                                text = "“${req.studentNote}”",
                                style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                color = BlackText
                            )
                        }

                        if (req.adminNote.isNotBlank()) {
                            Text(
                                text = "Admin Note: ${req.adminNote}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = BlackText
                            )
                        }

                        // Admin actions for pending or approved requests
                        if (currentUser.role == UserRole.ADMIN && req.status != RewardStatus.FULFILLED && req.status != RewardStatus.REJECTED) {
                            HorizontalDivider(
                                color = BlackBorder,
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        selectedRequestToReview = req
                                    },
                                    border = BorderStroke(1.5.dp, BlackBorder),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = BlackText
                                    )
                                ) {
                                    Text("Review / Fulfill", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Reward Catalogue
        item {
            SectionHeader(title = "Rewards Catalogue (5 Items)")
        }

        items(rewards, key = { it.id }) { rew ->
            val canAfford = totalPoints >= rew.pointsCost
            val icon = when (rew.id) {
                "rew_1" -> Icons.Default.PhotoCamera
                "rew_2" -> Icons.Default.Mic
                "rew_3" -> Icons.Default.Favorite
                "rew_4" -> Icons.Default.Videocam
                else -> Icons.Default.Stars
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, BlackBorder),
                colors = CardDefaults.cardColors(containerColor = CreamSurface)
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
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(CreamSurfaceVariant)
                                .border(1.dp, BlackBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (rew.id == "rew_3") PurpleLogo else BlackText,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = rew.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BlackText
                                )
                            )
                            Text(
                                text = rew.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = BlackMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${rew.pointsCost} Points",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BlackText
                                )
                            )
                        }
                    }

                    if (currentUser.role == UserRole.STUDENT) {
                        Button(
                            onClick = {
                                selectedRewardToClaim = rew
                                studentNoteInput = ""
                            },
                            enabled = canAfford,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.5.dp, BlackBorder),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canAfford) BlackBorder else CreamSurfaceVariant,
                                contentColor = if (canAfford) CreamBackground else BlackMuted,
                                disabledContainerColor = CreamSurfaceVariant,
                                disabledContentColor = BlackMuted
                            )
                        ) {
                            Text(
                                text = if (canAfford) "Claim" else "Need Pts",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }

    // Student Claim Note Dialog
    if (selectedRewardToClaim != null) {
        val rew = selectedRewardToClaim!!
        AlertDialog(
            onDismissRequest = { selectedRewardToClaim = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = CreamSurface,
            title = {
                Text(
                    text = "Request ${rew.title}",
                    fontWeight = FontWeight.Bold,
                    color = BlackText
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Cost: ${rew.pointsCost} points (Remaining after: ${totalPoints - rew.pointsCost})",
                        color = BlackText
                    )
                    OutlinedTextField(
                        value = studentNoteInput,
                        onValueChange = { studentNoteInput = it },
                        label = { Text("Personal note or request details", color = BlackText) },
                        placeholder = { Text("e.g. Can't wait for this after botany revision!", color = BlackMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BlackBorder,
                            unfocusedBorderColor = BlackBorder,
                            focusedTextColor = BlackText,
                            unfocusedTextColor = BlackText
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val (ok, msg) = repository.requestReward(rew, studentNoteInput)
                        snackMessage = msg
                        selectedRewardToClaim = null
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, BlackBorder),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlackBorder,
                        contentColor = CreamBackground
                    )
                ) {
                    Text("Confirm Claim", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { selectedRewardToClaim = null },
                    border = BorderStroke(1.5.dp, BlackBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BlackText)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Admin Review Dialog
    if (selectedRequestToReview != null) {
        val req = selectedRequestToReview!!
        AlertDialog(
            onDismissRequest = { selectedRequestToReview = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = CreamSurface,
            title = {
                Text(
                    text = "Review Request: ${req.rewardTitle}",
                    fontWeight = FontWeight.Bold,
                    color = BlackText
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Student note: “${req.studentNote}”",
                        color = BlackText
                    )
                    OutlinedTextField(
                        value = adminReviewNote,
                        onValueChange = { adminReviewNote = it },
                        label = { Text("Admin message to Surbie", color = BlackText) },
                        placeholder = { Text("e.g. Sent via voice note / recording completed!", color = BlackMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BlackBorder,
                            unfocusedBorderColor = BlackBorder,
                            focusedTextColor = BlackText,
                            unfocusedTextColor = BlackText
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                    )
                    Text(
                        text = "Select Action:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BlackText
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                repository.updateRewardRequestStatus(req.id, RewardStatus.APPROVED, adminReviewNote)
                                selectedRequestToReview = null
                                adminReviewNote = ""
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.5.dp, BlackBorder),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BlackBorder,
                                contentColor = CreamBackground
                            )
                        ) {
                            Text("Approve", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                repository.updateRewardRequestStatus(req.id, RewardStatus.FULFILLED, adminReviewNote)
                                selectedRequestToReview = null
                                adminReviewNote = ""
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.5.dp, BlackBorder),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CreamSurfaceVariant,
                                contentColor = BlackText
                            )
                        ) {
                            Text("Fulfilled", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                repository.updateRewardRequestStatus(req.id, RewardStatus.REJECTED, adminReviewNote)
                                selectedRequestToReview = null
                                adminReviewNote = ""
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.5.dp, BlackBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = BlackText
                            )
                        ) {
                            Text("Reject", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedRequestToReview = null }) {
                    Text("Close", color = BlackText, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
