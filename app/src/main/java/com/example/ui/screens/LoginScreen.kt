package com.example.ui.screens

import android.app.Activity
import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.data.model.ContractEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Vibrant Blue Gradient for Top Section
private val BlueGradientStart = Color(0xFF323CE8)
private val BlueGradientMid = Color(0xFF4552FA)
private val BlueGradientEnd = Color(0xFF5664FC)

// Button Horizontal Gradient (Vibrant Blue to Purple/Pink)
private val ButtonGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF3855F8),
        Color(0xFF7551F9),
        Color(0xFFB55CF9)
    )
)

private val PrimaryDarkText = Color(0xFF1E2124)
private val SubtitleGray = Color(0xFF737A84)
private val LabelGray = Color(0xFF8F96A0)
private val BorderNormal = Color(0xFFE5E7EB)
private val BorderFocused = Color(0xFF4552FA)
private val InputBgColor = Color(0xFFFCFDFE)
private val ErrorRed = Color(0xFFFF3B30)
private val ErrorBg = Color(0xFFFFF2F2)
private val ErrorBorder = Color(0xFFFFD2D0)

private val AppleSpring = CubicBezierEasing(0.16f, 1.0f, 0.3f, 1.0f)
private val IOSEasing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)

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

    var isEmailFocused by remember { mutableStateOf(false) }
    var isPasswordFocused by remember { mutableStateOf(false) }

    // Validation & Error States
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var authError by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    val isImeVisible = WindowInsets.isImeVisible

    // Match Device System Bar Color: White icons on Dark Blue Header, dark icons on White Sheet
    val activity = context as? Activity
    DisposableEffect(Unit) {
        val window = activity?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.isAppearanceLightStatusBars = false // White status bar text/icons (time, battery, wifi)
            insetsController.isAppearanceLightNavigationBars = true // Dark navigation bar buttons on white sheet
        }
        onDispose {
            val window = activity?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.isAppearanceLightStatusBars = true
            }
        }
    }



    fun resolveContractAndRole(trimmedEmail: String): Pair<String, String> {
        val matchingContract = contracts.firstOrNull { it.clientEmail.equals(trimmedEmail, ignoreCase = true) }
        val role = "CLIENT"
        val contractId = matchingContract?.id ?: contracts.firstOrNull()?.id ?: "contract_apex_01"
        return Pair(role, contractId)
    }

    fun parseFirebaseAuthError(e: Throwable): String {
        return when (e) {
            is FirebaseAuthInvalidUserException -> "No account found with this email. Please check your credentials."
            is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password. Please verify your credentials."
            is FirebaseTooManyRequestsException -> "Too many failed attempts. Please wait a moment before trying again."
            is FirebaseNetworkException -> "Network connection error. Please check your internet connection."
            is FirebaseAuthException -> e.localizedMessage ?: "Authentication failed. Please verify your credentials."
            else -> e.localizedMessage ?: "Unable to sign in. Please try again."
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

            val isFirebaseAvailable = try {
                FirebaseApp.getApps(context).isNotEmpty()
            } catch (e: Exception) {
                false
            }

            if (isFirebaseAvailable) {
                val auth = FirebaseAuth.getInstance()
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
                coroutineScope.launch {
                    delay(350)
                    isLoading = false
                    val (role, contractId) = resolveContractAndRole(trimmedEmail)
                    onLogin(trimmedEmail, role, contractId)
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BlueGradientStart, BlueGradientMid, BlueGradientEnd)
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
    ) {
        val totalScreenHeight = maxHeight
        val normalTopBlueSectionHeight = (totalScreenHeight * 0.28f).coerceAtLeast(150.dp)

        val animatedTopBlueHeight by animateDpAsState(
            targetValue = if (isImeVisible) (normalTopBlueSectionHeight - 40.dp) else normalTopBlueSectionHeight,
            animationSpec = spring(
                dampingRatio = 0.85f,
                stiffness = 380f
            ),
            label = "topBlueHeight"
        )
        val titleScale by animateFloatAsState(
            targetValue = if (isImeVisible) 0.92f else 1.0f,
            animationSpec = spring(
                dampingRatio = 0.85f,
                stiffness = 380f
            ),
            label = "titleScale"
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. TOP BLUE SECTION: Shrinks gracefully when keyboard is active, smoothly shifting white container up
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(animatedTopBlueHeight)
                    .statusBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                MacHelloHandwritingTitle(
                    modifier = Modifier.scale(titleScale)
                )
            }

            // 2. BOTTOM WHITE SHEET: Whole container moves up smoothly, inner column is NOT scrollable
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Layered Card Peek Indicator (matches reference image curve directly above sheet)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .background(Color.White.copy(alpha = 0.25f))
                    )

                    // Main White Bottom Sheet (Fills remaining height, whole container shifts up, NOT internally scrollable)
                    Surface(
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                        color = Color.White,
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .navigationBarsPadding()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    horizontal = 24.dp,
                                    vertical = if (isImeVisible) 18.dp else 24.dp
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Title: Welcome Back
                            Text(
                                text = "Welcome Back",
                                color = PrimaryDarkText,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.4).sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Subtitle: Enter your details below
                            Text(
                                text = "Enter your details below",
                                color = SubtitleGray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(if (isImeVisible) 14.dp else 26.dp))

                            // Error Banner
                            AnimatedVisibility(
                                visible = authError != null,
                                enter = fadeIn(animationSpec = tween(200, easing = IOSEasing)) +
                                        slideInVertically(animationSpec = tween(200, easing = IOSEasing)),
                                exit = fadeOut(animationSpec = tween(160, easing = IOSEasing)) +
                                        slideOutVertically(animationSpec = tween(160, easing = IOSEasing))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ErrorBg)
                                        .border(1.dp, ErrorBorder, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ErrorOutline,
                                            contentDescription = null,
                                            tint = ErrorRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = authError ?: "",
                                            color = ErrorRed,
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
                                        .padding(bottom = 16.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF0FDF4))
                                        .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF16A34A),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = successMessage ?: "",
                                            color = Color(0xFF16A34A),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }

                            // 1. Professional Email Address Input Card
                            val emailBorderColor by animateColorAsState(
                                targetValue = when {
                                    emailError != null -> ErrorRed
                                    isEmailFocused -> BorderFocused
                                    else -> BorderNormal
                                },
                                animationSpec = tween(180),
                                label = "emailBorderColor"
                            )

                            val emailLabelColor by animateColorAsState(
                                targetValue = when {
                                    emailError != null -> ErrorRed
                                    isEmailFocused -> BorderFocused
                                    else -> LabelGray
                                },
                                animationSpec = tween(180),
                                label = "emailLabelColor"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(InputBgColor)
                                    .border(
                                        width = if (isEmailFocused) 1.5.dp else 1.dp,
                                        color = emailBorderColor,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 18.dp, vertical = 9.dp)
                                    .testTag("login_email_input")
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Email Address",
                                        color = emailLabelColor,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.2.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    BasicTextField(
                                        value = email,
                                        onValueChange = {
                                            email = it
                                            emailError = null
                                            authError = null
                                            successMessage = null
                                        },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            color = PrimaryDarkText,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        cursorBrush = SolidColor(BorderFocused),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Email,
                                            imeAction = ImeAction.Next
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onFocusChanged { isEmailFocused = it.isFocused }
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = emailError != null,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Text(
                                    text = emailError ?: "",
                                    color = ErrorRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp, start = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(if (isImeVisible) 12.dp else 16.dp))

                            // 2. Professional Password Input Card with Eye Toggle
                            val passwordBorderColor by animateColorAsState(
                                targetValue = when {
                                    passwordError != null -> ErrorRed
                                    isPasswordFocused -> BorderFocused
                                    else -> BorderNormal
                                },
                                animationSpec = tween(180),
                                label = "passwordBorderColor"
                            )

                            val passwordLabelColor by animateColorAsState(
                                targetValue = when {
                                    passwordError != null -> ErrorRed
                                    isPasswordFocused -> BorderFocused
                                    else -> LabelGray
                                },
                                animationSpec = tween(180),
                                label = "passwordLabelColor"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(InputBgColor)
                                    .border(
                                        width = if (isPasswordFocused) 1.5.dp else 1.dp,
                                        color = passwordBorderColor,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(start = 18.dp, end = 8.dp, top = 9.dp, bottom = 9.dp)
                                    .testTag("login_password_input")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Password",
                                            color = passwordLabelColor,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.2.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        BasicTextField(
                                            value = password,
                                            onValueChange = {
                                                password = it
                                                passwordError = null
                                                authError = null
                                                successMessage = null
                                            },
                                            singleLine = true,
                                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                            textStyle = TextStyle(
                                                color = PrimaryDarkText,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            cursorBrush = SolidColor(BorderFocused),
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Password,
                                                imeAction = ImeAction.Done
                                            ),
                                            keyboardActions = KeyboardActions(
                                                onDone = {
                                                    if (!isLoading) performAuth()
                                                }
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .onFocusChanged { isPasswordFocused = it.isFocused }
                                        )
                                    }

                                    IconButton(
                                        onClick = { passwordVisible = !passwordVisible },
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                            tint = if (isPasswordFocused) BorderFocused else LabelGray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            AnimatedVisibility(
                                visible = passwordError != null,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Text(
                                    text = passwordError ?: "",
                                    color = ErrorRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp, start = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(if (isImeVisible) 20.dp else 28.dp))

                            // 3. Vibrant Gradient Sign In Button
                            val buttonInteractionSource = remember { MutableInteractionSource() }
                            val isPressed by buttonInteractionSource.collectIsPressedAsState()
                            val buttonScale by animateFloatAsState(
                                targetValue = if (isPressed) 0.97f else 1.0f,
                                animationSpec = tween(durationMillis = 120, easing = IOSEasing),
                                label = "buttonScale"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .scale(buttonScale)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(ButtonGradient)
                                    .clickable(
                                        interactionSource = buttonInteractionSource,
                                        indication = null,
                                        enabled = !isLoading
                                    ) {
                                        performAuth()
                                    }
                                    .testTag("login_submit_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.2.dp,
                                        modifier = Modifier.size(22.dp)
                                    )
                                } else {
                                    Text(
                                        text = "Sign in",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.2).sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(if (isImeVisible) 14.dp else 28.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Mac "hello" Handwriting Animation for AppTrack.
 *
 * Faithfully recreates the iconic Apple MacBook cursive write-on animation:
 * 1. Fluid cursive typography (FontFamily.Cursive, bold, luminous white on royal blue).
 * 2. Progressive ink stroke reveal following natural cursive handwriting slant.
 * 3. Glowing fountain pen nib / ink droplet that traces the cursive baseline, ascenders, and descenders.
 * 4. Signature Mac flourish underline sweeping gracefully underneath once the script finishes drawing.
 * 5. Specular light shimmer sweep across the completed script.
 * 6. Interactive: Tapping replays the handwriting animation.
 */
@Composable
fun MacHelloHandwritingTitle(
    modifier: Modifier = Modifier,
    text: String = "AppTrack"
) {
    var replayTrigger by remember { mutableStateOf(0) }
    val writeProgress = remember { Animatable(0f) }
    val flourishProgress = remember { Animatable(0f) }
    val shimmerSweep = remember { Animatable(0f) }

    val textMeasurer = rememberTextMeasurer()
    val textStyle = remember {
        TextStyle(
            fontFamily = FontFamily.Cursive,
            fontSize = 46.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            shadow = Shadow(
                color = Color(0x55000000),
                offset = Offset(0f, 4f),
                blurRadius = 12f
            )
        )
    }

    val textLayoutResult = remember(textMeasurer, text, textStyle) {
        textMeasurer.measure(
            text = AnnotatedString(text),
            style = textStyle
        )
    }

    val density = LocalDensity.current
    val totalWidth = textLayoutResult.size.width.toFloat()
    val totalHeight = textLayoutResult.size.height.toFloat()
    val canvasWidthDp = with(density) { (totalWidth + 28.dp.toPx()).toDp() }
    val canvasHeightDp = with(density) { (totalHeight + 28.dp.toPx()).toDp() }

    LaunchedEffect(replayTrigger) {
        writeProgress.snapTo(0f)
        flourishProgress.snapTo(0f)
        shimmerSweep.snapTo(0f)
        delay(120)

        // 1. Write the cursive letters progressively (stroke by stroke)
        writeProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 2100,
                easing = CubicBezierEasing(0.35f, 0.0f, 0.25f, 1.0f)
            )
        )

        // 2. Draw signature Mac cursive flourish underline
        flourishProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 650,
                easing = CubicBezierEasing(0.25f, 0.0f, 0.15f, 1.0f)
            )
        )

        // 3. Shimmer wave sweep across the completed script
        shimmerSweep.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 900,
                easing = FastOutSlowInEasing
            )
        )
    }

    // Continuous subtle shimmer wave while idle
    val infiniteTransition = rememberInfiniteTransition(label = "idleShimmer")
    val idleShimmerX by infiniteTransition.animateFloat(
        initialValue = -150f,
        targetValue = totalWidth + 250f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, delayMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "idleShimmerX"
    )

    Canvas(
        modifier = modifier
            .size(canvasWidthDp, canvasHeightDp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                replayTrigger++
            }
            .testTag("app_brand_title")
    ) {
        val wp = writeProgress.value
        val fp = flourishProgress.value

        // 1. Reveal Cursive Letters
        if (wp > 0.001f) {
            val slantOffset = 18.dp.toPx()
            val currentX = totalWidth * wp

            if (wp < 1f) {
                val clipPath = Path().apply {
                    moveTo(-slantOffset - 20f, -30f)
                    lineTo(currentX + 8.dp.toPx(), -30f)
                    lineTo(currentX + 8.dp.toPx() - slantOffset, totalHeight + 30f)
                    lineTo(-slantOffset - 20f, totalHeight + 30f)
                    close()
                }

                clipPath(clipPath) {
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(4.dp.toPx(), 4.dp.toPx())
                    )
                }

                // Glowing Pen Nib / Ink Spark at current writing position
                val penY = calculatePenY(wp, totalHeight) + 4.dp.toPx()
                // Adjust X for the slant at this specific Y
                val penTipX = currentX + 4.dp.toPx() - slantOffset * (penY / totalHeight).coerceIn(0f, 1f)
                val penTip = Offset(penTipX, penY)

                // Soft outer glowing halo
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.95f),
                            Color(0xFF93C5FD).copy(alpha = 0.65f),
                            Color(0xFF60A5FA).copy(alpha = 0.20f),
                            Color.Transparent
                        ),
                        center = penTip,
                        radius = 18.dp.toPx()
                    ),
                    center = penTip,
                    radius = 18.dp.toPx()
                )

                // Bright core spark
                drawCircle(
                    color = Color.White,
                    radius = 3.6.dp.toPx(),
                    center = penTip
                )

                // Trailing ink droplet particle
                drawCircle(
                    color = Color(0xFFBAE6FD).copy(alpha = 0.85f),
                    radius = 1.8.dp.toPx(),
                    center = Offset(penTip.x - 5.dp.toPx(), penTip.y + 2.5.dp.toPx())
                )
            } else {
                // Completed text: render with subtle specular shimmer
                val shimmerBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.90f),
                        Color.White,
                        Color(0xFFE2E8FF),
                        Color.White
                    ),
                    start = Offset(idleShimmerX, 0f),
                    end = Offset(idleShimmerX + 160f, totalHeight)
                )
                drawText(
                    textLayoutResult = textLayoutResult,
                    brush = shimmerBrush,
                    topLeft = Offset(4.dp.toPx(), 4.dp.toPx())
                )
            }
        }

        // 2. Signature Mac Underline Flourish
        if (fp > 0.001f) {
            val flourishY = totalHeight + 4.dp.toPx()
            val startX = 4.dp.toPx() + totalWidth * 0.04f
            val endX = 4.dp.toPx() + totalWidth * 0.98f

            val flourishPath = Path().apply {
                moveTo(startX, flourishY - 2.dp.toPx())
                cubicTo(
                    startX + totalWidth * 0.28f, flourishY + 12.dp.toPx(),
                    startX + totalWidth * 0.68f, flourishY + 14.dp.toPx(),
                    endX, flourishY + 2.dp.toPx()
                )
            }

            val pathMeasure = PathMeasure()
            pathMeasure.setPath(flourishPath, false)
            val pathLen = pathMeasure.length
            val drawnSegment = Path()
            pathMeasure.getSegment(0f, pathLen * fp, drawnSegment, true)

            drawPath(
                path = drawnSegment,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.35f),
                        Color.White,
                        Color(0xFFDDE6FF),
                        Color.White
                    )
                ),
                style = Stroke(
                    width = 2.8.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            if (fp in 0.01f..0.98f) {
                val tipPos = pathMeasure.getPosition(pathLen * fp)
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = tipPos
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.8f),
                            Color(0xFF93C5FD).copy(alpha = 0.4f),
                            Color.Transparent
                        ),
                        center = tipPos,
                        radius = 12.dp.toPx()
                    ),
                    center = tipPos,
                    radius = 12.dp.toPx()
                )
            } else if (fp >= 0.98f) {
                // Elegant terminal dot at the tip of the flourish
                val endPos = pathMeasure.getPosition(pathLen)
                drawCircle(
                    color = Color.White,
                    radius = 2.8.dp.toPx(),
                    center = endPos
                )
            }
        }
    }
}

private fun lerpFloat(start: Float, stop: Float, fraction: Float): Float =
    (1f - fraction) * start + fraction * stop

/**
 * Calculates cursive pen height (Y coordinate) as "AppTrack" is being written.
 * Accurately undulates between the baseline, ascender crests ('A', 'T', 'k'),
 * and descender loops ('p', 'p').
 */
private fun calculatePenY(progress: Float, totalHeight: Float): Float {
    val h = totalHeight
    val base = h * 0.72f
    val mid = h * 0.48f
    val asc = h * 0.20f
    val desc = h * 0.88f

    return when {
        // 'A' (0.00 .. 0.15)
        progress < 0.03f -> base
        progress < 0.08f -> lerpFloat(base, asc, (progress - 0.03f) / 0.05f)
        progress < 0.12f -> lerpFloat(asc, base, (progress - 0.08f) / 0.04f)
        progress < 0.15f -> lerpFloat(base, mid, (progress - 0.12f) / 0.03f)
        // First 'p' (0.15 .. 0.27)
        progress < 0.20f -> lerpFloat(mid, desc, (progress - 0.15f) / 0.05f)
        progress < 0.24f -> lerpFloat(desc, mid, (progress - 0.20f) / 0.04f)
        progress < 0.27f -> lerpFloat(mid, base, (progress - 0.24f) / 0.03f)
        // Second 'p' (0.27 .. 0.39)
        progress < 0.32f -> lerpFloat(base, desc, (progress - 0.27f) / 0.05f)
        progress < 0.36f -> lerpFloat(desc, mid, (progress - 0.32f) / 0.04f)
        progress < 0.39f -> lerpFloat(mid, base, (progress - 0.36f) / 0.03f)
        // 'T' (0.39 .. 0.52)
        progress < 0.43f -> lerpFloat(base, asc, (progress - 0.39f) / 0.04f)
        progress < 0.48f -> lerpFloat(asc, base, (progress - 0.43f) / 0.05f)
        progress < 0.52f -> lerpFloat(base, mid, (progress - 0.48f) / 0.04f)
        // 'r' (0.52 .. 0.63)
        progress < 0.56f -> lerpFloat(mid, mid - 10f, (progress - 0.52f) / 0.04f)
        progress < 0.63f -> lerpFloat(mid - 10f, base, (progress - 0.56f) / 0.07f)
        // 'a' (0.63 .. 0.74)
        progress < 0.68f -> lerpFloat(base, mid, (progress - 0.63f) / 0.05f)
        progress < 0.74f -> lerpFloat(mid, base, (progress - 0.68f) / 0.06f)
        // 'c' (0.74 .. 0.85)
        progress < 0.79f -> lerpFloat(base, mid, (progress - 0.74f) / 0.05f)
        progress < 0.85f -> lerpFloat(mid, base, (progress - 0.79f) / 0.06f)
        // 'k' (0.85 .. 1.00)
        progress < 0.90f -> lerpFloat(base, asc, (progress - 0.85f) / 0.05f)
        progress < 0.95f -> lerpFloat(asc, base, (progress - 0.90f) / 0.05f)
        else -> lerpFloat(base, mid, (progress - 0.95f) / 0.05f)
    }
}
