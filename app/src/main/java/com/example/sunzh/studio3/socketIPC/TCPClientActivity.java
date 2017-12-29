package com.example.sunzh.studio3.socketIPC;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.sunzh.studio3.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TCPClientActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = TCPClientActivity.class.getSimpleName();

    private static final int MSG_RECEIVE_NEW_MSG = 1;
    private static final int MSG_SOCKET_CONNECTED = 2;
    private TextView msg;
    private Button send;
    private EditText input;
    private Socket mClientSocket;
    private PrintWriter mPrintWriter;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg1) {
            switch (msg1.what) {
                case MSG_RECEIVE_NEW_MSG:
                    msg.setText(msg.getText() + (String) msg1.obj);
                    break;
                case MSG_SOCKET_CONNECTED:
                    send.setEnabled(true);
                    break;
                default:
                    break;
            }
        }
    };
    private Intent service;
    private InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcpclient);
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        msg = findViewById(R.id.msg);
        send = findViewById(R.id.send);
        input = findViewById(R.id.input);
        send.setOnClickListener(this);
        service = new Intent(this, TCPServerService.class);
        startService(service);
        new Thread() {
            @Override
            public void run() {
                connectTCPServer();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        if (mClientSocket != null) {
            try {
                mClientSocket.shutdownInput();
                mClientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        stopService(service);
        super.onDestroy();
    }

    private void connectTCPServer() {
        Socket socket = null;
        while (socket == null) {
            try {
                socket = new Socket("localhost", 8688);

                mClientSocket = socket;
                mPrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mClientSocket.getOutputStream())));
                mHandler.sendEmptyMessage(MSG_SOCKET_CONNECTED);
                System.out.println("connect server success.");
            } catch (IOException e) {
                SystemClock.sleep(1000);
                System.out.println("connect tcp server failed, retry...");
//                e.printStackTrace();
            }
        }
        try {
            //接收服务器的消息
            BufferedReader br = new BufferedReader(new BufferedReader(new InputStreamReader(socket.getInputStream())));
            while (!TCPClientActivity.this.isFinishing()) {
                String msg = br.readLine();
                System.out.println("recieve: " + msg);
                if (msg != null) {
                    String time = formatDateTime(System.currentTimeMillis());
                    final String showedMsg = "server " + time + ": " + msg + "\n";
                    mHandler.obtainMessage(MSG_RECEIVE_NEW_MSG, showedMsg).sendToTarget();
                }

            }
            System.out.println("quit...");
            mPrintWriter.close();
            br.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatDateTime(long timeMillis) {
        return new SimpleDateFormat("(HH:mm:ss)").format(new Date(timeMillis));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send:
                String msg = this.input.getText().toString();
                if (!TextUtils.isEmpty(msg) && mPrintWriter != null) {
                    mPrintWriter.println(msg);
                    input.setText("");
                    String time = formatDateTime(System.currentTimeMillis());
                    String showedMsg = "self " + time + ":" + msg + "\n";
                    this.msg.setText(this.msg.getText() + showedMsg);
                    hideKeyboard();
                }
                break;
            default:
                break;
        }
    }

    private boolean isKeyboardOpen() {
        return imm.isActive();
    }

    private void hideKeyboard() {
        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
    }
}
