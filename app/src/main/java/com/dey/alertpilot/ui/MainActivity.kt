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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dey.alertpilot.data.model.ImportanceLevel
import com.dey.alertpilot.data.model.NotificationItem
import com.dey.alertpilot.data.repository.NotificationRepository
import com.dey.alertpilot.di.AppModule
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings


class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repo: NotificationRepository = AppModule.notificationRepository
                val emailApi = AppModule.emailApi
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(repo, emailApi) as T
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
        val list = if (showImportantOnly) important else all

        if (all.isEmpty()) {
            Box(modifier = Modifier.padding(padding)) {
                EmptyNotificationsScreen()
                
                // Keep the settings icon even when empty so users can grant permissions
                var showMenu by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Notification access") },
                            onClick = {
                                showMenu = false
                                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                // Settings Icon Row
                var showMenu by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Notification access") },
                            onClick = {
                                showMenu = false
                                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                context.startActivity(intent)
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (showImportantOnly) "Showing: Priority" else "Showing: Everything",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Button(onClick = { showImportantOnly = !showImportantOnly }) {
                        Text(if (showImportantOnly) "Show: Everything" else "Show: Priority")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (list.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No priority notifications found.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    NotificationList(list = list, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun EmptyNotificationsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            modifier = Modifier.size(150.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "No Notifications",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Stay Informed With Instant Notifications About Any Updates Or Changes To Your Plans.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun NotificationList(
    list: List<NotificationItem>,
    viewModel: MainViewModel
) {
    var selected by remember { mutableStateOf<NotificationItem?>(null) }

    // Dialog showing details
    selected?.let { item ->
        AlertDialog(
            onDismissRequest = { selected = null },
            title = {
                Text(item.title.orEmpty().ifBlank { "(no title)" })
            },
            text = {
                Column {
                    Text(text = "Source: ${item.appName}")
                    Spacer(Modifier.height(4.dp))
                    Text(text = "Package: ${item.packageName}")
                    Spacer(Modifier.height(4.dp))
                    Text(text = "Received: ${java.util.Date(item.postedAtMillis)}")
                    Spacer(Modifier.height(8.dp))
                    if (!item.text.isNullOrBlank()) {
                        Text(text = item.text)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selected = null }) {
                    Text("Close")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(list) { item ->
            NotificationCard(
                item = item,
                onOpen = { id ->
                    viewModel.onNotificationOpened(id) // mark as read in repo
                    selected = item                    // open dialog
                },
                onDelete = { id ->
                    viewModel.onNotificationDeleted(id)
                    if (selected?.id == id) selected = null
                }
            )
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
                    fontWeight = if (item.isRead) FontWeight.Normal else FontWeight.SemiBold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                AssistChip(
                    onClick = {},
                    label = { Text(item.importance.name) },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = badgeColor.copy(alpha = 0.2f)
                    )
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Source: ${item.appName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { onDelete(item.id) }
                ) {
                    Text("Delete")
                }
            }
        }
    }
}
