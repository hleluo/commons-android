package com.monsent.commons.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.monsent.commons.R;
import com.monsent.commons.util.LogUtils;
import com.monsent.commons.util.TimeUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnWifiP2pServer, btnWifiP2pClient;
    private Button btnBluetoothServer, btnBluetoothClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        SystemUtils.setWifiDormancy(this);

        btnWifiP2pServer = (Button) findViewById(R.id.btnWifiP2pServer);
        btnWifiP2pServer.setOnClickListener(this);

        btnWifiP2pClient = (Button) findViewById(R.id.btnWifiP2pClient);
        btnWifiP2pClient.setOnClickListener(this);

        btnBluetoothServer = (Button) findViewById(R.id.btnBluetoothServer);
        btnBluetoothServer.setOnClickListener(this);

        btnBluetoothClient = (Button) findViewById(R.id.btnBluetoothClient);
        btnBluetoothClient.setOnClickListener(this);

        LogUtils.i(TimeUtils.getCurrentLocalDateStr(TimeUtils.yyyyMMddHHmmssSSS));

        // 检查是否支持BLE蓝牙
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            LogUtils.e("本机不支持低功耗蓝牙！");
            finish();
            return;
        }

        // Android 6.0动态请求权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            for (String str : permissions) {
                if (checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(permissions, 111);
                    break;
                }
            }
        }
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
                intent = new Intent(this, BleServerActivity.class);
                break;
            case R.id.btnBluetoothClient:
                intent = new Intent(this, BleClientActivity.class);
                break;
        }
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
//        SystemUtils.restoreWifiDormancy(this);
        super.onDestroy();
    }
}
