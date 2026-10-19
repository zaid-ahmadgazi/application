package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.WeeklyReportSummary
import com.example.ui.theme.*

@Composable
fun WeeklyReportDialog(
    summary: WeeklyReportSummary,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CreamSurface,
            border = BorderStroke(2.dp, BlackBorder),
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Seal / Trophy Icon (Purple for logo/award)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(CreamSurfaceVariant)
                        .border(1.5.dp, BlackBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = PurpleLogo,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "CERTIFICATE OF EXCELLENCE",
                    style = MaterialTheme.typography.labelMedium,
                    color = BlackText,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Awarded to Surbie 🌸",
                    style = MaterialTheme.typography.headlineMedium,
                    color = BlackText,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "For outstanding dedication, focus, and unwavering progress toward NEET mastery.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BlackText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = BlackBorder)
                Spacer(modifier = Modifier.height(18.dp))

                // 4-box metrics grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ReportStatBox(
                        title = "Focus Time",
                        value = "${summary.totalMinutes}m",
                        icon = Icons.Default.Timer
                    )
                    ReportStatBox(
                        title = "Chapters Done",
                        value = "${summary.chaptersCompleted}",
                        icon = Icons.Default.MenuBook
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ReportStatBox(
                        title = "Active Streak",
                        value = "${summary.currentStreak} Days 🔥",
                        icon = Icons.Default.LocalFireDepartment
                    )
                    ReportStatBox(
                        title = "Top Focus",
                        value = summary.topSubject,
                        icon = Icons.Default.AutoGraph
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Admin Signature Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CreamSurfaceVariant,
                    border = BorderStroke(1.dp, BlackBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Verified & Signed by:",
                                style = MaterialTheme.typography.labelSmall,
                                color = BlackText
                            )
                            Text(
                                text = "Zaid (Admin ❤️)",
                                style = MaterialTheme.typography.labelLarge,
                                color = BlackText,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Date:",
                                style = MaterialTheme.typography.labelSmall,
                                color = BlackText
                            )
                            Text(
                                text = summary.certificateDate,
                                style = MaterialTheme.typography.labelMedium,
                                color = BlackText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlackBorder,
                        contentColor = CreamBackground
                    ),
                    border = BorderStroke(1.5.dp, BlackBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Proud of Myself! 💖", fontWeight = FontWeight.Bold, color = CreamBackground)
                }
            }
        }
    }
}

@Composable
fun ReportStatBox(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = CreamSurface,
        border = BorderStroke(1.5.dp, BlackBorder),
        modifier = Modifier.width(130.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BlackText,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = BlackText,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = BlackText
            )
        }
    }
}
