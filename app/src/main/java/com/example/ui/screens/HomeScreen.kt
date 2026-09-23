package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.ProjectEntity
import kotlinx.coroutines.launch

// AppTrack Visual Design Tokens
private val AppTrackBg = Color(0xFFF8FAFC)
private val AppTrackCardBg = Color(0xFFFFFFFF)
private val AppTrackBorder = Color(0xFFE2E8F0)
private val AppTrackBorderSubtle = Color(0xFFF1F5F9)
private val AppTrackPrimary = Color(0xFF2563EB)
private val AppTrackPrimaryDark = Color(0xFF1D4ED8)
private val AppTrackPrimaryLight = Color(0xFFEFF6FF)
private val AppTrackTextPrimary = Color(0xFF0F172A)
private val AppTrackTextMuted = Color(0xFF64748B)
private val AppTrackTextLight = Color(0xFF94A3B8)

// Status Tokens
private val StatusLiveGreen = Color(0xFF16A34A)
private val StatusLiveGreenBg = Color(0xFFDCFCE7)
private val StatusNotFoundAmber = Color(0xFFD97706)
private val StatusNotFoundBg = Color(0xFFFEF3C7)
private val StatusUnableRed = Color(0xFFDC2626)
private val StatusUnableBg = Color(0xFFFEE2E2)
private val StatusCheckingBlue = Color(0xFF2563EB)
private val StatusCheckingBg = Color(0xFFEFF6FF)

/**
 * AppTrack Main Dashboard Screen.
 *
 * Exact hierarchy:
 * 1. App bar (Title: AppTrack, Tagline: Track all your apps in one place., compact, logout/account button)
 * 2. Apps / Games tabs (Smooth animated sliding indicator)
 * 3. Search bar (Search apps... or Search games...)
 * 4. Summary cards (Compact 2x2 grid: Total Apps/Games, Live, In Development, Pending)
 * 5. App/Game list header ("Your Apps" or "Your Games")
 * 6. App/Game list (Clean horizontal rows with icon, name, live status, row retry button)
 * 7. Floating refresh/check-all button (bottom-right ↻)
 */
@OptIn(ExperimentalFoundationApi::class)
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
    showAddDialog: Boolean,
    onTabSelected: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onRefreshAll: () -> Unit,
    onCheckSingle: (ProjectEntity) -> Unit,
    onShowAddDialog: (Boolean) -> Unit,
    onAddNewProject: (title: String, packageName: String, category: String, iconUrl: String) -> Unit,
    onDismissFeedback: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    // Two tabs: 0 for "APP", 1 for "GAME"
    val pagerState = rememberPagerState(
        initialPage = if (selectedTab.equals("GAME", ignoreCase = true)) 1 else 0,
        pageCount = { 2 }
    )

    // Synchronize tab clicks with Pager smoothly
    LaunchedEffect(selectedTab) {
        val targetPage = if (selectedTab.equals("GAME", ignoreCase = true)) 1 else 0
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    // Synchronize swiping gesture back to ViewModel tab state
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            val tabKey = if (page == 1) "GAME" else "APP"
            if (selectedTab != tabKey) {
                onTabSelected(tabKey)
            }
        }
    }

    // Filter projects for Apps vs Games
    val filteredApps = remember(projects, searchQuery) {
        projects.filter { project ->
            val matchesCategory = project.category.equals("APP", ignoreCase = true)
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                project.title.contains(searchQuery, ignoreCase = true) ||
                        project.packageName.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesSearch
        }
    }

    val filteredGames = remember(projects, searchQuery) {
        projects.filter { project ->
            val matchesCategory = project.category.equals("GAME", ignoreCase = true)
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                project.title.contains(searchQuery, ignoreCase = true) ||
                        project.packageName.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesSearch
        }
    }

    val appsList = remember(projects) { projects.filter { it.category.equals("APP", ignoreCase = true) } }
    val gamesList = remember(projects) { projects.filter { it.category.equals("GAME", ignoreCase = true) } }

    val currentTab = if (selectedTab.equals("GAME", ignoreCase = true)) "GAME" else "APP"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTrackBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // 1. App Bar Section (Clean Client-Side Top Bar)
            AppTrackTopBar(
                userEmail = userEmail,
                onLogout = onLogout
            )

            // 2. Control & Search Section (Unified Tabs & Search Bar Card, clearly distinct from background)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppTrackCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppTrackBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("controls_search_section")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Apps / Games Tabs
                    AppTrackTabs(
                        selectedTab = currentTab,
                        appsCount = appsList.size,
                        gamesCount = gamesList.size,
                        onTabSelected = { tab ->
                            onTabSelected(tab)
                            coroutineScope.launch {
                                val targetPage = if (tab.equals("GAME", ignoreCase = true)) 1 else 0
                                pagerState.animateScrollToPage(targetPage)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Search Bar (Distinct from Card Surface, zero stroke when keyboard active)
                    AppTrackSearchBar(
                        query = searchQuery,
                        isGame = currentTab == "GAME",
                        onQueryChange = onSearchQueryChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Live Checking Progress Banner (when multi-threading check is active)
            AnimatedVisibility(
                visible = isRefreshing,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                AppTrackCheckingProgress(
                    completed = checkingProgress.first,
                    total = checkingProgress.second,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // 3. App / Game List Section with Smooth HorizontalPager Swiping
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("app_list_section")
            ) { page ->
                val isGamePage = page == 1
                val itemsList = if (isGamePage) filteredGames else filteredApps
                val totalInTab = if (isGamePage) gamesList.size else appsList.size

                AppTrackListContent(
                    isGame = isGamePage,
                    items = itemsList,
                    totalInTab = totalInTab,
                    searchQuery = searchQuery,
                    onRetrySingle = onCheckSingle,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 6. Floating Status Feedback Toast (Consistent Google-style bottom notification pill)
        AnimatedVisibility(
            visible = !isRefreshing && feedbackMessage != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 84.dp, start = 20.dp, end = 20.dp)
        ) {
            if (feedbackMessage != null) {
                AppTrackStatusToast(
                    message = feedbackMessage,
                    onDismiss = onDismissFeedback
                )
            }
        }

        // 7. Modern Compact Floating Action Button (Check All)
        FloatingActionButton(
            onClick = {
                if (!isRefreshing) {
                    onRefreshAll()
                }
            },
            shape = CircleShape,
            containerColor = AppTrackPrimary,
            contentColor = Color.White,
            elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 20.dp)
                .size(54.dp)
                .testTag("fab_check_all")
        ) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Check all app statuses",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Add App Dialog (Allows user to track their apps/games)
        if (showAddDialog) {
            AddAppDialog(
                defaultCategory = currentTab,
                onDismiss = { onShowAddDialog(false) },
                onConfirm = { title, pkg, cat, iconUrl ->
                    onAddNewProject(title, pkg, cat, iconUrl)
                    onShowAddDialog(false)
                }
            )
        }
    }
}

/**
 * 1. App Bar (Client-Side Header):
 * - AppTrack Logo + Title: AppTrack
 * - Tagline: Track all your apps in one place.
 * - Logout / Account action
 */
@Composable
fun AppTrackTopBar(
    userEmail: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = AppTrackCardBg,
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand: Prominent Full-Size App Logo + Title + Tagline (No awkward surrounding box/padding)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_apptrack_logo),
                    contentDescription = "AppTrack Logo",
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("header_apptrack_logo")
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "AppTrack",
                        color = AppTrackTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                        modifier = Modifier.testTag("header_app_title")
                    )

                    Text(
                        text = "Track all your apps in one place.",
                        color = AppTrackTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // Actions: User Logout
            IconButton(
                onClick = onLogout,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9))
                    .testTag("user_logout_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Logout",
                    tint = AppTrackTextMuted,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

/**
 * 2. Apps / Games Tabs:
 * - Professional Google-style segmented blue pill indicator
 * - Clear counts and sharp icons for each
 */
@Composable
fun AppTrackTabs(
    selectedTab: String,
    appsCount: Int,
    gamesCount: Int,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF1F5F9),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier.height(44.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                Triple("APP", "Apps", appsCount),
                Triple("GAME", "Games", gamesCount)
            )

            tabs.forEach { (key, title, count) ->
                val isSelected = selectedTab.equals(key, ignoreCase = true)

                val pillBg by animateColorAsState(
                    targetValue = if (isSelected) AppTrackPrimary else Color.Transparent,
                    animationSpec = tween(220),
                    label = "tabPillBg"
                )

                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else Color(0xFF64748B),
                    animationSpec = tween(220),
                    label = "tabContentColor"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(9.dp))
                        .background(pillBg)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onTabSelected(key)
                        }
                        .testTag("tab_$key"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (key == "APP") Icons.Default.Apps else Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = title,
                            color = contentColor,
                            fontSize = 13.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) Color.White.copy(alpha = 0.22f) else Color(0xFFE2E8F0)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = count.toString(),
                                color = if (isSelected) Color.White else Color(0xFF475569),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3. Search Bar:
 * - Visually distinct from card surface (styled just like the login form input fields).
 * - Completely stroke-free: No outline/stroke appears when focused or when keyboard is active.
 * - Placeholder dynamically matches category: "Search apps…" or "Search games…"
 */
@Composable
fun AppTrackSearchBar(
    query: String,
    isGame: Boolean,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val placeholderText = if (isGame) "Search games…" else "Search apps…"

    Surface(
        modifier = modifier
            .height(46.dp)
            .testTag("search_bar_input"),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF1F5F9), // Clearly distinct from the white section card, same as login form
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 0.dp
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
                tint = AppTrackTextMuted,
                modifier = Modifier.size(19.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholderText,
                        color = AppTrackTextLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = AppTrackTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(AppTrackPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear Search",
                        tint = AppTrackTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Animated Banner showing parallel checking progress.
 */
@Composable
fun AppTrackCheckingProgress(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = AppTrackPrimaryLight),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        color = AppTrackPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Checking store status in parallel...",
                        color = AppTrackPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = if (total > 0) "$completed / $total" else "Starting...",
                    color = AppTrackPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { if (total > 0) completed.toFloat() / total.toFloat() else 0f },
                color = AppTrackPrimary,
                trackColor = Color(0xFFDBEAFE),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
            )
        }
    }
}

/**
 * Professional Google-style bottom Floating Status Toast.
 * High-contrast, non-disruptive, auto-dismissing pill that does not push list content.
 */
@Composable
fun AppTrackStatusToast(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF1E293B), // Google-style Slate 800
        shadowElevation = 6.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4ADE80), // Vibrant emerald
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = message,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/**
 * Professional Vertical Scrollbar for App and Game lists.
 * Features a sleek subtle track and a vibrant AppTrack Primary Blue indicator pill
 * that responds dynamically during scrolling and remains visible with a clean resting opacity.
 */
@Composable
fun AppTrackVerticalScrollbar(
    listState: LazyListState,
    totalItems: Int,
    modifier: Modifier = Modifier
) {
    if (totalItems <= 1) return

    val isScrollable by remember(totalItems) {
        derivedStateOf {
            val visibleCount = listState.layoutInfo.visibleItemsInfo.size
            visibleCount < totalItems || listState.firstVisibleItemScrollOffset > 0 || listState.firstVisibleItemIndex > 0
        }
    }

    if (!isScrollable) return

    val isScrolling = listState.isScrollInProgress
    val thumbAlpha by animateFloatAsState(
        targetValue = if (isScrolling) 1.0f else 0.65f,
        animationSpec = tween(durationMillis = 200),
        label = "thumbAlpha"
    )

    BoxWithConstraints(
        modifier = modifier.width(6.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        val fullHeight = maxHeight
        val visibleItems = listState.layoutInfo.visibleItemsInfo
        val visibleCount = visibleItems.size

        val thumbRatio = if (totalItems > 0) (visibleCount.toFloat() / totalItems).coerceIn(0.15f, 0.75f) else 0.35f
        val thumbHeight = (fullHeight * thumbRatio).coerceAtLeast(36.dp)

        val progress by remember(totalItems) {
            derivedStateOf {
                val layoutInfo = listState.layoutInfo
                val vItems = layoutInfo.visibleItemsInfo
                if (vItems.isEmpty() || totalItems <= vItems.size) {
                    0f
                } else {
                    val first = vItems.first()
                    val maxFirstIndex = (totalItems - vItems.size).coerceAtLeast(1)
                    val indexProgress = first.index.toFloat() / maxFirstIndex
                    val itemOffsetRatio = if (first.size > 0) {
                        listState.firstVisibleItemScrollOffset.toFloat() / (first.size * maxFirstIndex)
                    } else 0f
                    (indexProgress + itemOffsetRatio).coerceIn(0f, 1f)
                }
            }
        }

        val maxOffset = fullHeight - thumbHeight
        val thumbOffset = maxOffset * progress

        // Track (Subtle Slate Track)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(3.dp)
                .clip(CircleShape)
                .background(Color(0xFFE2E8F0).copy(alpha = 0.55f))
        )

        // Thumb Pill (Modern AppTrack Royal-Cobalt Pill)
        Box(
            modifier = Modifier
                .offset(y = thumbOffset)
                .width(5.dp)
                .height(thumbHeight)
                .clip(CircleShape)
                .background(
                    color = Color(0xFF2563EB).copy(alpha = thumbAlpha)
                )
        )
    }
}

/**
 * 5 & 6. App / Game List Content:
 * - Header: "Your Apps" or "Your Games"
 * - Compact horizontal rows:
 *   [Icon] App Name       Status + Retry (if Unable to Check)
 * - Clean empty states if no items or search with no matches
 * - Smooth vertical scrollbar in professional brand color
 */
@Composable
fun AppTrackListContent(
    isGame: Boolean,
    items: List<ProjectEntity>,
    totalInTab: Int,
    searchQuery: String,
    onRetrySingle: (ProjectEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    Box(modifier = modifier) {
        if (items.isEmpty()) {
            // Empty State
            AppTrackEmptyState(
                isSearchEmpty = searchQuery.isNotBlank(),
                isGame = isGame,
                totalInTab = totalInTab,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.id }) { project ->
                    AppTrackRow(
                        project = project,
                        isGame = isGame,
                        onRetry = { onRetrySingle(project) },
                        onOpenStore = {
                            val pkg = project.packageName.trim()
                            val webUrl = project.liveStoreUrl.ifBlank {
                                "https://play.google.com/store/apps/details?id=$pkg"
                            }
                            // Professional Play Store intent launcher:
                            // Try native Play Store market:// protocol first, seamlessly falling back to https:// in browser
                            try {
                                val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                                    setPackage("com.android.vending")
                                }
                                context.startActivity(marketIntent)
                            } catch (e: Exception) {
                                try {
                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(browserIntent)
                                } catch (e2: Exception) {
                                    Toast.makeText(context, "Unable to open Play Store", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }

            // Professional Vertical Scrollbar on right edge
            AppTrackVerticalScrollbar(
                listState = listState,
                totalItems = items.size,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(top = 10.dp, bottom = 92.dp, end = 4.dp)
                    .fillMaxHeight()
            )
        }
    }
}

/**
 * Compact horizontal row for an App or Game:
 * [Icon]   App Name                 Status Badge (+ Retry if Unable to Check)
 *
 * Requirements:
 * - Do NOT show package name in main row.
 * - Do NOT show Firebase document IDs or technical info.
 * - Status states:
 *     - Checking…
 *     - ● Live
 *     - ● Not Found
 *     - ● Unable to Check (with small subtle retry button beside it)
 */
@Composable
fun AppTrackRow(
    project: ProjectEntity,
    isGame: Boolean,
    onRetry: () -> Unit,
    onOpenStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusUpper = project.status.uppercase()
    val isLive = statusUpper == "LIVE"
    val isChecking = statusUpper == "CHECKING"
    val isUnableToCheck = statusUpper == "UNABLE TO CHECK" || statusUpper == "OFFLINE"
    val isNotFound = statusUpper == "NOT FOUND" || statusUpper == "NOT LIVE"

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppTrackCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppTrackBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                if (isLive) onOpenStore()
            }
            .testTag("app_row_${project.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: App Icon (38dp squircle, neat, high-quality and well-proportioned)
            AppTrackIconBox(
                iconUrl = project.iconUrl,
                title = project.title,
                category = project.category
            )

            Spacer(modifier = Modifier.width(12.dp))

            // CENTER COLUMN: Title on top, Status Badge below (No overlap, Google Play Console layout)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = project.title,
                    color = AppTrackTextPrimary,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("app_title_${project.id}")
                )

                Spacer(modifier = Modifier.height(3.dp))

                AppTrackStatusBadge(
                    status = project.status,
                    isLive = isLive,
                    isChecking = isChecking,
                    isUnableToCheck = isUnableToCheck,
                    isNotFound = isNotFound
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // RIGHT ACTION BUTTON: Dedicated button, completely separated with zero overlap
            if (isLive) {
                IconButton(
                    onClick = onOpenStore,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEFF6FF))
                        .testTag("open_store_btn_${project.id}")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Open in Google Play",
                        tint = AppTrackPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            } else if (isUnableToCheck) {
                IconButton(
                    onClick = onRetry,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(StatusUnableBg)
                        .testTag("retry_btn_${project.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry check for ${project.title}",
                        tint = StatusUnableRed,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

/**
 * App Icon with Coil and rich Google-style brand & category default icons.
 * Sized proportionally (38dp) for a neat, professional dashboard density.
 */
@Composable
fun AppTrackIconBox(
    iconUrl: String,
    title: String,
    category: String = "APP",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cleanTitle = title.trim().lowercase()

    // Determine tailored Google-style fallback icon & colors for famous apps & games
    val (fallbackBgColor, fallbackIcon, fallbackText) = remember(cleanTitle, category) {
        when {
            cleanTitle.contains("spotify") -> Triple(Color(0xFF1DB954), Icons.Default.MusicNote, null)
            cleanTitle.contains("youtube") -> Triple(Color(0xFFFF0000), Icons.Default.PlayArrow, null)
            cleanTitle.contains("whatsapp") -> Triple(Color(0xFF25D366), Icons.AutoMirrored.Filled.Chat, null)
            cleanTitle.contains("duolingo") -> Triple(Color(0xFF58CC02), Icons.Default.School, null)
            cleanTitle.contains("reddit") -> Triple(Color(0xFFFF4500), Icons.Default.Forum, null)
            cleanTitle.contains("nova") || cleanTitle.contains("wallet") -> Triple(Color(0xFF0284C7), Icons.Default.AccountBalanceWallet, null)
            cleanTitle.contains("clash") || cleanTitle.contains("subway") || cleanTitle.contains("candy") || cleanTitle.contains("roblox") || category.equals("GAME", ignoreCase = true) -> {
                val gameColor = when {
                    cleanTitle.contains("clash") -> Color(0xFFF59E0B)
                    cleanTitle.contains("candy") -> Color(0xFFEC4899)
                    cleanTitle.contains("subway") -> Color(0xFF3B82F6)
                    cleanTitle.contains("roblox") -> Color(0xFF10B981)
                    else -> Color(0xFF8B5CF6)
                }
                Triple(gameColor, Icons.Default.SportsEsports, null)
            }
            else -> {
                // High-quality letter avatar with rich Google-style gradient/color
                val hash = kotlin.math.abs(cleanTitle.hashCode())
                val palette = listOf(
                    Color(0xFF3B82F6), Color(0xFF6366F1), Color(0xFF8B5CF6),
                    Color(0xFFEC4899), Color(0xFF10B981), Color(0xFFF59E0B),
                    Color(0xFF06B6D4), Color(0xFF0284C7)
                )
                val color = palette[hash % palette.size]
                val letter = title.firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString() ?: "A"
                Triple(color, null, letter)
            }
        }
    }

    Box(
        modifier = modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(fallbackBgColor),
        contentAlignment = Alignment.Center
    ) {
        if (iconUrl.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(iconUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "$title icon",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp)),
                error = null
            )
        }

        // If no iconUrl or while loading, display tailored default icon/letter
        if (iconUrl.isBlank()) {
            if (fallbackIcon != null) {
                Icon(
                    imageVector = fallbackIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else if (fallbackText != null) {
                Text(
                    text = fallbackText,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Status Badge implementing the 4 exact states:
 * - Checking…
 * - ● Live
 * - ● Not Found
 * - ● Unable to Check
 */
@Composable
fun AppTrackStatusBadge(
    status: String,
    isLive: Boolean,
    isChecking: Boolean,
    isUnableToCheck: Boolean,
    isNotFound: Boolean,
    modifier: Modifier = Modifier
) {
    when {
        isChecking -> {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = StatusCheckingBg,
                modifier = modifier
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = StatusCheckingBlue,
                        strokeWidth = 1.8.dp,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Checking…",
                        color = StatusCheckingBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        isLive -> {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = StatusLiveGreenBg,
                modifier = modifier
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(StatusLiveGreen)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Live",
                        color = StatusLiveGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        isUnableToCheck -> {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = StatusUnableBg,
                modifier = modifier
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(StatusUnableRed)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Unable to Check",
                        color = StatusUnableRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        isNotFound -> {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = StatusNotFoundBg,
                modifier = modifier
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(StatusNotFoundAmber)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Not Found",
                        color = StatusNotFoundAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        else -> {
            // Other states like Development / Pending
            val display = when {
                status.contains("DEV", ignoreCase = true) -> "In Dev"
                status.contains("PEND", ignoreCase = true) -> "Pending"
                else -> status
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF1F5F9),
                modifier = modifier
            ) {
                Text(
                    text = display,
                    color = AppTrackTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Empty States for:
 * 1. Search returns no matches
 * 2. No apps yet in the selected category
 */
@Composable
fun AppTrackEmptyState(
    isSearchEmpty: Boolean,
    isGame: Boolean,
    totalInTab: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(AppTrackPrimaryLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSearchEmpty) Icons.Default.Search
                    else if (isGame) Icons.Default.SportsEsports else Icons.Default.Apps,
                    contentDescription = null,
                    tint = AppTrackPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isSearchEmpty) {
                    if (isGame) "No games found" else "No apps found"
                } else {
                    if (isGame) "No games yet" else "No apps yet"
                },
                color = AppTrackTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isSearchEmpty) {
                    "Try a different search."
                } else {
                    val label = if (isGame) "Games" else "Apps"
                    "$label added to your AppTrack account will appear here."
                },
                color = AppTrackTextMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Track New Application Dialog:
 * Allows user to add an app or game to track on their dashboard.
 */
@Composable
fun AddAppDialog(
    defaultCategory: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, packageName: String, category: String, iconUrl: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(defaultCategory) }
    var iconUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Application",
                color = AppTrackTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("App Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppTrackPrimary,
                        unfocusedBorderColor = AppTrackBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it.trim() },
                    label = { Text("Package Name (e.g. com.example.app)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppTrackPrimary,
                        unfocusedBorderColor = AppTrackBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = iconUrl,
                    onValueChange = { iconUrl = it.trim() },
                    label = { Text("Icon URL (Optional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppTrackPrimary,
                        unfocusedBorderColor = AppTrackBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Category:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { category = "APP" }
                    ) {
                        RadioButton(
                            selected = category == "APP",
                            onClick = { category = "APP" },
                            colors = RadioButtonDefaults.colors(selectedColor = AppTrackPrimary)
                        )
                        Text("App", fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { category = "GAME" }
                    ) {
                        RadioButton(
                            selected = category == "GAME",
                            onClick = { category = "GAME" },
                            colors = RadioButtonDefaults.colors(selectedColor = AppTrackPrimary)
                        )
                        Text("Game", fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && packageName.isNotBlank()) {
                        onConfirm(title.trim(), packageName.trim(), category, iconUrl.trim())
                    }
                },
                enabled = title.isNotBlank() && packageName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AppTrackPrimary)
            ) {
                Text("Add to AppTrack")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AppTrackTextMuted)
            }
        }
    )
}
