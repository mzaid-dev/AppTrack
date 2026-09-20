package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.CommunityChannelEntity
import com.example.data.model.CommunityMessageEntity
import com.example.data.model.ContractEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.UserSessionEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class AppRepository(private val db: AppDatabase) {

    // Contracts
    val allContracts: Flow<List<ContractEntity>> = db.contractDao().getAllContracts()

    fun getContract(id: String): Flow<ContractEntity?> = db.contractDao().getContractById(id)

    suspend fun insertContract(contract: ContractEntity) = db.contractDao().insertContract(contract)

    suspend fun updateContract(contract: ContractEntity) = db.contractDao().updateContract(contract)

    suspend fun deleteContract(id: String) = db.contractDao().deleteContract(id)

    // Projects
    val allProjects: Flow<List<ProjectEntity>> = db.projectDao().getAllProjects()

    fun getProjectsByCategory(category: String): Flow<List<ProjectEntity>> =
        db.projectDao().getProjectsByCategory(category)

    fun getProjectsForContract(contractId: String): Flow<List<ProjectEntity>> =
        db.projectDao().getProjectsByContract(contractId)

    fun getProjectsByContractAndCategory(contractId: String, category: String): Flow<List<ProjectEntity>> =
        db.projectDao().getProjectsByContractAndCategory(contractId, category)

    fun getProject(id: String): Flow<ProjectEntity?> = db.projectDao().getProjectById(id)

    suspend fun insertProject(project: ProjectEntity) = db.projectDao().insertProject(project)

    suspend fun insertProjects(projects: List<ProjectEntity>) = db.projectDao().insertProjects(projects)

    suspend fun updateProject(project: ProjectEntity) = db.projectDao().updateProject(project)

    suspend fun updateProjectLiveStatus(id: String, status: String, liveUrl: String, timestamp: Long) =
        db.projectDao().updateProjectLiveStatus(id, status, liveUrl, timestamp, System.currentTimeMillis())

    suspend fun deleteProject(id: String) = db.projectDao().deleteProject(id)

    // Mark project as LIVE action
    suspend fun publishAndMarkLive(
        project: ProjectEntity,
        liveUrl: String,
        notes: String
    ): NotificationEntity {
        val updated = project.copy(
            status = "LIVE",
            liveStoreUrl = liveUrl,
            releaseNotes = if (notes.isNotBlank()) notes else project.releaseNotes,
            liveReleaseTimestamp = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        db.projectDao().updateProject(updated)

        // Generate in-app Notification for client
        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            title = "🎉 ${project.title} is now LIVE on Store!",
            message = "Your ${project.category.lowercase()} is officially published and live. Payout milestone of ${project.payoutMilestoneAmount} is unlocked for transfer.",
            timestamp = System.currentTimeMillis(),
            isRead = false,
            type = "APP_LIVE",
            projectId = project.id,
            storeUrl = liveUrl
        )
        db.notificationDao().insertNotification(notif)

        // Post announcement to Discord community #live-releases
        val liveMsg = CommunityMessageEntity(
            id = UUID.randomUUID().toString(),
            channelId = "ch_live_releases",
            authorName = "LaunchPulse Bot",
            authorRole = "SYSTEM_BOT",
            authorAvatarColor = 0xFF5865F2,
            content = "🚀 [LIVE DEPLOYMENT ALERT] **${project.title}** (${project.versionName}) is officially verified LIVE! Store URL: $liveUrl. Milestone payout ($${String.format("%.2f", project.payoutMilestoneAmount)}) is now UNLOCKED.",
            timestamp = System.currentTimeMillis(),
            isLiveAnnouncement = true,
            projectId = project.id,
            projectTitle = project.title,
            liveUrl = liveUrl
        )
        db.communityDao().insertMessage(liveMsg)

        return notif
    }

    // Community Channels & Messages
    val allChannels: Flow<List<CommunityChannelEntity>> = db.communityDao().getAllChannels()

    fun getMessagesForChannel(channelId: String): Flow<List<CommunityMessageEntity>> =
        db.communityDao().getMessagesForChannel(channelId)

    suspend fun sendMessage(
        channelId: String,
        authorName: String,
        authorRole: String,
        content: String,
        color: Long = 0xFF5865F2
    ) {
        val msg = CommunityMessageEntity(
            id = UUID.randomUUID().toString(),
            channelId = channelId,
            authorName = authorName,
            authorRole = authorRole,
            authorAvatarColor = color,
            content = content,
            timestamp = System.currentTimeMillis()
        )
        db.communityDao().insertMessage(msg)
    }

    // Notifications
    val allNotifications: Flow<List<NotificationEntity>> = db.notificationDao().getAllNotifications()

    val unreadNotificationsCount: Flow<Int> = db.notificationDao().getUnreadCount()

    suspend fun markNotificationRead(id: String) = db.notificationDao().markAsRead(id)

    suspend fun markAllNotificationsRead() = db.notificationDao().markAllAsRead()

    suspend fun clearNotifications() = db.notificationDao().clearAll()

    // User Session
    val userSession: Flow<UserSessionEntity?> = db.userSessionDao().getSession()

    suspend fun saveSession(session: UserSessionEntity) = db.userSessionDao().saveSession(session)

    suspend fun clearSession() = db.userSessionDao().clearSession()
}
