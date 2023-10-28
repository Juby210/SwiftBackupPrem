@file:JvmName("DexKit")

package io.github.juby210.swiftbackupprem

import android.content.Context
import android.util.Log
import android.widget.Toast
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.query.matchers.*
import java.lang.reflect.Modifier

private val classesClientId = mapOf(561 to "kf.s0", 569 to "rf.r0")
private val classesBackupApk = mapOf(561 to "org.swiftapps.swiftbackup.common.w1", 569 to "org.swiftapps.swiftbackup.common.n2")
private val classesPaths = mapOf(561 to "me.b", 569 to "te.c")

@JvmField
var clientId: Class<*>? = null
@JvmField
var backupApk: Class<*>? = null
@JvmField
var paths: Class<*>? = null

@Suppress("DEPRECATION")
fun findObfuscatedClasses(ctx: Context, cl: ClassLoader, sourceDir: String) {
    val ver = Integer.valueOf(ctx.packageManager.getPackageInfo(Consts.packageName, 0).versionCode)
    if (classesClientId.containsKey(ver)) {
        clientId = cl.loadClass(classesClientId[ver])
        backupApk = cl.loadClass(classesBackupApk[ver])
        paths = cl.loadClass(classesPaths[ver])
    } else {
        System.loadLibrary("dexkit")
        val excludePackages = listOf("android", "androidx", "com", "iammert", "java", "javax", "kotlin", "kotlinx", "moe", "nz.mega",
            "okhttp3", "okio", "org", "retrofit", "rikka")
        DexKitBridge.create(sourceDir)?.use { bridge ->
            bridge.findClass {
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
            }.firstOrNull()?.let {
                clientId = it.getInstance(cl)
                Log.d("SBP", "Found client id class: ${it.name}")
            }

            bridge.findClass {
                searchPackages("org.swiftapps.swiftbackup.common")
                matcher {
                    fields {
                        addForName("a")
                        count(1)
                    }
                    addMethod {
                        modifiers(Modifier.PRIVATE or Modifier.FINAL)
                        returnType("void")
                        name("c")
                        paramCount(0)
                        usingStrings("stable", "swift_backup_apks/", "SwiftBackupApkSaver")
                    }
                }
            }.firstOrNull()?.let {
                backupApk = it.getInstance(cl)
                Log.d("SBP", "Found backup apk class: ${it.name}")
            }

            bridge.findClass {
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
                        add {
                            modifiers(Modifier.PUBLIC or Modifier.FINAL)
                            returnType("java.lang.String")
                            name("m")
                            paramCount(0)
                        }
                    }
                }
            }.firstOrNull()?.let {
                paths = it.getInstance(cl)
                Log.d("SBP", "Found paths class: ${it.name}")
            }
        }

        if (clientId == null || backupApk == null || paths == null) Toast.makeText(
            ctx,
            "[SBP] Couldn't fully hook Swift Backup. Check if there's module update or report an issue.",
            Toast.LENGTH_LONG
        ).show()
    }
}
