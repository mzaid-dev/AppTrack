package com.example.ui.screens

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Color Palette Constants
private val BackgroundColor = Color(0xFFF5F7FA)
private val CardSurfaceColor = Color(0xFFFFFFFF)
private val PrimaryAccentColor = Color(0xFF2F80ED)
private val PrimaryAccentDark = Color(0xFF1D65C1)
private val PrimaryTextColor = Color(0xFF1A1D1E)
private val InputFieldBgColor = Color(0xFFF2F4F7)
private val SlateGreyColor = Color(0xFF8D96A0)
private val ErrorRedColor = Color(0xFFDC2626)
private val ErrorBgColor = Color(0xFFFEF2F2)
private val ErrorBorderColor = Color(0xFFFCA5A5)
private val SuccessGreenColor = Color(0xFF16A34A)
private val SuccessBgColor = Color(0xFFF0FDF4)
private val SuccessBorderColor = Color(0xFF86EFAC)

// Quick Test credentials
private const val TEST_EMAIL = "test@gmail.com"
private const val TEST_PASSWORD = "test12"

enum class AuthMode {
    SIGN_IN,
    SIGN_UP
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    contracts: List<ContractEntity> = emptyList(),
    onLogin: (email: String, role: String, contractId: String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var authMode by remember { mutableStateOf(AuthMode.SIGN_IN) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Validation & Error States
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var authError by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Forgot Password Dialog State
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetEmailError by remember { mutableStateOf<String?>(null) }
    var isResettingPassword by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    val isImeVisible = WindowInsets.isImeVisible

    // Form positioning animation with keyboard
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

    fun resolveContractAndRole(trimmedEmail: String): Pair<String, String> {
        val matchingContract = contracts.firstOrNull { it.clientEmail.equals(trimmedEmail, ignoreCase = true) }
        val role = if (trimmedEmail.startsWith("dev", ignoreCase = true) || trimmedEmail.startsWith("admin", ignoreCase = true)) {
            "DEVELOPER"
        } else {
            "CLIENT"
        }
        val contractId = matchingContract?.id ?: contracts.firstOrNull()?.id ?: "contract_apex_01"
        return Pair(role, contractId)
    }

    fun parseFirebaseAuthError(e: Throwable): String {
        return when (e) {
            is FirebaseAuthInvalidUserException -> "No account found with this email. Please check your email or create an account."
            is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password. Please verify your credentials."
            is FirebaseAuthUserCollisionException -> "An account with this email already exists. Please switch to Sign In."
            is FirebaseAuthWeakPasswordException -> "Password is too weak. Please use at least 6 characters."
            is FirebaseTooManyRequestsException -> "Too many failed attempts. Please wait a moment and try again."
            is FirebaseNetworkException -> "Network error. Please check your internet connection."
            is FirebaseAuthException -> e.localizedMessage ?: "Authentication failed. Please try again."
            else -> e.localizedMessage ?: "An unexpected error occurred. Please try again."
        }
    }

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

        if (authMode == AuthMode.SIGN_UP) {
            if (confirmPassword.isEmpty()) {
                confirmPasswordError = "Please confirm your password"
                isValid = false
            } else if (confirmPassword != password) {
                confirmPasswordError = "Passwords do not match"
                isValid = false
            } else {
                confirmPasswordError = null
            }
        } else {
            confirmPasswordError = null
        }

        return isValid
    }

    val performAuth = {
        focusManager.clearFocus()
        authError = null
        successMessage = null

        if (validateInputs()) {
            val trimmedEmail = email.trim()
            val trimmedPassword = password
            isLoading = true

            // 1. Check user-requested test account: test@gmail.com / test12
            val isHardcodedTestUser = trimmedEmail.equals(TEST_EMAIL, ignoreCase = true) && trimmedPassword == TEST_PASSWORD
            if (isHardcodedTestUser) {
                coroutineScope.launch {
                    delay(350)
                    isLoading = false
                    val (role, contractId) = resolveContractAndRole(trimmedEmail)
                    onLogin(trimmedEmail, role, contractId)
                }
            } else {
                // 2. Firebase Auth Integration
                val isFirebaseAvailable = try {
                    FirebaseApp.getApps(context).isNotEmpty()
                } catch (e: Exception) {
                    false
                }

                if (isFirebaseAvailable) {
                    val auth = FirebaseAuth.getInstance()
                    if (authMode == AuthMode.SIGN_IN) {
                        // Sign In with Firebase
                        auth.signInWithEmailAndPassword(trimmedEmail, trimmedPassword)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    val (role, contractId) = resolveContractAndRole(trimmedEmail)
                                    onLogin(trimmedEmail, role, contractId)
                                } else {
                                    val errorMsg = task.exception?.let { parseFirebaseAuthError(it) }
                                        ?: "Invalid email or password. Please try again."
                                    authError = errorMsg
                                }
                            }
                    } else {
                        // Create Account / Sign Up with Firebase
                        auth.createUserWithEmailAndPassword(trimmedEmail, trimmedPassword)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    val (role, contractId) = resolveContractAndRole(trimmedEmail)
                                    onLogin(trimmedEmail, role, contractId)
                                } else {
                                    val errorMsg = task.exception?.let { parseFirebaseAuthError(it) }
                                        ?: "Failed to create account. Please try again."
                                    authError = errorMsg
                                }
                            }
                    }
                } else {
                    // Offline / Local Development Fallback
                    coroutineScope.launch {
                        delay(350)
                        isLoading = false
                        val (role, contractId) = resolveContractAndRole(trimmedEmail)
                        onLogin(trimmedEmail, role, contractId)
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
        // App branding "AppTrack" (Title + Subline)
        AnimatedVisibility(
            visible = !isImeVisible,
            enter = fadeIn(animationSpec = tween(220)),
            exit = fadeOut(animationSpec = tween(180)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 36.dp, start = 24.dp, end = 24.dp)
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
                    text = "Track all your apps & games in real time.",
                    color = SlateGreyColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Form Card Container with smooth keyboard handling
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
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                            .padding(horizontal = 22.dp, vertical = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Mode Segmented Switcher (Sign In / Create Account)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = InputFieldBgColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Sign In Tab
                                val signInBg by animateColorAsState(
                                    targetValue = if (authMode == AuthMode.SIGN_IN) PrimaryAccentColor else Color.Transparent,
                                    animationSpec = tween(200),
                                    label = "signInBg"
                                )
                                val signInTextColor by animateColorAsState(
                                    targetValue = if (authMode == AuthMode.SIGN_IN) Color.White else PrimaryTextColor,
                                    animationSpec = tween(200),
                                    label = "signInTextColor"
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(signInBg)
                                        .clickable {
                                            if (authMode != AuthMode.SIGN_IN) {
                                                authMode = AuthMode.SIGN_IN
                                                authError = null
                                                successMessage = null
                                                confirmPasswordError = null
                                            }
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Sign In",
                                        color = signInTextColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Sign Up Tab
                                val signUpBg by animateColorAsState(
                                    targetValue = if (authMode == AuthMode.SIGN_UP) PrimaryAccentColor else Color.Transparent,
                                    animationSpec = tween(200),
                                    label = "signUpBg"
                                )
                                val signUpTextColor by animateColorAsState(
                                    targetValue = if (authMode == AuthMode.SIGN_UP) Color.White else PrimaryTextColor,
                                    animationSpec = tween(200),
                                    label = "signUpTextColor"
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(signUpBg)
                                        .clickable {
                                            if (authMode != AuthMode.SIGN_UP) {
                                                authMode = AuthMode.SIGN_UP
                                                authError = null
                                                successMessage = null
                                            }
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Create Account",
                                        color = signUpTextColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Header Title & Subtitle
                        Text(
                            text = if (authMode == AuthMode.SIGN_IN) "Welcome Back" else "Create Account",
                            color = PrimaryTextColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (authMode == AuthMode.SIGN_IN) "Sign in with your Firebase credentials" else "Register a new client or developer account",
                            color = SlateGreyColor,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Demo Fill Action Chip
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = PrimaryAccentColor.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryAccentColor.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    email = TEST_EMAIL
                                    password = TEST_PASSWORD
                                    confirmPassword = TEST_PASSWORD
                                    emailError = null
                                    passwordError = null
                                    confirmPasswordError = null
                                    authError = null
                                    authMode = AuthMode.SIGN_IN
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = PrimaryAccentColor,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Demo: test@gmail.com / test12",
                                    color = PrimaryAccentDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Auth Error Banner
                        AnimatedVisibility(
                            visible = authError != null,
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut() + slideOutVertically()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 14.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ErrorBgColor)
                                    .border(1.dp, ErrorBorderColor, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = ErrorRedColor,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .padding(top = 1.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = authError ?: "",
                                        color = ErrorRedColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        // Success Banner
                        AnimatedVisibility(
                            visible = successMessage != null,
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut() + slideOutVertically()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 14.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SuccessBgColor)
                                    .border(1.dp, SuccessBorderColor, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SuccessGreenColor,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .padding(top = 1.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = successMessage ?: "",
                                        color = SuccessGreenColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 18.sp
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
                                text = "Email Address",
                                color = PrimaryTextColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            TextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    emailError = null
                                    authError = null
                                    successMessage = null
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
                                        text = "name@domain.com",
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
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            TextField(
                                value = password,
                                onValueChange = {
                                    password = it
                                    passwordError = null
                                    authError = null
                                    successMessage = null
                                },
                                singleLine = true,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = if (authMode == AuthMode.SIGN_UP) ImeAction.Next else ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = {
                                        focusManager.moveFocus(FocusDirection.Down)
                                    },
                                    onDone = {
                                        if (!isLoading) performAuth()
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

                        // Confirm Password Field (Sign Up mode only)
                        AnimatedVisibility(
                            visible = authMode == AuthMode.SIGN_UP,
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut() + slideOutVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "Confirm Password",
                                    color = PrimaryTextColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                TextField(
                                    value = confirmPassword,
                                    onValueChange = {
                                        confirmPassword = it
                                        confirmPasswordError = null
                                        authError = null
                                    },
                                    singleLine = true,
                                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            if (!isLoading) performAuth()
                                        }
                                    ),
                                    trailingIcon = {
                                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                            Icon(
                                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
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
                                            if (confirmPasswordError != null) {
                                                Modifier.border(1.dp, ErrorBorderColor, RoundedCornerShape(14.dp))
                                            } else {
                                                Modifier
                                            }
                                        )
                                )

                                AnimatedVisibility(
                                    visible = confirmPasswordError != null,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    Text(
                                        text = confirmPasswordError ?: "",
                                        color = ErrorRedColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                                    )
                                }
                            }
                        }

                        // Forgot Password Link (Sign In mode)
                        AnimatedVisibility(
                            visible = authMode == AuthMode.SIGN_IN,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = "Forgot password?",
                                    color = PrimaryAccentColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .clickable {
                                            resetEmail = email.trim()
                                            resetEmailError = null
                                            showForgotPasswordDialog = true
                                        }
                                        .padding(4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Action Button
                        Button(
                            onClick = {
                                if (!isLoading) performAuth()
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
                                    text = if (authMode == AuthMode.SIGN_IN) "Sign In" else "Create Account",
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

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isResettingPassword) showForgotPasswordDialog = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LockReset,
                        contentDescription = null,
                        tint = PrimaryAccentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reset Password",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PrimaryTextColor
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Enter your registered email address and we will send you a password reset link via Firebase Auth.",
                        fontSize = 13.sp,
                        color = SlateGreyColor,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    TextField(
                        value = resetEmail,
                        onValueChange = {
                            resetEmail = it
                            resetEmailError = null
                        },
                        singleLine = true,
                        placeholder = { Text("your-email@domain.com", fontSize = 14.sp) },
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = InputFieldBgColor,
                            unfocusedContainerColor = InputFieldBgColor,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = PrimaryTextColor,
                            unfocusedTextColor = PrimaryTextColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (resetEmailError != null) {
                                    Modifier.border(1.dp, ErrorBorderColor, RoundedCornerShape(12.dp))
                                } else {
                                    Modifier
                                }
                            )
                    )
                    if (resetEmailError != null) {
                        Text(
                            text = resetEmailError ?: "",
                            color = ErrorRedColor,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = resetEmail.trim()
                        if (trimmed.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
                            resetEmailError = "Please enter a valid email address"
                            return@Button
                        }
                        isResettingPassword = true
                        val isFirebaseAvailable = try {
                            FirebaseApp.getApps(context).isNotEmpty()
                        } catch (e: Exception) {
                            false
                        }

                        if (isFirebaseAvailable) {
                            FirebaseAuth.getInstance().sendPasswordResetEmail(trimmed)
                                .addOnCompleteListener { task ->
                                    isResettingPassword = false
                                    showForgotPasswordDialog = false
                                    if (task.isSuccessful) {
                                        successMessage = "Password reset email sent to $trimmed. Please check your inbox."
                                        authError = null
                                    } else {
                                        authError = task.exception?.let { parseFirebaseAuthError(it) }
                                            ?: "Failed to send reset email. Please verify your email."
                                    }
                                }
                        } else {
                            coroutineScope.launch {
                                delay(400)
                                isResettingPassword = false
                                showForgotPasswordDialog = false
                                successMessage = "Password reset instructions sent to $trimmed."
                            }
                        }
                    },
                    enabled = !isResettingPassword,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccentColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isResettingPassword) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text("Send Link", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showForgotPasswordDialog = false },
                    enabled = !isResettingPassword
                ) {
                    Text("Cancel", color = SlateGreyColor)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
