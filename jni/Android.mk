LOCAL_PATH := $(call my-dir)

###################################################
# libspotify-jni
#
include $(CLEAR_VARS)
LOCAL_MODULE := libspotify-jni
LOCAL_SRC_FILES := \
    com_wigwamlabs_spotify_Playlist.cpp \
    com_wigwamlabs_spotify_PlaylistContainer.cpp \
    com_wigwamlabs_spotify_app_SpotifyContext.cpp \
    com_wigwamlabs_spotify_app_SpotifySession.cpp \
    ExceptionUtils.cpp \
    wigwamlabs/Context.cpp \
    wigwamlabs/Playlist.cpp \
    wigwamlabs/PlaylistContainer.cpp \
    wigwamlabs/Session.cpp \
#
LOCAL_SHARED_LIBRARIES += \
    libspotify
#
LOCAL_LDLIBS += \
    -llog
#
include $(BUILD_SHARED_LIBRARY)

###################################################
# libspotify
#
include $(CLEAR_VARS)
LOCAL_MODULE := libspotify
LOCAL_SRC_FILES := ../vendor/spotify/lib/libspotify.so
include $(PREBUILT_SHARED_LIBRARY)
