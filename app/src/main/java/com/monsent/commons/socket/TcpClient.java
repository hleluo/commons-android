package com.monsent.commons.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TcpClient {

    public interface Callback {
        void onConnect(Socket socket, boolean success);

        void onRead(Socket socket);

        void onDisconnect();
    }

    private Socket socket;
    private boolean readable = false;
    private Thread threadRead, threadConnect;
    private OutputStream os = null;
    private InputStream is = null;
    private Callback callback;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    /**
     * 连接
     *
     * @param address 服务端地址
     * @param port    服务端端口号
     * @return 是否连接成功
     */
    public void connect(String address, int port) {
        this.disconnect();
        boolean result = false;
        try {
            socket = new Socket(address, port);
            socket.setKeepAlive(true);
            socket.setTcpNoDelay(true);
            socket.setSoLinger(false, -1);
            socket.setSoTimeout(10000);
            result = true;
            this.startRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (callback != null) {
            callback.onConnect(socket, result);
        }
    }

    private void startRead() {
        if (threadRead == null) {
            threadRead = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (readable) {
                        try {
                            is = socket.getInputStream();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        if (!readable) {
            readable = true;
            threadRead.start();
        }
    }

    private void stopRead() {
        readable = false;
        if (threadRead != null && !threadRead.isInterrupted()) {
            threadRead.interrupt();
        }
        threadRead = null;
    }

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
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean write(byte[] bytes) {
        return write(bytes, 0, bytes.length);
    }

    public boolean write(String message) {
        return write(message.getBytes());
    }

    public void disconnect() {
        stopRead();
        if (threadConnect != null && !threadConnect.isInterrupted()) {
            threadConnect.interrupt();
        }
        threadConnect = null;
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
