package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.ProjectEntity
import com.example.util.NetworkUtils
import com.google.firebase.auth.FirebaseAuth

// ───── Royal Blue & Frosted Glass Design Palette ─────
private val BlueGradientStart = Color(0xFF1E2CC8)
private val BlueGradientMid = Color(0xFF2E40ED)
private val BlueGradientEnd = Color(0xFF4358F6)

private val HeaderRoyalGradient = Brush.verticalGradient(
    colors = listOf(BlueGradientStart, BlueGradientMid, BlueGradientEnd)
)

private val GlassSurfaceBrush = Brush.linearGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.24f),
        Color.White.copy(alpha = 0.10f)
    )
)

private val GlassBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.40f),
        Color.White.copy(alpha = 0.12f)
    )
)

private val AppTrackBg = Color(0xFFF8FAFC)
private val AppTrackPrimary = Color(0xFF2835D8)
private val AppTrackTextPrimary = Color(0xFF0F172A)
private val AppTrackTextMuted = Color(0xFF64748B)

// Status colors
private val StatusLiveGreen = Color(0xFF10B981)
private val StatusLiveGreenBg = Color(0xFFECFDF5)
private val StatusLiveGreenBorder = Color(0xFFA7F3D0)

private val StatusPendingAmber = Color(0xFFF59E0B)
private val StatusPendingAmberBg = Color(0xFFFFFBEB)
private val StatusPendingAmberBorder = Color(0xFFFDE68A)

private val StatusUnableRed = Color(0xFFEF4444)
private val StatusUnableBg = Color(0xFFFEF2F2)
private val StatusUnableBorder = Color(0xFFFECACA)

/**
 * State-of-the-Art HomeScreen for AppTrack:
 * - Fixed Top Royal Blue Header Container with smooth 32.dp rounded bottom corners.
 * - iOS-Style Segmented Tabs (Applications vs Games).
 * - Enhanced Frosted Glass Stats Cards with focused, sharp top-right icons.
 * - Crisp White/Light scrollable list of apps with squircle icons, pulsing status, and capsule actions.
 * - No package names, no redundant headers, and silent non-intrusive Floating Action Button.
 * - Clean, professional logout dialog.
 */
@Composable
fun HomeScreen(
    userEmail: String,
    userRole: String = "CLIENT",
    projects: List<ProjectEntity>,
    selectedTab: String,
    searchQuery: String,
    isRefreshing: Boolean,
    checkingProgress: Pair<Int, Int>,
    feedbackMessage: String?,
    showAddDialog: Boolean = false,
    isLoadingNextPage: Boolean = false,
    checkingIds: Set<String> = emptySet(),
    userName: String = "",
    onTabSelected: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onRefreshTab: (String) -> Unit = {},
    onLoadMore: () -> Unit = {},
    onCheckSingle: (ProjectEntity) -> Unit,
    onShowAddDialog: (Boolean) -> Unit = {},
    onAddNewProject: (title: String, packageName: String, category: String, iconUrl: String) -> Unit = { _, _, _, _ -> },
    onDismissFeedback: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showOfflineDialog by remember { mutableStateOf(false) }

    // Status bar: White icons on royal blue header; dark navigation bar icons
    val activity = context as? Activity
    DisposableEffect(Unit) {
        val window = activity?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = true
        }
        onDispose { }
    }

    // Filter projects for Apps vs Games
    val filteredApps = remember(projects, searchQuery) {
        projects.filter { project ->
            val matchesCategory = project.category.equals("APP", ignoreCase = true)
            val matchesSearch = if (searchQuery.isBlank()) true else {
                project.title.contains(searchQuery, ignoreCase = true) ||
                        project.packageName.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesSearch
        }
    }

    val filteredGames = remember(projects, searchQuery) {
        projects.filter { project ->
            val matchesCategory = project.category.equals("GAME", ignoreCase = true)
            val matchesSearch = if (searchQuery.isBlank()) true else {
                project.title.contains(searchQuery, ignoreCase = true) ||
                        project.packageName.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesSearch
        }
    }

    val appsList = remember(projects) { projects.filter { it.category.equals("APP", ignoreCase = true) } }
    val gamesList = remember(projects) { projects.filter { it.category.equals("GAME", ignoreCase = true) } }

    val currentTab = if (selectedTab.equals("GAME", ignoreCase = true)) "GAME" else "APP"
    val currentItems = if (currentTab == "GAME") filteredGames else filteredApps

    // Pagination: Smoothly trigger next page when user scrolls near the end
    val shouldLoadMore by remember(currentItems, isLoadingNextPage) {
        derivedStateOf {
            val totalItems = currentItems.size
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            !isLoadingNextPage && totalItems > 0 && lastVisibleIndex >= totalItems - 2
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    // Dynamic stats: Only Live in Store & Pending Review, calculated dynamically for active tab
    val currentTabProjects = if (currentTab == "GAME") gamesList else appsList
    val tabTotal = currentTabProjects.size
    val tabLiveCount = remember(currentTabProjects) { currentTabProjects.count { it.status.equals("LIVE", ignoreCase = true) } }
    val tabPendingCount = tabTotal - tabLiveCount
    val tabLivePercentage = if (tabTotal > 0) ((tabLiveCount.toFloat() / tabTotal.toFloat()) * 100).toInt() else 0
    val tabPendingPercentage = if (tabTotal > 0) ((tabPendingCount.toFloat() / tabTotal.toFloat()) * 100).toInt() else 0

    val animatedLiveCount by animateIntAsState(targetValue = tabLiveCount, animationSpec = tween(320), label = "liveCount")
    val animatedPendingCount by animateIntAsState(targetValue = tabPendingCount, animationSpec = tween(320), label = "pendingCount")
    val animatedLivePercentage by animateIntAsState(targetValue = tabLivePercentage, animationSpec = tween(320), label = "livePercentage")
    val animatedPendingPercentage by animateIntAsState(targetValue = tabPendingPercentage, animationSpec = tween(320), label = "pendingPercentage")

    // Dynamic user display name: from Firestore profile, Auth, or formatted email prefix (no hardcoded fallback)
    val displayName = remember(userName, userEmail) {
        if (userName.isNotBlank()) {
            userName
        } else {
            val fbUser = try { FirebaseAuth.getInstance().currentUser } catch (e: Exception) { null }
            val nameFromFb = fbUser?.displayName?.takeIf { it.isNotBlank() }
                ?: fbUser?.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
            val raw = nameFromFb ?: userEmail.substringBefore("@")
            if (raw.isBlank()) {
                "User"
            } else {
                raw.replace(".", " ")
                    .replace("_", " ")
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTrackBg)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ───── 1. STICKY TOP ROYAL BLUE HEADER CONTAINER (32.dp Rounded Corners) ─────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                        clip = false
                    )
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(HeaderRoyalGradient)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(bottom = 20.dp)
                ) {
                    // User Profile Greeting Row + Frosted Logout Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Hi, $displayName!",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp,
                                modifier = Modifier.testTag("header_app_title")
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Welcome Back!",
                                color = Color.White.copy(alpha = 0.82f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        // Frosted Logout Button
                        IconButton(
                            onClick = { showLogoutDialog = true },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(Color.White.copy(alpha = 0.18f))
                                .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(13.dp))
                                .testTag("user_logout_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Logout",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // iOS-Style Segmented Tabs (Applications vs Games)
                    AppTrackIOSSegmentedTabs(
                        selectedTab = currentTab,
                        appsCount = appsList.size,
                        gamesCount = gamesList.size,
                        onTabSelected = onTabSelected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // ───── REFINED FROSTED GLASS STATS CARDS ─────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Card 1: Live in Store (Neon Mint Glass + Sharp Top-Right Check Circle)
                        AppTrackFrostedStatCard(
                            title = "Live in Store",
                            stat = animatedLiveCount.toString(),
                            percentage = animatedLivePercentage,
                            accentColor = Color(0xFF34D399),
                            icon = Icons.Default.CheckCircle,
                            modifier = Modifier.weight(1f)
                        )

                        // Card 2: Pending Review (Warm Amber Glass + Sleek Hourglass Indicator)
                        AppTrackFrostedStatCard(
                            title = "Pending Review",
                            stat = animatedPendingCount.toString(),
                            percentage = animatedPendingPercentage,
                            accentColor = Color(0xFFFBBF24),
                            icon = Icons.Default.HourglassTop,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Frosted Glass Search Bar
                    AppTrackFrostedSearchBar(
                        query = searchQuery,
                        onQueryChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )
                }
            }

            // ───── 2. SCROLLABLE APPLICATION LIST OR CENTERED EMPTY STATE ─────
            if (currentItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 40.dp)
                        .testTag("app_empty_state_box"),
                    contentAlignment = Alignment.Center
                ) {
                    AppTrackEmptyState(
                        isSearchEmpty = searchQuery.isNotBlank(),
                        isGame = currentTab == "GAME",
                        totalInTab = if (currentTab == "GAME") gamesList.size else appsList.size,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("app_list_section"),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp, start = 20.dp, end = 20.dp)
                ) {
                    items(
                        items = currentItems,
                        key = { it.id }
                    ) { project ->
                        Box(modifier = Modifier.padding(vertical = 5.dp)) {
                            AppTrackRow(
                                project = project,
                                isChecking = checkingIds.contains(project.id) || project.status.equals("CHECKING", ignoreCase = true),
                                onOpenStore = {
                                    val cleanPkg = project.packageName.trim()
                                    val url = "https://play.google.com/store/apps/details?id=$cleanPkg"
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Fallback
                                    }
                                },
                                onRetry = { onCheckSingle(project) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    if (isLoadingNextPage) {
                        item(key = "loading_next_page_indicator") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = AppTrackPrimary,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ───── 3. FLOATING ACTION BUTTON (Royal Blue Theme) ─────
        val infiniteTransition = rememberInfiniteTransition(label = "fabSpin")
        val spinAngle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 850, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "fabSpinAngle"
        )

        FloatingActionButton(
            onClick = {
                if (!isRefreshing) {
                    if (!NetworkUtils.isNetworkAvailable(context)) {
                        showOfflineDialog = true
                    } else {
                        // Strictly isolated: refreshes ONLY the active tab!
                        onRefreshTab(currentTab)
                    }
                }
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = AppTrackPrimary,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 10.dp
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(20.dp)
                .size(54.dp)
                .testTag("fab_check_all")
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Check app statuses for current tab",
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .then(if (isRefreshing) Modifier.rotate(spinAngle) else Modifier)
            )
        }

        // ───── 4. LOGOUT CONFIRMATION DIALOG ─────
        if (showLogoutDialog) {
            AppTrackLogoutDialog(
                onDismiss = { showLogoutDialog = false },
                onConfirm = {
                    showLogoutDialog = false
                    onLogout()
                }
            )
        }

        // ───── 5. NETWORK OFFLINE DIALOG ─────
        if (showOfflineDialog) {
            AppTrackOfflineDialog(
                onDismiss = { showOfflineDialog = false },
                onRetrySuccess = {
                    showOfflineDialog = false
                    onRefreshTab(currentTab)
                }
            )
        }
    }
}

/**
 * Refined Frosted Glass Stats Card:
 * Features dual-layer angled glassmorphic shine, glowing progress ring,
 * and a focused, sharp, frosted top-right icon badge.
 * Notice: Badges like "71% Active" are removed for clean bold numbers.
 */
@Composable
fun AppTrackFrostedStatCard(
    title: String,
    stat: String,
    percentage: Int,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(GlassSurfaceBrush)
                .border(
                    width = 1.dp,
                    brush = GlassBorderBrush,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Row: Circular Progress Ring on left + Sharp Focused Icon Badge on right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Mini Glowing Progress Ring
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(36.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { (percentage.coerceIn(0, 100) / 100f) },
                            color = accentColor,
                            trackColor = Color.White.copy(alpha = 0.20f),
                            strokeWidth = 3.2.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "$percentage%",
                            color = Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Sharp, Focused Icon Badge (Frosted glass circle with vivid accent icon)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f))
                            .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title Label
                Text(
                    text = title,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Bold, Clean Stat Number (No cluttering extra badges)
                Text(
                    text = stat,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
            }
        }
    }
}

/**
 * iOS-Style Segmented Tab Switcher with Apple Physics Spring
 */
@Composable
fun AppTrackIOSSegmentedTabs(
    selectedTab: String,
    appsCount: Int,
    gamesCount: Int,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isGame = selectedTab.equals("GAME", ignoreCase = true)

    val springFraction by animateFloatAsState(
        targetValue = if (isGame) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.78f,
            stiffness = 380f
        ),
        label = "iosSpringTab"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Black.copy(alpha = 0.22f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
        modifier = modifier.height(48.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.5.dp)
        ) {
            val halfWidth = maxWidth / 2f
            val pillOffset = halfWidth * springFraction

            // Sliding White Card Indicator
            Surface(
                shape = RoundedCornerShape(13.dp),
                color = Color.White,
                shadowElevation = 3.dp,
                modifier = Modifier
                    .offset(x = pillOffset)
                    .width(halfWidth)
                    .fillMaxHeight()
            ) {}

            // Clickable Tab Labels
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Applications
                val appTextColor by animateColorAsState(
                    targetValue = if (!isGame) AppTrackPrimary else Color.White.copy(alpha = 0.85f),
                    animationSpec = tween(180),
                    label = "appTextColor"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onTabSelected("APP")
                        }
                        .testTag("tab_APP"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = appTextColor,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Applications",
                            color = appTextColor,
                            fontSize = 13.5.sp,
                            fontWeight = if (!isGame) FontWeight.Bold else FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (!isGame) AppTrackPrimary.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = appsCount.toString(),
                                color = if (!isGame) AppTrackPrimary else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                // Tab 2: Games
                val gameTextColor by animateColorAsState(
                    targetValue = if (isGame) AppTrackPrimary else Color.White.copy(alpha = 0.85f),
                    animationSpec = tween(180),
                    label = "gameTextColor"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onTabSelected("GAME")
                        }
                        .testTag("tab_GAME"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = gameTextColor,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Games",
                            color = gameTextColor,
                            fontSize = 13.5.sp,
                            fontWeight = if (isGame) FontWeight.Bold else FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isGame) AppTrackPrimary.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = gamesCount.toString(),
                                color = if (isGame) AppTrackPrimary else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Frosted Glass Search Bar with clear button
 */
@Composable
fun AppTrackFrostedSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.14f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
        modifier = modifier.height(46.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White.copy(alpha = 0.75f),
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search for apps or games…",
                        color = Color.White.copy(alpha = 0.60f),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_text_field")
                )
            }

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Modern Apple App Store inspired card with soft floating depth, 20.dp rounded corners,
 * squircle icons, glowing live dot, and capsule "View ↗" action.
 * Package name omitted per design specification.
 */
@Composable
fun AppTrackRow(
    project: ProjectEntity,
    isChecking: Boolean,
    onOpenStore: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLive = project.status.equals("LIVE", ignoreCase = true)
    val isUnableToCheck = project.status.equals("UNABLE_TO_CHECK", ignoreCase = true)
    val isNotFound = project.status.equals("NOT_FOUND", ignoreCase = true)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("app_row_${project.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Squircle App Icon Box
            AppTrackIconBox(
                iconUrl = project.iconUrl,
                title = project.title,
                category = project.category,
                packageName = project.packageName
            )

            Spacer(modifier = Modifier.width(13.dp))

            // Title & Status Badge (Package name omitted for ultra clean UI)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = project.title,
                    color = AppTrackTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("app_title_${project.id}")
                )

                Spacer(modifier = Modifier.height(5.dp))

                AppTrackStatusBadge(
                    status = project.status,
                    isLive = isLive,
                    isChecking = isChecking,
                    isUnableToCheck = isUnableToCheck,
                    isNotFound = isNotFound
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Action Button: Capsule "View ↗"
            if (isLive) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFEEF2FF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC7D2FE)),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onOpenStore() }
                        .testTag("open_store_btn_${project.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "View",
                            color = AppTrackPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open in Google Play",
                            tint = AppTrackPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            } else if (isUnableToCheck) {
                Surface(
                    shape = CircleShape,
                    color = StatusUnableBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusUnableBorder),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onRetry() }
                        .testTag("retry_btn_${project.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Retry",
                            color = StatusUnableRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry check",
                            tint = StatusUnableRed,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 46dp Squircle App Icon Box with CDN original brand icon resolvers & Firebase public link support
 */
@Composable
fun AppTrackIconBox(
    iconUrl: String,
    title: String,
    category: String = "APP",
    packageName: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cleanTitle = title.trim()
    val isGame = category.equals("GAME", ignoreCase = true)

    // Strictly use ONLY the iconUrl provided in Firebase. Zero hardcoding.
    val effectiveUrl = remember(iconUrl) { iconUrl.trim() }

    var isImageLoadedSuccessfully by remember(effectiveUrl) { mutableStateOf(false) }

    val fallbackBgColor = if (isGame) Color(0xFF6D28D9) else AppTrackPrimary
    val fallbackIcon = if (isGame) Icons.Default.SportsEsports else Icons.Default.Apps

    Surface(
        shape = RoundedCornerShape(13.dp),
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x14000000)),
        modifier = modifier.size(46.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(13.dp))
                .background(if (isImageLoadedSuccessfully) Color.White else fallbackBgColor),
            contentAlignment = Alignment.Center
        ) {
            if (!isImageLoadedSuccessfully) {
                Icon(
                    imageVector = fallbackIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (effectiveUrl.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(effectiveUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "$title Icon",
                    contentScale = ContentScale.Crop,
                    onSuccess = { isImageLoadedSuccessfully = true },
                    onError = { isImageLoadedSuccessfully = false },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * Status Badge with glowing pulse indicator
 */
@Composable
fun AppTrackStatusBadge(
    status: String,
    isLive: Boolean,
    isChecking: Boolean,
    isUnableToCheck: Boolean,
    isNotFound: Boolean
) {
    val (badgeBg, badgeBorder, badgeText, badgeColor) = when {
        isChecking -> Quadruple(
            Color(0xFFEFF6FF),
            Color(0xFFBFDBFE),
            "Checking…",
            Color(0xFF3B82F6)
        )
        isLive -> Quadruple(
            StatusLiveGreenBg,
            StatusLiveGreenBorder,
            "Live on Play Store",
            StatusLiveGreen
        )
        isUnableToCheck -> Quadruple(
            StatusUnableBg,
            StatusUnableBorder,
            "Unable to Check",
            StatusUnableRed
        )
        isNotFound -> Quadruple(
            StatusPendingAmberBg,
            StatusPendingAmberBorder,
            "NOT FOUND",
            StatusPendingAmber
        )
        else -> Quadruple(
            Color(0xFFF1F5F9),
            Color(0xFFE2E8F0),
            status.replace("_", " ").uppercase(),
            Color(0xFF64748B)
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = badgeBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, badgeBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = badgeText,
                color = badgeColor,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Empty State
 */
@Composable
fun AppTrackEmptyState(
    isSearchEmpty: Boolean,
    isGame: Boolean,
    totalInTab: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(if (isGame && !isSearchEmpty) Color(0xFFF3E8FF) else Color(0xFFEEF2FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when {
                    isSearchEmpty -> Icons.Default.Search
                    isGame -> Icons.Default.SportsEsports
                    else -> Icons.Default.Apps
                },
                contentDescription = null,
                tint = if (isGame && !isSearchEmpty) Color(0xFF8B5CF6) else AppTrackPrimary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = if (isSearchEmpty) "No Results Found" else if (isGame) "No Games Added" else "No Applications Added",
            color = AppTrackTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (isSearchEmpty) "Try searching with a different keyword" else "Sync with Firebase to view tracked projects",
            color = AppTrackTextMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Concise Logout Dialog
 */
@Composable
fun AppTrackLogoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Log Out",
                    color = AppTrackTextPrimary,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Are you sure you want to sign out?",
                    color = AppTrackTextMuted,
                    fontSize = 13.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF475569)),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(
                            text = "Log Out",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Offline Dialog
 */
@Composable
fun AppTrackOfflineDialog(
    onDismiss: () -> Unit,
    onRetrySuccess: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            shadowElevation = 14.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No Internet Connection",
                    color = AppTrackTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please check your network connection and try again.",
                    color = AppTrackTextMuted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppTrackPrimary),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
