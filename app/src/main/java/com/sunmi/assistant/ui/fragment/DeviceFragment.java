package com.sunmi.assistant.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunmi.apmanager.config.AppConfig;
import com.sunmi.apmanager.constant.NotificationConstant;
import com.sunmi.apmanager.receiver.MyNetworkCallback;
import com.sunmi.apmanager.ui.activity.config.PrimaryRouteStartActivity;
import com.sunmi.apmanager.ui.activity.router.RouterManagerActivity;
import com.sunmi.apmanager.utils.ApCompatibleUtils;
import com.sunmi.apmanager.utils.RouterDBHelper;
import com.sunmi.assistant.R;
import com.sunmi.assistant.contract.DeviceContract;
import com.sunmi.assistant.pos.PosManagerActivity_;
import com.sunmi.assistant.presenter.DevicePresenter;
import com.sunmi.assistant.ui.DeviceSettingMenu;
import com.sunmi.assistant.ui.adapter.DeviceListAdapter;
import com.sunmi.assistant.utils.ShopTitlePopupWindow;
import com.sunmi.cloudprinter.ui.activity.PrinterManageActivity_;
import com.sunmi.ipc.config.IpcConstants;
import com.sunmi.ipc.utils.IpcUtils;
import com.sunmi.ipc.view.activity.IpcManagerActivity_;
import com.sunmi.ipc.view.activity.setting.IpcSettingActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cn.bingoogolapple.refreshlayout.BGANormalRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import sunmi.common.base.BaseMvpFragment;
import sunmi.common.constant.CommonConstants;
import sunmi.common.constant.CommonNotifications;
import sunmi.common.constant.enums.DeviceStatus;
import sunmi.common.model.ShopInfo;
import sunmi.common.model.SunmiDevice;
import sunmi.common.notification.BaseNotification;
import sunmi.common.rpc.sunmicall.ResponseBean;
import sunmi.common.utils.CommonHelper;
import sunmi.common.utils.NetworkUtils;
import sunmi.common.utils.SMDeviceDiscoverUtils;
import sunmi.common.utils.SpUtils;
import sunmi.common.utils.StatusBarUtils;
import sunmi.common.utils.Utils;
import sunmi.common.view.SmRecyclerView;
import sunmi.common.view.dialog.ChooseDeviceDialog;
import sunmi.common.view.dialog.CommonDialog;
import sunmi.common.view.dialog.InputDialog;

/**
 * Description:
 * Created by bruce on 2019/6/27.
 */
@EFragment(R.layout.fragment_device)
public class DeviceFragment extends BaseMvpFragment<DevicePresenter>
        implements DeviceContract.View, DeviceListAdapter.OnDeviceClickListener,
        DeviceSettingMenu.OnSettingsClickListener,
        BGARefreshLayout.BGARefreshLayoutDelegate, View.OnClickListener {

    @ViewById(R.id.bga_refresh)
    BGARefreshLayout refreshView;
    @ViewById(R.id.rv_device)
    SmRecyclerView rvDevice;
    @ViewById(R.id.btn_add)
    TextView btnAdd;
    @ViewById(R.id.rl_shop_title)
    RelativeLayout rlShopTitle;
    @ViewById(R.id.tv_shop_title)
    TextView tvShopTitle;
    private RelativeLayout rlNoDevice;
    private ShopTitlePopupWindow popupWindow;
    private List<SunmiDevice> deviceList = new ArrayList<>();//设备列表全集
    private ScheduledExecutorService service;
    private DeviceListAdapter deviceListAdapter;
    private DeviceSettingMenu deviceSettingMenu;
    private InputDialog dialogPassword = null;
    private String password = "";    //路由管理密码
    private SunmiDevice clickedDevice;
    private boolean isFirstLogin;//是否首次校验ap管理密码

    private List<SunmiDevice> routerList = new ArrayList<>();
    private List<SunmiDevice> ipcList = new ArrayList<>();
    private List<SunmiDevice> printerList = new ArrayList<>();
    private List<SunmiDevice> posList = new ArrayList<>();

    @AfterViews
    protected void init() {
        initDimens(mActivity);
        mPresenter = new DevicePresenter(mActivity);
        mPresenter.attachView(this);
        deviceList.addAll(DataSupport.findAll(SunmiDevice.class));
        SunmiDevice currentDevice;
        for (SunmiDevice device : deviceList) {
            currentDevice = CommonConstants.SUNMI_DEVICE_MAP.get(device.getDeviceid());
            device.setStatus(currentDevice != null ?
                    DeviceStatus.EXCEPTION.ordinal() :
                    DeviceStatus.UNKNOWN.ordinal());
        }
        initViews();
        loadData();
        if (!CommonHelper.isGooglePlay()) {
            startTimer();
        }
    }

    private void initDimens(Context context) {
        int mStatusBarHeight = Utils.getStatusBarHeight(context);
        rlShopTitle.getLayoutParams().height = (int) context.getResources().getDimension(R.dimen.dp_64) + mStatusBarHeight;
        tvShopTitle.setPadding(30, mStatusBarHeight, 0, 0);
        rlShopTitle.requestLayout();
    }

    protected void initViews() {
        StatusBarUtils.setStatusBarFullTransparent(getActivity());
        tvShopTitle.setText(SpUtils.getShopName());
        initRefreshLayout();
        rvDevice.init(R.drawable.divider_transparent_8dp);
        deviceListAdapter = new DeviceListAdapter(mActivity, deviceList);
        View headerView = getLayoutInflater().inflate(R.layout.include_banner,
                (ViewGroup) refreshView.getParent(), false);
        deviceListAdapter.addHeaderView(headerView);
        rlNoDevice = headerView.findViewById(R.id.rl_empty);
        headerView.findViewById(R.id.btn_add_device).setOnClickListener(this);
        deviceListAdapter.setClickListener(this);
        rvDevice.setAdapter(deviceListAdapter);
        showEmptyView();
        showLoadingDialog();
    }

    private void initRefreshLayout() {
        refreshView.setDelegate(this);
        // 设置下拉刷新和上拉加载更多的风格(参数1：应用程序上下文，参数2：是否具有上拉加载更多功能)
        BGANormalRefreshViewHolder refreshViewHolder =
                new BGANormalRefreshViewHolder(mActivity, false);
        refreshViewHolder.setRefreshingText(getString(R.string.str_refresh_loading));
        refreshViewHolder.setPullDownRefreshText(getString(R.string.str_refresh_pull));
        refreshViewHolder.setReleaseRefreshText(getString(R.string.str_refresh_release));
        refreshView.setRefreshViewHolder(refreshViewHolder); // 为了增加下拉刷新头部和加载更多的通用性，提供了以下可选配置选项
        refreshView.setIsShowLoadingMoreView(false); // 设置正在加载更多时的文本// 设置正在加载更多时的文本
    }

    private void loadData() {
        mPresenter.getRouterList();
        if (!CommonHelper.isGooglePlay()) {
            mPresenter.getIpcList();
            mPresenter.getPrinterList();
            mPresenter.getPosList();
        }
    }

    private void startTimer() {
        service = Executors.newScheduledThreadPool(1);
        service.scheduleAtFixedRate(() -> {
            mPresenter.getIpcList();
            mPresenter.getRouterList();
            mPresenter.getPrinterList();
            mPresenter.getPosList();
        }, 30000, 120000, TimeUnit.MILLISECONDS);
    }

    private void closeTimer() {
        if (service != null) {
            service.shutdownNow();
            service = null;
        }
    }

    @Override
    public void onClick(View v) {
        addClick();
    }

    @Click(R.id.btn_add)
    void addClick() {
        if (isFastClick(1500)) {
            return;
        }
        //选择设备
        ChooseDeviceDialog chooseDeviceDialog = new ChooseDeviceDialog(mActivity, SpUtils.getShopId());
        chooseDeviceDialog.show();
        AppConfig.globalDevList = routerList;//todo
    }

    @Click({R.id.rl_shop_title})
    void dropSelectShop() {
        if (popupWindow != null && popupWindow.isShowing()) {
            tvShopTitle.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    ContextCompat.getDrawable(mActivity, R.drawable.ic_arrow_drop_down_white), null);
            popupWindow.dismiss();
            return;
        }
        mPresenter.getShopList();
    }

    @Override
    public void getShopListSuccess(List<ShopInfo> shopList) {
        tvShopTitle.setCompoundDrawablesWithIntrinsicBounds(null, null,
                ContextCompat.getDrawable(mActivity, R.drawable.ic_arrow_drop_up_white), null);
        popupWindow = new ShopTitlePopupWindow(mActivity, rlShopTitle, shopList, tvShopTitle);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeTimer();
    }

    @Override
    public void getRouterListSuccess(List<SunmiDevice> devices) {
        routerList.clear();
        routerList.addAll(devices);
        refreshList();
    }

    @Override
    public void getIpcListSuccess(List<SunmiDevice> devices) {
        ipcList.clear();
        ipcList.addAll(devices);
        refreshList();
    }

    @Override
    public void getPrinterListSuccess(List<SunmiDevice> devices) {
        printerList.clear();
        printerList.addAll(devices);
        refreshList();
    }

    @Override
    public void getPosListSuccess(List<SunmiDevice> devices) {
        posList.clear();
        posList.addAll(devices);
        refreshList();
    }

    /**
     * W1是否配置
     */
    @Override
    public void getApConfigSuccess(String factory) {
        if (TextUtils.equals("0", factory)) {
            isFirstLogin = true;
            mPresenter.apCheckLogin(RouterDBHelper.queryApPassword(clickedDevice.getDeviceid()));
        } else {
            openActivity(mActivity, PrimaryRouteStartActivity.class);
        }
    }

    /**
     * 推送W1状态
     */
    @UiThread
    @Override
    public void apEventStatus(String sn, boolean isOnline) {
        if (isOnline) {
            devStatusChangeList(sn, DeviceStatus.ONLINE);
        } else {
            devStatusChangeList(sn, DeviceStatus.OFFLINE);
        }
    }

    @Override
    public void refreshApEventStatus() {
        refreshList();
    }

    /**
     * ap 登录密码校验成功
     */
    @UiThread
    @Override
    public void getCheckApLoginSuccess() {
        if (!isFirstLogin) {
            RouterDBHelper.saveLocalMangerPassword(clickedDevice.getDeviceid(), password);//保存本地管理密码
            dialogPasswordDismiss();
        }
        checkApVersion(clickedDevice.getDeviceid(), clickedDevice.getStatus()); //校验版本
    }

    /**
     * ap 登录密码校验失败
     */
    @UiThread
    @Override
    public void getCheckApLoginFail(String errorCode) {
        if (isFirstLogin) {
            dialogPassword = null;
            saveMangerPasswordDialog(TextUtils.equals(errorCode, AppConfig.ERROR_CODE_PASSWORD_ERROR) ? "1" : "0");
        } else {
            shortTip(R.string.tip_password_error);
        }
    }

    @Override
    public void unbindIpcSuccess(int code, String msg, Object data) {
        BaseNotification.newInstance().postNotificationName(IpcConstants.refreshIpcList);
    }

    @Override
    public void unbindPrinterSuccess(String sn) {
        mPresenter.getPrinterList();
    }

    //关闭刷新状态
    @UiThread
    @Override
    public void endRefresh() {
        if (refreshView != null) {
            refreshView.endRefreshing();
        }
    }

    @Override
    public void onDeviceClick(SunmiDevice device) {
        if (isFastClick(1500)) {
            return;
        }
        switch (device.getType()) {
            case "PRINTER":
                if (device.getStatus() != DeviceStatus.UNKNOWN.ordinal()) {
                    PrinterManageActivity_.intent(mActivity)
                            .sn(device.getDeviceid())
                            .userId(SpUtils.getUID())
                            .shopId(SpUtils.getShopId() + "")
                            .channelId(device.getChannelId()).start();
                    clickedDevice = device;
                }
                break;
            case "ROUTER":
                clickedDevice = device;
                showLoadingDialog();
                //校验ap是已初始化配置
                if (TextUtils.equals(device.getDeviceid(), MyNetworkCallback.CURRENT_ROUTER)) {
                    mPresenter.apConfig(device.getDeviceid());
                } else if (device.getStatus() == DeviceStatus.ONLINE.ordinal()) {
                    checkApVersion(device.getDeviceid(), device.getStatus());
                } else {
                    gotoRouterManager(device.getDeviceid(), device.getStatus());
                }
                break;
            case "IPC":
                if (TextUtils.isEmpty(device.getUid())) {
                    shortTip(R.string.tip_play_fail);
                } else {
                    clickedDevice = device;
                    IpcManagerActivity_.intent(mActivity).device(device).start();
                }
                break;
            case "POS":
                if (device.getStatus() == DeviceStatus.ONLINE.ordinal()) {
                    clickedDevice = device;
                    PosManagerActivity_.intent(mActivity).device(device).start();
                } else {
                    shortTip(getString(R.string.str_cannot_manager_device));
                }
                break;
            default:
        }
    }

    @Override
    public void onMoreClick(SunmiDevice item, int position) {
        if (deviceSettingMenu != null) {
            deviceSettingMenu.dismiss();
            deviceSettingMenu = null;
        } else {
            deviceSettingMenu = new DeviceSettingMenu(mActivity, item);
            deviceSettingMenu.setOnSettingsClickListener(this);
            if (rvDevice.getLayoutManager() != null &&
                    rvDevice.getLayoutManager().findViewByPosition(position) != null) {
                deviceSettingMenu.show(rvDevice.getLayoutManager()
                        .findViewByPosition(position).findViewById(R.id.iv_more));
            }
        }
    }

    @Override
    public void onSettingsClick(SunmiDevice device, int type) {
        if (type == 0) {
            onDeviceClick(device);
        } else if (type == 1) {
            deleteDevice(device);
        } else if (type == 2) {
            if (IpcUtils.isIpcManageable(device.getDeviceid(), device.getStatus())) {
                IpcSettingActivity_.intent(mActivity).mDevice(device).start();
            } else {
                shortTip(getString(R.string.str_cannot_manager_device));
            }
        }
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
        loadData();
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        return false;
    }

    @Override
    public int[] getUnStickNotificationId() {
        return new int[]{
                NotificationConstant.bindRouterChanged,
                NotificationConstant.apPostStatus, NotificationConstant.apStatusException,
                NotificationConstant.apLogin, NotificationConstant.apisConfig,
                NotificationConstant.checkLoginAgain, CommonConstants.tabDevice,
                com.sunmi.cloudprinter.constant.Constants.NOTIFICATION_PRINTER_ADDED
        };
    }

    @Override
    public int[] getStickNotificationId() {
        return new int[]{CommonNotifications.shopSwitched, CommonNotifications.netConnected,
                CommonNotifications.netDisconnection, NotificationConstant.updateConnectComplete,
                NotificationConstant.connectedTosunmiDevice, NotificationConstant.unBindRouterChanged,
                CommonNotifications.ipcUpgradeComplete, CommonNotifications.ipcUpgrade, IpcConstants.refreshIpcList,
                CommonNotifications.companyNameChanged, CommonNotifications.companySwitch,
                CommonNotifications.shopNameChanged, IpcConstants.ipcDiscovered, NotificationConstant.apPostStatus};
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (args == null) {
            return;
        }
        if (id == CommonConstants.tabDevice
                || NotificationConstant.bindRouterChanged == id
                || NotificationConstant.updateConnectComplete == id
                || CommonNotifications.netConnected == id
                || NotificationConstant.unBindRouterChanged == id
                || CommonNotifications.ipcUpgradeComplete == id
                || CommonNotifications.ipcUpgrade == id) {
            loadData();
        } else if (id == CommonNotifications.shopSwitched) {
            tvShopTitle.setText(SpUtils.getShopName());
            loadData();
        } else if (id == CommonNotifications.companySwitch) {
            tvShopTitle.setText(SpUtils.getShopName());
            loadData();
        } else if (CommonNotifications.netDisconnection == id
                || NotificationConstant.apStatusException == id) {//网络断开,异常
            networkDisconnected();
        } else if (NotificationConstant.connectedTosunmiDevice == id) {//异常
            devStatusUnknownToException();
        } else if (NotificationConstant.apPostStatus == id) {//在线 离线 设备状态
            mPresenter.getStatusEvent((String) args[0], routerList);
        } else if (CommonNotifications.shopNameChanged == id) {
            tvShopTitle.setText(SpUtils.getShopName());
        } else if (NotificationConstant.apisConfig == id) {//ap是否配置2034
            ResponseBean res = (ResponseBean) args[0];
            mPresenter.getApConfig(res);
        } else if (NotificationConstant.apLogin == id) {
            ResponseBean res = (ResponseBean) args[0];
            mPresenter.checkApLoginPassword(res);
        } else if (com.sunmi.cloudprinter.constant.Constants.NOTIFICATION_PRINTER_ADDED == id) {
            mPresenter.getPrinterList();
        } else if (IpcConstants.refreshIpcList == id) {
            mPresenter.getIpcList();
        } else if (IpcConstants.ipcDiscovered == id) {
            //上线成功发送udp保存设备数据
            SunmiDevice bean = (SunmiDevice) args[0];
            SMDeviceDiscoverUtils.saveInfo(bean);
        }
    }

    @UiThread
    void saveMangerPasswordDialog(String type) {
        hideLoadingDialog();
        if (mActivity == null || dialogPassword != null) {
            dialogPassword = null;
            return;
        }
        password = "";
        dialogPassword = new InputDialog.Builder(mActivity)
                .setTitle(TextUtils.equals(type, "0") ?
                        R.string.hint_input_manger_password : R.string.curr_router_error_password)
                .setHint(R.string.str_tip_password_6_32)
                .setCancelButton(com.sunmi.apmanager.R.string.sm_cancel, (dialog, which) -> dialogPassword = null)
                .setConfirmButton(com.sunmi.apmanager.R.string.str_confirm, (dialog, input) -> {
                    if (isFastClick(1500)) {
                        return;
                    }
                    password = input;
                    if (TextUtils.isEmpty(password)) {
                        shortTip(R.string.hint_input_manger_password);
                        return;
                    }
                    //再次校验管理密码
                    isFirstLogin = false;
                    mPresenter.apCheckLogin(password);
                }).create();
        dialogPassword.setCancelable(false);
        dialogPassword.show();
    }

    @UiThread
    void dialogPasswordDismiss() {
        if (dialogPassword != null) {
            dialogPassword.dismiss();
            dialogPassword = null;
        }
    }

    private void devStatusChangeList(String currentRouter, DeviceStatus status) {
        for (SunmiDevice device : deviceList) {
            if (TextUtils.equals(device.getDeviceid(), currentRouter)) {
                device.setStatus(status.ordinal());
                deviceListRefresh();
                return;
            }
        }
    }

    private void devStatusUnknownToException() {
        if (TextUtils.isEmpty(MyNetworkCallback.CURRENT_ROUTER)) {
            return;
        }
        for (SunmiDevice device : deviceList) {
            if (TextUtils.equals(device.getDeviceid(), MyNetworkCallback.CURRENT_ROUTER)) {
                if (device.getStatus() == DeviceStatus.UNKNOWN.ordinal()
                        || device.getStatus() == DeviceStatus.OFFLINE.ordinal()) {
                    device.setStatus(DeviceStatus.EXCEPTION.ordinal());
                    deviceListRefresh();
                    return;
                }
            }
        }
    }

    @UiThread
    void deviceListRefresh() {
        deviceListAdapter.notifyDataSetChanged();
    }

    @UiThread
    void refreshList() {
        hideLoadingDialog();
        if (deviceList != null) {
            endRefresh();
            deviceList.clear();
            deviceList.addAll(ipcList);
            deviceList.addAll(routerList);
            deviceList.addAll(printerList);
            deviceList.addAll(posList);
            showEmptyView();
            deviceListRefresh();
        }
    }

    @UiThread
    void showEmptyView() {
        if (rlNoDevice == null || btnAdd == null) {
            return;
        }
        if (deviceList.size() > 0) {
            rlNoDevice.setVisibility(View.GONE);
            btnAdd.setVisibility(View.VISIBLE);
        } else {
            rlNoDevice.setVisibility(View.VISIBLE);
            btnAdd.setVisibility(View.GONE);
        }
    }

    @UiThread
    void networkDisconnected() {
        hideLoadingDialog();
        SunmiDevice currentDevice;
        for (SunmiDevice device : deviceList) {
            currentDevice = CommonConstants.SUNMI_DEVICE_MAP.get(device.getDeviceid());
            device.setStatus(currentDevice != null ?
                    DeviceStatus.EXCEPTION.ordinal() :
                    DeviceStatus.UNKNOWN.ordinal());
        }
        deviceListRefresh();
        endRefresh();
    }

    //检查ap版本
    private void checkApVersion(String sn, int status) {
        ApCompatibleUtils.getInstance().checkVersion(mActivity, sn, (isCompatible, currSn) ->
                gotoRouterManager(sn, status));
    }

    private void gotoRouterManager(String sn, int status) {
        Bundle bundle = new Bundle();
        bundle.putString("shopId", SpUtils.getShopId() + "");
        bundle.putString("sn", sn);
        bundle.putInt("status", status);
        openActivity(mActivity, RouterManagerActivity.class, bundle);
    }

    private void deleteDevice(SunmiDevice device) {
        new CommonDialog.Builder(mActivity).setTitle(getDeleteDeviceTitle(device))
                .setCancelButton(R.string.sm_cancel)
                .setConfirmButton(R.string.str_delete, R.color.caution_primary,
                        (dialog, which) -> {
                            if (!NetworkUtils.isNetworkAvailable(mActivity)) {
                                unBindNetDisConnected();
                                return;
                            }
                            mPresenter.unbind(device);
                        }).create().show();
    }

    @NonNull
    private String getDeleteDeviceTitle(SunmiDevice device) {
        if (TextUtils.equals(device.getType(), "ROUTER")) {
            return getString(R.string.str_unbind_ap_dialog_tip);
        } else if (TextUtils.equals(device.getType(), "IPC")) {
            return getString(R.string.tip_delete_ipc);
        } else if (TextUtils.equals(device.getType(), "PRINTER")) {
            return getString(R.string.tip_delete_printer);
        }
        return "";
    }

    //无网络
    private void unBindNetDisConnected() {
        new CommonDialog.Builder(mActivity)
                .setTitle(R.string.str_dialog_net_disconnected)
                .setCancelButton(R.string.str_confirm, (dialog, which) -> dialog.dismiss()).create().show();
    }
}
