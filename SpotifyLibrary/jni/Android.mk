LOCAL_PATH := $(call my-dir)

###################################################
# libspotify-jni
#
include $(CLEAR_VARS)
LOCAL_MODULE := libspotify-jni
LOCAL_SRC_FILES := \
    ExceptionUtils.cpp \
    JNIEnvProvider.cpp \
    com_wigwamlabs_spotify_Artist.cpp \
    com_wigwamlabs_spotify_FolderEnd.cpp \
    com_wigwamlabs_spotify_FolderStart.cpp \
    com_wigwamlabs_spotify_Placeholder.cpp \
    com_wigwamlabs_spotify_Player.cpp \
    com_wigwamlabs_spotify_Playlist.cpp \
    com_wigwamlabs_spotify_PlaylistContainer.cpp \
    com_wigwamlabs_spotify_Session.cpp \
    com_wigwamlabs_spotify_Track.cpp \
    wigwamlabs/Artist.cpp \
    wigwamlabs/FolderEnd.cpp \
    wigwamlabs/FolderStart.cpp \
    wigwamlabs/Placeholder.cpp \
    wigwamlabs/Player.cpp \
    wigwamlabs/Playlist.cpp \
    wigwamlabs/PlaylistContainer.cpp \
    wigwamlabs/Session.cpp \
    wigwamlabs/Track.cpp \
#
LOCAL_SHARED_LIBRARIES += \
    libspotify \
#
LOCAL_LDLIBS += \
    -llog \
    -lOpenSLES \
#
include $(BUILD_SHARED_LIBRARY)

###################################################
# libspotify
#
include $(CLEAR_VARS)
LOCAL_MODULE := libspotify
LOCAL_SRC_FILES := ../../vendor/spotify/lib/libspotify.so
include $(PREBUILT_SHARED_LIBRARY)
