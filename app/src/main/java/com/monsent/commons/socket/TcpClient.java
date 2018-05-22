package com.monsent.commons.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TcpClient {

    public interface Callback {

        void onConnect();

        void onReceive(byte[] bytes);

        void onDisconnect();

        void onError(Exception e);
    }

    private final static long MAX_NO_DATA_SECOND = 30 * 60L;    //最长未接收数据断开连接时长
    private Socket socket;
    private long lastTime = 0L;     //最后接收数据时间
    private boolean readable = false;
    private Thread threadRead, threadConnect;
    private OutputStream os = null;
    private InputStream is = null;
    private Callback callback;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    /**
     * 连接服务端
     *
     * @param address 服务端地址
     * @param port    服务端端口号
     */
    public void connect(final String address, final int port) {
        //停止连接
        stopConnect();
        //关闭socket
        closeSocket();
        threadConnect = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(address, port);
                    socket.setKeepAlive(true);
                    socket.setTcpNoDelay(true);
                    socket.setSoLinger(false, -1);
                    socket.setSoTimeout(10000);
                    lastTime = System.currentTimeMillis();
                    //启动读线程
                    startRead();
                    if (callback != null) {
                        callback.onConnect();
                    }
                } catch (IOException e) {
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
     * 停止连接线程
     */
    private void stopConnect() {
        if (threadConnect != null && !threadConnect.isInterrupted()) {
            threadConnect.interrupt();
        }
        threadConnect = null;
    }

    /**
     * 启动读线程
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
                            is.read(bytes);
                            if (callback != null) {
                                callback.onReceive(bytes);
                            }
                        } else {
                            //{MAX_NO_DATA_SECOND}未收到任何数据，关闭连接
                            if (System.currentTimeMillis() - lastTime > MAX_NO_DATA_SECOND * 1000) {
                                disconnect();
                                if (callback != null) {
                                    callback.onDisconnect();
                                }
                            }
                        }
                    } catch (IOException e) {
                        handleError(e);
                    }
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {

                    }
                }
            }
        });
        threadRead.start();
    }

    /**
     * 停止读线程
     */
    private void stopRead() {
        readable = false;
        if (threadRead != null && !threadRead.isInterrupted()) {
            threadRead.interrupt();
        }
        threadRead = null;
    }

    /**
     * 判断远程socket是否关闭
     *
     * @return 是否关闭
     */
    private boolean isSocketClosed() {
        try {
            socket.sendUrgentData(0xFF);
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    /**
     * 写入数据
     *
     * @param bytes 字节数组
     * @param off   起始位
     * @param len   长度
     * @return 是否成功
     */
    public boolean write(byte[] bytes, int off, int len) {
        if (socket == null) {
            return false;
        }
        if (bytes == null) {
            return true;
        }
        try {
            os = socket.getOutputStream();
            os.write(bytes, off, len);
            os.flush();
        } catch (IOException e) {
            handleError(e);
            return false;
        }
        return true;
    }

    /**
     * 写入数据
     *
     * @param bytes 字节数组
     * @return 是否成功
     */
    public boolean write(byte[] bytes) {
        return write(bytes, 0, bytes.length);
    }

    /**
     * 写入数据
     *
     * @param message 字符串
     * @return 是否成功
     */
    public boolean write(String message) {
        return message != null && write(message.getBytes());
    }

    /**
     * 关闭socket
     */
    private void closeSocket() {
        lastTime = 0L;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                handleError(e);
            }
            socket = null;
        }
    }

    /**
     * 关闭数据流
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
     * 关闭连接
     */
    public void disconnect() {
        stopConnect();
        stopRead();
        closeStream();
        closeSocket();
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
