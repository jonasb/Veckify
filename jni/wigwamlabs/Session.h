#ifndef WIGWAMLABS_SESSION_H_INCLUDED
#define WIGWAMLABS_SESSION_H_INCLUDED

namespace wigwamlabs {

class Context;

class Session {
public:
    Session(Context *context);
    ~Session();
private:
    Context *mContext;
};

}

#endif // WIGWAMLABS_SESSION_H_INCLUDED
