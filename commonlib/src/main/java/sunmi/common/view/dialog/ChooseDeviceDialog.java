package sunmi.common.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.commonlibrary.R;

import sunmi.common.constant.CommonConstants;
import sunmi.common.view.SimpleRecyclerViewAdapter;
import sunmi.common.view.activity.StartConfigSMDeviceActivity_;

/**
 * Description:
 * Created by bruce on 2019/4/12.
 */
public class ChooseDeviceDialog extends Dialog {

    private int shopId;

    public ChooseDeviceDialog(Context context, int shopId) {
        super(context);
        this.shopId = shopId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_choose_device, null);
        setContentView(contentView);
        Window window = getWindow();
        if (window != null) {
            window.setWindowAnimations(R.style.BottomDialog_Animation);
        }
        RecyclerView recyclerView = findViewById(R.id.rv_devices);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(getAdapter());

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = getContext().getResources().getDisplayMetrics().widthPixels;
        getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_corner_top_white);
        getWindow().setAttributes(lp);//设置宽度适应屏幕
        getWindow().setGravity(Gravity.BOTTOM);
    }

    /**
     * 只有图标和文字的简单adapter
     */
    private SimpleRecyclerViewAdapter getAdapter() {
        int[] imageIds;
        imageIds = new int[]{R.mipmap.ic_add_sunmi_ap, R.mipmap.ic_add_sunmi_printer,
                R.mipmap.ic_add_sunmi_fs, R.mipmap.ic_add_sunmi_ss, R.mipmap.ic_add_more};
        final String[] names = getContext().getResources().getStringArray(R.array.sunmi_devices);
        SimpleRecyclerViewAdapter adapter = new SimpleRecyclerViewAdapter(
                R.layout.item_choose_device, imageIds, names);
        adapter.setOnItemClickListener(new SimpleRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                if (pos == names.length - 1) {
                    return;
                }
                dismiss();
                if (pos == CommonConstants.TYPE_PRINTER) {
                    gotoPrinterConfig();
                } else {
                    StartConfigSMDeviceActivity_.intent(getContext())
                            .deviceType(pos).shopId(shopId + "").start();
                }
            }
        });
        return adapter;
    }

    private void gotoPrinterConfig() {
        try {
            Class<?> printerSearchActivity =
                    Class.forName("com.sunmi.cloudprinter.ui.Activity.StartConfigPrinterActivity_");
            Intent intent = new Intent(getContext(), printerSearchActivity);
            intent.putExtra("shopId", shopId);
            getContext().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}