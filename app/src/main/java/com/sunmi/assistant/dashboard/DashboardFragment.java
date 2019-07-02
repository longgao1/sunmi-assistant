package com.sunmi.assistant.dashboard;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sunmi.apmanager.constant.NotificationConstant;
import com.sunmi.assistant.R;
import com.sunmi.assistant.dashboard.card.BaseRefreshCard;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import sunmi.common.base.BaseMvpFragment;
import sunmi.common.base.recycle.BaseArrayAdapter;
import sunmi.common.utils.CommonHelper;

/**
 * 首页数据Dashboard的展示
 *
 * @author yinhui
 * @since 2019-06-13
 */
@EFragment(R.layout.dashboard_fragment_main)
public class DashboardFragment extends BaseMvpFragment<DashboardPresenter>
        implements DashboardContract.View {

    @ViewById(R.id.srl_dashboard_refresh)
    SwipeRefreshLayout mRefreshLayout;
    @ViewById(R.id.rv_dashboard_card_list)
    RecyclerView mCardList;
    @ViewById(R.id.layout_dashboard_sticky_tab)
    ViewGroup mStickyTab;
    @ViewById(R.id.tv_dashboard_today)
    TextView mTabToday;
    @ViewById(R.id.tv_dashboard_week)
    TextView mTabWeek;
    @ViewById(R.id.tv_dashboard_month)
    TextView mTabMonth;


    private BaseArrayAdapter<Object> mAdapter;
    private GridLayoutManager mLayoutManager;

    @AfterViews
    void init() {
        mPresenter = new DashboardPresenter();
        mPresenter.attachView(this);
        initView();
        initAdapter();
        mPresenter.loadConfig();
        mPresenter.switchPeriodTo(DashboardContract.TIME_PERIOD_TODAY);
        updateStickyTab(DashboardContract.TIME_PERIOD_TODAY);
    }

    private void initView() {
        RecyclerView.ItemAnimator animator = mCardList.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        mRefreshLayout.setOnRefreshListener(() -> {
            mPresenter.refresh();
            mRefreshLayout.setRefreshing(false);
        });
    }

    private void initAdapter() {
//        TitleType titleType = new TitleType();
//        TabType tabType = new TabType();
//        tabType.addOnViewClickListener(R.id.tv_dashboard_today,
//                (adapter, holder, v, model, position) -> {
//                    model.timeSpan = DashboardContract.TIME_PERIOD_TODAY;
//                    adapter.notifyItemChanged(position);
//                    mPresenter.switchPeriodTo(DashboardContract.TIME_PERIOD_TODAY);
//                    updateStickyTab(model.timeSpan);
//                });
//        tabType.addOnViewClickListener(R.id.tv_dashboard_week,
//                (adapter, holder, v, model, position) -> {
//                    model.timeSpan = DashboardContract.TIME_PERIOD_WEEK;
//                    adapter.notifyItemChanged(position);
//                    mPresenter.switchPeriodTo(DashboardContract.TIME_PERIOD_WEEK);
//                    updateStickyTab(model.timeSpan);
//                });
//        tabType.addOnViewClickListener(R.id.tv_dashboard_month,
//                (adapter, holder, v, model, position) -> {
//                    model.timeSpan = DashboardContract.TIME_PERIOD_MONTH;
//                    adapter.notifyItemChanged(position);
//                    mPresenter.switchPeriodTo(DashboardContract.TIME_PERIOD_MONTH);
//                    updateStickyTab(model.timeSpan);
//                });
//        DataCardType dataCardType = new DataCardType();
//        dataCardType.setOnItemClickListener(new OnItemClickListener<DataCard>() {
//            @Override
//            public void onClick(BaseRecyclerAdapter<DataCard> adapter, BaseViewHolder<DataCard> holder, DataCard model, int position) {
//                OrderListActivity_.intent(DashboardFragment.this)
//                        .mTimeStart(model.timeSpanPair.first)
//                        .mTimeEnd(model.timeSpanPair.second)
//                        .start();
//            }
//        });
//        BarChartCardType barChartCardType = new BarChartCardType();
//        barChartCardType.addOnViewClickListener(R.id.tv_dashboard_radio_by_sales,
//                (adapter, holder, v, model, position) -> {
//                    model.dataSource = DashboardContract.DATA_MODE_SALES;
//                    adapter.notifyItemChanged(position);
//                });
//        barChartCardType.addOnViewClickListener(R.id.tv_dashboard_radio_by_order,
//                (adapter, holder, v, model, position) -> {
//                    model.dataSource = DashboardContract.DATA_MODE_ORDER;
//                    adapter.notifyItemChanged(position);
//                });
//        PieChartCardType pieChartCardType = new PieChartCardType();
//        pieChartCardType.addOnViewClickListener(R.id.tv_dashboard_radio_by_sales,
//                (adapter, holder, v, model, position) -> {
//                    model.dataSource = DashboardContract.DATA_MODE_SALES;
//                    adapter.notifyItemChanged(position);
//                });
//        pieChartCardType.addOnViewClickListener(R.id.tv_dashboard_radio_by_order,
//                (adapter, holder, v, model, position) -> {
//                    model.dataSource = DashboardContract.DATA_MODE_ORDER;
//                    adapter.notifyItemChanged(position);
//                });
//        ListCardType listCardType = new ListCardType();

        mAdapter = new BaseArrayAdapter<>();
        mLayoutManager = new GridLayoutManager(getContext(), 2);
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return mAdapter.getItemType(position).getSpanSize();
            }
        });
        mCardList.setLayoutManager(mLayoutManager);
        mCardList.addOnScrollListener(new ItemStickyListener());
        mCardList.addItemDecoration(new ItemSpaceDecoration());
        mCardList.setAdapter(mAdapter);
    }

    @Click(R.id.tv_dashboard_today)
    void clickPeriodToday() {
        mPresenter.switchPeriodTo(DashboardContract.TIME_PERIOD_TODAY);
        updateStickyTab(DashboardContract.TIME_PERIOD_TODAY);
        mAdapter.notifyItemChanged(1);
    }

    @Click(R.id.tv_dashboard_week)
    void clickPeriodWeek() {
        mPresenter.switchPeriodTo(DashboardContract.TIME_PERIOD_WEEK);
        updateStickyTab(DashboardContract.TIME_PERIOD_WEEK);
        mAdapter.notifyItemChanged(1);
    }

    @Click(R.id.tv_dashboard_month)
    void clickPeriodMonth() {
        mPresenter.switchPeriodTo(DashboardContract.TIME_PERIOD_MONTH);
        updateStickyTab(DashboardContract.TIME_PERIOD_MONTH);
        mAdapter.notifyItemChanged(1);
    }

    private void updateStickyTab(int period) {
        mTabToday.setSelected(period == DashboardContract.TIME_PERIOD_TODAY);
        mTabWeek.setSelected(period == DashboardContract.TIME_PERIOD_WEEK);
        mTabMonth.setSelected(period == DashboardContract.TIME_PERIOD_MONTH);
    }

    @UiThread
    @Override
    public void initData(List<BaseRefreshCard> data) {
        if (mAdapter == null && data == null) {
            return;
        }
        List<Object> list = new ArrayList<>(data.size());
        for (BaseRefreshCard item : data) {
            item.registerIntoAdapter(mAdapter);
            list.add(item.getModel());
        }
        mAdapter.setData(list);
    }

    @Override
    public int[] getStickNotificationId() {
        return new int[]{NotificationConstant.shopSwitched};
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationConstant.shopSwitched) {
            mPresenter.switchShopTo((int) args[0]);
        }
    }

    private class ItemStickyListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (mLayoutManager.findFirstVisibleItemPosition() > 0) {
                mStickyTab.setVisibility(View.VISIBLE);
            } else {
                mStickyTab.setVisibility(View.INVISIBLE);
            }
        }
    }

    private class ItemSpaceDecoration extends RecyclerView.ItemDecoration {

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            if (getContext() == null || position < 2) {
                super.getItemOffsets(outRect, view, parent, state);
                return;
            }
            int space = CommonHelper.dp2px(getContext(), 10.0f);
            int spanSize = mAdapter.getItemType(position).getSpanSize();
            if (spanSize == 2) {
                outRect.left = space;
                outRect.right = space;
            } else {
                int posPoint = position - 1;
                boolean isFirst = true;
                while (posPoint >= 2) {
                    if (mAdapter.getItemType(posPoint).getSpanSize() == 1) {
                        isFirst = !isFirst;
                    } else {
                        break;
                    }
                    posPoint--;
                }
                if (isFirst) {
                    outRect.left = space;
                    outRect.right = space / 2;
                } else {
                    outRect.left = space / 2;
                    outRect.right = space;
                }
            }
            outRect.top = space;
            outRect.bottom = position == mAdapter.getData().size() - 1 ? space : 0;
        }
    }

}
