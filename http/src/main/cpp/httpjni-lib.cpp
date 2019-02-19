#include <jni.h>
#include <string>
#include "MD5.h"
#include "base64.h"
#include "3des.h"
#include <iostream>
#include <cstring>

extern "C" {
JNIEXPORT jstring
JNICALL

Java_cn_berfy_sdk_http_demo_HttpDemo_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++1";
    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT jstring
JNICALL
Java_cn_berfy_sdk_http_demo_HttpDemo_stringFromJNI1(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++2";
    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT  jstring
JNICALL
Java_cn_berfy_sdk_http_HttpApi_encodeMd5JNI(JNIEnv *env, jobject, jboolean isUpper, jstring str) {
    const char *originStr;
    //将jstring转化成char *类型
    originStr = env->GetStringUTFChars(str, false);
    MD5 md5 = MD5(originStr);

    std::string md5Result = md5.hexdigest();
    if (isUpper)
        for (int i = 0; i < md5Result.size(); i++)
            md5Result[i] = toupper(md5Result[i]);
    //将char *类型转化成jstring返回给Java层
    return env->NewStringUTF(md5Result.c_str());
}

JNIEXPORT  jstring
JNICALL
Java_cn_berfy_sdk_http_HttpApi_encodeBase64JNI(JNIEnv *env, jobject, jstring str) {
    //base64加密后字符串指针
    unsigned char *base64String;
    //接收java端字符串 jstring转换成c++ byte
    base64String = (unsigned char *) env->GetStringUTFChars(str, false);
    Base64 *base = new Base64();
    //计算出字符串的长度
    int len = env->GetStringUTFLength(str);
    std::string encoded;
    encoded = base->Encode(base64String, len);
    return env->NewStringUTF(encoded.c_str());
}

JNIEXPORT  jstring
JNICALL
Java_cn_berfy_sdk_http_HttpApi_decodeBase64JNI(JNIEnv *env, jobject, jstring str) {
    //base64加密后字符串指针
    const char *base64String;
    //接收java端字符串
    base64String = env->GetStringUTFChars(str, false);
    std::string decoded;
    Base64 *base = new Base64();
    //计算出字符串的长度
    int len = env->GetStringUTFLength(str);
    decoded = base->Decode(base64String, len);
    return env->NewStringUTF(decoded.c_str());
}

char *ConvertJByteaArrayToChars(JNIEnv *env, jbyteArray bytearray) {
    char *chars = NULL;
    jbyte *bytes;
    bytes = env->GetByteArrayElements(bytearray, 0);
    int chars_len = env->GetArrayLength(bytearray);
    chars = new char[chars_len + 1];
    memset(chars, 0, chars_len + 1);
    memcpy(chars, bytes, chars_len);
    chars[chars_len] = 0;

    env->ReleaseByteArrayElements(bytearray, bytes, 0);

    return chars;
}

/*
 * Class:     ItonLifecubeJni_My3DesAlgorithm
 * Method:    Encrypt
 * Signature: wtbee
 */
/*********************3DES加密*********************/
JNIEXPORT jint JNICALL
Java_cn_berfy_sdk_http_HttpApi_encode3DES(JNIEnv *env, jobject, jbyteArray msg,
                                           jbyteArray key, jbyteArray cipher) {
    jbyte *pMsg = (jbyte *) env->GetByteArrayElements(msg, 0);
    jbyte *pKey = (jbyte *) env->GetByteArrayElements(key, 0);
    jbyte *pCipher = (jbyte *) env->GetByteArrayElements(cipher, 0);

    if (!pMsg || !pKey || !pCipher) {
        return -1;
    }
    int flag = Encrypt(ConvertJByteaArrayToChars(env, msg), ConvertJByteaArrayToChars(env, key),
                       ConvertJByteaArrayToChars(env, cipher), sizeof(msg));
    env->ReleaseByteArrayElements(msg, pMsg, 0);
    env->ReleaseByteArrayElements(key, pKey, 0);
    env->ReleaseByteArrayElements(cipher, pCipher, 0);
    return flag;
}

/*********************3DES解密*********************/
JNIEXPORT jint JNICALL
Java_cn_berfy_sdk_http_HttpApi_decode3DES(JNIEnv *env, jobject, jbyteArray cipher,
                                           jbyteArray key,
                                           jbyteArray result) {
    jbyte *pCipher = (jbyte *) env->GetByteArrayElements(cipher, 0);
    jbyte *pKey = (jbyte *) env->GetByteArrayElements(key, 0);
    jbyte *pResult = (jbyte *) env->GetByteArrayElements(result, 0);

    if (!pResult || !pKey || !pCipher) {
        return -1;
    }
    int flag = Decrypt(ConvertJByteaArrayToChars(env, cipher), ConvertJByteaArrayToChars(env, key),
                       ConvertJByteaArrayToChars(env, result), sizeof(result));
    env->ReleaseByteArrayElements(result, pResult, 0);
    env->ReleaseByteArrayElements(key, pKey, 0);
    env->ReleaseByteArrayElements(cipher, pCipher, 0);
    return flag;
}

/**获取DES key*/
JNIEXPORT jstring
JNICALL
Java_cn_berfy_sdk_http_HttpApi_getDesKey(JNIEnv *env ,jobject){
    std::string key = "12345678";
    return env->NewStringUTF(key.c_str());
}

/**获取DES key*/
JNIEXPORT jstring
JNICALL
Java_cn_berfy_sdk_http_HttpApi_get3DesKey(JNIEnv *env ,jobject){
    std::string key = "0535YANTAIJIANWA0535YANTAIJIANWA";
    return env->NewStringUTF(key.c_str());
}

/**获取DES key*/
JNIEXPORT jstring
JNICALL
Java_cn_berfy_sdk_http_HttpApi_getDesCipher(JNIEnv *env ,jobject){
    std::string cipher = "DES";
    return env->NewStringUTF(cipher.c_str());
}

}
