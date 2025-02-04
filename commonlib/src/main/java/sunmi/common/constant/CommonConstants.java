package sunmi.common.constant;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import sunmi.common.base.BaseApplication;
import sunmi.common.model.SunmiDevice;
import sunmi.common.utils.CommonHelper;

/**
 * Description:
 * Created by bruce on 2019/2/13.
 */
public class CommonConstants {
    public static final String FILE_PROVIDER_AUTHORITY =
            CommonHelper.getAppPackageName(BaseApplication.getContext()) + ".fileprovider";
    public static Map<String, SunmiDevice> SUNMI_DEVICE_MAP = new ConcurrentHashMap<>();

    /**
     * 视角（总部视角/门店视角）
     */
    public static final int PERSPECTIVE_NONE = -1;
    public static final int PERSPECTIVE_TOTAL = 1;
    public static final int PERSPECTIVE_SHOP = 2;

    public static long LONGITUDE;//精度
    public static long LATITUDE;//纬度

    //UDP发送IP
    public final static String SEND_IP = "255.255.255.255";
    //UDP发送端口号
    public final static int SEND_PORT = 10001;
    public static final int WHAT_UDP_GET_SN = 9988; //发送udp报文获取sn

    //tab support
    public static final int tabSupport = 100000;
    //tab store
    public static final int tabDevice = 100001;

    //选择商户和门店
    public static int ACTION_LOGIN_CHOOSE_COMPANY = 0;
    public static int ACTION_LOGIN_CHOOSE_SHOP = 1;
    public static int ACTION_CHANGE_COMPANY = 2;

    public static final String GOOGLE_PLAY = "google";

    //服务订阅状态
    public static final int SERVICE_ALREADY_OPENED = 1;
    public static final int SERVICE_NOT_OPENED = 2;
    public static final int SERVICE_EXPIRED = 3;

    //云存储续费状态
    public static final int CLOUD_STORAGE_RENEWABLE = 1;
    public static final int CLOUD_STORAGE_NOT_RENEWABLE = 2;

    //储捆绑服务状态
    public static final int SERVICE_INACTIVATED = 1;
    public static final int SERVICE_ACTIVATED = 2;

    //配置IPC的不同来源
    public static final int CONFIG_IPC_FROM_COMMON = 1;
    public static final int CONFIG_IPC_FROM_CASH_VIDEO = 2;

    public static final int IMPORT_ORDER_FROM_COMMON = 1;
    public static final int IMPORT_ORDER_FROM_CASH_VIDEO = 2;

    //H5 返回的结果
    public static final int RESULT_OK = 1;
    public static final int RESULT_FAIL = 0;

    //服务类型
    public static final int SERVICE_TYPE_CLOUD_7 = 1;
    public static final int SERVICE_TYPE_CLOUD_30 = 2;
    public static final int SERVICE_TYPE_LOAN = 6;

    public static final int SERVICE_STATUS_UNABLE = 1;
    public static final int SERVICE_STATUS_ABLE = 2;

    //H5后序地址
    public static final String H5_CLOUD_STORAGE = CommonConfig.SERVICE_H5_URL + "cloudStorage";
    public static final String H5_CASH_VIDEO = CommonConfig.SERVICE_H5_URL + "cashvideo/welcome";
    public static final String H5_ORDER_MANAGE = CommonConfig.SERVICE_H5_URL + "orderManagement/orderList";
    public static final String H5_CLOUD_RENEW = CommonConfig.SERVICE_H5_URL + "cloudStorage/subscribeService";
    public static final String H5_AGREEMENT = CommonConfig.SERVICE_H5_URL + "privacyManagement";
    public static final String H5_CASH_PREVENT_LOSS = CommonConfig.SERVICE_H5_URL + "cashPreventLoss";
    public static final String H5_LOAN = CommonConfig.SUNMI_H5_URL + "jie/";
    public static final String H5_SERVICE_MANAGER = CommonConfig.SERVICE_H5_URL + "serviceManagement";
    public static final String H5_CASH_VIDEO_RENEW = CommonConfig.SERVICE_H5_URL + "cashvideo/service";
    public static final String H5_CASH_PREVENT_RENEW = CommonConfig.SERVICE_H5_URL + "cashPreventLoss/confirmOrder";
    public static final String H5_SERVICE_COURSE = CommonConfig.SERVICE_H5_URL + "dinglive/dinghome";

}
