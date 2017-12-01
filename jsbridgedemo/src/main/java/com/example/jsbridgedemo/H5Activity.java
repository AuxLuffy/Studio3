package com.example.jsbridgedemo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.BridgeWebViewClient;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.github.lzyzsd.jsbridge.DefaultHandler;
import com.google.gson.Gson;

public class H5Activity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = H5Activity.class.getSimpleName();
    private BridgeWebView mWebView;
    //java调用js方法
    private FloatingActionButton btn1;
    private User user;

    public static final String H5_URL = "H5_URL";

    private Toolbar mToolbar;
    private ProgressBar mProgressBar;

    private WebViewClient mWebViewClient;
    private WebChromeClient mWebChromeClient;

    private String mUrl;

    /**
     * 打开指定网页
     *
     * @param context 上下文
     * @param url     网页地址，支持本地和网络地址
     */
    public static void openH5(Context context, String url) {
        Intent intent1 = new Intent(context, H5Activity.class);
        intent1.putExtra(H5Activity.H5_URL, url);
        context.startActivity(intent1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_h5);

        initView();
        initData();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        setupJockey();
        setupData();
    }

    private void setupData() {
        mUrl = getIntent().getStringExtra(H5_URL);
        if (TextUtils.isEmpty(mUrl)) {
            //TODO show error page
        } else {
            mWebView.loadUrl(mUrl);
        }
    }


    private void initView() {
        mWebView = (BridgeWebView) findViewById(R.id.jswebview);
        btn1 = (FloatingActionButton) findViewById(R.id.btn1);
        mToolbar = (Toolbar) findViewById(R.id.h5_toolbar);
        mProgressBar = (ProgressBar) findViewById(R.id.h5_progressbar);
        btn1.setOnClickListener(this);
    }

    private void initData() {

        setupSettings();

        mWebView.setDefaultHandler(new DefaultHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Log.i(TAG, "defaulthandler, msg from js: " + data);
                super.handler(data, function);
            }
        });

        //加载本地网页
//        mWebView.loadUrl("file:///android_asset/local.html");
//        btn1.setVisibility(View.VISIBLE);

        //加载服务器网页
        mWebView.loadUrl("http://www.jianshu.com/");
        btn1.setVisibility(View.GONE);



        //----------------------------------jsBridge代码-----------------------------------
        //注册一个handler方法供js调用
        //必须和js同名函数，注册具体执行函数，类似java实现类
        mWebView.registerHandler("submitFromWeb", new BridgeHandler() {
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
        mWebView.callHandler("functionInJs", new Gson().toJson(user), new CallBackFunction() {
            @Override
            public void onCallBack(String data) {
                Toast.makeText(H5Activity.this, "网页在获取你的位置", Toast.LENGTH_SHORT).show();
            }
        });

        mWebView.send("hello");
        //--------------------------------end------------------------------------
    }

    private void setupSettings() {

        WebSettings mWebSettings = mWebView.getSettings();

        //JS
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        //缓存
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            String wvcc = info.getTypeName();
            Log.d(TAG, "current network: " + wvcc);
            mWebSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
            Log.d(TAG, "No network is connected, use cache");
            mWebSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }

        if (Build.VERSION.SDK_INT >= 16) {
            mWebSettings.setAllowFileAccessFromFileURLs(true);
            mWebSettings.setAllowUniversalAccessFromFileURLs(true);
        }

        if (Build.VERSION.SDK_INT >= 12) {
            mWebSettings.setAllowContentAccess(true);
        }

        setupWebViewClient();
        setupWebChromeClient();
    }

    /**
     * 设置WebChromeClient，进度，标题，h5 alert弹窗处理
     */
    private void setupWebChromeClient() {
        mWebChromeClient = new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                mToolbar.setTitle(title);

            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                mProgressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }

        };
        mWebView.setWebChromeClient(mWebChromeClient);
    }

    /**
     * 设置webviewclient
     * 对不同的请求拦截、页面加载开始，结束，错误等处理
     */
    private void setupWebViewClient() {
        mWebViewClient = new BridgeWebViewClient(mWebView) {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                //TODO 处理URL, 例如对指定的URL做不同的处理等
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.e("tag", "onPageFinished");
                super.onPageFinished(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mUrl = url;
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
            }
        };
        mWebView.setWebViewClient(mWebViewClient);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onClick(View v) {
        if (btn1 == v) {
            // --------以下为jsBridge的方法------------
            mWebView.callHandler("functionInJs", "data from Java", new CallBackFunction() {
                @Override
                public void onCallBack(String data) {
                    Log.i(TAG, "response data from js " + data);
                }
            });
            //--------------------end------------------
        }
    }
}
