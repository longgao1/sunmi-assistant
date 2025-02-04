package com.sunmi.assistant.dashboard.card.shop;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.sunmi.assistant.R;
import com.sunmi.assistant.dashboard.card.BaseRefreshCard;
import com.sunmi.assistant.dashboard.data.DashboardCondition;
import com.xiaojinzi.component.impl.Router;

import retrofit2.Call;
import sunmi.common.base.recycle.BaseViewHolder;
import sunmi.common.base.recycle.ItemType;
import sunmi.common.constant.CommonConstants;
import sunmi.common.constant.enums.ModelType;
import sunmi.common.model.Interval;
import sunmi.common.router.IpcApi;
import sunmi.common.rpc.retrofit.BaseResponse;

/**
 * @author yinhui
 * @since 2019-07-01
 */
public class CustomerNoFsCard extends BaseRefreshCard<CustomerNoFsCard.Model, Object> {

    private static CustomerNoFsCard sInstance;

    private int mColorGray;
    private int mColorWhite;
    private GradientDrawable mContentBg;

    private CustomerNoFsCard(Presenter presenter, DashboardCondition condition, int period, Interval periodTime) {
        super(presenter, condition, period, periodTime);
    }

    public static CustomerNoFsCard get(Presenter presenter, DashboardCondition condition, int period, Interval periodTime) {
        if (sInstance == null) {
            sInstance = new CustomerNoFsCard(presenter, condition, period, periodTime);
        } else {
            sInstance.reset(presenter, condition, period, periodTime);
        }
        return sInstance;
    }

    @Override
    public void init(Context context) {
    }

    @Override
    public int getLayoutId(int type) {
        return R.layout.dashboard_item_no_fs;
    }

    @Override
    protected Call<BaseResponse<Object>> load(int companyId, int shopId, int period, Interval periodTime,
                                              CardCallback callback) {
        callback.onSuccess();
        return null;
    }

    @Override
    protected Model createModel() {
        return new Model();
    }

    @Override
    protected void setupModel(Model model, Object response) {
    }

    @NonNull
    @Override
    public BaseViewHolder<Model> onCreateViewHolder(@NonNull View view, @NonNull ItemType<Model, BaseViewHolder<Model>> type) {
        BaseViewHolder<Model> holder = super.onCreateViewHolder(view, type);
        Context context = holder.getContext();
        this.mColorGray = ContextCompat.getColor(context, R.color.common_fill);
        this.mColorWhite = 0xFFFFFFFF;
        float radius = context.getResources().getDimension(R.dimen.dp_12);
        this.mContentBg = new GradientDrawable();
        this.mContentBg.setCornerRadii(new float[]{radius, radius, radius, radius, radius, radius, radius, radius});
        view.setPaddingRelative(0, 0, 0, (int) context.getResources().getDimension(R.dimen.dp_32));

        holder.addOnClickListener(R.id.btn_dashboard_add, (h, model, position) ->
                Router.withApi(IpcApi.class).goToIpcStartConfig(context, ModelType.MODEL_FS,CommonConstants.CONFIG_IPC_FROM_COMMON));
        return holder;
    }

    @Override
    protected void setupView(@NonNull BaseViewHolder<Model> holder, Model model, int position) {
        View root = holder.getView(R.id.layout_dashboard_root);
        View content = holder.getView(R.id.layout_dashboard_content);
        root.setBackgroundColor(mColorWhite);
        mContentBg.setColor(mColorGray);
        content.setBackground(mContentBg);
    }

    public static class Model extends BaseRefreshCard.BaseModel {
    }
}
