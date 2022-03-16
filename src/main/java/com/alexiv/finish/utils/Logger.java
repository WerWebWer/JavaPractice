package com.alexiv.finish.utils;

import org.jetbrains.annotations.NotNull;

import static com.alexiv.finish.utils.Constants.LOGGER_FORMAT;

public class Logger {
    private static final String TAG = Logger.class.getSimpleName();

    public static void d(@NotNull String msg) {
        System.out.println(msg);
    }

    public static void d(@NotNull String tag, @NotNull String msg) {
        System.out.printf(LOGGER_FORMAT, tag, msg);
    }
}
