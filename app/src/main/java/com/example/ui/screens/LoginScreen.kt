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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TwoHeartsRepository
import com.example.model.UserRole
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    repository: TwoHeartsRepository? = null,
    onLoginSuccess: (UserRole) -> Unit,
    onOpenSettings: () -> Unit
) {
    var username by remember { mutableStateOf("surbie") }
    var password by remember { mutableStateOf("••••••••") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var isLoggingIn by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

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
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Heart & App Logo (Purple for Logo)
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(PurpleLogo)
                    .border(2.dp, BlackBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Two Hearts Logo",
                    tint = CreamBackground,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Two Hearts Study",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = BlackText
                )
            )

            Text(
                text = "Our private world of study, dedication & love",
                style = MaterialTheme.typography.bodyMedium,
                color = BlackText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CreamSurface
                ),
                border = BorderStroke(1.5.dp, BlackBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Choose Your Account",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = BlackText
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Role selector tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CreamSurfaceVariant)
                            .border(1.dp, BlackBorder, RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp)),
                            color = if (selectedRole == UserRole.STUDENT) BlackBorder else Color.Transparent,
                            onClick = {
                                selectedRole = UserRole.STUDENT
                                username = "surbie"
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🌸 Surbie (Student)",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedRole == UserRole.STUDENT) CreamBackground else BlackText
                                    )
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp)),
                            color = if (selectedRole == UserRole.ADMIN) BlackBorder else Color.Transparent,
                            onClick = {
                                selectedRole = UserRole.ADMIN
                                username = "admin"
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "👑 Admin",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedRole == UserRole.ADMIN) CreamBackground else BlackText
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            loginError = null
                        },
                        label = { Text("Username / Email", color = BlackText) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = BlackText)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BlackText,
                            unfocusedTextColor = BlackText,
                            focusedBorderColor = BlackBorder,
                            unfocusedBorderColor = BlackBorder,
                            cursorColor = BlackText
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            loginError = null
                        },
                        label = { Text("Password", color = BlackText) },
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = BlackText)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BlackText,
                            unfocusedTextColor = BlackText,
                            focusedBorderColor = BlackBorder,
                            unfocusedBorderColor = BlackBorder,
                            cursorColor = BlackText
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (loginError != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = loginError!!,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = BlackText,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (repository != null && repository.supabase.isConfigured) {
                                isLoggingIn = true
                                loginError = null
                                scope.launch {
                                    val (ok, msg) = repository.loginWithSupabase(username, password, selectedRole)
                                    isLoggingIn = false
                                    if (ok) {
                                        onLoginSuccess(repository.currentUser.value.role)
                                    } else {
                                        loginError = msg
                                    }
                                }
                            } else {
                                repository?.switchRole(selectedRole)
                                onLoginSuccess(selectedRole)
                            }
                        },
                        enabled = !isLoggingIn,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, BlackBorder),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BlackBorder,
                            contentColor = CreamBackground
                        )
                    ) {
                        if (isLoggingIn) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = CreamBackground,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (selectedRole == UserRole.STUDENT) "Enter My Study Dashboard 🌸" else "Enter Admin Control 👑",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CreamBackground
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Secured with Cloud Synchronization",
                        style = MaterialTheme.typography.bodySmall,
                        color = BlackText,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Supabase Backend Settings button
            OutlinedButton(
                onClick = onOpenSettings,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BlackBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = BlackText
                )
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp), tint = BlackText)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Supabase Cloud Setup & SQL Script", color = BlackText)
            }
        }
    }
}
