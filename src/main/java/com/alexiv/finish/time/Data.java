package com.alexiv.finish.time;

import com.alexiv.finish.utils.TextUtils;
import org.jetbrains.annotations.NotNull;

public class Data {

    @NotNull
    private Time mTime;
    private String mAction;

    Data(@NotNull Time time, String action) {
        mTime = time;
        mAction = action;
    }

    public Time getTime() {
        return mTime;
    }

    public String getAction() {
        return "\"" + mAction + "\"";
    }

    public void addAction(String action) {
        if (TextUtils.isEmpty(action)) return;
        if (TextUtils.isEmpty(mAction)) {
            mAction = action;
            return;
        }
        mAction = mAction + "\", \"" + action;
    }
}
