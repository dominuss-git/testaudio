#include <jni.h>
#include <string.h>

JNIEXPORT jshortArray JNICALL Java_com_example_myapplication_MainActivity_helloWorld
    (JNIEnv* env, jobject thiz, jshortArray buffShort) {

    jsize length = (*env)->GetArrayLength(env, buffShort);
    jshort* body = (* env) -> GetShortArrayElements (env, buffShort, 0);
    for (int i = 0; i < length; i++) {
        body[i] = body[i] / 2;
    }

    (*env)->ReleaseShortArrayElements(env, buffShort, body, 0);
    return buffShort;
}