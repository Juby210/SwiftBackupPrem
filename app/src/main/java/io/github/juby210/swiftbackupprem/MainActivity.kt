package io.github.juby210.swiftbackupprem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.juby210.swiftbackupprem.ui.MainViewModel
import io.github.juby210.swiftbackupprem.ui.component.AboutScreen
import io.github.juby210.swiftbackupprem.ui.component.GuidedSetupWizard
import io.github.juby210.swiftbackupprem.ui.component.SettingsSwitch
import io.github.juby210.swiftbackupprem.ui.theme.Theme
import io.github.juby210.swiftbackupprem.util.AppUtils
import io.github.juby210.swiftbackupprem.util.PreferencesManager

enum class AppScreen {
    Settings, About
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val prefs: PreferencesManager? = try {
            @Suppress("DEPRECATION")
            PreferencesManager(getSharedPreferences("${BuildConfig.APPLICATION_ID}_preferences", MODE_WORLD_READABLE))
        } catch (e: Throwable) {
            null
        }

        setContent {
            val context = LocalContext.current

            var currentScreen by remember { mutableStateOf(AppScreen.Settings) }
            var showMenu by remember { mutableStateOf(false) }

            BackHandler(enabled = currentScreen != AppScreen.Settings) {
                currentScreen = AppScreen.Settings
            }

            val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
                if (uri != null && prefs != null) {
                    viewModel.exportConfig(contentResolver, uri, prefs)
                }
            }

            val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                if (uri != null && prefs != null) {
                    viewModel.importConfig(contentResolver, uri, prefs)
                }
            }

            Theme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = when (currentScreen) {
                                        AppScreen.Settings -> "SwiftBackupPrem"
                                        AppScreen.About -> "About"
                                    }
                                )
                            },
                            navigationIcon = {
                                if (currentScreen != AppScreen.Settings) {
                                    IconButton(onClick = { currentScreen = AppScreen.Settings }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Settings")
                                    }
                                }
                            },
                            actions = {
                                if (currentScreen == AppScreen.Settings) {
                                    IconButton(onClick = { showMenu = true }) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "Menu Options")
                                    }
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Export Config") },
                                            onClick = {
                                                showMenu = false
                                                exportLauncher.launch("sbp_config.json")
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Default.Upload, contentDescription = "Export")
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Import Config") },
                                            onClick = {
                                                showMenu = false
                                                importLauncher.launch("application/json")
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Default.Download, contentDescription = "Import")
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("About") },
                                            onClick = {
                                                showMenu = false
                                                currentScreen = AppScreen.About
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Default.Info, contentDescription = "About")
                                            }
                                        )
                                    }
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                        AnimatedContent(
                            targetState = currentScreen,
                            label = "ScreenTransition"
                        ) { screen ->
                            when (screen) {
                                AppScreen.About -> AboutScreen()
                                AppScreen.Settings -> {
                                    if (prefs == null) {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Warning,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                                    )
                                                    Text(
                                                        text = "LSPosed Module Not Enabled",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        color = MaterialTheme.colorScheme.onErrorContainer
                                                    )
                                                }
                                                Text(
                                                    text = "Enable SwiftBackupPrem in LSPosed manager and restart device.",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                                Button(
                                                    onClick = { finishAndRemoveTask() },
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Close App")
                                                }
                                            }
                                        }
                                    } else {
                                        Column(modifier = Modifier.fillMaxSize()) {
                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxWidth()
                                                    .verticalScroll(rememberScrollState())
                                                    .padding(vertical = 8.dp)
                                            ) {
                                                ElevatedCard(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                                    shape = RoundedCornerShape(16.dp),
                                                    colors = CardDefaults.elevatedCardColors(
                                                        containerColor = if (prefs.customFirebaseApp)
                                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                        else
                                                            MaterialTheme.colorScheme.surface
                                                    )
                                                ) {
                                                    Column {
                                                        SettingsSwitch(
                                                            label = "Custom firebase app",
                                                            secondaryLabel = "Recommended, forces Swift Backup to use your own firebase credentials",
                                                            pref = prefs.customFirebaseApp,
                                                            onPrefChange = { prefs.customFirebaseApp = it }
                                                        )

                                                        AnimatedVisibility(
                                                            visible = prefs.customFirebaseApp,
                                                            enter = expandVertically() + fadeIn(),
                                                            exit = shrinkVertically() + fadeOut()
                                                        ) {
                                                            Column(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(bottom = 8.dp)
                                                            ) {
                                                                HorizontalDivider(
                                                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                                                )
                                                                GuidedSetupWizard(
                                                                    prefs = prefs
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            Surface(
                                                tonalElevation = 3.dp,
                                                shadowElevation = 4.dp,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Button(
                                                        onClick = { AppUtils.forceStopSwiftBackup(context) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                                        modifier = Modifier.weight(1f).heightIn(min = 46.dp),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                                    ) {
                                                        Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(18.dp))
                                                        Spacer(Modifier.width(6.dp))
                                                        Text("Force Stop", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, softWrap = true)
                                                    }

                                                    Button(
                                                        onClick = { AppUtils.openSwiftBackup(context) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                        modifier = Modifier.weight(1f).heightIn(min = 46.dp),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                                    ) {
                                                        Icon(Icons.AutoMirrored.Filled.Launch, contentDescription = null, modifier = Modifier.size(18.dp))
                                                        Spacer(Modifier.width(6.dp))
                                                        Text("Open App", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, softWrap = true)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
