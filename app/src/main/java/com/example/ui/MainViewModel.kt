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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

sealed class AppScreen {
    object Initializing : AppScreen()
    object Login : AppScreen()
    object Home : AppScreen()
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

    // Navigation — starts on Initializing to avoid flashing Login on restore
    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Initializing)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Selected Contract for client view
    private val _selectedContractId = MutableStateFlow<String?>(null)
    val selectedContractId: StateFlow<String?> = _selectedContractId.asStateFlow()

    // Active Category tab (APP / GAME)
    private val _categoryTab = MutableStateFlow("APP")
    val categoryTab: StateFlow<String> = _categoryTab.asStateFlow()

    // Active channel in community
    private val _selectedChannelId = MutableStateFlow("ch_live_releases")
    val selectedChannelId: StateFlow<String> = _selectedChannelId.asStateFlow()

    // UI Dialog States
    private val _showNotificationCenter = MutableStateFlow(false)
    val showNotificationCenter: StateFlow<Boolean> = _showNotificationCenter.asStateFlow()

    // Home Screen State
    private val _homeSelectedTab = MutableStateFlow("APP")
    val homeSelectedTab: StateFlow<String> = _homeSelectedTab.asStateFlow()

    private val _homeSearchQuery = MutableStateFlow("")
    val homeSearchQuery: StateFlow<String> = _homeSearchQuery.asStateFlow()

    private val _isRefreshingStatus = MutableStateFlow(false)
    val isRefreshingStatus: StateFlow<Boolean> = _isRefreshingStatus.asStateFlow()

    private val _checkingProgress = MutableStateFlow(Pair(0, 0))
    val checkingProgress: StateFlow<Pair<Int, Int>> = _checkingProgress.asStateFlow()

    private val _refreshFeedbackMessage = MutableStateFlow<String?>(null)
    val refreshFeedbackMessage: StateFlow<String?> = _refreshFeedbackMessage.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val channelMessages: StateFlow<List<CommunityMessageEntity>> = _selectedChannelId
        .flatMapLatest { channelId ->
            repository.getMessagesForChannel(channelId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Check session on startup — if logged in go straight to Home, else Login
        viewModelScope.launch {
            repository.userSession.collect { session ->
                if (_currentScreen.value == AppScreen.Initializing) {
                    _currentScreen.value = if (session?.isLoggedIn == true) AppScreen.Home else AppScreen.Login
                }
            }
        }
    }

    // ───── Navigation ─────

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    // ───── Session ─────

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

    fun logout() {
        viewModelScope.launch {
            try {
                FirebaseAuth.getInstance().signOut()
            } catch (e: Exception) {
                // Firebase not initialized or offline — safe to ignore
            }
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

    // ───── Home Screen ─────

    fun setHomeTab(tab: String) {
        _homeSelectedTab.value = tab
    }

    fun setHomeSearchQuery(query: String) {
        _homeSearchQuery.value = query
    }

    fun dismissFeedbackMessage() {
        _refreshFeedbackMessage.value = null
    }

    /**
     * Parallel live-status refresh for all tracked apps/games.
     * Syncs from Firestore first (if available), then checks Play Store concurrently.
     */
    fun refreshAllAppsStatus(context: Context? = null) {
        if (_isRefreshingStatus.value) return
        viewModelScope.launch {
            _isRefreshingStatus.value = true
            _refreshFeedbackMessage.value = null

            // 1. Pull latest from Firestore
            if (context != null) {
                val firestoreProjects = FirebaseProjectSync.fetchFromFirestore(context)
                if (firestoreProjects != null && firestoreProjects.isNotEmpty()) {
                    repository.insertProjects(firestoreProjects)
                }
            }

            // 2. Get current project list
            val currentList = projects.value
            if (currentList.isEmpty()) {
                _isRefreshingStatus.value = false
                return@launch
            }

            // 3. Set all to CHECKING visually
            currentList.forEach { project ->
                repository.updateProjectLiveStatus(
                    id = project.id,
                    status = "CHECKING",
                    liveUrl = project.liveStoreUrl,
                    timestamp = System.currentTimeMillis()
                )
            }
            _checkingProgress.value = Pair(0, currentList.size)

            // 4. Parallel check all package names
            val packages = currentList.map { it.packageName }
            val resultMap = LiveStatusChecker.checkAllInParallel(packages) { pkg, result, completed, total ->
                _checkingProgress.value = Pair(completed, total)
                viewModelScope.launch {
                    currentList.filter { it.packageName == pkg }.forEach { project ->
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
                "All statuses are up to date ($liveCount Live)"
            } else {
                "Status check complete • $liveCount Live" +
                    (if (unableCount > 0) " • $unableCount Unable to Check" else "") +
                    (if (notFoundCount > 0) " • $notFoundCount Not Found" else "")
            }

            // Auto-dismiss after 3.5 seconds
            viewModelScope.launch {
                delay(3500)
                if (_refreshFeedbackMessage.value != null) {
                    _refreshFeedbackMessage.value = null
                }
            }
        }
    }

    /**
     * Checks a single app's live status on Play Store.
     */
    fun checkSingleApp(project: ProjectEntity, context: Context? = null) {
        viewModelScope.launch {
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

    // ───── Client Portal ─────

    fun selectContract(contractId: String) {
        _selectedContractId.value = contractId
    }

    fun setCategoryTab(tab: String) {
        _categoryTab.value = tab
    }

    // ───── Community Chat ─────

    fun selectChannel(channelId: String) {
        _selectedChannelId.value = channelId
    }

    fun sendChatMessage(content: String) {
        if (content.isBlank()) return
        val currentSession = userSession.value
        val authorName = "Client (${currentSession?.email?.substringBefore("@") ?: "User"})"
        viewModelScope.launch {
            repository.sendMessage(
                channelId = _selectedChannelId.value,
                authorName = authorName,
                authorRole = "CLIENT",
                content = content,
                color = 0xFF23A55A
            )
        }
    }

    // ───── Notifications ─────

    fun setNotificationCenter(show: Boolean) {
        _showNotificationCenter.value = show
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

class MainViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
