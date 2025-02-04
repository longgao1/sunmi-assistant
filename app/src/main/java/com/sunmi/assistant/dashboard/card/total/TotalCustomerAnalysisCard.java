package com.sunmi.assistant.dashboard.card.total;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.Group;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.sunmi.assistant.R;
import com.sunmi.assistant.dashboard.card.BaseRefreshCard;
import com.sunmi.assistant.dashboard.data.DashboardCondition;
import com.sunmi.assistant.dashboard.util.Constants;
import com.sunmi.assistant.dashboard.util.Utils;
import com.sunmi.ipc.face.model.FaceAge;
import com.sunmi.ipc.model.FaceAgeRangeResp;
import com.sunmi.ipc.rpc.IpcCloudApi;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import sunmi.common.base.adapter.CommonAdapter;
import sunmi.common.base.adapter.ViewHolder;
import sunmi.common.base.recycle.BaseViewHolder;
import sunmi.common.base.recycle.ItemType;
import sunmi.common.model.CountListBean;
import sunmi.common.model.CustomerDistributionResp;
import sunmi.common.model.Interval;
import sunmi.common.rpc.cloud.SunmiStoreApi;
import sunmi.common.rpc.retrofit.BaseResponse;
import sunmi.common.rpc.retrofit.RetrofitCallback;
import sunmi.common.utils.CacheManager;

/**
 * @author yinhui
 * @since 2019-07-01
 */
public class TotalCustomerAnalysisCard extends BaseRefreshCard<TotalCustomerAnalysisCard.Model, CustomerDistributionResp> {

    private static final int MAX_ITEM_COUNT = 3;

    private static TotalCustomerAnalysisCard sInstance;

    private String mAgeLabel;
    private String mMaleLabel;
    private String mFemaleLabel;

    private SparseArray<FaceAge> mAgeList;
    private CommonAdapter<Item> mAdapter;

    private TotalCustomerAnalysisCard(Presenter presenter, DashboardCondition condition, int period, Interval periodTime) {
        super(presenter, condition, period, periodTime);
    }

    public static TotalCustomerAnalysisCard get(Presenter presenter, DashboardCondition condition, int period, Interval periodTime) {
        if (sInstance == null) {
            sInstance = new TotalCustomerAnalysisCard(presenter, condition, period, periodTime);
        } else {
            sInstance.mPresenter = presenter;
            sInstance.reset(presenter, condition, period, periodTime);
        }
        return sInstance;
    }

    @Override
    public void init(Context context) {
        mAgeLabel = context.getString(R.string.dashboard_unit_age);
        mMaleLabel = context.getString(R.string.str_gender_male);
        mFemaleLabel = context.getString(R.string.str_gender_female);
    }

    @Override
    public int getLayoutId(int type) {
        return R.layout.dashboard_item_total_customer_analysis;
    }

    @Override
    protected Call<BaseResponse<CustomerDistributionResp>> load(int companyId, int shopId, int period, Interval periodTime,
                                                                CardCallback callback) {
        if (mAgeList == null) {
            loadAgeList(companyId, period, periodTime, callback);
        } else {
            loadDetail(companyId, period, periodTime, callback);
        }
        return null;
    }

    private void loadAgeList(int companyId, int period, Interval periodTime, CardCallback callback) {
        mAgeList = CacheManager.get().get(CacheManager.CACHE_AGE_NAME);
        if (mAgeList != null) {
            loadDetail(companyId, period, periodTime, callback);
            return;
        }
        IpcCloudApi.getInstance().getFaceAgeRange(new RetrofitCallback<FaceAgeRangeResp>() {
            @Override
            public void onSuccess(int code, String msg, FaceAgeRangeResp data) {
                if (data == null || data.getAgeRangeList() == null) {
                    onFail(code, msg, data);
                    return;
                }
                List<FaceAge> list = data.getAgeRangeList();
                mAgeList = new SparseArray<>(list.size());
                int size = 4;
                for (FaceAge age : list) {
                    mAgeList.put(age.getCode(), age);
                    size += age.getName().length() * 2 + 8;
                }
                CacheManager.get().put(CacheManager.CACHE_AGE_NAME, mAgeList, size);
                loadDetail(companyId, period, periodTime, callback);
            }

            @Override
            public void onFail(int code, String msg, FaceAgeRangeResp data) {
                callback.onFail(code, msg, null);
            }
        });
    }

    private void loadDetail(int companyId, int period, Interval periodTime, CardCallback callback) {
        String time = Utils.formatTime(Utils.FORMAT_API_DATE, periodTime.start);
        SunmiStoreApi.getInstance().getTotalCustomerGenderDistribution(companyId, time, period,
                new RetrofitCallback<CustomerDistributionResp>() {
                    @Override
                    public void onSuccess(int code, String msg, CustomerDistributionResp data) {
                        callback.onSuccess(code, msg, data);
                    }

                    @Override
                    public void onFail(int code, String msg, CustomerDistributionResp data) {
                        if (code == Constants.NO_CUSTOMER_DATA) {
                            callback.onSuccess(code, msg, data);
                        } else {
                            callback.onFail(code, msg, data);
                        }
                    }
                });
    }

    @NonNull
    @Override
    public BaseViewHolder<Model> onCreateViewHolder(@NonNull View view, @NonNull ItemType<Model, BaseViewHolder<Model>> type) {
        BaseViewHolder<Model> holder = super.onCreateViewHolder(view, type);
        Context context = view.getContext();
        ListView lv = holder.getView(R.id.lv_dashboard_list);
        mAdapter = new DetailListAdapter(context);
        lv.setDividerHeight(0);
        lv.setAdapter(mAdapter);
        return holder;
    }

    @Override
    protected Model createModel() {
        return new Model();
    }

    @Override
    protected void setupModel(Model models, CustomerDistributionResp response) {
        Model model = getModel();
        model.list.clear();
        if (response != null && response.getCountList() != null && !response.getCountList().isEmpty()) {
            List<CountListBean> list = response.getCountList();
            List<Item> result = new ArrayList<>();
            int total = 0;
            for (CountListBean item : list) {
                if (item.getUniqCount() == 0) {
                    continue;
                }
                String ageName = mAgeList.get(item.getAgeRangeCode()).getName();
                String name;
                int count = Math.max(0, item.getUniqCount());
                int oldCount = Math.max(0, item.getRegularUniqCount());
                if (item.getGender() == Constants.GENDER_MALE) {
                    name = String.format("%s  |  %s%s", mMaleLabel, ageName, mAgeLabel);
                } else {
                    name = String.format("%s  |  %s%s", mFemaleLabel, ageName, mAgeLabel);
                }
                result.add(new Item(item.getAgeRangeCode(), item.getGender(), name, count, oldCount));
                total += count;
            }
            Collections.sort(result, (o1, o2) -> o2.count - o1.count);
            if (result.size() > MAX_ITEM_COUNT) {
                model.list.addAll(result.subList(0, 3));
            } else {
                model.list.addAll(result);
            }
            for (Item item : model.list) {
                item.setTotal(total);
            }
        }

        if (model.list.isEmpty()) {
            Item e = new Item();
            e.setError();
            model.list.add(e);
        }

    }

    @Override
    protected void setupView(@NonNull BaseViewHolder<Model> holder, Model model, int position) {
        Context context = holder.getContext();
        View view = holder.itemView;
        if (mCondition.hasFloating) {
            int paddingBottom = (int) context.getResources().getDimension(R.dimen.dp_80);
            view.setPaddingRelative(0, 0, 0, paddingBottom);
        } else {
            int paddingBottom = (int) context.getResources().getDimension(R.dimen.dp_32);
            view.setPaddingRelative(0, 0, 0, paddingBottom);
        }
        mAdapter.setDatas(model.list);
        mAdapter.notifyDataSetChanged();
        view.post(() -> {
            mAdapter.notifyDataSetChanged();
            holder.getView(R.id.lv_dashboard_list).requestLayout();
        });
    }

    @Override
    protected void showLoading(@NonNull BaseViewHolder<Model> holder, Model model, int position) {
        if (model.list.isEmpty()) {
            model.list.add(new Item());
        }
        for (Item item : model.list) {
            item.setLoading();
        }
        setupView(holder, model, position);
    }

    @Override
    protected void showError(@NonNull BaseViewHolder<Model> holder, Model model, int position) {
        if (model.list.isEmpty()) {
            model.list.add(new Item());
        }
        for (Item item : model.list) {
            item.setError();
        }
        setupView(holder, model, position);
    }

    private static class DetailListAdapter extends CommonAdapter<Item> {

        private DetailListAdapter(Context context) {
            super(context, R.layout.dashboard_item_total_customer_analysis_item);
        }

        @Override
        public void convert(ViewHolder holder, Item item) {
            if (item.state == Item.STATE_LOADING) {
                showLoading(holder);
                return;
            }
            showContent(holder);
            ImageView ivAvatar = holder.getView(R.id.iv_dashboard_avatar);
            TextView tvTitle = holder.getView(R.id.tv_dashboard_title);
            TextView tvCount = holder.getView(R.id.tv_dashboard_count);
            TextView tvRatio = holder.getView(R.id.tv_dashboard_ratio);
            TextView tvOldRatio = holder.getView(R.id.tv_dashboard_old_ratio);
            if (item.state == Item.STATE_ERROR) {
                ivAvatar.setImageResource(R.mipmap.dashboard_customer_avatar_error);
                tvTitle.setText(R.string.dashboard_tip_customer_none);
                tvCount.setText(Utils.DATA_ZERO);
                tvRatio.setText(Utils.DATA_ZERO_RATIO);
                tvOldRatio.setText(Utils.DATA_ZERO_RATIO);
            } else {
                float ratio = item.total > 0 ? (float) item.count / item.total : 0f;
                float oldRatio = item.count > 0 ? (float) item.oldCount / item.count : 0f;

                ivAvatar.setImageResource(item.gender == 1 ?
                        R.mipmap.dashboard_customer_avatar_male : R.mipmap.dashboard_customer_avatar_female);
                tvTitle.setText(item.name);
                tvCount.setText(Utils.formatNumber(mContext, item.count, false, true));
                tvRatio.setText(Utils.formatPercent(ratio, false, true));
                tvOldRatio.setText(Utils.formatPercent(oldRatio, false, true));
            }
        }

        private void showContent(ViewHolder holder) {
            Group content = holder.getView(R.id.group_dashboard_content);
            Group skeleton = holder.getView(R.id.group_dashboard_skeleton);
            content.setVisibility(View.VISIBLE);
            skeleton.setVisibility(View.INVISIBLE);
        }

        private void showLoading(ViewHolder holder) {
            ImageView avatar = holder.getView(R.id.iv_dashboard_avatar);
            Group content = holder.getView(R.id.group_dashboard_content);
            Group skeleton = holder.getView(R.id.group_dashboard_skeleton);
            avatar.setImageResource(R.drawable.dashboard_bg_skeleton_circle);
            content.setVisibility(View.INVISIBLE);
            skeleton.setVisibility(View.VISIBLE);
        }
    }

    public static class Item {

        private static final int STATE_NORMAL = 0;
        private static final int STATE_LOADING = 1;
        private static final int STATE_ERROR = 2;

        private int age;
        private int gender;
        private String name;
        private int count;
        private int oldCount;

        private int total;
        private int state;

        private Item() {
        }

        private Item(int age, int gender, String name, int count, int oldCount) {
            this.age = age;
            this.gender = gender;
            this.name = name;
            this.count = count;
            this.oldCount = oldCount;
            this.state = STATE_NORMAL;
        }

        private void setTotal(int total) {
            this.total = total;
        }

        private void setLoading() {
            this.state = STATE_LOADING;
        }

        private void setError() {
            this.state = STATE_ERROR;
        }

        @NotNull
        @Override
        public String toString() {
            return "Item{" +
                    "name='" + name + '\'' +
                    ", count=" + count +
                    '}';
        }
    }

    public static class Model extends BaseRefreshCard.BaseModel {
        private List<Item> list = new ArrayList<>(3);

        @Override
        public void init(DashboardCondition condition) {
            list.clear();
        }

    }
}
