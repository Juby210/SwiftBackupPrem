@file:JvmName("DexKit")

package io.github.juby210.swiftbackupprem

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import org.luckypray.dexkit.DexKitBridge
import java.lang.reflect.Modifier

private val classesClientId = mapOf(561 to "kf.s0", 569 to "rf.r0", 590 to "eh.u", 620 to "defpackage.gn5")
private val classesBackupApk = mapOf(561 to "org.swiftapps.swiftbackup.common.w1", 569 to "org.swiftapps.swiftbackup.common.n2", 590 to "org.swiftapps.swiftbackup.common.c2", 620 to "defpackage.qm")
private val classesPaths = mapOf(561 to "me.b", 569 to "te.c", 590 to "org.swiftapps.swiftbackup.a", 620 to "defpackage.ry5")

@Keep
@JvmField
var clientIdClass: Class<*>? = null
@Keep
@JvmField
var backupApkClass: Class<*>? = null
@Keep
@JvmField
var pathsClass: Class<*>? = null

@Keep
@Suppress("DEPRECATION")
fun findObfuscatedClasses(ctx: Context, cl: ClassLoader, sourceDir: String) {
    val ver = Integer.valueOf(ctx.packageManager.getPackageInfo(Consts.packageName, 0).versionCode)
    if (classesClientId.containsKey(ver)) {
        clientIdClass = cl.loadClass(classesClientId[ver])
        backupApkClass = cl.loadClass(classesBackupApk[ver])
        pathsClass = cl.loadClass(classesPaths[ver])
    } else {
        try {
            System.loadLibrary("dexkit")
        } catch (t: Throwable) {
            Log.e("SBP", "Failed loading dexkit library", t)
        }
        val excludePackages = listOf("android", "androidx", "com", "iammert", "java", "javax", "kotlin", "kotlinx", "moe", "nz.mega",
            "okhttp3", "okio", "retrofit", "rikka")
        DexKitBridge.create(sourceDir).use { bridge ->
            (bridge.findClass {
                excludePackages(excludePackages)
                matcher {
                    usingStrings("org.swiftapps.swiftbackup:/oauth")
                }
            }.firstOrNull() ?: bridge.findClass {
                excludePackages(excludePackages)
                matcher {
                    fields {
                        add {
                            modifiers(Modifier.PUBLIC or Modifier.STATIC or Modifier.FINAL)
                            name("a")
                        }
                        add {
                            modifiers(Modifier.PRIVATE or Modifier.STATIC or Modifier.FINAL)
                            name("b")
                        }
                        add {
                            modifiers(Modifier.PRIVATE or Modifier.STATIC or Modifier.FINAL)
                            name("c")
                            type("java.lang.String")
                        }
                        add {
                            modifiers(Modifier.PRIVATE or Modifier.STATIC or Modifier.FINAL)
                            name("d")
                            type("android.net.Uri")
                        }
                        count(4)
                    }
                    addMethod {
                        modifiers(Modifier.PUBLIC or Modifier.FINAL)
                        returnType("android.content.Intent")
                        name("f")
                        addParamType("boolean")
                    }
                }
            }.singleOrNull())?.let {
                clientIdClass = it.getInstance(cl)
                Log.d("SBP", "Found client id class: ${it.name}")
            }

            (bridge.findClass {
                matcher {
                    usingStrings("swift_backup_apks/", "SwiftBackupApkSaver")
                }
            }.firstOrNull())?.let {
                backupApkClass = it.getInstance(cl)
                Log.d("SBP", "Found backup apk class: ${it.name}")
            }

            (bridge.findClass {
                excludePackages(excludePackages)
                matcher {
                    methods {
                        add {
                            name("<init>")
                            addParamType("org.swiftapps.swiftbackup.anonymous.MFirebaseUser")
                            addParamType("java.lang.String")
                            paramCount(2)
                            usingStrings("accounts/", "backups/", "cache/", "apps/", "local/", "cloud/", "icon_cache/", "sms/", "calls/")
                        }
                    }
                }
            }.singleOrNull())?.let {
                pathsClass = it.getInstance(cl)
                Log.d("SBP", "Found paths class: ${it.name}")
            }
        }

        if (clientIdClass == null || backupApkClass == null || pathsClass == null) {
            Log.w("SBP", "Couldn't fully hook Swift Backup.")
        }
    }
}
