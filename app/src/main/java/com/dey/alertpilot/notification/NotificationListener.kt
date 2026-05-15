package com.dey.alertpilot.notification

import android.content.pm.PackageManager
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

        // NEW: resolve human‑readable app name like "Gmail", "Facebook"
        val appName = getAppNameFromPackage(packageName)

        Log.d("NotificationListener", "New notification from $appName ($packageName): $title - $text")

        repo.addNotification(
            packageName = packageName,
            appName = appName,
            title = title,
            text = text,
            postedAtMillis = postedAt,
            isOngoing = isOngoing
        )
    }

    // Keep or ignore removals; for now we don't auto-delete from our history
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        // v1: do nothing so our own history is not cleared automatically
    }

    private fun getAppNameFromPackage(packageName: String): String {
        return try {
            val pm: PackageManager = applicationContext.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName // fallback
        }
    }
}