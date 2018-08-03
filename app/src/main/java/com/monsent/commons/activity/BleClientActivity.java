package com.monsent.commons.activity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.monsent.commons.R;
import com.monsent.commons.ble.BleClient;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BleClientActivity extends AppCompatActivity implements BleClient.Callback {

    private BleClient bleClient;

    private ListView lvDevice;
    private TextView txtMessage;
    private EditText editMessage;
    private Button btnSend;

    private List<Map<String, Object>> dataDevices;
    private SimpleAdapter adapter;
    private List<BluetoothDevice> devices;

    private MessageHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifip2p_client);

        dataDevices = new ArrayList<>();
        devices = new ArrayList<>();
        lvDevice = (ListView) findViewById(R.id.lvDevice);
        adapter = new SimpleAdapter(this, dataDevices, R.layout.device_item,
                new String[]{"name", "address"}, new int[]{R.id.txtName, R.id.txtAddress});
        lvDevice.setAdapter(adapter);
        lvDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bleClient.disconnect();
                bleClient.stopScan();
                String address = (String) dataDevices.get(position).get("address");
                bleClient.connect(address, false);
            }
        });

        txtMessage = (TextView) findViewById(R.id.txtMessage);
        editMessage = (EditText) findViewById(R.id.editMessage);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bleClient.writeToBuffer(editMessage.getText().toString());
            }
        });

        handler = new MessageHandler(this);

        bleClient = new BleClient(this);
        bleClient.setCallback(this);
        boolean initialize = bleClient.initialize();
        if (!initialize) {
            txtMessage.append("初始化失败：" + "\r\n");
            finish();
        }
    }

    private static class MessageHandler extends Handler {
        private WeakReference<BleClientActivity> reference;

        public MessageHandler(BleClientActivity activity) {
            this.reference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BleClientActivity activity = reference.get();
            if (msg.what == 1) {
                String message = (String) msg.obj;
                activity.txtMessage.append(message + "\r\n");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bleClient.startScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleClient.disconnect();
    }

    @Override
    public void onScanStart() {
        txtMessage.append("开始扫描" + "\r\n");
        dataDevices.clear();
        adapter.notifyDataSetChanged();
        devices.clear();
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        BluetoothDevice device = result.getDevice();
        txtMessage.append("扫描到设备：" + device.getName() + " / " + device.getAddress() + " / " + result.getRssi() + "\r\n");
        Map<String, Object> map = new HashMap<>();
        map.put("name", device.getName());
        map.put("address", device.getAddress());
        if (!devices.contains(device)) {
            devices.add(device);
            dataDevices.add(map);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onScanFinish() {
        txtMessage.append("扫描结束" + "\r\n");
    }

    @Override
    public void onScanFailed(int errorCode) {
        txtMessage.append("扫描错误：" + errorCode + "\r\n");
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        showMessage("连接状态改变：" + status + " / " + newState);
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            showMessage("连接成功：" + gatt.getDevice().getAddress());
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            showMessage("断开连接：" + gatt.getDevice().getAddress());
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            /*List<BluetoothGattService> services = gatt.getServices();
            for (BluetoothGattService service : services) {
                LogUtils.e("--------------" + service.getUuid());
                //得到每个Service的Characteristics
                List<BluetoothGattCharacteristic> gattCharacteristics = service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : gattCharacteristics) {
                    int properties = characteristic.getProperties();
                    //所有Characteristics按属性分类
                    if ((properties | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                        LogUtils.e("gattCharacteristic的UUID为:" + characteristic.getUuid());
                        LogUtils.e("gattCharacteristic的属性为:  可读");
                        gatt.readCharacteristic(characteristic);
                    }
                    if ((properties | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                        LogUtils.e("gattCharacteristic的UUID为:" + characteristic.getUuid());
                        LogUtils.e("gattCharacteristic的属性为:  可写");
                    }
                    if ((properties | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        LogUtils.e("gattCharacteristic的UUID为:" + characteristic.getUuid() + characteristic);
                        LogUtils.e("gattCharacteristic的属性为:  具备通知属性");
                    }
                }
            }*/
            bleClient.readCharacteristic();
            bleClient.enableNotification();
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        byte[] bytes = characteristic.getValue();
        String value = new String(bytes);
        showMessage("读取数据：" + characteristic.getUuid() + " / " + " / " + value);
        SystemClock.sleep(3000);
        bleClient.readCharacteristic();
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//        if (status == BluetoothGatt.GATT_SUCCESS) {
//            byte[] bytes = characteristic.getValue();
//            String value = new String(bytes);
//            showMessage("读取数据：" + characteristic.getUuid() + " / " + status + " / " + value);
//        }
        SystemClock.sleep(3000);
        bleClient.readCharacteristic();
    }

    private void showMessage(String msg) {
        Message message = new Message();
        message.what = 1;
        message.obj = msg;
        handler.sendMessage(message);
    }
}
