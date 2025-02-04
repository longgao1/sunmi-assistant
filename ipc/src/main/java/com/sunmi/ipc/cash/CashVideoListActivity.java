package com.sunmi.ipc.cash;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.sunmi.ipc.R;
import com.sunmi.ipc.cash.adapter.CashDropdownTimeAdapter;
import com.sunmi.ipc.cash.adapter.CashVideoAdapter;
import com.sunmi.ipc.cash.model.CashVideo;
import com.sunmi.ipc.config.IpcConstants;
import com.sunmi.ipc.contract.CashVideoListConstract;
import com.sunmi.ipc.model.DropdownTime;
import com.sunmi.ipc.presenter.CashVideoListPresenter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import cn.bingoogolapple.refreshlayout.BGANormalRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import cn.bingoogolapple.refreshlayout.BGARefreshViewHolder;
import sunmi.common.base.BaseMvpActivity;
import sunmi.common.constant.CommonNotifications;
import sunmi.common.model.CashServiceInfo;
import sunmi.common.model.FilterItem;
import sunmi.common.utils.DateTimeUtils;
import sunmi.common.utils.NetworkUtils;
import sunmi.common.utils.StatusBarUtils;
import sunmi.common.view.DropdownAdapterNew;
import sunmi.common.view.DropdownAnimNew;
import sunmi.common.view.DropdownMenuNew;
import sunmi.common.view.TitleBarView;
import sunmi.common.view.loopview.LoopView;

/**
 * Description:
 *
 * @author linyuanpeng on 2019-12-04.
 */

@EActivity(resName = "activity_cash_video_list")
public class CashVideoListActivity extends BaseMvpActivity<CashVideoListPresenter>
        implements CashVideoListConstract.View, BGARefreshLayout.BGARefreshLayoutDelegate {

    private static final int MINUTE_PER_HOUR = 60;
    private static final int HOUR_PER_DAY = 24;
    private static final long MILLISECONDS_PER_DAY = 3600 * 24 * 1000;

    @ViewById(resName = "tv_date")
    TextView tvDate;
    @ViewById(resName = "bga_refresh")
    BGARefreshLayout refreshLayout;
    @ViewById(resName = "rv_cash_video")
    RecyclerView rvCashVideo;
    @ViewById(resName = "layout_network_error")
    View networkError;
    @ViewById(resName = "dm_device")
    DropdownMenuNew dmDevice;
    @ViewById(resName = "dm_time")
    DropdownMenuNew dmTime;
    @ViewById(resName = "tv_abnormal")
    TextView tvAbnormal;
    @ViewById(resName = "tv_no_cash")
    TextView tvNoCash;
    @ViewById(resName = "title_bar")
    TitleBarView titleBar;
    @ViewById(resName = "tv_fast_play")
    TextView tvFastPlay;
    @ViewById(resName = "tv_suggestion")
    TextView tvSuggestion;

    @Extra
    int deviceId = -1;
    @Extra
    long startTime;
    @Extra
    long endTime;
    @Extra
    int videoType;
    @Extra
    ArrayList<FilterItem> items;
    @Extra
    boolean isSingleDevice;
    @Extra
    int total;
    @Extra
    boolean isAbnormalBehavior;
    @Extra
    HashMap<Integer, CashServiceInfo> cashServiceMap;

    private final int REQUEST = 0x101;

    private ArrayList<CashVideo> dataList = new ArrayList<>();
    private boolean hasMore;
    private CashVideoAdapter adapter;

    private DropdownAdapterNew filterDeviceAdapter;
    private CashDropdownTimeAdapter filterTimeAdapter;

    private int selectIndex = 0;
    private DropdownTime select;
    private long fastPlayStart;
    private long fastPlayEnd;
    private int pageNum;
    private int pageSize = 10;

    @AfterViews
    void init() {
        StatusBarUtils.setStatusBarFullTransparent(this);
        titleBar.getLeftLayout().setOnClickListener(v -> onBackPressed());
        mPresenter = new CashVideoListPresenter(isAbnormalBehavior, cashServiceMap);
        mPresenter.attachView(this);
        fastPlayStart = startTime;
        fastPlayEnd = endTime;
        if (isSingleDevice) {
            dmDevice.setVisibility(View.GONE);
        }
        if (isAbnormalBehavior) {
            titleBar.setAppTitle(R.string.str_abnormal_behavior_list);
            tvFastPlay.setVisibility(View.GONE);
            tvSuggestion.setText(R.string.tip_other_anomalies);
            tvAbnormal.setVisibility(View.GONE);
        }
        tvDate.setText(DateTimeUtils.secondToDate(startTime, "yyyy.MM.dd"));
        refreshLayout.setDelegate(this);
        BGARefreshViewHolder viewHolder = new BGANormalRefreshViewHolder(context, true);
        viewHolder.setLoadingMoreText(getString(R.string.str_loding_more));
        viewHolder.setLoadMoreBackgroundColorRes(R.color.bg_common);
        // 设置下拉刷新和上拉加载更多的风格(参数1：应用程序上下文，参数2：是否具有上拉加载更多功能)
        refreshLayout.setRefreshViewHolder(viewHolder);
        showLoadingDialog();
        initFilter();
        refreshList();
    }

    private void initFilter() {
        int gap = (int) getResources().getDimension(R.dimen.dp_6);
        filterDeviceAdapter = new DropdownAdapterNew(this);
        filterDeviceAdapter.setOnItemClickListener((adapter, model, position) -> {
            if (deviceId != model.getId()) {
                deviceId = model.getId();
                refreshList();
            }
        });
        dmDevice.setAnim(new DropdownAnimNew(dmTime));
        dmDevice.setAdapter(filterDeviceAdapter);

        filterTimeAdapter = new CashDropdownTimeAdapter(this);
        filterTimeAdapter.setOnItemClickListener((adapter, model, position) -> {
            if (select != null && select.isCustom() && !model.isCustom()) {
                // 如果上次选择是自定义时间，切换到给定时间段时，清空自定义时间的时间信息
                select.setTime(-1, -1);
            }
            select = model;
            if (!model.isCustom()) {
                selectIndex = position;
                dmTime.dismiss(true);
                reloadForTimeChange(model);
            }
        });

        dmTime.setAnim(new DropdownTimeAnim(dmDevice));
        dmTime.setLayoutManager(new GridLayoutManager(this, 3));
        dmTime.setDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.set(gap, gap, gap, gap);
            }
        });
        dmTime.setAdapter(filterTimeAdapter);
        dmTime.getContent().findViewById(R.id.tv_ok).setOnClickListener(v -> {
            int state = filterTimeAdapter.checkAndUpdateTime(select, startTime * 1000);
            if (state == CashDropdownTimeAdapter.STATE_SUCCESS) {
                selectIndex = -1;
                dmTime.dismiss(true);
                reloadForTimeChange(select);
            } else {
                if (state == CashDropdownTimeAdapter.STATE_RANGE_ERROR) {
                    shortTip(R.string.cash_time_range_error_tip);
                } else {
                    shortTip(R.string.cash_time_empty_error_tip);
                }
            }
        });
        tvAbnormal.setSelected(videoType == IpcConstants.CASH_VIDEO_ABNORMAL);
        tvAbnormal.setOnClickListener(v -> {
            dmDevice.dismiss(true);
            dmTime.dismiss(true);
            tvAbnormal.setSelected(!tvAbnormal.isSelected());
            videoType = tvAbnormal.isSelected() ?
                    IpcConstants.CASH_VIDEO_ABNORMAL : IpcConstants.CASH_VIDEO_ALL;
            refreshList();
        });

        initTimeWheel();
        initFilterData();
    }

    private void reloadForTimeChange(DropdownTime model) {
        long newStart = model.getTimeStart() / 1000;
        long newEnd = model.getTimeEnd() / 1000;
        if (startTime != newStart || endTime != newEnd) {
            startTime = newStart;
            endTime = newEnd;
            refreshList();
        }
    }

    private void initFilterData() {
        // 设置设备筛选数据
        filterDeviceAdapter.setData(items);

        // 设置时间筛选数据
        String customName = getString(R.string.ipc_setting_detection_time_custom);
        String customTimeAll = getString(R.string.cash_time_all);

        // 创建给定时间段信息集
        List<Pair<Integer, Integer>> info = new ArrayList<>(4);
        info.add(new Pair<>(9, 12));
        info.add(new Pair<>(12, 15));
        info.add(new Pair<>(15, 18));
        info.add(new Pair<>(18, 21));

        // 根据信息集生成DropdownTime筛选数据model
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(startTime * 1000);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int date = c.get(Calendar.DATE);
        c.clear();
        c.set(year, month, date);
        long today = c.getTimeInMillis();

        List<DropdownTime> time = new ArrayList<>();
        DropdownTime all = new DropdownTime(-1, customTimeAll, true, false);
        DropdownTime custom = new DropdownTime(100, customName, false, true);
        all.setTime(today, today + MILLISECONDS_PER_DAY);
        time.add(all);
        for (Pair<Integer, Integer> item : info) {
            String name = String.format(Locale.getDefault(),
                    "%02d:00~%02d:00", item.first, item.second);
            c.set(year, month, date, item.first, 0);
            long start = c.getTimeInMillis();
            c.set(year, month, date, item.second, 0);
            long end = c.getTimeInMillis();
            time.add(new DropdownTime(item.first, name, start, end));
        }
        time.add(custom);
        filterTimeAdapter.setData(time);
    }

    private void initTimeWheel() {
        String hourText = getString(R.string.text_hour);
        String minuteText = getString(R.string.text_minute);
        LoopView lvWheelHour = dmTime.getContent().findViewById(R.id.lv_time_hour);
        LoopView lvWheelMinute = dmTime.getContent().findViewById(R.id.lv_time_minute);

        ArrayList<String> hours = new ArrayList<>();
        ArrayList<String> minutes = new ArrayList<>();
        for (int i = 0; i < HOUR_PER_DAY; i++) {
            hours.add(String.format(Locale.getDefault(),
                    "%02d %s", i, hourText));
        }
        for (int i = 0; i < MINUTE_PER_HOUR; i++) {
            minutes.add(String.format(Locale.getDefault(),
                    "%02d %s", i, minuteText));
        }

        // 设置是否循环播放
        lvWheelHour.setNotLoop();
        lvWheelMinute.setNotLoop();

        // 设置原始数据
        lvWheelHour.setItems(hours);
        lvWheelMinute.setItems(minutes);
        // 设置初始位置
        lvWheelHour.setInitPosition(0);
        lvWheelMinute.setInitPosition(0);
    }

    @Click(resName = "tv_fast_play")
    void fastPlayClick() {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            shortTip(R.string.network_error);
            return;
        }
        if (total <= 0) {
            shortTip(R.string.str_no_cash_video);
            return;
        }
        CashPlayActivity_.intent(context)
                .isWholeDayVideoPlay(true)
                .deviceId(deviceId)
                .startTime(fastPlayStart)
                .endTime(fastPlayEnd)
                .serviceInfoMap(cashServiceMap)
                .start();
    }

    @Click(resName = "btn_refresh")
    public void refreshClick() {
        showLoadingDialog();
        refreshList();
    }

    @Override
    public void getCashVideoSuccess(List<CashVideo> beans, int total) {
        networkError.setVisibility(View.GONE);
        if (total <= 0) {
            tvNoCash.setVisibility(View.VISIBLE);
            addData(beans, pageNum == 1);
        } else {
            tvNoCash.setVisibility(View.GONE);
            if (pageNum == 1 || total > dataList.size()) {
                addData(beans, pageNum == 1);
                if (total <= (pageNum - 1) * pageSize + beans.size()) {
                    hasMore = false;
                }
            } else {
                hasMore = false;
            }
        }

    }

    @Override
    public void netWorkError() {
        if (dataList.size() <= 0) {
            networkError.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void endRefresh() {
        refreshLayout.endLoadingMore();
        refreshLayout.endRefreshing();
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
        refreshList();
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        if (NetworkUtils.isNetworkAvailable(context) && hasMore) {
            pageNum++;
            mPresenter.loadMore(pageNum, pageSize);
            return true;
        }
        return false;
    }

    @UiThread
    protected void addData(List<CashVideo> beans, boolean isRefresh) {
        initAdapter();
        if (isRefresh) {
            dataList.clear();
        }
        dataList.addAll(beans);
        adapter.notifyDataSetChanged();

    }

    private void refreshList() {
        pageNum = 1;
        hasMore = true;
        mPresenter.load(deviceId, videoType, startTime, endTime, pageNum, pageSize);
    }

    private void initAdapter() {
        if (adapter == null) {
            adapter = new CashVideoAdapter(context, dataList, cashServiceMap, isAbnormalBehavior);
            rvCashVideo.setLayoutManager(new LinearLayoutManager(context));
            adapter.setOnItemClickListener((data, pos) -> {
                if (!NetworkUtils.isNetworkAvailable(context)) {
                    shortTip(R.string.network_error);
                    return;
                }
                CashPlayActivity_.intent(context)
                        .isWholeDayVideoPlay(false)
                        .deviceId(deviceId)
                        .startTime(startTime)
                        .endTime(endTime)
                        .serviceInfoMap(cashServiceMap)
                        .videoList(data)
                        .hasMore(hasMore).pageNum(pageNum).videoListPosition(pos)
                        .videoType(videoType).isAbnormalBehavior(isAbnormalBehavior).startForResult(REQUEST);
            });
            rvCashVideo.setAdapter(adapter);
        }
    }

    @Override
    public int[] getStickNotificationId() {
        return new int[]{CommonNotifications.cashPreventSubscribe};
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == CommonNotifications.cashPreventSubscribe) {
            if (args.length <= 0 || !(args[0] instanceof Set)) {
                return;
            }
            @SuppressWarnings("unchecked")
            Set<String> snSet = (Set<String>) args[0];
            for (Map.Entry<Integer, CashServiceInfo> entry : cashServiceMap.entrySet()) {
                CashServiceInfo info = entry.getValue();
                if (snSet.contains(info.getDeviceSn())) {
                    info.setHasCashLossPrevention(true);
                }
            }
        }
    }

    private class DropdownTimeAnim extends DropdownAnimNew {

        public DropdownTimeAnim(DropdownMenuNew... menus) {
            super(menus);
        }

        @Override
        public void onPostDismiss(DropdownMenuNew.ViewHolder titleHolder, View menu, View overlay) {
            super.onPostDismiss(titleHolder, menu, overlay);
            if (selectIndex >= 0) {
                filterTimeAdapter.setSelected(selectIndex);
            }
        }

    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK);
        finish();
    }

    @SuppressWarnings("unchecked")
    @OnActivityResult(REQUEST)
    void onResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                ArrayList<CashVideo> list = bundle.getParcelableArrayList("videoList");
                if (list != null) {
                    dataList.clear();
                    dataList.addAll(list);
                    adapter.setSelectPosition(bundle.getInt("videoListPosition"));
                    hasMore = true;
                    pageNum = list.size() / pageNum + 1;
                }
            }
        }
    }

}
