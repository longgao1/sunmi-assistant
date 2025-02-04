package com.sunmi.sunmiservice.cloud;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunmi.bean.ServiceDetailBean;
import com.sunmi.constant.ServiceConstants;
import com.sunmi.contract.ServiceDetailContract;
import com.sunmi.presenter.ServiceDetailPresenter;
import com.sunmi.sunmiservice.R;
import com.xiaojinzi.component.anno.RouterAnno;
import com.xiaojinzi.component.impl.RouterRequest;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import sunmi.common.base.BaseMvpActivity;
import sunmi.common.constant.CommonConstants;
import sunmi.common.constant.CommonNotifications;
import sunmi.common.constant.RouterConfig;
import sunmi.common.rpc.RpcErrorCode;
import sunmi.common.utils.CommonHelper;
import sunmi.common.utils.DateTimeUtils;
import sunmi.common.utils.StatusBarUtils;
import sunmi.common.utils.WebViewParamsUtils;

/**
 * Description:
 *
 * @author linyuanpeng on 2019-10-24.
 */
@EActivity(resName = "activity_service_detail")
public class ServiceDetailActivity extends BaseMvpActivity<ServiceDetailPresenter> implements ServiceDetailContract.View {

    @ViewById(resName = "rl_service")
    RelativeLayout rlService;
    @ViewById(resName = "rl_order")
    RelativeLayout rlOrder;
    @ViewById(resName = "tv_service_name")
    TextView tvServiceName;
    @ViewById(resName = "tv_status")
    TextView tvStatus;
    @ViewById(resName = "tv_device_name")
    TextView tvDeviceName;
    @ViewById(resName = "tv_device_model")
    TextView tvDeviceModel;
    @ViewById(resName = "tv_device_sn")
    TextView tvDeviceSn;
    @ViewById(resName = "tv_subscribe_time")
    TextView tvSubScribeTime;
    @ViewById(resName = "tv_expire_time")
    TextView tvExpireTime;
    @ViewById(resName = "tv_remaining")
    TextView tvRemaining;
    @ViewById(resName = "tv_service_num")
    TextView tvServiceNum;
    @ViewById(resName = "tv_order_num")
    TextView tvOrderNum;
    @ViewById(resName = "btn_renewal")
    Button btnRenewal;
    @ViewById(resName = "layout_network_error")
    View networkError;

    @Extra
    String mSn;
    @Extra
    boolean isBind;
    @Extra
    String deviceName;
    @Extra
    String serviceNo;

    private ServiceDetailBean bean;

    /**
     * 路由启动Activity
     *
     * @param request
     * @return
     */
    @RouterAnno(
            path = RouterConfig.SunmiService.SERVICE_DETAIL
    )
    public static Intent start(RouterRequest request) {
        Intent intent = new Intent(request.getRawContext(), ServiceDetailActivity_.class);
        return intent;
    }

    @AfterViews
    void init() {
        StatusBarUtils.setStatusBarColor(this, StatusBarUtils.TYPE_DARK);
        mPresenter = new ServiceDetailPresenter(mSn);
        mPresenter.attachView(this);
        getServiceDetail();
        showLoadingDialog();
    }

    @UiThread
    @Override
    public void getServiceDetail(ServiceDetailBean bean) {
        if (bean != null) {
            initNetworkNormal();
            this.bean = bean;
            String sn = bean.getDeviceSn();
            if (bean.getServiceType() == CommonConstants.SERVICE_TYPE_CLOUD_7) {
                tvServiceName.setText(R.string.service_cloud_7);
            } else {
                tvServiceName.setText(R.string.service_cloud_30);
            }
            if (isBind) {
                tvDeviceName.setText(deviceName);
            } else {
                tvDeviceName.setText("- -");
                tvStatus.setText(R.string.str_unbind);
                btnRenewal.setVisibility(View.GONE);
            }
            tvDeviceModel.setText(bean.getDeviceModel());
            tvDeviceSn.setText(sn);
            tvSubScribeTime.setText(DateTimeUtils.secondToDate(bean.getSubscribeTime(), "yyyy-MM-dd HH:mm"));
            tvExpireTime.setText(DateTimeUtils.secondToDate(bean.getExpireTime(), "yyyy-MM-dd HH:mm"));
            if (bean.getStatus() != CommonConstants.SERVICE_EXPIRED) {
                tvRemaining.setText(DateTimeUtils.secondToPeriod(bean.getValidTime()));
            } else if (isBind) {
                tvStatus.setText(R.string.str_expired);
                tvRemaining.setText("- -");
            }
            tvServiceNum.setText(bean.getServiceNo());
            tvOrderNum.setText(bean.getOrderNo());

        } else {
            initNetworkError();
        }
    }

    @Override
    public int[] getStickNotificationId() {
        return new int[]{CommonNotifications.cloudStorageChange};
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (CommonNotifications.cloudStorageChange == id) {
            getServiceDetail();
        }
    }

    private void initNetworkError() {
        rlService.setVisibility(View.GONE);
        rlOrder.setVisibility(View.GONE);
        btnRenewal.setVisibility(View.GONE);
        networkError.setVisibility(View.VISIBLE);
    }

    private void initNetworkNormal() {
        rlService.setVisibility(View.VISIBLE);
        rlOrder.setVisibility(View.VISIBLE);
        networkError.setVisibility(View.GONE);
        if (!CommonHelper.isGooglePlay()) {
            btnRenewal.setVisibility(View.VISIBLE);
        }
    }


    @Click(resName = "btn_renewal")
    void renewalClick() {
        if (bean.getRenewStatus() == CommonConstants.CLOUD_STORAGE_NOT_RENEWABLE) {
            switch (bean.getRenewErrorCode()) {
                case RpcErrorCode.ERR_SERVICE_SUBSCRIBE_ERROR:
                    shortTip(R.string.tip_renewal_less_three_days);
                    break;
                default:
                    break;
            }
        } else {
            WebViewCloudServiceActivity_.intent(context).params(WebViewParamsUtils.getCloudStorageParams(bean.getDeviceSn(), bean.getProductNo()))
                    .mUrl(CommonConstants.H5_CLOUD_RENEW).start();
        }
    }

    @Click(resName = "btn_refresh")
    void refreshClick() {
        getServiceDetail();
    }

    private void getServiceDetail() {
        if (serviceNo == null) {
            mPresenter.getServiceDetailByDevice(ServiceConstants.CLOUD_STORAGE_CATEGORY);
        } else {
            mPresenter.getServiceDetailByServiceNo(serviceNo);
        }
    }
}
