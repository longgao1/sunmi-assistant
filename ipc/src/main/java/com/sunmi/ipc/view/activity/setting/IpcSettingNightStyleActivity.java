package com.sunmi.ipc.view.activity.setting;

import android.content.Intent;
import android.view.View;

import com.sunmi.ipc.R;
import com.sunmi.ipc.rpc.IPCCall;
import com.sunmi.ipc.rpc.OpcodeConstants;
import com.sunmi.ipc.utils.TimeoutTimer;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import sunmi.common.base.BaseActivity;
import sunmi.common.constant.CommonNotifications;
import sunmi.common.model.SunmiDevice;
import sunmi.common.rpc.sunmicall.ResponseBean;
import sunmi.common.utils.StatusBarUtils;
import sunmi.common.view.SettingItemLayout;
import sunmi.common.view.TitleBarView;

/**
 * Created by YangShiJie on 2019/7/12.
 */
@EActivity(resName = "ipc_activity_night_style")
public class IpcSettingNightStyleActivity extends BaseActivity
        implements View.OnClickListener {
    //夜视模式 0:始终关闭/1:始终开启/2:自动切换
    private final int NIGHT_MODE_OFF = 0;
    private final int NIGHT_MODE_ON = 1;
    private final int NIGHT_MODE_AUTO = 2;
    //wdr 0 关闭  wdr 1 开启
    private final int WDR_MODE_OFF = 0;
    private final int WDR_MODE_ON = 1;

    @ViewById(resName = "title_bar")
    TitleBarView titleBar;
    @ViewById(resName = "sil_auto_switch")
    SettingItemLayout silAutoSwitch;
    @ViewById(resName = "sil_open")
    SettingItemLayout silOpen;
    @ViewById(resName = "sil_close")
    SettingItemLayout silClose;

    @Extra
    SunmiDevice mDevice;
    @Extra
    int nightMode, wdrMode, ledIndicator, rotation;
    private boolean isNetException;

    @AfterViews
    void init() {
        StatusBarUtils.setStatusBarColor(this, StatusBarUtils.TYPE_DARK);
        titleBar.getLeftImg().setOnClickListener(this);
        selectNightStyle(nightMode);//初始化
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        intent.putExtra("nightMode", nightMode);
        intent.putExtra("wdrMode", wdrMode);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        onBackPressed();
    }

    private void setIpc(int type) {
        isNetException = false;
        TimeoutTimer.getInstance().start();
        selectNightStyle(type);
        showLoadingDialog();
        IPCCall.getInstance().setIpcNightIdeRotation(context, mDevice.getModel(), mDevice.getDeviceid(),
                type, wdrMode, ledIndicator, rotation);
    }

    @Click(resName = "sil_auto_switch")
    void autoClick() {
        setIpc(NIGHT_MODE_AUTO);
    }

    @Click(resName = "sil_open")
    void openClick() {
        wdrMode = WDR_MODE_OFF;//夜视模式开启，wdr 0 关闭 |wdr 1 开启
        setIpc(NIGHT_MODE_ON);
    }

    @Click(resName = "sil_close")
    void closeClick() {
        setIpc(NIGHT_MODE_OFF);
    }

    /**
     * 0 自动 1 打开 2 关闭
     *
     * @param type
     */
    private void selectNightStyle(int type) {
        nightMode = type;
        silAutoSwitch.setChecked(type == NIGHT_MODE_AUTO);
        silOpen.setChecked(type == NIGHT_MODE_ON);
        silClose.setChecked(type == NIGHT_MODE_OFF);
    }

    private void stopTimer() {
        TimeoutTimer.getInstance().stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    @Override
    public int[] getStickNotificationId() {
        return new int[]{OpcodeConstants.setIpcNightIdeRotation,
                CommonNotifications.mqttResponseTimeout};
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        super.didReceivedNotification(id, args);
        hideLoadingDialog();
        if (args == null) {
            return;
        }
        if (id == CommonNotifications.mqttResponseTimeout) { //连接超时
            isNetException = true;
            shortTip(R.string.str_server_exception);
            return;
        }
        ResponseBean res = (ResponseBean) args[0];
        if (id == OpcodeConstants.setIpcNightIdeRotation) {
            stopTimer();
            if (isNetException) {
                return;
            }
            setIpcNightIdeRotation(res);
        }
    }

    //led_indicator   rotation设置结果
    @UiThread
    void setIpcNightIdeRotation(ResponseBean res) {
        if (res.getResult() == null) {
            return;
        }
        if (res.getDataErrCode() == 1) {
            shortTip(R.string.tip_set_complete);
        } else {
            wdrMode = wdrMode == WDR_MODE_OFF ? WDR_MODE_ON : WDR_MODE_OFF;
            shortTip(R.string.tip_set_fail);
        }
    }
}
