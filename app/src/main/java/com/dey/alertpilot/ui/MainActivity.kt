package com.dey.alertpilot.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dey.alertpilot.data.model.ImportanceLevel
import com.dey.alertpilot.data.model.NotificationItem
import com.dey.alertpilot.data.repository.NotificationRepository
import com.dey.alertpilot.di.AppModule
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog


class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repo: NotificationRepository = AppModule.notificationRepository
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(repo) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ImportantAlertsAppScreen(viewModel = viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportantAlertsAppScreen(viewModel: MainViewModel) {
    val all by viewModel.allNotifications.collectAsState()
    val important by viewModel.importantNotifications.collectAsState()

    var showImportantOnly by remember { mutableStateOf(true) }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Important Alerts") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (showImportantOnly) "Showing: Important only" else "Showing: All",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Button(onClick = { showImportantOnly = !showImportantOnly }) {
                    Text(if (showImportantOnly) "Show all" else "Show important")
                }
            }

            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth()
            ) {
                Text("Open notification access settings")
            }

            Spacer(modifier = Modifier.height(8.dp))

            val list = if (showImportantOnly) important else all

            if (list.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No notifications captured yet.")
                }
            } else {
                NotificationList(list = list, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun NotificationList(
    list: List<NotificationItem>,
    viewModel: MainViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(list) { item ->
            NotificationCard(
                item = item,
                onOpen = {id -> viewModel.onNotificationOpened(id) },
                onDelete = {id -> viewModel.onNotificationDeleted(id) })
        }
    }
}

@Composable
fun NotificationCard(
    item: NotificationItem,
    onOpen: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    val badgeColor = when (item.importance) {
        ImportanceLevel.HIGH -> MaterialTheme.colorScheme.error
        ImportanceLevel.MEDIUM -> MaterialTheme.colorScheme.tertiary
        ImportanceLevel.LOW -> MaterialTheme.colorScheme.outline
    }

    // Background shade based on read/unread
    val containerColor =
        if (item.isRead) MaterialTheme.colorScheme.surface
        else MaterialTheme.colorScheme.surfaceVariant

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor
        ),
        onClick = { onOpen(item.id) } // mark as read + open dialog
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.title.orEmpty().ifBlank { "(no title)" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (item.isRead) FontWeight.Normal else FontWeight.SemiBold
                )
                AssistChip(
                    onClick = {},
                    label = { Text(item.importance.name) },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = badgeColor.copy(alpha = 0.2f)
                    )
                )
            }
            Spacer(Modifier.height(4.dp))
            if (!item.text.isNullOrBlank()) {
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Source: ${item.appName}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(4.dp))
            TextButton(
                onClick = { onDelete(item.id) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Delete")
            }
        }
    }
}