package com.dey.alertpilot.data.repository

import com.dey.alertpilot.data.classifier.ImportanceClassifier
import com.dey.alertpilot.data.model.ImportanceLevel
import com.dey.alertpilot.data.model.NotificationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class NotificationRepository(
    private val importanceClassifier: ImportanceClassifier
) {

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()
    fun addNotification(
        packageName: String,
        appName: String,
        title: String?,
        text: String?,
        postedAtMillis: Long,
        isOngoing: Boolean
    ) {
        val importance = importanceClassifier.classify(text, title, packageName)
        val item = NotificationItem(
            id = UUID.randomUUID().toString(),
            packageName = packageName,
            appName = appName,
            title = title,
            text = text,
            postedAtMillis = postedAtMillis,
            importance = importance,
            isOngoing = isOngoing
        )
        _notifications.value = listOf(item) + _notifications.value
    }

    fun getNotification(id: String): NotificationItem? {
        return _notifications.value.firstOrNull { it.id == id }
    }

    fun getImportantOnly(): List<NotificationItem> {
        return _notifications.value.filter {
            it.importance == ImportanceLevel.HIGH ||
                    it.importance == ImportanceLevel.MEDIUM
        }
    }

    fun markAsRead(id: String) {
        _notifications.value = _notifications.value.map { item ->
            if (item.id == id) item.copy(isRead = true) else item
        }
    }

    fun delete(id: String) {
        _notifications.value = _notifications.value.filterNot { it.id == id }
        // If you prefer soft delete:
        // _notifications.value = _notifications.value.map { item ->
        //     if (item.id == id) item.copy(isDeleted = true) else item
        // }
    }
}