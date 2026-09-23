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
import com.example.ui.components.AddContractModal
import com.example.ui.components.AddProjectModal
import com.example.ui.components.CelebrationModal
import com.example.ui.components.DiscordTopBar
import com.example.ui.components.LiveTriggerDialog
import com.example.ui.components.NotificationCenterDialog
import com.example.ui.components.RoleSwitcherModal
import com.example.ui.screens.ClientPortalScreen
import com.example.ui.screens.CommunityChatScreen
import com.example.ui.screens.DeveloperDashboardScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SplashScreen
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

    // Dialog & Modal States
    val showAddContract by viewModel.showAddContractDialog.collectAsStateWithLifecycle()
    val showAddProject by viewModel.showAddProjectDialog.collectAsStateWithLifecycle()
    val activeLiveProject by viewModel.activeLiveTriggerProject.collectAsStateWithLifecycle()
    val celebrationData by viewModel.celebrationEvent.collectAsStateWithLifecycle()
    val showNotifCenter by viewModel.showNotificationCenter.collectAsStateWithLifecycle()
    val showRoleSwitcher by viewModel.showRoleSwitcher.collectAsStateWithLifecycle()

    val homeTab by viewModel.homeSelectedTab.collectAsStateWithLifecycle()
    val homeSearchQuery by viewModel.homeSearchQuery.collectAsStateWithLifecycle()
    val isRefreshingStatus by viewModel.isRefreshingStatus.collectAsStateWithLifecycle()
    val checkingProgress by viewModel.checkingProgress.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.refreshFeedbackMessage.collectAsStateWithLifecycle()
    val showAddAppDialog by viewModel.showAddAppDialog.collectAsStateWithLifecycle()

    val currentRole = userSession?.role ?: "DEVELOPER"
    val activeContract = contracts.find { it.id == selectedContractId } ?: contracts.firstOrNull()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            if (currentScreen != AppScreen.Initializing && currentScreen != AppScreen.Splash && currentScreen != AppScreen.Login && currentScreen != AppScreen.Home && currentScreen != AppScreen.CommunityHub) {
                DiscordTopBar(
                    title = "AppTrack",
                    subtitle = if (currentRole == "DEVELOPER") "Developer Workspace • ${contracts.size} Active Contracts"
                    else "${activeContract?.clientName ?: "Client"} Portal • ${activeContract?.companyName ?: "Live Hub"}",
                    currentRole = currentRole,
                    unreadNotifs = unreadNotifs,
                    onRoleClick = { viewModel.setRoleSwitcher(true) },
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
                    // Blank smooth transition matching native launch window, prevents any flash of Login
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                    )
                }

                is AppScreen.Splash -> {
                    SplashScreen(
                        onSplashComplete = {
                            viewModel.onSplashComplete()
                        }
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
                        userRole = userSession?.role ?: "CLIENT",
                        projects = projects,
                        selectedTab = homeTab,
                        searchQuery = homeSearchQuery,
                        isRefreshing = isRefreshingStatus,
                        checkingProgress = checkingProgress,
                        feedbackMessage = feedbackMessage,
                        showAddDialog = showAddAppDialog,
                        onTabSelected = { viewModel.setHomeTab(it) },
                        onSearchQueryChange = { viewModel.setHomeSearchQuery(it) },
                        onRefreshAll = { viewModel.refreshAllAppsStatus(context) },
                        onCheckSingle = { viewModel.checkSingleApp(it, context) },
                        onShowAddDialog = { viewModel.setShowAddAppDialog(it) },
                        onAddNewProject = { title, pkg, cat, iconUrl ->
                            viewModel.addNewProject(title, pkg, cat, iconUrl, context)
                        },
                        onDismissFeedback = { viewModel.dismissFeedbackMessage() },
                        onLogout = { viewModel.logout() }
                    )
                }

                is AppScreen.DevDashboard -> {
                    DeveloperDashboardScreen(
                        contracts = contracts,
                        projects = projects,
                        selectedContractId = selectedContractId,
                        selectedCategoryTab = selectedCategoryTab,
                        onSelectContract = { viewModel.selectContract(it) },
                        onSelectCategoryTab = { viewModel.setCategoryTab(it) },
                        onAddContractClick = { viewModel.setAddContractDialog(true) },
                        onAddProjectClick = { viewModel.setAddProjectDialog(true) },
                        onMarkLiveClick = { viewModel.setLiveTriggerProject(it) },
                        onOpenCommunityHub = { viewModel.navigateTo(AppScreen.CommunityHub) }
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
                        currentRole = currentRole,
                        onSelectChannel = { viewModel.selectChannel(it) },
                        onSendMessage = { viewModel.sendChatMessage(it) },
                        onBackClick = {
                            if (currentRole == "CLIENT") {
                                viewModel.navigateTo(AppScreen.ClientPortal)
                            } else {
                                viewModel.navigateTo(AppScreen.DevDashboard)
                            }
                        }
                    )
                }

                else -> {
                    DeveloperDashboardScreen(
                        contracts = contracts,
                        projects = projects,
                        selectedContractId = selectedContractId,
                        selectedCategoryTab = selectedCategoryTab,
                        onSelectContract = { viewModel.selectContract(it) },
                        onSelectCategoryTab = { viewModel.setCategoryTab(it) },
                        onAddContractClick = { viewModel.setAddContractDialog(true) },
                        onAddProjectClick = { viewModel.setAddProjectDialog(true) },
                        onMarkLiveClick = { viewModel.setLiveTriggerProject(it) },
                        onOpenCommunityHub = { viewModel.navigateTo(AppScreen.CommunityHub) }
                    )
                }
            }
        }
    }

    // Modal: Live Trigger
    if (activeLiveProject != null) {
        LiveTriggerDialog(
            project = activeLiveProject!!,
            onDismiss = { viewModel.setLiveTriggerProject(null) },
            onConfirmLive = { liveUrl, notes ->
                viewModel.markProjectAsLive(context, activeLiveProject!!, liveUrl, notes)
            }
        )
    }

    // Modal: Celebration & Milestone Receipt
    if (celebrationData != null) {
        CelebrationModal(
            celebration = celebrationData!!,
            onDismiss = { viewModel.dismissCelebration() }
        )
    }

    // Modal: Role Switcher
    if (showRoleSwitcher) {
        RoleSwitcherModal(
            currentRole = currentRole,
            contracts = contracts,
            selectedContractId = selectedContractId,
            onDismiss = { viewModel.setRoleSwitcher(false) },
            onSelectRole = { newRole, contractId ->
                viewModel.switchRole(newRole, contractId)
            }
        )
    }

    // Modal: Notification Center
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

    // Modal: Add Contract
    if (showAddContract) {
        AddContractModal(
            onDismiss = { viewModel.setAddContractDialog(false) },
            onAddContract = { name, client, email, comp, budget, payout, notes ->
                viewModel.addContract(name, client, email, comp, budget, payout, notes)
            }
        )
    }

    // Modal: Add Project
    if (showAddProject) {
        AddProjectModal(
            contractId = selectedContractId ?: contracts.firstOrNull()?.id ?: "contract_apex_01",
            initialCategory = selectedCategoryTab,
            onDismiss = { viewModel.setAddProjectDialog(false) },
            onAddProject = { title, cat, plat, pkg, ver, build, payout, notes, iconKey ->
                viewModel.addProject(
                    contractId = selectedContractId ?: contracts.firstOrNull()?.id ?: "contract_apex_01",
                    title = title,
                    category = cat,
                    platform = plat,
                    packageName = pkg,
                    versionName = ver,
                    buildNumber = build,
                    payoutAmount = payout,
                    releaseNotes = notes,
                    iconKey = iconKey
                )
            }
        )
    }
}
