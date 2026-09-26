package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CommunityChannelEntity
import com.example.data.model.CommunityMessageEntity
import com.example.data.model.ContractEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.UserSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContractDao {
    @Query("SELECT * FROM contracts ORDER BY createdDate DESC")
    fun getAllContracts(): Flow<List<ContractEntity>>

    @Query("SELECT * FROM contracts WHERE id = :id")
    fun getContractById(id: String): Flow<ContractEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContract(contract: ContractEntity)

    @Update
    suspend fun updateContract(contract: ContractEntity)

    @Query("DELETE FROM contracts WHERE id = :id")
    suspend fun deleteContract(id: String)
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY rowid ASC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE category = :category ORDER BY rowid ASC")
    fun getProjectsByCategory(category: String): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE contractId = :contractId ORDER BY rowid ASC")
    fun getProjectsByContract(contractId: String): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE contractId = :contractId AND category = :category ORDER BY rowid ASC")
    fun getProjectsByContractAndCategory(contractId: String, category: String): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun getProjectById(id: String): Flow<ProjectEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<ProjectEntity>)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("UPDATE projects SET status = :status, liveStoreUrl = :liveUrl, lastCheckedTimestamp = :timestamp, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateProjectLiveStatus(id: String, status: String, liveUrl: String, timestamp: Long, updatedAt: Long)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProject(id: String)

    @Query("DELETE FROM projects")
    suspend fun clearAllProjects()
}

@Dao
interface CommunityDao {
    @Query("SELECT * FROM community_channels")
    fun getAllChannels(): Flow<List<CommunityChannelEntity>>

    @Query("SELECT * FROM community_messages WHERE channelId = :channelId ORDER BY timestamp ASC")
    fun getMessagesForChannel(channelId: String): Flow<List<CommunityMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: CommunityChannelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: CommunityMessageEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notifications")
    suspend fun clearAll()
}

@Dao
interface UserSessionDao {
    @Query("SELECT * FROM user_sessions WHERE id = 1")
    fun getSession(): Flow<UserSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: UserSessionEntity)

    @Query("DELETE FROM user_sessions")
    suspend fun clearSession()
}
