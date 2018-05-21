package com.monsent.commons.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TcpServer {

    public interface Callback {

        void onAccept(String key, Socket socket);

        void onRead(String key, InputStream is);

    }

    private static Map<String, Socket> mapSocket = new ConcurrentHashMap<>();
    private static Map<String, Long> mapExpired = new ConcurrentHashMap<>();
    private ServerSocket serverSocket;
    private boolean accepted = false, readable = false;
    private Thread threadAccept, threadRead;
    private OutputStream os = null;
    private InputStream is = null;
    private Callback callback;

    public TcpServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TcpServer(int port, Callback callback) {
        this(port);
        this.callback = callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void startAccept() {
        if (threadAccept == null) {
            threadAccept = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (accepted) {
                        try {
                            Socket socket = serverSocket.accept();
                            String key = UUID.randomUUID().toString();
                            mapSocket.put(key, socket);
                            if (callback != null) {
                                callback.onAccept(key, socket);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        if (!accepted) {
            accepted = true;
            threadAccept.start();
        }
    }

    public void stopAccept() {
        accepted = false;
        threadAccept = null;
    }

    public void startRead() {
        if (threadRead == null) {
            threadRead = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (readable) {
                        for (String key : mapSocket.keySet()) {
                            try {
                                is = mapSocket.get(key).getInputStream();
                                if (callback != null) {
                                    callback.onRead(key, is);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
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

    public void stopRead() {
        readable = false;
        threadRead = null;
    }

    public boolean write(Socket socket, byte[] bytes, int off, int len) {
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

    public boolean write(Socket socket, byte[] bytes) {
        return write(socket, bytes, 0, bytes.length);
    }

    public boolean write(Socket socket, String message) {
        if (message == null) {
            return true;
        }
        return write(socket, message.getBytes());
    }

    public boolean write(String key, byte[] bytes, int off, int len) {
        return write(mapSocket.get(key), bytes, 0, bytes.length);
    }

    public boolean write(String key, byte[] bytes) {
        return write(mapSocket.get(key), bytes);
    }

    public boolean write(String id, String message) {
        return write(mapSocket.get(id), message);
    }

    public void broadcast(byte[] bytes, int off, int len) {
        for (Socket socket : mapSocket.values()) {
            write(socket, bytes, off, len);
        }
    }

    public void broadcast(byte[] bytes) {
        broadcast(bytes, 0, bytes.length);
    }

    public void broadcast(String message) {
        if (message != null) {
            broadcast(message.getBytes());
        }
    }

    public void disconnect(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnect(String key) {
        disconnect(mapSocket.get(key));
    }

    public void disconnectAll() {
        for (Socket socket : mapSocket.values()) {
            disconnect(socket);
        }
    }

    public void close() {
        stopAccept();
        stopRead();
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
        disconnectAll();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
