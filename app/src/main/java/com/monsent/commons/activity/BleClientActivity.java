package com.monsent.commons.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.monsent.commons.R;

public class BleClientActivity extends AppCompatActivity {

    private BluetoothAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifip2p_client);

        if (Build.VERSION.SDK_INT >= 18) {
            final BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            adapter = manager == null ? BluetoothAdapter.getDefaultAdapter() : manager.getAdapter();
        } else {
            adapter = BluetoothAdapter.getDefaultAdapter();
        }
        if (!adapter.isEnabled()) {
            adapter.enable();
        }

        adapter.startDiscovery();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
