package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CommunityChannelEntity
import com.example.data.model.CommunityMessageEntity
import com.example.data.model.ContractEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.UserSessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ContractEntity::class,
        ProjectEntity::class,
        CommunityChannelEntity::class,
        CommunityMessageEntity::class,
        NotificationEntity::class,
        UserSessionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contractDao(): ContractDao
    abstract fun projectDao(): ProjectDao
    abstract fun communityDao(): CommunityDao
    abstract fun notificationDao(): NotificationDao
    abstract fun userSessionDao(): UserSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "launchpulse_database.db"
                )
                .fallbackToDestructiveMigration(true)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate with seed data on first creation
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            seedInitialData(database)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(db: AppDatabase) {
            val contract1Id = "contract_apex_01"
            val contract2Id = "contract_stellar_02"

            val initialContracts = listOf(
                ContractEntity(
                    id = contract1Id,
                    name = "Apex Studio Mobile Suite",
                    clientName = "Sarah Jenkins",
                    clientEmail = "sarah@apexstudio.io",
                    companyName = "Apex Entertainment Inc.",
                    totalBudget = 14500.0,
                    payoutOnLive = 5000.0,
                    currency = "$",
                    notes = "Contract clause: 35% final milestone released immediately upon Play Store public live status."
                ),
                ContractEntity(
                    id = contract2Id,
                    name = "Stellar FinTech & HyperCasual",
                    clientName = "Marcus Vance",
                    clientEmail = "marcus@stellargroup.co",
                    companyName = "Stellar Global Holdings",
                    totalBudget = 9800.0,
                    payoutOnLive = 3200.0,
                    currency = "$",
                    notes = "Apps & Games package. Live status triggers milestone invoice."
                )
            )
            initialContracts.forEach { db.contractDao().insertContract(it) }

            val initialProjects = listOf(
                // Live Apps
                ProjectEntity(
                    id = "app_spotify",
                    contractId = contract1Id,
                    title = "Spotify: Music and Podcasts",
                    category = "APP",
                    platform = "GOOGLE_PLAY",
                    packageName = "com.spotify.music",
                    versionName = "8.9.12",
                    buildNumber = 104,
                    status = "LIVE",
                    liveStoreUrl = "https://play.google.com/store/apps/details?id=com.spotify.music",
                    payoutMilestoneAmount = 3500.0,
                    iconKey = "music",
                    iconUrl = "https://play-lh.googleusercontent.com/P2MD5kuqUVbvMdpQUs7SxDtmNuqu-Pp-IsioENQi9QA"
                ),
                ProjectEntity(
                    id = "app_youtube",
                    contractId = contract1Id,
                    title = "YouTube",
                    category = "APP",
                    platform = "GOOGLE_PLAY",
                    packageName = "com.google.android.youtube",
                    versionName = "19.20.34",
                    buildNumber = 1420,
                    status = "LIVE",
                    liveStoreUrl = "https://play.google.com/store/apps/details?id=com.google.android.youtube",
                    payoutMilestoneAmount = 4000.0,
                    iconKey = "video",
                    iconUrl = "https://play-lh.googleusercontent.com/lMoItBgdPPVDJsAq7qRIEKrTFc75WdTNTrTWUh8DUEq2iAyYEuIVzNoV2rV1"
                ),
                ProjectEntity(
                    id = "app_whatsapp",
                    contractId = contract1Id,
                    title = "WhatsApp Messenger",
                    category = "APP",
                    platform = "GOOGLE_PLAY",
                    packageName = "com.whatsapp",
                    versionName = "2.24.11",
                    buildNumber = 882,
                    status = "LIVE",
                    liveStoreUrl = "https://play.google.com/store/apps/details?id=com.whatsapp",
                    payoutMilestoneAmount = 3000.0,
                    iconKey = "chat"
                ),
                ProjectEntity(
                    id = "app_duolingo",
                    contractId = contract2Id,
                    title = "Duolingo: Language Lessons",
                    category = "APP",
                    platform = "GOOGLE_PLAY",
                    packageName = "com.duolingo",
                    versionName = "5.148.4",
                    buildNumber = 1622,
                    status = "LIVE",
                    liveStoreUrl = "https://play.google.com/store/apps/details?id=com.duolingo",
                    payoutMilestoneAmount = 2800.0,
                    iconKey = "education"
                ),
                ProjectEntity(
                    id = "app_reddit",
                    contractId = contract2Id,
                    title = "Reddit",
                    category = "APP",
                    platform = "GOOGLE_PLAY",
                    packageName = "com.reddit.frontpage",
                    versionName = "2024.19.0",
                    buildNumber = 502,
                    status = "LIVE",
                    liveStoreUrl = "https://play.google.com/store/apps/details?id=com.reddit.frontpage",
                    payoutMilestoneAmount = 2200.0,
                    iconKey = "forum"
                ),
                ProjectEntity(
                    id = "app_nova_wallet",
                    contractId = contract2Id,
                    title = "Nova Wallet & Crypto Tracker",
                    category = "APP",
                    platform = "GOOGLE_PLAY",
                    packageName = "com.stellar.novawallet",
                    versionName = "1.1.2",
                    buildNumber = 33,
                    status = "NOT LIVE",
                    liveStoreUrl = "",
                    payoutMilestoneAmount = 1400.0,
                    iconKey = "wallet"
                ),
                ProjectEntity(
                    id = "app_apex_playerhub",
                    contractId = contract1Id,
                    title = "Apex Player Hub & Companion",
                    category = "APP",
                    platform = "GOOGLE_PLAY",
                    packageName = "com.apex.playerhub",
                    versionName = "2.1.0",
                    buildNumber = 45,
                    status = "NOT LIVE",
                    liveStoreUrl = "",
                    payoutMilestoneAmount = 2000.0,
                    iconKey = "phone"
                ),

                // Live & Staging Games
                ProjectEntity(
                    id = "game_clash_of_clans",
                    contractId = contract1Id,
                    title = "Clash of Clans",
                    category = "GAME",
                    platform = "GOOGLE_PLAY",
                    packageName = "com.supercell.clashofclans",
                    versionName = "16.253.25",
                    buildNumber = 1740,
                    status = "LIVE",
                    liveStoreUrl = "https://play.google.com/store/apps/details?id=com.supercell.clashofclans",
                    payoutMilestoneAmount = 5000.0,
                    iconKey = "gamepad"
                ),
                ProjectEntity(
                    id = "game_subway_surfers",
                    contractId = contract1Id,
                    title = "Subway Surfers",
                    category = "GAME",
                    platform = "GOOGLE_PLAY",
                    packageName = "com.kiloo.subwaysurf",
                    versionName = "3.28.0",
                    buildNumber = 980,
                    status = "LIVE",
                    liveStoreUrl = "https://play.google.com/store/apps/details?id=com.kiloo.subwaysurf",
                    payoutMilestoneAmount = 4500.0,
                    iconKey = "rocket"
                ),
                ProjectEntity(
                    id = "game_candy_crush",
                    contractId = contract2Id,
                    title = "Candy Crush Saga",
                    category = "GAME",
                    platform = "GOOGLE_PLAY",
                    packageName = "com.king.candycrushsaga",
                    versionName = "1.277.0",
                    buildNumber = 2100,
                    status = "LIVE",
                    liveStoreUrl = "https://play.google.com/store/apps/details?id=com.king.candycrushsaga",
                    payoutMilestoneAmount = 4200.0,
                    iconKey = "gamepad"
                ),
                ProjectEntity(
                    id = "game_roblox",
                    contractId = contract2Id,
                    title = "Roblox",
                    category = "GAME",
                    platform = "GOOGLE_PLAY",
                    packageName = "com.roblox.client",
                    versionName = "2.628.543",
                    buildNumber = 543,
                    status = "LIVE",
                    liveStoreUrl = "https://play.google.com/store/apps/details?id=com.roblox.client",
                    payoutMilestoneAmount = 4800.0,
                    iconKey = "gamepad"
                ),
                ProjectEntity(
                    id = "game_cyberblade",
                    contractId = contract1Id,
                    title = "CyberBlade Arena",
                    category = "GAME",
                    platform = "GOOGLE_PLAY",
                    packageName = "com.apex.cyberblade",
                    versionName = "1.0.4",
                    buildNumber = 28,
                    status = "NOT LIVE",
                    liveStoreUrl = "",
                    payoutMilestoneAmount = 3000.0,
                    iconKey = "gamepad"
                ),
                ProjectEntity(
                    id = "game_orbit_drift",
                    contractId = contract2Id,
                    title = "Orbit Drift 3D",
                    category = "GAME",
                    platform = "CROSS_PLATFORM",
                    packageName = "com.stellar.orbitdrift",
                    versionName = "1.0.0",
                    buildNumber = 12,
                    status = "NOT LIVE",
                    liveStoreUrl = "",
                    payoutMilestoneAmount = 1800.0,
                    iconKey = "rocket"
                )
            )
            initialProjects.forEach { db.projectDao().insertProject(it) }

            // Channels
            val channels = listOf(
                CommunityChannelEntity(
                    id = "ch_live_releases",
                    name = "live-releases",
                    description = "Automated live release announcements & store URLs"
                ),
                CommunityChannelEntity(
                    id = "ch_announcements",
                    name = "announcements",
                    description = "Official project milestones & store submissions"
                ),
                CommunityChannelEntity(
                    id = "ch_client_chat",
                    name = "client-discussion",
                    description = "Direct chat between developer and client"
                ),
                CommunityChannelEntity(
                    id = "ch_payouts",
                    name = "payout-verification",
                    description = "Contract payout updates & milestone confirmations"
                )
            )
            channels.forEach { db.communityDao().insertChannel(it) }

            // Initial Messages
            val messages = listOf(
                CommunityMessageEntity(
                    id = "msg_01",
                    channelId = "ch_live_releases",
                    authorName = "LaunchPulse Bot",
                    authorRole = "SYSTEM_BOT",
                    authorAvatarColor = 0xFF5865F2,
                    content = "🎉 [LIVE DEPLOYMENT] CyberBlade Arena (v1.0.4) has been approved and is NOW LIVE on Google Play Store! Milestone payout of $3,000.00 is officially unlocked.",
                    timestamp = System.currentTimeMillis() - 86400000L * 2,
                    isLiveAnnouncement = true,
                    projectTitle = "CyberBlade Arena",
                    liveUrl = "https://play.google.com/store/apps/details?id=com.apex.cyberblade"
                ),
                CommunityMessageEntity(
                    id = "msg_02",
                    channelId = "ch_announcements",
                    authorName = "Lead Developer",
                    authorRole = "DEVELOPER",
                    authorAvatarColor = 0xFF00A8FC,
                    content = "Hey @Sarah! Just uploaded Apex Player Hub build #45 to Google Play Closed Testing & submitted for Production Review. ETA 24-48 hours until live.",
                    timestamp = System.currentTimeMillis() - 3600000L * 6
                ),
                CommunityMessageEntity(
                    id = "msg_03",
                    channelId = "ch_client_chat",
                    authorName = "Sarah Jenkins",
                    authorRole = "CLIENT",
                    authorAvatarColor = 0xFF23A55A,
                    content = "Awesome! As agreed in the contract, once Apex Player Hub is live on Play Store, I'll release the remaining $2,000 immediately.",
                    timestamp = System.currentTimeMillis() - 3600000L * 4
                ),
                CommunityMessageEntity(
                    id = "msg_04",
                    channelId = "ch_payouts",
                    authorName = "LaunchPulse Bot",
                    authorRole = "SYSTEM_BOT",
                    authorAvatarColor = 0xFFFFB74D,
                    content = "💰 [PAYOUT UPDATE] Payout invoice #LP-884 for Nova Wallet ($1,400.00) was marked SETTLED upon live verification.",
                    timestamp = System.currentTimeMillis() - 86400000L * 4
                )
            )
            messages.forEach { db.communityDao().insertMessage(it) }

            // Initial Notification
            val notification = NotificationEntity(
                id = "notif_01",
                title = "🎉 CyberBlade Arena is LIVE!",
                message = "Your game CyberBlade Arena v1.0.4 is now publicly accessible on Google Play. Milestone payout unlocked.",
                timestamp = System.currentTimeMillis() - 86400000L * 2,
                isRead = false,
                type = "APP_LIVE",
                storeUrl = "https://play.google.com/store/apps/details?id=com.apex.cyberblade"
            )
            db.notificationDao().insertNotification(notification)

            // User Session
            val session = UserSessionEntity(
                id = 1,
                isLoggedIn = true,
                role = "DEVELOPER",
                email = "dev@launchpulse.studio",
                selectedContractId = contract1Id
            )
            db.userSessionDao().saveSession(session)
        }
    }
}
