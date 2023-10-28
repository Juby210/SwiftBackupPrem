package io.github.juby210.swiftbackupprem

import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import io.github.juby210.swiftbackupprem.util.PreferencesManager
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.lang.reflect.Modifier

fun hookBackupApk(cl: ClassLoader, ctx: Context, customFirebaseApp: Boolean, prefs: PreferencesManager) {
    val pathsA = cl.loadClass("${paths!!.name}\$a")
    XposedBridge.hookMethod(backupApk!!.getDeclaredMethod("c"), object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            val pathsClass = paths!!
            val aInstance = pathsClass.getDeclaredField("y").get(null)
            val instance = pathsA.getDeclaredMethod("d").invoke(aInstance)
            val basePath = pathsClass.getDeclaredMethod("m").invoke(instance) as String

            val dir = File(basePath, "sbp")
            if (!dir.exists()) dir.mkdir()

            val apkFile = File(dir, "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}).apk")
            if (!apkFile.exists())
                File(ctx.packageManager.getPackageInfo(BuildConfig.APPLICATION_ID, 0).applicationInfo.sourceDir).copyTo(apkFile, true)

            if (customFirebaseApp) with(prefs) {
                val json = JSONObject().apply {
                    put("client", JSONArray().apply {
                        put(JSONObject().apply {
                            put("client_info", JSONObject().apply { put("mobilesdk_app_id", googleAppId) })
                            put("api_key", JSONArray().apply { put(JSONObject().apply { put("current_key", googleApiKey) }) })
                        })
                    })
                    put("project_info", JSONObject().apply {
                        put("firebase_url", firebaseDatabaseUrl)
                        put("project_number", gcmDefaultSenderId)
                        put("storage_bucket", googleStorageBucket)
                        put(Consts.projectId, projectId)
                    })
                    put(Consts.oauthClientId, clientId)
                }.toString()
                File(dir, "google-services.json").run { if (!exists() || readText() != json) writeText(json) }
            }
        }
    })
}
