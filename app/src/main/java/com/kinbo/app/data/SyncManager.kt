package com.kinbo.app.data

import android.util.Log

/**
 * Picks the active [ListRepository] at runtime.
 *
 * - If Firebase is configured (google-services.json present), returns [FirestoreSyncRepository]
 *   → real-time cloud sync across devices.
 * - Otherwise returns [KinboRepository] → local in-memory store (offline-first).
 *
 * The ViewModel never references either concrete class; it talks to [ListRepository].
 * This means switching from local → cloud requires zero UI changes.
 */
object SyncManager {

    private const val TAG = "KinboSync"

    @Volatile
    private var instance: ListRepository? = null

    fun get(): ListRepository {
        instance?.let { return it }
        val repo = if (FirestoreSyncRepository.isConfigured()) {
            Log.i(TAG, "Firebase configured — using FirestoreSyncRepository (real-time collaboration active)")
            FirestoreSyncRepository()
        } else {
            Log.i(TAG, "Firebase not configured — using KinboRepository (local/offline mode)")
            KinboRepository()
        }
        instance = repo
        return repo
    }

    /** Whether the app is syncing to the cloud in real time. */
    fun isSynced(): Boolean = get().isSynced
}
