package com.sunmi.assistant.dashboard.ui.chart;

import android.content.Context;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.sunmi.assistant.R;
import com.sunmi.assistant.dashboard.util.Constants;

/**
 * Description:
 *
 * @author linyuanpeng on 2020-01-16.
 */
public class XAxisFrequencyDistributionFormatter extends ValueFormatter {

    private Context context;
    private int max;

    public XAxisFrequencyDistributionFormatter(Context context) {
        this.context = context;
    }

    public void setPeriod(int period) {
        if (period == Constants.TIME_PERIOD_DAY) {
            max = 4;
        } else {
            max = 10;
        }
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        if (value > max) {
            return context.getString(R.string.dashboard_unit_frequency_count_above_abbr, max);
        } else {
            return String.valueOf((int) value);
        }
    }
}
