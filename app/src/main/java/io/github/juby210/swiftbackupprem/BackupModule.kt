package io.github.juby210.swiftbackupprem

import android.content.Context
import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import io.github.juby210.swiftbackupprem.util.PreferencesManager
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

fun hookBackupApk(cl: ClassLoader, ctx: Context, customFirebaseApp: Boolean, prefs: PreferencesManager) {
    val bClass = backupApkClass ?: return
    val pClass = pathsClass ?: return

    val backupMethod = bClass.declaredMethods.firstOrNull { it.name == "c" }
        ?: bClass.declaredMethods.firstOrNull { it.name == "invoke" }
        ?: bClass.declaredMethods.firstOrNull { it.parameterTypes.isEmpty() }
        ?: return

    XposedBridge.hookMethod(backupMethod, object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            val basePath = try {
                if (pClass.name == "defpackage.ry5" || pClass.declaredFields.any { it.name == "v" }) {
                    val vField = pClass.getDeclaredField("v")
                    vField.isAccessible = true
                    var vInstance = vField.get(null)
                    if (vInstance == null) {
                        try {
                            val qy5 = cl.loadClass("defpackage.qy5")
                            val fMethod = qy5.getDeclaredMethod("f", Boolean::class.javaPrimitiveType, cl.loadClass("defpackage.zn7"))
                            vInstance = fMethod.invoke(null, false, null)
                        } catch (t: Throwable) {
                            Log.e("SBP", "Failed to invoke qy5.f", t)
                        }
                    }
                    val aField = pClass.getDeclaredField("a")
                    aField.isAccessible = true
                    aField.get(vInstance) as? String ?: return
                } else {
                    val pathsA = cl.loadClass("${pClass.name}\$a")
                    val aInstance = pClass.declaredFields.firstOrNull { it.type == pathsA }?.get(null) ?: return
                    val instance = pathsA.getDeclaredMethod("d").invoke(aInstance)
                    pClass.getDeclaredMethod("m").invoke(instance) as? String ?: return
                }
            } catch (e: Throwable) {
                Log.e("SBP", "Failed to resolve basePath", e)
                return
            }

            val dir = File(basePath, "sbp")
            if (!dir.exists()) dir.mkdirs()

            val apkFile = File(dir, "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}).apk")
            if (!apkFile.exists()) {
                try {
                    File(ctx.packageManager.getPackageInfo(BuildConfig.APPLICATION_ID, 0).applicationInfo.sourceDir).copyTo(apkFile, true)
                } catch (t: Throwable) {
                    Log.e("SBP", "Failed copying module APK", t)
                }
            }

            if (customFirebaseApp) {
                val json = JSONObject().apply {
                    put("client", JSONArray().apply {
                        put(JSONObject().apply {
                            put("client_info", JSONObject().apply { put("mobilesdk_app_id", prefs.googleAppId) })
                            put("api_key", JSONArray().apply { put(JSONObject().apply { put("current_key", prefs.googleApiKey) }) })
                        })
                    })
                    put("project_info", JSONObject().apply {
                        put("firebase_url", prefs.firebaseDatabaseUrl)
                        put("project_number", prefs.gcmDefaultSenderId)
                        put("storage_bucket", prefs.googleStorageBucket)
                        put(Consts.projectId, prefs.projectId)
                    })
                    put(Consts.oauthClientId, prefs.clientId)
                }.toString(2)

                try {
                    File(dir, "google-services.json").run { if (!exists() || readText() != json) writeText(json) }
                } catch (t: Throwable) {
                    Log.e("SBP", "Failed writing google-services.json", t)
                }
            }
        }
    })
}
