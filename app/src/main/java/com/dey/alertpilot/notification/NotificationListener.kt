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
        val packageName = sbn.packageName
        val postedAt = sbn.postTime
        val isOngoing = sbn.isOngoing

        // 1. Extract standard text fields
        val text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)
        val bigText = extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT)
        val subText = extras.getCharSequence(android.app.Notification.EXTRA_SUB_TEXT)
        val textLines = extras.getCharSequenceArray(android.app.Notification.EXTRA_TEXT_LINES)

        // 2. Extract MessagingStyle content (common for WhatsApp, Telegram, etc.)
        val messagingText = try {
            val messages = extras.getParcelableArray(android.app.Notification.EXTRA_MESSAGES)
            if (messages != null && messages.isNotEmpty()) {
                messages.joinToString("\n") { messageBundle ->
                    if (messageBundle is android.os.Bundle) {
                        val sender = messageBundle.getCharSequence("sender")
                        val msgText = messageBundle.getCharSequence("text")
                        if (sender != null) "$sender: $msgText" else msgText ?: ""
                    } else ""
                }
            } else null
        } catch (e: Exception) {
            null
        }

        // 3. Build the full body by prioritizing the most "detailed" fields
        val fullBodyText = buildString {
            // Priority 1: Messaging style (full conversation context)
            if (!messagingText.isNullOrBlank()) {
                append(messagingText)
            } 
            
            // Priority 2: BigText (the expanded full message)
            if (!bigText.isNullOrBlank() && !this.contains(bigText)) {
                if (this.isNotEmpty()) append("\n---\n")
                append(bigText)
            } 
            
            // Priority 3: Normal text (if not already covered)
            if (!text.isNullOrBlank() && !this.contains(text)) {
                if (this.isNotEmpty()) append("\n")
                append(text)
            }

            // Priority 4: Extra lines (common in multi-message notifications or emails)
            if (textLines != null && textLines.isNotEmpty()) {
                for (line in textLines) {
                    if (!line.isNullOrBlank() && !this.contains(line)) {
                        append("\n").append(line)
                    }
                }
            }

            // Priority 5: Subtext (often contains category or sender info)
            if (!subText.isNullOrBlank() && !this.contains(subText)) {
                append("\n(").append(subText).append(")")
            }
        }.trim()

        // NEW: resolve human‑readable app name like "Gmail", "Facebook"
        val appName = getAppNameFromPackage(packageName)

        Log.d("NotificationListener", "New notification from $appName ($packageName)")
        Log.d("NotificationListener", "Title: $title")
        Log.d("NotificationListener", "Full Text: $fullBodyText")

        repo.addNotification(
            packageName = packageName,
            appName = appName,
            title = title,
            text = fullBodyText,
            postedAtMillis = postedAt,
            isOngoing = isOngoing
        )
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        // IMPORTANT: do not remove from repo here – we keep history
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