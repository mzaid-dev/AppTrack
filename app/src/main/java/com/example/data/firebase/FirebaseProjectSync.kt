package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.model.ProjectEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

object FirebaseProjectSync {

    data class PagedResult(
        val projects: List<ProjectEntity>,
        val lastDocument: DocumentSnapshot?,
        val hasMore: Boolean
    )

    fun isFirebaseReady(context: Context): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
    private fun mapDocumentToProject(doc: DocumentSnapshot, currentUserEmail: String = ""): ProjectEntity? {
        var rawTitle = (doc.getString("appName")
            ?: doc.getString("title")
            ?: doc.getString("name")
            ?: "").trim()
        val packageName = (doc.getString("packageName")
            ?: doc.getString("package")
            ?: "").trim()

        var rawCategory = (doc.getString("category") ?: doc.getString("type") ?: "").trim()

        // Smart Swap: If user put "APP" or "GAME" in title, and placed the real app name in category (e.g. category="WhatsApp", title="APP")
        if ((rawTitle.equals("APP", ignoreCase = true) || rawTitle.equals("GAME", ignoreCase = true)) &&
            rawCategory.isNotBlank() && !rawCategory.equals("APP", ignoreCase = true) && !rawCategory.equals("GAME", ignoreCase = true)) {
            val temp = rawTitle
            rawTitle = rawCategory
            rawCategory = temp
        }

        // If title is generic ("APP", "GAME", or blank), extract clean name from category or package
        if (rawTitle.isBlank() || rawTitle.equals("APP", ignoreCase = true) || rawTitle.equals("GAME", ignoreCase = true)) {
            if (rawCategory.isNotBlank() && !rawCategory.equals("APP", ignoreCase = true) && !rawCategory.equals("GAME", ignoreCase = true)) {
                rawTitle = rawCategory
            } else if (packageName.isNotBlank()) {
                val lastPart = packageName.substringAfterLast(".").replace("_", " ")
                rawTitle = lastPart.replaceFirstChar { it.uppercase() }
            }
        }

        // Strictly ignore empty or unconfigured documents
        if (rawTitle.isBlank() && packageName.isBlank()) return null
        val title = if (rawTitle.isNotBlank()) rawTitle else packageName

        // User / client isolation filter
        val docUserEmail = (doc.getString("userEmail")
            ?: doc.getString("clientEmail")
            ?: doc.getString("email")
            ?: "").trim().lowercase()
        val cleanCurrentEmail = currentUserEmail.trim().lowercase()

        if (cleanCurrentEmail.isNotBlank() && docUserEmail.isNotBlank() && !docUserEmail.equals("all", ignoreCase = true)) {
            if (docUserEmail != cleanCurrentEmail) {
                return null
            }
        }

        val category = if (rawCategory.contains("game", ignoreCase = true) || title.contains("game", ignoreCase = true)) "GAME" else "APP"
        // Default to PENDING until checked against Play Store (never assume LIVE)
        val rawStatus = doc.getString("status")?.takeIf { it.isNotBlank() } ?: "PENDING"
        val iconUrl = doc.getString("iconUrl") ?: doc.getString("icon") ?: ""
        val iconKey = doc.getString("iconKey") ?: (if (category == "GAME") "gamepad" else "phone")
        val liveStoreUrl = doc.getString("liveStoreUrl")?.takeIf { it.isNotBlank() }
            ?: doc.getString("playStoreUrl")?.takeIf { it.isNotBlank() }
            ?: "https://play.google.com/store/apps/details?id=$packageName"
        val contractId = doc.getString("contractId") ?: "contract_apex_01"
        val lastChecked = doc.getLong("lastCheckedTimestamp") ?: 0L

        return ProjectEntity(
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
    }

    suspend fun fetchPageFromFirestore(
        context: Context,
        pageSize: Long = 25L,
        lastDoc: DocumentSnapshot? = null,
        currentUserEmail: String = ""
    ): PagedResult? {
        if (!isFirebaseReady(context)) return null
        val cleanEmail = currentUserEmail.trim().lowercase()
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val collectionName = "apps"

            var snapshot: com.google.firebase.firestore.QuerySnapshot? = null

            // 1. Direct query with whereEqualTo for secure Firestore rule compliance
            if (cleanEmail.isNotBlank()) {
                try {
                    var emailQuery = firestore.collection(collectionName)
                        .whereEqualTo("userEmail", cleanEmail)
                        .limit(pageSize)
                    if (lastDoc != null) {
                        emailQuery = emailQuery.startAfter(lastDoc)
                    }
                    val emailSnap = try {
                        emailQuery.get(Source.SERVER).await()
                    } catch (e: Exception) {
                        emailQuery.get().await()
                    }
                    if (!emailSnap.isEmpty) {
                        snapshot = emailSnap
                    }
                } catch (e: Exception) {
                    Log.d("FirebaseSync", "Direct userEmail query note: ${e.message}")
                }
            }

            // 2. Fallback to general collection query
            if (snapshot == null) {
                var query = firestore.collection(collectionName).limit(pageSize)
                if (lastDoc != null) {
                    query = query.startAfter(lastDoc)
                }
                snapshot = try {
                    query.get(Source.SERVER).await()
                } catch (e: Exception) {
                    query.get().await()
                }
            }

            if (snapshot != null && !snapshot.isEmpty) {
                val list = snapshot.documents.mapNotNull { mapDocumentToProject(it, cleanEmail) }
                val newLastDoc = snapshot.documents.lastOrNull()
                val hasMore = snapshot.size().toLong() >= pageSize
                PagedResult(list, newLastDoc, hasMore)
            } else {
                PagedResult(emptyList(), null, false)
            }
        } catch (e: Exception) {
            Log.d("FirebaseSync", "Paged fetch note: ${e.message}")
            null
        }
    }

    suspend fun fetchFromFirestore(context: Context): List<ProjectEntity>? {
        val result = fetchPageFromFirestore(context, pageSize = 50L)
        return result?.projects
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
