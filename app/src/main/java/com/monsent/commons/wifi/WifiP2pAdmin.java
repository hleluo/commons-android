package com.monsent.commons.wifi;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Looper;

import java.util.Collection;

public class WifiP2pAdmin {

    public interface ChannelCallback {
        void onCreateGroup(boolean success);

        void onRemoveGroup(boolean success);

        void onConnect(boolean success);
    }

    public interface PeerCallback {

        void onStateChanged(boolean enabled);

        void onPeersChanged();

        void onConnectionChanged(NetworkInfo networkInfo);

        void onPeersAvailable(Collection<WifiP2pDevice> devices);

        void onConnectionInfoAvailable(WifiP2pInfo info);
    }

    private Context context;
    private WifiP2pReceiver receiver;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private ChannelCallback channelCallback;
    private PeerCallback peerCallback;

    public WifiP2pAdmin(Context context) {
        this.context = context;
        this.manager = (WifiP2pManager) context.getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE);
        this.receiver = new WifiP2pReceiver();
    }

    public void setChannelCallback(ChannelCallback channelCallback) {
        this.channelCallback = channelCallback;
    }

    public void setPeerCallback(PeerCallback peerCallback) {
        this.peerCallback = peerCallback;
    }

    /**
     * 初始化
     *
     * @param looper looper
     */
    public void initialize(Looper looper) {
        looper = looper == null ? context.getMainLooper() : looper;
        this.channel = manager.initialize(this.context, looper, null);
    }

    /**
     * 初始化
     */
    public void initialize() {
        initialize(null);
    }

    /**
     * 注册广播
     */
    public void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        if (Build.VERSION.SDK_INT >= 16) {
            filter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        }
        context.registerReceiver(receiver, filter);
    }

    /**
     * 注销广播
     */
    public void unregisterReceiver() {
        context.unregisterReceiver(receiver);
    }

    /**
     * 创建组
     */
    public void createGroup() {
        manager.createGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                if (channelCallback != null) {
                    channelCallback.onCreateGroup(true);
                }
            }

            @Override
            public void onFailure(int reason) {
                if (channelCallback != null) {
                    channelCallback.onCreateGroup(false);
                }
            }
        });
    }

    /**
     * 移除组
     */
    public void removeGroup() {
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                if (channelCallback != null) {
                    channelCallback.onRemoveGroup(true);
                }
            }

            @Override
            public void onFailure(int i) {
                if (channelCallback != null) {
                    channelCallback.onRemoveGroup(false);
                }
            }
        });
    }

    /**
     * 扫描对等点
     */
    public void discoverPeers() {
        manager.discoverPeers(channel, null);
    }

    /**
     * 停止扫描对等点
     */
    @TargetApi(16)
    public void stopPeerDiscovery() {
        manager.stopPeerDiscovery(channel, null);
    }

    /**
     * 连接对等点
     *
     * @param address          对等点地址
     * @param groupOwnerIntent 组长推荐值，0-15
     */
    public void connect(String address, int groupOwnerIntent) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = address;
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = groupOwnerIntent;
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                if (channelCallback != null) {
                    channelCallback.onConnect(true);
                }
            }

            @Override
            public void onFailure(int reason) {
                if (channelCallback != null) {
                    channelCallback.onConnect(false);
                }
            }
        });
    }

    /**
     * 请求获取对等点列表
     */
    public void requestPeers() {
        manager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                if (peerCallback != null) {
                    peerCallback.onPeersAvailable(wifiP2pDeviceList.getDeviceList());
                }
            }
        });
    }

    /**
     * 请求连接信息
     */
    public void requestConnectionInfo() {
        manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
                if (peerCallback != null) {
                    peerCallback.onConnectionInfoAvailable(wifiP2pInfo);
                }
            }
        });
    }

    public class WifiP2pReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                //确定Wi-Fi Direct模式是否已经启用
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (peerCallback != null) {
                    peerCallback.onStateChanged(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED);
                }
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                if (peerCallback != null) {
                    peerCallback.onPeersChanged();
                }
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (peerCallback != null) {
                    peerCallback.onConnectionChanged(networkInfo);
                }
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

            } else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {

            }
        }
    }

}
