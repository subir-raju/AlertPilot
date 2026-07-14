package com.dey.alertpilot.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dey.alertpilot.data.model.NotificationItem
import com.dey.alertpilot.data.repository.NotificationRepository
import com.dey.alertpilot.data.api.EmailApiService
import com.dey.alertpilot.data.api.EmailSummaryDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

class MainViewModel(
    private val repository: NotificationRepository,
    private val emailApi: EmailApiService
) : ViewModel() {

    private val _allNotifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val allNotifications: StateFlow<List<NotificationItem>> = _allNotifications

    private val _importantNotifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val importantNotifications: StateFlow<List<NotificationItem>> = _importantNotifications

    init {
        viewModelScope.launch {
            repository.notifications.collectLatest { list ->
                _allNotifications.value = list
                _importantNotifications.value = repository.getImportantOnly()
            }
        }
    }
    private val _importantEmails = MutableStateFlow<List<EmailSummaryDto>>(emptyList())
    val importantEmails: StateFlow<List<EmailSummaryDto>> = _importantEmails

    fun refreshImportantEmails() {
        viewModelScope.launch {
            try {
                val emails = emailApi.getImportantEmails(limit = 20)
                _importantEmails.value = emails
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    fun getById(id: String): NotificationItem? = repository.getNotification(id)

    fun onNotificationOpened(id: String) {
        repository.markAsRead(id)
    }

    fun onNotificationDeleted(id: String) {
        repository.delete(id)
    }
}