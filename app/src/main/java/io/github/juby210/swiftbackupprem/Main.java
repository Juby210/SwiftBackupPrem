package io.github.juby210.swiftbackupprem;

import android.content.Context;

import java.lang.reflect.Method;
import java.util.List;

import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@SuppressWarnings("unchecked")
public final class Main implements IXposedHookLoadPackage {
    public static XC_MethodReplacement returnTrue = XC_MethodReplacement.returnConstant(Boolean.TRUE);

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        System.loadLibrary("nativelib");

        var cl = lpparam.classLoader;
        XposedHelpers.findAndHookMethod("org.swiftapps.swiftbackup.SwiftApp", cl, "onCreate", new XC_MethodHook() {
            public void beforeHookedMethod(MethodHookParam param) throws Throwable {
                var c = cl.loadClass("com.google.firebase.FirebaseApp");
                c.getDeclaredMethod("initializeApp", Context.class).invoke(null, param.thisObject);
            }
        });

        var c = cl.loadClass("org.swiftapps.swiftbackup.common.V$a");
        for (var m : c.getDeclaredMethods()) {
            if (m.getName().equals("invoke")) {
                XposedBridge.hookMethod(m, returnTrue);
                break;
            }
        }

        var utils = cl.loadClass("org.swiftapps.swiftbackup.common.h0");
        var loggedIn = utils.getDeclaredMethod("a");
        hookLoggedIn(loggedIn);

        var instance = utils.getDeclaredField("a").get(null);
        var cca = lpparam.classLoader.loadClass("org.swiftapps.swiftbackup.cloud.connect.common.CloudConnectActivity");
        XposedBridge.hookMethod(cca.getDeclaredMethod("o0"), new XC_MethodHook() {
            public void beforeHookedMethod(MethodHookParam param) throws Throwable {
                var l0 = cca.getDeclaredMethod("l0");
                l0.setAccessible(true);
                var g = (List<Object>) l0.getReturnType().getMethod("g").invoke(l0.invoke(param.thisObject));
                if (g.size() > 0) {
                    var item = g.get(0);
                    if (
                        item.getClass().getDeclaredMethod("getItemId").invoke(item).equals("google_drive") &&
                            XposedBridge.invokeOriginalMethod(loggedIn, instance, null).equals(Boolean.FALSE)
                    ) {
                        unhookLoggedIn.unhook();
                        unhookLoggedIn = null;
                    }
                }
            }

            public void afterHookedMethod(MethodHookParam param) {
                if (unhookLoggedIn == null) hookLoggedIn(loggedIn);
            }
        });
    }

    public static XC_MethodHook.Unhook unhookLoggedIn;
    public static void hookLoggedIn(Method m) {
        unhookLoggedIn = XposedBridge.hookMethod(m, returnTrue);
    }
}
