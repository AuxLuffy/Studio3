package com.example.jsbridgedemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.widget.Button;
import android.widget.Toast;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.github.lzyzsd.jsbridge.DefaultHandler;
import com.google.gson.Gson;

public class H5Activity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = H5Activity.class.getSimpleName();
    private static final int RESULT_CODE = 1;
    private BridgeWebView jsWebview;
    private Button btn1;
    ValueCallback<Uri> mUploadMsg;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_h5);

        initView();
        initData();
    }


    private void initView() {
        jsWebview = (BridgeWebView) findViewById(R.id.jswebview);
        btn1 = (Button) findViewById(R.id.btn1);
        btn1.setOnClickListener(this);
    }

    private void initData() {
        jsWebview.setDefaultHandler(new DefaultHandler(){
            @Override
            public void handler(String data, CallBackFunction function) {
                Log.i(TAG, "defaulthandler, msg from js: "+ data);
                super.handler(data, function);
            }
        });
        jsWebview.setWebViewClient(new MyWebViewClient(jsWebview));
        jsWebview.setWebChromeClient(new WebChromeClient() {
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType, String capture) {
                this.openFileChooser(uploadMsg);
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType) {
                this.openFileChooser(uploadMsg);
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUploadMsg = uploadMsg;
                pickFile();
            }
        });
        //加载本地网页
        jsWebview.loadUrl("file:///android_asset/local.html");
        //加载服务器网页
//        jsWebview.loadUrl("http://www.baidu.com");
        //注册一个handler方法供js调用
        //必须和js同名函数，注册具体执行函数，类似java实现类
        jsWebview.registerHandler("submitFromWeb", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                String str = "这是html返回给java的数据：" + data;
                Toast.makeText(H5Activity.this, str, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "handler = submitFromWeb, data from web = " + data);
                function.onCallBack(str + ",Java经过处理后截取了一部分：" + str.substring(0, 5));
            }
        });

        user = new User();
        User.Location location = new User.Location();
        location.adress = "北京";
        user.location = location;
        user.name = "luffy";

        //java里调用js里的handler方法, webview刚开始就执行一段java代码，可以通过webview.callHandler()来实现。当然我们注册的方法也要和js里的方法名一致
        jsWebview.callHandler("functionInJs", new Gson().toJson(user), new CallBackFunction() {
            @Override
            public void onCallBack(String data) {
                Toast.makeText(H5Activity.this, "网页在获取你的位置", Toast.LENGTH_SHORT).show();
            }
        });

        jsWebview.send("hello");

    }

    /**
     * 选择文件
     */
    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CODE) {
            if (null == mUploadMsg) {
                return;
            }
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            mUploadMsg.onReceiveValue(result);
            mUploadMsg = null;
        }
    }


    @Override
    public void onClick(View v) {
        if (btn1 == v) {
            jsWebview.callHandler("functionInJs", "data from Java", new CallBackFunction() {
                @Override
                public void onCallBack(String data) {
                    Log.i(TAG, "response data from js " + data);
                }
            });
        }
    }
}
