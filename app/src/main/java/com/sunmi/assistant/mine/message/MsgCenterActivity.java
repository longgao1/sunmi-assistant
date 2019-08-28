package com.sunmi.assistant.mine.message;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.FrameLayout;

import com.sunmi.assistant.R;
import com.sunmi.assistant.mine.model.MessageCountBean;
import com.sunmi.assistant.rpc.MessageCenterApi;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

import me.leolin.shortcutbadger.ShortcutBadger;
import sunmi.common.base.BaseActivity;
import sunmi.common.constant.CommonNotifications;
import sunmi.common.notification.BaseNotification;
import sunmi.common.rpc.retrofit.RetrofitCallback;
import sunmi.common.utils.SpUtils;
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

    private ArrayList<CustomTabEntity> tabEntities = new ArrayList<>();
    private ArrayList<Fragment> fragments = new ArrayList<>();

    DeviceMessageFragment deviceF;
    SystemMessageFragment systemF;

    @AfterViews
    void init() {
        titleBar.getRightTextView().setOnClickListener(this);
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
    public void onClick(View v) {
        MsgSettingActivity_.intent(context).start();
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
    public int[] getStickNotificationId() {
        return new int[]{CommonNotifications.msgReadedOrChange, CommonNotifications.pushMsgArrived};
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == CommonNotifications.msgReadedOrChange) {
            showLoadingDialog();
            refreshMsgCount();
        } else if (CommonNotifications.pushMsgArrived == id) {
            refreshMsgCount();
        }
    }

    public void refreshMsgCount() {
        MessageCenterApi.getInstance().getMessageCount(new RetrofitCallback<MessageCountBean>() {
            @Override
            public void onSuccess(int code, String msg, MessageCountBean data) {
                hideLoadingDialog();
                int unreadMsg = data.getUnreadCount();
                int remindUnreadMsg = data.getRemindUnreadCount();
                if (SpUtils.getUnreadMsg() != unreadMsg || SpUtils.getRemindUnreadMsg() != remindUnreadMsg) {
                    SpUtils.setUnreadMsg(unreadMsg);
                    SpUtils.setRemindUnreadMsg(remindUnreadMsg);
                    SpUtils.setUnreadDeviceMsg(data.getModelCountList().get(0).getUnreadCount());
                    SpUtils.setUnreadSystemMsg(data.getModelCountList().get(1).getUnreadCount());
                    initDot();
                    ShortcutBadger.applyCount(context, SpUtils.getRemindUnreadMsg()); //for 1.1.4+
                    BaseNotification.newInstance().postNotificationName(CommonNotifications.msgUpdated);
                }
                if (deviceF != null) {
                    deviceF.getMessageCountSuccess(data);
                }
                if (systemF != null) {
                    systemF.getMessageCountSuccess(data);
                }
            }

            @Override
            public void onFail(int code, String msg, MessageCountBean data) {
                hideLoadingDialog();
                if (deviceF != null) {
                    deviceF.getMessageCountFail();
                }
                if (systemF != null) {
                    systemF.getMessageCountFail();
                }
            }
        });
    }

}
