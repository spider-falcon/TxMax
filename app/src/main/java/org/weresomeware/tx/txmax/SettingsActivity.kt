package org.weresomeware.tx.txmax

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import database.HistoryDatabase
import kotlinx.coroutines.launch
import org.weresomeware.tx.txmax.ui.theme.TxMaxTheme

@OptIn(ExperimentalMaterial3Api::class)
class SettingsActivity : ComponentActivity() {

    private lateinit var historyDb: HistoryDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        historyDb = HistoryDatabase(this)

        setContent {
            TxMaxTheme {
                val context = LocalContext.current
                val activity = LocalActivity.current
                val scrollState = rememberScrollState()



                val sharedPreferences = remember {
                    context.getSharedPreferences("tx_max_settings", Context.MODE_PRIVATE)
                }

                val systemInDarkTheme = isSystemInDarkTheme()

                var isDarkMode by remember {
                    mutableStateOf(
                        if (sharedPreferences.contains("dark_mode")) {
                            sharedPreferences.getBoolean("dark_mode", false)
                        } else {
                            systemInDarkTheme
                        }
                    )
                }

                var selectedVoice by remember {
                    mutableStateOf(sharedPreferences.getString("voice_engine", "Default System") ?: "Default System")
                }
                var fontSizeSelection by remember {
                    mutableStateOf(sharedPreferences.getString("font_size", "Normal") ?: "Normal")
                }
                var offlineVoiceFirst by remember {
                    mutableStateOf(sharedPreferences.getBoolean("offline_voice_first", false))
                }

                var showVoiceDialog by remember { mutableStateOf(false) }
                var showFontDialog by remember { mutableStateOf(false) }
                var showClearDbDialog by remember { mutableStateOf(false) }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Settings", fontWeight = FontWeight.Bold) },
                            navigationIcon = {
                                IconButton(onClick = { activity?.finish() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .verticalScroll(scrollState)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        CategoryHeader(title = "Appearance")

                        SettingsSwitchRow(
                            icon = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                            title = "Dark Theme",
                            subtitle = "Enable system-wide dark mode styling",
                            checked = isDarkMode,
                            onCheckedChange = { checked ->
                                isDarkMode = checked
                                sharedPreferences.edit { putBoolean("dark_mode", checked) }
                            }
                        )

                        SettingsClickableRow(
                            icon = Icons.Default.FormatSize,
                            title = "Font Size",
                            subtitle = "Current setting: $fontSizeSelection",
                            onClick = { showFontDialog = true }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        CategoryHeader(title = "Voice & Engine Configuration")

                        SettingsClickableRow(
                            icon = Icons.Default.RecordVoiceOver,
                            title = "Text-to-Speech Voice",
                            subtitle = selectedVoice,
                            onClick = { showVoiceDialog = true }
                        )

                        SettingsSwitchRow(
                            icon = Icons.Default.CloudOff,
                            title = "Offline Recognition First",
                            subtitle = "Force localized voice dictation models over web lookups",
                            checked = offlineVoiceFirst,
                            onCheckedChange = { checked ->
                                offlineVoiceFirst = checked
                                sharedPreferences.edit { putBoolean("offline_voice_first", checked) }
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        CategoryHeader(title = "Data Management")

                        SettingsClickableRow(
                            icon = Icons.Default.DeleteForever,
                            title = "Clear Chat History",
                            subtitle = "Irreversibly wipe local storage caching logs",
                            onClick = { showClearDbDialog = true }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        CategoryHeader(title = "System Information")

                        SettingsInfoRow(
                            icon = Icons.Default.Info,
                            title = "Application Version",
                            value = "v1.4.2-Release"
                        )
                        SettingsInfoRow(
                            icon = Icons.Default.DeveloperMode,
                            title = "Environment",
                            value = "Production-TX"
                        )
                    }
                }

                if (showFontDialog) {
                    val fontOptions = listOf("Small", "Normal", "Large", "Extra Large")
                    SelectionDialog(
                        title = "Select Font Size",
                        options = fontOptions,
                        selectedOption = fontSizeSelection,
                        onOptionSelected = { option ->
                            fontSizeSelection = option
                            sharedPreferences.edit { putString("font_size", option) }
                            showFontDialog = false
                        },
                        onDismissRequest = { showFontDialog = false }
                    )
                }

                if (showVoiceDialog) {
                    val voiceOptions = listOf("Default System", "TX-Max Premium Male", "TX-Max Premium Female", "Neural English Echo")
                    SelectionDialog(
                        title = "Select TTS Voice Profile",
                        options = voiceOptions,
                        selectedOption = selectedVoice,
                        onOptionSelected = { option ->
                            selectedVoice = option
                            sharedPreferences.edit { putString("voice_engine", option) }
                            showVoiceDialog = false
                        },
                        onDismissRequest = { showVoiceDialog = false }
                    )
                }

                if (showClearDbDialog) {
                    AlertDialog(
                        onDismissRequest = { showClearDbDialog = false },
                        icon = { Icon(Icons.Default.Warning, contentDescription = "Warning", tint = MaterialTheme.colorScheme.error) },
                        title = { Text("Wipe Local Storage Data?") },
                        text = { Text("This operation is final. It drops local cache nodes, wipes standard SQLite transaction schemas, and clears conversation structures permanently.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    lifecycleScope.launch {
                                        historyDb.clearAllHistory()
                                        Toast.makeText(context, "Chat logs dropped and cleared successfully.", Toast.LENGTH_LONG).show()
                                        showClearDbDialog = false
                                    }
                                }
                            ) {
                                Text("Clear Everything", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showClearDbDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}

// --- CORE REUSABLE COMPOSABLE RENDERING SUBCOMPONENTS ---

@Composable
fun CategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 6.dp)
    )
}

@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsClickableRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SettingsInfoRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SelectionDialog(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = {
            Column(Modifier.selectableGroup()) {
                options.forEach { text ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (text == selectedOption),
                                onClick = { onOptionSelected(text) },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (text == selectedOption),
                            onClick = null
                        )
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) { Text("Dismiss") }
        }
    )
}