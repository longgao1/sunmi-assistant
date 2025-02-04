package sunmi.common.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.commonlibrary.R;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author yinhui
 * @date 2019-11-11
 */
public class DropdownMenuNew extends FrameLayout implements View.OnClickListener {

    public static final int MODE_DIALOG = 0;
    public static final int MODE_POPUP = 1;
    public static final int MODE_CUSTOM = 2;
    private static final int MODE_DEFAULT = -1;

    private int mode;
    private boolean isAnimated = true;
    private boolean isAutoDismiss = true;
    private boolean isMultiSelect = false;

    private Popup popup;
    private Anim dropdownAnim;
    private RecyclerView.LayoutManager manager;
    private RecyclerView.ItemDecoration decoration;

    private ViewHolder title;
    private boolean isPopupInit = false;

    public DropdownMenuNew(@NonNull Context context) {
        this(context, MODE_DEFAULT);
    }

    public DropdownMenuNew(@NonNull Context context, int mode) {
        this(context, null, mode);
    }

    public DropdownMenuNew(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, MODE_DEFAULT);
    }

    public DropdownMenuNew(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, MODE_DEFAULT);
    }

    public DropdownMenuNew(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int mode) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0, mode);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DropdownMenuNew(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes, int mode) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes, mode);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int mode) {
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.DropdownMenuNew, defStyleAttr, defStyleRes);

        // 获取自定义布局
        int layoutId = a.getResourceId(R.styleable.DropdownMenuNew_dm_layout, 0);
        int listId = a.getResourceId(R.styleable.DropdownMenuNew_dm_list, 0);
        if (layoutId == 0) {
            layoutId = R.layout.dropdown_menu_item_rv;
            listId = R.id.dropdown_rv;
        }
        // 获取指定的下拉菜单父亲布局的ID
        int menuParent = a.getResourceId(R.styleable.DropdownMenuNew_dm_menuParent, 0);
        int menuIndex = a.getInteger(R.styleable.DropdownMenuNew_dm_menuIndex, -1);

        // 获取下拉菜单的样式
        if (mode == MODE_DEFAULT) {
            mode = a.getInt(R.styleable.DropdownMenuNew_dm_style, MODE_CUSTOM);
        }

        switch (mode) {
            case MODE_DIALOG:
                break;
            case MODE_POPUP:
                break;
            case MODE_CUSTOM:
                popup = new ViewListPopup(context, layoutId, listId, menuParent, menuIndex);
                break;
            default:
        }

        // 列表最大高度所展示的列表元素个数 / 列表最大高度限定，元素个数优先级高
        float count = a.getFloat(R.styleable.DropdownMenuNew_dm_maxHeightCount, 7.5f);
        float height = a.getDimension(R.styleable.DropdownMenuNew_dm_maxHeight, -1);
        manager = new FixedLayoutManager(context, count, height);

        // 点击选择后是否自动dismiss菜单
        isAutoDismiss = a.getBoolean(R.styleable.DropdownMenuNew_dm_autoDismiss, isAutoDismiss);
        // 是否为多选
        isMultiSelect = a.getBoolean(R.styleable.DropdownMenuNew_dm_multiSelect, isMultiSelect);
        // 是否开启动画
        isAnimated = a.getBoolean(R.styleable.DropdownMenuNew_dm_animated, isAnimated);

        a.recycle();
    }

    public void setAdapter(Adapter adapter) {
        popup.init();
        popup.setAdapter(adapter);
        adapter.setMenu(this);
        title = adapter.getTitle();
        LayoutParams lp = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        addView(title.itemView, lp);
        adapter.updateTitle();
        adapter.updateContent();
        setOnClickListener(this);
    }

    public void setLayoutManager(RecyclerView.LayoutManager manager) {
        this.manager = manager;
        popup.setLayoutManager(manager);
    }

    public void setDecoration(RecyclerView.ItemDecoration decoration) {
        this.decoration = decoration;
        popup.setDecoration(decoration);
    }

    public void setAnim(Anim anim) {
        this.dropdownAnim = anim;
    }

    public boolean isShowing() {
        return popup.isShowing();
    }

    public void show(boolean animated) {
        popup.show(animated);
    }

    public void dismiss(boolean animated) {
        popup.dismiss(animated);
    }

    public void toggle(boolean animated) {
        if (isShowing()) {
            dismiss(animated);
        } else {
            show(animated);
        }
    }

    public View getContent() {
        return popup.getContent();
    }

    public RecyclerView getRecyclerView() {
        return popup.getRecyclerView();
    }

    @Override
    public void onClick(View v) {
        toggle(isAnimated);
    }

    /**
     * Implements some sort of popup selection interface for selecting a dropdown menu option.
     * Allows for different dropdown menu modes.
     */
    private interface Popup {

        /**
         * Initiation for popup
         */
        void init();

        /**
         * Setup adapter for dropdown menu.
         *
         * @param adapter Adapter
         */
        void setAdapter(Adapter adapter);

        /**
         * Setup layout manager for recycler view in dropdown menu
         *
         * @param manager layout manager
         */
        void setLayoutManager(RecyclerView.LayoutManager manager);

        /**
         * Set item decoration for recycler view in dropdown menu
         *
         * @param decoration item decoration
         */
        void setDecoration(RecyclerView.ItemDecoration decoration);

        /**
         * whether popup is showing or not.
         *
         * @return true if the popup is showing, false otherwise.
         */
        boolean isShowing();

        /**
         * Show the popup
         *
         * @param animated is animated or not
         */
        void show(boolean animated);

        /**
         * Dismiss the popup
         *
         * @param animated is animated or not
         */
        void dismiss(boolean animated);

        View getContent();

        RecyclerView getRecyclerView();

    }

    private class ViewListPopup implements Popup {

        private Context context;
        private ViewHolder title;
        private AnimatorSet currentAnim;
        private int layoutId;
        private int listId;

        private int menuParentId;
        private int menuIndex;

        boolean isInit = false;
        boolean isShowing = false;
        private Adapter adapter;

        private ViewGroup menuParent;
        private ViewGroup menuTitleGroup;

        private ViewGroup menuContainer;
        private RecyclerView menuList;
        private View overlay;

        public ViewListPopup(Context context, @LayoutRes int layoutId, @IdRes int listId,
                             @IdRes int menuParentId, int menuIndex) {
            this.context = context;
            this.layoutId = layoutId;
            this.listId = listId;
            this.menuParentId = menuParentId;
            this.menuIndex = menuIndex;
        }

        @Override
        public void init() {
            if (isInit) {
                return;
            }
            // 获取下拉菜单的容器View，DropdownMenu容器布局View，下拉菜单在父亲容器中的索引
            if (menuParentId == 0) {
                menuParent = (ViewGroup) getParent();
                menuTitleGroup = DropdownMenuNew.this;
                if (menuIndex < 0) {
                    menuIndex = getIndexOfMenu();
                }
                if (menuIndex < 0) {
                    throw new IllegalStateException("No DropdownMenu in the layout: "
                            + menuParent.getClass().getSimpleName());
                }
            } else {
                ViewGroup parent = DropdownMenuNew.this;
                while (parent.getParent() instanceof ViewGroup) {
                    ViewGroup target = (ViewGroup) parent.getParent();
                    if (target.getId() == menuParentId) {
                        menuParent = target;
                        menuTitleGroup = parent;
                        break;
                    }
                    parent = target;
                }
                if (menuParent == null) {
                    throw new InvalidParameterException(
                            "Attr dm_parent is not EXIST or ancestor of DropdownMenu.");
                }
                // 校验menuIndex
                if (menuIndex < 0 || menuIndex >= menuParent.getChildCount()) {
                    throw new InvalidParameterException(
                            "If dm_parent is set, dm_menuIndex must be set an index greater than " +
                                    "or equal to 0 and less than the number of children. dm_menuIndex:"
                                    + menuIndex);
                }
            }
            if (menuTitleGroup.getId() == View.NO_ID) {
                menuTitleGroup.setId(View.generateViewId());
            }

            // 创建和初始化Menu列表View
            menuContainer = (ViewGroup) LayoutInflater.from(context).inflate(
                    layoutId, menuParent, false);
            // 查找并获取Menu列表中的RecyclerView对象
            if (listId == 0) {
                int count = menuContainer.getChildCount();
                for (int i = 0; i < count; i++) {
                    View child = menuContainer.getChildAt(i);
                    if (child instanceof RecyclerView) {
                        menuList = (RecyclerView) child;
                        break;
                    }
                }
                if (menuList == null) {
                    throw new RuntimeException("No RecyclerView in container layout: "
                            + menuContainer.getClass().getSimpleName());
                }
            } else {
                menuList = menuContainer.findViewById(listId);
            }

            menuParent.addView(menuContainer, menuIndex);
            initContainer(menuParent);

            // 创建和初始化Overlay
            overlay = new View(context);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            menuParent.addView(overlay, menuIndex, lp);
            initOverlay(menuParent);

            // 初始化List动画和布局管理器
            if (dropdownAnim == null) {
                dropdownAnim = new DefaultAnimation();
            }
            if (manager == null) {
                manager = new LinearLayoutManager(menuContainer.getContext());
            }
            menuList.setLayoutManager(manager);
            if (decoration != null) {
                menuList.addItemDecoration(decoration);
            }
            isInit = true;
        }

        private void initContainer(ViewGroup parent) {
            ViewGroup.LayoutParams layoutParams = menuContainer.getLayoutParams();
            menuContainer.setVisibility(INVISIBLE);
            if (menuContainer.getId() == View.NO_ID) {
                menuContainer.setId(View.generateViewId());
            }
            menuContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.c_white));
            if (menuContainer != menuList) {
                menuContainer.setOnTouchListener((v, event) -> true);
            }
            int menuTop = getBottom();
            if (parent instanceof FrameLayout) {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) layoutParams;
                lp.setMargins(0, menuTop, 0, 0);
            } else if (parent instanceof RelativeLayout) {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) layoutParams;
                lp.addRule(RelativeLayout.BELOW, menuTitleGroup.getId());
            } else if (parent instanceof ConstraintLayout) {
                ConstraintSet set = new ConstraintSet();
                set.clone((ConstraintLayout) parent);
                set.connect(menuContainer.getId(), ConstraintSet.TOP, menuTitleGroup.getId(), ConstraintSet.BOTTOM, 0);
                set.connect(menuContainer.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0);
                set.connect(menuContainer.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0);
                set.connect(menuContainer.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0);
                set.setVerticalBias(menuContainer.getId(), 0f);
                set.constrainHeight(menuContainer.getId(), ConstraintSet.MATCH_CONSTRAINT);
                set.constrainWidth(menuContainer.getId(), ConstraintSet.MATCH_CONSTRAINT);
                if (layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
                    set.constrainDefaultHeight(menuContainer.getId(), ConstraintSet.MATCH_CONSTRAINT_WRAP);
                }
                set.applyTo((ConstraintLayout) parent);
            } else {
                throw new UnsupportedOperationException("Unsupported parent layout type:"
                        + parent.getClass().getSimpleName()
                        + "; Only FrameLayout, RelativeLayout & ConstraintLayout allowed.");
            }
        }

        private void initOverlay(ViewGroup parent) {
            // TODO: 同页面复用
            overlay.setId(View.generateViewId());
            overlay.setBackgroundColor(ContextCompat.getColor(context, R.color.black_60));
            overlay.setAlpha(0f);
            overlay.setOnClickListener(v -> dismiss(isAnimated));
            overlay.setVisibility(INVISIBLE);
        }

        @Override
        public void setAdapter(Adapter adapter) {
            this.adapter = adapter;
            title = adapter.getTitle();
            menuList.setAdapter(adapter);
            adapter.setContent(menuContainer);
        }

        @Override
        public void setLayoutManager(RecyclerView.LayoutManager manager) {
            if (menuList != null) {
                menuList.setLayoutManager(manager);
            }
        }

        @Override
        public void setDecoration(RecyclerView.ItemDecoration decoration) {
            if (menuList != null) {
                menuList.addItemDecoration(decoration);
            }
        }

        @Override
        public boolean isShowing() {
            return isShowing;
        }

        @Override
        public void show(boolean animated) {
            if (isShowing) {
                return;
            }
            isShowing = true;
            if (menuContainer.getVisibility() == INVISIBLE) {
                dropdownAnim.onPostDismiss(title, menuContainer, overlay);
                menuContainer.setVisibility(VISIBLE);
            }
            if (animated) {
                AnimatorSet set = dropdownAnim.showAnimator(title, menuContainer, overlay);
                addShowAnimListener(set);
                if (currentAnim != null && currentAnim.isRunning()) {
                    currentAnim.cancel();
                }
                set.start();
                currentAnim = set;
            } else {
                dropdownAnim.onPostShow(title, menuContainer, overlay);
            }
        }

        @Override
        public void dismiss(boolean animated) {
            if (!isShowing) {
                return;
            }
            isShowing = false;
            if (animated) {
                AnimatorSet set = dropdownAnim.dismissAnimator(title, menuContainer, overlay);
                addDismissAnimListener(set);
                if (currentAnim != null && currentAnim.isRunning()) {
                    currentAnim.cancel();
                }
                set.start();
                currentAnim = set;
            } else {
                adapter.updateTitle();
                dropdownAnim.onPostDismiss(title, menuContainer, overlay);
            }
        }

        @Override
        public View getContent() {
            return menuContainer;
        }

        @Override
        public RecyclerView getRecyclerView() {
            return menuList;
        }

        private int getIndexOfMenu() {
            int count = menuParent.getChildCount();
            int index = 0;
            for (; index < count; index++) {
                if (menuParent.getChildAt(index) instanceof DropdownMenuNew) {
                    return index;
                }
            }
            return -1;
        }

        private void addShowAnimListener(AnimatorSet set) {
            if (set.getListeners() == null || set.getListeners().isEmpty()) {
                set.addListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationStart(Animator animation) {
                        dropdownAnim.onPreShow(title, menuContainer, overlay);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        dropdownAnim.onPostShow(title, menuContainer, overlay);
                    }
                });
            }
        }

        private void addDismissAnimListener(AnimatorSet set) {
            if (set.getListeners() == null || set.getListeners().isEmpty()) {
                set.addListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationStart(Animator animation) {
                        dropdownAnim.onPreDismiss(title, menuContainer, overlay);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        dropdownAnim.onPostDismiss(title, menuContainer, overlay);
                        adapter.updateTitle();
                    }
                });
            }
        }

    }

    public static abstract class Adapter<T extends Model>
            extends RecyclerView.Adapter<ViewHolder<T>> {

        private DropdownMenuNew dropdownMenu;
        private ViewHolder<T> title;
        private ViewHolder<T> content;

        @LayoutRes
        private int itemRes;

        private List<T> data = new ArrayList<>();
        private List<T> selected = new ArrayList<>();

        private OnItemClickListener<T> listener;

        protected Adapter(Context context, @LayoutRes int titleRes, @LayoutRes int itemRes) {
            View title = LayoutInflater.from(context).inflate(titleRes, null);
            this.title = new ViewHolder<>(title);
            this.itemRes = itemRes;
        }

        protected Adapter(Context context, View title, @LayoutRes int itemRes) {
            this.title = new ViewHolder<>(title);
            this.itemRes = itemRes;
        }

        private void setMenu(DropdownMenuNew menu) {
            this.dropdownMenu = menu;
        }

        private void setContent(View content) {
            this.content = new ViewHolder<>(content);
        }

        @Override
        @NonNull
        public ViewHolder<T> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(itemRes, parent, false);
            ViewHolder<T> holder = new ViewHolder<>(itemView);
            holder.setOnClickListener(v -> {
                int position = holder.getAdapterPosition();
                T model = data.get(position);
                setSelected(position);
                if (listener != null) {
                    listener.onItemSelected(this, model, position);
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder<T> holder, int position) {
            T model = data.get(position);
            setupItem(holder, model, position);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public void setSelected(int position) {
            T model = data.get(position);
            if (dropdownMenu.isMultiSelect) {
                boolean checked = !model.isChecked();
                model.setChecked(checked);
                if (!checked) {
                    selected.remove(model);
                } else {
                    selected.add(model);
                }
            } else {
                for (T item : selected) {
                    item.setChecked(false);
                }
                model.setChecked(true);
                selected.clear();
                selected.add(model);
            }
            updateTitle();
            updateContent();
            notifyDataSetChanged();
            if (dropdownMenu.isAutoDismiss) {
                dropdownMenu.dismiss(dropdownMenu.isAnimated);
            }
        }

        protected void updateTitle() {
            setupTitle(title, selected);
        }

        protected void updateContent() {
            setupContent(content, selected);
        }

        public void setData(@NonNull Collection<? extends T> items) {
            this.data.clear();
            this.selected.clear();
            this.data.addAll(items);
            for (T item : items) {
                if (item.isChecked()) {
                    this.selected.add(item);
                }
            }
            updateTitle();
            updateContent();
            notifyDataSetChanged();
        }

        public void setOnItemClickListener(OnItemClickListener<T> l) {
            listener = l;
        }

        public DropdownMenuNew getMenu() {
            return dropdownMenu;
        }

        public ViewHolder<T> getTitle() {
            return title;
        }

        public ViewHolder<T> getContent() {
            return content;
        }

        public List<T> getData() {
            return data;
        }

        public List<T> getSelected() {
            return selected;
        }

        /**
         * 根据Model更新标题（DropdownMenu本身）
         *
         * @param holder 标题的ViewHolder
         * @param models Model
         */
        protected abstract void setupTitle(@NonNull ViewHolder<T> holder, List<T> models);

        /**
         * 根据Model更新内容（下拉列表中除了List以外的自定义组件）
         *
         * @param holder 内容的ViewHolder
         * @param models Model
         */
        protected void setupContent(@NonNull ViewHolder<T> holder, List<T> models) {
        }

        /**
         * 根据Model更新List元素
         *
         * @param holder   List元素的ViewHolder
         * @param model    Model
         * @param position 位置
         */
        protected void setupItem(@NonNull ViewHolder<T> holder, T model, int position) {
        }

    }

    public static class ViewHolder<T> extends RecyclerView.ViewHolder {

        private SparseArray<View> views = new SparseArray<>();

        public ViewHolder(View itemView) {
            super(itemView);
        }

        public void setOnClickListener(@Nullable OnClickListener l) {
            itemView.setOnClickListener(l);
        }

        public Context getContext() {
            return itemView.getContext();
        }

        @SuppressWarnings("unchecked")
        public <V extends View> V getView(int resId) {
            View view = views.get(resId);
            if (view == null) {
                view = itemView.findViewById(resId);
                if (view == null) {
                    return null;
                }
                views.put(resId, view);
            }
            return (V) view;
        }

    }

    private static class FixedLayoutManager extends LinearLayoutManager {

        private float maxCount;
        private float maxHeight;

        private FixedLayoutManager(Context context, float count, float height) {
            super(context);
            this.maxCount = count;
            this.maxHeight = height;
        }

        @Override
        public void setMeasuredDimension(Rect childrenBounds, int wSpec, int hSpec) {
            boolean requestLimit = maxCount > 0 || maxHeight > 0;
            int count = getChildCount();
            if (!requestLimit || count == 0) {
                super.setMeasuredDimension(childrenBounds, wSpec, hSpec);
                return;
            }
            if (maxCount > 0 && count > maxCount) {
                int height = childrenBounds.bottom - childrenBounds.top;
                height = (int) (height * maxCount / count);
                childrenBounds.bottom = childrenBounds.top + height;
                super.setMeasuredDimension(childrenBounds, wSpec, hSpec);
            } else if (maxHeight > 0 && childrenBounds.bottom - childrenBounds.top > maxHeight) {
                childrenBounds.bottom = (int) (childrenBounds.top + maxHeight);
                super.setMeasuredDimension(childrenBounds, wSpec, hSpec);
            } else {
                super.setMeasuredDimension(childrenBounds, wSpec, hSpec);
            }
        }

    }

    private static class DefaultAnimation implements Anim {

        @Override
        public AnimatorSet showAnimator(ViewHolder titleHolder, View menu, View overlay) {
            AnimatorSet set = new AnimatorSet();
            ObjectAnimator menuAnim = ObjectAnimator.ofFloat(menu, "translationY",
                    menu.getTranslationY(), 0);
            ObjectAnimator overlayAnim = ObjectAnimator.ofFloat(overlay, "alpha",
                    overlay.getAlpha(), 1f);
            set.play(menuAnim).with(overlayAnim);
            set.setDuration(250);
            set.setInterpolator(new DecelerateInterpolator());
            return set;
        }

        @Override
        public void onPostShow(ViewHolder titleHolder, View menu, View overlay) {
            menu.setTranslationY(0);
            overlay.setAlpha(1f);
        }

        @Override
        public void onPreShow(ViewHolder titleHolder, View menu, View overlay) {
            menu.setVisibility(VISIBLE);
            overlay.setVisibility(VISIBLE);
        }

        @Override
        public AnimatorSet dismissAnimator(ViewHolder titleHolder, View menu, View overlay) {
            AnimatorSet set = new AnimatorSet();
            ObjectAnimator menuAnim = ObjectAnimator.ofFloat(menu, "translationY",
                    menu.getTranslationY(), -menu.getHeight());
            ObjectAnimator overlayAnim = ObjectAnimator.ofFloat(overlay, "alpha",
                    overlay.getAlpha(), 0);
            set.play(menuAnim).with(overlayAnim);
            set.setDuration(250);
            set.setInterpolator(new DecelerateInterpolator());
            return set;
        }

        @Override
        public void onPostDismiss(ViewHolder titleHolder, View menu, View overlay) {
            menu.setVisibility(INVISIBLE);
            menu.setTranslationY(-menu.getHeight());
            overlay.setVisibility(INVISIBLE);
            overlay.setAlpha(0f);
        }

        @Override
        public void onPreDismiss(ViewHolder titleHolder, View menu, View overlay) {
        }
    }

    public static class Model {

        protected boolean isChecked;

        public Model() {
        }

        public Model(boolean isChecked) {
            this.isChecked = isChecked;
        }

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(boolean checked) {
            this.isChecked = checked;
        }
    }

    public interface Anim {

        /**
         * 菜单展示时的动画
         *
         * @param titleHolder 标题
         * @param menu        菜单
         * @param overlay     遮罩
         * @return 动画集
         */
        AnimatorSet showAnimator(ViewHolder titleHolder, View menu, View overlay);

        /**
         * 菜单无动画立即展示
         *
         * @param titleHolder 标题
         * @param menu        菜单
         * @param overlay     遮罩
         */
        void onPostShow(ViewHolder titleHolder, View menu, View overlay);

        /**
         * 菜单展示动画前回调
         *
         * @param titleHolder 标题
         * @param menu        菜单
         * @param overlay     遮罩
         */
        void onPreShow(ViewHolder titleHolder, View menu, View overlay);

        /**
         * 菜单消失时的动画
         *
         * @param titleHolder 标题
         * @param menu        菜单
         * @param overlay     遮罩
         * @return 动画集
         */
        AnimatorSet dismissAnimator(ViewHolder titleHolder, View menu, View overlay);

        /**
         * 菜单无动画立即消失
         *
         * @param titleHolder 标题
         * @param menu        菜单
         * @param overlay     遮罩
         */
        void onPostDismiss(ViewHolder titleHolder, View menu, View overlay);

        /**
         * 菜单消失动画前回调
         *
         * @param titleHolder 标题
         * @param menu        菜单
         * @param overlay     遮罩
         */
        void onPreDismiss(ViewHolder titleHolder, View menu, View overlay);
    }

    public interface OnItemClickListener<T extends Model> {

        /**
         * 菜单列表选择事件回调
         *
         * @param adapter  适配器
         * @param model    Model
         * @param position 位置
         */
        void onItemSelected(Adapter<T> adapter, T model, int position);
    }

}
