#ifndef LOG_H_INCLUDED
#define LOG_H_INCLUDED

#include <android/log.h>

#ifndef LOG_NDEBUG
#define LOG_NDEBUG 0
#endif

#ifndef LOG_TAG
#define LOG_TAG NULL
#endif

#if LOG_NDEBUG
#define LOGV(...)   ((void)0)
#else
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG,__VA_ARGS__)
#endif

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , LOG_TAG,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO   , LOG_TAG,__VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN   , LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , LOG_TAG,__VA_ARGS__)

#endif // LOG_H_INCLUDED
