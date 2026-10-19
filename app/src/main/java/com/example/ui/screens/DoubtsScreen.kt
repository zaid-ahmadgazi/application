package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TwoHeartsRepository
import com.example.model.DoubtEntry
import com.example.model.UserRole
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoubtsScreen(
    repository: TwoHeartsRepository,
    onBack: () -> Unit
) {
    val doubts by repository.doubts.collectAsState()
    val currentUser by repository.currentUser.collectAsState()
    val subjects by repository.subjects.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var solvingDoubt by remember { mutableStateOf<DoubtEntry?>(null) }
    var selectedFilter by remember { mutableStateOf("all") } // "all", "pending", "resolved"

    val filteredDoubts = remember(doubts, selectedFilter) {
        when (selectedFilter) {
            "pending" -> doubts.filter { !it.isResolved }
            "resolved" -> doubts.filter { it.isResolved }
            else -> doubts
        }
    }

    val pendingCount = doubts.count { !it.isResolved }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Doubt Diary 💡",
                            style = MaterialTheme.typography.titleLarge,
                            color = BlackText,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (pendingCount > 0) "$pendingCount doubts waiting for resolution" else "All doubts resolved! Great job! ✨",
                            style = MaterialTheme.typography.bodySmall,
                            color = BlackText
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("doubts_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BlackText
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BlackBorder,
                            contentColor = CreamBackground
                        ),
                        border = BorderStroke(1.dp, BlackBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("ask_doubt_button")
                    ) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(18.dp), tint = CreamBackground)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Doubt", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CreamBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CreamBackground
                )
            )
        },
        containerColor = CreamBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "all",
                    onClick = { selectedFilter = "all" },
                    label = {
                        Text(
                            "All (${doubts.size})",
                            color = if (selectedFilter == "all") CreamBackground else BlackText
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BlackBorder,
                        containerColor = CreamSurface
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedFilter == "all",
                        borderColor = BlackBorder
                    )
                )
                FilterChip(
                    selected = selectedFilter == "pending",
                    onClick = { selectedFilter = "pending" },
                    label = {
                        Text(
                            "Needs Solving ($pendingCount)",
                            color = if (selectedFilter == "pending") CreamBackground else BlackText
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BlackBorder,
                        containerColor = CreamSurface
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedFilter == "pending",
                        borderColor = BlackBorder
                    )
                )
                FilterChip(
                    selected = selectedFilter == "resolved",
                    onClick = { selectedFilter = "resolved" },
                    label = {
                        Text(
                            "Resolved (${doubts.size - pendingCount})",
                            color = if (selectedFilter == "resolved") CreamBackground else BlackText
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BlackBorder,
                        containerColor = CreamSurface
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedFilter == "resolved",
                        borderColor = BlackBorder
                    )
                )
            }

            if (filteredDoubts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No doubts logged in this category.\nTap 'Log Doubt' to write down any tricky question!",
                        color = BlackText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredDoubts, key = { it.id }) { doubt ->
                        DoubtCardItem(
                            doubt = doubt,
                            currentUserRole = currentUser.role,
                            onSolveClick = { solvingDoubt = doubt },
                            onToggleHeart = { repository.toggleAdminHeartOnDoubt(doubt.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddDoubtDialog(
            subjects = subjects,
            onDismiss = { showAddDialog = false },
            onAdd = { subId, chapTitle, question ->
                repository.addDoubt(subId, chapTitle, question)
                showAddDialog = false
            }
        )
    }

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

@Composable
fun DoubtCardItem(
    doubt: DoubtEntry,
    currentUserRole: UserRole,
    onSolveClick: () -> Unit,
    onToggleHeart: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
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
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CreamSurfaceVariant,
                    border = BorderStroke(1.dp, BlackBorder)
                ) {
                    Text(
                        text = doubt.chapterTitle,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BlackText
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (doubt.isResolved) BlackBorder else CreamSurfaceVariant,
                    border = BorderStroke(1.dp, BlackBorder)
                ) {
                    Text(
                        text = if (doubt.isResolved) "Resolved ✓" else "Needs Help ⚠️",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (doubt.isResolved) CreamBackground else BlackText
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = doubt.question,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = BlackText
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Asked by ${doubt.askedBy} • ${doubt.createdAt}",
                style = MaterialTheme.typography.labelSmall,
                color = BlackText
            )

            if (doubt.isResolved && doubt.adminSolution.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CreamSurfaceVariant,
                    border = BorderStroke(1.dp, BlackBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BlackText, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Solution by ${doubt.solvedBy}:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BlackText
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = doubt.adminSolution,
                            style = MaterialTheme.typography.bodyMedium,
                            color = BlackText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Heart reaction button
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CreamSurfaceVariant,
                    border = BorderStroke(1.dp, BlackBorder),
                    onClick = onToggleHeart
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Love",
                            tint = if (doubt.adminHeartReaction) PurpleLogo else BlackText,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (doubt.adminHeartReaction) "Loved" else "Cheer",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BlackText
                        )
                    }
                }

                // Solve or Edit Solution Button
                Button(
                    onClick = onSolveClick,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BlackBorder),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (doubt.isResolved) CreamSurface else BlackBorder,
                        contentColor = if (doubt.isResolved) BlackText else CreamBackground
                    )
                ) {
                    Text(
                        text = if (doubt.isResolved) "Update Solution" else "Solve & Clear (+15)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (doubt.isResolved) BlackText else CreamBackground
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDoubtDialog(
    subjects: List<com.example.model.Subject>,
    onDismiss: () -> Unit,
    onAdd: (subjectId: String, chapterTitle: String, question: String) -> Unit
) {
    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: "sub_physics") }
    var chapterTitle by remember { mutableStateOf("") }
    var question by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Academic Doubt 💡",
                style = MaterialTheme.typography.titleLarge,
                color = BlackText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Subject:", style = MaterialTheme.typography.bodySmall, color = BlackText)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(subjects) { sub ->
                        val isSelected = selectedSubjectId == sub.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSubjectId = sub.id },
                            label = { Text(sub.name, fontSize = 12.sp, color = if (isSelected) CreamBackground else BlackText) },
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

                OutlinedTextField(
                    value = chapterTitle,
                    onValueChange = { chapterTitle = it },
                    label = { Text("Chapter / Concept Topic", color = BlackText) },
                    placeholder = { Text("e.g. Thermodynamics, Carnot Cycle") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BlackBorder,
                        unfocusedBorderColor = BlackBorder,
                        focusedTextColor = BlackText,
                        unfocusedTextColor = BlackText
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Doubt or Question Details", color = BlackText) },
                    placeholder = { Text("Describe the tricky problem or formula you're stuck on...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BlackBorder,
                        unfocusedBorderColor = BlackBorder,
                        focusedTextColor = BlackText,
                        unfocusedTextColor = BlackText
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (chapterTitle.isNotBlank() && question.isNotBlank()) {
                        onAdd(selectedSubjectId, chapterTitle, question)
                    }
                },
                enabled = chapterTitle.isNotBlank() && question.isNotBlank(),
                border = BorderStroke(1.dp, BlackBorder),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BlackBorder,
                    contentColor = CreamBackground
                )
            ) {
                Text("Post Doubt", color = CreamBackground)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = BlackText)
            }
        },
        containerColor = CreamSurface
    )
}

@Composable
fun SolveDoubtDialog(
    doubt: DoubtEntry,
    onDismiss: () -> Unit,
    onResolve: (String) -> Unit
) {
    var solutionText by remember { mutableStateOf(doubt.adminSolution) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Provide Doubt Solution ✨",
                style = MaterialTheme.typography.titleLarge,
                color = BlackText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CreamSurfaceVariant,
                    border = BorderStroke(1.dp, BlackBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "${doubt.chapterTitle}:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BlackText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "\"${doubt.question}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = BlackText
                        )
                    }
                }

                OutlinedTextField(
                    value = solutionText,
                    onValueChange = { solutionText = it },
                    label = { Text("Step-by-Step Explanation / Formula", color = BlackText) },
                    placeholder = { Text("Write the derivation, shortcut, or concept explanation...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BlackBorder,
                        unfocusedBorderColor = BlackBorder,
                        focusedTextColor = BlackText,
                        unfocusedTextColor = BlackText
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (solutionText.isNotBlank()) {
                        onResolve(solutionText)
                    }
                },
                enabled = solutionText.isNotBlank(),
                border = BorderStroke(1.dp, BlackBorder),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BlackBorder,
                    contentColor = CreamBackground
                )
            ) {
                Text("Mark Resolved & Award Points (+15)", color = CreamBackground)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = BlackText)
            }
        },
        containerColor = CreamSurface
    )
}
