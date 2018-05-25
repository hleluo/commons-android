package com.monsent.commons.activity;

import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.monsent.commons.R;
import com.monsent.commons.socket.TcpClient;
import com.monsent.commons.util.LogUtils;
import com.monsent.commons.util.SystemUtils;
import com.monsent.commons.util.TimeUtils;
import com.monsent.commons.util.ToastUtils;
import com.monsent.commons.wifi.WifiP2pAdmin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WifiP2pClientActivity extends AppCompatActivity implements WifiP2pAdmin.PeerCallback, TcpClient.Callback {

    private PowerManager.WakeLock wakeLock;

    private List<Map<String, Object>> dataDevices;
    private SimpleAdapter adapter;
    private ListView lvDevice;

    private WifiP2pAdmin wifiP2pAdmin;
    private TcpClient tcpClient;
    private String address = null;
    private final static int MAX_REPEAT_TIMES = 3;
    private int repeatTimes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifip2p_client);

        wakeLock = SystemUtils.setKeepCpuRunning(this);

        dataDevices = new ArrayList<>();
        lvDevice = (ListView) findViewById(R.id.lvDevice);
        adapter = new SimpleAdapter(this, dataDevices, R.layout.device_item,
                new String[]{"name", "address"}, new int[]{R.id.txtName, R.id.txtAddress});
        lvDevice.setAdapter(adapter);
        lvDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String deviceAddress = (String) dataDevices.get(position).get("address");
                wifiP2pAdmin.connect(deviceAddress, 15);
            }
        });

        wifiP2pAdmin = new WifiP2pAdmin(this);
        wifiP2pAdmin.setPeerCallback(this);
        wifiP2pAdmin.initialize();
        wifiP2pAdmin.removeGroup();


        tcpClient = new TcpClient();
        tcpClient.setCallback(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        wifiP2pAdmin.registerReceiver();
        wifiP2pAdmin.discoverPeers();
    }


    @Override
    protected void onPause() {
        super.onPause();
        wifiP2pAdmin.unregisterReceiver();
    }

    @Override
    protected void onDestroy() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
        super.onDestroy();
    }

    @Override
    public void onConnect() {
        LogUtils.d("Wifi P2P Client：onConnect");
        tcpClient.write(TimeUtils.getCurrentLocalDateStr(TimeUtils.yyyyMMddHHmmssSSS));
    }

    @Override
    public void onReceive(byte[] bytes) {
        try {
            if (bytes.length > 0) {
                LogUtils.i("onReceive：" + new String(bytes));
                tcpClient.write(TimeUtils.getCurrentLocalDateStr(TimeUtils.yyyyMMddHHmmssSSS));
            }
            Thread.sleep(1000);
        } catch (Exception e) {

        }
    }

    @Override
    public void onDisconnect() {
        LogUtils.d("Wifi P2P Client：onDisconnect");
        if (repeatTimes <= MAX_REPEAT_TIMES) {
            tcpClient.connect(address, 9999);
            repeatTimes++;
        }
    }

    @Override
    public void onError(Exception e) {
        LogUtils.e("onError：" + e.getMessage());
    }

    @Override
    public void onStateChanged(int state) {

    }

    @Override
    public void onDiscoveryChanged(int state) {

    }

    @Override
    public void onThisDeviceChanged(WifiP2pDevice device) {

    }

    @Override
    public void onPeersChanged() {
        wifiP2pAdmin.requestPeers();
    }

    @Override
    public void onConnectionChanged(NetworkInfo networkInfo) {
        if (networkInfo.isConnected()) {
            ToastUtils.show(this, "连接成功");
            wifiP2pAdmin.requestConnectionInfo();
        } else {
            ToastUtils.show(this, "状态：" + networkInfo.getState().name());
        }
    }

    @Override
    public void onPeersAvailable(Collection<WifiP2pDevice> devices) {
        dataDevices.clear();
        for (WifiP2pDevice device : devices) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", device.deviceName);
            map.put("address", device.deviceAddress);
            LogUtils.i("onPeersAvailable：" + device.deviceName + " : " + device.deviceAddress);
            dataDevices.add(map);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if (info.groupFormed && info.isGroupOwner) {
            LogUtils.i("groupFormed：" + info.groupOwnerAddress.getHostAddress());
        } else if (info.groupFormed) {
            address = info.groupOwnerAddress.getHostAddress();
            LogUtils.i("groupOwnerAddress：" + address);
            tcpClient.connect(address, 9999);
        }
    }
}
