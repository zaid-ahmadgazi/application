package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TwoHeartsRepository
import com.example.model.Chapter
import com.example.model.Subject
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun AmbientFocusScreen(
    repository: TwoHeartsRepository,
    initialSubject: Subject? = null,
    initialChapter: Chapter? = null,
    onCloseAmbient: () -> Unit
) {
    var isRunning by remember { mutableStateOf(true) }
    var isPaused by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableLongStateOf(0L) }

    val subjects by repository.subjects.collectAsState()
    var selectedSubject by remember { mutableStateOf(initialSubject ?: subjects.firstOrNull()) }
    var selectedChapter by remember { mutableStateOf(initialChapter) }

    var quoteIndex by remember { mutableIntStateOf(0) }
    val quotes = repository.ambientQuotes

    var showCompleteDialog by remember { mutableStateOf(false) }
    var sessionNotes by remember { mutableStateOf("") }

    // Timer loop
    LaunchedEffect(isRunning, isPaused) {
        while (isRunning && !isPaused) {
            delay(1000L)
            elapsedSeconds++
        }
    }

    // Quote rotation loop (every 20s)
    LaunchedEffect(Unit) {
        while (true) {
            delay(20000L)
            quoteIndex = (quoteIndex + 1) % quotes.size
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    BackHandler {
        if (isRunning && elapsedSeconds > 0) {
            showCompleteDialog = true
        } else {
            onCloseAmbient()
        }
    }

    val hours = elapsedSeconds / 3600
    val minutes = (elapsedSeconds % 3600) / 60
    val seconds = elapsedSeconds % 60
    val timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Ambient Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (elapsedSeconds > 0) showCompleteDialog = true else onCloseAmbient()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CreamSurfaceVariant)
                        .border(1.dp, BlackBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit Ambient Mode",
                        tint = BlackText
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = CreamSurfaceVariant,
                    border = BorderStroke(1.dp, BlackBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (!isPaused) BlackBorder else CreamSurfaceVariant)
                                .border(0.5.dp, BlackBorder, CircleShape)
                        )
                        Text(
                            text = if (!isPaused) "AMBIENT FOCUS" else "PAUSED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = BlackText
                            )
                        )
                    }
                }

                IconButton(
                    onClick = { quoteIndex = (quoteIndex + 1) % quotes.size },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CreamSurfaceVariant)
                        .border(1.dp, BlackBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "New Quote",
                        tint = BlackText
                    )
                }
            }

            // Subject / Chapter pills
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = selectedSubject?.name ?: "General Focus Session",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = BlackText
                    )
                )
                if (selectedChapter != null) {
                    Text(
                        text = selectedChapter!!.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = BlackText
                    )
                }
            }

            // Progress Canvas with Digital Timer
            Box(
                modifier = Modifier
                    .size(280.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val radius = (size.minDimension / 2) * 0.90f * pulseScale

                    // Track arc
                    drawCircle(
                        color = CreamSurfaceVariant,
                        radius = radius,
                        center = center,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Progress arc in solid black
                    val sweep = (seconds / 60f) * 360f
                    drawArc(
                        color = BlackBorder,
                        startAngle = -90f,
                        sweepAngle = sweep.coerceAtLeast(6f),
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Digital Timer typography in center
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = PurpleLogo,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BlackText,
                            letterSpacing = 2.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (!isPaused) "DEEP STUDY IN PROGRESS" else "SESSION PAUSED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            color = BlackText
                        )
                    )
                }
            }

            // Rotating Inspirational Quote Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { quoteIndex = (quoteIndex + 1) % quotes.size },
                backgroundColor = CreamSurface,
                borderColor = BlackBorder,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CreamSurfaceVariant)
                            .border(1.dp, BlackBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = null,
                            tint = BlackText,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "“${quotes[quoteIndex]}”",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = BlackText,
                                lineHeight = 20.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "— From Admin with Love • Tap for next",
                            style = MaterialTheme.typography.labelSmall,
                            color = BlackText
                        )
                    }
                }
            }

            // Control Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause / Resume Button
                Button(
                    onClick = { isPaused = !isPaused },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CreamSurface,
                        contentColor = BlackText
                    ),
                    border = BorderStroke(1.5.dp, BlackBorder)
                ) {
                    Icon(
                        imageVector = if (!isPaused) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = BlackText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (!isPaused) "Pause" else "Resume",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = BlackText
                    )
                }

                // Stop & Log Session Button
                Button(
                    onClick = { showCompleteDialog = true },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    border = BorderStroke(1.5.dp, BlackBorder),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlackBorder,
                        contentColor = CreamBackground
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp), tint = CreamBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Finish & Save",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = CreamBackground
                    )
                }
            }
        }
    }

    // Complete Session Dialog
    if (showCompleteDialog) {
        val earnedPts = (elapsedSeconds / 3600).toInt() * 10
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = PurpleLogo)
                    Text("Save Focused Study Session", fontWeight = FontWeight.Bold, color = BlackText)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Total Focused Time: $timeFormatted",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = BlackText)
                    )
                    Text("Subject: ${selectedSubject?.name ?: "General Focus"}", color = BlackText)
                    if (selectedChapter != null) {
                        Text("Chapter: ${selectedChapter?.title}", color = BlackText)
                    }
                    if (earnedPts > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CreamSurfaceVariant,
                            border = BorderStroke(1.dp, BlackBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🎉 Earned +$earnedPts Points (10 pts/hr)!",
                                modifier = Modifier.padding(10.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = BlackText)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = sessionNotes,
                        onValueChange = { sessionNotes = it },
                        label = { Text("What did you conquer in this session?", color = BlackText) },
                        placeholder = { Text("e.g. Practiced 20 kinematics questions, revised formulas") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BlackBorder,
                            unfocusedBorderColor = BlackBorder,
                            focusedTextColor = BlackText,
                            unfocusedTextColor = BlackText
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.recordStudySession(
                            subject = selectedSubject,
                            chapter = selectedChapter,
                            durationSeconds = elapsedSeconds,
                            notes = sessionNotes
                        )
                        showCompleteDialog = false
                        onCloseAmbient()
                    },
                    border = BorderStroke(1.dp, BlackBorder),
                    colors = ButtonDefaults.buttonColors(containerColor = BlackBorder, contentColor = CreamBackground)
                ) {
                    Text("Save Session", color = CreamBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) {
                    Text("Keep Studying", color = BlackText)
                }
            },
            containerColor = CreamSurface
        )
    }
}
