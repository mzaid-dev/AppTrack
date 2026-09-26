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
import com.example.util.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot

import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FirebaseFirestore

sealed class AppScreen {
    object Initializing : AppScreen()
    object Login : AppScreen()
    object Home : AppScreen()
    object ClientPortal : AppScreen()
    object CommunityHub : AppScreen()
}

class MainViewModel(
    private val repository: AppRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

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

    // Dynamic user display name loaded from Firestore "users" collection
    private val _userName = MutableStateFlow<String>("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    // Navigation — synchronously initialized based on persistent session!
    private val _currentScreen = MutableStateFlow<AppScreen>(
        if (sessionManager.isLoggedIn()) AppScreen.Home else AppScreen.Login
    )
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Selected Contract for client view
    private val _selectedContractId = MutableStateFlow<String?>(
        sessionManager.getContractId().takeIf { it.isNotBlank() }
    )
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

    // In-memory set of project IDs currently undergoing status verification
    private val _checkingProjectIds = MutableStateFlow<Set<String>>(emptySet())
    val checkingProjectIds: StateFlow<Set<String>> = _checkingProjectIds.asStateFlow()

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
        // Synchronize persisted session with Room DB and restore state
        if (sessionManager.isLoggedIn()) {
            val savedEmail = sessionManager.getUserEmail()
            val savedRole = sessionManager.getUserRole().ifBlank { "CLIENT" }
            val savedContractId = sessionManager.getContractId()

            viewModelScope.launch {
                val entity = UserSessionEntity(
                    id = 1,
                    isLoggedIn = true,
                    role = savedRole,
                    email = savedEmail,
                    selectedContractId = savedContractId
                )
                repository.saveSession(entity)
                if (savedContractId.isNotBlank()) {
                    _selectedContractId.value = savedContractId
                }
            }
            _currentScreen.value = AppScreen.Home
        }

        // Keep observing repository.userSession in case of database-level updates
        viewModelScope.launch {
            repository.userSession.collect { session ->
                if (session != null && session.isLoggedIn) {
                    if (!sessionManager.isLoggedIn()) {
                        sessionManager.saveSession(session.email, session.role, session.selectedContractId)
                    }
                    if (_currentScreen.value == AppScreen.Initializing || _currentScreen.value == AppScreen.Login) {
                        _currentScreen.value = AppScreen.Home
                    }
                }
            }
        }
    }

    // ───── Navigation ─────

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    // ───── Session ─────

    fun getUserEmail(): String {
        return userSession.value?.email?.takeIf { it.isNotBlank() }
            ?: sessionManager.getUserEmail()
    }

    fun getUserRole(): String {
        return userSession.value?.role?.takeIf { it.isNotBlank() }
            ?: sessionManager.getUserRole()
    }

    /**
     * Loads user profile from Firestore "users" collection and pulls only their apps.
     * Supports both direct document ID (users/email) and auto-ID documents with email field.
     */
    fun loadUserData(context: Context, email: String) {
        if (email.isBlank()) return
        val cleanEmail = email.trim().lowercase()
        viewModelScope.launch {
            // 1. Fetch user display name from Firestore
            val db = FirebaseFirestore.getInstance()
            var firestoreName: String? = null

            // Method A: Auto-ID document with email field (matches user documents created with random IDs)
            try {
                val querySnap = db.collection("users")
                    .whereEqualTo("email", cleanEmail)
                    .limit(1)
                    .get()
                    .await()
                val matchDoc = querySnap.documents.firstOrNull()
                firestoreName = matchDoc?.getString("name")?.takeIf { it.isNotBlank() }
                    ?: matchDoc?.getString("displayName")?.takeIf { it.isNotBlank() }
                    ?: matchDoc?.getString("userName")?.takeIf { it.isNotBlank() }
            } catch (e: Exception) {
                Log.d("MainViewModel", "Users query by email note: ${e.message}")
            }

            // Method B: Direct document ID (users/email)
            if (firestoreName.isNullOrBlank()) {
                try {
                    val doc = db.collection("users").document(cleanEmail).get().await()
                    if (doc.exists()) {
                        firestoreName = doc.getString("name")?.takeIf { it.isNotBlank() }
                            ?: doc.getString("displayName")?.takeIf { it.isNotBlank() }
                            ?: doc.getString("userName")?.takeIf { it.isNotBlank() }
                    }
                } catch (e: Exception) {
                    Log.d("MainViewModel", "Users direct doc note: ${e.message}")
                }
            }

            // Method C: Also check if collection is named "clients"
            if (firestoreName.isNullOrBlank()) {
                try {
                    val clientSnap = db.collection("clients")
                        .whereEqualTo("email", cleanEmail)
                        .limit(1)
                        .get()
                        .await()
                    val matchDoc = clientSnap.documents.firstOrNull()
                    firestoreName = matchDoc?.getString("name")?.takeIf { it.isNotBlank() }
                        ?: matchDoc?.getString("clientName")?.takeIf { it.isNotBlank() }
                } catch (e: Exception) {
                    Log.d("MainViewModel", "Clients query note: ${e.message}")
                }
            }

            // Method D: Firebase Auth currentUser.displayName
            if (firestoreName.isNullOrBlank()) {
                try {
                    firestoreName = FirebaseAuth.getInstance().currentUser?.displayName?.takeIf { it.isNotBlank() }
                } catch (e: Exception) {
                    // Safe
                }
            }

            if (!firestoreName.isNullOrBlank()) {
                _userName.value = firestoreName
            } else {
                val raw = email.substringBefore("@")
                _userName.value = formatCleanName(raw)
            }

            // 2. Fetch apps for this specific user from Firestore
            // STRICT SOURCE OF TRUTH: Wipe old local SQLite cache and replace ONLY with this user's apps!
            try {
                val paged = FirebaseProjectSync.fetchPageFromFirestore(
                    context = context,
                    pageSize = 50L,
                    currentUserEmail = cleanEmail
                )
                if (paged != null) {
                    repository.clearAllProjects()
                    if (paged.projects.isNotEmpty()) {
                        repository.insertProjects(paged.projects)
                    }
                }
            } catch (e: Exception) {
                Log.d("MainViewModel", "Initial sync note: ${e.message}")
            }
        }
    }

    private fun formatCleanName(raw: String): String {
        return raw.replace(".", " ")
            .replace("_", " ")
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
    }

    fun login(email: String, role: String, contractId: String = "", context: Context? = null) {
        // 1. Save synchronously to SharedPreferences
        sessionManager.saveSession(email = email, role = role, contractId = contractId)

        if (contractId.isNotBlank()) {
            _selectedContractId.value = contractId
        }
        _currentScreen.value = AppScreen.Home

        // 2. Persist to Room DB and sync strictly this user's data from Firestore
        viewModelScope.launch {
            val session = UserSessionEntity(
                id = 1,
                isLoggedIn = true,
                role = role,
                email = email,
                selectedContractId = contractId
            )
            repository.saveSession(session)
            // Clear any old local projects from previous account
            repository.clearAllProjects()
            if (context != null) {
                loadUserData(context, email)
            }
        }
    }

    fun logout() {
        // 1. Clear SharedPreferences immediately
        sessionManager.clearSession()
        _userName.value = ""
        _currentScreen.value = AppScreen.Login

        // 2. Sign out of Firebase if initialized
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            // Safe to ignore
        }

        // 3. Clear Room DB session and remove all cached projects
        viewModelScope.launch {
            repository.clearSession()
            repository.clearAllProjects()
            val session = UserSessionEntity(
                id = 1,
                isLoggedIn = false,
                role = "CLIENT",
                email = "",
                selectedContractId = ""
            )
            repository.saveSession(session)
        }
    }

    // ───── Home Screen ─────

    private var lastFirestoreDoc: DocumentSnapshot? = null
    private var hasMorePages: Boolean = true
    private var isLoadingPage: Boolean = false

    private val _isLoadingNextPage = MutableStateFlow(false)
    val isLoadingNextPage: StateFlow<Boolean> = _isLoadingNextPage.asStateFlow()

    fun setHomeTab(tab: String, context: Context? = null) {
        _homeSelectedTab.value = tab
        // Background check for the selected tab without blocking UI or rebuilding list
        refreshCurrentTab(category = tab, context = context, isBackground = true)
    }

    fun setHomeSearchQuery(query: String) {
        _homeSearchQuery.value = query
    }

    fun dismissFeedbackMessage() {
        _refreshFeedbackMessage.value = null
    }

    /**
     * Infinite scroll / pagination: loads next batch from Firestore smoothly for current user.
     */
    fun loadNextPage(context: Context) {
        if (isLoadingPage || !hasMorePages) return
        val currentEmail = getUserEmail()
        viewModelScope.launch {
            isLoadingPage = true
            _isLoadingNextPage.value = true
            try {
                val pageResult = FirebaseProjectSync.fetchPageFromFirestore(
                    context = context,
                    pageSize = 15L,
                    lastDoc = lastFirestoreDoc,
                    currentUserEmail = currentEmail
                )
                if (pageResult != null) {
                    if (pageResult.projects.isNotEmpty()) {
                        val existingIds = projects.value.map { it.id }.toSet()
                        val newOnly = pageResult.projects.filter { it.id !in existingIds }
                        if (newOnly.isNotEmpty()) {
                            repository.insertProjects(newOnly)
                        }
                    }
                    lastFirestoreDoc = pageResult.lastDocument
                    hasMorePages = pageResult.hasMore
                }
            } catch (e: Exception) {
                Log.d("MainViewModel", "Pagination note: ${e.message}")
            } finally {
                isLoadingPage = false
                _isLoadingNextPage.value = false
            }
        }
    }

    /**
     * Refreshes live status ONLY for the current active tab (strictly isolated: "APP" or "GAME").
     * Keeps the list order completely stable (from 1 to end) without jumping or rebuilding.
     * When isBackground = true, checks quietly in background without full-screen spinners.
     */
    fun refreshCurrentTab(category: String = _homeSelectedTab.value, context: Context? = null, isBackground: Boolean = false) {
        if (_isRefreshingStatus.value && !isBackground) return
        val targetCategory = if (category.contains("game", ignoreCase = true)) "GAME" else "APP"
        val currentEmail = getUserEmail()

        viewModelScope.launch {
            if (!isBackground) {
                _isRefreshingStatus.value = true
                _refreshFeedbackMessage.value = null
            }

            var freshlySyncedProjects: List<ProjectEntity>? = null

            // 1. Sync strictly from Firestore for this user (both apps and user profile name)!
            if (context != null && !isBackground) {
                try {
                    loadUserData(context, currentEmail)
                    val paged = FirebaseProjectSync.fetchPageFromFirestore(
                        context = context,
                        pageSize = 50L,
                        currentUserEmail = currentEmail
                    )
                    if (paged != null) {
                        repository.clearAllProjects()
                        if (paged.projects.isNotEmpty()) {
                            repository.insertProjects(paged.projects)
                        }
                        freshlySyncedProjects = paged.projects
                    }
                } catch (e: Exception) {
                    Log.d("MainViewModel", "Refresh sync note: ${e.message}")
                }
            }

            // 2. Filter ONLY current active tab items! Use freshly synced projects to eliminate async state lag
            val activeList = freshlySyncedProjects ?: projects.value
            val tabProjects = activeList.filter { it.category.equals(targetCategory, ignoreCase = true) }
            if (tabProjects.isEmpty()) {
                if (!isBackground) _isRefreshingStatus.value = false
                return@launch
            }

            // 3. Mark current tab items as checking
            if (!isBackground) {
                _checkingProjectIds.value = tabProjects.map { it.id }.toSet()
                _checkingProgress.value = Pair(0, tabProjects.size)
            }

            // 4. Parallel check Play Store live status ONLY for current active tab's packages!
            val packages = tabProjects.map { it.packageName.trim() }
            val resultMap = LiveStatusChecker.checkAllInParallel(packages) { pkg, result, completed, total ->
                if (!isBackground) {
                    _checkingProgress.value = Pair(completed, total)
                }
                viewModelScope.launch {
                    val matchingProjects = tabProjects.filter { it.packageName.trim().equals(pkg, ignoreCase = true) }
                    matchingProjects.forEach { project ->
                        _checkingProjectIds.update { it - project.id }
                        repository.updateProjectLiveStatus(
                            id = project.id,
                            status = if (result.isLive) "LIVE" else "PENDING",
                            liveUrl = "https://play.google.com/store/apps/details?id=$pkg",
                            timestamp = result.checkedAt
                        )
                    }
                }
            }

            // Ensure checking IDs for this tab are cleared
            _checkingProjectIds.update { currentSet -> currentSet - tabProjects.map { it.id }.toSet() }

            if (!isBackground) {
                _isRefreshingStatus.value = false

                val liveCount = resultMap.values.count { it.isLive }
                val unableCount = resultMap.values.count { it.statusText == "UNABLE TO CHECK" }
                val notFoundCount = resultMap.values.count { it.statusText == "NOT FOUND" }
                val tabLabel = if (targetCategory == "GAME") "Games" else "Applications"
                _refreshFeedbackMessage.value = if (unableCount == 0 && notFoundCount == 0) {
                    "$tabLabel up to date ($liveCount Live)"
                } else {
                    "$tabLabel: $liveCount Live" +
                        (if (unableCount > 0) " • $unableCount Unable to Check" else "") +
                        (if (notFoundCount > 0) " • $notFoundCount Not Found" else "")
                }

                viewModelScope.launch {
                    delay(3500)
                    if (_refreshFeedbackMessage.value != null) {
                        _refreshFeedbackMessage.value = null
                    }
                }
            }
        }
    }

    /**
     * Backward-compatible helper that delegates to refreshCurrentTab.
     */
    fun refreshAllAppsStatus(context: Context? = null) {
        refreshCurrentTab(category = _homeSelectedTab.value, context = context, isBackground = false)
    }

    /**
     * Checks a single app's live status on Play Store.
     */
    fun checkSingleApp(project: ProjectEntity, context: Context? = null) {
        viewModelScope.launch {
            _checkingProjectIds.update { it + project.id }
            val result = LiveStatusChecker.checkAppLive(project.packageName)
            _checkingProjectIds.update { it - project.id }

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

class MainViewModelFactory(
    private val repository: AppRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    constructor(repository: AppRepository, context: Context) : this(
        repository,
        SessionManager(context)
    )

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
