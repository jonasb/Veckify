#ifndef WIGWAMLABS_SESSION_H_INCLUDED
#define WIGWAMLABS_SESSION_H_INCLUDED

#include <pthread.h>
#include <libspotify/api.h>

namespace wigwamlabs {

class PlaylistContainer;
class Player;

class SessionCallback {
public:
    virtual void onLoggedIn(sp_error error) = 0;
    virtual void onMetadataUpdated() = 0;
    virtual void onCredentialsBlobUpdated(const char *blob) = 0;
    virtual void onConnectionStateUpdated(int state) = 0;
    virtual void onOfflineTracksToSyncChanged(bool syncing, int tracks) = 0;
};

class Session {
public:
    static Session *create(SessionCallback *callback, const char *settingsPath, const char *cachePath, const char *deviceId, sp_error &outError);
    sp_error destroy();
    ~Session();

    sp_session *getSession();

    sp_error setStreamingBitrate(sp_bitrate bitrate);
    sp_error setOfflineBitrate(sp_bitrate bitrate);
    sp_error setConnectionType(sp_connection_type type);
    sp_error setConnectionRules(sp_connection_rules connectionRules);
    int getConnectionState() const;
    sp_error login(const char *username, const char *password, const char *blob);
    sp_error logout();
    PlaylistContainer *getPlaylistContainer();
    Player *getPlayer();
private:
    static void *mainThreadLoop(void *self);
    static void onLoggedIn(sp_session *session, sp_error error);
    static void onLoggedOut(sp_session *session);
    static void onMetadataUpdated(sp_session *session);
    static void onConnectionError(sp_session *session, sp_error error);
    static void onMessageToUser(sp_session *session, const char *message);
    static void onNotifyMainThread(sp_session *session);
    static int onMusicDelivery(sp_session *session, const sp_audioformat *format, const void *frames, int numFrames);
    static void onPlayTokenLost(sp_session *session);
    static void onLogMessage(sp_session *session, const char *data);
    static void onStreamingError(sp_session *session, sp_error error);
    static void onStartPlayback(sp_session *session);
    static void onStopPlayback(sp_session *session);
    static void onGetAudioBufferStats(sp_session *session, sp_audio_buffer_stats *stats);
    static void onOfflineStatusUpdated(sp_session *session);
    static void onOfflineError(sp_session *session, sp_error error);
    static void onEndOfTrack(sp_session *session);
    static void onCredentialsBlobUpdated(sp_session *session, const char *blob);
    static void onConnectionStateUpdated(sp_session *session);

    Session(SessionCallback *callback);
    sp_error startThread();
    void *mainThreadLoop();
    void onLoggedIn(sp_error error);
    void onLoggedOut();
    void onConnectionError(sp_error error);
    void onMessageToUser(const char *message);
    void onNotifyMainThread();
    void onLogMessage(const char *data);
    void onConnectionStateUpdated();
private:
    SessionCallback *mCallback;
    sp_session *mSession;
    pthread_t mMainThread;
    pthread_mutex_t mMainNotifyMutex;
    pthread_cond_t mMainNotifyCond;
    int mMainNotifyDo;
    bool mMainThreadRunning;
    Player *mPlayer;
    bool mWaitingForLoggedIn;
    sp_error mOfflineError;
};

}

#endif // WIGWAMLABS_SESSION_H_INCLUDED
