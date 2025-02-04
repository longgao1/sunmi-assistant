package sunmi.common.view.webview;

import android.app.Activity;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.commonlibrary.R;

import sunmi.common.utils.log.LogCat;
import sunmi.common.view.dialog.CommonDialog;

public abstract class SMWebViewClient extends WebViewClient {

    public Activity mContext;
    private CommonDialog sslDialog;

    public SMWebViewClient(Activity activity) {
        super();
        this.mContext = activity;
    }

    /**
     * (1) 当请求的方式是"POST"方式时这个回调是不会通知的。
     * (2) 当我们访问的地址需要我们应用程序自己处理的时候，可以在这里截获，比如我们发现跳转到的是一个market的链接，那么我们可以直接跳转到应用市场，或者其他app。
     *
     * @param view 接收WebViewClient的那个实例，前面看到webView.setWebViewClient(new MyAndroidWebViewClient())，即是这个webview。
     * @param url  即将要被加载的url
     * @return true 当前应用程序要自己处理这个url， 返回false则不处理。
     */
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        //默认用true,防止ERR_UNKNOW_URL_SCHEME错误
        return true;
    }

    @Override
    public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
        LogCat.e("smwebviewclient", "onReceivedSslError error = " + error.toString());
        if (mContext == null || mContext.isDestroyed()) {
            return;
        }
        if (sslDialog == null) {
            sslDialog = new CommonDialog.Builder(mContext).setTitle(R.string.str_ssl_error)
                    .setCancelButton(R.string.sm_cancel, (dialog, which) -> handler.cancel())
                    .setConfirmButton(R.string.str_confirm, (dialog, which) -> handler.proceed()).create();
        }
        if (!sslDialog.isShowing() && !mContext.isDestroyed()) {
            sslDialog.show();
        }
    }

    /**
     * goBack时重新发送POST数据
     */
    @Override
    public void onFormResubmission(WebView view, Message dontResend, Message resend) {
        resend.sendToTarget();
    }

    /**
     * 加载异常
     */
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            LogCat.e("smwebviewclient", "onReceivedError error = " + error.getDescription().toString());
        }
        receiverError(view, request, error);
    }

    protected abstract void receiverError(WebView view, WebResourceRequest request, WebResourceError error);

}