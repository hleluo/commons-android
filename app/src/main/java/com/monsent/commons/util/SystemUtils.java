package com.monsent.commons.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.provider.Settings;

import java.util.UUID;

public class SystemUtils {

    private final static String SP_SYSTEM_FILENAME = "sp_system_policy";
    private final static String KEY_WIFI_SLEEP_POLICY = "WIFI_SLEEP_POLICY";

    /**
     * 保持休眠锁
     *
     * @param context 上下文
     * @param flags   标识
     * @return 休眠锁
     */
    public static PowerManager.WakeLock acquireWakeLock(Context context, int flags) {
        PowerManager manager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = null;
        if (manager != null) {
            String tag = UUID.randomUUID().toString();
            wakeLock = manager.newWakeLock(flags, tag);
            wakeLock.acquire(3000);
        }
        return wakeLock;
    }

    /**
     * 保持CPU不休眠
     *
     * @param context 上下文
     * @return 休眠锁
     */
    public static PowerManager.WakeLock setKeepCpuRunning(Context context) {
        return acquireWakeLock(context, PowerManager.PARTIAL_WAKE_LOCK);
    }

    /**
     * 设置wifi不休眠
     *
     * @param context 上下文
     */
    public static void setWifiDormancy(Context context) {
        int defaultPolicy = Settings.System.getInt(context.getContentResolver(), Settings.System.WIFI_SLEEP_POLICY, Settings.System.WIFI_SLEEP_POLICY_DEFAULT);
        final SharedPreferences prefs = context.getSharedPreferences(SP_SYSTEM_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_WIFI_SLEEP_POLICY, defaultPolicy);
        editor.apply();
        if (Settings.System.WIFI_SLEEP_POLICY_NEVER != defaultPolicy) {
            Settings.System.putInt(context.getContentResolver(), Settings.System.WIFI_SLEEP_POLICY, Settings.System.WIFI_SLEEP_POLICY_NEVER);
        }
    }

    /**
     * 获取wifi休眠策略
     *
     * @param context 上下文
     */
    public static void restoreWifiDormancy(Context context) {
        final SharedPreferences prefs = context.getSharedPreferences(SP_SYSTEM_FILENAME, Context.MODE_PRIVATE);
        int defaultPolicy = prefs.getInt(KEY_WIFI_SLEEP_POLICY, Settings.System.WIFI_SLEEP_POLICY_DEFAULT);
        Settings.System.putInt(context.getContentResolver(), Settings.System.WIFI_SLEEP_POLICY, defaultPolicy);
    }


}
