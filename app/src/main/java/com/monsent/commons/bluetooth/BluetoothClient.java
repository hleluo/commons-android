package com.monsent.commons.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import com.monsent.commons.util.BtClsUtils;

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

        /**
         * 扫描开始
         */
        void onDiscoveryStarted();

        /**
         * 扫描到设备
         *
         * @param device 设备信息
         */
        void onDeviceDiscovered(BluetoothDevice device);

        void onDiscoveryFinished();

        /**
         * 绑定状态改变
         *
         * @param device 设备信息
         */
        void onBondStateChanged(BluetoothDevice device);

        /**
         * 设备配对请求，PIN码一般为0000或1234
         *
         * @param device 设备，setPairingConfirmation、createBond、setPin、cancelPairingUserInput
         */
        void onDevicePairingRequest(BluetoothDevice device);

        void onConnect();

        void onReceive(byte[] bytes);

        void onDisconnect();

        void onError(Exception e);
    }

    private final static long MAX_NO_DATA_SECOND = 30 * 60L;    //最长未接收数据断开连接时长
    private long lastReadTime = 0L;     //最后接收数据时间
    private Context context;
    private BluetoothAdapter adapter;
    private BluetoothSocket socket;
    private BluetoothReceiver receiver;
    private Callback callback;
    private boolean readable = false;
    private Thread threadConnect, threadRead;
    private InputStream is = null;
    private OutputStream os = null;

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
    public Set<BluetoothDevice> getAllBond() {
        return adapter.getBondedDevices();
    }

    /**
     * 设备是否已配对
     *
     * @param device 设备
     * @return 是否已配对
     */
    public boolean isBounded(BluetoothDevice device) {
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
    public void removeAllBond() {
        Set<BluetoothDevice> bondedDevices = adapter.getBondedDevices();
        for (BluetoothDevice device : bondedDevices) {
            removeBond(device);
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
     * @param address 地址
     * @param uuid    uuid
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
                        socket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(uuid));
                    } else {
                        socket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid));
                    }
                    socket.connect();
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

    /**
     * 停止连接
     */
    private void stopConnect() {
        if (threadConnect != null && !threadConnect.isInterrupted()) {
            threadConnect.interrupt();
        }
        threadConnect = null;
    }

    /**
     * 启动读数据
     */
    private void startRead() {
        stopRead();
        threadRead = new Thread(new Runnable() {
            @Override
            public void run() {
                readable = true;
                while (readable) {
                    try {
                        is = socket.getInputStream();
                        int size = is.available();
                        if (size > 0) {
                            byte[] bytes = new byte[size];
                            size = is.read(bytes);
                            if (callback != null) {
                                callback.onReceive(bytes);
                            }
                        } else {
                            //{MAX_NO_DATA_SECOND}未收到任何数据，关闭连接
                            if (System.currentTimeMillis() - lastReadTime > MAX_NO_DATA_SECOND * 1000) {
                                disconnect();
                                if (callback != null) {
                                    callback.onDisconnect();
                                }
                            }
                        }
                    } catch (IOException e) {
                        handleError(e);
                    }
                }
            }
        });
        threadRead.start();
    }

    /**
     * 停止读数据
     */
    private void stopRead() {
        readable = false;
        if (threadRead != null && !threadRead.isInterrupted()) {
            threadRead.interrupt();
        }
        threadRead = null;
    }

    /**
     * 关闭数据流
     */
    public void closeStream() {
        if (os != null) {
            try {
                os.close();
            } catch (Exception e) {
                handleError(e);
            }
        }
        if (is != null) {
            try {
                is.close();
            } catch (Exception e) {
                handleError(e);
            }
        }
    }

    /**
     * 关闭socket
     */
    private void closeSocket() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                handleError(e);
            }
        }
        socket = null;
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        stopConnect();
        stopRead();
        closeStream();
        closeSocket();
    }

    /**
     * 写数据
     *
     * @param message 数据
     * @return 是否成功
     */
    public boolean write(String message) {
        return message != null && write(message.getBytes());
    }

    /**
     * 写数据
     *
     * @param bytes 字节数组
     * @return 是否成功
     */
    public boolean write(byte[] bytes) {
        return write(bytes, 0, bytes == null ? 0 : bytes.length);
    }

    /**
     * 写数据
     *
     * @param bytes 字节数组
     * @param off   起始
     * @param len   长度
     * @return 是否成功
     */
    public boolean write(byte[] bytes, int off, int len) {
        if (socket == null || bytes == null) {
            return false;
        }
        try {
            os = socket.getOutputStream();
            os.write(bytes, off, len);
            os.flush();
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
                if (callback != null) {
                    callback.onDiscoveryStarted();
                }
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (callback != null) {
                    callback.onDeviceDiscovered(device);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (callback != null) {
                    callback.onDiscoveryFinished();
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (callback != null) {
                    callback.onBondStateChanged(bluetoothDevice);
                }
            } else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (callback != null) {
                    callback.onDevicePairingRequest(bluetoothDevice);
                }
            }
        }
    }
}
