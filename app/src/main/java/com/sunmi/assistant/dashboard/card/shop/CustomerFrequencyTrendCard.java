package com.sunmi.assistant.dashboard.card.shop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.renderer.XAxisRenderer;
import com.sunmi.assistant.R;
import com.sunmi.assistant.dashboard.card.BaseRefreshCard;
import com.sunmi.assistant.dashboard.data.DashboardCondition;
import com.sunmi.assistant.dashboard.ui.chart.ChartEntry;
import com.sunmi.assistant.dashboard.ui.chart.LineChartMarkerView;
import com.sunmi.assistant.dashboard.ui.chart.TimeMarkerFormatter;
import com.sunmi.assistant.dashboard.ui.chart.YAxisVolumeLabelFormatter;
import com.sunmi.assistant.dashboard.ui.chart.YAxisVolumeLabelsRenderer;
import com.sunmi.assistant.dashboard.util.Constants;
import com.sunmi.assistant.dashboard.util.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import sunmi.common.base.recycle.BaseViewHolder;
import sunmi.common.base.recycle.ItemType;
import sunmi.common.model.CustomerFrequencyTrendResp;
import sunmi.common.model.Interval;
import sunmi.common.rpc.cloud.SunmiStoreApi;
import sunmi.common.rpc.retrofit.BaseResponse;
import sunmi.common.utils.CommonHelper;

/**
 * Description:
 *
 * @author linyuanpeng on 2020-01-16.
 */
public class CustomerFrequencyTrendCard extends BaseRefreshCard<CustomerFrequencyTrendCard.Model, CustomerFrequencyTrendResp> {

    private static CustomerFrequencyTrendCard sInstance;

    private XAxisValueFormatter lineXAxisFormatter;
    private YAxisVolumeLabelsRenderer lineYAxisRenderer;
    private LineChartMarkerView mLineChartMarker;
    private MarkerFormatter mMarkerFormatter;

    private float mDashLength;
    private float mDashSpaceLength;

    private XAxisLabelRenderer lineXAxisRenderer;

    private CustomerFrequencyTrendCard(Presenter presenter, DashboardCondition condition, int period, Interval periodTime) {
        super(presenter, condition, period, periodTime);
    }

    public static CustomerFrequencyTrendCard get(Presenter presenter, DashboardCondition condition, int period, Interval periodTime) {
        if (sInstance == null) {
            sInstance = new CustomerFrequencyTrendCard(presenter, condition, period, periodTime);
        } else {
            sInstance.reset(presenter, condition, period, periodTime);
        }
        return sInstance;
    }

    @Override
    public int getLayoutId(int type) {
        return R.layout.dashboard_item_customer_frequency_trend;
    }

    @Override
    public void init(Context context) {
    }

    @Override
    protected Call<BaseResponse<CustomerFrequencyTrendResp>> load(int companyId, int shopId, int period, Interval periodTime,
                                                                  CardCallback callback) {
        String group;
        if (period == Constants.TIME_PERIOD_MONTH) {
            group = "week";
        } else {
            group = "day";
        }
        SunmiStoreApi.getInstance().getCustomerFrequencyTrend(companyId, shopId, period, group, callback);
        return null;
    }

    @Override
    protected Model createModel() {
        return new Model();
    }

    @Override
    protected void setupModel(Model model, CustomerFrequencyTrendResp response) {
        // 清空原有数据
        model.dataSet.clear();
        if (response == null || response.getFrequencyList() == null) {
            return;
        }
        // 遍历Response并创建Entry
        List<CustomerFrequencyTrendResp.Item> list = response.getFrequencyList();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            for (CustomerFrequencyTrendResp.Item item : list) {
                long time = format.parse(item.getTime()).getTime();
                int unique = item.getUniqPassengerCount();
                float y = unique <= 0 ? 0f : (float) item.getPassengerCount() / unique;
                model.dataSet.add(new ChartEntry(0f, y, time));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // 对Entry进行排序并填充x值
        Collections.sort(model.dataSet, (o1, o2) -> Long.compare(o1.getTime(), o2.getTime()));
        int i = 0;
        for (ChartEntry entry : model.dataSet) {
            entry.setX(++i);
        }
    }

    @NonNull
    @Override
    public BaseViewHolder<Model> onCreateViewHolder(@NonNull View view, @NonNull ItemType<Model, BaseViewHolder<Model>> type) {
        BaseViewHolder<Model> holder = super.onCreateViewHolder(view, type);
        Context context = view.getContext();

        mDashLength = CommonHelper.dp2px(context, 4f);
        mDashSpaceLength = CommonHelper.dp2px(context, 2f);

        LineChart lineChart = holder.getView(R.id.view_dashboard_line_chart);

        // 设置图表坐标Label格式
        lineXAxisFormatter = new XAxisValueFormatter(context);
        lineXAxisRenderer = new XAxisLabelRenderer(lineChart);
        lineYAxisRenderer = new YAxisVolumeLabelsRenderer(lineChart);
        lineChart.setRendererLeftYAxis(lineYAxisRenderer);
        lineChart.setXAxisRenderer(lineXAxisRenderer);

        // 设置通用图表
        lineChart.setTouchEnabled(true);
        lineChart.setScaleEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);

        // 设置X轴
        XAxis lineXAxis = lineChart.getXAxis();
        lineXAxis.setDrawAxisLine(true);
        lineXAxis.setDrawGridLines(false);
        lineXAxis.setTextSize(10f);
        lineXAxis.setTextColor(ContextCompat.getColor(context, R.color.text_disable));
        lineXAxis.setValueFormatter(lineXAxisFormatter);
        lineXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        lineXAxis.setAxisMinimum(0f);

        // 设置Y轴
        YAxis lineYAxis = lineChart.getAxisLeft();
        lineYAxis.setDrawAxisLine(false);
        lineYAxis.setGranularityEnabled(true);
        lineYAxis.setGranularity(1f);
        lineYAxis.setTextSize(10f);
        lineYAxis.setTextColor(ContextCompat.getColor(context, R.color.text_disable));
        lineYAxis.setAxisMinimum(0f);
        lineYAxis.setDrawGridLines(true);
        lineYAxis.setGridColor(ContextCompat.getColor(context, R.color.black_10));
        lineYAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        lineYAxis.setYOffset(-5f);
        lineYAxis.setXOffset(-1f);
        lineYAxis.setValueFormatter(new YAxisVolumeLabelFormatter(context));

        // 设置Line图
        mMarkerFormatter = new MarkerFormatter(context);
        mLineChartMarker = new LineChartMarkerView(context, mMarkerFormatter);
        mLineChartMarker.setTitle(R.string.dashboard_var_frequency);
        mLineChartMarker.setChartView(lineChart);
        lineChart.setMarker(mLineChartMarker);

        return holder;
    }

    @Override
    protected void setupView(@NonNull BaseViewHolder<Model> holder, Model model, int position) {
        TextView tvTitle = holder.getView(R.id.tv_dashboard_title);
        LineChart line = holder.getView(R.id.view_dashboard_line_chart);

        // 更新Title
        tvTitle.setText(model.period == Constants.TIME_PERIOD_WEEK
                ? R.string.dashboard_title_frequency_trend_week
                : R.string.dashboard_title_frequency_trend_month);

        List<ChartEntry> dataSet = model.dataSet;

        // Calculate min & max of axis value.
        line.getXAxis().setAxisMaximum(model.period == Constants.TIME_PERIOD_WEEK ? 8 : 6);
        int max = 0;
        float lastX = 0;
        for (ChartEntry entry : dataSet) {
            if (entry.getX() > lastX) {
                lastX = entry.getX();
            }
            if (entry.getY() > max) {
                max = (int) Math.ceil(entry.getY());
            }
        }

        float maxAxis = lineYAxisRenderer.setMaxValue(max);
        line.getAxisLeft().setAxisMaximum(maxAxis);

        // Use correct chart marker & update it.
        if (model.period == Constants.TIME_PERIOD_WEEK) {
            mMarkerFormatter.setTimeType(TimeMarkerFormatter.TIME_TYPE_DATE);
            mMarkerFormatter.setPeriod(Constants.TIME_PERIOD_DAY);
        } else {
            mMarkerFormatter.setTimeType(TimeMarkerFormatter.TIME_TYPE_DATE_SPAN);
            mMarkerFormatter.setPeriod(Constants.TIME_PERIOD_WEEK);
        }
        lineXAxisRenderer.setPeriod(model.period);
        lineXAxisFormatter.setPeriod(model.period);

        // Refresh data set
        LineDataSet set;
        LineData data = line.getData();
        ArrayList<Entry> values = new ArrayList<>(dataSet);
        if (data != null && data.getDataSetCount() > 0) {
            set = (LineDataSet) data.getDataSetByIndex(0);
            set.setValues(values);
            data.notifyDataChanged();
            line.notifyDataSetChanged();
        } else {
            set = new LineDataSet(values, "data");
            int color = ContextCompat.getColor(holder.getContext(), R.color.common_orange);
            mLineChartMarker.setPointColor(color);
            set.setColor(color);
            set.setHighLightColor(color);
            set.setLineWidth(2f);
            set.setDrawValues(false);
            set.setDrawCircles(false);
            set.setDrawHorizontalHighlightIndicator(false);
            set.setHighlightLineWidth(1f);
            set.enableDashedHighlightLine(mDashLength, mDashSpaceLength, 0);
            set.setLineContinuous(false);
            set.setLinePhase(1f);
            data = new LineData(set);
            line.setData(data);
        }
        line.highlightValue(lastX, 0);
        line.animateX(300);
    }

    @Override
    protected void showLoading(@NonNull BaseViewHolder<Model> holder, Model model, int position) {
        model.period = mPeriod;
        model.dataSet.clear();
        setupView(holder, model, position);
    }

    @Override
    protected void showError(@NonNull BaseViewHolder<Model> holder, Model model, int position) {
        model.period = mPeriod;
        model.dataSet.clear();
        setupView(holder, model, position);
    }

    private static class MarkerFormatter extends TimeMarkerFormatter {

        private int period;

        private MarkerFormatter(Context context) {
            super(context);
        }

        public void setPeriod(int period) {
            this.period = period;
        }

        @Override
        public CharSequence valueFormat(Context context, float value) {
            return Utils.formatFrequency(context, value, period, true);
        }
    }

    private static class XAxisLabelRenderer extends XAxisRenderer {

        private float[] labels;

        private XAxisLabelRenderer(BarLineChartBase chart) {
            super(chart.getViewPortHandler(), chart.getXAxis(), chart.getTransformer(YAxis.AxisDependency.LEFT));
        }

        public void setPeriod(int period) {
            if (period == Constants.TIME_PERIOD_WEEK) {
                labels = new float[]{1, 2, 3, 4, 5, 6, 7};
            } else {
                labels = new float[]{1, 2, 3, 4, 5};
            }
        }

        @Override
        protected void computeAxisValues(float min, float max) {
            if (labels == null) {
                super.computeAxisValues(min, max);
                return;
            }
            mAxis.mEntryCount = labels.length;
            mAxis.mEntries = labels;
            mAxis.setCenterAxisLabels(false);
            computeSize();
        }
    }

    private static class XAxisValueFormatter extends ValueFormatter {

        private String[] weekName;
        private String[] weekCountName;
        private int period = Constants.TIME_PERIOD_WEEK;

        private XAxisValueFormatter(Context context) {
            weekName = context.getResources().getStringArray(R.array.week_name);
            weekCountName = context.getResources().getStringArray(R.array.dashboard_week_list);
        }

        public void setPeriod(int period) {
            this.period = period;
        }

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            if (period == Constants.TIME_PERIOD_WEEK) {
                value = value == 7 ? 0 : value;
                return weekName[(int) value];
            } else {
                return weekCountName[(int) value - 1];
            }
        }
    }

    public static class Model extends BaseRefreshCard.BaseModel {
        private List<ChartEntry> dataSet = new ArrayList<>();
    }
}
