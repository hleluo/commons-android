package com.monsent.commons.activity;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.monsent.commons.R;
import com.monsent.commons.bluetooth.BluetoothServer;
import com.monsent.commons.util.BluetoothUtils;
import com.monsent.commons.util.LogUtils;
import com.monsent.commons.util.TimeUtils;

public class BluetoothServerActivity extends AppCompatActivity implements BluetoothServer.Callback {

    private TextView txtMessage;

    private BluetoothServer bluetoothServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_server);

        txtMessage = (TextView) findViewById(R.id.txtMessage);

        bluetoothServer = new BluetoothServer();
        bluetoothServer.setCallback(this);
        boolean enabled = BluetoothUtils.enable();
        if (enabled) {
            bluetoothServer.startAccept("", "00001101-0000-1000-8000-00805F9B34FB");
        }
    }

    @Override
    public void onAccept(BluetoothSocket socket) {
        LogUtils.i("onAccept：" + socket.getRemoteDevice().getAddress());
    }

    @Override
    public void onReceive(BluetoothSocket socket, byte[] bytes) {
        try {
            if (bytes.length > 0) {
                LogUtils.i("onReceive：" + new String(bytes));
                bluetoothServer.write(TimeUtils.getCurrentLocalDateStr(TimeUtils.yyyyMMddHHmmssSSS));
            }
            Thread.sleep(1000);
        } catch (Exception e) {

        }
    }

    @Override
    public void onDisconnect(BluetoothSocket socket) {
        LogUtils.i("onDisconnect：" + socket.getRemoteDevice().getAddress());
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
        LogUtils.e("onError：" + e.getMessage());
    }
}
