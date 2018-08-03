package com.monsent.commons.activity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.monsent.commons.R;
import com.monsent.commons.ble.BleServer;

import java.lang.ref.WeakReference;

public class BleServerActivity extends AppCompatActivity implements BleServer.Callback {

    private TextView txtMessage;
    private EditText editMessage;
    private Button btnSend;

    private BleServer bleServer;
    private MessageHandler handler;

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

        handler = new MessageHandler(this);

        bleServer = new BleServer(this);
        bleServer.setCallback(this);
        boolean initialize = bleServer.initialize();
        if (initialize) {
            bleServer.startAdvertising();
        } else {
            txtMessage.append("初始化失败：" + "\r\n");
        }
    }

    private static class MessageHandler extends Handler {
        private WeakReference<BleServerActivity> reference;

        public MessageHandler(BleServerActivity activity) {
            this.reference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BleServerActivity activity = reference.get();
            if (msg.what == 1) {
                String message = (String) msg.obj;
                activity.txtMessage.append(message + "\r\n");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleServer.close();
    }

    @Override
    public void onAdvertiseStart(boolean success) {
        showMessage("广播初始化：" + success);
    }

    @Override
    public void onServiceAdded(int status, BluetoothGattService service) {
        showMessage("服务添加：" + status + " / " + service.getUuid());
    }

    @Override
    public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            showMessage("连接成功：" + device.getName() + " / " + device.getAddress() + " / " + status + " / " + newState);
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            showMessage("断开连接：" + device.getName() + " / " + device.getAddress() + " / " + status + " / " + newState);
        }
    }

    @Override
    public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
        showMessage("读数据请求：" + device.getName() + " / " + device.getAddress());
        String value = "OK_" + (int) (Math.random() * 100);   //模拟返回
        boolean result = bleServer.write(device, requestId, offset, value);
        bleServer.response(device, value);
        showMessage("响应数据：" + device.getName() + " / " + device.getAddress() + " / " + result + " / " + value);
    }

    @Override
    public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] requestBytes) {
        String value = new String(requestBytes);
        showMessage("写数据请求：" + device.getAddress() + " / " + value);
        boolean result = bleServer.write(device, requestId, offset, value);
        bleServer.response(device, "OK_" + value);
        showMessage("回复数据：" + device.getAddress() + " / " + value);
    }

    private void showMessage(String msg) {
        Message message = new Message();
        message.what = 1;
        message.obj = msg;
        handler.sendMessage(message);
    }
}
