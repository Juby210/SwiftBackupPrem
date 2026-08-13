#include <jni.h>
#include <string.h>
#include <stdbool.h>
#include <dlfcn.h>
#include "hook.h"

static HookFunType hook_func = NULL;

jint (*backup)(JavaVM *vm, void *reserved);

jint fakeLoad(JavaVM *vm, void *reserved) {
    (void) vm;
    (void) reserved;
    return JNI_VERSION_1_6;
}

bool ends_with(const char *a, const char *b) {
    size_t len = strlen(a);
    size_t len2 = strlen(b);
    if (len2 > len) return false;
    return strncmp(a + len - len2, b, len2) == 0;
}

void on_library_loaded(const char *name, void *handle) {
    if (name && hook_func && ends_with(name, "libnative-lib.so")) {
        void *target = dlsym(handle, "JNI_OnLoad");
        if (target) {
            hook_func(target, (void *) fakeLoad, (void **) &backup);
        }
    }
}

JNIEXPORT __attribute__((visibility("default"))) NativeOnModuleLoaded native_init(const NativeAPIEntries *entries) {
    if (!entries) return NULL;
    hook_func = entries->hook_func;
    return on_library_loaded;
}
