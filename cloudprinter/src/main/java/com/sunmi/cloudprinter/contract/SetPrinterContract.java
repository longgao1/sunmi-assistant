package com.sunmi.cloudprinter.contract;

import com.sunmi.cloudprinter.bean.PrintRouter;

import sunmi.common.base.BaseView;

public interface SetPrinterContract {

    interface View extends BaseView {
        void onInitMtu();

        void onSendMessage(final byte[] data);

        void onInitNotify();

        void initRouter(PrintRouter printRouter);

//        void hideProgressBar();

        void setSn(String sn);

        void wifiSetSuccess();
    }

    interface Presenter {
        void initMtuSuccess();

        void initMtuFailed();

        void initNotifySuccess(byte version);

        void initNotifyFailed();

        void onNotify(byte[] value, byte version);
    }
}
