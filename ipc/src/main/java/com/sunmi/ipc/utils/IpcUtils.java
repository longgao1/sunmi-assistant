package com.sunmi.ipc.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sunmi.common.constant.CommonConstants;
import sunmi.common.constant.enums.DeviceStatus;
import sunmi.common.utils.log.LogCat;

/**
 * @author yinhui
 * @date 2019-11-14
 */
public class IpcUtils {

    private static final String TAG = "IpcUtils";

    private static final Pattern IPC_VERSION_NAME = Pattern.compile("^\\d{1,2}\\.\\d{1,2}\\.\\d{1,2}$");

    public static boolean isNewVersion(String currVersion, String lastVersion) {
        return getVersionCode(currVersion) >= getVersionCode(lastVersion);
    }

    public static int getVersionCode(String version) {
        if (!isVersionValid(version)) {
            LogCat.e(TAG, "Version name of \"" + version + "\" is invalid.");
            return -1;
        }
        String[] split = version.split("\\.");
        int versionCode = 0;
        for (int i = 0, size = split.length; i < size; i++) {
            versionCode += Integer.valueOf(split[i]) * (int) Math.pow(100, 2 - i);
        }
        return versionCode;
    }

    public static boolean isVersionValid(String version) {
        return IPC_VERSION_NAME.matcher(version).matches();
    }

    public static boolean isIpcManageable(String deviceId, int status) {
        return status == DeviceStatus.ONLINE.ordinal()
                || CommonConstants.SUNMI_DEVICE_MAP.containsKey(deviceId);
    }

    public static String formatMac(String mac, String split) {
        String regex = "[0-9a-fA-F]{12}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(mac);

        if (!matcher.matches()) {
            return mac;
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 12; i++) {
            char c = mac.charAt(i);
            sb.append(c);
            if ((i & 1) == 1 && i <= 9) {
                sb.append(split);
            }
        }

        return sb.toString();
    }

}
