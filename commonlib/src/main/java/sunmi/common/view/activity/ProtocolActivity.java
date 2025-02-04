package sunmi.common.view.activity;

import android.annotation.SuppressLint;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import sunmi.common.base.BaseActivity;
import sunmi.common.constant.CommonConfig;
import sunmi.common.utils.CommonHelper;
import sunmi.common.utils.StatusBarUtils;
import sunmi.common.utils.Utils;
import sunmi.common.view.webview.BaseJSCall;
import sunmi.common.view.webview.SMWebView;
import sunmi.common.view.webview.SMWebViewClient;
import sunmi.common.view.webview.SsConstants;

/**
 * 用户协议，隐私
 */
@EActivity(resName = "activity_protocol")
public class ProtocolActivity extends BaseActivity {

    //用户协议 https://wifi.cdn.sunmi.com/Privacy/user_sunmi.html
    public final static String PROTOCOL_USER = CommonConfig.SERVICE_H5_URL + "privacy/zh/user?topPadding=";
    //用户协议英文
    public final static String PROTOCOL_USER_ENGLISH = CommonConfig.SERVICE_H5_URL + "privacy/en/user?topPadding=";
    //隐私协议
    public final static String PROTOCOL_PRIVATE = CommonConfig.SERVICE_H5_URL + "privacy/zh/private?topPadding=";
    //隐私协议英文
    public final static String PROTOCOL_PRIVATE_ENGLISH = CommonConfig.SERVICE_H5_URL + "privacy/en/private?topPadding=";
    //商米智能摄像机隐私政策
    public final static String PROTOCOL_IPC = CommonConfig.SERVICE_H5_URL + "privacy/zh/ipcPrivacy?topPadding=";

    public final static String PROTOCOL_IPC_ENGLISH = CommonConfig.SERVICE_H5_URL + "privacy/en/ipcPrivacy?topPadding=";

    public static final int USER_PROTOCOL = 0;
    public static final int USER_PRIVATE = 1;
    public static final int USER_IPC_PROTOCOL = 2;

    @ViewById(resName = "wv_protocol")
    SMWebView webView;
    @ViewById(resName = "layout_network_error")
    View networkError;
    @Extra
    int protocolType;

    private CountDownTimer countDownTimer;
    private long timeout = 15000;//超时时间

    @AfterViews
    protected void init() {
        StatusBarUtils.setStatusBarColor(this, StatusBarUtils.TYPE_DARK);//状态栏
        initNormal();
        // 设置标题
        WebChromeClient webChrome = new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    showLoadingDialog();
                } else {
                    hideLoadingDialog();
                    closeTimer();
                }
                super.onProgressChanged(view, newProgress);
            }
        };
        // 设置setWebChromeClient对象
        webView.setWebChromeClient(webChrome);
    }

    private void initNormal() {
        switch (protocolType) {
            case USER_PROTOCOL:
                loadWebView(CommonHelper.isChinese() ? PROTOCOL_USER : PROTOCOL_USER_ENGLISH);
                break;
            case USER_PRIVATE:
                loadWebView(CommonHelper.isChinese() ? PROTOCOL_PRIVATE : PROTOCOL_PRIVATE_ENGLISH);
                break;
            case USER_IPC_PROTOCOL:
                loadWebView(CommonHelper.isChinese() ? PROTOCOL_IPC : PROTOCOL_IPC_ENGLISH);
                break;
            default:
                break;

        }
    }

    private void startTimer() {
        if (countDownTimer == null) {
            countDownTimer = new CountDownTimer(timeout, timeout) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    loadError();
                }
            };
        }
        countDownTimer.start();
    }

    private void closeTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebView(String url) {
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.loadUrl(url+Utils.getWebViewStatusBarHeight(context));
        startTimer();
        // 可以运行JavaScript
        WebSettings webSetting = webView.getSettings();
        webSetting.setSupportZoom(false);
        webSetting.setBuiltInZoomControls(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setJavaScriptEnabled(true);
        //自适应屏幕
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSetting.setLoadWithOverviewMode(true);
        webView.getSettings().setMixedContentMode(
                WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        // 不用启动客户端的浏览器来加载未加载出来的数据
        BaseJSCall jsCall = new BaseJSCall(this, webView);
        webView.addJavascriptInterface(jsCall, SsConstants.JS_INTERFACE_NAME);
        webView.setWebViewClient(new SMWebViewClient(this) {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                hideLoadingDialog();
            }

            @Override
            protected void receiverError(WebView view, WebResourceRequest request, WebResourceError error) {
                loadError();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
            webView.resumeTimers();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
            webView.pauseTimers();
        }
    }

    @Click(resName = "btn_refresh")
    void refreshClick() {
        networkError.setVisibility(View.GONE);
        if (webView != null) {
            webView.setVisibility(View.VISIBLE);
            webView.reload();
        }
        startTimer();
    }

    @UiThread
    protected void loadError() {
        if (webView != null) {
            webView.setVisibility(View.GONE);
        }
        networkError.setVisibility(View.VISIBLE);
        hideLoadingDialog();
        closeTimer();
    }

    //销毁Webview 防止内存溢出
    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            webView.clearHistory();

            ((ViewGroup) webView.getParent()).removeView(webView);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

}
