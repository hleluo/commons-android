package com.monsent.commons.util;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by lj on 2017/7/2.
 */

public class BluetoothUtils {

    private static BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

    /**
     * 是否可用
     *
     * @return 是否可用
     */
    public static boolean isEnabled() {
        return adapter != null && adapter.isEnabled();
    }

    /**
     * 设置是否可用
     *
     * @param enabled 是否可用
     * @return 是否设置成功
     */
    public static boolean setEnabled(boolean enabled) {
        if (adapter == null) {
            return !enabled;
        }
        if (enabled) {
            return adapter.isEnabled() || adapter.enable();
        } else {
            return !adapter.isEnabled() || adapter.disable();
        }
    }

    /**
     * 打开蓝牙
     *
     * @return 是否打开成功
     */
    public static boolean enable() {
        return setEnabled(true);
    }

    /**
     * 关闭蓝牙
     *
     * @return 是否关闭成功
     */
    public static boolean disable() {
        return setEnabled(false);
    }

    /**
     * 发送文件
     *
     * @param context 上下文
     * @param file    文件
     */
    public static void sendFile(Context context, File file) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent();
        Map<String, ActivityInfo> map = new HashMap<>();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        intent.setType("*/*");
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String name = resolveInfo.activityInfo.processName;
            if (name.contains("bluetooth")) {
                map.put(name, resolveInfo.activityInfo);
            }
        }
        ActivityInfo activityInfo = map.get("com.android.bluetooth");
        activityInfo = activityInfo == null ? map.get("com.mediatek.bluetooth") : activityInfo;
        if (activityInfo == null) {
            Iterator<ActivityInfo> iterator = map.values().iterator();
            if (iterator.hasNext()) {
                activityInfo = iterator.next();
            }
        }
        if (activityInfo != null) {
            intent.setComponent(new ComponentName(activityInfo.packageName, activityInfo.name));
            ((Activity) context).startActivityForResult(intent, 4098);
        }
    }

}
