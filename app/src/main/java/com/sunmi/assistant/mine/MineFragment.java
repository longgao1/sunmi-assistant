package com.sunmi.assistant.mine;

import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunmi.apmanager.constant.Constants;
import com.sunmi.apmanager.constant.NotificationConstant;
import com.sunmi.apmanager.utils.CommonUtils;
import com.sunmi.assistant.R;
import com.sunmi.assistant.mine.company.CompanyDetailActivity_;
import com.sunmi.assistant.mine.contract.MineContract;
import com.sunmi.assistant.mine.message.MsgCenterActivity_;
import com.sunmi.assistant.mine.presenter.MinePresenter;
import com.sunmi.assistant.mine.setting.SettingActivity_;
import com.sunmi.assistant.mine.shop.ShopListActivity_;
import com.sunmi.sunmiservice.SunmiServiceConfig;
import com.sunmi.sunmiservice.WebViewSunmiMallActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import cn.bingoogolapple.badgeview.BGABadgeRelativeLayout;
import sunmi.common.base.BaseMvpFragment;
import sunmi.common.constant.CommonNotifications;
import sunmi.common.utils.CommonHelper;
import sunmi.common.utils.ImageUtils;
import sunmi.common.utils.SpUtils;
import sunmi.common.utils.StringHelper;
import sunmi.common.view.CircleImage;

/**
 * 我的页面
 *
 * @author bruce
 */
@EFragment(R.layout.fragment_mime)
public class MineFragment extends BaseMvpFragment<MinePresenter>
        implements MineContract.View {

    @ViewById(R.id.civ_avatar)
    CircleImage civAvatar;
    @ViewById(R.id.tv_name)
    TextView tvName;
    @ViewById(R.id.tv_account)
    TextView tvAccount;
    @ViewById(R.id.rlMsg)
    BGABadgeRelativeLayout rlMsg;
    @ViewById(R.id.rlOrder)
    RelativeLayout rlOrder;
    @ViewById(R.id.rlAddress)
    RelativeLayout rlAddress;
    @ViewById(R.id.rlCoupon)
    RelativeLayout rlCoupon;
    @ViewById(R.id.rlHelp)
    RelativeLayout rlHelp;
    @ViewById(R.id.v1)
    View v1;

    @AfterViews
    void init() {
        mPresenter = new MinePresenter();
        mPresenter.attachView(this);
        mPresenter.getUserInfo();
        initView();
    }

    private void initView() {
        if (!CommonHelper.isGooglePlay()) {
            rlOrder.setVisibility(View.VISIBLE);
            rlAddress.setVisibility(View.VISIBLE);
            rlCoupon.setVisibility(View.VISIBLE);
            rlHelp.setVisibility(View.VISIBLE);
            rlMsg.setVisibility(View.VISIBLE);
            v1.setVisibility(View.VISIBLE);
        }
        initAvatar(false);
        setMsgBadge();
        initUsername();
        initAccount();
    }

    @UiThread
    void initAvatar(boolean forceRefresh) {
        ImageUtils.loadImage(mActivity, SpUtils.getAvatarUrl(), civAvatar,
                forceRefresh, R.mipmap.default_avatar);
    }

    @UiThread
    void initAccount() {
        String mobile = SpUtils.getMobile();
        if (!TextUtils.isEmpty(mobile)) {
            tvAccount.setText(StringHelper.getEncryptPhone(mobile));
        } else if (!TextUtils.isEmpty(SpUtils.getEmail())) {
            tvAccount.setText(StringHelper.getEncryptEmail(SpUtils.getEmail()));
        }
    }

    @UiThread
    void initUsername() {
        if (!TextUtils.isEmpty(SpUtils.getUsername())) {
            tvName.setText(SpUtils.getUsername());
        }
    }

    @UiThread
    public void setMsgBadge() {
        if (rlMsg == null) {
            return;
        }
        if (SpUtils.getUnreadMsg() > 0) {
            int count = SpUtils.getRemindUnreadMsg();
            if (count <= 0) {
                rlMsg.showCirclePointBadge();
            } else if (count > 99) {
                rlMsg.showTextBadge("99+");
            } else {
                rlMsg.showTextBadge(String.valueOf(count));
            }
        } else {
            rlMsg.hiddenBadge();
        }
    }

    /**
     * 顶部头像和用户名
     */
    @Click(R.id.rl_head)
    public void userInfoClick() {
        UserInfoActivity_.intent(mActivity).start();
    }

    /**
     * 当前商户
     */
    @Click(R.id.rlCompany)
    public void companyClick() {
        CompanyDetailActivity_.intent(mActivity).start();
    }

    /**
     * 门店管理
     */
    @Click(R.id.rlStore)
    public void storeClick() {
        CommonUtils.trackCommonEvent(mActivity, "myStore",
                "主页_我的_我的店铺", Constants.EVENT_MY_INFO);
        ShopListActivity_.intent(this).start();
    }

    @Click(R.id.rlMsg)
    public void msgClick() {
        MsgCenterActivity_.intent(mActivity).start();
    }

    /**
     * 我的订单
     */
    @Click(R.id.rlOrder)
    public void orderClick() {
        WebViewSunmiMallActivity_.intent(mActivity).mUrl(SunmiServiceConfig.SUNMI_MALL_HOST
                + "my-order?channel=2&subchannel=4").start();
    }

    /**
     * 收货地址
     */
    @Click(R.id.rlAddress)
    public void addressClick() {
        WebViewSunmiMallActivity_.intent(mActivity).mUrl(SunmiServiceConfig.SUNMI_MALL_HOST
                + "select-address?channel=2&subchannel=4").start();

    }

    /**
     * 优惠券
     */
    @Click(R.id.rlCoupon)
    public void couponClick() {
        WebViewSunmiMallActivity_.intent(mActivity).mUrl(SunmiServiceConfig.SUNMI_MALL_HOST
                + "my-coupon?channel=2&subchannel=4").start();
    }

    /**
     * 帮助与反馈
     */
    @Click(R.id.rlHelp)
    public void helpClick() {
        CommonUtils.trackCommonEvent(mActivity, "feedback",
                "主页_我的_帮助与反馈", Constants.EVENT_MY_INFO);
        HelpActivity_.intent(this).start();
    }

    /**
     * 我的设置
     */
    @Click(R.id.rlSetting)
    public void settingClick() {
        SettingActivity_.intent(mActivity).start();
    }

    @Override
    public int[] getStickNotificationId() {
        return new int[]{NotificationConstant.updateUsernameSuccess,
                NotificationConstant.updateAvatarSuccess,
                CommonNotifications.homePageBadgeUpdate, CommonNotifications.pushMsgArrived};
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationConstant.updateUsernameSuccess) {
            initUsername();
        } else if (id == NotificationConstant.updateAvatarSuccess) {
            initAvatar(true);
        } else if (id == CommonNotifications.homePageBadgeUpdate
                || id == CommonNotifications.pushMsgArrived) {
            setMsgBadge();
        }
    }

    @Override
    public void updateUserInfo() {
        initView();
    }

}
