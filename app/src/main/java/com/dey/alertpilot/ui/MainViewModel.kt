package com.dey.alertpilot.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dey.alertpilot.data.model.NotificationItem
import com.dey.alertpilot.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: NotificationRepository
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

    fun getById(id: String): NotificationItem? = repository.getNotification(id)
}