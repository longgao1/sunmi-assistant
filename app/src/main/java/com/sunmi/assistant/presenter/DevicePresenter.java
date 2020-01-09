package com.sunmi.assistant.presenter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.sunmi.apmanager.config.AppConfig;
import com.sunmi.apmanager.constant.NotificationConstant;
import com.sunmi.apmanager.receiver.MyNetworkCallback;
import com.sunmi.apmanager.rpc.ap.APCall;
import com.sunmi.apmanager.rpc.cloud.CloudApi;
import com.sunmi.apmanager.ui.activity.config.PrimaryRouteStartActivity;
import com.sunmi.apmanager.utils.EncryptUtils;
import com.sunmi.assistant.R;
import com.sunmi.assistant.contract.DeviceContract;
import com.sunmi.assistant.data.apresp.ApConfigResp;
import com.sunmi.assistant.data.apresp.ApEventResp;
import com.sunmi.assistant.data.apresp.ApLoginResp;
import com.sunmi.assistant.pos.data.PosApi;
import com.sunmi.assistant.pos.response.PosListResp;
import com.sunmi.cloudprinter.rpc.IOTCloudApi;
import com.sunmi.ipc.rpc.IpcCloudApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import sunmi.common.base.BaseActivity;
import sunmi.common.base.BaseApplication;
import sunmi.common.base.BasePresenter;
import sunmi.common.constant.CommonConfig;
import sunmi.common.constant.enums.DeviceStatus;
import sunmi.common.model.ShopInfo;
import sunmi.common.model.ShopListResp;
import sunmi.common.model.SunmiDevice;
import sunmi.common.notification.BaseNotification;
import sunmi.common.router.model.IpcListResp;
import sunmi.common.rpc.cloud.SunmiStoreApi;
import sunmi.common.rpc.http.HttpCallback;
import sunmi.common.rpc.http.RpcCallback;
import sunmi.common.rpc.retrofit.RetrofitCallback;
import sunmi.common.rpc.sunmicall.ResponseBean;
import sunmi.common.utils.DBUtils;
import sunmi.common.utils.SpUtils;
import sunmi.common.utils.ThreadPool;

/**
 * Description:
 * Created by bruce on 2019/6/27.
 */
public class DevicePresenter extends BasePresenter<DeviceContract.View>
        implements DeviceContract.Presenter {

    private BaseActivity activity;

    public DevicePresenter(BaseActivity mActivity) {
        this.activity = mActivity;
    }

    /**
     * 校验路由器是否配置
     */
    @Override
    public void apConfig(String sn) {
        APCall.getInstance().apIsConfig(activity, sn);
    }

    /**
     * 校验路由器登录密码
     */
    @Override
    public void apCheckLogin(String password) {
        APCall.getInstance().checkLogin(activity, password);
    }

    /**
     * factory: 0已初始配置 1未初始化设置
     * 检测ap是否配置：已配置校验密码 ,未配置进行搜索
     */
    @Override
    public void getApConfig(ResponseBean res) {
        if (TextUtils.equals("0", res.getErrCode())) {
            ApConfigResp resp = new Gson().fromJson(res.getResult().toString(), ApConfigResp.class);
            String factory = resp.getSystem().getFactory();
            if (isViewAttached()) {
                mView.getApConfigSuccess(factory);
            }
        }
    }

    @Override
    public void getStatusEvent(String result, List<SunmiDevice> routerList) {
        if (TextUtils.isEmpty(result)) {
            return;
        }
        ApEventResp eventResp = new Gson().fromJson(result, ApEventResp.class);
        String opcode = eventResp.getParams().get(0).getEvent();
        int event = EncryptUtils.decodeOp(opcode);
        if (NotificationConstant.apOnline == event) { //在线状态
            String sn = eventResp.getParams().get(0).getParam().getSn();
            if (isViewAttached()) {
                mView.apEventStatus(sn, true);
            }
        } else if (NotificationConstant.apOffline == event) {//离线状态
            String sn = eventResp.getParams().get(0).getParam().getSn();
            if (isViewAttached()) {
                mView.apEventStatus(sn, false);
            }
            if (TextUtils.equals(sn, AppConfig.GLOBAL_SN)) {
                Intent intent = new Intent();
                intent.setAction(AppConfig.BROADCAST_ACTION);
                intent.putExtra("type", AppConfig.BROADCAST_STATUS);
                activity.sendBroadcast(intent);
                BaseNotification.newInstance().postNotificationName(NotificationConstant.apOffline, sn);
            }
        } else if (NotificationConstant.apStatusList == event) {//w1所有设备列表
            routerList.clear();
            List<ApEventResp.ParamsBean.ParamBean.DeviceListBean> beanList =
                    eventResp.getParams().get(0).getParam().getDeviceList();
            for (ApEventResp.ParamsBean.ParamBean.DeviceListBean bean : beanList) {
                int shopId = bean.getShopId();
                if (shopId == SpUtils.getShopId()) {
                    SunmiDevice device = new SunmiDevice();
                    device.setDeviceid(bean.getSn());
                    device.setStatus(bean.getActiveStatus());
                    device.setShopId(shopId);
                    device.setName("SUNMI-W1");
                    device.setModel("W1");
                    device.setType("ROUTER");
                    routerList.add(device);
                }
            }
            if (isViewAttached()) {
                mView.refreshApEventStatus();
            }
        }
    }

    /**
     * 设备ap登录，检测管理密码item
     */
    @Override
    public void checkApLoginPassword(ResponseBean res) {
        String errorCode = res.getErrCode();
        if (TextUtils.equals(errorCode, "0")) {
            ApLoginResp resp = new Gson().fromJson(res.getResult().toString(), ApLoginResp.class);
            SpUtils.saveRouterToken(resp.getAccount().getToken());
            if (isViewAttached()) {
                mView.getCheckApLoginSuccess();
            }
        } else if (TextUtils.equals(errorCode, AppConfig.ERROR_CODE_PASSWORD_ERROR)
                || TextUtils.equals(errorCode, AppConfig.ERROR_CODE_PASSWORD_INVALID)) {// 账户密码错误 ,账户登录缺少密码
            if (isViewAttached()) {
                mView.getCheckApLoginFail(errorCode);
            }
        } else if (TextUtils.equals(errorCode, AppConfig.ERROR_CODE_PASSWORD_INCORRECT_MANY)) { // 账户密码错误次数过多
            if (isViewAttached()) {
                mView.shortTip(R.string.tip_password_fail_too_often);
            }
        } else if (TextUtils.equals(errorCode, AppConfig.ERROR_CODE_UNSET_PASSWORD)) { // 账户密码未设置
            activity.startActivity(new Intent(activity, PrimaryRouteStartActivity.class));
        }
    }

    @Override
    public void getRouterList() {
        CloudApi.getBindDeviceList(SpUtils.getShopId(), new RpcCallback(null) {
            @Override
            public void onSuccess(int code, String msg, String data) {
                ThreadPool.getCachedThreadPool().submit(() -> {
                    DBUtils.deleteSunmiDeviceByType("ROUTER");
                    List<SunmiDevice> list = new ArrayList<>();
                    try {
                        JSONArray jsonArray = new JSONArray(data);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            list.add(getRouterDevice((JSONObject) jsonArray.opt(i)));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (isViewAttached()) {
                        mView.endRefresh();
                        mView.getRouterListSuccess(list);
                    }
                });
            }

            @Override
            public void onError(int code, String msg, String data) {
                if (isViewAttached()) {
                    mView.endRefresh();
                }
            }
        });
    }

    @Override
    public void unbind(SunmiDevice device) {
        switch (device.getType()) {
            case "ROUTER":
                unbindRouter(device.getDeviceid());
                break;
            case "IPC":
                unbindIPC(device.getId());
                break;
            case "PRINTER":
                unbindPrinter(device.getDeviceid());
                break;
        }
    }

    private void unbindRouter(String sn) {
        CloudApi.unbind(sn, new HttpCallback<String>(null) {
            @Override
            public void onSuccess(int code, String msg, String data) {
                if (isViewAttached()) {
                    mView.shortTip(R.string.str_delete_success);
                    DBUtils.deleteSunmiDevice(sn);
                    getRouterList();
                }
            }

            @Override
            public void onFail(int code, String msg, String data) {
                if (isViewAttached()) {
                    mView.shortTip(R.string.str_delete_fail);
                }
            }
        });
    }

    @Override
    public void getIpcList() {
        IpcCloudApi.getInstance().getDetailList(SpUtils.getCompanyId(), SpUtils.getShopId(),
                new RetrofitCallback<IpcListResp>() {
                    @Override
                    public void onSuccess(int code, String msg, IpcListResp data) {
                        ThreadPool.getCachedThreadPool().submit(() -> {
                            DBUtils.deleteSunmiDeviceByType("IPC");
                            List<SunmiDevice> list = new ArrayList<>();
                            if (data.getFs_list() != null && data.getFs_list().size() > 0) {
                                for (IpcListResp.SsListBean bean : data.getFs_list()) {
                                    SunmiDevice device = getIpcDevice(bean);
                                    list.add(device);
                                }
                            }
                            if (data.getSs_list() != null && data.getSs_list().size() > 0) {
                                for (IpcListResp.SsListBean bean : data.getSs_list()) {
                                    SunmiDevice device = getIpcDevice(bean);
                                    list.add(device);
                                }
                            }
                            if (isViewAttached()) {
                                mView.endRefresh();
                                mView.getIpcListSuccess(list);
                            }
                        });
                    }

                    @Override
                    public void onFail(int code, String msg, IpcListResp data) {
                        if (isViewAttached()) {
                            mView.endRefresh();
                        }
                    }
                });
    }

    private void unbindIPC(int deviceId) {
        IpcCloudApi.getInstance().unbindIpc(SpUtils.getCompanyId(), SpUtils.getShopId(), deviceId,
                new RetrofitCallback<Object>() {
                    @Override
                    public void onSuccess(int code, String msg, Object data) {
                        if (isViewAttached()) {
                            mView.shortTip(R.string.str_delete_success);
                            mView.unbindIpcSuccess(code, msg, data);
                        }
                    }

                    @Override
                    public void onFail(int code, String msg, Object data) {
                        if (isViewAttached()) {
                            mView.shortTip(R.string.str_delete_fail);
                        }
                    }
                });
    }

    @Override
    public void getPrinterList() {
        if (!CommonConfig.SUPPORT_PRINTER) {
            return;
        }
        IOTCloudApi.getPrinterList(SpUtils.getShopId(), new HttpCallback<String>(null) {
            @Override
            public void onSuccess(int code, String msg, String data) {
                if (isViewAttached()) {
                    mView.endRefresh();
                    DBUtils.deleteSunmiDeviceByType("PRINTER");
                    mView.getPrinterListSuccess(getList(data));
                }
            }

            @Override
            public void onFail(int code, String msg, String data) {
                if (isViewAttached()) {
                    mView.endRefresh();
                }
            }
        });
    }

    @Override
    public void getPosList() {
        PosApi.getInstance().getPosList(new RetrofitCallback<PosListResp>() {
            @Override
            public void onSuccess(int code, String msg, PosListResp data) {
                ThreadPool.getCachedThreadPool().submit(() -> {
                    DBUtils.deleteSunmiDeviceByType("POS");
                    List<SunmiDevice> posList = new ArrayList<>();
                    if (data.getDeviceList().size() > 0) {
                        for (PosListResp.DeviceListBean bean : data.getDeviceList()) {
                            posList.add(getPosDevice(bean));
                        }
                    }
                    if (isViewAttached()) {
                        mView.endRefresh();
                        mView.getPosListSuccess(posList);
                    }
                });
            }

            @Override
            public void onFail(int code, String msg, PosListResp data) {
                if (isViewAttached()) {
                    mView.endRefresh();
                }
            }
        });
    }

    @Override
    public void getPrinterStatus(final String sn) {
        IOTCloudApi.getPrinterStatus(sn, new HttpCallback<String>(null) {
            @Override
            public void onSuccess(int code, String msg, String data) {
                try {
                    getPrinterDevice(new JSONObject(data));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void unbindPrinter(final String sn) {
        IOTCloudApi.unbindPrinter(SpUtils.getShopId(), sn, new HttpCallback<String>(null) {
            @Override
            public void onSuccess(int code, String msg, String data) {
                if (isViewAttached()) {
                    mView.unbindPrinterSuccess(sn);
                }
            }
        });
    }

    @Override
    public void getShopList() {
        mView.showLoadingDialog();
        SunmiStoreApi.getInstance().getShopList(SpUtils.getCompanyId(), new RetrofitCallback<ShopListResp>() {
            @Override
            public void onSuccess(int code, String msg, ShopListResp data) {
                if (isViewAttached()) {
                    mView.hideLoadingDialog();
                    List<ShopInfo> shopList = data.getShop_list();
                    if (shopList == null) {
                        return;
                    }
                    List<ShopInfo> newShopList = new ArrayList<>();
                    for (ShopInfo shop : shopList) {
                        if (shop.getShopId() == SpUtils.getShopId()) {
                            newShopList.add(0, shop);
                        } else {
                            newShopList.add(shop);
                        }
                    }
                    mView.getShopListSuccess(newShopList);
                }
            }

            @Override
            public void onFail(int code, String msg, ShopListResp data) {
                if (isViewAttached()) {
                    mView.hideLoadingDialog();
                    mView.shortTip(R.string.str_server_exception);
                }
            }
        });
    }

    private List<SunmiDevice> getList(String data) {
        List<SunmiDevice> list = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(data);
            if (jsonObject.has("device")) {
                JSONArray array = jsonObject.getJSONArray("device");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    list.add(getPrinterDevice(object));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    @NonNull
    private SunmiDevice getRouterDevice(JSONObject object) throws JSONException {
        SunmiDevice device = new SunmiDevice();
        device.setType("ROUTER");
        if (object.has("sn")) {
            device.setDeviceid(object.getString("sn"));
        }
        if (object.has("active_status")) {
            device.setStatus(object.getInt("active_status"));
        }
        if (TextUtils.equals(device.getDeviceid(), MyNetworkCallback.CURRENT_ROUTER)) {
            if (device.getStatus() == DeviceStatus.OFFLINE.ordinal()) {
                device.setStatus(DeviceStatus.EXCEPTION.ordinal());
            }
        }
        if (object.has("model")) {
            device.setModel(object.getString("model"));
            device.setName(object.getString("model"));
        }
        if (object.has("shop_id")) {
            device.setShopId(object.getInt("shop_id"));
        } else {
            device.setShopId(SpUtils.getShopId());
        }
        saveDevice(device);
        return device;
    }

    @NonNull
    private SunmiDevice getIpcDevice(IpcListResp.SsListBean bean) {
        SunmiDevice device = new SunmiDevice();
        device.setType("IPC");
        device.setStatus(bean.getActive_status());
        device.setDeviceid(bean.getSn());
        device.setModel(bean.getModel());
        device.setName(bean.getDevice_name());
        device.setImgPath(bean.getCdn_address());
        device.setUid(bean.getUid());
        device.setShopId(bean.getShop_id());
        device.setId(bean.getId());
        device.setFirmware(bean.getBin_version());
        saveDevice(device);
        return device;
    }

    @NonNull
    private SunmiDevice getPrinterDevice(JSONObject object) throws JSONException {
        SunmiDevice device = new SunmiDevice();
        device.setType("PRINTER");
        device.setName(BaseApplication.getContext().getString(R.string.str_cloud_printer));
        device.setModel("NT211");
        if (object.has("msn")) {
            device.setDeviceid(object.getString("msn"));
        }
        if (object.has("isOnline")) {
            device.setStatus(object.getInt("isOnline"));
        }
        if (object.has("merchantId")) {
            device.setShopId(object.getInt("merchantId"));
        }
        if (object.has("channelId")) {
            device.setChannelId(object.getInt("channelId"));
        } else {
            device.setChannelId(1);
        }
        saveDevice(device);
        return device;
    }

    @NonNull
    private SunmiDevice getPosDevice(PosListResp.DeviceListBean bean) {
        SunmiDevice device = new SunmiDevice();
        device.setType("POS");
        device.setDeviceid(bean.getSn());
        device.setModel(bean.getModel());
        device.setDisplayModel(bean.getDisplayModel());
        device.setPosModelDetails(bean.getModelDetail());
        device.setChannelId(bean.getChannelId());
        device.setStatus(bean.getActiveStatus());
        device.setImgPath(bean.getImgPath());
        device.setShopId(SpUtils.getShopId());
        saveDevice(device);
        return device;
    }

    private void saveDevice(SunmiDevice device) {
        device.saveOrUpdate("deviceid=?", device.getDeviceid());
    }
}
