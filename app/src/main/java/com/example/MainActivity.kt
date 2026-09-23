package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.repository.AppRepository
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.components.DiscordTopBar
import com.example.ui.components.NotificationCenterDialog
import com.example.ui.screens.ClientPortalScreen
import com.example.ui.screens.CommunityChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.theme.AppTrackTheme
import com.example.util.NotificationHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.initNotificationChannel(this)

        val database = AppDatabase.getInstance(applicationContext)
        val repository = AppRepository(database)
        val viewModelFactory = MainViewModelFactory(repository)

        setContent {
            AppTrackTheme {
                AppTrackApp(viewModelFactory)
            }
        }
    }
}

@Composable
fun AppTrackApp(
    viewModelFactory: MainViewModelFactory,
    viewModel: MainViewModel = viewModel(factory = viewModelFactory)
) {
    val context = LocalContext.current

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val userSession by viewModel.userSession.collectAsStateWithLifecycle()
    val contracts by viewModel.contracts.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val channels by viewModel.channels.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadNotifs by viewModel.unreadNotifCount.collectAsStateWithLifecycle()

    val selectedContractId by viewModel.selectedContractId.collectAsStateWithLifecycle()
    val selectedCategoryTab by viewModel.categoryTab.collectAsStateWithLifecycle()
    val selectedChannelId by viewModel.selectedChannelId.collectAsStateWithLifecycle()
    val channelMessages by viewModel.channelMessages.collectAsStateWithLifecycle()

    val showNotifCenter by viewModel.showNotificationCenter.collectAsStateWithLifecycle()

    val homeTab by viewModel.homeSelectedTab.collectAsStateWithLifecycle()
    val homeSearchQuery by viewModel.homeSearchQuery.collectAsStateWithLifecycle()
    val isRefreshingStatus by viewModel.isRefreshingStatus.collectAsStateWithLifecycle()
    val checkingProgress by viewModel.checkingProgress.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.refreshFeedbackMessage.collectAsStateWithLifecycle()

    val activeContract = contracts.find { it.id == selectedContractId } ?: contracts.firstOrNull()

    val isLoggedIn = currentScreen != AppScreen.Initializing && currentScreen != AppScreen.Login

    Scaffold(
        containerColor = Color.White,
        topBar = {
            if (isLoggedIn && currentScreen != AppScreen.Home) {
                DiscordTopBar(
                    title = "AppTrack",
                    subtitle = "${activeContract?.clientName ?: userSession?.email?.substringBefore("@") ?: "Client"} • ${activeContract?.companyName ?: "Live Hub"}",
                    currentRole = "CLIENT",
                    unreadNotifs = unreadNotifs,
                    onRoleClick = { /* client-only, no role switch */ },
                    onNotificationClick = { viewModel.setNotificationCenter(true) }
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                is AppScreen.Initializing -> {
                    // Blank white screen — session resolving, no flash
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                    )
                }

                is AppScreen.Login -> {
                    LoginScreen(
                        contracts = contracts,
                        onLogin = { email, role, contractId ->
                            viewModel.login(email, role, contractId)
                        }
                    )
                }

                is AppScreen.Home -> {
                    HomeScreen(
                        userEmail = userSession?.email ?: "",
                        userRole = "CLIENT",
                        projects = projects,
                        selectedTab = homeTab,
                        searchQuery = homeSearchQuery,
                        isRefreshing = isRefreshingStatus,
                        checkingProgress = checkingProgress,
                        feedbackMessage = feedbackMessage,
                        showAddDialog = false,
                        onTabSelected = { viewModel.setHomeTab(it) },
                        onSearchQueryChange = { viewModel.setHomeSearchQuery(it) },
                        onRefreshAll = { viewModel.refreshAllAppsStatus(context) },
                        onCheckSingle = { viewModel.checkSingleApp(it, context) },
                        onShowAddDialog = { /* client cannot add apps */ },
                        onAddNewProject = { _, _, _, _ -> /* not available */ },
                        onDismissFeedback = { viewModel.dismissFeedbackMessage() },
                        onLogout = { viewModel.logout() }
                    )
                }

                is AppScreen.ClientPortal -> {
                    ClientPortalScreen(
                        contract = activeContract,
                        projects = projects,
                        selectedCategoryTab = selectedCategoryTab,
                        onSelectCategoryTab = { viewModel.setCategoryTab(it) },
                        onOpenCommunityHub = { viewModel.navigateTo(AppScreen.CommunityHub) }
                    )
                }

                is AppScreen.CommunityHub -> {
                    CommunityChatScreen(
                        channels = channels,
                        selectedChannelId = selectedChannelId,
                        messages = channelMessages,
                        currentRole = "CLIENT",
                        onSelectChannel = { viewModel.selectChannel(it) },
                        onSendMessage = { viewModel.sendChatMessage(it) },
                        onBackClick = { viewModel.navigateTo(AppScreen.ClientPortal) }
                    )
                }
            }
        }
    }

    // Notification Center Modal
    if (showNotifCenter) {
        NotificationCenterDialog(
            notifications = notifications,
            onDismiss = { viewModel.setNotificationCenter(false) },
            onMarkAllRead = { viewModel.markAllNotificationsRead() },
            onNotificationClick = { notif ->
                viewModel.markNotificationRead(notif.id)
            }
        )
    }
}
