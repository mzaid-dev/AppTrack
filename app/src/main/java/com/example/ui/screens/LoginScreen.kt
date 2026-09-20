package com.example.ui.screens

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContractEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.compose.foundation.layout.ExperimentalLayoutApi

// Color Palette constants
private val BackgroundColor = Color(0xFFF5F7FA)
private val CardSurfaceColor = Color(0xFFFFFFFF)
private val PrimaryAccentColor = Color(0xFF2F80ED)
private val PrimaryTextColor = Color(0xFF1A1D1E)
private val InputFieldBgColor = Color(0xFFF2F4F7)
private val SlateGreyColor = Color(0xFF8D96A0)
private val ErrorRedColor = Color(0xFFDC2626)
private val ErrorBorderColor = Color(0xFFEF4444)

// Hardcoded test credentials requested by user
private const val TEST_EMAIL = "test@gmail.com"
private const val TEST_PASSWORD = "test12"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    contracts: List<ContractEntity> = emptyList(),
    onLogin: (email: String, role: String, contractId: String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Validation & Error States
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var authError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    val isImeVisible = WindowInsets.isImeVisible

    // Form positioning:
    // When keyboard is closed: form is centered comfortably below the top branding (bias = 0.05f)
    // When keyboard is open: form moves up and sits right above the keyboard (bias = 0.90f, bottom = 18.dp)
    val formVerticalBias by animateFloatAsState(
        targetValue = if (isImeVisible) 0.90f else 0.05f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "formVerticalBias"
    )

    val formBottomPadding by animateDpAsState(
        targetValue = if (isImeVisible) 18.dp else 0.dp,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "formBottomPadding"
    )

    fun validateInputs(): Boolean {
        var isValid = true
        val trimmedEmail = email.trim()

        if (trimmedEmail.isEmpty()) {
            emailError = "Please enter your email"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            emailError = "Please enter a valid email address"
            isValid = false
        } else {
            emailError = null
        }

        if (password.isEmpty()) {
            passwordError = "Please enter your password"
            isValid = false
        } else if (password.length < 6) {
            passwordError = "Password must be at least 6 characters"
            isValid = false
        } else {
            passwordError = null
        }

        return isValid
    }

    val performLogin = {
        focusManager.clearFocus()
        authError = null

        if (validateInputs()) {
            val trimmedEmail = email.trim()
            val trimmedPassword = password
            isLoading = true

            // 1. Check user-requested test account: test@gmail.com / test12
            val isHardcodedTestUser = trimmedEmail.equals(TEST_EMAIL, ignoreCase = true) && trimmedPassword == TEST_PASSWORD
            if (isHardcodedTestUser) {
                coroutineScope.launch {
                    delay(300)
                    isLoading = false
                    val contractId = contracts.firstOrNull()?.id ?: "contract_apex_01"
                    onLogin(trimmedEmail, "CLIENT", contractId)
                }
            } else {
                // 2. Firebase Auth authentication
                val isFirebaseAvailable = try {
                    FirebaseApp.getApps(context).isNotEmpty()
                } catch (e: Exception) {
                    false
                }

                if (isFirebaseAvailable) {
                    try {
                        val auth = FirebaseAuth.getInstance()
                        auth.signInWithEmailAndPassword(trimmedEmail, trimmedPassword)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    isLoading = false
                                    val contractId = contracts.firstOrNull { it.clientEmail.equals(trimmedEmail, ignoreCase = true) }?.id
                                        ?: contracts.firstOrNull()?.id ?: "contract_apex_01"
                                    onLogin(trimmedEmail, "CLIENT", contractId)
                                } else {
                                    auth.createUserWithEmailAndPassword(trimmedEmail, trimmedPassword)
                                        .addOnCompleteListener { createAccountTask ->
                                            isLoading = false
                                            val contractId = contracts.firstOrNull { it.clientEmail.equals(trimmedEmail, ignoreCase = true) }?.id
                                                ?: contracts.firstOrNull()?.id ?: "contract_apex_01"
                                            onLogin(trimmedEmail, "CLIENT", contractId)
                                        }
                                }
                            }
                    } catch (e: Exception) {
                        isLoading = false
                        val contractId = contracts.firstOrNull { it.clientEmail.equals(trimmedEmail, ignoreCase = true) }?.id
                            ?: contracts.firstOrNull()?.id ?: "contract_apex_01"
                        onLogin(trimmedEmail, "CLIENT", contractId)
                    }
                } else {
                    // Smooth local fallback
                    coroutineScope.launch {
                        delay(350)
                        isLoading = false
                        val contractId = contracts.firstOrNull { it.clientEmail.equals(trimmedEmail, ignoreCase = true) }?.id
                            ?: contracts.firstOrNull()?.id ?: "contract_apex_01"
                        onLogin(trimmedEmail, "CLIENT", contractId)
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
    ) {
        // App branding "AppTrack" (Title + Subline, without app icon as requested)
        // Positioned centrally above the form, and hides smoothly when the keyboard is open/form moves up
        AnimatedVisibility(
            visible = !isImeVisible,
            enter = fadeIn(animationSpec = tween(220)),
            exit = fadeOut(animationSpec = tween(180)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 44.dp, start = 24.dp, end = 24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "AppTrack",
                    color = PrimaryAccentColor,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("app_brand_title")
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Track all your apps in one place.",
                    color = SlateGreyColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Only the form moves up when keyboard is active.
        // It sits right above the keyboard without being pushed too high.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .padding(bottom = formBottomPadding),
            contentAlignment = BiasAlignment(horizontalBias = 0f, verticalBias = formVerticalBias)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Form Card Container
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceColor),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header
                        Text(
                            text = "Welcome to AppTrack\nSign in to your account",
                            color = PrimaryTextColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 27.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Optional Auth Error Banner
                        AnimatedVisibility(
                            visible = authError != null,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Box(
                                modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFEF2F2))
                                .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = ErrorRedColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = authError ?: "",
                                        color = ErrorRedColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Email Field
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Email",
                                color = PrimaryTextColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            TextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    emailError = null
                                    authError = null
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = {
                                        focusManager.moveFocus(FocusDirection.Down)
                                    }
                                ),
                                placeholder = {
                                    Text(
                                        text = "example@gmail.com",
                                        color = SlateGreyColor,
                                        fontSize = 14.sp
                                    )
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = InputFieldBgColor,
                                    unfocusedContainerColor = InputFieldBgColor,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedTextColor = PrimaryTextColor,
                                    unfocusedTextColor = PrimaryTextColor,
                                    cursorColor = PrimaryAccentColor
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .then(
                                        if (emailError != null) {
                                            Modifier.border(1.dp, ErrorBorderColor, RoundedCornerShape(14.dp))
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .testTag("login_email_input")
                            )

                            // Email Error Message
                            AnimatedVisibility(
                                visible = emailError != null,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Text(
                                    text = emailError ?: "",
                                    color = ErrorRedColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Password Field
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Password",
                                color = PrimaryTextColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            TextField(
                                value = password,
                                onValueChange = {
                                    password = it
                                    passwordError = null
                                    authError = null
                                },
                                singleLine = true,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (!isLoading) performLogin()
                                    }
                                ),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                            tint = SlateGreyColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                placeholder = {
                                    Text(
                                        text = "••••••••",
                                        color = SlateGreyColor,
                                        fontSize = 14.sp
                                    )
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = InputFieldBgColor,
                                    unfocusedContainerColor = InputFieldBgColor,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedTextColor = PrimaryTextColor,
                                    unfocusedTextColor = PrimaryTextColor,
                                    cursorColor = PrimaryAccentColor
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .then(
                                        if (passwordError != null) {
                                            Modifier.border(1.dp, ErrorBorderColor, RoundedCornerShape(14.dp))
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .testTag("login_password_input")
                            )

                            // Password Error Message
                            AnimatedVisibility(
                                visible = passwordError != null,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Text(
                                    text = passwordError ?: "",
                                    color = ErrorRedColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Login Button with Loading Indicator
                        Button(
                            onClick = {
                                if (!isLoading) performLogin()
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryAccentColor,
                                disabledContainerColor = PrimaryAccentColor.copy(alpha = 0.7f)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("login_submit_btn")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = "Login",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
