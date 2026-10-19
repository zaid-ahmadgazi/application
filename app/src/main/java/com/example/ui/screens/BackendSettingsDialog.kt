package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TwoHeartsRepository
import com.example.model.SyncState
import com.example.model.UserRole
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun BackendSettingsDialog(
    repository: TwoHeartsRepository,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var projectUrl by remember {
        mutableStateOf(repository.supabase.projectUrl.ifBlank { repository.getSavedUrl().ifBlank { "https://kxxdqektnssyltnmzgre.supabase.co" } })
    }
    var anonKey by remember {
        mutableStateOf(repository.supabase.anonKey.ifBlank { repository.getSavedKey() })
    }
    var testResult by remember { mutableStateOf<String?>(null) }
    var isTesting by remember { mutableStateOf(false) }

    val currentUser by repository.currentUser.collectAsState()
    val syncState by repository.syncState.collectAsState()
    val lastSyncMessage by repository.lastSyncMessage.collectAsState()

    var showSqlViewer by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = CreamSurface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.CloudSync, contentDescription = null, tint = PurpleLogo)
                Text(
                    text = "Supabase Cloud Setup & Sync",
                    fontWeight = FontWeight.Bold,
                    color = BlackText
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Connection status card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, BlackBorder),
                    colors = CardDefaults.cardColors(containerColor = CreamSurfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Current Sync Status:",
                            style = MaterialTheme.typography.labelSmall,
                            color = BlackMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when (syncState) {
                                SyncState.CONNECTED -> "Cloud Connected & Synced"
                                SyncState.LOCAL_CACHE -> "Local Cache Active (Offline Safe)"
                                SyncState.SYNCING -> "Synchronizing with Supabase..."
                                SyncState.ERROR -> "Offline / Check Credentials"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = BlackText
                        )
                    }
                }

                // Switch Role
                Text(
                    text = "Current Active Role:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = BlackText
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { repository.switchRole(UserRole.STUDENT) },
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.5.dp, BlackBorder),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentUser.role == UserRole.STUDENT) BlackBorder else CreamSurfaceVariant,
                            contentColor = if (currentUser.role == UserRole.STUDENT) CreamBackground else BlackText
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Surbie", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { repository.switchRole(UserRole.ADMIN) },
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.5.dp, BlackBorder),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentUser.role == UserRole.ADMIN) BlackBorder else CreamSurfaceVariant,
                            contentColor = if (currentUser.role == UserRole.ADMIN) CreamBackground else BlackText
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Admin", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = BlackBorder, thickness = 1.dp)

                // Project URL
                OutlinedTextField(
                    value = projectUrl,
                    onValueChange = { projectUrl = it },
                    label = { Text("Supabase Project URL", color = BlackText) },
                    placeholder = { Text("https://your-id.supabase.co", color = BlackMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BlackBorder,
                        unfocusedBorderColor = BlackBorder,
                        focusedTextColor = BlackText,
                        unfocusedTextColor = BlackText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Anon API Key
                OutlinedTextField(
                    value = anonKey,
                    onValueChange = { anonKey = it },
                    label = { Text("Supabase Anon Key", color = BlackText) },
                    placeholder = { Text("eyJhbGciOi...", color = BlackMuted) },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BlackBorder,
                        unfocusedBorderColor = BlackBorder,
                        focusedTextColor = BlackText,
                        unfocusedTextColor = BlackText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Save & Test Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            isTesting = true
                            testResult = null
                            scope.launch {
                                repository.saveCustomCredentials(projectUrl.trim(), anonKey.trim())
                                val result = repository.supabase.testConnection()
                                isTesting = false
                                testResult = if (result.isSuccess) {
                                    "Connected successfully to Supabase!"
                                } else {
                                    "Connection error: ${result.exceptionOrNull()?.message}"
                                }
                            }
                        },
                        enabled = !isTesting,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.5.dp, BlackBorder),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BlackBorder,
                            contentColor = CreamBackground,
                            disabledContainerColor = CreamSurfaceVariant,
                            disabledContentColor = BlackMuted
                        )
                    ) {
                        if (isTesting) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = CreamBackground,
                                    strokeWidth = 2.dp
                                )
                                Text("Testing Connection...")
                            }
                        } else {
                            Text("Save & Test", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                val displayResult = testResult ?: lastSyncMessage
                if (displayResult != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.5.dp, BlackBorder),
                        colors = CardDefaults.cardColors(containerColor = CreamSurfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = BlackText,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = displayResult,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = BlackText
                            )
                        }
                    }
                }

                HorizontalDivider(color = BlackBorder, thickness = 1.dp)

                // How to find credentials guide
                Text(
                    text = "How to get credentials in Supabase:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = BlackText
                )
                Text(
                    text = "1. Open Supabase Dashboard for 'app Project'\n" +
                            "2. Go to Project Settings (gear icon at bottom left)\n" +
                            "3. Select 'API' under Settings\n" +
                            "4. Copy 'Project URL' and 'anon public' API Key into the boxes above.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BlackMuted
                )

                // SQL Script Section
                OutlinedButton(
                    onClick = { showSqlViewer = !showSqlViewer },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.5.dp, BlackBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BlackText)
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp), tint = BlackText)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showSqlViewer) "Hide SQL Setup Script" else "View / Copy Supabase SQL Script",
                        fontWeight = FontWeight.Bold
                    )
                }

                if (showSqlViewer) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.5.dp, BlackBorder),
                        colors = CardDefaults.cardColors(containerColor = CreamSurfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "supabase_setup.sql",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BlackText,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Supabase SQL", SUPABASE_SQL_SNIPPET)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "SQL Script copied to clipboard! Paste into Supabase SQL Editor.", Toast.LENGTH_LONG).show()
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy SQL",
                                        tint = BlackText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Run this in Supabase SQL Editor to create all 88 chapters, 16 routine blocks, rewards, and RLS tables.",
                                style = MaterialTheme.typography.bodySmall,
                                color = BlackMuted
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.5.dp, BlackBorder),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BlackBorder,
                    contentColor = CreamBackground
                )
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}

private const val SUPABASE_SQL_SNIPPET = """-- TWO HEARTS STUDY - COMPLETE SUPABASE SETUP
-- Open Supabase -> SQL Editor -> New Query -> Paste & Run!
-- Full script is also saved in root: supabase_setup.sql
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    username TEXT UNIQUE NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('admin', 'student')),
    display_name TEXT NOT NULL,
    avatar_url TEXT,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);
-- See supabase_setup.sql for the complete 88 chapters and 16 routine blocks script!
"""
