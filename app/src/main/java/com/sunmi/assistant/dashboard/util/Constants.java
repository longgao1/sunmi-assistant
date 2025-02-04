package com.sunmi.assistant.dashboard.util;

/**
 * @author yinhui
 * @date 2019-09-10
 */
public class Constants {

    public static final int PAGE_NONE = 0;
    public static final int PAGE_OVERVIEW = 1;
    public static final int PAGE_CUSTOMER = 2;
    public static final int PAGE_PROFILE = 3;

    public static final int PAGE_TOTAL_REALTIME = 11;
    public static final int PAGE_TOTAL_CUSTOMER = 12;

    public static final int FLAG_SHOP = 0x1;
    public static final int FLAG_SAAS = 0x2;
    public static final int FLAG_FS = 0x4;
    public static final int FLAG_CUSTOMER = 0x8;
    public static final int FLAG_BUNDLED_LIST = 0x10;
    public static final int FLAG_ALL_MASK = 0x1F;
    public static final int FLAG_CONDITION_COMPANY_MASK = FLAG_SAAS | FLAG_FS;
    public static final int FLAG_CONDITION_SHOP_MASK = FLAG_SAAS | FLAG_FS | FLAG_CUSTOMER | FLAG_BUNDLED_LIST;

    public static final int TIME_PERIOD_DAY = 1;
    public static final int TIME_PERIOD_WEEK = 2;
    public static final int TIME_PERIOD_MONTH = 3;

    public static final int DATA_MODE_SALES = 0;
    public static final int DATA_MODE_ORDER = 1;

    public static final int GENDER_MALE = 1;
    public static final int GENDER_FEMALE = 2;

    public static final int DATA_TYPE_RATE = 0;
    public static final int DATA_TYPE_VOLUME = 1;
    public static final int DATA_TYPE_CUSTOMER = 2;

    public static final int DATA_TYPE_NEW_OLD = 10;
    public static final int DATA_TYPE_GENDER = 11;
    public static final int DATA_TYPE_AGE = 12;

    public static final int DATA_TYPE_ALL = 20;
    public static final int DATA_TYPE_NEW = 21;
    public static final int DATA_TYPE_OLD = 22;

    public static final int SAAS_STATE_NONE = 0;
    public static final int SAAS_STATE_AUTH = 1;
    public static final int SAAS_STATE_IMPORT = 2;

    public static final int IMPORT_STATE_NONE = 0;
    public static final int IMPORT_STATE_DOING = 1;
    public static final int IMPORT_STATE_SUCCESS = 2;
    public static final int IMPORT_STATE_FAIL = 3;
    public static final int IMPORT_STATE_COMPLETE = 10;

    public static final int NO_CUSTOMER_DATA = 5087;
}
