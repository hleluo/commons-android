package com.monsent.commons.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TcpServer {

    public interface Callback {

        void onAccept(Socket socket);

        void onReceive(Socket socket, byte[] bytes);

        void onDisconnect(Socket socket);

        void onError(Exception e);

    }

    private final static long MAX_NO_DATA_SECOND = 30 * 60L;    //最长未接收数据断开连接时长
    private static Map<Socket, AtomicLong> mapSocket = new ConcurrentHashMap<Socket, AtomicLong>(); //socket、最后接收数据时间键值对
    private ServerSocket serverSocket;  //服务端socket
    private boolean accepted = false, readable = false;     //是否可以接收连接，是否继续读数据
    private Thread threadAccept, threadRead;    //接收线程、读线程
    private OutputStream os = null;
    private InputStream is = null;
    private Callback callback;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    /**
     * 开启接收连接线程
     *
     * @param port 监听端口号
     */
    public void startAccept(final int port) {
        stopAccept();
        //断开所有客户端
        disconnectAll();
        //关闭服务端
        closeServerSocket();
        threadAccept = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(port);
                    accepted = true;
                } catch (IOException e) {
                    handleError(e);
                }
                while (accepted) {
                    try {
                        Socket socket = serverSocket.accept();
                        mapSocket.put(socket, new AtomicLong(System.currentTimeMillis()));
                        if (callback != null) {
                            callback.onAccept(socket);
                        }
                    } catch (IOException e) {
                        handleError(e);
                    }
                }
            }
        });
        threadAccept.start();
        startRead();
    }

    /**
     * 停止接收连接线程
     */
    private void stopAccept() {
        accepted = false;
        if (threadAccept != null && !threadAccept.isInterrupted()) {
            threadAccept.interrupt();
        }
        threadAccept = null;
    }

    /**
     * 开启读数据线程
     */
    private void startRead() {
        stopRead();
        threadRead = new Thread(new Runnable() {
            @Override
            public void run() {
                readable = true;
                while (readable) {
                    for (Socket socket : mapSocket.keySet()) {
                        try {
                            is = socket.getInputStream();
                            int size = is.available();
                            if (size > 0) {
                                mapSocket.get(socket).set(System.currentTimeMillis());
                                byte[] bytes = new byte[size];
                                size = is.read(bytes);
                                if (callback != null) {
                                    callback.onReceive(socket, bytes);
                                }
                            } else {
                                //{MAX_NO_DATA_SECOND}未收到任何数据，关闭连接
                                long lastTime = mapSocket.get(socket).get();
                                if (System.currentTimeMillis() - lastTime > MAX_NO_DATA_SECOND * 1000) {
                                    disconnect(socket);
                                    if (callback != null) {
                                        callback.onDisconnect(socket);
                                    }
                                }
                            }
                        } catch (IOException e) {
                            handleError(e);
                        }
                    }
                }
            }
        });
        threadRead.start();
    }

    /**
     * 停止读数据线程
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
     * @param socket socket
     * @return 是否关闭
     */
    private boolean isSocketClosed(Socket socket) {
        try {
            socket.sendUrgentData(0xFF);
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    /**
     * 向客户端写入数据
     *
     * @param socket 客户端
     * @param bytes  字节数组
     * @param off    起始位
     * @param len    长度
     * @return 是否写入成功
     */
    public boolean write(Socket socket, byte[] bytes, int off, int len) {
        if (socket == null || bytes == null) {
            return false;
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
     * 向客户端写入数据
     *
     * @param socket 客户端
     * @param bytes  字节数组
     * @return 是否写入成功
     */
    public boolean write(Socket socket, byte[] bytes) {
        return bytes != null && write(socket, bytes, 0, bytes.length);
    }

    /**
     * 向客户端写入数据
     *
     * @param socket  客户端
     * @param message 字符串
     * @return 是否写入成功
     */
    public boolean write(Socket socket, String message) {
        return message != null && write(socket, message.getBytes());
    }

    /**
     * 向客户端广播写入数据
     *
     * @param bytes 字节数组
     * @param off   起始位
     * @param len   长度
     */
    public void broadcast(byte[] bytes, int off, int len) {
        for (Socket socket : mapSocket.keySet()) {
            write(socket, bytes, off, len);
        }
    }

    /**
     * 向客户端广播写入数据
     *
     * @param bytes 字节数组
     */
    public void broadcast(byte[] bytes) {
        broadcast(bytes, 0, bytes == null ? 0 : bytes.length);
    }

    /**
     * 向客户端广播写入数据
     *
     * @param message 字符串
     */
    public void broadcast(String message) {
        if (message != null) {
            broadcast(message.getBytes());
        }
    }

    /**
     * 关闭客户端
     *
     * @param socket 客户端socket
     */
    private void closeSocket(Socket socket) {
        if (socket == null) {
            return;
        }
        try {
            socket.close();
        } catch (IOException e) {
            handleError(e);
        }
        socket = null;
    }

    /**
     * 断开客户端连接
     *
     * @param socket 客户端
     */
    public void disconnect(Socket socket) {
        closeSocket(socket);
        mapSocket.remove(socket);
    }

    /**
     * 断开所有客户端连接
     */
    public void disconnectAll() {
        for (Socket socket : mapSocket.keySet()) {
            disconnect(socket);
        }
    }

    /**
     * 关闭服务端连接
     */
    private void closeServerSocket() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                handleError(e);
            }
            serverSocket = null;
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
    public void close() {
        stopAccept();
        stopRead();
        closeStream();
        disconnectAll();
        closeServerSocket();
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
