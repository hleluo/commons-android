package com.monsent.commons.activity;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.monsent.commons.R;
import com.monsent.commons.bluetooth.BluetoothServer;
import com.monsent.commons.util.BluetoothUtils;

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
            bluetoothServer.startAccept("", "");
        }
    }

    @Override
    public void onAccept(BluetoothSocket socket) {

    }

    @Override
    public void onReceive(BluetoothSocket socket, byte[] bytes) {

    }

    @Override
    public void onDisconnect(BluetoothSocket socket) {

    }

    @Override
    public void onError(Exception e) {

    }
}
