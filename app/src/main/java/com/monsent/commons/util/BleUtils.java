package com.monsent.commons.util;

import android.content.Context;
import android.content.pm.PackageManager;

public class BleUtils {

    /**
     * 判断是否支持ble
     * @param context 上下文
     * @return 是否支持ble
     */
    public static boolean hasBleFeature(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

}
