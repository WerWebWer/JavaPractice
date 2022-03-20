package com.alexiv.finish.time;

public interface AlarmCallback {
    void action(Time time);

    void startAction();
    void pauseAction();
    void resumeAction();
    void stopAction();

    void endAction(); // when time is end - 0:0:0

    void alarm(Time time, String action);
}
