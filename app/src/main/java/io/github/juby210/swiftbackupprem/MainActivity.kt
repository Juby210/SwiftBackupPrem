package io.github.juby210.swiftbackupprem

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import io.github.juby210.swiftbackupprem.ui.component.SettingsSwitch
import io.github.juby210.swiftbackupprem.ui.component.SettingsTextField
import io.github.juby210.swiftbackupprem.ui.theme.Theme
import io.github.juby210.swiftbackupprem.util.PreferencesManager
import kotlinx.coroutines.*
import org.json.JSONObject
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    @SuppressLint("WorldReadableFiles")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val prefs: PreferencesManager
        try {
            @Suppress("DEPRECATION")
            prefs = PreferencesManager(getSharedPreferences(BuildConfig.APPLICATION_ID + "_preferences", MODE_WORLD_READABLE))
        } catch (e: Throwable) {
            Toast.makeText(this, "Enable module in LSPosed manager before using it", Toast.LENGTH_SHORT).show()
            finishAndRemoveTask()
            exitProcess(0)
        }

        setContent {
            Theme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("SwiftBackupPrem") }
                        )
                    }
                ) { paddingValues ->
                    Column(modifier = Modifier.padding(paddingValues).fillMaxSize().verticalScroll(state = rememberScrollState())) {
                        SettingsSwitch(
                            label = "Custom firebase app",
                            secondaryLabel = "Recommended, forces Swift Backup to use your own firebase credentials",
                            pref = prefs.customFirebaseApp,
                            onPrefChange = { prefs.customFirebaseApp = it }
                        )
                        if (prefs.customFirebaseApp) {
                            SettingsTextField(
                                label = "Google App ID",
                                pref = prefs.googleAppId,
                                onPrefChange = { prefs.googleAppId = it }
                            )
                            SettingsTextField(
                                label = "Google Api Key",
                                pref = prefs.googleApiKey,
                                onPrefChange = { prefs.googleApiKey = it }
                            )
                            SettingsTextField(
                                label = "Firebase Database URL",
                                pref = prefs.firebaseDatabaseUrl,
                                onPrefChange = { prefs.firebaseDatabaseUrl = it }
                            )
                            SettingsTextField(
                                label = "GCM Default Sender ID",
                                pref = prefs.gcmDefaultSenderId,
                                onPrefChange = { prefs.gcmDefaultSenderId = it }
                            )
                            SettingsTextField(
                                label = "Google Storage Bucket",
                                pref = prefs.googleStorageBucket,
                                onPrefChange = { prefs.googleStorageBucket = it }
                            )
                            SettingsTextField(
                                label = "Project ID",
                                pref = prefs.projectId,
                                onPrefChange = { prefs.projectId = it }
                            )
                            SettingsTextField(
                                label = "Client ID",
                                pref = prefs.clientId,
                                onPrefChange = { prefs.clientId = it }
                            )

                            val pickJson = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                                if (it != null) {
                                    contentResolver.openInputStream(it)?.use { inputStream ->
                                        try {
                                            val json = JSONObject(inputStream.bufferedReader().use { r -> r.readText() })
                                            with(prefs) {
                                                with(json.getJSONArray("client").getJSONObject(0)) {
                                                    googleAppId = getJSONObject("client_info").getString("mobilesdk_app_id")
                                                    googleApiKey = getJSONArray("api_key").getJSONObject(0).getString("current_key")
                                                }
                                                with(json.getJSONObject("project_info")) {
                                                    firebaseDatabaseUrl = getString("firebase_url")
                                                    gcmDefaultSenderId = getString("project_number")
                                                    googleStorageBucket = getString("storage_bucket")
                                                    projectId = getString(Consts.projectId)
                                                }
                                                if (json.has(Consts.oauthClientId)) clientId = json.getString(Consts.oauthClientId)
                                            }
                                        } catch (e: Throwable) {
                                            Toast.makeText(this@MainActivity, "Failed to parse json\n$e", Toast.LENGTH_LONG).show()
                                            Log.e("SBP", "Failed to parse json", e)
                                        }
                                    }
                                }
                            }
                            Button(
                                onClick = { pickJson.launch("application/json") },
                                modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp).fillMaxWidth().height(40.dp)
                            ) {
                                Text("Import from google-services.json")
                            }

                            val uriHandler = LocalUriHandler.current
                            Button(
                                onClick = { uriHandler.openUri("https://console.firebase.google.com/u/0/") },
                                modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp).fillMaxWidth().height(40.dp)
                            ) {
                                Text("Open Firebase Console")
                            }

                            Button(
                                onClick = { uriHandler.openUri("https://console.developers.google.com/") },
                                modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp).fillMaxWidth().height(40.dp)
                            ) {
                                Text("Open Google Developer Console")
                            }

                            val clip = LocalClipboardManager.current
                            Button(
                                onClick = {
                                    clip.setText(
                                        AnnotatedString(
                                            "{\n" +
                                                    "  \"rules\": {\n" +
                                                    "    \"users\": {\n" +
                                                    "      \"\$uid\": {\n" +
                                                    "        \".read\": \"\$uid === auth.uid\",\n" +
                                                    "        \".write\": \"\$uid === auth.uid\"\n" +
                                                    "      }\n" +
                                                    "    }\n" +
                                                    "  }\n" +
                                                    "}"
                                        )
                                    )
                                },
                                modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp).fillMaxWidth().height(40.dp)
                            ) {
                                Text("Copy database rules")
                            }

                            Button(
                                onClick = { clip.setText(AnnotatedString(Consts.packageName)) },
                                modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp).fillMaxWidth().height(40.dp)
                            ) {
                                Text("Copy Swift Backup package name")
                            }

                            Button(
                                onClick = { clip.setText(AnnotatedString(randomFingerprint())) },
                                modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp).fillMaxWidth().height(40.dp)
                            ) {
                                Text("Copy random fingerprint")
                            }

                            Button(
                                onClick = { uriHandler.openUri("https://console.cloud.google.com/apis/library/drive.googleapis.com?project=${prefs.projectId}") },
                                modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp).fillMaxWidth().height(40.dp)
                            ) {
                                Text("Enable Google Drive API")
                            }
                        }
                    }
                }
            }
        }
    }

    private val chars = ('A'..'F') + ('0'..'9')
    private fun randomFingerprint() = List(20) { chars.random().toString() + chars.random() }.joinToString(":")
}
