package io.github.juby210.swiftbackupprem.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import io.github.juby210.swiftbackupprem.Consts
import java.security.MessageDigest

object AppUtils {
    fun openSwiftBackup(context: Context) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(Consts.packageName)
            ?: context.packageManager.getLeanbackLaunchIntentForPackage(Consts.packageName)
            ?: Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setPackage(Consts.packageName)
            }
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(launchIntent)
        } catch (e: Throwable) {
            Log.e("SBP", "Could not launch Swift Backup: ${e.localizedMessage}")
        }
    }

    fun forceStopSwiftBackup(context: Context) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "am force-stop ${Consts.packageName}"))
            val exitCode = process.waitFor()
            if (exitCode == 0) {
                return
            }
        } catch (_: Throwable) {}

        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", Consts.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Throwable) {}
    }

    fun getSwiftBackupSha1(context: Context): String {
        // Try obtaining SHA-1 from installed Swift Backup package first
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    Consts.packageName,
                    android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    Consts.packageName,
                    android.content.pm.PackageManager.GET_SIGNATURES
                )
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val signingInfo = packageInfo.signingInfo
                if (signingInfo != null) {
                    if (signingInfo.hasMultipleSigners()) {
                        signingInfo.apkContentsSigners
                    } else {
                        signingInfo.signingCertificateHistory
                    }
                } else null
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            val cert = signatures?.firstOrNull()?.toByteArray()
            if (cert != null) {
                val md = MessageDigest.getInstance("SHA-1")
                val digest = md.digest(cert)
                val sha1 = digest.joinToString(":") { "%02X".format(it) }
                Log.d("SBP", "Found Swift Backup SHA-1: $sha1")
                return sha1
            }
        } catch (e: Throwable) {
            Log.w("SBP", "Could not get Swift Backup package signing cert: ${e.localizedMessage}")
        }

        // Fallback: Try obtaining SHA-1 from current app's signing cert if Swift Backup is not installed
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.GET_SIGNATURES
                )
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val signingInfo = packageInfo.signingInfo
                if (signingInfo != null) {
                    if (signingInfo.hasMultipleSigners()) {
                        signingInfo.apkContentsSigners
                    } else {
                        signingInfo.signingCertificateHistory
                    }
                } else null
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            val cert = signatures?.firstOrNull()?.toByteArray()
            if (cert != null) {
                val md = MessageDigest.getInstance("SHA-1")
                val digest = md.digest(cert)
                val sha1 = digest.joinToString(":") { "%02X".format(it) }
                Log.d("SBP", "Found self app SHA-1: $sha1")
                return sha1
            }
        } catch (e: Throwable) {
            Log.w("SBP", "Could not get self package signing cert: ${e.localizedMessage}")
        }

        return ""
    }
}
