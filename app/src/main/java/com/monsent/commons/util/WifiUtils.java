package com.monsent.commons.util;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import java.lang.reflect.Method;
import java.util.List;

public class WifiUtils {

    /**
     * Wifi加密类型
     */
    public enum WifiCipherType {
        TYPE_WEP, TYPE_WPA_EAP, TYPE_WPA_PSK, TYPE_WPA2_PSK, TYPE_NOPASS
    }

    /**
     * 根据上下文获取WifiManager
     *
     * @param context 上下文
     * @return WifiManager
     */
    private static WifiManager getWifiManager(Context context) {
        return (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * 设置wifi状态
     *
     * @param context 上下文
     * @param enabled 状态
     * @return 是否设置成功
     */
    private static boolean setWifiEnabled(Context context, boolean enabled) {
        WifiManager manager = getWifiManager(context);
        return manager.isWifiEnabled() == enabled || manager.setWifiEnabled(enabled);
    }

    /**
     * 打开wifi
     *
     * @param context 上下文
     * @return 是否打开成功
     */
    public static boolean openWifi(Context context) {
        return setWifiEnabled(context, true);
    }

    /**
     * 关闭wifi
     *
     * @param context 上下文
     * @return 是否关闭成功
     */
    public static boolean closeWifi(Context context) {
        return setWifiEnabled(context, false);
    }

    /**
     * 创建Wifi热点
     *
     * @param context  上下文
     * @param ssid     热点名称
     * @param password 密码
     * @return 是否创建成功
     */
    public static boolean createWifiAp(Context context, String ssid, String password) {
        WifiManager manager = getWifiManager(context);
        if (manager.isWifiEnabled()) {
            //如果wifi处于打开状态，则关闭wifi
            manager.setWifiEnabled(false);
        }
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = ssid;
        configuration.preSharedKey = password;
        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        try {
            Method method = manager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            return (Boolean) method.invoke(manager, configuration, true);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Wifi热点开关是否打开
     *
     * @param context 上下文
     * @return Wifi热点开关是否打开
     */
    public static boolean isWifiApEnabled(Context context) {
        WifiManager manager = getWifiManager(context);
        try {
            Method method = manager.getClass().getMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(manager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 关闭WiFi热点
     *
     * @param context 上下文
     */
    public static void closeWifiAp(Context context) {
        if (!isWifiApEnabled(context)) {
            return;
        }
        WifiManager manager = getWifiManager(context);
        try {
            Method methodConfig = manager.getClass().getMethod("getWifiApConfiguration");
            methodConfig.setAccessible(true);
            WifiConfiguration config = (WifiConfiguration) methodConfig.invoke(manager);
            Method method = manager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(manager, config, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取wifi热点配置信息
     *
     * @param context 上下文
     * @return wifi热点配置信息
     */
    public static WifiConfiguration getWifiApConfig(Context context) {
        WifiManager manager = getWifiManager(context);
        try {
            Method method = manager.getClass().getMethod("getWifiApConfiguration");
            return (WifiConfiguration) method.invoke(manager);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取wifi状态
     *
     * @param context 上下文
     * @return wifi状态
     */
    public static int getWifiState(Context context) {
        WifiManager manager = getWifiManager(context);
        return manager.getWifiState();
    }

    /**
     * 获取当前已连接的Wifi信息
     *
     * @param context 上下文
     * @return 获取当前已连接的Wifi信息
     */
    public static WifiInfo getConnectionInfo(Context context) {
        WifiManager manager = getWifiManager(context);
        return manager.getConnectionInfo();
    }

    /**
     * 连接网络
     *
     * @param context  上下文
     * @param ssid     名称
     * @param password 密码
     * @param type     网络类型
     * @return 是否成功
     */
    public static boolean connect(Context context, String ssid, String password, WifiUtils.WifiCipherType type) {
        WifiConfiguration config = createWifiConfiguration(context, ssid, password, type);
        if (config != null) {
            WifiManager manager = getWifiManager(context);
            int networkId = manager.addNetwork(config);
            return manager.enableNetwork(networkId, true);
        }
        return false;
    }

    /**
     * 断开网络连接
     *
     * @param context 上下文
     * @param ssid    网络名
     * @return 是否成功
     */
    public static boolean disconnect(Context context, String ssid) {
        WifiConfiguration config = getWifiConfiguration(context, ssid);
        if (config == null) {
            return true;
        }
        try {
            WifiManager manager = getWifiManager(context);
            manager.disableNetwork(config.networkId);
            return manager.disconnect();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 扫描Wifi
     *
     * @param context 上下文
     * @return 是否成功
     */
    public static boolean startScan(Context context) {
        WifiManager manager = getWifiManager(context);
        return manager.startScan();
    }

    /**
     * 获取指定ssid的配置信息
     *
     * @param context 上下文
     * @param ssid    网络名
     * @return 配置信息
     */
    public static WifiConfiguration getWifiConfiguration(Context context, String ssid) {
        WifiManager manager = getWifiManager(context);
        List<WifiConfiguration> configurations = manager.getConfiguredNetworks();
        if (configurations != null) {
            for (WifiConfiguration configuration : configurations) {
                if (configuration.SSID.equals("\"" + ssid + "\"")) {
                    return configuration;
                }
            }
        }
        return null;
    }

    /**
     * 创建wifi配置信息
     *
     * @param context  上下文
     * @param ssid     网络名
     * @param password 密码
     * @param type     网络类型
     * @return 配置信息
     */
    public static WifiConfiguration createWifiConfiguration(Context context, String ssid, String password, WifiUtils.WifiCipherType type) {
        WifiConfiguration config = getWifiConfiguration(context, ssid);
        if (config != null) {
            // 本机之前配置过此wifi热点，直接移除
            WifiManager manager = getWifiManager(context);
            manager.removeNetwork(config.networkId);
        }
        config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        //如果设备大于6.0配置的时候就不需要双引号，加了就连接不上了
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            config.SSID = ssid;
        } else {
            config.SSID = "\"" + ssid + "\"";
        }
        config.status = WifiConfiguration.Status.ENABLED;
        if (type == WifiUtils.WifiCipherType.TYPE_NOPASS) {   // WIFICIPHER_NOPASS
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == WifiUtils.WifiCipherType.TYPE_WEP) {// WIFICIPHER_WEP
            config.hiddenSSID = true;
            config.preSharedKey = "\"" + password + "\"";
//            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == WifiUtils.WifiCipherType.TYPE_WPA_EAP) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.status = WifiConfiguration.Status.ENABLED;
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN | WifiConfiguration.Protocol.WPA);
        } else if (type == WifiUtils.WifiCipherType.TYPE_WPA_PSK) {
            config.preSharedKey = "\"" + password + "\"";
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN | WifiConfiguration.Protocol.WPA);
        } else if (type == WifiUtils.WifiCipherType.TYPE_WPA2_PSK) {
            config.preSharedKey = "\"" + password + "\"";
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        }
        return config;
    }

}
