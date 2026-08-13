package io.github.juby210.swiftbackupprem

import android.content.Context
import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.juby210.swiftbackupprem.util.PreferencesManager

class Module : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != Consts.packageName) return

        try {
            System.loadLibrary("nativelib")
        } catch (t: Throwable) {
            Log.e("SBP", "Failed to load nativelib native library", t)
        }

        val xPrefs = XSharedPreferences(BuildConfig.APPLICATION_ID)
        try {
            @Suppress("DEPRECATION")
            xPrefs.makeWorldReadable()
        } catch (t: Throwable) {
            Log.w("SBP", "Could not set world readable on XSharedPreferences", t)
        }

        val prefs = PreferencesManager(xPrefs)
        val cl = lpparam.classLoader

        val swiftAppClass = try {
            cl.loadClass("org.swiftapps.swiftbackup.SwiftApp")
        } catch (t: Throwable) {
            Log.e("SBP", "Failed to load SwiftApp class", t)
            return
        }

        XposedHelpers.findAndHookMethod(swiftAppClass, "onCreate", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                xPrefs.reload()
                val ctx = param.thisObject as? Context ?: return

                val isCustomFirebase = prefs.customFirebaseApp &&
                        prefs.googleAppId.isNotBlank() &&
                        prefs.googleApiKey.isNotBlank() &&
                        prefs.firebaseDatabaseUrl.isNotBlank() &&
                        prefs.gcmDefaultSenderId.isNotBlank() &&
                        prefs.projectId.isNotBlank() &&
                        prefs.clientId.isNotBlank()

                try {
                    val appSourceDir = lpparam.appInfo?.sourceDir ?: return
                    findObfuscatedClasses(ctx, cl, appSourceDir)
                } catch (t: Throwable) {
                    Log.e("SBP", "Failed DexKit search", t)
                }

                if (isCustomFirebase) {
                    try {
                        val firebaseAppClass = cl.loadClass("com.google.firebase.FirebaseApp")
                        val optionsClass = cl.loadClass("com.google.firebase.FirebaseOptions")
                        val params = Array(7) { String::class.java }
                        val constructor = optionsClass.getDeclaredConstructor(*params)

                        var storageBucket = prefs.googleStorageBucket
                        if (storageBucket.isBlank()) {
                            storageBucket = "${prefs.projectId}.appspot.com"
                        }

                        val optionsInstance = constructor.newInstance(
                            prefs.googleAppId,
                            prefs.googleApiKey,
                            prefs.firebaseDatabaseUrl,
                            null,
                            prefs.gcmDefaultSenderId,
                            storageBucket,
                            prefs.projectId
                        )

                        val initializeAppMethod = firebaseAppClass.getDeclaredMethod(
                            "initializeApp",
                            Context::class.java,
                            optionsClass
                        )
                        initializeAppMethod.invoke(null, ctx, optionsInstance)
                        Log.d("SBP", "Initialized custom FirebaseApp with storageBucket: $storageBucket")
                    } catch (t: Throwable) {
                        Log.e("SBP", "Failed to initialize custom FirebaseApp", t)
                    }

                    try {
                        XposedHelpers.findAndHookMethod(
                            swiftAppClass,
                            "getGoogleAuthAndroidClientId",
                            XC_MethodReplacement.returnConstant(prefs.clientId)
                        )
                    } catch (_: Throwable) {}

                    clientIdClass?.let { cIdClass ->
                        try {
                            val fMethod = cIdClass.getDeclaredMethod("f", Boolean::class.javaPrimitiveType)
                            XposedBridge.hookMethod(fMethod, object : XC_MethodHook() {
                                override fun beforeHookedMethod(param: MethodHookParam) {
                                    try {
                                        val clientIdField = cIdClass.getDeclaredField("c")
                                        clientIdField.isAccessible = true
                                        clientIdField.set(null, prefs.clientId)
                                    } catch (t: Throwable) {
                                        Log.e("SBP", "Failed setting clientId field c", t)
                                    }
                                }
                            })
                        } catch (_: NoSuchMethodException) {
                            try {
                                val aField = cIdClass.getDeclaredField("a")
                                aField.isAccessible = true
                                aField.set(null, prefs.clientId)
                            } catch (_: Throwable) {}
                        }
                    }
                }

                if (backupApkClass != null && pathsClass != null) {
                    try {
                        hookBackupApk(cl, ctx, isCustomFirebase, prefs)
                    } catch (t: Throwable) {
                        Log.e("SBP", "Failed to hook BackupApk", t)
                    }
                }
            }
        })

        if (prefs.enablePremiumFeatures) {
            try {
                XposedHelpers.findAndHookMethod("org.swiftapps.swiftbackup.cloud.d", cl, "d", XC_MethodReplacement.returnConstant(java.lang.Boolean.FALSE))
            } catch (_: Throwable) {}

            try {
                XposedHelpers.findAndHookMethod("org.swiftapps.swiftbackup.common.V", cl, "getG", XC_MethodReplacement.returnConstant(java.lang.Boolean.TRUE))
            } catch (_: Throwable) {}

            try {
                XposedHelpers.findAndHookMethod("org.swiftapps.swiftbackup.common.V", cl, "getA", XC_MethodReplacement.returnConstant(java.lang.Boolean.TRUE))
            } catch (t: Throwable) {
                Log.e("SBP", "Failed to hook V.getA", t)
            }

            try {
                val vClassA = cl.loadClass("org.swiftapps.swiftbackup.common.V\$a")
                for (m in vClassA.declaredMethods) {
                    if (m.name == "invoke") {
                        XposedBridge.hookMethod(m, XC_MethodReplacement.returnConstant(java.lang.Boolean.TRUE))
                        break
                    }
                }
            } catch (_: Throwable) {}
        }
    }
}
