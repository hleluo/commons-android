package com.monsent.commons.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by lj on 2017/6/25.
 */

public class BluetoothServer {

    public interface Callback {

        void onAccept(BluetoothSocket socket);

        void onReceive(BluetoothSocket socket, byte[] bytes);

        void onDisconnect(BluetoothSocket socket);

        void onError(Exception e);

    }

    private final static long MAX_NO_DATA_SECOND = 30 * 60L;    //最长未接收数据断开连接时长
    private long lastReadTime = 0L;     //最后接收数据时间
    private BluetoothAdapter adapter;
    private BluetoothServerSocket serverSocket;
    private BluetoothSocket socket;
    private Thread threadAccept, threadRead;
    private boolean accepted = false, readable = false;
    private InputStream is = null;
    private OutputStream os = null;
    private Callback callback;

    public BluetoothServer() {
        this.adapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    /**
     * 启动监听
     *
     * @param name 名称
     * @param uuid uuid
     */
    public void startAccept(final String name, final String uuid) {
        stopAccept();
        closeSocket();
        closeServerSocket();
        threadAccept = new Thread(new Runnable() {
            @Override
            public void run() {
                accepted = true;
                while (accepted) {
                    try {
                        //加密传输，Android强制执行配对，弹窗显示配对码
//                        serverSocket = adapter.listenUsingRfcommWithServiceRecord(name, UUID.fromString(uuid));
                        //明文传输(不安全)，无需配对
                        serverSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(name, UUID.fromString(uuid));
                        socket = serverSocket.accept();
                        accepted = false;
                        lastReadTime = System.currentTimeMillis();
                        if (callback != null) {
                            callback.onAccept(socket);
                        }
                        startRead();
                    } catch (Exception e) {
                        handleError(e);
                    }
                }
            }
        });
        threadAccept.start();
    }

    /**
     * 停止监听
     */
    private void stopAccept() {
        accepted = false;
        if (threadAccept != null && !threadAccept.isInterrupted()) {
            threadAccept.interrupt();
        }
        threadAccept = null;
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
                            lastReadTime = System.currentTimeMillis();
                            byte[] bytes = new byte[size];
                            size = is.read(bytes);
                            if (callback != null) {
                                callback.onReceive(socket, bytes);
                            }
                        } else {
                            //{MAX_NO_DATA_SECOND}未收到任何数据，关闭连接
                            if (System.currentTimeMillis() - lastReadTime > MAX_NO_DATA_SECOND * 1000) {
                                disconnect();
                                if (callback != null) {
                                    callback.onDisconnect(socket);
                                }
                            }
                        }
                    } catch (Exception e) {
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
     * 关闭流
     */
    private void closeStream() {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                handleError(e);
            }
        }
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                handleError(e);
            }
        }
    }

    /**
     * 关闭服务端socket
     */
    private void closeServerSocket() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                handleError(e);
            }
        }
        serverSocket = null;
    }

    /**
     * 关闭客户端socket
     */
    private void closeSocket() {
        lastReadTime = 0L;
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
        stopAccept();
        stopRead();
        closeStream();
        closeSocket();
        closeServerSocket();
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
     * @param off   起始位
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
            handleError(e);
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
}
