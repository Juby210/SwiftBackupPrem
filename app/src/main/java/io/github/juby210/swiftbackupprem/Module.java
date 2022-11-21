package io.github.juby210.swiftbackupprem;

import android.content.Context;

import java.util.Arrays;

import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.juby210.swiftbackupprem.util.PreferencesManager;

public final class Module implements IXposedHookLoadPackage {
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        System.loadLibrary("nativelib");

        var xPrefs = new XSharedPreferences(BuildConfig.APPLICATION_ID);
        xPrefs.makeWorldReadable();
        var prefs = new PreferencesManager(xPrefs);
        var customFirebaseApp = prefs.getCustomFirebaseApp() &&
            prefs.getGoogleAppId().length() > 0 &&
            prefs.getGoogleApiKey().length() > 0 &&
            prefs.getFirebaseDatabaseUrl().length() > 0 &&
            prefs.getGcmDefaultSenderId().length() > 0 &&
            prefs.getProjectId().length() > 0 &&
            prefs.getClientId().length() > 0;

        var cl = lpparam.classLoader;
        var sa = cl.loadClass("org.swiftapps.swiftbackup.SwiftApp");
        XposedHelpers.findAndHookMethod(sa, "onCreate", new XC_MethodHook() {
            @SuppressWarnings("JavaReflectionInvocation")
            public void beforeHookedMethod(MethodHookParam param) throws Throwable {
                var c = cl.loadClass("com.google.firebase.FirebaseApp");
                if (customFirebaseApp) {
                    var options = cl.loadClass("com.google.firebase.FirebaseOptions");
                    var params = new Class<?>[7];
                    Arrays.fill(params, String.class);
                    var constructor = options.getDeclaredConstructor(params);

                    c.getDeclaredMethod("initializeApp", Context.class, options).invoke(
                        null,
                        param.thisObject,
                        constructor.newInstance(
                            prefs.getGoogleAppId(),
                            prefs.getGoogleApiKey(),
                            prefs.getFirebaseDatabaseUrl(),
                            null,
                            prefs.getGcmDefaultSenderId(),
                            prefs.getGoogleStorageBucket(),
                            prefs.getProjectId()
                        )
                    );
                } else c.getDeclaredMethod("initializeApp", Context.class).invoke(null, param.thisObject);

            }
        });

        if (customFirebaseApp) {
            var c = cl.loadClass("kf.s0");
            XposedBridge.hookMethod(c.getDeclaredMethod("f", boolean.class), new XC_MethodHook() {
                public void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    var clientId = c.getDeclaredField("c");
                    clientId.setAccessible(true);
                    clientId.set(null, prefs.getClientId());
                }
            });

            XposedHelpers.findAndHookMethod("org.swiftapps.swiftbackup.cloud.d", cl, "d", XC_MethodReplacement.returnConstant(Boolean.FALSE));
        }

        var c = cl.loadClass("org.swiftapps.swiftbackup.common.V$a");
        for (var m : c.getDeclaredMethods()) {
            if (m.getName().equals("invoke")) {
                XposedBridge.hookMethod(m, XC_MethodReplacement.returnConstant(Boolean.TRUE));
                break;
            }
        }
    }
}
