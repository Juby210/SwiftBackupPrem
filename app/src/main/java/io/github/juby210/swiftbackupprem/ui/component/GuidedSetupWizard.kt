package io.github.juby210.swiftbackupprem.ui.component

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.juby210.swiftbackupprem.Consts
import io.github.juby210.swiftbackupprem.util.AppUtils
import io.github.juby210.swiftbackupprem.util.PreferencesManager
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidedSetupWizard(
    prefs: PreferencesManager,
    onFinish: () -> Unit = {}
) {
    val hasExistingConfig = remember(
        prefs.googleAppId,
        prefs.googleApiKey,
        prefs.firebaseDatabaseUrl,
        prefs.gcmDefaultSenderId,
        prefs.projectId,
        prefs.clientId
    ) {
        prefs.googleAppId.isNotBlank() &&
                prefs.googleApiKey.isNotBlank() &&
                prefs.firebaseDatabaseUrl.isNotBlank() &&
                prefs.gcmDefaultSenderId.isNotBlank() &&
                prefs.projectId.isNotBlank() &&
                prefs.clientId.isNotBlank()
    }
    var userOverrodeCollapse by remember { mutableStateOf<Boolean?>(null) }
    val isCollapsed = userOverrodeCollapse ?: hasExistingConfig
    var currentStep by remember { mutableIntStateOf(1) }
    val totalSteps = 5

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current

    val pickJsonLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                try {
                    val json = JSONObject(inputStream.bufferedReader().use { r -> r.readText() })
                    with(prefs) {
                        if (json.has("client")) {
                            val clientArray = json.getJSONArray("client")
                            if (clientArray.length() > 0) {
                                val clientObj = clientArray.getJSONObject(0)
                                val clientInfo = clientObj.optJSONObject("client_info")
                                if (clientInfo != null) googleAppId = clientInfo.optString("mobilesdk_app_id", "")

                                val apiKeyArray = clientObj.optJSONArray("api_key")
                                if (apiKeyArray != null && apiKeyArray.length() > 0) {
                                    googleApiKey = apiKeyArray.getJSONObject(0).optString("current_key", "")
                                }
                            }
                        }
                        if (json.has("project_info")) {
                            val projInfo = json.getJSONObject("project_info")
                            firebaseDatabaseUrl = projInfo.optString("firebase_url", "")
                            gcmDefaultSenderId = projInfo.optString("project_number", "")
                            googleStorageBucket = projInfo.optString("storage_bucket", "")
                            projectId = projInfo.optString(Consts.projectId, "")
                        }
                        if (json.has(Consts.oauthClientId)) clientId = json.optString(Consts.oauthClientId, "")
                        userOverrodeCollapse = null
                    }
                } catch (_: Throwable) {
                }
            }
        }
    }

    AnimatedContent(
        targetState = isCollapsed,
        label = "CollapseAnimation"
    ) { collapsed ->
        if (collapsed) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Firebase Configured",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Force stop and restart Swift Backup to apply changes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { userOverrodeCollapse = false },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 42.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Setup", textAlign = TextAlign.Center, softWrap = true)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Progress Header
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Step $currentStep of $totalSteps",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val stepIcon = when (currentStep) {
                                    1 -> Icons.Default.RocketLaunch
                                    2 -> Icons.Default.Storage
                                    3 -> Icons.Default.Lock
                                    4 -> Icons.Default.VpnKey
                                    5 -> Icons.Default.TaskAlt
                                    else -> Icons.Default.Tune
                                }
                                Icon(
                                    imageVector = stepIcon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = getStepTitle(currentStep),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        LinearProgressIndicator(
                            progress = { currentStep.toFloat() / totalSteps.toFloat() },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                        )
                    }
                }

                // Animated Step Body
                AnimatedContent(
                    targetState = currentStep,
                    label = "StepAnimation"
                ) { step ->
                    when (step) {
                        1 -> Step1WelcomeImport(
                            onImportClick = { pickJsonLauncher.launch("application/json") },
                            onNext = { currentStep = 2 }
                        )
                        2 -> Step2Database(
                            prefs = prefs,
                            onOpenConsole = { uriHandler.openUri("https://console.firebase.google.com/u/0/") },
                            onCopyRules = {
                                clipboardManager.setText(
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
                            onBack = { currentStep = 1 },
                            onNext = { currentStep = 3 }
                        )
                        3 -> Step3AuthAndStorage(
                            prefs = prefs,
                            onOpenConsole = { uriHandler.openUri("https://console.firebase.google.com/u/0/") },
                            onBack = { currentStep = 2 },
                            onNext = { currentStep = 4 }
                        )
                        4 -> Step4AndroidOAuth(
                            prefs = prefs,
                            onCopyPackageName = {
                                clipboardManager.setText(AnnotatedString(Consts.packageName))
                            },
                            onCopyFingerprint = {
                                clipboardManager.setText(AnnotatedString(AppUtils.getSwiftBackupSha1(context)))
                            },
                            onOpenCloudConsole = { uriHandler.openUri("https://console.developers.google.com/") },
                            onEnableDriveApi = {
                                uriHandler.openUri("https://console.cloud.google.com/apis/library/drive.googleapis.com?project=${prefs.projectId}")
                            },
                            onBack = { currentStep = 3 },
                            onNext = { currentStep = 5 }
                        )
                        5 -> Step5ReviewFinish(
                            prefs = prefs,
                            onImportClick = { pickJsonLauncher.launch("application/json") },
                            onBack = { currentStep = 4 },
                            onFinish = {
                                onFinish()
                                userOverrodeCollapse = true
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun getStepTitle(step: Int): String = when (step) {
    1 -> "Welcome & Import"
    2 -> "Database Setup"
    3 -> "Auth & Storage"
    4 -> "OAuth Client"
    5 -> "Review & Finish"
    else -> ""
}

@Composable
private fun Step1WelcomeImport(
    onImportClick: () -> Unit,
    onNext: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ListAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Guided Manual Setup",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 42.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Start Setup", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, softWrap = true)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Text(
                text = "OR",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "One-Tap Import",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                OutlinedButton(
                    onClick = onImportClick,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 42.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import google-services.json", textAlign = TextAlign.Center, softWrap = true)
                }
            }
        }
    }
}

@Composable
private fun Step2Database(
    prefs: PreferencesManager,
    onOpenConsole: () -> Unit,
    onCopyRules: () -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onOpenConsole,
                modifier = Modifier.weight(1f).heightIn(min = 40.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Launch,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Open Console", fontSize = 13.sp, textAlign = TextAlign.Center, softWrap = true)
            }
            OutlinedButton(
                onClick = onCopyRules,
                modifier = Modifier.weight(1f).heightIn(min = 40.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy Rules", fontSize = 13.sp, textAlign = TextAlign.Center, softWrap = true)
            }
        }

        SettingsTextField(
            label = "Project ID",
            pref = prefs.projectId,
            onPrefChange = { prefs.projectId = it }
        )

        SettingsTextField(
            label = "Firebase Database URL",
            pref = prefs.firebaseDatabaseUrl,
            onPrefChange = { prefs.firebaseDatabaseUrl = it }
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.heightIn(min = 40.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Back", textAlign = TextAlign.Center, softWrap = true)
            }
            Button(
                onClick = onNext,
                modifier = Modifier.heightIn(min = 40.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Next", textAlign = TextAlign.Center, softWrap = true)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun Step3AuthAndStorage(
    prefs: PreferencesManager,
    onOpenConsole: () -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onOpenConsole,
            modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Launch, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Open Firebase Console", textAlign = TextAlign.Center, softWrap = true)
        }

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
            label = "GCM Sender ID (Project Number)",
            pref = prefs.gcmDefaultSenderId,
            onPrefChange = { prefs.gcmDefaultSenderId = it }
        )

        SettingsTextField(
            label = "Google Storage Bucket (Optional)",
            pref = prefs.googleStorageBucket,
            onPrefChange = { prefs.googleStorageBucket = it }
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.heightIn(min = 40.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Back", textAlign = TextAlign.Center, softWrap = true)
            }
            Button(
                onClick = onNext,
                modifier = Modifier.heightIn(min = 40.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Next", textAlign = TextAlign.Center, softWrap = true)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun Step4AndroidOAuth(
    prefs: PreferencesManager,
    onCopyPackageName: () -> Unit,
    onCopyFingerprint: () -> Unit,
    onOpenCloudConsole: () -> Unit,
    onEnableDriveApi: () -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onCopyPackageName,
                modifier = Modifier.weight(1f).heightIn(min = 40.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy Package", fontSize = 12.sp, textAlign = TextAlign.Center, softWrap = true)
            }
            OutlinedButton(
                onClick = onCopyFingerprint,
                modifier = Modifier.weight(1f).heightIn(min = 40.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy Fingerprint", fontSize = 12.sp, textAlign = TextAlign.Center, softWrap = true)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onOpenCloudConsole,
                modifier = Modifier.weight(1f).heightIn(min = 40.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Launch, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cloud Console", fontSize = 12.sp, textAlign = TextAlign.Center, softWrap = true)
            }
            Button(
                onClick = onEnableDriveApi,
                modifier = Modifier.weight(1f).heightIn(min = 40.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Launch, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Enable Drive API", fontSize = 12.sp, textAlign = TextAlign.Center, softWrap = true)
            }
        }

        SettingsTextField(
            label = "Client ID",
            pref = prefs.clientId,
            onPrefChange = { prefs.clientId = it }
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.heightIn(min = 40.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Back", textAlign = TextAlign.Center, softWrap = true)
            }
            Button(
                onClick = onNext,
                modifier = Modifier.heightIn(min = 40.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Review", textAlign = TextAlign.Center, softWrap = true)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun Step5ReviewFinish(
    prefs: PreferencesManager,
    onImportClick: () -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    val requiredFields = listOf(
        "Google App ID" to prefs.googleAppId,
        "Google Api Key" to prefs.googleApiKey,
        "Firebase Database URL" to prefs.firebaseDatabaseUrl,
        "GCM Sender ID" to prefs.gcmDefaultSenderId,
        "Project ID" to prefs.projectId,
        "Client ID" to prefs.clientId
    )

    val allFilled = requiredFields.all { it.second.isNotBlank() }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (allFilled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (allFilled) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.ListAlt,
                    contentDescription = null,
                    tint = if (allFilled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = if (allFilled) "Credentials Complete!" else "Configuration Checklist",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                requiredFields.forEach { (label, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (value.isNotBlank()) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (value.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (value.isNotBlank()) "OK" else "Missing",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (value.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Storage Bucket (Optional)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (prefs.googleStorageBucket.isNotBlank()) Icons.Default.CheckCircle else Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (prefs.googleStorageBucket.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (prefs.googleStorageBucket.isNotBlank()) "OK" else "Auto Default",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (prefs.googleStorageBucket.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onImportClick,
            modifier = Modifier.fillMaxWidth().heightIn(min = 42.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Import google-services.json", textAlign = TextAlign.Center, softWrap = true)
        }

        val context = LocalContext.current
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.heightIn(min = 40.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Back", textAlign = TextAlign.Center, softWrap = true)
            }
            Button(
                onClick = {
                    onFinish()
                },
                modifier = Modifier.heightIn(min = 40.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Finish & Save", textAlign = TextAlign.Center, softWrap = true)
            }
        }
    }
}
