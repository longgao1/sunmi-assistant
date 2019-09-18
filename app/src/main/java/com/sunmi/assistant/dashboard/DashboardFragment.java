package com.sunmi.assistant.dashboard;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sunmi.assistant.R;
import com.sunmi.assistant.dashboard.card.BaseRefreshItem;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.refreshlayout.BGANormalRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import sunmi.common.base.BaseMvpFragment;
import sunmi.common.base.recycle.BaseArrayAdapter;
import sunmi.common.constant.CommonNotifications;
import sunmi.common.model.FilterItem;
import sunmi.common.notification.BaseNotification;
import sunmi.common.utils.SpUtils;
import sunmi.common.utils.StatusBarUtils;
import sunmi.common.utils.Utils;
import sunmi.common.view.DropdownMenu;

/**
 * 首页数据Dashboard的展示
 *
 * @author yinhui
 * @since 2019-06-13
 */
@EFragment(R.layout.dashboard_fragment_main)
public class DashboardFragment extends BaseMvpFragment<DashboardPresenter>
        implements DashboardContract.View, BGARefreshLayout.BGARefreshLayoutDelegate {

    @ViewById(R.id.cl_dashboard_content)
    ConstraintLayout mContent;
    @ViewById(R.id.layout_dashboard_refresh)
    BGARefreshLayout mRefreshLayout;
    @ViewById(R.id.rv_dashboard_list)
    RecyclerView mCardList;

    @ViewById(R.id.layout_shop_title)
    DropdownMenu mShopMenu;
    private LinearLayout mShopMenuList;
    @ViewById(R.id.view_dashboard_overlay)
    View mOverlay;

    @ViewById(R.id.layout_top_period_tab)
    ViewGroup mTopPeriodTab;
    @ViewById(R.id.tv_dashboard_top_today)
    TextView mTodayView;
    @ViewById(R.id.tv_dashboard_top_week)
    TextView mWeekViews;
    @ViewById(R.id.tv_dashboard_top_month)
    TextView mMonthViews;

    @ViewById(R.id.group_dashboard_content)
    Group mContentGroup;
    @ViewById(R.id.layout_dashboard_error)
    View mLayoutError;

    private int mDataSource;
    private BaseArrayAdapter<Object> mAdapter;
    private LinearLayoutManager mLayoutManager;

    private ShopMenuAdapter mShopMenuAdapter;
    private Drawable mShopMenuBg;
    private TextView mShopMenuTitle;
    private FilterItem mShopMenuItem;

    private int mStatusBarHeight;
    private int mTopShopMenuHeight;

    @AfterViews
    void init() {
        mPresenter = new DashboardPresenter();
        mPresenter.attachView(this);
        initView();
        showLoadingDialog();
        mPresenter.init();
    }

    private void initView() {
        FragmentActivity activity = getActivity();
        if (activity == null || activity.isDestroyed()) {
            return;
        }
        StatusBarUtils.setStatusBarFullTransparent(activity);
        initDimens();
        initShopMenu();
        initRefreshLayout();
        initRecycler();
    }

    private void initDimens() {
        mShopMenu.post(() -> {
            Context context = getContext();
            if (context == null) {
                return;
            }
            mStatusBarHeight = Utils.getStatusBarHeight(context);
            mTopShopMenuHeight = mShopMenu.getMeasuredHeight();
        });
    }

    private void initShopMenu() {
        Context context = getContext();
        if (context == null) {
            return;
        }
        mShopMenuBg = ContextCompat.getDrawable(context, R.drawable.dashboard_bg_top);
        mShopMenu.setLayoutManager(new ShopMenuLayoutManager(context));
        mShopMenu.setPopupHelper(new ShopMenuPopupHelper(context, mContent, mOverlay));
        mShopMenuAdapter = new ShopMenuAdapter(context);
        mShopMenuAdapter.setOnItemClickListener((adapter, model, position) -> {
            if (SpUtils.getShopId() != model.getId()) {
                mShopMenuItem.setChecked(false);
                mShopMenuItem = model;
                model.setChecked(true);
                List<FilterItem> shops = adapter.getData();
                shops.remove(position);
                shops.add(0, model);
                adapter.notifyDataSetChanged();
                SpUtils.setShopId(model.getId());
                SpUtils.setShopName(model.getItemName());
                BaseNotification.newInstance().postNotificationName(CommonNotifications.shopSwitched);
                resetTopView();
            }
        });
        mShopMenu.setAdapter(mShopMenuAdapter);
        mShopMenuTitle = mShopMenuAdapter.getTitle().getView(R.id.dropdown_item_title);
        mOverlay.setOnClickListener(v -> mShopMenu.getPopup().dismiss(true));
    }

    private void initRefreshLayout() {
        mRefreshLayout.setDelegate(this);
        BGANormalRefreshViewHolder refreshViewHolder =
                new BGANormalRefreshViewHolder(getContext(), false);
        View refreshHeaderView = refreshViewHolder.getRefreshHeaderView();
        mRefreshLayout.setRefreshViewHolder(refreshViewHolder);
        mRefreshLayout.setPullDownRefreshEnable(true);
        mRefreshLayout.setIsShowLoadingMoreView(false);
    }

    private void initRecycler() {
        RecyclerView.ItemAnimator animator = mCardList.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        mAdapter = new BaseArrayAdapter<>();
        mLayoutManager = new LinearLayoutManager(getContext());
        mCardList.setLayoutManager(mLayoutManager);
        mCardList.addOnScrollListener(new ItemStickyListener());
        mCardList.setAdapter(mAdapter);
    }

    private void resetTopView() {
        mShopMenu.setTranslationY(0);
        mShopMenu.setVisibility(View.VISIBLE);
        mTopPeriodTab.setVisibility(View.INVISIBLE);
        mCardList.scrollToPosition(0);
    }

    @Click(R.id.tv_dashboard_top_today)
    void clickPeriodToday() {
        mPresenter.switchPeriodTo(Constants.TIME_PERIOD_TODAY);
    }

    @Click(R.id.tv_dashboard_top_week)
    void clickPeriodWeek() {
        mPresenter.switchPeriodTo(Constants.TIME_PERIOD_WEEK);
    }

    @Click(R.id.tv_dashboard_top_month)
    void clickPeriodMonth() {
        mPresenter.switchPeriodTo(Constants.TIME_PERIOD_MONTH);
    }

    @Click(R.id.btn_refresh)
    void clickReload() {
        showLoadingDialog();
        mPresenter.init();
    }

    @Override
    public void updateTab(int period) {
        mTodayView.setSelected(period == Constants.TIME_PERIOD_TODAY);
        mWeekViews.setSelected(period == Constants.TIME_PERIOD_WEEK);
        mMonthViews.setSelected(period == Constants.TIME_PERIOD_MONTH);
    }

    @Override
    public void setShopList(List<FilterItem> list) {
        for (FilterItem item : list) {
            if (item.isChecked()) {
                mShopMenuItem = item;
                break;
            }
        }
        mShopMenuAdapter.setData(list);
    }

    @UiThread
    @Override
    public void setCards(List<BaseRefreshItem> data, int dataSource) {
        hideLoadingDialog();
        mContentGroup.setVisibility(View.VISIBLE);
        mLayoutError.setVisibility(View.GONE);
        mDataSource = dataSource;
        resetTopView();
        if (mAdapter == null && data == null) {
            return;
        }
        List<Object> list = new ArrayList<>(data.size());
        for (int i = 0, size = data.size(); i < size; i++) {
            BaseRefreshItem item = data.get(i);
            item.registerIntoAdapter(mAdapter, i);
            list.add(item.getModel());
        }
        mAdapter.setData(list);
    }

    @Override
    public void loadDataFailed() {
        hideLoadingDialog();
        mContentGroup.setVisibility(View.GONE);
        mLayoutError.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
        mPresenter.refresh();
        refreshLayout.postDelayed(refreshLayout::endRefreshing, 500);
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        return false;
    }

    @Override
    public int[] getStickNotificationId() {
        return new int[]{
                CommonNotifications.shopSwitched,
                CommonNotifications.shopNameChanged,
                CommonNotifications.companyNameChanged,
                CommonNotifications.companySwitch
        };
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == CommonNotifications.companySwitch
                || id == CommonNotifications.shopSwitched) {
            mPresenter.switchShopTo(SpUtils.getCompanyId(), SpUtils.getShopId());
        } else if (id == CommonNotifications.shopNameChanged
                || id == CommonNotifications.companyNameChanged) {
            mPresenter.refresh(0);
        }
    }

    private void showStickyTop(Context context, boolean animated) {
        StatusBarUtils.setStatusBarColor(getActivity(), StatusBarUtils.TYPE_DARK);
        if (mDataSource != 0) {
            mShopMenu.setTranslationY(-mTopShopMenuHeight);
            mShopMenu.setVisibility(View.INVISIBLE);
            mTopPeriodTab.setVisibility(View.VISIBLE);
        } else {
            mShopMenuTitle.setTextColor(ContextCompat.getColor(context, R.color.color_303540));
            mShopMenuTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0, 0, R.drawable.ic_arrow_drop_down_black_24dp, 0);
            mShopMenu.setBackgroundResource(R.drawable.dashboard_bg_white_with_divider);
        }
    }

    private void hideStickyTop(Context context, boolean animated) {
        StatusBarUtils.setStatusBarFullTransparent(getActivity());
        if (mDataSource != 0) {
            mShopMenu.setVisibility(View.VISIBLE);
            mTopPeriodTab.setVisibility(View.INVISIBLE);
        } else {
            mShopMenuTitle.setTextColor(0xFFFFFFFF);
            mShopMenuTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0, 0, R.drawable.ic_arrow_drop_down_white_24dp, 0);
            mShopMenu.setBackgroundResource(R.drawable.dashboard_bg_top);
        }
    }

    private class ItemStickyListener extends RecyclerView.OnScrollListener {

        private boolean mIsStickyTop = false;
        private View topBar = null;

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            if (recyclerView.getChildCount() == 0) {
                return;
            }
            int position = -1;
            if (topBar == null) {
                topBar = recyclerView.getChildAt(0);
            }
            if (topBar != null) {
                int[] coordinate = new int[2];
                topBar.getLocationInWindow(coordinate);
                position = coordinate[1];
            }

            Context context = recyclerView.getContext();
            if (mIsStickyTop && position > 0) {
                mIsStickyTop = false;
                hideStickyTop(context, true);
            } else if (!mIsStickyTop && position <= 0) {
                mIsStickyTop = true;
                showStickyTop(context, true);
            }
            if (position > 0 && mDataSource != 0) {
                mShopMenu.setTranslationY(Math.min(position - mTopShopMenuHeight, 0));
            }
        }
    }

    private static class ShopMenuLayoutManager extends LinearLayoutManager {

        private ShopMenuLayoutManager(Context context) {
            super(context);
        }

        @Override
        public void onMeasure(@NonNull RecyclerView.Recycler recycler, @NonNull RecyclerView.State state, int widthSpec, int heightSpec) {
            if (getChildCount() == 0) {
                super.onMeasure(recycler, state, widthSpec, heightSpec);
                return;
            }
            View firstChildView = recycler.getViewForPosition(0);
            measureChild(firstChildView, widthSpec, heightSpec);
            int itemHeight = firstChildView.getMeasuredHeight();
            setMeasuredDimension(View.MeasureSpec.getSize(widthSpec),
                    getChildCount() > 8 ? (int) (itemHeight * 8.5f) : itemHeight * getChildCount());
        }
    }
}
