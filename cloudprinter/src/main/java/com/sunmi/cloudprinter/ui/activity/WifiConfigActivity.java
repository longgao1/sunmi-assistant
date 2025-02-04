package com.sunmi.cloudprinter.ui.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunmi.cloudprinter.R;
import com.sunmi.cloudprinter.bean.PrintRouter;
import com.sunmi.cloudprinter.bean.PrinterDevice;
import com.sunmi.cloudprinter.constant.Constants;
import com.sunmi.cloudprinter.presenter.SunmiPrinterClient;
import com.sunmi.cloudprinter.ui.adaper.RouterListAdapter;
import com.xiaojinzi.component.impl.Router;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import sunmi.common.base.BaseActivity;
import sunmi.common.notification.BaseNotification;
import sunmi.common.router.AppApi;
import sunmi.common.utils.StatusBarUtils;
import sunmi.common.utils.ToastUtils;
import sunmi.common.view.SmRecyclerView;
import sunmi.common.view.TitleBarView;
import sunmi.common.view.dialog.CommonDialog;
import sunmi.common.view.dialog.InputDialog;

/**
 * Description:
 * Created by bruce on 2019/5/23.
 */
@EActivity(resName = "activity_printer_wifi_config")
public class WifiConfigActivity extends BaseActivity implements SunmiPrinterClient.IPrinterClient {

    @ViewById(resName = "title_bar")
    TitleBarView titleBar;
    @ViewById(resName = "tv_top")
    TextView tvConnectWifi;
    @ViewById(resName = "nsv_router")
    NestedScrollView nsvRouter;
    @ViewById(resName = "rv_router")
    SmRecyclerView rvRouter;
    @ViewById(resName = "rl_no_device")
    RelativeLayout rlNoWifi;
    @ViewById(resName = "rl_loading")
    RelativeLayout rlLoading;
    @ViewById(resName = "btn_refresh")
    Button btnRefresh;
    @ViewById(resName = "tv_skip")
    TextView tvSkip;

    @Extra
    String bleAddress;

    private static int TIMEOUT_GET_LIST = 46_000;
    private static int TIMEOUT_GET_WIFI_CONFIG = 45_000;
    private static int INTERVAL_COUNTDOWN = 2_000;
    private SunmiPrinterClient printerClient;
    private Dialog passwordDialog;
    private CountDownTimer wifiListTimer, wifiConfigTimer;
    private CommonDialog getWifiConfigTimeoutDialog;

    private List<PrintRouter> wifiList = new ArrayList<>();
    private RouterListAdapter adapter;

    @AfterViews
    protected void init() {
        StatusBarUtils.StatusBarLightMode(this);//状态栏
        initTvSkip();
        titleBar.getLeftLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        printerClient = new SunmiPrinterClient(context, this);
        getWifiList();
    }

    private void getWifiList() {
        wifiListTimer = new CountDownTimer(TIMEOUT_GET_LIST, INTERVAL_COUNTDOWN) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                onGetWifiListFinish();
            }
        };
        wifiListTimer.start();
        printerClient.getPrinterWifiList(bleAddress);
    }

    private void initTvSkip() {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(tvSkip.getText());
        int len = builder.length();
        String skip = getString(R.string.click_skip);
        builder.append(skip);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                confirmSkipDialog();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);
                tvSkip.postInvalidate();
            }
        };
        builder.setSpan(clickableSpan, len, len + skip.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvSkip.setText(builder);
        builder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.assist_primary)),
                len, len + skip.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvSkip.setText(builder);
        tvSkip.setMovementMethod(LinkMovementMethod.getInstance());
        tvSkip.setText(builder);
    }

    @Click(resName = {"btn_refresh", "btn_retry"})
    void refreshClick() {
        if (wifiListTimer != null) {
            wifiListTimer.cancel();
        }
        rlNoWifi.setVisibility(View.GONE);
        rlLoading.setVisibility(View.VISIBLE);
        tvConnectWifi.setVisibility(View.VISIBLE);
        nsvRouter.setVisibility(View.VISIBLE);
        getWifiList();
    }

    @Override
    public void onBackPressed() {
        if (printerClient != null) {
            printerClient.quitConfig(bleAddress);
        }
        Router.withApi(AppApi.class).goToMain(context);
    }

    @Override
    protected void onResume() {
        super.onResume();
        rvRouter.init(R.drawable.shap_line_divider);
        adapter = new RouterListAdapter(wifiList);
        adapter.setOnItemClickListener(new RouterListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, List<PrintRouter> data) {
                showMessageDialog(data.get(position));
            }
        });
        rvRouter.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (printerClient != null) {
            printerClient.disconnect(bleAddress);
        }
    }

    @Override
    public void onPrinterFount(PrinterDevice printerDevice) {

    }

    @Override
    public void sendDataFail(int code, String msg) {
        showErrorDialog(R.string.tip_printer_connect_fail);
    }

    @Override
    public void getSnRequestSuccess() {

    }

    @Override
    public void onSnReceived(String sn) {

    }

    @UiThread
    @Override
    public void onGetWifiListFinish() {
        if (wifiList.size() > 0) {
            rlLoading.setVisibility(View.GONE);
            tvConnectWifi.setVisibility(View.VISIBLE);
            nsvRouter.setVisibility(View.VISIBLE);
        } else {
            onGetWifiListFail();
        }
    }

    @Override
    public void onGetWifiListFail() {
        hideLoadingDialog();
        rlNoWifi.setVisibility(View.VISIBLE);
        tvConnectWifi.setVisibility(View.GONE);
        nsvRouter.setVisibility(View.GONE);
    }

    @Override
    public void onSetWifiSuccess() {
        if (getWifiConfigTimeoutDialog != null && getWifiConfigTimeoutDialog.isShowing()) {
            return;
        }
        wifiConfigTimer = new CountDownTimer(INTERVAL_COUNTDOWN, TIMEOUT_GET_WIFI_CONFIG) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                showGetWifiConfigTimeout();
            }
        };
    }

    @Override
    public void wifiConfigSuccess() {
        new Handler().postDelayed(() -> {
            hideLoadingDialog();
            if (passwordDialog != null) {
                passwordDialog.dismiss();
            }
            ToastUtils.toastCenter(context, getString(R.string.tip_config_success), R.mipmap.ic_toast_success);
            BaseNotification.newInstance().postNotificationName(Constants.NOTIFICATION_PRINTER_ADDED);
            Router.withApi(AppApi.class).goToMain(context);
        }, 1500);
    }

    @Override
    public void onWifiConfigFail() {
        hideLoadingDialog();
        showErrorDialog(R.string.tip_connect_wifi_fail);
    }

    @Override
    public void bindPrinterSuccess(int code, String msg, String data) {

    }

    @Override
    public void bindPrinterFail(int code, String msg, String data) {

    }

    @UiThread
    @Override
    public void routerFound(PrintRouter printRouter) {
        wifiList.add(printRouter);
        adapter.notifyDataSetChanged();
    }

    private void showMessageDialog(final PrintRouter printRouter) {
        if (!printRouter.isHasPwd()) {
            new CommonDialog.Builder(context)
                    .setTitle(getString(R.string.title_confirm_connect_to_wifi, printRouter.getName()))
                    .setCancelButton(R.string.sm_cancel)
                    .setConfirmButton(R.string.str_confirm, (dialog, which) -> {
                        showLoadingDialog();
                        if (printerClient != null) {
                            printerClient.setPrinterWifi(bleAddress, printRouter.getEssid(), "");
                        }
                    }).create().show();
        } else {
            new InputDialog.Builder(context)
                    .setTitle(getString(R.string.dialog_msg_input_password, printRouter.getName()))
                    .setCancelButton(R.string.sm_cancel)
                    .setConfirmButton(R.string.str_confirm, (dialog, input) -> {
                        if (TextUtils.isEmpty(input)) {
                            shortTip(R.string.hint_input_router_pwd);
                            return;
                        }
                        showLoadingDialog();
                        if (printerClient != null) {
                            printerClient.setPrinterWifi(bleAddress, printRouter.getEssid(), input);
                        }
                    }).create().show();
        }
    }

    private void showErrorDialog(int msgResId) {
        hideLoadingDialog();
        new CommonDialog.Builder(context)
                .setTitle(R.string.sm_title_hint)
                .setMessage(msgResId)
                .setConfirmButton(R.string.str_confirm).create().show();
    }

    private void showGetWifiConfigTimeout() {
        hideLoadingDialog();
        getWifiConfigTimeoutDialog = new CommonDialog.Builder(context)
                .setTitle(R.string.sm_title_hint)
                .setMessage(R.string.tip_get_wifi_config_timeout)
                .setCancelButton(R.string.sm_cancel, (dialog, which) -> {
                    if (wifiConfigTimer != null) {
                        wifiConfigTimer.cancel();
                    }
                    Router.withApi(AppApi.class).goToMain(context);
                }).setConfirmButton(R.string.str_confirm, (dialog, which) -> {
                    if (wifiConfigTimer != null) {
                        wifiConfigTimer.cancel();
                    }
                }).create();
        getWifiConfigTimeoutDialog.show();
    }

    private void confirmSkipDialog() {
        hideLoadingDialog();
        new CommonDialog.Builder(context)
                .setTitle(R.string.sm_title_hint)
                .setMessage(R.string.str_msg_clear_wifi_config)
                .setCancelButton(R.string.sm_cancel)
                .setConfirmButton(R.string.str_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showLoadingDialog();
                        if (printerClient != null) {
                            printerClient.deleteWifiInfo(bleAddress);
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                hideLoadingDialog();
                                BaseNotification.newInstance().postNotificationName(Constants.NOTIFICATION_PRINTER_ADDED);
                                Router.withApi(AppApi.class).goToMain(context);
                            }
                        }, 1500);
                    }
                }).create().show();
    }

}
