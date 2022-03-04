package com.alexiv.utils;

import org.jetbrains.annotations.NotNull;

public class Logger {

    private static final String LOG_FORMAT = "[%s]: %s";

    public static void d(@NotNull String msg) {
        System.out.println(msg);
    }

    public static void d(@NotNull String tag, @NotNull String msg) {
        System.out.printf((LOG_FORMAT) + "%n", tag, msg);
    }
}
