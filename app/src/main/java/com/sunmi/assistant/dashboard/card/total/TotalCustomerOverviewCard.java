package com.sunmi.assistant.dashboard.card.total;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunmi.assistant.R;
import com.sunmi.assistant.dashboard.card.BaseRefreshCard;
import com.sunmi.assistant.dashboard.data.DashboardCondition;
import com.sunmi.assistant.dashboard.util.Constants;
import com.sunmi.assistant.dashboard.util.Utils;

import java.util.Locale;

import retrofit2.Call;
import sunmi.common.base.recycle.BaseViewHolder;
import sunmi.common.model.Interval;
import sunmi.common.model.TotalCustomerDataResp;
import sunmi.common.rpc.cloud.SunmiStoreApi;
import sunmi.common.rpc.retrofit.BaseResponse;

public class TotalCustomerOverviewCard extends BaseRefreshCard<TotalCustomerOverviewCard.Model, TotalCustomerDataResp> {

    private static TotalCustomerOverviewCard sInstance;

    private TotalCustomerOverviewCard(Presenter presenter, DashboardCondition condition, int period, Interval periodTime) {
        super(presenter, condition, period, periodTime);
    }

    public static TotalCustomerOverviewCard get(Presenter presenter, DashboardCondition condition, int period, Interval periodTime) {
        if (sInstance == null) {
            sInstance = new TotalCustomerOverviewCard(presenter, condition, period, periodTime);
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
        return R.layout.dashboard_item_total_customer_overview;
    }

    @Override
    protected Call<BaseResponse<TotalCustomerDataResp>> load(int companyId, int shopId, int period, Interval periodTime, CardCallback callback) {
        String startTime = Utils.formatTime(Utils.FORMAT_API_DATE, periodTime.start);
        SunmiStoreApi.getInstance().getTotalCustomer(companyId, startTime, period, callback);
        return null;
    }

    @Override
    protected Model createModel() {
        return new Model();
    }

    @Override
    protected void setupModel(Model model, TotalCustomerDataResp response) {
        int count = Math.max(0, response.getPassengerCount());
        int earlyCount = Math.max(0, response.getEarlyPassengerCount());
        int head = Math.max(0, response.getPassHeadCount());
        int earlHead = Math.max(0, response.getEarlyPassHeadCount());
        model.count = count;
        if (count > 0) {
            model.enterRate = (float) count / (count + head);
        } else {
            model.enterRate = 0;
        }
        if (earlyCount > 0) {
            model.compareCount = (float) (count - earlyCount) / earlyCount;
        } else if (count > 0) {
            model.compareCount = null;
        } else {
            model.compareCount = 0f;
        }
        float earlyRate;
        if (earlyCount > 0) {
            earlyRate = (float) earlyCount / (earlyCount + earlHead);
        } else {
            earlyRate = 0;
        }
        model.compareRate = model.enterRate - earlyRate;
    }

    @Override
    protected void setupView(@NonNull BaseViewHolder<Model> holder, Model model, int position) {
        setupPeriod(holder, mPeriod);
        Context context = holder.getContext();
        TextView customer = holder.getView(R.id.tv_total_customer);
        TextView customerSubData = holder.getView(R.id.tv_total_customer_subdata);
        ImageView ivCustomer = holder.getView(R.id.iv_total_customer_trend);
        TextView enterRate = holder.getView(R.id.tv_enter_rate);
        TextView rateSubData = holder.getView(R.id.tv_enter_rate_subdata);
        ImageView ivRate = holder.getView(R.id.iv_total_enter_rate);
        customer.setText(model.getCount(context));
        enterRate.setText(model.getEnterRate());
        rateSubData.setText(model.getCompareRate());
        if (model.compareCount == null) {
            customerSubData.setTextColor(ContextCompat.getColor(context, R.color.text_caption));
            ivCustomer.setVisibility(View.INVISIBLE);
            customerSubData.setText(Utils.DATA_NONE);
        } else if (model.compareCount > 0) {
            customerSubData.setText(model.getCompareCount());
            customerSubData.setTextColor(ContextCompat.getColor(context, R.color.caution_primary));
            ivCustomer.setVisibility(View.VISIBLE);
            ivCustomer.setSelected(true);
        } else if (model.compareCount < 0) {
            customerSubData.setText(model.getCompareCount());
            customerSubData.setTextColor(ContextCompat.getColor(context, R.color.success_primary));
            ivCustomer.setVisibility(View.VISIBLE);
            ivCustomer.setSelected(false);
        } else {
            customerSubData.setText(model.getCompareCount());
            customerSubData.setTextColor(ContextCompat.getColor(context, R.color.text_caption));
            ivCustomer.setVisibility(View.INVISIBLE);
        }

        if (model.compareRate > 0) {
            rateSubData.setTextColor(ContextCompat.getColor(context, R.color.caution_primary));
            ivRate.setVisibility(View.VISIBLE);
            ivRate.setSelected(true);
        } else if (model.compareRate < 0) {
            rateSubData.setTextColor(ContextCompat.getColor(context, R.color.success_primary));
            ivRate.setVisibility(View.VISIBLE);
            ivRate.setSelected(false);
        } else {
            rateSubData.setTextColor(ContextCompat.getColor(context, R.color.text_caption));
            ivRate.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void showLoading(@NonNull BaseViewHolder<Model> holder, Model model, int position) {
        ImageView loading = holder.getView(R.id.iv_dashboard_loading);
        loading.setImageResource(R.mipmap.dashboard_skeleton_single);
        holder.getView(R.id.layout_total_customer).setVisibility(View.INVISIBLE);
        holder.getView(R.id.layout_enter_rate).setVisibility(View.INVISIBLE);
        loading.setVisibility(View.VISIBLE);
    }

    @Override
    protected void showError(@NonNull BaseViewHolder<Model> holder, Model model, int position) {
        setupPeriod(holder, mPeriod);
        Context context = holder.getContext();
        TextView customer = holder.getView(R.id.tv_total_customer);
        TextView customerSubData = holder.getView(R.id.tv_total_customer_subdata);
        holder.getView(R.id.iv_total_customer_trend).setVisibility(View.INVISIBLE);
        TextView enterRate = holder.getView(R.id.tv_enter_rate);
        TextView rateSubData = holder.getView(R.id.tv_enter_rate_subdata);
        holder.getView(R.id.iv_total_enter_rate).setVisibility(View.INVISIBLE);
        customerSubData.setTextColor(ContextCompat.getColor(context, R.color.text_caption));
        rateSubData.setTextColor(ContextCompat.getColor(context, R.color.text_caption));
        customer.setText(Utils.DATA_NONE);
        customerSubData.setText(Utils.DATA_NONE);
        enterRate.setText(Utils.DATA_NONE);
        rateSubData.setText(Utils.DATA_NONE);
    }

    private void setupPeriod(@NonNull BaseViewHolder<Model> holder, int period) {
        TextView customerSubTitle = holder.getView(R.id.tv_total_customer_subtitle);
        TextView rateSubTitle = holder.getView(R.id.tv_enter_rate_subtitle);
        holder.getView(R.id.iv_dashboard_loading).setVisibility(View.GONE);
        holder.getView(R.id.layout_total_customer).setVisibility(View.VISIBLE);
        holder.getView(R.id.layout_enter_rate).setVisibility(View.VISIBLE);
        switch (period) {
            case Constants.TIME_PERIOD_DAY:
                customerSubTitle.setText(R.string.dashboard_time_before_day);
                rateSubTitle.setText(R.string.dashboard_time_before_day);
                break;
            case Constants.TIME_PERIOD_WEEK:
                customerSubTitle.setText(R.string.dashboard_time_before_week);
                rateSubTitle.setText(R.string.dashboard_time_before_week);
                break;
            case Constants.TIME_PERIOD_MONTH:
                customerSubTitle.setText(R.string.dashboard_time_before_month);
                rateSubTitle.setText(R.string.dashboard_time_before_month);
                break;
            default:
                break;
        }
    }

    public static class Model extends BaseRefreshCard.BaseModel {
        int count;
        float enterRate;
        Float compareCount;
        float compareRate;

        private CharSequence getCount(Context context) {
            return Utils.formatNumber(context, count, false, true);
        }

        private CharSequence getEnterRate() {
            return Utils.formatPercent(enterRate, true, true);
        }

        private CharSequence getCompareCount() {
            return String.format(Locale.getDefault(), "%.2f%%", Math.abs(compareCount) * 100);
        }

        private CharSequence getCompareRate() {
            return String.format(Locale.getDefault(), "%.2f%%", Math.abs(compareRate) * 100);
        }
    }
}
