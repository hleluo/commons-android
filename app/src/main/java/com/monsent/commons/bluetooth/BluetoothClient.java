package com.monsent.commons.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Message;

import com.monsent.commons.util.BtClsUtils;
import com.mosent.cleaner.util.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by lj on 2017/6/25.
 */

public class BluetoothClient {

    public interface Callback {

        void onConnect();

        void onReceive(byte[] bytes);

        void onDisconnect();

        void onError(Exception e);
    }

    private Context context;
    private BluetoothAdapter adapter;
    private BluetoothSocket bluetoothSocket;
    private BluetoothReceiver receiver;
    private Callback callback;
    private boolean readable = false;
    private Thread threadConnect, threadRead;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;

    public BluetoothClient(Context context) {
        this.context = context;
        this.adapter = BluetoothAdapter.getDefaultAdapter();
        this.receiver = new BluetoothReceiver();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void registerReceiver() {
        //注册蓝牙扫描广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        if (Build.VERSION.SDK_INT >= 19) {
            filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        }
        context.registerReceiver(receiver, filter);
    }

    public void unregisterReceiver() {
        context.unregisterReceiver(receiver);
    }

    /**
     * 扫描设备
     */
    public void startDiscovery() {
        if (adapter.isDiscovering()) {
            adapter.cancelDiscovery();
        }
        adapter.startDiscovery();
    }

    /**
     * 取消扫描设备
     */
    public void cancelDiscovery() {
        adapter.cancelDiscovery();
    }

    /**
     * 配对设备
     *
     * @param device 蓝牙设备
     * @return 是否配对成功
     */
    public boolean createBond(BluetoothDevice device) {
        if (device == null) {
            return false;
        }
        try {
            return BtClsUtils.createBond(BluetoothDevice.class, device);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解除配对
     *
     * @param device 蓝牙设备
     * @return 是否接触配对成功
     */
    public boolean removeBond(BluetoothDevice device) {
        if (device == null) {
            return false;
        }
        try {
            return BtClsUtils.removeBond(BluetoothDevice.class, device);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 取消配对
     *
     * @param device 蓝牙设备
     * @return 是否取消配对成功
     */
    public boolean cancelBondProcess(BluetoothDevice device) {
        if (device == null) {
            return false;
        }
        try {
            return BtClsUtils.cancelBondProcess(BluetoothDevice.class, device);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 设置PIN码
     *
     * @param device 蓝牙设备
     * @param pin    PIN码
     * @return 是否设置成功
     */
    public boolean setPin(BluetoothDevice device, String pin) {
        if (device == null) {
            return false;
        }
        try {
            return BtClsUtils.setPin(BluetoothDevice.class, device, pin);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 取消用户输入
     *
     * @param device 蓝牙设备
     * @return 是否取消成功
     */
    public boolean cancelPairingUserInput(BluetoothDevice device) {
        if (device == null) {
            return false;
        }
        try {
            return BtClsUtils.cancelPairingUserInput(BluetoothDevice.class, device);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 确认配对
     *
     * @param device    蓝牙设备
     * @param confirmed 是否取消确认
     * @return 设置成功
     */
    public boolean setPairingConfirmation(BluetoothDevice device, boolean confirmed) {
        if (device == null) {
            return false;
        }
        try {
            return BtClsUtils.setPairingConfirmation(BluetoothDevice.class, device, confirmed);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取已配对的设备列表
     *
     * @return 设备列表
     */
    public Set<BluetoothDevice> getBondedDevices() {
        return adapter.getBondedDevices();
    }

    /**
     * 设备是否已配对
     *
     * @param device 设备
     * @return 是否已配对
     */
    public boolean isDeviceBounded(BluetoothDevice device) {
        if (device == null) {
            return false;
        }
        Set<BluetoothDevice> bondedDevices = adapter.getBondedDevices();
        for (BluetoothDevice d : bondedDevices) {
            if (d.getAddress().equals(device.getAddress())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解除所有配对的设备
     */
    public void removeAllBondedDevices() {
        Set<BluetoothDevice> bondedDevices = adapter.getBondedDevices();
        for (BluetoothDevice device : bondedDevices) {
            removeBond(device);
        }
    }

    //连接线程
    private class ConnectThread extends Thread {

        private String address;
        private String uuid;

        public ConnectThread(String address, String uuid) {
            this.address = address;
            this.uuid = uuid;
        }

        @Override
        public void run() {
            if (adapter == null) {
                return;
            }
            BluetoothDevice bluetoothDevice = null;
            try {
//                Thread.sleep(200);
                bluetoothDevice = adapter.getRemoteDevice(address);
                //创建一个Socket连接：只需要服务器在注册时的UUID号
                final int sdk = Build.VERSION.SDK_INT;
                if (sdk >= 10) {
                    bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(uuid));
                } else {
                    bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(uuid));
                }
//                bluetoothSocket =(BluetoothSocket) bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(bluetoothDevice,1);
                bluetoothSocket.connect();
                conntecedAddress = address;
                LogUtils.i(TAG, "Bluetooth onConnected");
                if (bluetoothCallback != null) {
                    //启动读数据
                    if (readThread == null) {
                        readThread = new ReadThread();
                    }
                    readThread.start();
                    if (handlerConnect != null) {
                        Message message = handlerConnect.obtainMessage();
                        message.what = STATE_SUCCESS;
                        message.obj = bluetoothDevice;
                        handlerConnect.sendMessage(message);
                    }
                }
            } catch (Exception e) {
                if (bluetoothCallback != null && handlerConnect != null) {
                    Message message = handlerConnect.obtainMessage();
                    message.what = STATE_FAILURE;
                    message.obj = bluetoothDevice;
                    handlerConnect.sendMessage(message);
                }
            }
        }
    }

    //读线程
    private class ReadThread extends Thread {

        @Override
        public void run() {
            try {
                inputStream = bluetoothSocket.getInputStream();
                while (readAvailable) {
                    if (inputStream != null && bluetoothCallback != null) {
                        bluetoothCallback.onDataArrived(inputStream);
                    }
                }
            } catch (Exception e) {
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e) {
                    }
                }
            }

        }
    }

    /**
     * 根据地址得到设备
     *
     * @param address 蓝牙设备地址
     * @return 设备信息
     */
    public BluetoothDevice getByAddress(String address) {
        return address == null ? null : adapter.getRemoteDevice(address);
    }

    /**
     * 连接设备
     *
     * @param address
     * @param uuid
     */
    public void connect(final String address, final String uuid) {
        stopConnect();
        closeSocket();
        if (address == null || uuid == null) {
            if (callback != null) {
                callback.onDisconnect();
            }
            return;
        }
        threadConnect = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BluetoothDevice device = adapter.getRemoteDevice(address);
                    //创建一个Socket连接：只需要服务器在注册时的UUID号
                    final int sdk = Build.VERSION.SDK_INT;
                    if (sdk >= 10) {
                        bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(uuid));
                    } else {
                        bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid));
                    }
                    bluetoothSocket.connect();
                    startRead();
                    if (callback != null) {
                        callback.onConnect();
                    }
                } catch (Exception e) {
                    handleError(e);
                    if (callback != null) {
                        callback.onDisconnect();
                    }
                }
            }
        });
        threadConnect.start();
    }

    private void stopConnect() {
        if (threadConnect != null && !threadConnect.isInterrupted()) {
            threadConnect.interrupt();
        }
        threadConnect = null;
    }

    private void startRead() {
        stopRead();
    }

    private void stopRead() {
        readable = false;
        if (threadRead != null && !threadRead.isInterrupted()) {
            threadRead.interrupt();
        }
        threadRead = null;
    }

    public void closeStream() {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (Exception e) {
                handleError(e);
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Exception e) {
                handleError(e);
            }
        }
    }

    private void closeSocket() {
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                handleError(e);
            }
        }
        bluetoothSocket = null;
    }


    /**
     * 关闭资源
     */
    public synchronized void dispose() {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
                bluetoothSocket = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        readAvailable = false;
        if (readThread != null) {
            try {
                readThread.interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
            readThread = null;
        }
        conntecedAddress = null;
    }

    /**
     * 断开连接
     */
    public synchronized void disconnect() {
        dispose();
        if (bluetoothCallback != null) {
            bluetoothCallback.onDisconnected();
        }
    }

    /**
     * 释放连接
     */
    public void close() {
        dispose();
        if (context != null && receiver != null) {
            context.unregisterReceiver(receiver);
        }
    }

    /**
     * 写数据
     *
     * @param data
     */
    public boolean write(String data) {
        return data == null ? false : write(data.getBytes());
    }

    /**
     * 写数据
     *
     * @param data
     */
    public boolean write(byte[] data) {
        return data == null ? false : write(data, 0, data.length);
    }

    /**
     * 写数据
     *
     * @param data
     * @param off
     * @param len
     */
    public boolean write(byte[] data, int off, int len) {
        if (bluetoothSocket == null || data == null || data.length == 0) {
            return false;
        }
        try {
            outputStream = bluetoothSocket.getOutputStream();
            outputStream.write(data, off, len);
            outputStream.flush();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 错误处理
     *
     * @param e 异常
     */
    private void handleError(Exception e) {
        if (callback != null) {
            try {
                callback.onError(e);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public class BluetoothReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                LogUtils.i(TAG, "Bluetooth onDiscoveryStarted.");
                if (bluetoothCallback != null) {
                    bluetoothCallback.onDiscoveryStarted();
                }
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                LogUtils.i(TAG, "Bluetooth onDeviceDiscovered.");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (bluetoothCallback != null) {
                    bluetoothCallback.onDeviceDiscovered(device);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                LogUtils.i(TAG, "Bluetooth onDiscoveryFinished.");
                if (bluetoothCallback != null) {
                    bluetoothCallback.onDiscoveryFinished();
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                LogUtils.i(TAG, "Bluetooth onBondStateChanged.");
                if (bluetoothCallback != null) {
                    bluetoothCallback.onBondStateChanged(bluetoothDevice, bluetoothDevice.getBondState());
                }
                if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    LogUtils.i(TAG, "Bluetooth onDeviceBounded.");
                    if (bluetoothCallback != null) {
                        bluetoothCallback.onDeviceBonded(bluetoothDevice);
                    }
                } else if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    LogUtils.i(TAG, "Bluetooth onDeviceUnBond.");
                    if (bluetoothCallback != null) {
                        bluetoothCallback.onDeviceUnBond(bluetoothDevice);
                    }
                }
            } else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                LogUtils.i(TAG, "Bluetooth onDevicePairingRequest.");
                if (bluetoothCallback != null) {
                    bluetoothCallback.onDevicePairingRequest(bluetoothDevice, bluetoothDevice.getBondState());
                }
            }
        }
    }
}
