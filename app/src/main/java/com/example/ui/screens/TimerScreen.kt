package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TwoHeartsRepository
import com.example.model.Chapter
import com.example.model.Subject
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    repository: TwoHeartsRepository,
    onLaunchAmbientMode: () -> Unit = {},
    onFinishSession: () -> Unit
) {
    val subjects by repository.subjects.collectAsState()
    val chapters by repository.chapters.collectAsState()

    var selectedSubject by remember { mutableStateOf<Subject?>(subjects.firstOrNull()) }
    var selectedChapter by remember { mutableStateOf<Chapter?>(null) }
    var sessionNotes by remember { mutableStateOf("") }

    var isRunning by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableLongStateOf(0L) }

    var subjectMenuExpanded by remember { mutableStateOf(false) }
    var chapterMenuExpanded by remember { mutableStateOf(false) }

    var showCompleteDialog by remember { mutableStateOf(false) }

    // Timer tick loop
    LaunchedEffect(isRunning, isPaused) {
        if (isRunning && !isPaused) {
            while (true) {
                delay(1000L)
                elapsedSeconds++
            }
        }
    }

    // Filter chapters by selected subject
    val availableChapters = remember(selectedSubject, chapters) {
        if (selectedSubject != null) {
            chapters.filter { it.subjectId == selectedSubject?.id }
        } else chapters
    }

    val hours = elapsedSeconds / 3600
    val minutes = (elapsedSeconds % 3600) / 60
    val seconds = elapsedSeconds % 60
    val timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Focused Study Timer ⏱️",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = BlackText
            )
        )

        Text(
            text = "Record actual focused study time. Each completed hour earns +10 points!",
            style = MaterialTheme.typography.bodySmall,
            color = BlackText,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onLaunchAmbientMode,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.5.dp, BlackBorder),
            colors = ButtonDefaults.buttonColors(
                containerColor = CreamSurface,
                contentColor = BlackText
            )
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BlackText, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Switch to Fullscreen Ambient Mode ✨", fontWeight = FontWeight.Bold, color = BlackText)
        }

        // Subject & Chapter selection dropdowns
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Subject Dropdown
            ExposedDropdownMenuBox(
                expanded = subjectMenuExpanded,
                onExpandedChange = { if (!isRunning) subjectMenuExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedSubject?.name ?: "Select Subject",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Subject", color = BlackText) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectMenuExpanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BlackBorder,
                        unfocusedBorderColor = BlackBorder,
                        focusedTextColor = BlackText,
                        unfocusedTextColor = BlackText
                    ),
                    modifier = Modifier.menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = subjectMenuExpanded,
                    onDismissRequest = { subjectMenuExpanded = false }
                ) {
                    subjects.forEach { sub ->
                        DropdownMenuItem(
                            text = { Text("${sub.name} (${sub.code})", color = BlackText) },
                            onClick = {
                                selectedSubject = sub
                                selectedChapter = null
                                subjectMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Chapter Dropdown
            ExposedDropdownMenuBox(
                expanded = chapterMenuExpanded,
                onExpandedChange = { if (!isRunning) chapterMenuExpanded = it },
                modifier = Modifier.weight(1.2f)
            ) {
                OutlinedTextField(
                    value = selectedChapter?.title ?: "Select Chapter",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Chapter", color = BlackText) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = chapterMenuExpanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BlackBorder,
                        unfocusedBorderColor = BlackBorder,
                        focusedTextColor = BlackText,
                        unfocusedTextColor = BlackText
                    ),
                    modifier = Modifier.menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = chapterMenuExpanded,
                    onDismissRequest = { chapterMenuExpanded = false }
                ) {
                    availableChapters.forEach { chap ->
                        DropdownMenuItem(
                            text = { Text(chap.title, color = BlackText) },
                            onClick = {
                                selectedChapter = chap
                                chapterMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Circular Timer Display
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(CreamSurfaceVariant)
                .border(2.dp, BlackBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(206.dp)
                    .clip(CircleShape)
                    .background(CreamSurface)
                    .border(1.5.dp, BlackBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = PurpleLogo,
                        modifier = Modifier.size(26.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = BlackText,
                            fontSize = 36.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = when {
                            !isRunning -> "Ready"
                            isPaused -> "Paused"
                            else -> "Deep Focus 💭"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = BlackText
                        )
                    )
                }
            }
        }

        // Control Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isRunning) {
                Button(
                    onClick = {
                        isRunning = true
                        isPaused = false
                    },
                    modifier = Modifier
                        .height(54.dp)
                        .widthIn(min = 160.dp),
                    shape = RoundedCornerShape(27.dp),
                    border = BorderStroke(1.5.dp, BlackBorder),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlackBorder,
                        contentColor = CreamBackground
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp), tint = CreamBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Focus", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = CreamBackground)
                }
            } else {
                if (!isPaused) {
                    Button(
                        onClick = { isPaused = true },
                        modifier = Modifier.height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        border = BorderStroke(1.5.dp, BlackBorder),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CreamSurfaceVariant,
                            contentColor = BlackText
                        )
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = null, tint = BlackText)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pause", color = BlackText)
                    }
                } else {
                    Button(
                        onClick = { isPaused = false },
                        modifier = Modifier.height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        border = BorderStroke(1.5.dp, BlackBorder),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BlackBorder,
                            contentColor = CreamBackground
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CreamBackground)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Resume", color = CreamBackground)
                    }
                }

                Button(
                    onClick = { showCompleteDialog = true },
                    modifier = Modifier.height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    border = BorderStroke(1.5.dp, BlackBorder),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlackBorder,
                        contentColor = CreamBackground
                    )
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, tint = CreamBackground)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Finish & Log", color = CreamBackground)
                }
            }
        }

        // Session Notes input
        OutlinedTextField(
            value = sessionNotes,
            onValueChange = { sessionNotes = it },
            label = { Text("Session Notes & Key Concepts", color = BlackText) },
            placeholder = { Text("e.g. Practiced 15 kinematics numericals, reviewed formula sheet") },
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

        // Encouragement card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CreamSurfaceVariant),
            border = BorderStroke(1.dp, BlackBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = PurpleLogo)
                Text(
                    text = "“One deep breath at a time. Your dedication today is carving a masterpiece for tomorrow.”",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = BlackText
                    )
                )
            }
        }

        // Feature 9: Interactive Soundscapes & Lofi Radio
        val soundscapes by repository.soundscapes.collectAsState()
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CreamSurface),
            border = BorderStroke(1.5.dp, BlackBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Headphones, contentDescription = null, tint = BlackText, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lofi & Focus Soundscapes 🎧",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BlackText
                        )
                    }
                    val activeTrackCount = soundscapes.count { it.isPlaying }
                    if (activeTrackCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CreamSurfaceVariant,
                            border = BorderStroke(1.dp, BlackBorder)
                        ) {
                            Text(
                                text = "$activeTrackCount Active 🎵",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = BlackText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                soundscapes.forEach { track ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (track.isPlaying) CreamSurfaceVariant else CreamSurface,
                        border = BorderStroke(1.dp, BlackBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                IconButton(
                                    onClick = { repository.toggleSoundscape(track.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (track.isPlaying) Icons.Default.PauseCircleFilled else Icons.Default.PlayCircleFilled,
                                        contentDescription = null,
                                        tint = BlackText,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = track.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = BlackText
                                    )
                                    Text(
                                        text = track.subtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BlackText
                                    )
                                }
                            }

                            if (track.isPlaying) {
                                Slider(
                                    value = track.volume,
                                    onValueChange = { repository.updateSoundscapeVolume(track.id, it) },
                                    modifier = Modifier.width(100.dp),
                                    colors = SliderDefaults.colors(
                                        thumbColor = BlackBorder,
                                        activeTrackColor = BlackBorder,
                                        inactiveTrackColor = CreamSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Complete Session Confirmation Dialog
    if (showCompleteDialog) {
        val earnedPts = (elapsedSeconds / 3600).toInt() * 10
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text("Log Study Session 🎉", fontWeight = FontWeight.Bold, color = BlackText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Focused Time: $timeFormatted", color = BlackText)
                    Text("Subject: ${selectedSubject?.name ?: "General Focus"}", color = BlackText)
                    if (selectedChapter != null) {
                        Text("Chapter: ${selectedChapter?.title}", color = BlackText)
                    }
                    if (earnedPts > 0) {
                        Text("Points Earned: +$earnedPts pts 💎", fontWeight = FontWeight.Bold, color = BlackText)
                    } else {
                        Text("Note: Complete a full hour (60m) to earn +10 bonus points!", color = BlackText, style = MaterialTheme.typography.bodySmall)
                    }
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
                        isRunning = false
                        isPaused = false
                        elapsedSeconds = 0L
                        sessionNotes = ""
                        onFinishSession()
                    },
                    border = BorderStroke(1.dp, BlackBorder),
                    colors = ButtonDefaults.buttonColors(containerColor = BlackBorder, contentColor = CreamBackground)
                ) {
                    Text("Save & Complete", color = CreamBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) { Text("Cancel", color = BlackText) }
            },
            containerColor = CreamSurface
        )
    }
}
