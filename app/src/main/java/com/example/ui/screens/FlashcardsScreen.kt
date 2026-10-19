package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TwoHeartsRepository
import com.example.model.Flashcard
import com.example.model.FlashcardMastery
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    repository: TwoHeartsRepository,
    onBack: () -> Unit
) {
    val flashcards by repository.flashcards.collectAsState()
    val subjects by repository.subjects.collectAsState()

    var selectedSubjectId by remember { mutableStateOf<String?>("all") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredCards = remember(flashcards, selectedSubjectId) {
        if (selectedSubjectId == null || selectedSubjectId == "all") {
            flashcards
        } else {
            flashcards.filter { it.subjectId == selectedSubjectId }
        }
    }

    val masteredCount = flashcards.count { it.mastery == FlashcardMastery.MASTERED }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Formula Flashcards ⚡",
                            style = MaterialTheme.typography.titleLarge,
                            color = BlackText,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$masteredCount / ${flashcards.size} Formulas Mastered",
                            style = MaterialTheme.typography.bodySmall,
                            color = BlackText
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("flashcards_back_button")) {
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
                            .testTag("add_flashcard_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = CreamBackground)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Card", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CreamBackground)
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
            // Subject Filter Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedSubjectId == "all",
                        onClick = { selectedSubjectId = "all" },
                        label = {
                            Text(
                                "All Subjects (${flashcards.size})",
                                color = if (selectedSubjectId == "all") CreamBackground else BlackText
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BlackBorder,
                            containerColor = CreamSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedSubjectId == "all",
                            borderColor = BlackBorder
                        )
                    )
                }
                items(subjects) { sub ->
                    val count = flashcards.count { it.subjectId == sub.id }
                    val isSelected = selectedSubjectId == sub.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSubjectId = sub.id },
                        label = {
                            Text(
                                "${sub.name} ($count)",
                                color = if (isSelected) CreamBackground else BlackText
                            )
                        },
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

            if (filteredCards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No flashcards in this subject yet.\nTap 'Add Card' above to create one!",
                        textAlign = TextAlign.Center,
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
                    items(filteredCards, key = { it.id }) { card ->
                        FlashcardItem(
                            card = card,
                            onMasteryChanged = { mastery ->
                                repository.updateFlashcardMastery(card.id, mastery)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddFlashcardDialog(
            subjects = subjects,
            onDismiss = { showAddDialog = false },
            onAdd = { subId, chapTitle, q, a ->
                repository.addNewFlashcard(subId, chapTitle, q, a)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun FlashcardItem(
    card: Flashcard,
    onMasteryChanged: (FlashcardMastery) -> Unit
) {
    var isFlipped by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "flashcard_flip"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isFlipped = !isFlipped },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFlipped) CreamSurfaceVariant else CreamSurface
        ),
        border = BorderStroke(1.5.dp, BlackBorder)
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
                        text = card.chapterTitle,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BlackText
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (card.mastery) {
                        FlashcardMastery.MASTERED -> BlackBorder
                        FlashcardMastery.GOOD -> CreamSurfaceVariant
                        FlashcardMastery.NEED_REVIEW -> CreamSurfaceVariant
                    },
                    border = BorderStroke(1.dp, BlackBorder)
                ) {
                    Text(
                        text = card.mastery.label,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (card.mastery) {
                            FlashcardMastery.MASTERED -> CreamBackground
                            else -> BlackText
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Front or Back content with flip indication
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 90.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!isFlipped) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = card.frontQuestion,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = BlackText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "(Tap card to reveal formula / answer 🔄)",
                            style = MaterialTheme.typography.labelSmall,
                            color = BlackText
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = card.backAnswer,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = BlackText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Reviewed: ${card.lastReviewedAt.ifBlank { "Just now" }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = BlackText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Spaced Repetition Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recall Rating:",
                    style = MaterialTheme.typography.bodySmall,
                    color = BlackText,
                    fontWeight = FontWeight.Medium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { onMasteryChanged(FlashcardMastery.NEED_REVIEW) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (card.mastery == FlashcardMastery.NEED_REVIEW) BlackBorder else CreamSurface,
                            contentColor = if (card.mastery == FlashcardMastery.NEED_REVIEW) CreamBackground else BlackText
                        ),
                        border = BorderStroke(1.dp, BlackBorder),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Review", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onMasteryChanged(FlashcardMastery.GOOD) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (card.mastery == FlashcardMastery.GOOD) BlackBorder else CreamSurface,
                            contentColor = if (card.mastery == FlashcardMastery.GOOD) CreamBackground else BlackText
                        ),
                        border = BorderStroke(1.dp, BlackBorder),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Good", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onMasteryChanged(FlashcardMastery.MASTERED) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (card.mastery == FlashcardMastery.MASTERED) BlackBorder else CreamSurface,
                            contentColor = if (card.mastery == FlashcardMastery.MASTERED) CreamBackground else BlackText
                        ),
                        border = BorderStroke(1.dp, BlackBorder),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Mastered (+5)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFlashcardDialog(
    subjects: List<com.example.model.Subject>,
    onDismiss: () -> Unit,
    onAdd: (subjectId: String, chapterTitle: String, question: String, answer: String) -> Unit
) {
    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: "sub_physics") }
    var chapterTitle by remember { mutableStateOf("") }
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Create New Flashcard 📝",
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
                Text("Select Subject:", style = MaterialTheme.typography.bodySmall, color = BlackText)
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
                    label = { Text("Topic or Chapter Name", color = BlackText) },
                    placeholder = { Text("e.g. Modern Physics, De Broglie") },
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
                    label = { Text("Question or Formula Prompt", color = BlackText) },
                    placeholder = { Text("e.g. What is the wavelength λ formula?") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BlackBorder,
                        unfocusedBorderColor = BlackBorder,
                        focusedTextColor = BlackText,
                        unfocusedTextColor = BlackText
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    label = { Text("Answer / Derivation / Key Note", color = BlackText) },
                    placeholder = { Text("e.g. λ = h / p = h / (m·v)") },
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
                    if (question.isNotBlank() && answer.isNotBlank()) {
                        onAdd(selectedSubjectId, chapterTitle, question, answer)
                    }
                },
                enabled = question.isNotBlank() && answer.isNotBlank(),
                border = BorderStroke(1.dp, BlackBorder),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BlackBorder,
                    contentColor = CreamBackground
                )
            ) {
                Text("Save Card", color = CreamBackground)
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
