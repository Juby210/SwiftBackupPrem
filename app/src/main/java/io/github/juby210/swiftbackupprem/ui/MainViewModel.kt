package io.github.juby210.swiftbackupprem.ui

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.juby210.swiftbackupprem.Consts
import io.github.juby210.swiftbackupprem.util.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainViewModel : ViewModel() {

    fun exportConfig(contentResolver: ContentResolver, uri: Uri, prefs: PreferencesManager) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val json = JSONObject().apply {
                        put("enablePremiumFeatures", prefs.enablePremiumFeatures)
                        put("customFirebaseApp", prefs.customFirebaseApp)
                        put("googleAppId", prefs.googleAppId)
                        put("googleApiKey", prefs.googleApiKey)
                        put("firebaseDatabaseUrl", prefs.firebaseDatabaseUrl)
                        put("gcmDefaultSenderId", prefs.gcmDefaultSenderId)
                        put("googleStorageBucket", prefs.googleStorageBucket)
                        put("projectId", prefs.projectId)
                        put("clientId", prefs.clientId)
                    }.toString(2)
                    outputStream.write(json.toByteArray(Charsets.UTF_8))
                }
            } catch (_: Throwable) {
            }
        }
    }

    fun importConfig(contentResolver: ContentResolver, uri: Uri, prefs: PreferencesManager) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val jsonStr = inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(jsonStr)

                    if (json.has("enablePremiumFeatures") || json.has("customFirebaseApp") || json.has("googleAppId")) {
                        if (json.has("enablePremiumFeatures")) {
                            prefs.enablePremiumFeatures = json.optBoolean("enablePremiumFeatures", true)
                        }
                        if (json.has("customFirebaseApp")) {
                            prefs.customFirebaseApp = json.optBoolean("customFirebaseApp", true)
                        }
                        prefs.googleAppId = json.optString("googleAppId", "")
                        prefs.googleApiKey = json.optString("googleApiKey", "")
                        prefs.firebaseDatabaseUrl = json.optString("firebaseDatabaseUrl", "")
                        prefs.gcmDefaultSenderId = json.optString("gcmDefaultSenderId", "")
                        prefs.googleStorageBucket = json.optString("googleStorageBucket", "")
                        prefs.projectId = json.optString("projectId", "")
                        prefs.clientId = json.optString("clientId", "")
                    } else if (json.has("client") && json.has("project_info")) {
                        prefs.customFirebaseApp = true
                        val clientArray = json.getJSONArray("client")
                        if (clientArray.length() > 0) {
                            val clientObj = clientArray.getJSONObject(0)
                            val clientInfo = clientObj.optJSONObject("client_info")
                            if (clientInfo != null) prefs.googleAppId = clientInfo.optString("mobilesdk_app_id", "")

                            val apiKeyArray = clientObj.optJSONArray("api_key")
                            if (apiKeyArray != null && apiKeyArray.length() > 0) {
                                prefs.googleApiKey = apiKeyArray.getJSONObject(0).optString("current_key", "")
                            }
                        }
                        val projectInfo = json.getJSONObject("project_info")
                        prefs.firebaseDatabaseUrl = projectInfo.optString("firebase_url", "")
                        prefs.gcmDefaultSenderId = projectInfo.optString("project_number", "")
                        prefs.googleStorageBucket = projectInfo.optString("storage_bucket", "")
                        prefs.projectId = projectInfo.optString(Consts.projectId, "")

                        if (json.has(Consts.oauthClientId)) {
                            prefs.clientId = json.getString(Consts.oauthClientId)
                        }
                    }
                }
            } catch (_: Throwable) {
            }
        }
    }
}
