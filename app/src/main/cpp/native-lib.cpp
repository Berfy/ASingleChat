#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_cn_zcgames_sdk_mqttsdk_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_cn_zcgames_sdk_mqttsdk_MainActivity_test(
        JNIEnv *env,
        jobject /* this */) {

    std::string hello = "test";
    return env->NewStringUTF(hello.c_str());
}
