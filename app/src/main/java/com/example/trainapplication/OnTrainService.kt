package com.example.trainapplication

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat


class OnTrainService : Service() {
    private val NOTIF_ID = 1
    private val NOTIF_CHANNEL_ID = "Train Service"

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // do your jobs here

        startForeground()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForeground() {
        val notifIntent = Intent(this, OnTrainTestActivity::class.java)
        notifIntent.setAction(Intent.ACTION_MAIN)
        notifIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        notifIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            notifIntent, PendingIntent.FLAG_IMMUTABLE
        )

        startForeground(
            NOTIF_ID, NotificationCompat.Builder(
                this,
                NOTIF_CHANNEL_ID
            ) // don't forget create a notification channel first
                .setOngoing(true)
                .setSmallIcon(R.drawable.eastwestcircle)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Service is running background")
                .setContentIntent(pendingIntent)
                .build()
        )
    }
}