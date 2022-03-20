package com.alexiv.finish.utils;

public class TextUtils {
    public static boolean isEmpty(String text) {
        if (text == null) return true;
        if (text.isEmpty()) return true;
        return false;
    }
}
