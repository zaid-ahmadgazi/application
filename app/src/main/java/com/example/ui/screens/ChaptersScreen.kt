package com.example.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TwoHeartsRepository
import com.example.model.Chapter
import com.example.model.ChapterStatus
import com.example.model.Subject
import com.example.model.UserRole
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaptersScreen(
    repository: TwoHeartsRepository,
    initialSubjectId: String? = null,
    onStartTimerForChapter: (Chapter) -> Unit
) {
    val subjects by repository.subjects.collectAsState()
    val chapters by repository.chapters.collectAsState()
    val currentUser by repository.currentUser.collectAsState()

    var selectedSubjectId by remember(initialSubjectId) { mutableStateOf<String?>(initialSubjectId ?: "sub_physics") }
    var selectedStatusFilter by remember { mutableStateOf<ChapterStatus?>(null) }
    var onlyHighYield by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Dialog states
    var selectedChapterForDetail by remember { mutableStateOf<Chapter?>(null) }
    var noteInput by remember { mutableStateOf("") }

    // Admin rename dialog
    var chapterToRename by remember { mutableStateOf<Chapter?>(null) }
    var renameInput by remember { mutableStateOf("") }

    // Admin add chapter dialog
    var showAddDialog by remember { mutableStateOf(false) }
    var addChapterTitle by remember { mutableStateOf("") }

    // Filter chapters
    val filteredChapters = remember(chapters, selectedSubjectId, selectedStatusFilter, onlyHighYield, searchQuery) {
        chapters.filter { chap ->
            val matchSub = selectedSubjectId == null || chap.subjectId == selectedSubjectId
            val matchStatus = selectedStatusFilter == null || chap.status == selectedStatusFilter
            val matchYield = !onlyHighYield || chap.yieldLevel == "High"
            val matchQuery = searchQuery.isBlank() || chap.title.contains(searchQuery, ignoreCase = true) || chap.notes.contains(searchQuery, ignoreCase = true)
            matchSub && matchStatus && matchYield && matchQuery
        }
    }

    val totalCount = chapters.size
    val completedCount = chapters.count { it.status == ChapterStatus.COMPLETED }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Title and Overall Progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Syllabus Chapters",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = BlackText
                    )
                )
                Text(
                    text = "$completedCount of $totalCount Completed (${((completedCount.toFloat() / totalCount.coerceAtLeast(1)) * 100).toInt()}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = BlackText
                )
            }

            if (currentUser.role == UserRole.ADMIN) {
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(BlackBorder)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Chapter", tint = CreamBackground)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search 88 chapters or notes...", color = BlackText) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BlackText) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = BlackText)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BlackBorder,
                unfocusedBorderColor = BlackBorder,
                focusedTextColor = BlackText,
                unfocusedTextColor = BlackText
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Subject Horizontal Tabs
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedSubjectId == null,
                    onClick = { selectedSubjectId = null },
                    label = { Text("All (88)", color = if (selectedSubjectId == null) CreamBackground else BlackText) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BlackBorder,
                        containerColor = CreamSurface
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedSubjectId == null,
                        borderColor = BlackBorder
                    )
                )
            }
            items(subjects) { sub ->
                val subCount = chapters.count { it.subjectId == sub.id }
                val subDone = chapters.count { it.subjectId == sub.id && it.status == ChapterStatus.COMPLETED }
                val isSelected = selectedSubjectId == sub.id
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedSubjectId = sub.id },
                    label = { Text("${sub.name} ($subDone/$subCount)", color = if (isSelected) CreamBackground else BlackText) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BlackBorder,
                        containerColor = CreamSurface
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = BlackBorder
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Status Filter Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            AssistChip(
                onClick = { selectedStatusFilter = null },
                label = { Text("All Status", color = BlackText) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selectedStatusFilter == null) CreamSurfaceVariant else CreamSurface
                ),
                border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = BlackBorder)
            )
            AssistChip(
                onClick = { selectedStatusFilter = ChapterStatus.COMPLETED },
                label = { Text("Completed", color = BlackText) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selectedStatusFilter == ChapterStatus.COMPLETED) CreamSurfaceVariant else CreamSurface
                ),
                border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = BlackBorder)
            )
            AssistChip(
                onClick = { selectedStatusFilter = ChapterStatus.IN_PROGRESS },
                label = { Text("In Progress", color = BlackText) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selectedStatusFilter == ChapterStatus.IN_PROGRESS) CreamSurfaceVariant else CreamSurface
                ),
                border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = BlackBorder)
            )
            FilterChip(
                selected = onlyHighYield,
                onClick = { onlyHighYield = !onlyHighYield },
                label = { Text("High Yield 🔥", fontSize = 12.sp, color = if (onlyHighYield) CreamBackground else BlackText) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BlackBorder,
                    containerColor = CreamSurface
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = onlyHighYield,
                    borderColor = BlackBorder
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chapter List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredChapters, key = { it.id }) { chap ->
                val subject = subjects.firstOrNull { it.id == chap.subjectId }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedChapterForDetail = chap
                            noteInput = chap.notes
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CreamSurface),
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
                            // Subject Color Pill / Number
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(CreamSurfaceVariant)
                                    .border(1.dp, BlackBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = String.format("%02d", chap.chapterNumber),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = BlackText
                                    )
                                )
                            }

                            Column {
                                Text(
                                    text = chap.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = BlackText
                                    )
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = subject?.name ?: "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BlackText
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = CreamSurfaceVariant,
                                        border = BorderStroke(0.5.dp, BlackBorder)
                                    ) {
                                        Text(
                                            text = "NEET ${chap.yieldLevel} Yield",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            color = BlackText,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                    if (chap.notes.isNotBlank()) {
                                        Text(
                                            text = "• Notes 📝",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = BlackText
                                        )
                                    }
                                }
                            }
                        }

                        // Status Tag & Cycle
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = when (chap.status) {
                                ChapterStatus.COMPLETED -> BlackBorder
                                else -> CreamSurfaceVariant
                            },
                            border = BorderStroke(1.dp, BlackBorder),
                            modifier = Modifier.clickable {
                                val nextStatus = when (chap.status) {
                                    ChapterStatus.NOT_STARTED -> ChapterStatus.IN_PROGRESS
                                    ChapterStatus.IN_PROGRESS -> ChapterStatus.COMPLETED
                                    ChapterStatus.COMPLETED -> ChapterStatus.NOT_STARTED
                                }
                                repository.updateChapterStatus(chap.id, nextStatus)
                            }
                        ) {
                            Text(
                                text = when (chap.status) {
                                    ChapterStatus.COMPLETED -> "Done ✓"
                                    ChapterStatus.IN_PROGRESS -> "Ongoing"
                                    ChapterStatus.NOT_STARTED -> "To Do"
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = when (chap.status) {
                                        ChapterStatus.COMPLETED -> CreamBackground
                                        else -> BlackText
                                    }
                                )
                            )
                        }

                        if (currentUser.role == UserRole.ADMIN) {
                            IconButton(onClick = {
                                chapterToRename = chap
                                renameInput = chap.title
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Rename", tint = BlackText, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Chapter Details & Notes Dialog
    if (selectedChapterForDetail != null) {
        val chap = selectedChapterForDetail!!
        AlertDialog(
            onDismissRequest = { selectedChapterForDetail = null },
            title = {
                Text(
                    text = chap.title,
                    fontWeight = FontWeight.Bold,
                    color = BlackText
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Current Status: ${chap.status.label}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = BlackText
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                repository.updateChapterStatus(chap.id, ChapterStatus.IN_PROGRESS)
                                selectedChapterForDetail = chap.copy(status = ChapterStatus.IN_PROGRESS)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BlackBorder),
                            colors = ButtonDefaults.buttonColors(containerColor = CreamSurfaceVariant, contentColor = BlackText)
                        ) {
                            Text("In Progress", style = MaterialTheme.typography.labelSmall, color = BlackText)
                        }

                        Button(
                            onClick = {
                                repository.updateChapterStatus(chap.id, ChapterStatus.COMPLETED)
                                selectedChapterForDetail = chap.copy(status = ChapterStatus.COMPLETED)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BlackBorder),
                            colors = ButtonDefaults.buttonColors(containerColor = BlackBorder, contentColor = CreamBackground)
                        ) {
                            Text("Complete ✓", style = MaterialTheme.typography.labelSmall, color = CreamBackground)
                        }
                    }

                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("Chapter Notes & Formulas", color = BlackText) },
                        placeholder = { Text("Add key points, doubts, or formulas here...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BlackBorder,
                            unfocusedBorderColor = BlackBorder,
                            focusedTextColor = BlackText,
                            unfocusedTextColor = BlackText
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    )

                    Button(
                        onClick = {
                            selectedChapterForDetail = null
                            onStartTimerForChapter(chap)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BlackBorder),
                        colors = ButtonDefaults.buttonColors(containerColor = BlackBorder, contentColor = CreamBackground)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = CreamBackground)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Timer for this Chapter", color = CreamBackground)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.updateChapterNotes(chap.id, noteInput)
                        selectedChapterForDetail = null
                    },
                    border = BorderStroke(1.dp, BlackBorder),
                    colors = ButtonDefaults.buttonColors(containerColor = BlackBorder, contentColor = CreamBackground)
                ) {
                    Text("Save Notes", color = CreamBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedChapterForDetail = null }) {
                    Text("Close", color = BlackText)
                }
            },
            containerColor = CreamSurface
        )
    }

    // Admin Rename Dialog
    if (chapterToRename != null) {
        AlertDialog(
            onDismissRequest = { chapterToRename = null },
            title = { Text("Rename Chapter", color = BlackText) },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("Chapter Title", color = BlackText) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BlackBorder,
                        unfocusedBorderColor = BlackBorder,
                        focusedTextColor = BlackText,
                        unfocusedTextColor = BlackText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            repository.renameChapter(chapterToRename!!.id, renameInput)
                            chapterToRename = null
                        }
                    },
                    border = BorderStroke(1.dp, BlackBorder),
                    colors = ButtonDefaults.buttonColors(containerColor = BlackBorder, contentColor = CreamBackground)
                ) {
                    Text("Rename", color = CreamBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { chapterToRename = null }) { Text("Cancel", color = BlackText) }
            },
            containerColor = CreamSurface
        )
    }

    // Admin Add Chapter Dialog
    if (showAddDialog) {
        var addSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: "") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Chapter", color = BlackText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Subject:", style = MaterialTheme.typography.labelMedium, color = BlackText)
                    subjects.forEach { s ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { addSubjectId = s.id }
                        ) {
                            RadioButton(
                                selected = addSubjectId == s.id,
                                onClick = { addSubjectId = s.id },
                                colors = RadioButtonDefaults.colors(selectedColor = BlackBorder, unselectedColor = BlackBorder)
                            )
                            Text(s.name, color = BlackText)
                        }
                    }
                    OutlinedTextField(
                        value = addChapterTitle,
                        onValueChange = { addChapterTitle = it },
                        label = { Text("Chapter Title", color = BlackText) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BlackBorder,
                            unfocusedBorderColor = BlackBorder,
                            focusedTextColor = BlackText,
                            unfocusedTextColor = BlackText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (addChapterTitle.isNotBlank()) {
                            repository.addNewChapter(addSubjectId, addChapterTitle)
                            showAddDialog = false
                            addChapterTitle = ""
                        }
                    },
                    border = BorderStroke(1.dp, BlackBorder),
                    colors = ButtonDefaults.buttonColors(containerColor = BlackBorder, contentColor = CreamBackground)
                ) {
                    Text("Add Chapter", color = CreamBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel", color = BlackText) }
            },
            containerColor = CreamSurface
        )
    }
}
