# Preserve line numbers and source file attributes for debugging stack traces
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions

# Keep all module classes, fields, and methods from R8 obfuscation/shrinking
-keep class io.github.juby210.swiftbackupprem.** { *; }
-keepclassmembers class io.github.juby210.swiftbackupprem.** { *; }

# Keep Xposed API framework classes provided at runtime via compileOnly
-dontwarn de.robv.android.xposed.**
-keep class de.robv.android.xposed.** { *; }
-keepclassmembers class de.robv.android.xposed.** { *; }

# Keep DexKit bridge classes used by native C++ (libdexkit.so) JNI reflection
-keep class org.luckypray.dexkit.** { *; }
-keepclassmembers class org.luckypray.dexkit.** { *; }

# Keep all native JNI method declarations
-keepclasseswithmembernames class * {
    native <methods>;
}

# Optimization iterations
-optimizationpasses 5

# Broaden access modifiers to increase results during optimization
-allowaccessmodification
