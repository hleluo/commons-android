package com.monsent.commons.activity;

import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.monsent.commons.R;
import com.monsent.commons.socket.TcpServer;
import com.monsent.commons.util.LogUtils;
import com.monsent.commons.util.SystemUtils;
import com.monsent.commons.util.TimeUtils;
import com.monsent.commons.wifi.WifiP2pAdmin;

import java.net.Socket;

public class WifiP2pServerActivity extends AppCompatActivity implements WifiP2pAdmin.ChannelCallback, TcpServer.Callback {

    private TextView txtMessage;

    private PowerManager.WakeLock wakeLock;

    private WifiP2pAdmin wifiP2pAdmin;
    private TcpServer tcpServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifip2p_server);

        txtMessage = (TextView) findViewById(R.id.txtMessage);

        wakeLock = SystemUtils.setKeepCpuRunning(this);

        wifiP2pAdmin = new WifiP2pAdmin(this);
        wifiP2pAdmin.setChannelCallback(this);
        wifiP2pAdmin.initialize();
        wifiP2pAdmin.removeGroup();

        tcpServer = new TcpServer();
        tcpServer.setCallback(this);
        tcpServer.startAccept(9999);
    }

    @Override
    protected void onDestroy() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
        super.onDestroy();
    }

    @Override
    public void onAccept(Socket socket) {
        LogUtils.i("onAccept：" + socket.getInetAddress().getHostAddress());
    }

    @Override
    public void onReceive(Socket socket, byte[] bytes) {
        try {
            if (bytes.length > 0) {
                LogUtils.i("onReceive：" + new String(bytes));
                tcpServer.write(socket, TimeUtils.getCurrentLocalDateStr(TimeUtils.yyyyMMddHHmmssSSS));
            }
            Thread.sleep(1000);
        } catch (Exception e) {

        }
    }

    @Override
    public void onDisconnect(Socket socket) {
        LogUtils.i("onDisconnect：" + socket.getInetAddress().getHostAddress());
    }

    @Override
    public void onError(Exception e) {
        LogUtils.e("onError：" + e.getMessage());
    }

    @Override
    public void onCreateGroup(boolean success) {
        txtMessage.append("创建组：" + success + "\r\n");
    }

    @Override
    public void onRemoveGroup(boolean success) {
        txtMessage.append("删除组：" + success + "\r\n");
        wifiP2pAdmin.createGroup();
    }

    @Override
    public void onConnect(boolean success) {

    }

    @Override
    public void onCancelConnect(boolean success) {

    }
}
