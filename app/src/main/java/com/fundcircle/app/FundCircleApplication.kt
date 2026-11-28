package com.fundcircle.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FundCircleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        // minSdk is 26, so notification channels are always available.
        val channel = NotificationChannel(
            CHANNEL_ID_TRANSACTIONS,
            getString(R.string.notification_channel_transactions_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.notification_channel_transactions_desc)
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID_TRANSACTIONS = "circle_transactions"
    }
}

