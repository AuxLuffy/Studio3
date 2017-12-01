jsBrdgewebview的h5与native交互
=====

#### 使用背景 :
在开发中，为了追求开发的效率以及移植的便利性，一些展示性强的页面我们会偏向于使用h5来完成，功能性强的页面我们会偏向于使用native来完成，而一旦使用了h5，为了在h5中尽可能的得到native的体验，我们native层需要暴露一些方法给js调用，比如，弹Toast提醒，弹Dialog，分享等等，有时候甚至把h5的网络请求放着native去完成。为了实现h5页面与native的交互之前一直使用原始的方法，但存在安全隐患（[WebView中接口隐患与手机挂马利用](http://drops.wooyun.org/papers/548)）虽然该漏洞在Android4.2上修复了，即使用@JavascriptInterface代替addJavascriptInterface方法或者使用@javascriptInterface注解来实现所以我们只能另辟蹊径，找到更安全又能兼容各Android版本的方案，于是就有了JSBridge。

#### 使用说明
两种方法:

1.引用aar文件

在项目级别gradle文件中增加：

	allprojects {
	    repositories {
		    ....
	        maven{
	            url "https://jitpack.io"
	        }
			....	
	    }
	}

module级build.gradle文件增加：
	
	repositories{
	    flatDir{
	        dirs 'libs'
	    }
	}

	dependencies{
		....
		implementation 'com.github.lzyzsd:jsbridge:1.0.4'
	    implementation 'com.google.code.gson:gson:2.8.2'
	    implementation (name:'h5activity', ext:'aar')
		....	
	}

2.直接引用library库（推荐）

直接将jsbridgedemo文件夹作为库导入项目并依赖即可

#### 注意

jsBridge进行h5与native交互是java和h5两端共同协商来解决交互问题的，所以要和前端人员协商其同使用，所以要使用就要两端共同准备

##### web端和java端使用方法
这个库要在web端注入一个WebViewJavascriptBridge对象给window，所以在js中使用前我们必须先检查此对象是否存在，如果不存在就要注册一个WebViewJavascriptBridgeReady的事件监听代码如下：

	if (window.WebViewJavascriptBridge) {
		//TODO sth
		callback(WebViewJavascriptBridge)
	} else {
		document.addEventListener('WebViewJavascriptBridgeReady'
			, function() {
				callback(WebViewJavascriptBridge)
		},false);
	}

通过init方法在js中注册一个默认的handler，这样java就可以不用指定handler名称来向js发送消息了代码如下

      bridge.init(function(message, responseCallback) {
        console.log('JS got a message', message);
        var data = {
            'Javascript Responds': 'Wee!'
        };
        console.log('JS responding with', data);
        responseCallback(data);
    });
	
在java中向js中发送匿名消息：
	
	webView.send("hello");

也可以指定handler名称：
	
	 WebViewJavascriptBridge.registerHandler("functionInJs", function(data, responseCallback) {
	        document.getElementById("show").innerHTML = ("data from Java: = " + data);
	        var responseData = "Javascript Says Right back aka!";
	        responseCallback(responseData);
	    });

这样java就可以向js发送指定名称的消息

     webView.callHandler("functionInJs", new Gson().toJson(user), new CallBackFunction() {
        @Override
        public void onCallBack(String data) {

        }
    });

同样的也可以由js向java发送消息来调用java代码：

首先在java端注册handler，同样也有匿名和指定名称两种方法

* 匿名方法
java端准备：
	
	`webView.setDefaultHandler(new DefaultHandler());`

js端发送消息：

	 window.WebViewJavascriptBridge.send(
	        data
	        , function(responseData) {
	            document.getElementById("show").innerHTML = "repsonseData from java, data = " + responseData
	        }
	    );

* 指定名称的方法

java端注册处理器handler:

      webView.registerHandler("submitFromWeb", new BridgeHandler() {
        @Override
        public void handler(String data, CallBackFunction function) {
            Log.i(TAG, "handler = submitFromWeb, data from web = " + data);
            function.onCallBack("submitFromWeb exe, response data from Java");
        }
    });

js端发送指定名称的消息的方法：

    WebViewJavascriptBridge.callHandler(
        'submitFromWeb'
        , {'param': str1}
        , function(responseData) {
            document.getElementById("show").innerHTML = "send get responseData from java, data = " + responseData
        }
    );
	
#### 代码中使用此H5Activity方法

    H5Activity.openH5(context, url);

#### 小结

jsbridge优点：

- 安全
- h5端和android端使用方法一致，简化开发难度

缺点（刚开始使用）：

- 增加使用成本，学习成本
- 比原始方法代码成本稍高