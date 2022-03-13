package com.alexiv.timer;

import com.alexiv.utils.Logger;
import com.alexiv.utils.Time;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.alexiv.utils.Constants.*;

public class MyTimer implements Time {
    private static final String TAG = MyTimer.class.getName();

    enum Status {
        RUN,
        STOP,
        PAUSE
    }

    private Status mStatus = Status.STOP;

    private int mSeconds = SECOND_DEFAULT;

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
                checkLastTime();
                print();
                addTime(SECOND_STEP_DEFAULT);
                runAction();
                stepSleep();
            }
        }
    };

    @Nullable
    private Runnable mAction = null;
    @Nullable
    private Runnable mStopAction = null;

    public MyTimer() {
    }

    public MyTimer(int s) {
        if (s > 0) {
            mSeconds = s;
        } else {
            mSeconds = SECOND_DEFAULT;
        }
    }

    @Override
    public void start() {
        Logger.d(TAG, "start");
        if (mStatus == Status.RUN) {
            Logger.d(TAG, "already started");
            return;
        } else if (mStatus == Status.PAUSE) {
            resume();
        }
        mStatus = Status.RUN;
        running = true;
        paused = false;
        mThread = new Thread(mRunnable);
        mThread.start();
    }

    @Override
    public void pause() {
        Logger.d(TAG, "pause");
        mStatus = Status.PAUSE;
        // TODO
    }

    @Override
    public void stop() {
        Logger.d(TAG, "stop");
        mStatus = Status.STOP;
        mAction = null;
        mStopAction = null;
        running = false;
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll(); // Unblocks thread
        }
        mThread = null;
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
    }

    public void setTime(int s) {
        mSeconds = s;
    }

    public int getTime() {
        return mSeconds;
    }

    public void setAction(@NotNull Runnable runnable) {
        mAction = runnable;
    }

    private void runAction() {
        if (mAction != null) {
            new Thread(mAction).start();
        }
    }

    public void setStopAction(@NotNull Runnable runnable) {
        mStopAction = runnable;
    }

    private void runStopAction() {
        if (mStopAction != null) {
            new Thread(mStopAction).start();
        }
    }

    public void print() {
        Logger.d(TAG, "MyTimer is " + mSeconds);
    }

    private void stepSleep() {
        try {
            Thread.sleep(STEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void checkLastTime() {
        if (mSeconds == 0) {
            runStopAction();
            stop();
        }
    }
}
