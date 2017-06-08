package com.jiali.coolweather.util;

/**
 * Created by asus on 2017/5/4.
 */

import android.util.Log;

public class LogUtil {
    private static boolean isDebug = true;// 默认为调试模式，开发完毕后将其设为false

    public static void i(String tag, String msg) {
        if (isDebug) {
            Log.i(tag, msg);
        }
    }
    public static void i(Object object, String msg) {
        if (isDebug) {
            Log.i(object.getClass().getSimpleName(),msg);
        }
    }
    public static void e(String tag, String msg) {
        if (isDebug) {
            Log.e(tag, msg);
        }
    }
    public static void e(Object object, String msg) {
        if (isDebug) {
            Log.e(object.getClass().getSimpleName(),msg);
        }
    }

}
