package com.bikeshare.app.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bikeshare.app.MainActivity
import com.bikeshare.app.R

class FreeTimeNotificationWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val bikeNumber = inputData.getInt(KEY_BIKE_NUMBER, 0)
        if (bikeNumber <= 0) return Result.success()
        showNotification(applicationContext, bikeNumber)
        return Result.success()
    }

    private fun showNotification(context: Context, bikeNumber: Int) {
        createChannel(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_wb)
            .setContentTitle(context.getString(R.string.notification_free_time_title))
            .setContentText(context.getString(R.string.notification_free_time_text, bikeNumber))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(buildOpenRentalsIntent(context, bikeNumber))
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_BASE + bikeNumber, notification)
    }

    private fun buildOpenRentalsIntent(context: Context, bikeNumber: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        intent.setPackage(context.packageName)
        intent.putExtra(EXTRA_NAVIGATE_TO, DESTINATION_RENTALS)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        return PendingIntent.getActivity(
            context,
            bikeNumber,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_rental),
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val KEY_BIKE_NUMBER = "bike_number"
        const val WORK_NAME_PREFIX = "free_time_notification_"
        const val EXTRA_NAVIGATE_TO = "navigate_to"
        const val DESTINATION_RENTALS = "rentals"
        private const val CHANNEL_ID = "bike_rental"
        private const val NOTIFICATION_ID_BASE = 2000
    }
}
