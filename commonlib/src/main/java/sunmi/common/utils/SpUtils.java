package sunmi.common.utils;

import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.Set;

import sunmi.common.base.BaseApplication;

public class SpUtils {

    public static final String TOKEN_ROUTER = "TOKEN_ROUTER";
    private static final String HAS_LOGIN = "has_login";//Y-已登录，N-未登录,null-未登录
    private static final String USERNAME = "username";
    private static final String MOBILE = "MOBILE";
    private static final String EMAIL = "email";
    private static final String AVATAR_URL = "avatar_url";
    private static final String MERCHANT_UID = "MERCHANT_UID";
    private static final String UID = "UID";
    private static final String SSO_TOKEN = "SSO_TOKEN";
    private static final String STORE_TOKEN = "STORE_TOKEN";
    private static final String LEAD_PAGES = "LEAD_PAGES";
    private static final String WEB_WIDTH = "WEB_WIDTH";
    private static final String WEB_HEIGHT = "WEB_HEIGHT";
    private static final String SET_ROUTER_MANGER_PASSWORD = "SET_ROUTER_MANGER_PASSWORD";//设置路由管理密码
    private static final String UDP_ROUTER = "UDP_ROUTER";    //快速配置路由的token
    private static final String BIND_TYPE_ERROR = "BIND_TYPE_ERROR";    //快速配置绑定路由 1 net异常  2 其他人绑定
    private static final String COMPANY_ID = "company_id";
    private static final String COMPANY_NAME = "company_name";
    private static final String CURRENT_SHOP_ID = "current_shop_id";
    private static final String CURRENT_SHOP_NAME = "current_shop_name";
    private static final String SAAS_EXIST = "saas_exist";
    private static final String UNREAD_MSG = "unread_msg";
    private static final String REMIND_UNREAD_MSG = "remind_unread_msg";
    private static final String UNREAD_DEVICE_MSG = "unread_device_msg";
    private static final String UNREAD_SYSTEM_MSG = "unread_system_msg";
    private static final String AD_LOAN_UIDS = "ad_loan_uids";
    private static final String LOAN_STATUS = "loan_status";
    private static final String PERSPECTIVE = "perspective";

    SpUtils() {
    }

    private static SharedPreferences getSharedPreference() {
        return SharedManager.getSharedPreference(BaseApplication.getContext());
    }

    public static boolean isLoginSuccess() {
        return TextUtils.equals(SpUtils.getLoginStatus(), "Y");
    }

    //用户名称
    public static String getUsername() {
        return SharedManager.getValue(BaseApplication.getContext(), USERNAME);
    }

    public static void setUsername(String nickname) {
        SharedManager.putValue(BaseApplication.getContext(), USERNAME, nickname);
    }

    //用户Mobile
    public static String getMobile() {
        return SharedManager.getValue(BaseApplication.getContext(), MOBILE);
    }

    public static void setMobile(String mobile) {
        SharedManager.putValue(BaseApplication.getContext(), MOBILE, mobile);
    }

    //邮箱
    public static String getEmail() {
        return SharedManager.getValue(BaseApplication.getContext(), EMAIL);
    }

    public static void setEmail(String email) {
        SharedManager.putValue(BaseApplication.getContext(), EMAIL, email);
    }

    //用户头像
    public static String getAvatarUrl() {
        return SharedManager.getValue(BaseApplication.getContext(), AVATAR_URL);
    }

    public static void setAvatarUrl(String avatarUrl) {
        SharedManager.putValue(BaseApplication.getContext(), AVATAR_URL, avatarUrl);
    }

    //用户uid
    public static String getUID() {
        return SharedManager.getValue(BaseApplication.getContext(), UID);
    }

    public static void setUID(String uid) {
        SharedManager.putValue(BaseApplication.getContext(), UID, uid);
    }

    //用户Token
    public static String getSsoToken() {
        return SharedManager.getValue(BaseApplication.getContext(), SSO_TOKEN);
    }

    public static void setSsoToken(String ssoToken) {
        SharedManager.putValue(BaseApplication.getContext(), SSO_TOKEN, ssoToken);
    }

    //sunmi store Token
    public static String getStoreToken() {
        return SharedManager.getValue(BaseApplication.getContext(), STORE_TOKEN);
    }

    public static void setStoreToken(String token) {
        SharedManager.putValue(BaseApplication.getContext(), STORE_TOKEN, token);
    }

    //登录状态
    public static void setLoginStatus(String hasLogin) {
        SharedManager.putValue(BaseApplication.getContext(), HAS_LOGIN, hasLogin);
    }

    public static String getLoginStatus() {
        return SharedManager.getValue(BaseApplication.getContext(), HAS_LOGIN);
    }

    public static void clearUDPName() {
        SharedManager.clearValue(BaseApplication.getContext(), UDP_ROUTER);
    }

    /**
     * 快速配置 设置管理密码
     *
     * @param password
     */
    public static void saveRouterMangerPassword(String password) {
        SharedManager.putValue(BaseApplication.getContext(), SET_ROUTER_MANGER_PASSWORD, password);
    }

    //获取管理密码
    public static String getRouterMangerPassword() {
        return SharedManager.getValue(BaseApplication.getContext(), SET_ROUTER_MANGER_PASSWORD);
    }

    //保存路由器Token
    public static void saveRouterToken(String token) {
        SharedManager.putValue(BaseApplication.getContext(), TOKEN_ROUTER, token);
    }

    //用户路由器Token
    public static String getTokenRouter() {
        return SharedManager.getValue(BaseApplication.getContext(), TOKEN_ROUTER);
    }

    public static void clearRouterMangerPassword() {
        SharedManager.clearValue(BaseApplication.getContext(), SET_ROUTER_MANGER_PASSWORD);
    }

    //保存是否显示引导页
    public static void saveLead() {
        SharedManager.putValue(BaseApplication.getContext(), LEAD_PAGES, "TRUE");
    }

    //获取引导页值
    public static String getLead() {
        return SharedManager.getValue(BaseApplication.getContext(), LEAD_PAGES);
    }

    //clear引导页值
    public static void clearLead() {
        SharedManager.clearValue(BaseApplication.getContext(), LEAD_PAGES);
    }

    public static void saveWebWidthHeight(int width, int height) {
        SharedManager.putValue(BaseApplication.getContext(), WEB_WIDTH, width + "");
        SharedManager.putValue(BaseApplication.getContext(), WEB_HEIGHT, height + "");
    }

    public static int getWebWidth() {
        return Integer.valueOf(SharedManager.getValue(BaseApplication.getContext(), WEB_WIDTH));
    }

    public static int getWebHeight() {
        return Integer.valueOf(SharedManager.getValue(BaseApplication.getContext(), WEB_HEIGHT));
    }

    //快速配置绑定失败类型
    public static void setConfigBindType(String type) {
        SharedManager.putValue(BaseApplication.getContext(), BIND_TYPE_ERROR, type);
    }

    public static String getConfigBindType() {
        return SharedManager.getValue(BaseApplication.getContext(), BIND_TYPE_ERROR);
    }

    public static void clearConfigBindType() {
        SharedManager.clearValue(BaseApplication.getContext(), BIND_TYPE_ERROR);
    }

    public static void setCompanyId(int companyId) {
        SharedManager.putValue(BaseApplication.getContext(), COMPANY_ID, companyId);
    }

    public static int getCompanyId() {
        return SharedManager.getIntValue(BaseApplication.getContext(), COMPANY_ID);
    }

    public static void setCompanyName(String companyName) {
        SharedManager.putValue(BaseApplication.getContext(), COMPANY_NAME, companyName);
    }

    public static String getCompanyName() {
        return SharedManager.getValue(BaseApplication.getContext(), COMPANY_NAME);
    }

    public static void setShopId(int shopId) {
        SharedManager.putValue(BaseApplication.getContext(), CURRENT_SHOP_ID, shopId);
    }

    public static int getShopId() {
        return SharedManager.getIntValue(BaseApplication.getContext(), CURRENT_SHOP_ID);
    }

    public static void setShopName(String shopName) {
        SharedManager.putValue(BaseApplication.getContext(), CURRENT_SHOP_NAME, shopName);
    }

    public static String getShopName() {
        return SharedManager.getValue(BaseApplication.getContext(), CURRENT_SHOP_NAME);
    }

    public static void setSaasExist(int exist) {
        SharedManager.putValue(BaseApplication.getContext(), SAAS_EXIST, exist);
    }

    public static int getSaasExist() {
        return SharedManager.getIntValue(BaseApplication.getContext(), SAAS_EXIST);
    }

    public static void setUnreadMsg(int unreadMsg) {
        SharedManager.putValue(BaseApplication.getContext(), UNREAD_MSG, unreadMsg);
    }

    public static int getUnreadMsg() {
        return SharedManager.getIntValue(BaseApplication.getContext(), UNREAD_MSG);
    }

    public static void setRemindUnreadMsg(int remindUnreadMsg) {
        SharedManager.putValue(BaseApplication.getContext(), REMIND_UNREAD_MSG, remindUnreadMsg);
    }

    public static int getRemindUnreadMsg() {
        return SharedManager.getIntValue(BaseApplication.getContext(), REMIND_UNREAD_MSG);
    }

    public static void setUnreadDeviceMsg(int unreadDeviceMsg) {
        SharedManager.putValue(BaseApplication.getContext(), UNREAD_DEVICE_MSG, unreadDeviceMsg);
    }

    public static int getUnreadDeviceMsg() {
        return SharedManager.getIntValue(BaseApplication.getContext(), UNREAD_DEVICE_MSG);
    }

    public static void setUnreadSystemMsg(int unreadSystemMsg) {
        SharedManager.putValue(BaseApplication.getContext(), UNREAD_SYSTEM_MSG, unreadSystemMsg);
    }

    public static int getUnreadSystemMsg() {
        return SharedManager.getIntValue(BaseApplication.getContext(), UNREAD_SYSTEM_MSG);
    }

    public static void addShowAdLoanUid(String uid) {
        Set<String> uids = getAdLoanUids();
        uids.add(uid);
        SharedManager.putValue(BaseApplication.getContext(), AD_LOAN_UIDS, uids);
    }

    public static Set<String> getAdLoanUids() {
        return SharedManager.getStringSetValue(BaseApplication.getContext(), AD_LOAN_UIDS);
    }

    public static void setLoanStatus(boolean status) {
        SharedManager.putValue(BaseApplication.getContext(), LOAN_STATUS, status);
    }

    public static boolean getLoanStatus() {
        return SharedManager.getBooleanValue(BaseApplication.getContext(), LOAN_STATUS);
    }

    public static void setPerspective(int perspective) {
        SharedManager.putValue(BaseApplication.getContext(), PERSPECTIVE, perspective);
    }

    public static int getPerspective() {
        return SharedManager.getIntValue(BaseApplication.getContext(), PERSPECTIVE);
    }
}
