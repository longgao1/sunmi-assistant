package com.sunmi.assistant.mine.message;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.sunmi.assistant.R;
import com.sunmi.assistant.mine.model.MessageCountBean;
import com.sunmi.assistant.rpc.MessageCenterApi;
import com.sunmi.assistant.utils.MsgCommonCache;
import com.sunmi.assistant.utils.PushUtils;
import com.xiaojinzi.component.anno.RouterAnno;
import com.xiaojinzi.component.impl.RouterRequest;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

import sunmi.common.base.BaseActivity;
import sunmi.common.constant.CommonNotifications;
import sunmi.common.constant.RouterConfig;
import sunmi.common.notification.BaseNotification;
import sunmi.common.rpc.retrofit.RetrofitCallback;
import sunmi.common.utils.FileHelper;
import sunmi.common.utils.FileUtils;
import sunmi.common.utils.PermissionUtils;
import sunmi.common.utils.SpUtils;
import sunmi.common.utils.StatusBarUtils;
import sunmi.common.view.TitleBarView;
import sunmi.common.view.tablayout.CommonTabLayout;
import sunmi.common.view.tablayout.listener.CustomTabEntity;

/**
 * Description:
 *
 * @author linyuanpeng on 2019-08-14.
 */
@EActivity(R.layout.activity_msg_center)
public class MsgCenterActivity extends BaseActivity implements View.OnClickListener {

    @ViewById(R.id.title_bar)
    TitleBarView titleBar;
    @ViewById(R.id.tab_message)
    CommonTabLayout commonTabLayout;
    @ViewById(R.id.frame_message)
    FrameLayout frameLayout;
    @ViewById(R.id.rl_notice)
    RelativeLayout rlNotice;

    private ArrayList<CustomTabEntity> tabEntities = new ArrayList<>();
    private ArrayList<Fragment> fragments = new ArrayList<>();
    private String fileName = "msgCount.json";

    DeviceMessageFragment deviceF;
    SystemMessageFragment systemF;

    @RouterAnno(
            path = RouterConfig.App.MSGCENTER
    )
    public static Intent start(RouterRequest request){
        Intent intent = new Intent(request.getRawContext(),MsgCenterActivity_.class);
        return intent;
    }

    @AfterViews
    void init() {
        StatusBarUtils.setStatusBarColor(this, StatusBarUtils.TYPE_DARK);
        titleBar.getRightTextView().setOnClickListener(this);
        titleBar.getLeftLayout().setOnClickListener(v -> onBackPressed());
        tabEntities.add(new TabEntity(getString(R.string.str_device_msg)));
        tabEntities.add(new TabEntity(getString(R.string.str_system_msg)));
        deviceF = DeviceMessageFragment_.builder().build();
        systemF = SystemMessageFragment_.builder().build();
        fragments.add(deviceF);
        fragments.add(systemF);
        commonTabLayout.setTabData(tabEntities, this, R.id.frame_message, fragments);
        initDot();
        showLoadingDialog();
        refreshMsgCount();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!PermissionUtils.checkNotificationPermission(context)) {
            rlNotice.setVisibility(View.VISIBLE);
        } else {
            rlNotice.setVisibility(View.GONE);
        }
        refreshMsgCount();
    }

    @Override
    public void onClick(View v) {
        MsgSettingActivity_.intent(context).start();
    }


    @Override
    public void onBackPressed() {
        MessageCountBean bean = MsgCommonCache.getInstance().getMsgCount();
        if (bean != null) {
            FileUtils.writeFileToSD(FileHelper.FILE_PATH, fileName, new Gson().toJson(bean));
        }
        finish();
    }

    @UiThread
    public void initDot() {
        if (SpUtils.getUnreadDeviceMsg() > 0) {
            commonTabLayout.showDot(0);
        } else {
            commonTabLayout.hideMsg(0);
        }
        if (SpUtils.getUnreadSystemMsg() > 0) {
            commonTabLayout.showDot(1);
        } else {
            commonTabLayout.hideMsg(1);
        }
    }

    @Override
    public int[] getUnStickNotificationId() {
        return new int[]{CommonNotifications.msgCenterBadgeUpdate, CommonNotifications.pushMsgArrived};
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == CommonNotifications.msgCenterBadgeUpdate) {
            refreshMsgCount();
        } else if (CommonNotifications.pushMsgArrived == id) {
            refreshMsgCount();
        }
    }

    @Click(R.id.tv_open)
    void openClick() {
        try {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                intent.putExtra(Settings.EXTRA_CHANNEL_ID, getApplicationInfo().uid);

            } else {
                intent.putExtra("app_package", getPackageName());
                intent.putExtra("app_uid", getApplicationInfo().uid);
            }
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }
    }

    public void refreshMsgCount() {
        MessageCenterApi.getInstance().getMessageCount(new RetrofitCallback<MessageCountBean>() {
            @Override
            public void onSuccess(int code, String msg, MessageCountBean data) {
                hideLoadingDialog();
                PushUtils.resetUnReadCount(data);
                BaseNotification.newInstance().postNotificationName(CommonNotifications.homePageBadgeUpdate);
                refreshData(data);
            }

            @Override
            public void onFail(int code, String msg, MessageCountBean data) {
                hideLoadingDialog();
                shortTip(R.string.toast_network_error);
                MessageCountBean bean = MsgCommonCache.getInstance().getMsgCount();
                if (bean == null) {
                    String response = FileUtils.readSDTxt(FileHelper.FILE_PATH + fileName, "utf-8");
                    bean = new Gson().fromJson(response, MessageCountBean.class);
                }
                if (deviceF != null) {
                    if (bean != null) {
                        deviceF.getMessageCountSuccess(bean);
                    } else {
                        deviceF.getMessageCountFail();
                    }

                }
                if (systemF != null) {
                    if (bean != null) {
                        systemF.getMessageCountSuccess(bean);
                    } else {
                        systemF.getMessageCountFail();
                    }
                }
            }
        });
    }

    @UiThread
    public void refreshData(MessageCountBean data) {
        initDot();
        if (deviceF != null) {
            deviceF.getMessageCountSuccess(data);
        }
        if (systemF != null) {
            systemF.getMessageCountSuccess(data);
        }
    }

}
