package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class ProjectCategory {
    APP,
    GAME
}

enum class ProjectPlatform {
    GOOGLE_PLAY,
    APP_STORE,
    CROSS_PLATFORM
}

enum class ReleaseStatus {
    DRAFT,
    DEVELOPMENT,
    IN_REVIEW,
    LIVE,
    ACTION_REQUIRED
}

enum class UserRole {
    DEVELOPER,
    CLIENT
}

@Entity(tableName = "contracts")
data class ContractEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val clientName: String,
    val clientEmail: String,
    val companyName: String,
    val totalBudget: Double,
    val payoutOnLive: Double,
    val currency: String = "$",
    val createdDate: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val contractId: String = "contract_apex_01",
    val title: String,
    val category: String, // "APP" or "GAME"
    val platform: String = "GOOGLE_PLAY", // "GOOGLE_PLAY", "APP_STORE", "CROSS_PLATFORM"
    val packageName: String,
    val versionName: String = "1.0.0",
    val buildNumber: Int = 1,
    val status: String = "DEVELOPMENT", // "DRAFT", "DEVELOPMENT", "IN_REVIEW", "LIVE", "ACTION_REQUIRED"
    val liveStoreUrl: String = "",
    val liveReleaseTimestamp: Long? = null,
    val payoutMilestoneAmount: Double = 0.0,
    val isPayoutSettled: Boolean = false,
    val releaseNotes: String = "",
    val iconKey: String = "phone",
    val iconUrl: String = "",
    val lastCheckedTimestamp: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "community_channels")
data class CommunityChannelEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val contractId: String? = null,
    val name: String,
    val description: String = "",
    val isLocked: Boolean = false
)

@Entity(tableName = "community_messages")
data class CommunityMessageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val channelId: String,
    val authorName: String,
    val authorRole: String, // "DEVELOPER", "CLIENT", "SYSTEM_BOT"
    val authorAvatarColor: Long = 0xFF5865F2,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isLiveAnnouncement: Boolean = false,
    val projectId: String? = null,
    val projectTitle: String? = null,
    val liveUrl: String? = null
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "APP_LIVE", // "APP_LIVE", "PAYOUT_READY", "MILESTONE_UPDATE"
    val projectId: String? = null,
    val storeUrl: String? = null
)

@Entity(tableName = "user_sessions")
data class UserSessionEntity(
    @PrimaryKey val id: Int = 1,
    val isLoggedIn: Boolean = true,
    val role: String = "DEVELOPER", // "DEVELOPER" or "CLIENT"
    val email: String = "dev@launchpulse.studio",
    val selectedContractId: String = ""
)
