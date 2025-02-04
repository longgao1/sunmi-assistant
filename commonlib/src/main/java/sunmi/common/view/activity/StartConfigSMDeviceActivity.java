package sunmi.common.view.activity;

import android.os.Handler;
import android.support.v4.widget.NestedScrollView;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.commonlibrary.R;
import com.xiaojinzi.component.impl.Router;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import sunmi.common.base.BaseActivity;
import sunmi.common.constant.enums.ModelType;
import sunmi.common.router.ApManagerApi;
import sunmi.common.utils.CommonHelper;
import sunmi.common.utils.StatusBarUtils;
import sunmi.common.view.TitleBarView;
import sunmi.common.view.dialog.TipDialog;

/**
 * Created by YangShiJie on 2019/3/29.
 * 主路由器开始配置
 */
@EActivity(resName = "activity_start_config_sm_device")
public class StartConfigSMDeviceActivity extends BaseActivity {

    @ViewById(resName = "title_bar")
    TitleBarView titleBar;
    @ViewById(resName = "iv_image")
    ImageView ivImage;
    @ViewById(resName = "tv_tip_1")
    TextView tvTip1;
    @ViewById(resName = "nsv_tips")
    NestedScrollView nsvTips;
    @ViewById(resName = "ll_sv_root")
    LinearLayout llSvRoot;
    @ViewById(resName = "tv_tip_2")
    TextView tvTip2;
    @ViewById(resName = "tv_tip_3")
    TextView tvTip3;
    @ViewById(resName = "tv_tip_4")
    TextView tvTip4;
    @ViewById(resName = "tv_tip_5")
    TextView tvTip5;
    @ViewById(resName = "tv_config_tip")
    TextView tvConfigTip;
    @ViewById(resName = "ctv_privacy")
    CheckedTextView ctvPrivacy;
    @ViewById(resName = "view_divider_bottom")
    View viewDivider;
    @ViewById(resName = "btn_start")
    Button btnStart;

    @Extra
    String shopId;
    @Extra
    int deviceType;

    @AfterViews
    protected void init() {
        StatusBarUtils.setStatusBarColor(this, StatusBarUtils.TYPE_DARK);
        if (deviceType == ModelType.MODEL_W1) {
            initView(R.string.str_title_ap_set,
                    CommonHelper.isGooglePlay() ? R.mipmap.ic_device_config_ap_foreign : R.mipmap.ic_device_config_ap,
                    R.string.str_config_tip_ap, R.string.str_config_tip_ap_1, R.string.str_config_tip_ap_2);
            tvConfigTip.setVisibility(View.VISIBLE);
        } else if (deviceType == ModelType.MODEL_W1S) {
            initView(R.string.str_title_ap_set, R.mipmap.ic_device_config_w1s,
                    R.string.str_config_tip_ap, R.string.str_config_tip_ap_1, R.string.str_config_tip_ap_2);
            tvConfigTip.setVisibility(View.VISIBLE);
        } else if (deviceType == ModelType.MODEL_FS) {
            initView(R.string.str_title_ipc_set, R.mipmap.ic_device_config_ipc_fs,
                    R.string.str_config_tip_ipc, R.string.str_config_tip_fs_1, R.string.str_config_tip_fs_2);
            tvTip4.setVisibility(View.VISIBLE);
            tvTip4.setText(getString(R.string.str_config_tip_fs_3));
            tvTip5.setVisibility(View.VISIBLE);
            tvTip5.setText(Html.fromHtml(getString(R.string.str_config_tip_fs_4)));
        } else if (deviceType == ModelType.MODEL_SS) {
            initView(R.string.str_title_ipc_set, R.mipmap.ic_device_config_ipc_ss,
                    R.string.str_config_tip_ipc, R.string.str_config_tip_ipc_1, R.string.str_config_tip_ipc_2);
            tvTip4.setVisibility(View.VISIBLE);
            tvTip4.setText(Html.fromHtml(getString(R.string.str_config_tip_ipc_3)));
        } else if (deviceType == ModelType.MODEL_PRINTER) {
            initView(R.string.str_title_printer_set, R.mipmap.ic_device_config_printer,
                    R.string.str_config_tip_printer, R.string.str_config_tip_printer_1, R.string.str_config_tip_printer_2);
        }
        setViewDividerVisible();
    }

    private void initView(int titleRes, int imageRes, int tip1Res, int tip2Res, int tip3Res) {
        titleBar.setAppTitle(titleRes);
        ivImage.setImageResource(imageRes);
        tvTip1.setText(tip1Res);
        tvTip2.setText(Html.fromHtml(getString(tip2Res)));
        tvTip3.setText(Html.fromHtml(getString(tip3Res)));
    }

    @UiThread
    void setViewDividerVisible() {
        new Handler().postDelayed(() -> {
            if (llSvRoot.getMeasuredHeight() > nsvTips.getMeasuredHeight()) {
                viewDivider.setVisibility(View.VISIBLE);
            }
        }, 100);
    }

    @Click(resName = "tv_config_tip")
    public void configTipClick(View v) {
        configChildDialog();
    }

    @Click(resName = "btn_start")
    public void nextClick(View v) {
        if (deviceType == ModelType.MODEL_W1 || deviceType == ModelType.MODEL_W1S) {
            startPrimaryRouteSearchActivity();
        }
    }

    private void startPrimaryRouteSearchActivity() {
        Router.withApi(ApManagerApi.class).goToPrimaryRouteSearch(context, deviceType);
    }

    private void configChildDialog() {
        new TipDialog.Builder(context).tipContent(R.string.str_config_child_tip)
                .tipDrawable(ModelType.MODEL_W1S == deviceType ?
                        R.mipmap.ic_config_find_w1s : R.mipmap.ic_config_find_router)
                .create().show();
    }

}
