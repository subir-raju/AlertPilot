package com.dey.alertpilot.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.dey.alertpilot.di.AppModule

class NotificationListener : NotificationListenerService() {

    private val repo by lazy { AppModule.notificationRepository }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        // Ignore our own app's notifications (safety)
        if (sbn.packageName == packageName) return

        val notification = sbn.notification

        // Ignore group summary notifications (they cause duplicates)
        val isGroupSummary =
            (notification.flags and android.app.Notification.FLAG_GROUP_SUMMARY) != 0

        // Ignore ongoing notifications like music players if you want
        if (isGroupSummary || sbn.isOngoing) return

        val extras = notification.extras
        val title = extras.getString(android.app.Notification.EXTRA_TITLE)
        val text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString()
        val packageName = sbn.packageName
        val postedAt = sbn.postTime
        val isOngoing = sbn.isOngoing

        Log.d("NotificationListener", "New notification from $packageName: $title - $text")

        repo.addNotification(
            packageName = packageName,
            title = title,
            text = text,
            postedAtMillis = postedAt,
            isOngoing = isOngoing
        )
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        // For v1 we don't need removal logic, but you could mark items as dismissed here
    }
}