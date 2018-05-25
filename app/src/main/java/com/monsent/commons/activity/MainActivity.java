package com.monsent.commons.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.monsent.commons.R;
import com.monsent.commons.util.SystemUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnWifiP2pServer, btnWifiP2pClient;
    private Button btnBluetoothServer, btnBluetoothClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SystemUtils.setWifiDormancy(this);

        btnWifiP2pServer = (Button) findViewById(R.id.btnWifiP2pServer);
        btnWifiP2pServer.setOnClickListener(this);

        btnWifiP2pClient = (Button) findViewById(R.id.btnWifiP2pClient);
        btnWifiP2pClient.setOnClickListener(this);

        btnBluetoothServer = (Button) findViewById(R.id.btnBluetoothServer);
        btnBluetoothServer.setOnClickListener(this);

        btnBluetoothClient = (Button) findViewById(R.id.btnBluetoothClient);
        btnBluetoothClient.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.btnWifiP2pServer:
                intent = new Intent(this, WifiP2pServerActivity.class);
                break;
            case R.id.btnWifiP2pClient:
                intent = new Intent(this, WifiP2pClientActivity.class);
                break;
            case R.id.btnBluetoothServer:
                intent = new Intent(this, BluetoothServerActivity.class);
                break;
            case R.id.btnBluetoothClient:
                intent = new Intent(this, BluetoothClientActivity.class);
                break;
        }
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SystemUtils.restoreWifiDormancy(this);
    }
}
