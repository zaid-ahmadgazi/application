package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Chapter
import com.example.model.ChapterStatus
import com.example.model.Subject
import com.example.ui.theme.*

/**
 * Editorial Card with Cream background and crisp Black border.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    backgroundColor: Color = CreamSurface,
    borderColor: Color = BlackBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = shape
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            content = content
        )
    }
}

/**
 * Interactive Weekly Study Hours Bar Chart built with pure Compose Canvas.
 */
@Composable
fun WeeklyStudyChart(
    weeklyHours: Map<String, Float>,
    targetGoalHours: Float = 12.0f,
    modifier: Modifier = Modifier
) {
    var selectedDay by remember { mutableStateOf<String?>(null) }
    var selectedHours by remember { mutableStateOf<Float?>(null) }

    GlassCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Weekly Focus Analytics",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = BlackText
                    )
                )
                Text(
                    text = "Target: ${String.format("%.0f", targetGoalHours)}h daily • Tap any bar for details",
                    style = MaterialTheme.typography.bodySmall,
                    color = BlackText
                )
            }

            if (selectedDay != null && selectedHours != null) {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = BlackBorder,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BlackBorder)
                ) {
                    Text(
                        text = "$selectedDay: ${String.format("%.1f", selectedHours)} hrs",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CreamBackground
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        val days = weeklyHours.keys.toList()
        val values = weeklyHours.values.toList()
        val maxHour = (values.maxOrNull() ?: 12f).coerceAtLeast(targetGoalHours) + 2f

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .pointerInput(weeklyHours) {
                    detectTapGestures { offset ->
                        val barWidthWithSpacing = size.width / days.size
                        val index = (offset.x / barWidthWithSpacing).toInt().coerceIn(0, days.size - 1)
                        selectedDay = days[index]
                        selectedHours = values[index]
                    }
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height - 30f // reserve space for text
            val slotWidth = canvasWidth / days.size
            val barWidth = slotWidth * 0.48f

            // Draw target goal dashed line
            val goalY = canvasHeight - (targetGoalHours / maxHour) * canvasHeight
            drawLine(
                color = BlackBorder,
                start = Offset(0f, goalY),
                end = Offset(canvasWidth, goalY),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            )

            // Draw bars
            days.forEachIndexed { i, day ->
                val hours = values[i]
                val barHeight = (hours / maxHour) * canvasHeight
                val barLeft = i * slotWidth + (slotWidth - barWidth) / 2
                val barTop = canvasHeight - barHeight

                val isSelected = selectedDay == day

                drawRoundRect(
                    color = if (isSelected) PurpleLogo else BlackBorder,
                    topLeft = Offset(barLeft, barTop),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }
        }

        // X-Axis Day labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            days.forEach { day ->
                val isSelected = selectedDay == day
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) PurpleLogo else BlackText
                    )
                )
            }
        }
    }
}

/**
 * Interactive 30-Day Streak Calendar View with heart markers on active study days.
 */
@Composable
fun StreakCalendarView(
    activeDays: Set<Int>,
    currentStreakDays: Int,
    modifier: Modifier = Modifier
) {
    var selectedDayNumber by remember { mutableStateOf<Int?>(18) }

    GlassCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = BlackText,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "30-Day Streak Calendar",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BlackText
                        )
                    )
                    Text(
                        text = "$currentStreakDays Day Active Streak • Oct 2026",
                        style = MaterialTheme.typography.bodySmall,
                        color = BlackText
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(50.dp),
                color = CreamSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, BlackBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        tint = PurpleLogo,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${activeDays.size}/30 Days",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = BlackText
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Day Status Text
        if (selectedDayNumber != null) {
            val isActive = activeDays.contains(selectedDayNumber!!)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = CreamSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, BlackBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "October ${selectedDayNumber}, 2026",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = BlackText
                    )
                    Text(
                        text = if (isActive) "Verified Study Day (+Focus logged)" else "Rest Day",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = BlackText
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Calendar Grid (30 days, 6 columns x 5 rows)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (row in 0 until 5) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (col in 0 until 6) {
                        val day = row * 6 + col + 1
                        if (day <= 30) {
                            val isActive = activeDays.contains(day)
                            val isSelected = selectedDayNumber == day

                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when {
                                            isSelected -> BlackBorder
                                            isActive -> CreamSurfaceVariant
                                            else -> CreamSurface
                                        }
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = BlackBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedDayNumber = day },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "$day",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isActive || isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isSelected -> CreamBackground
                                                else -> BlackText
                                            }
                                        )
                                    )
                                    if (isActive) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = null,
                                            tint = if (isSelected) CreamBackground else PurpleLogo,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Subject Progress Distribution Cards for the 88 Chapters
 */
@Composable
fun SubjectProgressDistribution(
    subjects: List<Subject>,
    chapters: List<Chapter>,
    onSelectSubject: (Subject) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Text(
            text = "Curriculum Mastery (88 Chapters)",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = BlackText
            )
        )
        Text(
            text = "Physics (30) • Chemistry (25) • Zoology (16) • Botany (17)",
            style = MaterialTheme.typography.bodySmall,
            color = BlackText
        )

        Spacer(modifier = Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            subjects.forEach { sub ->
                val subChapters = chapters.filter { it.subjectId == sub.id }
                val done = subChapters.count { it.status == ChapterStatus.COMPLETED }
                val total = subChapters.size.coerceAtLeast(1)
                val ratio = done.toFloat() / total

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectSubject(sub) },
                    color = CreamSurfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BlackBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(BlackBorder)
                                )
                                Text(
                                    text = sub.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = BlackText
                                )
                            }

                            Text(
                                text = "$done / $total Chapters (${(ratio * 100).toInt()}%)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = BlackText
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .border(0.5.dp, BlackBorder, RoundedCornerShape(4.dp)),
                            color = BlackBorder,
                            trackColor = CreamSurface
                        )
                    }
                }
            }
        }
    }
}
