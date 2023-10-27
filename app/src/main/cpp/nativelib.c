#include <jni.h>
#include <string.h>
#include <stdbool.h>
#include <dlfcn.h>
#include "hook.h"

static HookFunType hook_func = NULL;

jint (*backup)(JavaVM *, void *);

jint fakeLoad(JavaVM *, void *) {
    return JNI_VERSION_1_6;
}

bool ends_with(const char *a, const char *b) {
    size_t len = strlen(a);
    size_t len2 = strlen(b);
    if (len2 > len) return false;
    return strncmp(a + len - len2, b, len2) == 0;
}

void on_library_loaded(const char *name, void *handle) {
    if (ends_with(name, "libnative-lib.so")) {
        void *target = dlsym(handle, "JNI_OnLoad");
        hook_func(target, (void *) fakeLoad, (void **) &backup);
    }
}

NativeOnModuleLoaded native_init(const NativeAPIEntries *entries) {
    hook_func = entries->hook_func;
    return on_library_loaded;
}
