package com.monsent.commons.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * https://github.com/lifegh/Bluetooth
 */
public class BleServer {

    public interface Callback {

        void onAdvertiseStart(boolean success);

        void onServiceAdded(int status, BluetoothGattService service);

        void onConnectionStateChange(BluetoothDevice device, int status, int newState);

        void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic);

        void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] requestBytes);
    }

    private static final UUID SERVICE_UUID = UUID.fromString("10000000-0000-0000-0000-000000000000");
    private static final UUID CHARACTERISTIC_READ_UUID = UUID.fromString("11000000-0000-0000-0000-000000000000");
    private static final UUID CHARACTERISTIC_WRITE_UUID = UUID.fromString("12000000-0000-0000-0000-000000000000");
    private static final UUID DESCRIPTOR_UUID = UUID.fromString("11100000-0000-0000-0000-000000000000");

    private Context context;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGattServer bluetoothGattServer;
    private Callback callback;

    public BleServer(Context context) {
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
        return bluetoothAdapter.getBluetoothLeAdvertiser() != null;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startAdvertising() {
        //广播设置(必须)
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY) //广播模式: 低功耗,平衡,低延迟
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)     //发射功率级别: 极低,低,中,高
                .setConnectable(true)   //能否连接,广播分为可连接广播和不可连接广播
                .build();
        //广播数据(必须，广播启动就会发送)
        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)     //包含蓝牙名称
                .setIncludeTxPowerLevel(true)   //包含发射功率级别
                .build();

        //扫描响应数据(可选，当客户端扫描时才发送)
        AdvertiseData scanResponseData = new AdvertiseData.Builder()
                .addServiceUuid(new ParcelUuid(SERVICE_UUID))
                .setIncludeTxPowerLevel(true)
                .build();

        //广播创建成功之后的回调
        final AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                //初始化服务
                if (callback != null) {
                    callback.onAdvertiseStart(true);
                }
                initServices();
            }

            @Override
            public void onStartFailure(int errorCode) {
                if (callback != null) {
                    callback.onAdvertiseStart(false);
                }
            }
        };

        //部分设备不支持Ble中心
        BluetoothLeAdvertiser bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        //开始广播
        bluetoothLeAdvertiser.startAdvertising(settings, advertiseData, scanResponseData, advertiseCallback);
    }

    /**
     * 初始化服务
     */
    private void initServices() {
        //创建GattServer服务器
        bluetoothGattServer = bluetoothManager.openGattServer(this.context, gattServerCallback);

        //这个指定的创建指定UUID的服务
        BluetoothGattService service = new BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        //添加指定UUID的可读characteristic
        BluetoothGattCharacteristic characteristicRead = new BluetoothGattCharacteristic(CHARACTERISTIC_READ_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_READ);
        //添加可读characteristic的descriptor
        BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(DESCRIPTOR_UUID, BluetoothGattCharacteristic.PERMISSION_WRITE);
        characteristicRead.addDescriptor(descriptor);
        service.addCharacteristic(characteristicRead);

        //添加指定UUID的可写characteristic
        BluetoothGattCharacteristic characteristicWrite = new BluetoothGattCharacteristic(CHARACTERISTIC_WRITE_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);
        service.addCharacteristic(characteristicWrite);
        bluetoothGattServer.addService(service);
    }

    private BluetoothGattServerCallback gattServerCallback = new BluetoothGattServerCallback() {

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            if (callback != null) {
                callback.onConnectionStateChange(device, status, newState);
            }
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
            if (callback != null) {
                callback.onServiceAdded(status, service);
            }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            if (callback != null) {
                callback.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            }
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] requestBytes) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, requestBytes);
            if (callback != null) {
                callback.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, requestBytes);
            }
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
        }

        @Override
        public void onMtuChanged(BluetoothDevice device, int mtu) {
            super.onMtuChanged(device, mtu);
        }

        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            super.onExecuteWrite(device, requestId, execute);
        }
    };

    /**
     * 关闭连接
     */
    public void close() {
        if (bluetoothGattServer != null) {
            bluetoothGattServer.clearServices();
            bluetoothGattServer.close();
        }
    }

    public boolean write(BluetoothDevice device, int requestId, int offset, String value) {
        return value != null && write(device, requestId, offset, value.getBytes());
    }

    public boolean write(BluetoothDevice device, int requestId, int offset, byte[] bytes) {
        try {
            return bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, bytes);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean response(BluetoothDevice device, String value) {
        return value != null && response(device, value.getBytes());
    }

    public boolean response(BluetoothDevice device, byte[] bytes) {
        try {
            BluetoothGattService service = bluetoothGattServer.getService(SERVICE_UUID);
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_READ_UUID);
            characteristic.setValue(bytes);
            return bluetoothGattServer.notifyCharacteristicChanged(device, characteristic, false);
        } catch (Exception e) {
            return false;
        }
    }

}
