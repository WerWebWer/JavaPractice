package com.alexiv.clock;

import com.alexiv.utils.Logger;
import com.alexiv.utils.Time;
import org.jetbrains.annotations.NotNull;

import static com.alexiv.utils.Constants.*;

public class Clock implements Time {

    private int mHours = 0;
    private int mMinute = 0;
    private int mSeconds = 0;
    private int mMilliSeconds = 0;

    private volatile boolean running = true;
    private volatile boolean paused = false;
    private final Object pauseLock = new Object();

    private int countPrint = 0;

    private Thread mThread = null;
    private Runnable mRunnable = () -> {
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
                addTime(0, 0, 0, -1);
                stepSleep();
            }
        }
    };

    public Clock() {
    }

    public Clock(int h, int m, int s) {
        mHours = h;
        mMinute = m;
        mSeconds = s;
        checkTime();
    }

    public Clock(int h, int m, int s, int ms) {
        mHours = h;
        mMinute = m;
        mSeconds = s;
        mMilliSeconds = ms;
        checkTime();
    }

    public void start() {
        Logger.d("Time start");
        mThread = new Thread(mRunnable);
        mThread.start();
    }

    public void start(@NotNull Runnable runnable) {
        Logger.d("Time start");
        mThread = new Thread(runnable);
        mThread.start();
    }

    public void stop() {
        Logger.d("Time stop");
        running = false;
        resume();
        mThread = null;
    }

    public void pause() {
        Logger.d("Time pause");
        paused = true;
    }

    public void resume() {
        Logger.d("Time resume");
        if (mThread != null) {
            synchronized (pauseLock) {
                paused = false;
                pauseLock.notifyAll(); // Unblocks thread
            }
        }
    }

    public void stepSleep() {
        try {
            Thread.sleep(STEP_TIME);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    public String getTime() {
        return String.format(TIME_FORMAT, mHours, mMinute, mSeconds, mMilliSeconds);
    }

    @NotNull
    public void print() {
        if (countPrint >= 500) {
            System.out.println(String.format(PRINT_TIME_FORMAT, getTime()));
            countPrint = 0;
        } else {
            countPrint++;
        }
    }

    public void setTime(@NotNull String time) {
        String[] timeArray = time.split(":");
        mHours =  Integer.parseInt(timeArray[0]);
        mMinute =  Integer.parseInt(timeArray[1]);
        mSeconds =  Integer.parseInt(timeArray[2]);
        checkTime();
    }

    public void setTime(int h, int m, int s) {
        mHours = h;
        mMinute = m;
        mSeconds = s;
        checkTime();
    }

    public void addTime(int h, int m, int s, int ms) {
        mHours += h;
        mMinute += m;
        mSeconds += s;
        mMilliSeconds += ms;
        checkTime();
    }

    private void checkTime() {
        if (mMilliSeconds >= MAX_MILLISECONDS) {
            mSeconds += mMilliSeconds / MAX_MILLISECONDS;
            mMilliSeconds = mMilliSeconds % MAX_MILLISECONDS;
        } else if (mMilliSeconds < 0) {
            mSeconds -= mSeconds / MAX_MILLISECONDS + 1;
            mMilliSeconds = Math.abs(mMilliSeconds % MAX_MILLISECONDS);
        }
        if (mSeconds >= MAX_SECONDS) {
            mMinute += mSeconds / MAX_SECONDS;
            mSeconds = mSeconds % MAX_SECONDS;
        } else if (mSeconds < 0) {
            mMinute -= mSeconds / MAX_SECONDS + 1;
            mSeconds = Math.abs(mSeconds % MAX_SECONDS);
        }
        if (mMinute >= MAX_MINUTE) {
            mHours += mMinute / MAX_MINUTE;
            mMinute = mMinute % MAX_MINUTE;
        } else if (mMinute < 0) {
            mHours -= mMinute / MAX_MINUTE + 1;
            mMinute = Math.abs(mMinute % MAX_MINUTE);
        }
        if (mHours >= MAX_HOURS) {
            mHours = mHours % MAX_HOURS;
        } else if (mHours < 0) {
            mHours = 0;
            mMinute = 0;
            mSeconds = 0;
        }
    }
}
