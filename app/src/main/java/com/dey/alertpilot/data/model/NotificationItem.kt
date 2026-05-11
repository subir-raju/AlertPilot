package com.dey.alertpilot.data.model
data class NotificationItem(
    val id: String,
    val packageName: String,
    val title: String?,
    val text: String?,
    val postedAtMillis: Long,
    val importance: ImportanceLevel,
    val isOngoing: Boolean
)