package com.alexiv.finish.time;

import com.alexiv.finish.utils.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.alexiv.finish.utils.Constants.*;

public class Alarm implements ITime {
    private static final String TAG = Alarm.class.getSimpleName();

    private static final String TIME_TEXT_FORMAT = "%s [%s:%s:%s]";

    @NotNull
    private final List<AlarmCallback> mTimerActions = new ArrayList<>();
    @NotNull
    private final ArrayList<Data> mAlarms = new ArrayList<>();

    private StatusTime mStatus = StatusTime.STOP;

    private int mHours = HOURS_DEFAULT;
    private int mMinute = MINUTE_DEFAULT;
    private int mSeconds = SECOND_DEFAULT;

    // region thread
    private volatile boolean running = true;
    private volatile boolean paused = false;
    @NotNull
    private final Object pauseLock = new Object();
    @Nullable
    private Thread mThread = null;
    private final Runnable mRunnable = () -> {
        while (running) {
            synchronized (pauseLock) {
                if (!running) {
                    break;
                }
                if (paused) {
                    try {
                        synchronized (pauseLock) {
                            pauseLock.wait();
                        }
                    } catch (InterruptedException ex) {
                        break;
                    }
                    if (!running) {
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
    // endregion thread

    public Alarm() {
        checkTime();
    }

    public Alarm(int h, int m, int s) {
        mHours = h;
        mMinute = m;
        mSeconds = s;
        checkTime();
    }

    @Override
    public void start() {
        Logger.d(TAG, "start");
        if (mStatus == StatusTime.RUN) {
            Logger.d(TAG, "already started");
            return;
        } else if (mStatus == StatusTime.PAUSE) {
            resume();
            return;
        }
        mStatus = StatusTime.RUN;
        running = true;
        paused = false;
        runStartAction();
        mThread = new Thread(mRunnable);
        mThread.start();
    }

    @Override
    public void pause() {
        Logger.d(TAG, "pause");
        mStatus = StatusTime.PAUSE;
        paused = true;
        runPauseAction();
    }

    @Override
    public void stop() {
        Logger.d(TAG, "stop");
        mStatus = StatusTime.STOP;
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
        mStatus = StatusTime.RUN;
        if (mThread != null) {
            synchronized (pauseLock) {
                paused = false;
                pauseLock.notifyAll(); // Unblocks thread
            }
        }
        runResumeAction();
    }

    public void addAlarmCallback(@NotNull AlarmCallback actions) {
        mTimerActions.add(actions);
    }

    public void removeAlarmCallback(@NotNull AlarmCallback actions) {
        mTimerActions.remove(actions);
    }

    public void clearAlarmCallback() {
        mTimerActions.clear();
    }

    @NotNull
    public StatusTime getStatus() {
        return mStatus;
    }

    public void addTime(int s) {
        checkTime();
        mSeconds += s;
    }

    public void addAlarm(@NotNull Time time, @Nullable String alarmText) {
        Logger.d(TAG, "addAlarm: time = " + time.getTime());
        if (time == null) return;
        boolean contains = false;
        for (Data data : mAlarms) {
            if (data.getTime().equals(time)) {
                contains = true;
                Logger.d(TAG, "addAlarm: alarm already is " + time.getTime() + " add action =  " + alarmText);
                data.addAction(alarmText);
            }
        }
        if (!contains) {
            Logger.d(TAG, "addAlarm: " + time.getTime() + " = " + alarmText);
            mAlarms.add(new Data(time, alarmText));
        }
    }

    public void setTime(int h, int m, int s) {
        mHours = h;
        mMinute = m;
        mSeconds = s;
        checkTime();
    }

    private void print() {
        Logger.d(TAG, String.format(TIME_TEXT_FORMAT, "Timer is", mHours, mMinute, mSeconds));
    }

    private void stepSleep() {
        try {
            Thread.sleep(SLEEP_TREAD);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void checkAlarm() {
        Time now = new Time(mHours, mMinute, mSeconds);
        for (Data data : mAlarms) {
            if (data.getTime().equals(now)) {
                for (AlarmCallback actions : mTimerActions) {
                    actions.alarm(data.getTime(), data.getAction());
                }
                mAlarms.remove(data);
                return;
            }
        }
    }

    private boolean checkLastTime() {
        if (mHours == HOURS_MIN && mMinute == MINUTE_MIN && mSeconds == SECOND_MIN) {
            runEndAction();
            stop();
            return true;
        }
        return false;
    }

    private void checkTime() {
        if (mSeconds < SECOND_MIN) {
            mMinute -= 1;
            mSeconds = SECOND_MAX;
        }
        if (mMinute < MINUTE_MIN) {
            mHours -= 1;
            mMinute = MINUTE_MAX;
        }
        if (mHours < 0) {
            mHours = HOURS_MIN;
        }
        checkLastTime();
    }

    private void runAction() {
        Logger.d(TAG, "runAction");
        if (!mTimerActions.isEmpty()) {
            for (AlarmCallback actions : mTimerActions) {
                actions.action(new Time(mHours, mMinute, mSeconds));
            }
        }
    }

    private void runStartAction() {
        Logger.d(TAG, "runStartAction");
        if (!mTimerActions.isEmpty()) {
            for (AlarmCallback actions : mTimerActions) {
                actions.startAction();
            }
        }
    }

    private void runPauseAction() {
        Logger.d(TAG, "runPauseAction");
        if (!mTimerActions.isEmpty()) {
            for (AlarmCallback actions : mTimerActions) {
                actions.pauseAction();
            }
        }
    }

    private void runResumeAction() {
        Logger.d(TAG, "runResumeAction");
        if (!mTimerActions.isEmpty()) {
            for (AlarmCallback actions : mTimerActions) {
                actions.resumeAction();
            }
        }
    }

    private void runEndAction() {
        Logger.d(TAG, "runEndAction");
        if (!mTimerActions.isEmpty()) {
            for (AlarmCallback actions : mTimerActions) {
                actions.endAction();
            }
        }
    }

    private void runStopAction() {
        Logger.d(TAG, "runStopAction");
        if (!mTimerActions.isEmpty()) {
            for (AlarmCallback actions : mTimerActions) {
                actions.stopAction();
            }
        }
    }
}
