#include <jni.h>

// Links from Welcome Activity

//Links from MainActivity
JNIEXPORT jstring JNICALL Java_com_it_1tech613_zhe_teevee_MainActivity_getOne(JNIEnv *env, jobject instance){
    return (*env)->NewStringUTF(env, "dGVldmVlMTk=");
}
JNIEXPORT jstring JNICALL Java_com_it_1tech613_zhe_teevee_MainActivity_getTwo(JNIEnv *env, jobject instance){
    return (*env)->NewStringUTF(env, "aHR0cDovL3RoZXJhZGlvc2hhay5jby51ay9pcHR2L2luZGV4LnBocD9saWNlbmNlX2tleQ==");
}
JNIEXPORT jstring JNICALL Java_com_it_1tech613_zhe_teevee_MainActivity_getThree(JNIEnv *env, jobject instance){
    return (*env)->NewStringUTF(env, "aHR0cDovL3RoYUhSMGNEb3ZMM1JvYUhSMGNEb3ZMM1JvWlhKaFpHbHZjMmhoYXk1amJ5NTFheTlwY0hSMlgzQXlMMmx1WkdWNExuQm9jRDlzYVdObGJtTmxYMnRsZVQxelpERXk=");
}
JNIEXPORT jstring JNICALL Java_com_it_1tech613_zhe_teevee_MainActivity_getFour(JNIEnv *env, jobject instance){
    return (*env)->NewStringUTF(env, "aHR0cDovL3RoYUhSMGNEb3ZMM1JvYUhSMGNEb3ZMM1JvWlhKaFpHbHZjMmhoYXk1amJ5NTFheTlwY0hSMlgzQXhMMmx1WkdWNExuQm9jRDlzYVdObGJtTmxYMnRsZVQxelpERXk=");
}


