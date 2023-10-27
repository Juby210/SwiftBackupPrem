package io.github.juby210.swiftbackupprem.util

import android.content.SharedPreferences
import androidx.compose.runtime.*
import androidx.core.content.edit
import io.github.juby210.swiftbackupprem.Consts
import kotlin.reflect.KProperty

class PreferencesManager(private val prefs: SharedPreferences) {
    private class Preference<T>(
        private val key: String,
        defaultValue: T,
        getter: (key: String, defaultValue: T) -> T,
        private val setter: (key: String, newValue: T) -> Unit
    ) {
        var value by mutableStateOf(getter(key, defaultValue))
            private set

        operator fun getValue(thisRef: Any?, property: KProperty<*>) = value
        operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: T) {
            value = newValue
            setter(key, newValue)
        }
    }

    private fun getString(key: String, defaultValue: String) = prefs.getString(key, defaultValue) ?: defaultValue
    private fun getBoolean(key: String, defaultValue: Boolean) = prefs.getBoolean(key, defaultValue)

    private fun putString(key: String, value: String?) = prefs.edit { putString(key, value) }
    private fun putBoolean(key: String, value: Boolean) = prefs.edit { putBoolean(key, value) }

    private fun stringPreference(
        key: String
    ) = Preference(
        key = key,
        defaultValue = "",
        getter = ::getString,
        setter = ::putString
    )

    @Suppress("SameParameterValue")
    private fun booleanPreference(
        key: String
    ) = Preference(
        key = key,
        defaultValue = false,
        getter = ::getBoolean,
        setter = ::putBoolean
    )

    var googleAppId by stringPreference(Consts.googleAppId)
    var googleApiKey by stringPreference(Consts.googleApiKey)
    var firebaseDatabaseUrl by stringPreference(Consts.firebaseDatabaseUrl)
    var gcmDefaultSenderId by stringPreference(Consts.gcmDefaultSenderId)
    var googleStorageBucket by stringPreference(Consts.googleStorageBucket)
    var projectId by stringPreference(Consts.projectId)
    var clientId by stringPreference("oauth_client_id")

    var customFirebaseApp by booleanPreference("custom_firebase_app")
}
