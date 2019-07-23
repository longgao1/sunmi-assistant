package com.sunmi.cloudprinter.ui.Activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunmi.cloudprinter.R;
import com.sunmi.cloudprinter.bean.PrinterDevice;
import com.sunmi.cloudprinter.bean.Router;
import com.sunmi.cloudprinter.constant.Constants;
import com.sunmi.cloudprinter.presenter.SunmiPrinterClient;
import com.sunmi.cloudprinter.ui.adaper.RouterListAdapter;
import com.sunmi.cloudprinter.utils.Utility;

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
import sunmi.common.utils.GotoActivityUtils;
import sunmi.common.utils.StatusBarUtils;
import sunmi.common.view.ClearableEditText;
import sunmi.common.view.SmRecyclerView;
import sunmi.common.view.TitleBarView;
import sunmi.common.view.dialog.CommonDialog;

/**
 * Description:
 * Created by bruce on 2019/5/23.
 */
@EActivity(resName = "activity_set_printer")
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
    @Extra
    String sn;

    private SunmiPrinterClient printerClient;
    private Dialog passwordDialog;

    private List<Router> wifiList = new ArrayList<>();
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
        titleBar.getRightLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (printerClient != null) {
                    printerClient.sendData(bleAddress, Utility.cmdDeleteWifiInfo());
                }
            }
        });
        printerClient = new SunmiPrinterClient(context, bleAddress, this);
        printerClient.getPrinterWifiList(bleAddress);
    }

    private void initTvSkip() {
        int len;
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(tvSkip.getText());
        len = builder.length();
        String skip = getString(R.string.str_skip);
        builder.append(skip);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                confirmSkipDialog();
            }

            @Override
            public void updateDrawState( TextPaint ds) {
                ds.setUnderlineText(false);
                tvSkip.postInvalidate();
            }
        };
        builder.setSpan(clickableSpan, len, len + skip.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvSkip.setText(builder);
        builder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.c_text_blue)),
                len, len + skip.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvSkip.setText(builder);
        tvSkip.setMovementMethod(LinkMovementMethod.getInstance());
        tvSkip.setText(builder);
    }

    @Click(resName = {"btn_refresh", "btn_retry"})
    void refreshClick() {
        rlNoWifi.setVisibility(View.GONE);
        rlLoading.setVisibility(View.VISIBLE);
        tvConnectWifi.setVisibility(View.VISIBLE);
        nsvRouter.setVisibility(View.VISIBLE);

    }

    @Override
    public void onBackPressed() {
        if (printerClient != null) {
            printerClient.sendData(bleAddress, Utility.cmdQuitConfig());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        rvRouter.init(R.drawable.shap_line_divider);
        adapter = new RouterListAdapter(wifiList);
        adapter.setOnItemClickListener(new RouterListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, List<Router> data) {
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
    public void onSnReceived(String sn) {

    }

    @UiThread
    @Override
    public void onGetWifiListFinish() {
        if (wifiList.size() > 0) {
            rlLoading.setVisibility(View.GONE);
        } else {
            rlNoWifi.setVisibility(View.VISIBLE);
            tvConnectWifi.setVisibility(View.GONE);
            nsvRouter.setVisibility(View.GONE);
        }
    }

    @Override
    public void onGetWifiListFail() {

    }

    @Override
    public void onSetWifiSuccess() {

    }

    @Override
    public void wifiConfigSuccess() {

    }

    @Override
    public void bindPrinterSuccess(int code, String msg, String data) {

    }

    @Override
    public void bindPrinterFail(int code, String msg, String data) {

    }

    @UiThread
    @Override
    public void routerFound(Router router) {
        wifiList.add(router);
        adapter.notifyDataSetChanged();
    }

    public void wifiSetSuccess() {
        hideLoadingDialog();
        if (passwordDialog != null)
            passwordDialog.dismiss();
        GotoActivityUtils.gotoMainActivity(context);
        BaseNotification.newInstance().postNotificationName(Constants.NOTIFICATION_PRINTER_ADDED);
    }

    private void showMessageDialog(final Router router) {
        passwordDialog = new Dialog(context, R.style.Son_dialog);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_router_message, null);
        final ClearableEditText etPassword = view.findViewById(R.id.etPassword);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnSure = view.findViewById(R.id.btnSure);
        RelativeLayout rlPassword = view.findViewById(R.id.rlPassword);
        if (!router.isHasPwd()) {
            rlPassword.setVisibility(View.GONE);
        }
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordDialog.dismiss();
                passwordDialog = null;
            }
        });
        btnSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadingDialog();
                String psw = etPassword.getText().toString().trim();
                if (router.isHasPwd() && TextUtils.isEmpty(psw)) {
                    shortTip(R.string.hint_input_router_pwd);
                    return;
                }
                if (printerClient != null) {
                    printerClient.setPrinterWifi(bleAddress, router.getEssid(), psw);
                }
            }
        });
        passwordDialog.setContentView(view);
        passwordDialog.setCancelable(false);
        passwordDialog.show();
    }

    private void showErrorDialog(int msgResId) {
        new CommonDialog.Builder(context)
                .setTitle(R.string.sm_title_hint)
                .setMessage(msgResId)
                .setConfirmButton(R.string.str_confirm).create().show();
    }

    private void confirmSkipDialog() {
        new CommonDialog.Builder(context)
                .setTitle(R.string.sm_title_hint)
                .setMessage(R.string.str_msg_clear_wifi_config)
                .setCancelButton(R.string.sm_cancel)
                .setConfirmButton(R.string.str_confirm,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (printerClient != null) {
                                    printerClient.sendData(bleAddress, Utility.cmdDeleteWifiInfo());
                                }
                            }
                        }).create().show();
    }

}
