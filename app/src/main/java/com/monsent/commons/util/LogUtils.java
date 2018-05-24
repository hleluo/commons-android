package com.monsent.commons.util;

import android.util.Log;

/**
 * Created by lj on 2017/6/25.
 */

public class LogUtils {

    private final static boolean DEBUG = true;  //是否显示日志
    private final static String TAG_PREFIX = "Monsent-";

    /**
     * info日志
     *
     * @param tag 标签
     * @param msg 消息
     */
    public static void i(String tag, String msg) {
        if (DEBUG) {
            Log.i(TAG_PREFIX + (tag == null ? "info" : tag), msg);
        }
    }

    /**
     * info日志
     *
     * @param msg 消息
     */
    public static void i(String msg) {
        i(null, msg);
    }

    /**
     * error日志
     *
     * @param tag 标签
     * @param msg 消息
     */
    public static void e(String tag, String msg) {
        if (DEBUG) {
            Log.e(TAG_PREFIX + (tag == null ? "error" : tag), msg);
        }
    }

    /**
     * error日志
     *
     * @param msg 消息
     */
    public static void e(String msg) {
        e(null, msg);
    }

    /**
     * debug日志
     *
     * @param tag 标签
     * @param msg 消息
     */
    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(TAG_PREFIX + (tag == null ? "debug" : tag), msg);
        }
    }

    /**
     * debug日志
     *
     * @param msg 消息
     */
    public static void d(String msg) {
        d(null, msg);
    }

}
