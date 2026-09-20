package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.CommunityChannelEntity
import com.example.data.model.CommunityMessageEntity
import com.example.data.model.ContractEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.UserSessionEntity
import com.example.data.repository.AppRepository
import com.example.data.firebase.FirebaseProjectSync
import com.example.data.network.LiveStatusChecker
import com.example.util.NotificationHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

sealed class AppScreen {
    object Initializing : AppScreen()
    object Splash : AppScreen()
    object Login : AppScreen()
    object Home : AppScreen()
    object DevDashboard : AppScreen()
    object ContractDetail : AppScreen()
    object ClientPortal : AppScreen()
    object CommunityHub : AppScreen()
}

class MainViewModel(private val repository: AppRepository) : ViewModel() {

    val contracts: StateFlow<List<ContractEntity>> = repository.allContracts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val channels: StateFlow<List<CommunityChannelEntity>> = repository.allChannels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotifCount: StateFlow<Int> = repository.unreadNotificationsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val userSession: StateFlow<UserSessionEntity?> = repository.userSession
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current active navigation screen - starts on Initializing to seamlessly resolve auth session without flashing Login
    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Initializing)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Selected Contract for detail or client view
    private val _selectedContractId = MutableStateFlow<String?>("contract_apex_01")
    val selectedContractId: StateFlow<String?> = _selectedContractId.asStateFlow()

    // Active Category tab inside contract ("APP" or "GAME")
    private val _categoryTab = MutableStateFlow("APP")
    val categoryTab: StateFlow<String> = _categoryTab.asStateFlow()

    // Active channel in community
    private val _selectedChannelId = MutableStateFlow("ch_live_releases")
    val selectedChannelId: StateFlow<String> = _selectedChannelId.asStateFlow()

    // UI Dialog States
    private val _showAddContractDialog = MutableStateFlow(false)
    val showAddContractDialog: StateFlow<Boolean> = _showAddContractDialog.asStateFlow()

    private val _showAddProjectDialog = MutableStateFlow(false)
    val showAddProjectDialog: StateFlow<Boolean> = _showAddProjectDialog.asStateFlow()

    private val _activeLiveTriggerProject = MutableStateFlow<ProjectEntity?>(null)
    val activeLiveTriggerProject: StateFlow<ProjectEntity?> = _activeLiveTriggerProject.asStateFlow()

    private val _celebrationEvent = MutableStateFlow<CelebrationData?>(null)
    val celebrationEvent: StateFlow<CelebrationData?> = _celebrationEvent.asStateFlow()

    private val _showNotificationCenter = MutableStateFlow(false)
    val showNotificationCenter: StateFlow<Boolean> = _showNotificationCenter.asStateFlow()

    private val _showRoleSwitcher = MutableStateFlow(false)
    val showRoleSwitcher: StateFlow<Boolean> = _showRoleSwitcher.asStateFlow()

    // Home Screen specific state
    private val _homeSelectedTab = MutableStateFlow("APP") // "APP" or "GAME"
    val homeSelectedTab: StateFlow<String> = _homeSelectedTab.asStateFlow()

    private val _homeSearchQuery = MutableStateFlow("")
    val homeSearchQuery: StateFlow<String> = _homeSearchQuery.asStateFlow()

    private val _isRefreshingStatus = MutableStateFlow(false)
    val isRefreshingStatus: StateFlow<Boolean> = _isRefreshingStatus.asStateFlow()

    private val _checkingProgress = MutableStateFlow(Pair(0, 0))
    val checkingProgress: StateFlow<Pair<Int, Int>> = _checkingProgress.asStateFlow()

    private val _refreshFeedbackMessage = MutableStateFlow<String?>(null)
    val refreshFeedbackMessage: StateFlow<String?> = _refreshFeedbackMessage.asStateFlow()

    private val _showAddAppDialog = MutableStateFlow(false)
    val showAddAppDialog: StateFlow<Boolean> = _showAddAppDialog.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val channelMessages: StateFlow<List<CommunityMessageEntity>> = _selectedChannelId
        .flatMapLatest { channelId ->
            repository.getMessagesForChannel(channelId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedContract: StateFlow<ContractEntity?> = combine(contracts, _selectedContractId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Check session directly on startup - if logged in go straight to Home, else Login (zero flash of Login screen)
        viewModelScope.launch {
            repository.userSession.collect { session ->
                if (_currentScreen.value == AppScreen.Initializing) {
                    _currentScreen.value = if (session?.isLoggedIn == true) AppScreen.Home else AppScreen.Login
                }
            }
        }
    }

    fun onSplashComplete() {
        val session = userSession.value
        if (session?.isLoggedIn == true) {
            _currentScreen.value = AppScreen.Home
        } else {
            _currentScreen.value = AppScreen.Login
        }
    }

    fun setHomeTab(tab: String) {
        _homeSelectedTab.value = tab
    }

    fun setHomeSearchQuery(query: String) {
        _homeSearchQuery.value = query
    }

    fun setShowAddAppDialog(show: Boolean) {
        _showAddAppDialog.value = show
    }

    fun dismissFeedbackMessage() {
        _refreshFeedbackMessage.value = null
    }

    /**
     * Multithreaded parallel status checker across all Apps and Games.
     * Uses coroutines supervisorScope with async(Dispatchers.IO) to check dozens of apps concurrently.
     * Sets each item to "CHECKING" first, then updates each as results arrive.
     */
    fun refreshAllAppsStatus(context: Context? = null) {
        if (_isRefreshingStatus.value) return
        viewModelScope.launch {
            _isRefreshingStatus.value = true
            _refreshFeedbackMessage.value = null

            // 1. Sync from Firebase Firestore if available
            if (context != null) {
                val firestoreProjects = FirebaseProjectSync.fetchFromFirestore(context)
                if (firestoreProjects != null && firestoreProjects.isNotEmpty()) {
                    repository.insertProjects(firestoreProjects)
                }
            }

            // 2. Fetch current list of projects
            val currentList = projects.value
            if (currentList.isEmpty()) {
                _isRefreshingStatus.value = false
                return@launch
            }

            // 3. Mark each visible item to "CHECKING" state
            currentList.forEach { project ->
                repository.updateProjectLiveStatus(
                    id = project.id,
                    status = "CHECKING",
                    liveUrl = project.liveStoreUrl,
                    timestamp = System.currentTimeMillis()
                )
            }

            _checkingProgress.value = Pair(0, currentList.size)

            // 4. Parallel check across all package names concurrently
            val packages = currentList.map { it.packageName }
            val resultMap = LiveStatusChecker.checkAllInParallel(packages) { pkg, result, completed, total ->
                _checkingProgress.value = Pair(completed, total)
                viewModelScope.launch {
                    val matching = currentList.filter { it.packageName == pkg }
                    matching.forEach { project ->
                        repository.updateProjectLiveStatus(
                            id = project.id,
                            status = result.statusText,
                            liveUrl = result.storeUrl,
                            timestamp = result.checkedAt
                        )
                        if (context != null) {
                            FirebaseProjectSync.syncProjectToFirestore(
                                context,
                                project.copy(
                                    status = result.statusText,
                                    liveStoreUrl = result.storeUrl,
                                    lastCheckedTimestamp = result.checkedAt
                                )
                            )
                        }
                    }
                }
            }

            delay(250)
            _isRefreshingStatus.value = false

            val liveCount = resultMap.values.count { it.isLive }
            val unableCount = resultMap.values.count { it.statusText == "UNABLE TO CHECK" }
            val notFoundCount = resultMap.values.count { it.statusText == "NOT FOUND" }
            _refreshFeedbackMessage.value = if (unableCount == 0 && notFoundCount == 0) {
                "All app and game statuses are up to date ($liveCount Live)"
            } else {
                "Status check complete • $liveCount Live" +
                    (if (unableCount > 0) " • $unableCount Unable to Check" else "") +
                    (if (notFoundCount > 0) " • $notFoundCount Not Found" else "")
            }

            // Auto-dismiss feedback message after 3.5 seconds
            viewModelScope.launch {
                delay(3500)
                if (_refreshFeedbackMessage.value != null) {
                    _refreshFeedbackMessage.value = null
                }
            }
        }
    }

    /**
     * Checks an individual app's live status.
     * Transitions row to "CHECKING" first, then updates to Live, Not Found, or Unable to Check.
     */
    fun checkSingleApp(project: ProjectEntity, context: Context? = null) {
        viewModelScope.launch {
            // Immediately transition this single item to CHECKING
            repository.updateProjectLiveStatus(
                id = project.id,
                status = "CHECKING",
                liveUrl = project.liveStoreUrl,
                timestamp = System.currentTimeMillis()
            )

            val result = LiveStatusChecker.checkAppLive(project.packageName)
            repository.updateProjectLiveStatus(
                id = project.id,
                status = result.statusText,
                liveUrl = result.storeUrl,
                timestamp = result.checkedAt
            )
            if (context != null) {
                FirebaseProjectSync.syncProjectToFirestore(
                    context,
                    project.copy(
                        status = result.statusText,
                        liveStoreUrl = result.storeUrl,
                        lastCheckedTimestamp = result.checkedAt
                    )
                )
            }
        }
    }

    fun addNewProject(
        title: String,
        packageName: String,
        category: String,
        iconUrl: String = "",
        context: Context? = null
    ) {
        viewModelScope.launch {
            val newProject = ProjectEntity(
                id = "proj_" + UUID.randomUUID().toString().take(8),
                contractId = _selectedContractId.value ?: "contract_apex_01",
                title = title.trim(),
                category = category,
                packageName = packageName.trim(),
                iconUrl = iconUrl.trim(),
                iconKey = if (category == "GAME") "gamepad" else "phone",
                status = "CHECKING",
                updatedAt = System.currentTimeMillis()
            )
            repository.insertProject(newProject)
            _showAddAppDialog.value = false

            // Immediately check live status
            val checkResult = LiveStatusChecker.checkAppLive(newProject.packageName)
            repository.updateProjectLiveStatus(
                id = newProject.id,
                status = checkResult.statusText,
                liveUrl = checkResult.storeUrl,
                timestamp = checkResult.checkedAt
            )
            if (context != null) {
                FirebaseProjectSync.syncProjectToFirestore(
                    context,
                    newProject.copy(
                        status = checkResult.statusText,
                        liveStoreUrl = checkResult.storeUrl,
                        lastCheckedTimestamp = checkResult.checkedAt
                    )
                )
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun selectContract(contractId: String) {
        _selectedContractId.value = contractId
    }

    fun setCategoryTab(tab: String) {
        _categoryTab.value = tab
    }

    fun selectChannel(channelId: String) {
        _selectedChannelId.value = channelId
    }

    fun setAddContractDialog(show: Boolean) {
        _showAddContractDialog.value = show
    }

    fun setAddProjectDialog(show: Boolean) {
        _showAddProjectDialog.value = show
    }

    fun setLiveTriggerProject(project: ProjectEntity?) {
        _activeLiveTriggerProject.value = project
    }

    fun dismissCelebration() {
        _celebrationEvent.value = null
    }

    fun setNotificationCenter(show: Boolean) {
        _showNotificationCenter.value = show
    }

    fun setRoleSwitcher(show: Boolean) {
        _showRoleSwitcher.value = show
    }

    fun login(email: String, role: String, contractId: String = "") {
        viewModelScope.launch {
            val session = UserSessionEntity(
                id = 1,
                isLoggedIn = true,
                role = role,
                email = email,
                selectedContractId = contractId
            )
            repository.saveSession(session)
            if (contractId.isNotBlank()) {
                _selectedContractId.value = contractId
            }
            _currentScreen.value = AppScreen.Home
        }
    }

    fun switchRole(newRole: String, contractId: String? = null) {
        viewModelScope.launch {
            val current = userSession.value
            val targetContract = contractId ?: _selectedContractId.value ?: "contract_apex_01"
            val session = UserSessionEntity(
                id = 1,
                isLoggedIn = true,
                role = newRole,
                email = if (newRole == "CLIENT") "sarah@apexstudio.io" else "dev@launchpulse.studio",
                selectedContractId = targetContract
            )
            repository.saveSession(session)
            _selectedContractId.value = targetContract
            if (newRole == "CLIENT") {
                _currentScreen.value = AppScreen.ClientPortal
            } else {
                _currentScreen.value = AppScreen.DevDashboard
            }
            _showRoleSwitcher.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.clearSession()
            val session = UserSessionEntity(
                id = 1,
                isLoggedIn = false,
                role = "CLIENT",
                email = "",
                selectedContractId = ""
            )
            repository.saveSession(session)
            _currentScreen.value = AppScreen.Login
        }
    }

    fun addContract(
        name: String,
        clientName: String,
        clientEmail: String,
        companyName: String,
        totalBudget: Double,
        payoutOnLive: Double,
        notes: String
    ) {
        viewModelScope.launch {
            val newId = "contract_" + UUID.randomUUID().toString().take(8)
            val contract = ContractEntity(
                id = newId,
                name = name,
                clientName = clientName,
                clientEmail = clientEmail,
                companyName = companyName,
                totalBudget = totalBudget,
                payoutOnLive = payoutOnLive,
                notes = notes
            )
            repository.insertContract(contract)
            _selectedContractId.value = newId
            _showAddContractDialog.value = false
        }
    }

    fun addProject(
        contractId: String,
        title: String,
        category: String,
        platform: String,
        packageName: String,
        versionName: String,
        buildNumber: Int,
        payoutAmount: Double,
        releaseNotes: String,
        iconKey: String
    ) {
        viewModelScope.launch {
            val project = ProjectEntity(
                id = "proj_" + UUID.randomUUID().toString().take(8),
                contractId = contractId,
                title = title,
                category = category,
                platform = platform,
                packageName = packageName,
                versionName = versionName,
                buildNumber = buildNumber,
                status = "DEVELOPMENT",
                payoutMilestoneAmount = payoutAmount,
                releaseNotes = releaseNotes,
                iconKey = iconKey
            )
            repository.insertProject(project)
            _showAddProjectDialog.value = false
        }
    }

    fun markProjectAsLive(
        context: Context,
        project: ProjectEntity,
        liveUrl: String,
        notes: String
    ) {
        viewModelScope.launch {
            val notif = repository.publishAndMarkLive(project, liveUrl, notes)
            _activeLiveTriggerProject.value = null

            // Trigger real Android Push / System Notification
            NotificationHelper.showLiveNotification(
                context = context,
                title = "🎉 ${project.title} is now LIVE!",
                message = "Your ${project.category.lowercase()} is published on the store. Milestone payment ($${String.format("%.2f", project.payoutMilestoneAmount)}) is now UNLOCKED!"
            )

            // Show celebratory dialog with invoice breakdown
            _celebrationEvent.value = CelebrationData(
                project = project,
                liveUrl = liveUrl,
                payoutAmount = project.payoutMilestoneAmount,
                notes = notes
            )
        }
    }

    fun updateProjectStatus(project: ProjectEntity, newStatus: String) {
        viewModelScope.launch {
            val updated = project.copy(status = newStatus, updatedAt = System.currentTimeMillis())
            repository.updateProject(updated)
        }
    }

    fun sendChatMessage(content: String) {
        if (content.isBlank()) return
        val currentSession = userSession.value
        val authorName = if (currentSession?.role == "CLIENT") "Client (${currentSession.email.substringBefore("@")})" else "Developer (Studio)"
        val authorRole = currentSession?.role ?: "DEVELOPER"
        val authorColor = if (authorRole == "CLIENT") 0xFF23A55A else 0xFF5865F2

        viewModelScope.launch {
            repository.sendMessage(
                channelId = _selectedChannelId.value,
                authorName = authorName,
                authorRole = authorRole,
                content = content,
                color = authorColor
            )
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
        }
    }
}

data class CelebrationData(
    val project: ProjectEntity,
    val liveUrl: String,
    val payoutAmount: Double,
    val notes: String
)

class MainViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
