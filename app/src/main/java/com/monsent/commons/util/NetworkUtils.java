package com.monsent.commons.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

/**
 * Created by lj on 2017/6/30.
 */

public class NetworkUtils {

    /**
     * 获取连接的网络信息
     *
     * @param context 上下文
     * @return 网络信息
     */
    public static NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager == null ? null : manager.getActiveNetworkInfo();
    }

    /**
     * 网络是否可用
     *
     * @param context 上下文
     * @return 网络是否可用
     */
    public static boolean isAvailable(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        return networkInfo != null && networkInfo.isAvailable();
    }

    /**
     * 网络是否连接
     *
     * @param context 上下文
     * @return 网络是否连接
     */
    public static boolean isConnected(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * WiFi是否连接
     *
     * @param context 上下文
     * @return WiFi是否连接
     */
    public static boolean isWifiConnected(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        return networkInfo != null && networkInfo.isConnected()
                && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * 移动网络是否连接
     *
     * @param context 上下文
     * @return 移动网络是否连接
     */
    public static boolean isMobileConnected(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        return networkInfo != null && networkInfo.isConnected()
                && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    /**
     * 获取本地MAC地址
     *
     * @return AC地址
     */
    public static String getMacAddress() {
        try {
            String name = "wlan0";
            List<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : networkInterfaces) {
                if (!networkInterface.getName().equalsIgnoreCase(name)) {
                    continue;
                }
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac == null) {
                    return null;
                }
                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) {
                    buf.append(String.format("%02X:", aMac));
                }
                if (buf.length() > 0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                return buf.toString();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
