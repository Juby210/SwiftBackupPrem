package io.github.juby210.swiftbackupprem;

import android.content.Context;
import android.widget.Toast;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindClass;
import org.luckypray.dexkit.query.matchers.*;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.juby210.swiftbackupprem.util.PreferencesManager;

public final class Module implements IXposedHookLoadPackage {
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("org.swiftapps.swiftbackup")) return;
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

                    var ctx = (Context) param.thisObject;
                    var ver = Integer.valueOf(ctx.getPackageManager().getPackageInfo(lpparam.packageName, 0).versionCode);
                    var classNames = Consts.getClassNames();
                    Class<?> classClientId;
                    if (classNames.containsKey(ver)) classClientId = cl.loadClass(classNames.get(ver));
                    else {
                        System.loadLibrary("dexkit");
                        try (DexKitBridge bridge = DexKitBridge.create(lpparam.appInfo.sourceDir)) {
                            var classData = bridge.findClass(
                                FindClass.create()
                                    .excludePackages("android", "androidx", "com", "iammert", "java", "javax", "kotlin", "kotlinx", "moe", "nz.mega",
                                        "okhttp3", "okio", "org", "retrofit", "rikka")
                                    .matcher(
                                        ClassMatcher.create()
                                            .fields(
                                                FieldsMatcher.create()
                                                    .add(FieldMatcher.create().modifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL).name("a"))
                                                    .add(FieldMatcher.create().modifiers(Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL).name("b"))
                                                    .add(FieldMatcher.create().modifiers(Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL).name("c")
                                                        .type("java.lang.String"))
                                                    .add(FieldMatcher.create().modifiers(Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL).name("d")
                                                        .type("android.net.Uri"))
                                                    .count(4)
                                            )
                                            .addMethod(
                                                MethodMatcher.create()
                                                    .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                                                    .returnType("android.content.Intent")
                                                    .name("f")
                                                    .addParamType("boolean")
                                            )
                                    )
                            ).firstOrNull();

                            if (classData == null) {
                                Toast.makeText(
                                    ctx,
                                    "[SBP] Couldn't fully hook Swift Backup. Check if there's module update or report an issue.",
                                    Toast.LENGTH_LONG
                                ).show();
                                classClientId = null;
                            } else classClientId = classData.getInstance(cl);
                        }
                    }

                    if (classClientId != null) {
                        XposedBridge.hookMethod(classClientId.getDeclaredMethod("f", boolean.class), new XC_MethodHook() {
                            public void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                var clientId = c.getDeclaredField("c");
                                clientId.setAccessible(true);
                                clientId.set(null, prefs.getClientId());
                            }
                        });
                    }
                } else c.getDeclaredMethod("initializeApp", Context.class).invoke(null, param.thisObject);
            }
        });

        if (customFirebaseApp)
            XposedHelpers.findAndHookMethod("org.swiftapps.swiftbackup.cloud.d", cl, "d", XC_MethodReplacement.returnConstant(Boolean.FALSE));

        var c = cl.loadClass("org.swiftapps.swiftbackup.common.V$a");
        for (var m : c.getDeclaredMethods()) {
            if (m.getName().equals("invoke")) {
                XposedBridge.hookMethod(m, XC_MethodReplacement.returnConstant(Boolean.TRUE));
                break;
            }
        }
    }
}
