package com.sunmi.assistant.ui.activity.contract;

import com.sunmi.assistant.ui.activity.model.AuthStoreInfo;

import sunmi.common.base.BaseView;

/**
 * Created by YangShiJie on 2019/7/3.
 */
public interface PlatformMobileContract {
    interface View extends BaseView {
        void sendMobileCodeSuccess(Object data);

        void sendMobileCodeFail(int code, String msg);

        void checkMobileCodeSuccess(Object data);

        void checkMobileCodeFail(int code, String msg);

        void getSaasInfoSuccess(AuthStoreInfo data);

        void getSaasInfoFail(int code, String msg);

//        void createStoreSuccess(CreateShopInfo data);
//
//        void createStoreFail(int code, String msg);

    }

    interface Presenter {
        void sendMobileCode(String mobile);

        void checkMobileCode(String mobile, String code);

        void getSaasInfo(String mobile);

//        void createStore(String shopName);
    }
}
