#include <jni.h>
#include <string>
#include <dlfcn.h>
#include "hook.h"

static HookFunType hook_func = nullptr;

jint (*backup)(JavaVM *, void *);

jint fakeLoad(JavaVM *, void *) {
    return JNI_VERSION_1_6;
}

bool ends_with(const std::string& a, const std::string& b) {
    if (b.size() > a.size()) return false;
    return std::equal(a.begin() + a.size() - b.size(), a.end(), b.begin());
}

void on_library_loaded(const char *name, void *handle) {
    if (ends_with(std::string(name), "libnative-lib.so")) {
        void *target = dlsym(handle, "JNI_OnLoad");
        hook_func(target, (void *) fakeLoad, (void **) &backup);
    }
}

extern "C" [[gnu::visibility("default")]] [[gnu::used]]
NativeOnModuleLoaded native_init(const NativeAPIEntries *entries) {
    hook_func = entries->hook_func;
    return on_library_loaded;
}
