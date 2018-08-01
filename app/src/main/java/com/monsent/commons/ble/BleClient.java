package com.monsent.commons.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

public class BleClient {

    public interface Callback {
        void onScanStart();

        void onScanResult(int callbackType, ScanResult result);

        void onScanFinish();

        void onScanFailed(int errorCode);

        void onConnectionStateChange(BluetoothGatt gatt, int status, int newState);

        void onServicesDiscovered(BluetoothGatt gatt, int status);

        void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);
    }

    private static final UUID SERVICE_UUID = UUID.fromString("10000000-0000-0000-0000-000000000000");
    private static final UUID CHARACTERISTIC_READ_UUID = UUID.fromString("11000000-0000-0000-0000-000000000000");
    private static final UUID CHARACTERISTIC_WRITE_UUID = UUID.fromString("12000000-0000-0000-0000-000000000000");
    private static final UUID DESCRIPTOR_UUID = UUID.fromString("11100000-0000-0000-0000-000000000000");

    private Context context;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private Handler handler;
    private Callback callback;

    public BleClient(Context context) {
        this.context = context;
    }

    public Callback getCallback() {
        return callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public boolean initialize() {
        bluetoothManager = (BluetoothManager) this.context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager == null ? BluetoothAdapter.getDefaultAdapter() : bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            return false;
        }
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
        return true;
    }

    /**
     * 开始扫描
     *
     * @param delay 扫描时长
     */
    public void startScan(long delay) {
        if (handler == null) {
            handler = new Handler();
        }
        if (callback != null) {
            callback.onScanStart();
        }
        if (bluetoothAdapter == null || bluetoothAdapter.getBluetoothLeScanner() == null) {
            return;
        }
        bluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onScanFinish();
                }
                stopScan();
            }
        }, delay);
    }

    public void startScan() {
        startScan(5000);
    }

    public void stopScan() {
        if (bluetoothAdapter == null || bluetoothAdapter.getBluetoothLeScanner() == null) {
            return;
        }
        bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
    }

    public void connect(String address, boolean autoConnect) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        bluetoothGatt = device.connectGatt(this.context, autoConnect, gattCallback);
        bluetoothGatt.connect();
    }

    public void connect(BluetoothDevice device, boolean autoConnect) {
        connect(device.getAddress(), autoConnect);
    }

    public void disconnect() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
    }

    public void enableNotification() {
        BluetoothGattService service = bluetoothGatt.getService(SERVICE_UUID);
        if (service == null) {
            return;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_READ_UUID);
        if (characteristic == null) {
            return;
        }
        bluetoothGatt.setCharacteristicNotification(characteristic, true);
//        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(DESCRIPTOR_UUID);
//        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//        bluetoothGatt.writeDescriptor(descriptor);
        List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
        for (BluetoothGattDescriptor descriptor : descriptors) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(descriptor);
        }

    }

    public void readCharacteristic() {
        BluetoothGattService service = bluetoothGatt.getService(SERVICE_UUID);
        if (service == null) {
            return;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_READ_UUID);
        if (characteristic == null) {
            return;
        }
        bluetoothGatt.readCharacteristic(characteristic);
    }

    public boolean writeCharacteristic(String value) {
        BluetoothGattService service = bluetoothGatt.getService(SERVICE_UUID);
        if (service == null) {
            return false;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_WRITE_UUID);
        if (characteristic == null) {
            return false;
        }
        characteristic.setValue(value);
        return bluetoothGatt.writeCharacteristic(characteristic);
    }

    private void refreshDeviceCache(final BluetoothGatt gatt) {
        /*
         * There is a refresh() method in BluetoothGatt class but for now it's
         * hidden. We will call it using reflections.
         */
        try {
            final Method refresh = gatt.getClass().getMethod("refresh");
            if (refresh != null) {
                final boolean success = (Boolean) refresh.invoke(gatt);
            }
        } catch (Exception e) {
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (callback != null) {
                callback.onScanResult(callbackType, result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            if (callback != null) {
                callback.onScanFailed(errorCode);
            }
        }
    };

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                refreshDeviceCache(gatt);
            }
            if (callback != null) {
                callback.onConnectionStateChange(gatt, status, newState);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (callback != null) {
                callback.onServicesDiscovered(gatt, status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (callback != null) {
                callback.onCharacteristicRead(gatt, characteristic, status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }
    };
}
