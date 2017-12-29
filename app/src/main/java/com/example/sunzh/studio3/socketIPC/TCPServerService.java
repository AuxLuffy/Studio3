package com.example.sunzh.studio3.socketIPC;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class TCPServerService extends Service {
    private boolean mIsTcpDestroyed = false;
    private String[] mDefinedMsgs = new String[]{
            "你好，呵呵",
            "请问你叫什么名字？",
            "今天北京天气不错啊",
            "知道不，我可以和好几个人聊天的哦",
            "给你讲个笑话吧"
    };
    private static final String TAG = TCPServerService.class.getSimpleName();

    public TCPServerService() {
    }

    @Override
    public void onCreate() {
        new Thread(new TcpServer()).start();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        System.out.println("server destroyed!!!");
        mIsTcpDestroyed = true;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    ServerSocket serverSocket = null;

    private class TcpServer implements Runnable {

        private Thread thread;

        @Override
        public void run() {

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
                    if (thread == null) {
                        thread = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    responseClient(client);
                                } catch (IOException e) {
                                    System.out.println("client has been closed!");
                                    Log.e(TAG, "client has been closed!");
                                    e.printStackTrace();
                                }
                            }
                        };
                        thread.start();
                    }
                } catch (IOException e) {
                    System.out.println("serversocket accept() erro!!!");
                    Log.e(TAG, "serversocket.accept() erro!!!");
                    e.printStackTrace();
                }
            }
        }
    }

    private void responseClient(Socket client) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
        out.println("欢迎来到聊天室！");
        while (!mIsTcpDestroyed) {
            String str = in.readLine();
            System.out.println("msg from client: " + str);
            if (TextUtils.isEmpty(str)) {
                System.out.println("客户端断开连接");
                break;
            }
            int i = new Random().nextInt(mDefinedMsgs.length);
            String msg = mDefinedMsgs[i];
            out.println(msg);
            out.flush();
            System.out.println("send: " + msg);
        }
        System.out.println("client quit.");
        out.close();
        in.close();
        client.close();
    }
}
