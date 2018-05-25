package com.monsent.commons.activity;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.monsent.commons.R;
import com.monsent.commons.bluetooth.BluetoothClient;
import com.monsent.commons.util.BluetoothUtils;
import com.monsent.commons.util.LogUtils;
import com.monsent.commons.util.TimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BluetoothClientActivity extends AppCompatActivity implements BluetoothClient.Callback {

    private List<Map<String, Object>> dataDevices;
    private SimpleAdapter adapter;
    private ListView lvDevice;

    private BluetoothClient bluetoothClient;
    private String address = null;
    private final static int MAX_REPEAT_TIMES = 3;
    private int repeatTimes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifip2p_client);

        dataDevices = new ArrayList<>();
        lvDevice = (ListView) findViewById(R.id.lvDevice);
        adapter = new SimpleAdapter(this, dataDevices, R.layout.device_item,
                new String[]{"name", "address"}, new int[]{R.id.txtName, R.id.txtAddress});
        lvDevice.setAdapter(adapter);
        lvDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String deviceAddress = (String) dataDevices.get(position).get("address");
                bluetoothClient.connect(deviceAddress, "00001101-0000-1000-8000-00805F9B34FB");
            }
        });

        boolean enabled = BluetoothUtils.enable();
        bluetoothClient = new BluetoothClient(this);
        bluetoothClient.setCallback(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bluetoothClient.registerReceiver();
        bluetoothClient.startDiscovery();
    }


    @Override
    protected void onPause() {
        super.onPause();
        bluetoothClient.unregisterReceiver();
    }


    @Override
    public void onDiscoveryStarted() {
        dataDevices.clear();
        LogUtils.e("onDiscoveryStarted");
    }

    @Override
    public void onDeviceDiscovered(BluetoothDevice device) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", device.getName());
        map.put("address", device.getAddress());
        if (device.getUuids() != null) {
            for (ParcelUuid uuid : device.getUuids()) {
                LogUtils.e("onDeviceDiscovered：" + uuid.getUuid().toString());
            }
        }
        dataDevices.add(map);
    }

    @Override
    public void onDiscoveryFinished() {
        LogUtils.e("onDiscoveryFinished");
    }

    @Override
    public void onBondStateChanged(BluetoothDevice device) {

    }

    @Override
    public void onDevicePairingRequest(BluetoothDevice device) {

    }

    @Override
    public void onConnect(BluetoothDevice device) {
        address = device.getAddress();
        repeatTimes = 0;
    }

    @Override
    public void onReceive(byte[] bytes) {
        try {
            if (bytes.length > 0) {
                LogUtils.i("onReceive：" + new String(bytes));
                bluetoothClient.write(TimeUtils.getCurrentLocalDateStr(TimeUtils.yyyyMMddHHmmssSSS));
            }
            Thread.sleep(1000);
        } catch (Exception e) {

        }
    }

    @Override
    public void onDisconnect() {
        LogUtils.d("Bluetooth Client：onDisconnect");
        if (repeatTimes <= MAX_REPEAT_TIMES) {
            bluetoothClient.connect(address, "00001101-0000-1000-8000-00805F9B34FB");
            repeatTimes++;
        }
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
        LogUtils.e("onError：" + e.getMessage());
    }
}
