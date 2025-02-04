package com.sunmi.assistant.dashboard.card.total;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.sunmi.assistant.R;
import com.sunmi.assistant.dashboard.card.BaseRefreshCard;
import com.sunmi.assistant.dashboard.data.DashboardCondition;
import com.sunmi.assistant.dashboard.util.Constants;
import com.sunmi.assistant.dashboard.util.Utils;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.temporal.IsoFields;

import retrofit2.Call;
import sunmi.common.base.recycle.BaseViewHolder;
import sunmi.common.base.recycle.ItemType;
import sunmi.common.model.Interval;
import sunmi.common.rpc.retrofit.BaseResponse;

/**
 * @author yinhui
 * @since 2019-07-01
 */
public class TotalCustomerPeriodCard extends BaseRefreshCard<TotalCustomerPeriodCard.Model, Object> {

    private static TotalCustomerPeriodCard sInstance;

    private OnTimeClickListener listener;

    private TotalCustomerPeriodCard(Presenter presenter, DashboardCondition condition, int period, Interval periodTime) {
        super(presenter, condition, period, periodTime);
    }

    public static TotalCustomerPeriodCard get(Presenter presenter, DashboardCondition condition, int period, Interval periodTime) {
        if (sInstance == null) {
            sInstance = new TotalCustomerPeriodCard(presenter, condition, period, periodTime);
        } else {
            sInstance.reset(presenter, condition, period, periodTime);
        }
        return sInstance;
    }

    public void setListener(OnTimeClickListener l) {
        this.listener = l;
    }

    @Override
    public void init(Context context) {
    }

    @Override
    public int getLayoutId(int type) {
        return R.layout.dashboard_item_total_period_tab;
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
        long yesterday = System.currentTimeMillis() - Utils.MILLIS_OF_DAY;
        holder.addOnClickListener(R.id.tv_dashboard_day, (h, model, position) ->
                mPresenter.setPeriod(Constants.TIME_PERIOD_DAY, Utils.getPeriodTimestamp(Constants.TIME_PERIOD_DAY, yesterday)));
        holder.addOnClickListener(R.id.tv_dashboard_week, (h, model, position) ->
                mPresenter.setPeriod(Constants.TIME_PERIOD_WEEK, Utils.getPeriodTimestamp(Constants.TIME_PERIOD_WEEK, yesterday)));
        holder.addOnClickListener(R.id.tv_dashboard_month, (h, model, position) ->
                mPresenter.setPeriod(Constants.TIME_PERIOD_MONTH, Utils.getPeriodTimestamp(Constants.TIME_PERIOD_MONTH, yesterday)));
        holder.addOnClickListener(R.id.tv_dashboard_time, (h, model, position) -> {
            if (listener != null) {
                listener.onTimeClicked();
            }
        });
        return holder;
    }

    @Override
    protected void setupView(@NonNull BaseViewHolder<Model> holder, Model model, int position) {
        Context context = holder.getContext();
        TextView tvDay = holder.getView(R.id.tv_dashboard_day);
        TextView tvWeek = holder.getView(R.id.tv_dashboard_week);
        TextView tvMonth = holder.getView(R.id.tv_dashboard_month);
        TextView tvTime = holder.getView(R.id.tv_dashboard_time);

        tvDay.setSelected(model.period == Constants.TIME_PERIOD_DAY);
        tvDay.setTypeface(null, model.period == Constants.TIME_PERIOD_DAY ? Typeface.BOLD : Typeface.NORMAL);
        tvWeek.setSelected(model.period == Constants.TIME_PERIOD_WEEK);
        tvWeek.setTypeface(null, model.period == Constants.TIME_PERIOD_WEEK ? Typeface.BOLD : Typeface.NORMAL);
        tvMonth.setSelected(model.period == Constants.TIME_PERIOD_MONTH);
        tvMonth.setTypeface(null, model.period == Constants.TIME_PERIOD_MONTH ? Typeface.BOLD : Typeface.NORMAL);

        String pattern;
        if (model.period == Constants.TIME_PERIOD_WEEK) {
            Instant instant = Instant.ofEpochMilli(model.periodTime.start);
            LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
            int year = localDate.get(IsoFields.WEEK_BASED_YEAR);
            int week = localDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            pattern = context.getString(R.string.dashboard_unit_time_pattern_week);
            tvTime.setText(String.format(pattern, year, week));
        } else {
            if (model.period == Constants.TIME_PERIOD_DAY) {
                pattern = context.getString(R.string.dashboard_unit_time_pattern_day);
            } else if (model.period == Constants.TIME_PERIOD_MONTH) {
                pattern = context.getString(R.string.dashboard_unit_time_pattern_month);
            } else {
                pattern = Utils.FORMAT_API_DATE;
            }
            tvTime.setText(Utils.formatTime(pattern, model.periodTime.start));
        }
    }

    public static class Model extends BaseRefreshCard.BaseModel {
    }

    public interface OnTimeClickListener {
        void onTimeClicked();
    }
}
