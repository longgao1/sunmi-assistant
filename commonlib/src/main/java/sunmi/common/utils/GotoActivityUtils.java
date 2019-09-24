package sunmi.common.utils;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import sunmi.common.base.BaseApplication;
import sunmi.common.utils.log.LogCat;

/**
 * Created by YangShiJie on 2019/4/10.
 * 多端登录剔除，针对部分手机无法后台默认自启动如：Oppo等
 */
public class GotoActivityUtils {

    public static void gotoLoginActivity(String className) {
        if (TextUtils.isEmpty(SpUtils.getLoginStatus())
                && !className.contains("LoginActivity")
                && !className.contains("LeadPagesActivity")
                && !className.contains("WelcomeActivity")
                && !className.contains("RegisterActivity")
                && !className.contains("RetrievePasswordActivity")
                && !className.contains("InputCaptchaActivity")
                && !className.contains("InputMobileActivity")
                && !className.contains("SetPasswordActivity")
                && !className.contains("ProtocolActivity")
                && !className.contains("UserMergeActivity")
                && !className.contains("LoginChooseShopActivity")
                && !className.contains("PlatformMobileActivity")
                && !className.contains("SelectPlatformActivity")
                && !className.contains("SelectStoreActivity")
                && !className.contains("CreateCompanyActivity")
                && !className.contains("CreateCompanyNextActivity")
                && !className.contains("CreateShopActivity")
                && !className.contains("CreateShopPreviewActivity")
                ) {
            LogCat.e("TAG", "gotoLoginActivity= " + className);
            gotoLoginActivity(BaseApplication.getContext(), "1"); //1 剔除多端登录
        }
    }

    public static void gotoLoginActivity(Context context, String extra) {
        try {
            Class<?> loginActivity = Class.forName("com.sunmi.assistant.ui.activity.login.LoginActivity_");
            Intent intent = new Intent(context, loginActivity);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);

            if (!TextUtils.isEmpty(extra))
                intent.putExtra("reason", extra);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void gotoMainActivityClearTask(Context context) {
        try {
            Class<?> mainActivity = Class.forName("com.sunmi.assistant.ui.activity.MainActivity_");
            Intent intent = new Intent(context, mainActivity);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void gotoMainActivity(Context context) {
        try {
            Class<?> mainActivity = Class.forName("com.sunmi.assistant.ui.activity.MainActivity_");
            Intent intent = new Intent(context, mainActivity);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void gotoSunmiLinkSearchActivity(Context context, String shopId, String sn) {
        try {
            Class<?> loginActivity = Class.forName("com.sunmi.assistant.ui.activity.SunmiLinkSearchActivity_");
            Intent intent = new Intent(context, loginActivity);
            if (!TextUtils.isEmpty(sn))
                intent.putExtra("sn", sn);
            if (!TextUtils.isEmpty(shopId))
                intent.putExtra("shopId", shopId);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void gotoMsgDetailActivity(Context context, int modelId, String modelName) {
        try {
            Class<?> activity = Class.forName("com.sunmi.assistant.mine.message.MsgDetailActivity_");
            Intent intent = new Intent(context, activity);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("modelId", modelId);
            intent.putExtra("modelName", modelName);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void gotoMsgCenterActivity(Context context) {
        try {
            Class<?> activity = Class.forName("com.sunmi.assistant.mine.message.MsgCenterActivity_");
            Intent intent = new Intent(context, activity);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
