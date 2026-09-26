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

            // Projects are dynamically synced from Firebase Firestore

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

            // Default User Session (Initial unauthenticated state)
            val session = UserSessionEntity(
                id = 1,
                isLoggedIn = false,
                role = "CLIENT",
                email = "",
                selectedContractId = ""
            )
            db.userSessionDao().saveSession(session)
        }
    }
}
