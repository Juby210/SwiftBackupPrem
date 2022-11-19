package io.github.juby210.swiftbackupprem;

import android.content.Context;

import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class Main implements IXposedHookLoadPackage {
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        System.loadLibrary("nativelib");

        XposedHelpers.findAndHookMethod("org.swiftapps.swiftbackup.SwiftApp", lpparam.classLoader, "onCreate", new XC_MethodHook() {
            public void beforeHookedMethod(MethodHookParam param) throws Throwable {
                var c = lpparam.classLoader.loadClass("com.google.firebase.FirebaseApp");
                c.getDeclaredMethod("initializeApp", Context.class).invoke(null, param.thisObject);
            }
        });

        var c = lpparam.classLoader.loadClass("org.swiftapps.swiftbackup.common.V$a");
        for (var m : c.getDeclaredMethods()) {
            if (m.getName().equals("invoke")) {
                XposedBridge.hookMethod(m, new XC_MethodHook() {
                    public void beforeHookedMethod(MethodHookParam param) {
                        param.setResult(Boolean.TRUE);
                    }
                });
                break;
            }
        }
    }
}
