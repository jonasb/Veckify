#ifndef WIGWAMLABS_SESSION_H_INCLUDED
#define WIGWAMLABS_SESSION_H_INCLUDED

#include <pthread.h>
#include <libspotify/api.h>

namespace wigwamlabs {

class Context;

class Session {
public:
    static Session *create(Context *context, const char *settingsPath, const char *cachePath, const char *deviceId, sp_error &outError);
    sp_error destroy();
    ~Session();
    bool relogin();
    sp_error login(const char *username, const char *password, bool rememberMe);
private:
    static void *mainThreadLoop(void *self);
    static void onLoggedIn(sp_session *session, sp_error error);
    static void onLoggedOut(sp_session *session);
    static void onConnectionError(sp_session *session, sp_error error);
    static void onMessageToUser(sp_session *session, const char *message);
    static void onNotifyMainThread(sp_session *session);
    static void onLogMessage(sp_session *session, const char *data);
    static void onConnectionStateUpdated(sp_session *session);

    Session(Context *context);
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
    Context *mContext;
    sp_session *mSession;
    pthread_t mMainThread;
    pthread_mutex_t mMainNotifyMutex;
    pthread_cond_t mMainNotifyCond;
    int mMainNotifyDo;
    bool mMainThreadRunning;
};

}

#endif // WIGWAMLABS_SESSION_H_INCLUDED
