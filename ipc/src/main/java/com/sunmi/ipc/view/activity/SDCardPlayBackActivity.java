package com.sunmi.ipc.view.activity;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunmi.ipc.R;
import com.sunmi.ipc.calendar.Config;
import com.sunmi.ipc.calendar.VerticalCalendar;
import com.sunmi.ipc.config.IpcConstants;
import com.sunmi.ipc.contract.SDCardPlaybackContract;
import com.sunmi.ipc.model.VideoTimeSlotBean;
import com.sunmi.ipc.presenter.SDCardPlaybackPresenter;
import com.sunmi.ipc.rpc.IPCCall;
import com.sunmi.ipc.rpc.OpcodeConstants;
import com.sunmi.ipc.service.P2pService;
import com.sunmi.ipc.utils.IOTCClient;
import com.sunmi.ipc.view.ZFTimeLine;
import com.xiaojinzi.component.impl.Router;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import sunmi.common.base.BaseMvpActivity;
import sunmi.common.constant.CommonConstants;
import sunmi.common.constant.CommonNotifications;
import sunmi.common.model.SunmiDevice;
import sunmi.common.router.SunmiServiceApi;
import sunmi.common.rpc.sunmicall.ResponseBean;
import sunmi.common.utils.CommonHelper;
import sunmi.common.utils.DateTimeUtils;
import sunmi.common.utils.DeviceTypeUtils;
import sunmi.common.utils.NetworkUtils;
import sunmi.common.utils.StatusBarUtils;
import sunmi.common.utils.Utils;
import sunmi.common.utils.VolumeHelper;
import sunmi.common.utils.WebViewParamsUtils;
import sunmi.common.view.TitleBarView;
import sunmi.common.view.dialog.BottomDialog;

/**
 * Description:
 * Created by bruce on 2019/12/3.
 */
@EActivity(resName = "activity_sdcard_playback")
public class SDCardPlayBackActivity extends BaseMvpActivity<SDCardPlaybackPresenter>
        implements SDCardPlaybackContract.View, ZFTimeLine.OnZFTimeLineListener,
        View.OnClickListener, VolumeHelper.VolumeChangeListener, SurfaceHolder.Callback,
        P2pService.OnPlayStatusChangedListener, P2pService.OnPlayingListener {

    private static final int PLAY_FAIL_STATUS_COMMON = 0;
    private static final int PLAY_FAIL_STATUS_OFFLINE = 1;
    private static final int PLAY_FAIL_STATUS_NO_SD = 2;
    private static final int PLAY_FAIL_STATUS_SD_EXCEPTION = 3;
    private static final int PLAY_FAIL_STATUS_NET_EXCEPTION = 4;
    private static final int PLAY_FAIL_STATUS_GET_SD_TIMEOUT = 5;

    private final static long SECONDS_IN_ONE_DAY = 24 * 60 * 60;

    @ViewById(resName = "rl_screen")
    LinearLayout rlScreen;
    @ViewById(resName = "title_bar")
    TitleBarView titleBar;
    @ViewById(resName = "sv_playback")
    SurfaceView svPlayback;
    @ViewById(resName = "rl_top")
    RelativeLayout rlTopBar;
    @ViewById(resName = "rl_bottom_playback")
    RelativeLayout rlBottomBar;
    @ViewById(resName = "iv_mute")
    ImageView ivMute;//音量
    @ViewById(resName = "cm_timer")
    Chronometer cmTimer;//录制时间
    @ViewById(resName = "rl_record")
    RelativeLayout rlRecord;
    @ViewById(resName = "iv_pause")
    ImageView ivPlay;//开始播放
    @ViewById(resName = "iv_full_screen")
    ImageView ivFullScreen;
    @ViewById(resName = "tv_time_scroll")
    TextView tvTimeScroll;
    @ViewById(resName = "rl_video")
    RelativeLayout rlVideo;
    @ViewById(resName = "ll_play_fail")
    LinearLayout llPlayFail;
    @ViewById(resName = "tv_play_fail")
    TextView tvPlayFail;
    @ViewById(resName = "btn_retry")
    Button btnRetry;
    @ViewById(resName = "ll_no_service")
    LinearLayout llGotoCloudPlayback;
    @ViewById(resName = "tv_no_service")
    TextView tvGotoCloudPlayback;
    @ViewById(resName = "btn_open_service")
    Button btnGotoCloudPlayback;
    @ViewById(resName = "ll_portrait_controller_bar")
    LinearLayout llPortraitBar;
    @ViewById(resName = "iv_pre_day")
    ImageView ivPreDay;
    @ViewById(resName = "tv_calendar")
    TextView tvCalendar;
    @ViewById(resName = "iv_next_day")
    ImageView ivNextDay;
    @ViewById(resName = "time_line")
    ZFTimeLine timeLine;
    @ViewById(resName = "rl_loading")
    RelativeLayout rlLoading;

    @Extra
    SunmiDevice device;
    @Extra
    int cloudStorageServiceStatus;

    private CountDownTimer timeLineScrollTimer; //时间轴滑动延时

    private int screenW; //手机屏幕的宽
    private boolean isPaused;//回放是否暂停
    private boolean isStartRecord;//是否开始录制
    private boolean isControlPanelShow = true;//是否点击屏幕

    private boolean switchSuccess = true;//是否切换到新的时间开始播放

    //当前时间，已选日期的开始和结束时间  in seconds
    private long presentTime, startTimeCurrentDate, endTimeCurrentDate;
    private long lastVideoEndTime;    //已经在播放的视频结束时间

    private Handler handler = new Handler();
    private VolumeHelper volumeHelper = null;

    private List<VideoTimeSlotBean> timeSlotsInDay = new ArrayList<>();

    private List<VideoTimeSlotBean> timeSlotsAll;

    private Dialog calendarDialog;
    private VerticalCalendar calendarView;
    private Calendar calendarSelected;

    private P2pService p2pService;
    private boolean isBind;
    private boolean hasGetSdcardStatus;

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            isBind = true;
            P2pService.MyBinder myBinder = (P2pService.MyBinder) binder;
            p2pService = myBinder.getService();
            getSdcardStatus();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBind = false;
        }
    };

    private void getSdcardStatus() {
        new CountDownTimer(10_000, 2_000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                if (!hasGetSdcardStatus) {
                    hasGetSdcardStatus = true;
                    showPlayFail(PLAY_FAIL_STATUS_GET_SD_TIMEOUT, R.string.network_error);
                }
            }
        }.start();
        IPCCall.getInstance().getSdStatus(context, device.getModel(), device.getDeviceid());
        mPresenter.getPlaybackListForCalendar(getIOTCClient(),
                startTimeCurrentDate - SECONDS_IN_ONE_DAY * 730, endTimeCurrentDate);
    }

    @AfterViews
    void init() {
        mPresenter = new SDCardPlaybackPresenter();
        mPresenter.attachView(this);
        StatusBarUtils.setStatusBarColor(this, StatusBarUtils.TYPE_DARK);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//保持屏幕常亮
        showDarkLoading();
        ivMute.setEnabled(false);
        ivFullScreen.setEnabled(false);
        bindService(new Intent(context, P2pService.class)
                .putExtra("uid", device.getUid()), conn, BIND_AUTO_CREATE);
        initViews();
        initVolume();
        initData();
        svPlayback.getHolder().addCallback(this);
        switchOrientation(Configuration.ORIENTATION_PORTRAIT);
        llGotoCloudPlayback.setOnTouchListener((v, event) -> true);
        rlLoading.setOnTouchListener((v, event) -> true);
        llPlayFail.setOnTouchListener((v, event) -> true);
    }

    private void initViews() {
        titleBar.setAppTitle(device.getName());
        titleBar.getLeftLayout().setOnClickListener(this);
        rlBottomBar.setVisibility(View.VISIBLE);
        ivNextDay.setEnabled(false);
        ivPreDay.setEnabled(false);
        //初始化时间轴 设置时间轴每个小刻度5分钟，每个大刻度包含6个小刻度
        timeLine.setInterval(300, 6);
        timeLine.setListener(this);
    }

    void initData() {
        screenW = CommonHelper.getScreenWidth(context);
        presentTime = System.currentTimeMillis() / 1000;
        startTimeCurrentDate = DateTimeUtils.getDayStart(new Date()).getTime() / 1000;
        endTimeCurrentDate = presentTime;
        refreshDay();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isPaused) {
            pausePlayClick();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
        volumeHelper.unregisterVolumeReceiver();
        stopPlay();
        removeCallbacks();
    }

    @Override
    public void onBackPressed() {
        if (isPortrait()) {
            if (rlLoading != null && rlLoading.isShown()) {
                rlLoading.setVisibility(View.GONE);
                return;
            }
            stopPlay();
            if (p2pService != null) {
                p2pService.setNeedReinitialize(true);
            }
            finish();
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switchOrientation(newConfig.orientation);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_out_right);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (p2pService != null) {
            p2pService.startDecode();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    /**
     * 按键控制音量，return true时不显示系统音量 return false时显示系统音量
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            setVolumeViewImage(volumeHelper.get100CurrentVolume());
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ll_back_layout) {
            onBackPressed();
        }
    }

    @Click(resName = "btn_open_service")
    void openServiceClick() {
        if (isServiceUnopened()) {
            Router.withApi(SunmiServiceApi.class)
                    .goToWebViewCloud(context, CommonConstants.H5_CLOUD_STORAGE,
                            WebViewParamsUtils.getCloudStorageParams(device.getDeviceid(), ""));
        } else {
            CloudPlaybackActivity_.intent(context).device(device)
                    .cloudStorageServiceStatus(cloudStorageServiceStatus)
                    .start().withAnimation(R.anim.slide_in_right, 0);
        }
    }

    @Click(resName = "rl_top")
    void backClick() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Click(resName = "iv_full_screen")
    void fullScreenClick() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

//    视频录制
//    @Click(resName = "iv_record")
//    void recordClick() {
//        if (isStartRecord) {
//            ivRecord.setBackgroundResource(R.mipmap.ic_recording_normal);
//            rlRecord.setVisibility(View.GONE);
//            isStartRecord = false;
//            cmTimer.stop();//关闭录制
//        } else {
//            ivRecord.setBackgroundResource(R.mipmap.ic_recording);
//            rlRecord.setVisibility(View.VISIBLE);
//            isStartRecord = true;
//            startRecord();//开始录制
//        }
//    }

    //音量
    @Click(resName = "iv_mute")
    void volumeClick() {
        if (volumeHelper.isMute()) {
            setVolumeViewImage(1);
            volumeHelper.unMute();
        } else {
            volumeHelper.mute();
            setVolumeViewImage(0);
        }
    }

    //开始，暂停
    @Click(resName = "iv_pause")
    void pausePlayClick() {
        if (isFastClick(1000)) {
            return;
        }
        ivPlay.setBackgroundResource(isPaused ? R.mipmap.pause_normal : R.mipmap.play_normal);
        isPaused = !isPaused;
        p2pService.pausePlayback(isPaused);
    }

    //点击屏幕
    @Click(resName = "rl_video")
    void screenClick() {
        if (isPlayFailShown()) {
            return;
        }
        if (isControlPanelShow) {
            setPanelVisible(View.GONE);
            isControlPanelShow = false;
        } else {
            setPanelVisible(View.VISIBLE);
            isControlPanelShow = true;
        }
    }

    @Click(resName = "iv_pre_day")
    void preDayClick() {
        if (isFastClick(1000) || isServiceUnopened()) {
            return;
        }
        switchDay(startTimeCurrentDate - SECONDS_IN_ONE_DAY);
    }

    @Click(resName = "iv_next_day")
    void nextDayClick() {
        if (isFastClick(1000) || isServiceUnopened()) {
            return;
        }
        switchDay(startTimeCurrentDate + SECONDS_IN_ONE_DAY);
    }

    @Click(resName = "tv_calendar")
    void chooseCalendarClick() {
        if (isFastClick(1000) || isServiceUnopened()) {
            return;
        }
        if (timeSlotsAll == null) {
            return;
        }
        if (calendarDialog == null || calendarView == null) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.MONTH, -24);
            Config config = new Config.Builder()
                    .setMinDate(c)
                    .setPoint(getTimeSlotOfCalendar(timeSlotsAll))
                    .build();
            int height = (int) (Utils.getScreenHeight(context) * 0.85);
            calendarView = new VerticalCalendar(this, config);
            calendarView.setOnCalendarSelectListener(calendar -> calendarSelected = calendar);
            ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, height);
            calendarDialog = new BottomDialog.Builder(context)
                    .setTitle(R.string.str_title_calendar)
                    .setContent(calendarView, lp)
                    .setCancelButton(R.string.sm_cancel)
                    .setOkButton(R.string.str_confirm, (dialog, which) -> {
                        if (calendarSelected != null) {
                            switchDay(calendarSelected.getTimeInMillis() / 1000);
                        }
                    }).create();
        }
        calendarSelected = null;
        calendarView.setSelected(startTimeCurrentDate * 1000);
        calendarDialog.show();
    }

    @Click(resName = "btn_retry")
    void retryClick() {

    }

    private void switchDay(long currentDay) {
        switchSuccess = isPlayFailShown() || //播放失败或播放完成后重置状态
                timeSlotsInDay == null || timeSlotsInDay.isEmpty();//无视频切到有视频的天重置状态
        startTimeCurrentDate = currentDay;
        hidePlayFail();
        endTimeCurrentDate = startTimeCurrentDate + SECONDS_IN_ONE_DAY;
        refreshDay();
        updateCalendarBtnEnable();
        initTimeSlotData(false);
    }

    private void updateCalendarBtnEnable() {
        ivNextDay.setEnabled(endTimeCurrentDate <= System.currentTimeMillis() / 1000);
    }

    private boolean isPlayOver(long time) {
        return time >= lastVideoEndTime;
//        return lastVideoEndTime == endTimeCurrentDate
//                || time == endTimeCurrentDate //time slots的最后一段视频的结束时间可能比当天的0点大
//                || (timeSlotsInDay != null && timeSlotsInDay.size() > 0
//                && lastVideoEndTime >= timeSlotsInDay.get(timeSlotsInDay.size() - 1).getEndTime());
    }

    @Override
    public void getDeviceTimeSlotSuccess(List<VideoTimeSlotBean> slots) {
        if (slots != null && slots.size() > 0) {
            timeSlotsInDay.addAll(slots);
            mPresenter.getPlaybackList(getIOTCClient(),
                    slots.get(slots.size() - 1).getEndTime(), endTimeCurrentDate);
        } else {
            if (timeSlotsInDay.size() > 0) {
                refreshScaleTimePanel();
                lastVideoEndTime = timeSlotsInDay.get(timeSlotsInDay.size() - 1).getEndTime();
                selectedTimeHasVideo(getDefaultPlayTime());
            } else {
                showNoVideoTip();
            }
            hideVideoLoading();
        }
    }

    private long getDefaultPlayTime() {
        if (presentTime - startTimeCurrentDate < SECONDS_IN_ONE_DAY) {
            return timeSlotsInDay.get(timeSlotsInDay.size() - 1).getStartTime();
        } else {
            return startTimeCurrentDate;
        }
    }

    @Override
    public void startPlaybackSuccess() {
        hideLoading();
    }

    @Override
    public void getAllTimeSlotSuccess(List<VideoTimeSlotBean> slots) {
        timeSlotsAll = slots;
    }

    @Override
    public void didMoveToTime(long timeStamp) {
        hideTimeScroll();
        if (isPlayOver(timeStamp)) {
            playOver();
        } else if (!NetworkUtils.isNetworkAvailable(context)) {
            showPlayFail(PLAY_FAIL_STATUS_NET_EXCEPTION, R.string.network_error);
        } else {
            hidePlayFail();
            startDelayPlay(timeStamp);
        }
    }

    @Override
    public void moveTo(String data, boolean isLeftScroll, long timeStamp) {
        cancelDelayPlay();
        switchSuccess = false;
        showTimeScroll(data.substring(11), isLeftScroll);//toast显示时间
    }

    @Override
    public void onVolumeChanged(int volume) {
        setVolumeViewImage(volume);
    }

    @Override
    public void onPlayFail() {
        showPlayFail(PLAY_FAIL_STATUS_NET_EXCEPTION, R.string.network_error);
    }

    @Override
    public void onPlayStarted() {
        hideLoading();
        hidePlayFail();
    }

    @Override
    public void onPlayFinished() {
        if (switchSuccess) {
            playOver();
        }
    }

    @Override
    public int[] getStickNotificationId() {
        return new int[]{IpcConstants.ipcNameChanged, CommonNotifications.cloudStorageChange,
                OpcodeConstants.getSdStatus};
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        super.didReceivedNotification(id, args);
        if (id == IpcConstants.ipcNameChanged) {
            if (args != null) {
                SunmiDevice sd = (SunmiDevice) args[0];
                if (TextUtils.equals(sd.getDeviceid(), device.getDeviceid())) {
                    device.setName(sd.getName());
                    titleBar.setAppTitle(device.getName());
                }
            }
        } else if (id == CommonNotifications.cloudStorageChange) {
            cloudStorageServiceOpened();
        } else if (id == OpcodeConstants.getSdStatus) {
            if (hasGetSdcardStatus) return;
            hasGetSdcardStatus = true;
            ResponseBean res = (ResponseBean) args[0];
            switch (getSdcardStatus(res)) {
                case 2:
                    initTimeSlotData(true);
                    break;
                case 0:
                    showPlayFail(PLAY_FAIL_STATUS_NO_SD, R.string.tip_no_sd_to_cloud_playback);
                    break;
                default:
                    showPlayFail(PLAY_FAIL_STATUS_SD_EXCEPTION, R.string.tip_sd_exception_to_cloud_playback);
                    break;
            }
            p2pService.init(svPlayback.getHolder().getSurface(), this, this);
        }
    }

    @Override
    public void onPlaying(long time, int flag) {
        if (flag == 10) {//切换成功的返回
            switchSuccess = true;
            moveToTime(time);
        } else {
            if (switchSuccess) {
                if (timeLine.getCurrentInterval() + 60 < time) {
                    moveToTime(time);
                }
            }
        }
    }

    private IOTCClient getIOTCClient() {
        return p2pService.getIOTCClient();
    }

    private int getSdcardStatus(ResponseBean res) {
        int status = -1;
        if (res.getDataErrCode() == 1) {
            try {
                status = res.getResult().getInt("sd_status_code");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return status;
    }

    @UiThread
    public void showNoVideoTip() {
        timeLine.setVisibility(View.GONE);
        showPlayFail(PLAY_FAIL_STATUS_COMMON, R.string.tip_no_sd_video_current_day);
    }

    @UiThread
    void cloudStorageServiceOpened() {
        cloudStorageServiceStatus = CommonConstants.SERVICE_ALREADY_OPENED;
        btnGotoCloudPlayback.setText(R.string.str_view_cloup_playback);
    }

    @UiThread
    public void showPlayFail(int type, @StringRes int tipResId) {
        hideLoading();
        stopPlay();
        if (PLAY_FAIL_STATUS_OFFLINE == type) {
            showGotoCloudPlayback(tipResId);
        } else if (PLAY_FAIL_STATUS_NO_SD == type) {
            showGotoCloudPlayback(!CommonHelper.isGooglePlay() ? tipResId : R.string.tip_no_sd_to_cloud_playback_fs);
        } else if (PLAY_FAIL_STATUS_SD_EXCEPTION == type) {
            showGotoCloudPlayback(!CommonHelper.isGooglePlay() ? tipResId : R.string.tip_sd_exception_to_cloud_playback_fs);
        } else {
            btnRetry.setVisibility(PLAY_FAIL_STATUS_NET_EXCEPTION == type ? View.VISIBLE : View.GONE);
            tvPlayFail.setText(tipResId);
            llPlayFail.setVisibility(View.VISIBLE);
        }
    }

    private void showGotoCloudPlayback(@StringRes int tipResId) {
        if (!CommonHelper.isGooglePlay()) {
            btnGotoCloudPlayback.setVisibility(View.VISIBLE);
            btnGotoCloudPlayback.setText(isServiceUnopened()
                    ? R.string.str_open_cloud_storage : R.string.str_view_cloup_playback);
        }
        tvGotoCloudPlayback.setText(tipResId);
        llGotoCloudPlayback.setVisibility(View.VISIBLE);
    }

    private boolean isServiceUnopened() {
        return cloudStorageServiceStatus == CommonConstants.SERVICE_NOT_OPENED;
    }

    @UiThread
    public void hidePlayFail() {
        llGotoCloudPlayback.setVisibility(View.GONE);
        llPlayFail.setVisibility(View.GONE);
    }

    @UiThread
    public void showVideoLoading() {
        rlLoading.setVisibility(View.VISIBLE);
    }

    @UiThread
    public void hideVideoLoading() {
        rlLoading.setVisibility(View.GONE);
    }

    @UiThread
    public void hideLoading() {
        ivMute.setEnabled(true);
        ivFullScreen.setEnabled(true);
        hideLoadingDialog();
        hideVideoLoading();
    }

    private boolean isPortrait() {
        return getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    private void refreshDay() {
        tvCalendar.setText(DateTimeUtils.formatDateTime(new Date(startTimeCurrentDate * 1000)));
    }

    /**
     * 视频全屏切换
     */
    public void switchOrientation(int orientation) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
            setPortraitViewVisible(View.GONE);
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setPortraitViewVisible(View.VISIBLE);
            setPanelVisible(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//显示状态栏
        }
        setPanelVisible(View.VISIBLE);
        setVideoParams(orientation);
    }

    private void setPortraitViewVisible(int visibility) {
        ivFullScreen.setVisibility(visibility);
        titleBar.setVisibility(visibility);
        llPortraitBar.setVisibility(visibility);
    }

    /**
     * 设置SurfaceView的参数
     */
    public void setVideoParams(int orientation) {
        int videoW = screenW, videoH = screenW;
        ViewGroup.LayoutParams rlLP = rlVideo.getLayoutParams();
        ViewGroup.LayoutParams bottomBarLp = rlBottomBar.getLayoutParams();
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            rlBottomBar.setBackgroundResource(R.mipmap.bg_video_controller_bottom_h);
            bottomBarLp.height = CommonHelper.dp2px(context, 64);
            int screenH = CommonHelper.getScreenWidth(context);//横屏
            float aspectRatio = screenW / screenH;//宽高比
            videoW = screenH;
            if (isSS1()) {
                videoW = screenW;
            } else {
                if (aspectRatio > 16 / 9) {
                    videoW = videoH * 16 / 9;
                } else {
                    videoH = videoW * 9 / 16;
                }
            }
            rlLP.height = screenW;
            rlLP.width = screenH;
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            rlBottomBar.setBackgroundResource(R.mipmap.bg_video_controller_bottom_v);
            bottomBarLp.height = CommonHelper.dp2px(context, 48);
            if (isSS1()) {
                videoH = screenW;
            } else {
                videoH = screenW * 9 / 16;
            }
            rlLP.width = screenW;
            rlLP.height = videoH;
        }

        rlVideo.setLayoutParams(rlLP);
        rlBottomBar.setLayoutParams(bottomBarLp);

        ViewGroup.LayoutParams lpCloud = svPlayback.getLayoutParams();
        lpCloud.width = videoW;
        lpCloud.height = videoH;
        svPlayback.setLayoutParams(lpCloud);
    }

    private boolean isSS1() {
        return DeviceTypeUtils.getInstance().isSS1(device.getModel());
    }

    /*
     * 停止播放
     */
    private void stopPlay() {
        try {
            if (svPlayback != null) {
                svPlayback.setVisibility(View.GONE);
                stopRunning();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRunning() {
        if (p2pService != null) {
            p2pService.pausePlayback(true);
        }
    }

    @UiThread
    void setPanelVisible(int visible) {
        if (rlTopBar != null && rlBottomBar != null) {
            rlTopBar.setVisibility(isPortrait() ? View.GONE : visible);
            rlBottomBar.setVisibility(visible);
        }
    }

    //开始计时录制
    private void startRecord() {
        cmTimer.setOnChronometerTickListener(cArg -> {
            long time = System.currentTimeMillis() - cArg.getBase();
            Date d = new Date(time);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            cmTimer.setText(sdf.format(d));
        });
        cmTimer.setBase(System.currentTimeMillis());
        cmTimer.start();
    }

    /**
     * 调节音量
     */
    private void initVolume() {
        volumeHelper = new VolumeHelper(context);
        volumeHelper.setVolumeChangeListener(this);
        volumeHelper.registerVolumeReceiver();
        setVolumeViewImage(volumeHelper.get100CurrentVolume());
    }

    private void setVolumeViewImage(int currentVolume100) {
        ivMute.setImageResource(currentVolume100 == 0 ? R.drawable.ic_mute : R.drawable.ic_unmute);
    }

    @UiThread
    void moveToTime(long time) {
        if (p2pService != null && timeLine != null) {
            timeLine.moveToTime(time);
        }
    }

    @UiThread
    void refreshScaleTimePanel() {
        timeLine.setVisibility(View.VISIBLE);
        timeLine.setBound(startTimeCurrentDate, startTimeCurrentDate + SECONDS_IN_ONE_DAY,
                startTimeCurrentDate, endTimeCurrentDate);
        timeLine.setVideoData(timeSlotsInDay);
        timeLine.refresh();
    }

    //滑动回放定位的中间 position
    @UiThread
    void scrollCurrentPlayBackTime(long currentTimeMinutes) {
        ivPlay.setBackgroundResource(R.mipmap.pause_normal);
        isPaused = false;
        timeLine.moveToTime(currentTimeMinutes);
    }

    //拖动或选择的时间是否有video
    @Background
    void selectedTimeHasVideo(long currTime) {
        int apSize = timeSlotsInDay.size();
        if (apSize <= 0) {
            hideVideoLoading();
            return;
        }
        if (currTime >= endTimeCurrentDate) {
            playOver();
            return;
        }
        long slotStartTime, slotEndTime;
        for (int i = 0; i < apSize; i++) {
            slotStartTime = timeSlotsInDay.get(i).getStartTime();
            slotEndTime = timeSlotsInDay.get(i).getEndTime();

            if (currTime <= slotStartTime) {
                startPlayback(slotStartTime);
                scrollCurrentPlayBackTime(slotStartTime);
                return;
            } else if (currTime < slotEndTime) {
                startPlayback(currTime);
                scrollCurrentPlayBackTime(currTime);
                return;
            }
        }
        playOver();
    }

    @UiThread
    void startPlayback(long slotStartTime) {
        if (!svPlayback.isShown()) {
            svPlayback.setVisibility(View.VISIBLE);
        }
        if (timeSlotsInDay.size() > 0) {
            if (p2pService != null) {
                p2pService.setEndTime(lastVideoEndTime);
            }
            mPresenter.startPlayback(getIOTCClient(), slotStartTime, lastVideoEndTime);
        }
    }

    private boolean isPlayFailShown() {
        return llPlayFail != null && llPlayFail.isShown();
    }

    private void playOver() {
        if (isPlayFailShown()) {
            return;
        }
        showPlayFail(PLAY_FAIL_STATUS_COMMON, R.string.tip_video_played_over);
    }

    private void removeCallbacks() {
        handler.removeCallbacksAndMessages(null);
    }

    //初始化时间轴
    @UiThread
    void initTimeSlotData(boolean isFirstInit) {
        if (isFirstInit) {
            tvCalendar.setEnabled(true);
            ivPreDay.setEnabled(true);
        } else {
            showVideoLoading();
        }
        timeSlotsInDay.clear();
        timeLine.clearData();
        mPresenter.getPlaybackList(getIOTCClient(), startTimeCurrentDate, endTimeCurrentDate);
    }

    /**
     * 延时执行滑动处理，防止无视频区域直接跳过
     */
    private void startDelayPlay(long timeStamp) {
        cancelDelayPlay();
        timeLineScrollTimer = new CountDownTimer(500, 100) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                showVideoLoading();
                selectedTimeHasVideo(timeStamp);
            }
        };
        timeLineScrollTimer.start();
    }

    private void cancelDelayPlay() {
        if (timeLineScrollTimer != null) {
            timeLineScrollTimer.cancel();
            timeLineScrollTimer = null;
        }
    }

    @UiThread
    void showTimeScroll(final String time, final boolean isLeft) {
        if (TextUtils.isEmpty(time)) {
            return;
        }
        tvTimeScroll.setVisibility(View.VISIBLE);
        tvTimeScroll.setText(time);
        tvTimeScroll.setEnabled(isLeft);
    }

    @UiThread
    void hideTimeScroll() {
        handler.postDelayed(() -> tvTimeScroll.setVisibility(View.GONE), 500);
    }

    private List<Calendar> getTimeSlotOfCalendar(List<VideoTimeSlotBean> slots) {
        List<Calendar> result = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        for (VideoTimeSlotBean slot : slots) {
            long start = slot.getStartTime() * 1000;
            long end = slot.getEndTime() * 1000;
            c.clear();
            c.setTimeInMillis(start);
            while (c.getTimeInMillis() < end) {
                result.add((Calendar) c.clone());
                c.add(Calendar.DATE, 1);
            }
            c.setTimeInMillis(end);
            result.add((Calendar) c.clone());
        }
        return result;
    }

}
