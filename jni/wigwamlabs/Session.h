#ifndef WIGWAMLABS_SESSION_H_INCLUDED
#define WIGWAMLABS_SESSION_H_INCLUDED

#include <libspotify/api.h>

namespace wigwamlabs {

class Context;

class Session {
public:
    static Session *create(Context *context, const char *settingsPath, const char *cachePath, const char *deviceId, sp_error &outError);
    sp_error destroy();
    ~Session();
private:
    static void onNotifyMainThread(sp_session *session);
    static void onLogMessage(sp_session *session, const char *data);
    static void onConnectionError(sp_session *session, sp_error error);
    static void onLoggedIn(sp_session *session, sp_error error);
    static void onLoggedOut(sp_session *session);
    Session(Context *context);
    void onNotifyMainThread();
    void onLogMessage(const char *data);
    void onConnectionError(sp_error error);
    void onLoggedIn(sp_error error);
    void onLoggedOut();
private:
    Context *mContext;
    sp_session *mSession;
};

}

#endif // WIGWAMLABS_SESSION_H_INCLUDED
