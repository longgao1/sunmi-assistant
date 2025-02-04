package com.sunmi.assistant.ui.activity.login;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;

import com.sunmi.assistant.ui.MergeDialog;
import com.sunmi.apmanager.utils.HelpUtils;
import com.sunmi.apmanager.utils.SomeMonitorEditText;
import com.sunmi.assistant.R;
import com.sunmi.assistant.ui.activity.contract.InputMobileContract;
import com.sunmi.assistant.ui.activity.presenter.InputMobilePresenter;
import com.xiaojinzi.component.anno.RouterAnno;
import com.xiaojinzi.component.impl.RouterRequest;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import sunmi.common.base.BaseMvpActivity;
import sunmi.common.constant.RouterConfig;
import sunmi.common.utils.CommonHelper;
import sunmi.common.utils.RegexUtils;
import sunmi.common.utils.ViewUtils;
import sunmi.common.view.ClearableEditText;
import sunmi.common.view.dialog.CommonDialog;

import static sunmi.common.view.activity.ProtocolActivity.USER_PRIVATE;
import static sunmi.common.view.activity.ProtocolActivity.USER_PROTOCOL;

/**
 * register
 */
@EActivity(R.layout.activity_register)
public class RegisterActivity extends BaseMvpActivity<InputMobilePresenter>
        implements InputMobileContract.View {

    @ViewById(R.id.etMobile)
    ClearableEditText etMobile;
    @ViewById(R.id.btnNext)
    Button btnNext;
    @ViewById(R.id.ctv_privacy)
    CheckedTextView ctvPrivacy;

    private String mobile;

    @RouterAnno(
            path = RouterConfig.App.REGISTER
    )
    public static Intent start(RouterRequest request) {
        Intent intent = new Intent(request.getRawContext(), RegisterActivity_.class);
        return intent;
    }

    @AfterViews
    protected void init() {
        HelpUtils.setStatusBarFullTransparent(this);//透明标题栏
        mPresenter = new InputMobilePresenter();
        mPresenter.attachView(this);
        etMobile.setClearIcon(R.mipmap.ic_edit_delete_white);
        new SomeMonitorEditText().setMonitorEditText(btnNext, etMobile);
        //初始化
        ViewUtils.setPrivacy(this, ctvPrivacy, R.color.white_40a, USER_PROTOCOL, USER_PRIVATE);
        if (!TextUtils.isEmpty(mobile)) {
            etMobile.setText(mobile);
            CommonHelper.setSelectionEnd(etMobile);
        }
    }

    @Click(R.id.btnNext)
    public void onClick(View v) {
        if (isFastClick(1500) || etMobile.getText() == null) {
            return;
        }
        mobile = RegexUtils.handleIllegalCharacter(etMobile.getText().toString().trim());
        if (!ctvPrivacy.isChecked()) {
            shortTip(R.string.tip_agree_protocol);
            return;
        }
        if (!RegexUtils.isCorrectAccount(mobile)) {
            shortTip(R.string.str_invalid_phone);
            return;
        }
        invalidAccount();
    }

    private void invalidAccount() {
        showDarkLoading();
        mPresenter.isUserExist(mobile);
    }

    //手机号已注册
    private void mobileRegistered() {
        runOnUiThread(() -> new CommonDialog.Builder(RegisterActivity.this)
                .setTitle(R.string.tip_register_already)
                .setCancelButton(R.string.sm_cancel)
                .setConfirmButton(R.string.str_goto_register, (dialog, which) -> {
                    LoginActivity_.intent(context).mobile(mobile).start();
                    finish();
                }).create().show());
    }

    //账号合并
    public void userMerge() {
        if (etMobile.getText() == null) return;
        String user = etMobile.getText().toString();//email test: esyzim06497@chacuo.net
        if (RegexUtils.isCorrectAccount(user)) {
            showDarkLoading();
            mPresenter.checkUserName(user);
        }
    }

    @Override
    public void checkSuccess(int code, String data) {
        try {
            if (code == 1) {
                JSONObject object = new JSONObject(data);
                if (object.has("needMerge")) {
                    //needMerge 是否需要合并 0-否 1-是
                    int needMerge = object.getInt("needMerge");
                    String url = object.getString("url");
                    if (needMerge == 1) {
                        new MergeDialog(context, url).show();
                    } else {
                        mobileRegistered();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void isUserExistSuccess() {
        hideLoadingDialog();
        userMerge();
    }

    @Override
    public void isUserExistFail(int code, String msg) {
        hideLoadingDialog();
        InputCaptchaActivity_.intent(context).mobile(mobile).source("register").start();
    }

}
