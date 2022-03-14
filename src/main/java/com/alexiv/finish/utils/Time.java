package com.alexiv.finish.utils;

import org.jetbrains.annotations.NotNull;

import static com.alexiv.finish.utils.Constants.TIMER_TEXT_FORMAT;

public class Time {
    private int _h = 0;
    private int _m = 0;
    private int _s = 0;

    public Time() {}
    public Time(Time t) {
        if (t != null) {
            _h = t.getHour();
            _m = t.getMinute();
            _s = t.getSecond();
        }
    }
    public Time(String t) {
        if (t != null) {
            setTime(t);
        }
    }
    public Time(int h, int m, int s) {
        setTime(h, m, s);
    }

    public int getHour() {
        return _h;
    }

    public int getMinute() {
        return _m;
    }

    public int getSecond() {
        return _s;
    }

    public void setHour(int h) {
        _h = h;
    }

    public void setMinute(int m) {
        _m = m;
    }

    public void setSecond(int s) {
        _s = s;
    }

    public String getTime() {
        return String.format(TIMER_TEXT_FORMAT, _h, _m, _s);
    }

    public void setTime(@NotNull String t) {
        String[] arr = t.split(":");
        _h = Integer.parseInt(arr[0]);
        _m = Integer.parseInt(arr[1]);
        _s = Integer.parseInt(arr[2]);
    }

    public void setTime(int h, int m, int s) {
        _h = h;
        _m = m;
        _s = s;
    }

    public boolean equals(Time time) {
        if (_h != time.getHour()) return false;
        if (_m != time.getMinute()) return false;
        if (_s != time.getSecond()) return false;
        return true;
    }
}
