package com.example.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

data class AppCheckResult(
    val packageName: String,
    val isLive: Boolean,
    val statusCode: Int,
    val statusText: String, // "LIVE", "NOT LIVE", "OFFLINE"
    val storeUrl: String,
    val checkedAt: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)

object LiveStatusChecker {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .followRedirects(true)
        .retryOnConnectionFailure(true)
        .build()

    /**
     * Checks if an Android application or game is published and live on Google Play
     * by querying the Play Store package details endpoint with a standard User-Agent.
     */
    suspend fun checkAppLive(packageName: String): AppCheckResult = withContext(Dispatchers.IO) {
        val trimmed = packageName.trim()
        val playStoreUrl = "https://play.google.com/store/apps/details?id=$trimmed&hl=en&gl=US"
        try {
            val request = Request.Builder()
                .url(playStoreUrl)
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                )
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val code = response.code
                if (code == 200) {
                    val peek = response.peekBody(16384).string()
                    val isNotFound = peek.contains(
                        "We're sorry, the requested URL was not found on this server",
                        ignoreCase = true
                    ) || peek.contains("item not found", ignoreCase = true)

                    if (!isNotFound) {
                        AppCheckResult(
                            packageName = trimmed,
                            isLive = true,
                            statusCode = 200,
                            statusText = "LIVE",
                            storeUrl = playStoreUrl
                        )
                    } else {
                        AppCheckResult(
                            packageName = trimmed,
                            isLive = false,
                            statusCode = 404,
                            statusText = "NOT FOUND",
                            storeUrl = playStoreUrl
                        )
                    }
                } else if (code == 404) {
                    AppCheckResult(
                        packageName = trimmed,
                        isLive = false,
                        statusCode = 404,
                        statusText = "NOT FOUND",
                        storeUrl = playStoreUrl
                    )
                } else {
                    // 5xx server error, rate limit, or unexpected response code
                    AppCheckResult(
                        packageName = trimmed,
                        isLive = false,
                        statusCode = code,
                        statusText = "UNABLE TO CHECK",
                        storeUrl = playStoreUrl
                    )
                }
            }
        } catch (e: Exception) {
            AppCheckResult(
                packageName = trimmed,
                isLive = false,
                statusCode = -1,
                statusText = "UNABLE TO CHECK",
                errorMessage = e.message,
                storeUrl = playStoreUrl
            )
        }
    }

    /**
     * Executes parallel status checking across all package names concurrently.
     * Uses coroutines supervisorScope + async(Dispatchers.IO) so that even 50+ items
     * run simultaneously on the background thread pool and complete in seconds.
     */
    suspend fun checkAllInParallel(
        packageNames: List<String>,
        onItemChecked: (packageName: String, result: AppCheckResult, completed: Int, total: Int) -> Unit = { _, _, _, _ -> }
    ): Map<String, AppCheckResult> = supervisorScope {
        val distinctPackages = packageNames.filter { it.isNotBlank() }.distinct()
        val total = distinctPackages.size
        if (total == 0) return@supervisorScope emptyMap()

        val counter = AtomicInteger(0)

        val tasks = distinctPackages.map { pkg ->
            async(Dispatchers.IO) {
                val result = checkAppLive(pkg)
                val completed = counter.incrementAndGet()
                onItemChecked(pkg, result, completed, total)
                pkg to result
            }
        }

        tasks.mapNotNull {
            try {
                it.await()
            } catch (e: Exception) {
                null
            }
        }.toMap()
    }
}
