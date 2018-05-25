package com.monsent.commons.util;

import android.content.Context;
import android.os.PowerManager;

import java.util.UUID;

public class SystemUtils {

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

    public static PowerManager.WakeLock setKeepCpuRunning(Context context) {
        return acquireWakeLock(context, PowerManager.PARTIAL_WAKE_LOCK);
    }

}
