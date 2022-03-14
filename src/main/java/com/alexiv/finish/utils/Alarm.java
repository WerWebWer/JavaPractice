package com.alexiv.finish.utils;

import com.alexiv.utils.Logger;
import com.alexiv.utils.ITime;
import com.alexiv.finish.utils.Time;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.alexiv.finish.utils.Constants.*;

public class Alarm implements ITime {
    private static final String TAG = Alarm.class.getName();

    public enum Status {
        RUN,
        PAUSE,
        STOP
    }

    private Status mStatus = Status.STOP;

    private int mHours = HOURS_DEFAULT;
    private int mMinute = MINUTE_DEFAULT;
    private int mSeconds = SECOND_DEFAULT;

    public static List<Time> mAlarms = new ArrayList<>();

    private volatile boolean running = true;
    private volatile boolean paused = false;
    private final Object pauseLock = new Object();
    @Nullable
    private Thread mThread = null;
    private final Runnable mRunnable = () -> {
        while (running) {
            synchronized (pauseLock) {
                if (!running) { // may have changed while waiting to
                    // synchronize on pauseLock
                    break;
                }
                if (paused) {
                    try {
                        synchronized (pauseLock) {
                            pauseLock.wait();
                            // will cause this Thread to block until
                            // another thread calls pauseLock.notifyAll()
                            // Note that calling wait() will
                            // relinquish the synchronized lock that this
                            // thread holds on pauseLock so another thread
                            // can acquire the lock to call notifyAll()
                            // (link with explanation below this code)
                        }
                    } catch (InterruptedException ex) {
                        break;
                    }
                    if (!running) { // running might have changed since we paused
                        break;
                    }
                }
            }
            if (!paused) {
                print();
                runAction();
                checkAlarm();
                if (!checkLastTime()) {
                    addTime(TIME_STEP_DEFAULT);
                    stepSleep();
                }
            }
        }
    };

    public interface MyTimerActions {
        void action(Time time);

        void startAction();
        void pauseAction();
        void stopAction();

        void endAction(); // when time is end - 0:0:0

        void alarm();
    }

    private List<MyTimerActions> mTimerActions = new ArrayList<>();

    public Alarm() {
    }

    public Alarm(int h, int m, int s) {
        mHours = h;
        mMinute = m;
        mSeconds = s;
        checkTime();
    }

    public void addMyTimerActions(@NotNull MyTimerActions actions) {
        mTimerActions.add(actions);
    }

    public void removeMyTimerActions(@NotNull MyTimerActions actions) {
        mTimerActions.remove(actions);
    }

    public void clearMyTimerActions() {
        mTimerActions.clear();
    }

    @Override
    public void start() {
        Logger.d(TAG, "start");
        if (mStatus == Status.RUN) {
            Logger.d(TAG, "already started");
            return;
        } else if (mStatus == Status.PAUSE) {
            resume();
            return;
        }
        mStatus = Status.RUN;
        running = true;
        paused = false;
        runStartAction();
        mThread = new Thread(mRunnable);
        mThread.start();
    }

    @Override
    public void pause() {
        Logger.d(TAG, "pause");
        mStatus = Status.PAUSE;
        paused = true;
        runPauseAction();
    }

    @Override
    public void stop() {
        Logger.d(TAG, "stop");
        mStatus = Status.STOP;
        running = false;
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll(); // Unblocks thread
        }
        mThread = null;
        runStopAction();
    }

    @Override
    public void resume() {
        Logger.d(TAG, "resume");
        mStatus = Status.RUN;
        if (mThread != null) {
            synchronized (pauseLock) {
                paused = false;
                pauseLock.notifyAll(); // Unblocks thread
            }
        }
    }

    @NotNull
    public Status getStatus() {
        return mStatus;
    }

    public void addTime(int s) {
        mSeconds += s;
        checkTime();
    }

    public void addAlarm(Time time) {
        Logger.d(TAG, "Added alarm " + time);
        mAlarms.add(time);
    }

    public void setTime(int h, int m, int s) {
        mHours = h;
        mMinute = m;
        mSeconds = s;
        checkTime();
    }

    public void print() {
        Logger.d(TAG, "MyTimer is [" + mHours + ":" + mMinute + ":" + mSeconds + "]");
    }

    private void stepSleep() {
        try {
            Thread.sleep(SLEEP_TREAD);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void checkAlarm() {
        for (Time t : mAlarms) {
            if (t.equals(new Time(mHours, mMinute, mSeconds))) {
                for (MyTimerActions actions : mTimerActions) {
                    actions.alarm();
                }
            }
        }
    }

    private boolean checkLastTime() {
        Logger.d(TAG, "" + mSeconds);
        if (mHours == 0 && mMinute == 0 && mSeconds == 0) {
            runEndAction();
            stop();
            return true;
        }
        return false;
    }

    private void checkTime() {
        if (mSeconds < 0) {
            mMinute -= 1;
            mSeconds = 59;
        }
        if (mMinute < 0) {
            mHours -= 1;
            mMinute = 59;
        }
        checkLastTime();
    }

    private void runAction() {
        Logger.d(TAG, "runAction");
        if (!mTimerActions.isEmpty()) {
            for (MyTimerActions actions : mTimerActions) {
                actions.action(new Time(mHours, mMinute, mSeconds));
            }
        }
    }

    private void runStartAction() {
        Logger.d(TAG, "runStartAction");
        if (!mTimerActions.isEmpty()) {
            for (MyTimerActions actions : mTimerActions) {
                actions.startAction();
            }
        }
    }

    private void runPauseAction() {
        Logger.d(TAG, "runPauseAction");
        if (!mTimerActions.isEmpty()) {
            for (MyTimerActions actions : mTimerActions) {
                actions.pauseAction();
            }
        }
    }

    private void runEndAction() {
        Logger.d(TAG, "runEndAction");
        if (!mTimerActions.isEmpty()) {
            for (MyTimerActions actions : mTimerActions) {
                actions.endAction();
            }
        }
    }

    private void runStopAction() {
        Logger.d(TAG, "runStopAction");
        if (!mTimerActions.isEmpty()) {
            for (MyTimerActions actions : mTimerActions) {
                actions.stopAction();
            }
        }
    }
}
