package com.example.sunzh.studio3.socketIPC;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServerService extends Service {
    private boolean mIsTcpDestroyed = false;

    public TCPServerService() {
    }

    @Override
    public void onCreate() {
        new Thread(new TcpServer()).start();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        mIsTcpDestroyed = true;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private class TcpServer implements Runnable {
        @Override
        public void run() {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(8688);
            } catch (IOException e) {
                System.err.println("establish tcp server failed, port:8688");
                e.printStackTrace();
                return;
            }
            while (!mIsTcpDestroyed) {
                try {
                    final Socket client = serverSocket.accept();
                    System.out.println("accept");
                    new Thread(){
                        @Override
                        public void run() {
                            responseClient(client);
                        }
                    }.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void responseClient(Socket client) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
