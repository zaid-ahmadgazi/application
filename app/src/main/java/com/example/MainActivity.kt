package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TwoHeartsRepository
import com.example.model.Chapter
import com.example.model.Subject
import com.example.model.UserRole
import com.example.ui.components.PointsPill
import com.example.ui.components.SyncStatusBadge
import com.example.ui.screens.*
import com.example.ui.theme.*

enum class AppScreen {
    LOGIN,
    STUDENT_DASHBOARD,
    ADMIN_DASHBOARD,
    TIMER,
    AMBIENT_FOCUS,
    CHAPTERS,
    ROUTINE,
    WALLET,
    REWARDS,
    FLASHCARDS,
    DOUBTS
}

class MainActivity : ComponentActivity() {

    private lateinit var repository: TwoHeartsRepository

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = TwoHeartsRepository(applicationContext)

        setContent {
            MyApplicationTheme {
                val currentUser by repository.currentUser.collectAsState()
                val totalPoints by repository.totalPoints.collectAsState()
                val syncState by repository.syncState.collectAsState()

                var currentScreen by remember { mutableStateOf(AppScreen.STUDENT_DASHBOARD) }
                var showSettingsDialog by remember { mutableStateOf(false) }

                // Ambient focus session state
                var ambientInitialSubject by remember { mutableStateOf<Subject?>(null) }
                var ambientInitialChapter by remember { mutableStateOf<Chapter?>(null) }
                var chapterSubjectFilter by remember { mutableStateOf<String?>(null) }

                // Navigation back stack support
                BackHandler(enabled = currentScreen != AppScreen.STUDENT_DASHBOARD && currentScreen != AppScreen.ADMIN_DASHBOARD && currentScreen != AppScreen.LOGIN) {
                    currentScreen = if (currentUser.role == UserRole.ADMIN) AppScreen.ADMIN_DASHBOARD else AppScreen.STUDENT_DASHBOARD
                }

                if (currentScreen == AppScreen.LOGIN) {
                    LoginScreen(
                        repository = repository,
                        onLoginSuccess = { role ->
                            repository.switchRole(role)
                            currentScreen = if (role == UserRole.ADMIN) AppScreen.ADMIN_DASHBOARD else AppScreen.STUDENT_DASHBOARD
                        },
                        onOpenSettings = { showSettingsDialog = true }
                    )
                } else if (currentScreen == AppScreen.AMBIENT_FOCUS) {
                    // Fullscreen Ambient Focus Mode (without scaffold / nav bars)
                    AmbientFocusScreen(
                        repository = repository,
                        initialSubject = ambientInitialSubject,
                        initialChapter = ambientInitialChapter,
                        onCloseAmbient = {
                            currentScreen = if (currentUser.role == UserRole.ADMIN) AppScreen.ADMIN_DASHBOARD else AppScreen.STUDENT_DASHBOARD
                        }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = WindowInsets.safeDrawing,
                        topBar = {
                            TopAppBar(
                                title = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(PurpleLogo)
                                                .border(1.dp, BlackBorder, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Favorite,
                                                contentDescription = "Logo",
                                                tint = CreamBackground,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "Two Hearts",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = BlackText
                                            )
                                            // 1-tap Role Switch pill
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = CreamSurfaceVariant,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, BlackBorder),
                                                modifier = Modifier.clickable {
                                                    val nextRole = if (currentUser.role == UserRole.ADMIN) UserRole.STUDENT else UserRole.ADMIN
                                                    repository.switchRole(nextRole)
                                                    currentScreen = if (nextRole == UserRole.ADMIN) AppScreen.ADMIN_DASHBOARD else AppScreen.STUDENT_DASHBOARD
                                                }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = if (currentUser.role == UserRole.ADMIN) "👑 Admin (tap to switch)" else "🌸 Surbie (tap to switch)",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = BlackText,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                },
                                actions = {
                                    // Points Pill
                                    PointsPill(
                                        points = totalPoints,
                                        onClick = { currentScreen = AppScreen.WALLET },
                                        modifier = Modifier.testTag("points_pill")
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    // Quick Flashcards shortcut icon
                                    IconButton(
                                        onClick = { currentScreen = AppScreen.FLASHCARDS },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Style, contentDescription = "Flashcards", tint = BlackText, modifier = Modifier.size(20.dp))
                                    }

                                    // Quick Doubts shortcut icon
                                    IconButton(
                                        onClick = { currentScreen = AppScreen.DOUBTS },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Lightbulb, contentDescription = "Doubts", tint = BlackText, modifier = Modifier.size(20.dp))
                                    }

                                    // Cloud Sync Status Badge
                                    SyncStatusBadge(
                                        state = syncState,
                                        onClick = { showSettingsDialog = true },
                                        modifier = Modifier.testTag("sync_badge")
                                    )

                                    IconButton(
                                        onClick = { showSettingsDialog = true },
                                        modifier = Modifier.testTag("settings_button")
                                    ) {
                                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = BlackText)
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = CreamBackground,
                                    titleContentColor = BlackText,
                                    actionIconContentColor = BlackText
                                )
                            )
                        },
                        bottomBar = {
                            val navItemColors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BlackText,
                                selectedTextColor = BlackText,
                                unselectedIconColor = BlackText.copy(alpha = 0.55f),
                                unselectedTextColor = BlackText.copy(alpha = 0.55f),
                                indicatorColor = CreamSurfaceVariant
                            )
                            NavigationBar(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .windowInsetsPadding(WindowInsets.navigationBars),
                                containerColor = CreamBackground
                            ) {
                                if (currentUser.role == UserRole.STUDENT) {
                                    NavigationBarItem(
                                        selected = currentScreen == AppScreen.STUDENT_DASHBOARD,
                                        onClick = { currentScreen = AppScreen.STUDENT_DASHBOARD },
                                        icon = { Icon(Icons.Default.Today, contentDescription = "Today") },
                                        label = { Text("Today") },
                                        colors = navItemColors,
                                        modifier = Modifier.testTag("nav_student_today")
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == AppScreen.TIMER,
                                        onClick = { currentScreen = AppScreen.TIMER },
                                        icon = { Icon(Icons.Default.Timer, contentDescription = "Timer") },
                                        label = { Text("Focus") },
                                        colors = navItemColors,
                                        modifier = Modifier.testTag("nav_student_timer")
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == AppScreen.CHAPTERS,
                                        onClick = {
                                            chapterSubjectFilter = null
                                            currentScreen = AppScreen.CHAPTERS
                                        },
                                        icon = { Icon(Icons.Default.MenuBook, contentDescription = "Chapters") },
                                        label = { Text("Chapters") },
                                        colors = navItemColors,
                                        modifier = Modifier.testTag("nav_student_chapters")
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == AppScreen.FLASHCARDS,
                                        onClick = { currentScreen = AppScreen.FLASHCARDS },
                                        icon = { Icon(Icons.Default.Style, contentDescription = "Cards") },
                                        label = { Text("Cards") },
                                        colors = navItemColors,
                                        modifier = Modifier.testTag("nav_student_flashcards")
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == AppScreen.WALLET,
                                        onClick = { currentScreen = AppScreen.WALLET },
                                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Wallet") },
                                        label = { Text("Wallet") },
                                        colors = navItemColors,
                                        modifier = Modifier.testTag("nav_student_wallet")
                                    )
                                } else {
                                    // Admin navigation items
                                    NavigationBarItem(
                                        selected = currentScreen == AppScreen.ADMIN_DASHBOARD,
                                        onClick = { currentScreen = AppScreen.ADMIN_DASHBOARD },
                                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Overview") },
                                        label = { Text("Overview") },
                                        colors = navItemColors,
                                        modifier = Modifier.testTag("nav_admin_overview")
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == AppScreen.CHAPTERS,
                                        onClick = {
                                            chapterSubjectFilter = null
                                            currentScreen = AppScreen.CHAPTERS
                                        },
                                        icon = { Icon(Icons.Default.MenuBook, contentDescription = "Curriculum") },
                                        label = { Text("Syllabus") },
                                        colors = navItemColors,
                                        modifier = Modifier.testTag("nav_admin_chapters")
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == AppScreen.DOUBTS,
                                        onClick = { currentScreen = AppScreen.DOUBTS },
                                        icon = { Icon(Icons.Default.Lightbulb, contentDescription = "Doubts") },
                                        label = { Text("Doubts") },
                                        colors = navItemColors,
                                        modifier = Modifier.testTag("nav_admin_doubts")
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == AppScreen.REWARDS,
                                        onClick = { currentScreen = AppScreen.REWARDS },
                                        icon = { Icon(Icons.Default.Stars, contentDescription = "Rewards") },
                                        label = { Text("Rewards") },
                                        colors = navItemColors,
                                        modifier = Modifier.testTag("nav_admin_rewards")
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == AppScreen.WALLET,
                                        onClick = { currentScreen = AppScreen.WALLET },
                                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Audit") },
                                        label = { Text("Audit") },
                                        colors = navItemColors,
                                        modifier = Modifier.testTag("nav_admin_audit")
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentScreen) {
                                AppScreen.STUDENT_DASHBOARD -> {
                                    StudentDashboardScreen(
                                        repository = repository,
                                        onNavigateToTimer = { currentScreen = AppScreen.TIMER },
                                        onLaunchAmbientTimer = {
                                            ambientInitialSubject = null
                                            ambientInitialChapter = null
                                            currentScreen = AppScreen.AMBIENT_FOCUS
                                        },
                                        onNavigateToRoutine = { currentScreen = AppScreen.ROUTINE },
                                        onNavigateToChapters = { sub ->
                                            chapterSubjectFilter = sub?.id
                                            currentScreen = AppScreen.CHAPTERS
                                        },
                                        onNavigateToWallet = { currentScreen = AppScreen.WALLET },
                                        onNavigateToFlashcards = { currentScreen = AppScreen.FLASHCARDS },
                                        onNavigateToDoubts = { currentScreen = AppScreen.DOUBTS }
                                    )
                                }
                                AppScreen.ADMIN_DASHBOARD -> {
                                    AdminDashboardScreen(
                                        repository = repository,
                                        onNavigateToChapters = {
                                            chapterSubjectFilter = null
                                            currentScreen = AppScreen.CHAPTERS
                                        },
                                        onNavigateToRoutine = { currentScreen = AppScreen.ROUTINE },
                                        onNavigateToRewards = { currentScreen = AppScreen.REWARDS },
                                        onNavigateToWallet = { currentScreen = AppScreen.WALLET },
                                        onNavigateToDoubts = { currentScreen = AppScreen.DOUBTS },
                                        onNavigateToFlashcards = { currentScreen = AppScreen.FLASHCARDS }
                                    )
                                }
                                AppScreen.TIMER -> {
                                    TimerScreen(
                                        repository = repository,
                                        onLaunchAmbientMode = {
                                            ambientInitialSubject = null
                                            ambientInitialChapter = null
                                            currentScreen = AppScreen.AMBIENT_FOCUS
                                        },
                                        onFinishSession = {
                                            currentScreen = if (currentUser.role == UserRole.ADMIN) AppScreen.ADMIN_DASHBOARD else AppScreen.STUDENT_DASHBOARD
                                        }
                                    )
                                }
                                AppScreen.CHAPTERS -> {
                                    ChaptersScreen(
                                        repository = repository,
                                        initialSubjectId = chapterSubjectFilter,
                                        onStartTimerForChapter = { chap ->
                                            ambientInitialChapter = chap
                                            val sub = repository.subjects.value.firstOrNull { it.id == chap.subjectId }
                                            ambientInitialSubject = sub
                                            currentScreen = AppScreen.AMBIENT_FOCUS
                                        }
                                    )
                                }
                                AppScreen.FLASHCARDS -> {
                                    FlashcardsScreen(
                                        repository = repository,
                                        onBack = {
                                            currentScreen = if (currentUser.role == UserRole.ADMIN) AppScreen.ADMIN_DASHBOARD else AppScreen.STUDENT_DASHBOARD
                                        }
                                    )
                                }
                                AppScreen.DOUBTS -> {
                                    DoubtsScreen(
                                        repository = repository,
                                        onBack = {
                                            currentScreen = if (currentUser.role == UserRole.ADMIN) AppScreen.ADMIN_DASHBOARD else AppScreen.STUDENT_DASHBOARD
                                        }
                                    )
                                }
                                AppScreen.ROUTINE -> {
                                    RoutineScreen(
                                        repository = repository,
                                        onStartTimer = { currentScreen = AppScreen.TIMER }
                                    )
                                }
                                AppScreen.WALLET -> {
                                    WalletScreen(
                                        repository = repository,
                                        onNavigateToRewards = { currentScreen = AppScreen.REWARDS }
                                    )
                                }
                                AppScreen.REWARDS -> {
                                    RewardsScreen(repository = repository)
                                }
                                AppScreen.LOGIN -> {}
                                AppScreen.AMBIENT_FOCUS -> {}
                            }
                        }
                    }
                }

                if (showSettingsDialog) {
                    BackendSettingsDialog(
                        repository = repository,
                        onDismiss = { showSettingsDialog = false }
                    )
                }
            }
        }
    }
}
