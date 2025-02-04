package com.sunmi.assistant.ui.activity.presenter;

import android.content.Context;

import com.sunmi.assistant.R;
import com.sunmi.assistant.ui.activity.contract.SetPasswordContract;

import sunmi.common.base.BasePresenter;
import sunmi.common.model.CompanyListResp;
import sunmi.common.rpc.cloud.SunmiStoreApi;
import sunmi.common.rpc.cloud.SunmiStoreRetrofitClient;
import sunmi.common.rpc.retrofit.RetrofitCallback;
import sunmi.common.utils.SpUtils;

/**
 * Description:
 *
 * @author linyuanpeng on 2019-08-02.
 */
public class SetPasswordPresenter extends BasePresenter<SetPasswordContract.View>
        implements SetPasswordContract.Presenter {

    @Override
    public void register(String username, String password, String code) {
        if (isViewAttached()) {
            mView.showDarkLoading();
        }
        SunmiStoreApi.getInstance().register(username, password, code, new RetrofitCallback<Object>() {
            @Override
            public void onSuccess(int code, String msg, Object data) {
                if (isViewAttached()) {
                    mView.hideLoadingDialog();
                    SpUtils.setStoreToken(data.toString());
                    //初始化retrofit
                    SunmiStoreRetrofitClient.createInstance();
                    mView.registerSuccess();
                }
            }

            @Override
            public void onFail(int code, String msg, Object data) {
                if (isViewAttached()) {
                    mView.hideLoadingDialog();
                    mView.registerFail(code, msg);
                }
            }
        });
    }

    @Override
    public void resetPassword(Context context,String username, String password, String code) {
        if (isViewAttached()){
            mView.showDarkLoading(context.getString(R.string.str_now_reset_psw));
        }
        SunmiStoreApi.getInstance().resetPassword(username, password, code, new RetrofitCallback<Object>() {
            @Override
            public void onSuccess(int code, String msg, Object data) {
                if (isViewAttached()) {
                    mView.hideLoadingDialog();
                    mView.resetPasswordSuccess();
                }
            }

            @Override
            public void onFail(int code, String msg, Object data) {
                if (isViewAttached()) {
                    mView.hideLoadingDialog();
                    mView.reSetPasswordFail(code, msg);
                }
            }
        });
    }

    @Override
    public void getCompanyList() {
        if (isViewAttached()) {
            mView.showDarkLoading();
        }
        SunmiStoreApi.getInstance().getCompanyList(new RetrofitCallback<CompanyListResp>() {
            @Override
            public void onSuccess(int code, String msg, CompanyListResp data) {
                if (isViewAttached()) {
                    mView.hideLoadingDialog();
                    mView.getCompanyListSuccess(data.getCompany_list());
                }
            }

            @Override
            public void onFail(int code, String msg, CompanyListResp data) {
                if (isViewAttached()) {
                    mView.hideLoadingDialog();
                    mView.getCompanyListFail(code, msg);
                }
            }
        });

    }
}
