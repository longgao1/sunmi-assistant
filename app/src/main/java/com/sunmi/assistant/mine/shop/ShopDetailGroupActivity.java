package com.sunmi.assistant.mine.shop;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.view.View;

import com.sunmi.assistant.R;
import com.sunmi.ipc.face.FaceGroupListActivity_;
import com.sunmi.ipc.rpc.IpcCloudApi;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;

import sunmi.common.base.BaseActivity;
import sunmi.common.model.ShopInfo;
import sunmi.common.router.model.IpcListResp;
import sunmi.common.rpc.retrofit.RetrofitCallback;
import sunmi.common.utils.SpUtils;
import sunmi.common.utils.StatusBarUtils;
import sunmi.common.view.SettingItemLayout;
import sunmi.common.view.TitleBarView;

/**
 * @author yangShiJie
 * @date 2019/8/19
 */
@SuppressLint("Registered")
@EActivity(R.layout.activity_mine_store_detatils_group)
public class ShopDetailGroupActivity extends BaseActivity {

    public static final String INTENT_EXTRA_SHOP_NAME = "shop_name";

    @ViewById(R.id.title_bar)
    TitleBarView titleBar;
    @ViewById(R.id.sil_shop_face)
    SettingItemLayout mSilShopFace;

    @Extra
    ShopInfo info;

    @AfterViews
    void init() {
        StatusBarUtils.setStatusBarColor(this, StatusBarUtils.TYPE_DARK);
        titleBar.setAppTitle(info.getShopName());
        titleBar.getLeftLayout().setOnClickListener(v -> onBackPressed());
//        setupFaceManagement();
    }

    private void setupFaceManagement() {
        showLoadingDialog();
        IpcCloudApi.getInstance().getDetailList(SpUtils.getCompanyId(), info.getShopId(),
                new RetrofitCallback<IpcListResp>() {
                    @Override
                    public void onSuccess(int code, String msg, IpcListResp data) {
                        if (data.getFs_list() != null && data.getFs_list().size() > 0) {
                            mSilShopFace.setVisibility(View.VISIBLE);
                        } else {
                            mSilShopFace.setVisibility(View.GONE);
                        }
                        hideLoadingDialog();
                    }

                    @Override
                    public void onFail(int code, String msg, IpcListResp data) {
                        mSilShopFace.setVisibility(View.GONE);
                        hideLoadingDialog();
                        shortTip(R.string.toast_network_error);
                    }
                });
    }


    @Click(R.id.sil_shop_detail)
    public void toShopDetail() {
        ShopDetailActivity_.intent(context).info(info).startForResult(ShopListActivity.REQUEST_CODE_SHOP);
    }

    @Click(R.id.sil_shop_face)
    public void toShopFace() {
        FaceGroupListActivity_.intent(this).mShopId(info.getShopId()).start();
    }

    @Click(R.id.sil_shop_history_arrival)
    public void toShopHistoryArrival() {

    }

    @OnActivityResult(ShopListActivity.REQUEST_CODE_SHOP)
    void onResult(int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            setResult(RESULT_OK);
            if (data != null) {
                titleBar.setAppTitle(data.getStringExtra(INTENT_EXTRA_SHOP_NAME));
            }
        }
    }
}
