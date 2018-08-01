package com.monsent.commons.activity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.monsent.commons.R;
import com.monsent.commons.ble.BleServer;

public class BleServerActivity extends AppCompatActivity implements BleServer.Callback {

    private TextView txtMessage;
    private EditText editMessage;
    private Button btnSend;

    private BleServer bleServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_server);

        txtMessage = (TextView) findViewById(R.id.txtMessage);
        editMessage = (EditText) findViewById(R.id.editMessage);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        bleServer = new BleServer(this);
        bleServer.setCallback(this);
        boolean initialize = bleServer.initialize();
        if (initialize) {
            bleServer.startAdvertising();
        } else {
            txtMessage.append("初始化失败：" + "\r\n");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleServer.close();
    }

    @Override
    public void onAdvertiseStart(boolean success) {
        txtMessage.append("广播初始化：" + success + "\r\n");
    }

    @Override
    public void onServiceAdded(int status, BluetoothGattService service) {
        txtMessage.append("服务添加：" + status + " / " + service.getUuid() + "\r\n");
    }

    @Override
    public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            txtMessage.append("连接成功：" + device.getName() + " / " + device.getAddress() + " / " + status + " / " + newState + "\r\n");
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            txtMessage.append("断开连接：" + device.getName() + " / " + device.getAddress() + " / " + status + " / " + newState + "\r\n");
        }
    }

    @Override
    public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
        txtMessage.append("读数据请求：" + device.getName() + " / " + device.getAddress() + "\r\n");
        String value = "OK_" + (int) (Math.random() * 100);   //模拟返回
        boolean result = bleServer.write(device, requestId, offset, value);
        txtMessage.append("响应数据：" + device.getName() + " / " + device.getAddress() + " / " + result + " / " + value + "\r\n");
    }

    @Override
    public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] requestBytes) {
        String value = new String(requestBytes);
        txtMessage.append("写数据请求：" + value + "\r\n");
    }
}
