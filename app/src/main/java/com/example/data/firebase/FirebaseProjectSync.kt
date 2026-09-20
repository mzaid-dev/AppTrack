package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.model.ProjectEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

object FirebaseProjectSync {

    fun isFirebaseReady(context: Context): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun fetchFromFirestore(context: Context): List<ProjectEntity>? {
        if (!isFirebaseReady(context)) return null
        return try {
            val firestore = FirebaseFirestore.getInstance()
            // Check "apps" collection first, then "projects"
            var snapshot = firestore.collection("apps").get().await()
            if (snapshot.isEmpty) {
                snapshot = firestore.collection("projects").get().await()
            }

            if (!snapshot.isEmpty) {
                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        val title = doc.getString("appName")
                            ?: doc.getString("title")
                            ?: doc.getString("name")
                            ?: "Untitled"
                        val packageName = doc.getString("packageName")
                            ?: doc.getString("package")
                            ?: return@mapNotNull null
                        val rawCategory = doc.getString("category") ?: doc.getString("type") ?: "APP"
                        val category = if (rawCategory.contains("game", ignoreCase = true)) "GAME" else "APP"
                        val rawStatus = doc.getString("status") ?: "CHECKING"
                        val iconUrl = doc.getString("iconUrl") ?: doc.getString("icon") ?: ""
                        val iconKey = doc.getString("iconKey") ?: (if (category == "GAME") "gamepad" else "phone")
                        val liveStoreUrl = doc.getString("liveStoreUrl") ?: doc.getString("playStoreUrl") ?: ""
                        val contractId = doc.getString("contractId") ?: "contract_apex_01"
                        val lastChecked = doc.getLong("lastCheckedTimestamp") ?: 0L

                        ProjectEntity(
                            id = doc.id,
                            title = title,
                            category = category,
                            packageName = packageName,
                            status = rawStatus,
                            iconUrl = iconUrl,
                            iconKey = iconKey,
                            liveStoreUrl = liveStoreUrl,
                            contractId = contractId,
                            lastCheckedTimestamp = lastChecked
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                list.ifEmpty { null }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.d("FirebaseSync", "Firestore fetch note: ${e.message}")
            null
        }
    }

    suspend fun syncProjectToFirestore(context: Context, project: ProjectEntity) {
        if (!isFirebaseReady(context)) return
        try {
            val firestore = FirebaseFirestore.getInstance()
            val data = hashMapOf(
                "title" to project.title,
                "category" to project.category,
                "packageName" to project.packageName,
                "status" to project.status,
                "iconUrl" to project.iconUrl,
                "iconKey" to project.iconKey,
                "liveStoreUrl" to project.liveStoreUrl,
                "lastCheckedTimestamp" to project.lastCheckedTimestamp,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("projects").document(project.id).set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.d("FirebaseSync", "Sync to firestore skipped: ${e.message}")
        }
    }
}
