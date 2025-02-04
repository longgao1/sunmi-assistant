package com.sunmi.ipc.rpc;

/**
 * Description:
 * Created by bruce on 2019/3/29.
 */
public class OpcodeConstants {

    public static final int getWifiList = 0x3118;
    public static final int setIPCWifi = 0x3116;
    public static final int getIpcConnectApMsg = 0x3117;
    public static final int getApStatus = 0x3119;
    public static final int getIpcToken = 0x3124;
    public static final int bindIpc = 0x3059;
    public static final int getIsWire = 0x3126;

    public static final int getSdStatus = 0x3102;
    public static final int fsZoom = 0x3104;
    public static final int fsFocus = 0x3105;
    public static final int fsAutoFocus = 0x3106;
    public static final int fsReset = 0x3107;
    public static final int fsIrMode = 0x3108;
    public static final int fsGetStatus = 0x3109;
    public static final int fsSetLine = 0x3162;
    public static final int ipcUpgrade = 0x3140;
    public static final int ipcQueryUpgradeStatus = 0x3141;
    public static final int getIpcNightIdeRotation = 0x305c;
    public static final int setIpcNightIdeRotation = 0x305d;
    public static final int getIpcDetection = 0x3120;
    public static final int setIpcDetection = 0x3121;
    public static final int sdcardFormat = 0x3058;
    public static final int ipcRelaunch = 0x3170;

    public static final int fsAdjustFocusReset = 0x3167;//设置聚焦微复位
    public static final int fsAdjustFocusAdd = 0x3168;//设置聚焦微调加（+）的接口
    public static final int fsAdjustFocusMinus = 0x3169;//设置聚焦微调加（-）的接口
    public static final int fsAdjustBrightness = 0x316a;//设置图像亮度参数接口
    public static final int fsAdjustContrast = 0x316b;//设置图像对比度参数接口
    public static final int fsAdjustSaturation = 0x316c;//设置图像饱和度参数接口
    public static final int getVideoParams = 0x316d;//获取图像配置当前参数请求

}
