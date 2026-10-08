package com.trex.liferhythm.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Saves recent nights every few hours. Android keeps usage history for only
 * a few days, so this is what builds up your long-term history.
 */
class CollectWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        return try {
            val store = Store(applicationContext)
            val snap = Collector.compute(applicationContext, store.config())
            store.saveAll(snap.results.map { it.toRecord() })
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        fun schedule(ctx: Context) {
            val request = PeriodicWorkRequestBuilder<CollectWorker>(6, TimeUnit.HOURS).build()
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                "collect", ExistingPeriodicWorkPolicy.KEEP, request
            )
        }
    }
}