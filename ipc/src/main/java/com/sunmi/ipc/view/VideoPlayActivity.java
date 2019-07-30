package com.sunmi.ipc.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.datelibrary.DatePickDialog;
import com.datelibrary.OnSureLisener;
import com.datelibrary.bean.DateType;
import com.google.gson.JsonObject;
import com.sunmi.ipc.R;
import com.sunmi.ipc.model.ApCloudTimeBean;
import com.sunmi.ipc.model.TimeBean;
import com.sunmi.ipc.model.VideoListResp;
import com.sunmi.ipc.rpc.IPCCall;
import com.sunmi.ipc.rpc.IPCCloudApi;
import com.sunmi.ipc.rpc.IpcConstants;
import com.sunmi.ipc.utils.AACDecoder;
import com.sunmi.ipc.utils.H264Decoder;
import com.sunmi.ipc.utils.IOTCClient;
import com.sunmi.ipc.utils.TimeView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import sunmi.common.base.BaseActivity;
import sunmi.common.rpc.retrofit.RetrofitCallback;
import sunmi.common.rpc.sunmicall.ResponseBean;
import sunmi.common.utils.CommonHelper;
import sunmi.common.utils.DateTimeUtils;
import sunmi.common.utils.DeviceTypeUtils;
import sunmi.common.utils.IVideoPlayer;
import sunmi.common.utils.VolumeHelper;
import sunmi.common.utils.log.LogCat;
import sunmi.common.view.VerticalSeekBar;

/**
 * Description:
 * Created by bruce on 2019/4/11.
 */
@EActivity(resName = "activity_video_play")
public class VideoPlayActivity extends BaseActivity
        implements SurfaceHolder.Callback, IOTCClient.Callback,
        SeekBar.OnSeekBarChangeListener, View.OnTouchListener, IVideoPlayer.VideoPlayListener {
    @ViewById(resName = "rl_screen")
    RelativeLayout rlScreen;
    @ViewById(resName = "vv_ipc")
    SurfaceView videoView;
    @ViewById(resName = "ivp_cloud")
    IVideoPlayer ivpCloud;
    @ViewById(resName = "rl_top")
    RelativeLayout rlTopBar;
    @ViewById(resName = "rl_bottom")
    RelativeLayout rlBottomBar;
    @ViewById(resName = "sBar_voice")
    VerticalSeekBar sBarVoice;//音量控制
    @ViewById(resName = "ll_change_volume")
    LinearLayout llChangeVolume;//音量控制
    @ViewById(resName = "iv_record")
    ImageView ivRecord;//录制
    @ViewById(resName = "iv_volume")
    ImageView ivVolume;//音量
    @ViewById(resName = "tv_quality")
    TextView tvQuality;//画质
    @ViewById(resName = "ll_video_quality")
    LinearLayout llVideoQuality;//是否显示画质
    @ViewById(resName = "tv_hd_quality")
    TextView tvHDQuality;//高清画质
    @ViewById(resName = "tv_sd_quality")
    TextView tvSDQuality;//标清画质
    @ViewById(resName = "cm_timer")
    Chronometer cmTimer;//录制时间
    @ViewById(resName = "rl_record")
    RelativeLayout rlRecord;
    @ViewById(resName = "tv_calender")
    TextView tvCalender;//日历
    @ViewById(resName = "iv_screenshot")
    ImageView ivScreenshot;//截图
    @ViewById(resName = "iv_live")
    ImageView ivLive;//直播
    @ViewById(resName = "iv_play")
    ImageView ivPlay;//开始播放
    @ViewById(resName = "ll_date_view")
    RelativeLayout llDateView;
    @ViewById(resName = "recyclerView")
    RecyclerView recyclerView;
    @ViewById(resName = "my_view")
    TimeView timeView;//时间绘制
    @ViewById(resName = "iv_setting")
    ImageView ivSetting;//设置
    @ViewById(resName = "sb_zoom")
    SeekBar sbZoom;

    @Extra
    String UID;
    @Extra
    String ipcType;
    @Extra
    int deviceId; //2237 2223     //设备id

    //手机屏幕的宽高
    private int screenW, screenH;
    private float aspectRatio;//宽高比

    private int currFocus;

    //获取recyclerView width
    private int rvWidth;

    private H264Decoder videoDecoder = null;
    private AACDecoder audioDecoder = null;
    private VolumeHelper volumeHelper = null;
    private LinearLayoutManager linearLayoutManager;

    private boolean isStartRecord;//是否开始录制
    private boolean isControlPanelShow = true;//是否点击屏幕
    private boolean isCloudPlayBack;//是否正在云回放
    private boolean isDevPlayBack;//是否正在设备回放
    private boolean isPaused;//回放是否暂停
    private boolean isCurrentLive;//当前是否直播
    private int qualityType = 0;//0-超清，1-高清

    //adapter
    private DateAdapter adapter;
    //日历
    private Calendar calendar;
    //选择视频日期列表
    private List<TimeBean> list = new ArrayList<>();
    //绘制的小时列表
    private List<String> dateList = new ArrayList<>();
    //当前时间 ，三天前秒数 ，区间总共秒数
    private long currentDateSeconds, threeDaysBeforeSeconds, minutesTotal;
    //3天秒数
    private long threeDaysSeconds = 3 * 24 * 60 * 60;
    //12小时后的秒数
    private int twelveHoursSeconds = 12 * 60 * 60;
    //10分钟
    private int tenMinutes = 10 * 60;
    //当前分钟走的秒数
    private int currentSecond;
    //刻度尺移动定时器
    private Timer moveTimer;
    //滑动停止的时间戳
    private long scrollTime;
    //当前的itemPosition
    private int currentItemPosition;
    //是否往左滑动
    private boolean isLeftScroll;
    //是否为选择的日期
    private boolean isSelectedDate;
    //是否自动滚动
    private boolean isAutoScroll;
    //视频片段是否小于一分钟
    private boolean isVideoLess1Minute;
    private Handler handler = new Handler();

    //存放所有视频端的url
    private List<VideoListResp.VideoBean> videoListQueue = new ArrayList<>();

    //屏幕控件自动隐藏计时器
    private Timer screenHideTimer = null;
    private TimerTask screenHideTimerTask = null;
    private int countdown;

    //重置倒计时
    private void resetCountdown() {
        countdown = 0;
    }

    //开启计时
    private void startScreenHideTimer() {
        stopScreenHideTimer();
        screenHideTimer = new Timer();
        screenHideTimer.schedule(screenHideTimerTask = new TimerTask() {
            @Override
            public void run() {
                countdown++;
                if (countdown == 8) {
                    hideControlBar();
                    isControlPanelShow = false;
                    stopScreenHideTimer();
                }
            }
        }, 0, 1000);
    }

    // 停止计时
    private void stopScreenHideTimer() {
        resetCountdown();
        if (screenHideTimer != null) {
            screenHideTimer.cancel();
            screenHideTimer = null;
        }
        if (screenHideTimerTask != null) {
            screenHideTimerTask.cancel();
            screenHideTimerTask = null;
        }
    }

    @AfterViews
    void init() {
//        deviceId = 2239;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
        initData();
        sbZoom.setOnSeekBarChangeListener(this);
        rlScreen.setOnTouchListener(this);
        initVolume();
        initSurfaceView();
        setTextViewTimeDrawable();
        initRecyclerView();
        recyclerViewAddOnScrollListener();
        showTimeList(false, listAp);
        scrollCurrentTime(); //滚动到当前时间
    }

    void initData() {
        screenW = CommonHelper.getScreenWidth(context);
        screenH = CommonHelper.getScreenHeight(context);
        aspectRatio = screenW / screenH;
        //当前天
        calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        tvCalender.setText(day > 9 ? day + "" : "0" + day);
        //当前时间秒数
        currentDateSeconds = System.currentTimeMillis() / 1000;
        //三天前秒数
        threeDaysBeforeSeconds = currentDateSeconds - threeDaysSeconds;
        //区间总共秒数 --当前时间前三天+未来12小时的秒数
        minutesTotal = threeDaysSeconds + twelveHoursSeconds;
        //当前分钟走的秒数
        currentSecond = calendar.get(Calendar.SECOND);

        //直播回调
        IOTCClient.setCallback(this);
        audioDecoder = new AACDecoder();
    }

    private void initSurfaceView() {
        int width, height;
        if (isSS1()) {
            width = height = screenH;
        } else {
            if (aspectRatio > 1920 / 1080) {
                height = screenH;
                width = 1920;
            } else {
                width = screenW;
                height = 1080;
            }
        }
        ViewGroup.LayoutParams lp = videoView.getLayoutParams();
        lp.width = width;
        lp.height = height;
        videoView.setLayoutParams(lp);
        //, shCloud;
        SurfaceHolder surfaceHolder = videoView.getHolder();
        surfaceHolder.addCallback(this); // 因为这个类实现了SurfaceHolder.Callback接口，所以回调参数直接this

        ViewGroup.LayoutParams lpCloud = ivpCloud.getLayoutParams();
        lpCloud.width = width;
        lpCloud.height = height;
        ivpCloud.setLayoutParams(lpCloud);
        ivpCloud.setVideoPlayListener(this);
    }

    private boolean isSS1() {
        return DeviceTypeUtils.getInstance().isSS1(ipcType);
    }

    @Override
    protected boolean needLandscape() {
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        cloudPlayDestroy();//关闭云端视频
        IOTCClient.close();
        if (videoDecoder != null) {
            videoDecoder.release();
        }
        if (audioDecoder != null) {
            audioDecoder.stop();
        }
    }

    //开始直播
    @Background
    void initP2pLive() {
        IOTCClient.init(UID);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        IPCCall.getInstance().fsZoom(seekBar.getProgress(), context);
    }

    @Click(resName = "tv_add")
    void addClick() {
        if (currFocus > 775) {
            shortTip("已到最大焦距");
            return;
        }
        IPCCall.getInstance().fsFocus(currFocus += 5, context);
    }

    @Click(resName = "tv_minus")
    void minusClick() {
        if (currFocus < 5) {
            shortTip("已到最小焦距");
            return;
        }
        IPCCall.getInstance().fsFocus(currFocus -= 5, context);
    }

    @Click(resName = "tv_auto")
    void autoFocusClick() {
        IPCCall.getInstance().fsAutoFocus(context);
    }

    @Click(resName = "btn_next")
    void nextClick() {
        LogCat.e(TAG, "addClick");
        if (screenH <= 0 || screenW <= 0) {
            shortTip("已到最小焦距");
            return;
        }
        //        IPCCall.getInstance().fsReset(context);
        IPCCall.getInstance().fsIrMode(0, context);
        float currX = 540;
        shortTip("x = " + (int) currX * 100 / screenW);
        float currY = 860;
        shortTip("y = " + (int) currY * 100 / screenH);
        IPCCall.getInstance().fsSetFocusPoint((int) currX * 100 / screenW,
                (int) currY * 100 / screenH, context);
    }

    @Click(resName = "rl_video_back")
    void backClick() {
        onBackPressed();
    }

    //视频录制
    @Click(resName = "iv_record")
    void recordClick() {
        if (isStartRecord) {
            ivRecord.setBackgroundResource(R.mipmap.ic_recording_normal);
            rlRecord.setVisibility(View.GONE);
            isStartRecord = false;
            cmTimer.stop();//关闭录制
        } else {
            ivRecord.setBackgroundResource(R.mipmap.ic_recording);
            rlRecord.setVisibility(View.VISIBLE);
            isStartRecord = true;
            startRecord();//开始录制
        }
    }

    //音量
    @Click(resName = "iv_volume")
    void volumeClick() {
        if (llChangeVolume.isShown()) {
            llChangeVolume.setVisibility(View.GONE);
        } else {
            llChangeVolume.setVisibility(View.VISIBLE);
            int currentVolume100 = volumeHelper.get100CurrentVolume();//获取当前音量
            sBarVoice.setProgress(currentVolume100);
            if (currentVolume100 == 0) {
                ivVolume.setBackgroundResource(R.mipmap.ic_muse);
            } else {
                ivVolume.setBackgroundResource(R.mipmap.ic_volume);
            }
        }
    }

    //画质
    @Click(resName = "tv_quality")
    void qualityClick() {
        if (isDevPlayBack) return;
        llVideoQuality.setVisibility(llVideoQuality.isShown() ? View.GONE : View.VISIBLE);
        if (qualityType == 0) {
            tvHDQuality.setTextColor(getResources().getColor(R.color.colorOrange));
            tvSDQuality.setTextColor(getResources().getColor(R.color.c_white));
        } else {
            tvHDQuality.setTextColor(getResources().getColor(R.color.c_white));
            tvSDQuality.setTextColor(getResources().getColor(R.color.colorOrange));
        }
    }

    //超清画质
    @Click(resName = "tv_hd_quality")
    void hdQualityClick() {
        tvQuality.setText(R.string.str_FHD);
        changeQuality(0);
    }

    //高清画质
    @Click(resName = "tv_sd_quality")
    void sdQualityClick() {
        tvQuality.setText(R.string.str_HD);
        changeQuality(1);
    }

    //开始，暂停
    @Click(resName = "iv_play")
    void playLiveClick() {
        if (!isDevPlayBack && !isCloudPlayBack && isCurrentLive) return;
        if (isFastClick(1000)) return;
        ivPlay.setBackgroundResource(isPaused ? R.mipmap.pause_normal : R.mipmap.play_normal);
        isPaused = !isPaused;
        if (isDevPlayBack) {
            IOTCClient.pausePlayback(isPaused);
        } else if (isCloudPlayBack) {
            if (isPaused) {
                ivpCloud.pause();
            } else {
                ivpCloud.play();
            }
        }
    }

    //直播
    @Click(resName = "iv_live")
    void playApBackClick() {
        switch2Live();
    }

    //显示日历
    @Click(resName = "tv_calender")
    void calenderClick() {
        if (isFastClick(1000)) return;
        DatePickDialog dialog = new DatePickDialog(this);
        if (scrollTime > 0) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String d = format.format(scrollTime);
            try {
                Date date = format.parse(d);
                dialog.setStartDate(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        //设置上下年分限制
        dialog.setYearLimt(100);
        //设置标题
        dialog.setTitle(R.string.str_select_time);
        //设置类型
        dialog.setType(DateType.TYPE_YMD);
        //设置消息体的显示格式，日期格式
        dialog.setMessageFormat("yyyy-MM-dd");
        //设置选择回调
        dialog.setOnChangeLisener(null);
        //设置点击确定按钮回调
        dialog.setOnSureLisener(new OnSureLisener() {
            @Override
            public void onSure(Date date) {
                onSureButton(date);
            }
        });
        dialog.show();
    }

    //点击屏幕
    @Click(resName = "rl_screen")
    void screenClick() {
        if (isControlPanelShow) {
            hideControlBar();
            isControlPanelShow = false;
        } else {
            rlTopBar.setVisibility(View.VISIBLE);
            rlBottomBar.setVisibility(View.VISIBLE);
            isControlPanelShow = true;
        }
    }

    @UiThread
    void hideControlBar() {
        rlTopBar.setVisibility(View.GONE);
        rlBottomBar.setVisibility(View.GONE);
        llChangeVolume.setVisibility(View.GONE);//音量
        llVideoQuality.setVisibility(View.GONE);//画质
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!isControlPanelShow) startScreenHideTimer();
                break;
        }
        return false;
    }

    /**
     * 切回直播
     */
    private void switch2Live() {
        isCurrentLive = true; //直播
        ivPlay.setBackgroundResource(R.mipmap.play_disable);
        ivLive.setVisibility(View.GONE);
        if (!isCloudPlayBack && !isDevPlayBack) {
            LogCat.e(TAG, "6666666 switch2Live");
            scrollCurrentLive();
            return;
        }
        showLoadingDialog();
        //如果是云端回放此时需要调用停止操作然后直播
        if (isCloudPlayBack) {
            cloudPlayDestroy();
            ivpCloud.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);
            isCloudPlayBack = false;
        }
        IOTCClient.startPlay();
        scrollCurrentLive();
        isDevPlayBack = false;
        hideLoadingDialog();
    }

    /**
     * 切到设备回放
     */
    void switch2DevPlayback(long start) {
        showLoadingDialog();
        if (isCloudPlayBack) {
            LogCat.e(TAG, "6666666 switch2DevPlayback");
            cloudPlayDestroy();
            ivpCloud.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);
            isCloudPlayBack = false;
        }
        IOTCClient.startPlayback(start);
        isCurrentLive = false;
        isDevPlayBack = true;
        ivLive.setVisibility(View.VISIBLE);
        hideLoadingDialog();
    }

    void switch2CloudPlayback(long start, long end) {
        showLoadingDialog();
        if (!isCloudPlayBack) {
            if (isDevPlayBack) {
                IOTCClient.stopPlayback();//先停止设备回放
                isDevPlayBack = false;
            } else {
                IOTCClient.stopLive();//先停止直播
            }
            LogCat.e(TAG, "6666666 switch2CloudPlayback");
            ivpCloud.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.GONE);
            isCloudPlayBack = true;
        }
        isCurrentLive = false;
        ivLive.setVisibility(View.VISIBLE);
        getCloudVideoUrls(start, end);
    }

    //点击屏幕
    @Click(resName = "test_cloud_back")
    void cloudClick() {
        switch2CloudPlayback(1560096000, 1560100000);
    }

    //开始计时录制
    private void startRecord() {
        cmTimer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer cArg) {
                long time = System.currentTimeMillis() - cArg.getBase();
                Date d = new Date(time);
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                cmTimer.setText(sdf.format(d));
            }
        });
        cmTimer.setBase(System.currentTimeMillis());
        cmTimer.start();
    }

    private void changeQuality(int type) {
        llVideoQuality.setVisibility(View.GONE);
        if (type == qualityType) return;
        qualityType = qualityType == 0 ? 1 : 0;
        IOTCClient.changeValue(qualityType);
        if (qualityType == 0) {
            shortTip(R.string.tip_video_quality_fhd);
        } else if (qualityType == 1) {
            shortTip(R.string.tip_video_quality_hd);
        }
    }

    //*********************************************************************
    //***********************云端回放***************************************
    //*********************************************************************

    private void getCloudVideoUrls(long start, long end) {
        if (deviceId <= 0) {
            shortTip("设备信息不完整");
            return;
        }
        IPCCloudApi.getVideoList(deviceId, start, end, new RetrofitCallback<VideoListResp>() {
            @Override
            public void onSuccess(int code, String msg, VideoListResp data) {
                videoListQueue.clear();
                videoListQueue = data.getVideo_list();
                List<String> urlList = new ArrayList<>();
                for (VideoListResp.VideoBean bean : data.getVideo_list()) {
                    urlList.add(bean.getUrl());
                }
                cloudPlay(urlList);
            }

            @Override
            public void onFail(int code, String msg, VideoListResp data) {
                hideLoadingDialog();
            }
        });
    }

    private void cloudPlay(List<String> urlList) {
        ivpCloud.setUrlQueue(urlList);
        try {
            ivpCloud.startPlay();
        } catch (Exception e) {
            shortTip("播放失败");
            e.printStackTrace();
        }
    }

    /*
     * 负责界面销毁时，release各个mediaPlayer
     */
    private void cloudPlayDestroy() {
        try {
            if (ivpCloud != null) {
                ivpCloud.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //***********************云端回放***************************************!
    //*********************************************************************

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        videoDecoder = new H264Decoder(holder.getSurface(), 0);
        initP2pLive();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //关闭操作
        if (videoDecoder != null) {
            videoDecoder.stopRunning();
            videoDecoder = null;
        }
    }

    @Override
    public void onVideoReceived(byte[] videoBuffer) {
        if (videoDecoder != null)
            videoDecoder.setVideoData(videoBuffer);
    }

    @Override
    public void onAudioReceived(byte[] audioBuffer) {
        audioDecoder.setAudioData(audioBuffer);
    }


    @Override
    public int[] getStickNotificationId() {
        return new int[]{IpcConstants.fsAutoFocus, IpcConstants.fsFocus, IpcConstants.fsGetStatus,
                IpcConstants.fsIrMode, IpcConstants.fsReset, IpcConstants.fsZoom};
    }

    @UiThread
    void setSeekBarProgress(int progress) {
        sbZoom.setProgress(progress);
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (args == null) return;
        ResponseBean res = (ResponseBean) args[0];
        if (res == null) return;
        if (id == IpcConstants.fsGetStatus) {
            //{"data":[{"opcode":"0x3109","result":{"zoom":0,"max_zoom":500,"max_focus":780,
            // "irmode":0,"auto_focus_start":0,"focus":389},"errcode":1}],"msg_id":"11111","errcode":1}
            try {
                JSONObject jsonObject = res.getResult();
                if (jsonObject.has("zoom")) {
                    int currZoom = jsonObject.getInt("zoom");
                    setSeekBarProgress(currZoom);
                }
                if (jsonObject.has("focus")) {
                    currFocus = jsonObject.getInt("focus");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (id == IpcConstants.fsAutoFocus) {
            LogCat.e(TAG, "666666,222res = " + res.toString());
        } else if (id == IpcConstants.fsReset) {
        }
    }

    /**
     * 调节音量
     */
    private void initVolume() {
        volumeHelper = new VolumeHelper(this);
        int currentVolume100 = volumeHelper.get100CurrentVolume();
        sBarVoice.setMax(100);
        sBarVoice.setProgress(currentVolume100);
        sBarVoice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    ivVolume.setBackgroundResource(R.mipmap.ic_muse);
                } else {
                    ivVolume.setBackgroundResource(R.mipmap.ic_volume);
                }
                volumeHelper.setVoice100(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        if (currentVolume100 == 0) {
            ivVolume.setBackgroundResource(R.mipmap.ic_muse);
        } else {
            ivVolume.setBackgroundResource(R.mipmap.ic_volume);
        }
    }

    //按键控制音量，return true时不显示系统音量 return false时显示系统音量
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            llChangeVolume.setVisibility(View.GONE);
            //获取当前音量
            int currentVolume100 = volumeHelper.get100CurrentVolume();
            if (currentVolume100 == 0) {
                ivVolume.setBackgroundResource(R.mipmap.ic_muse);
            } else {
                ivVolume.setBackgroundResource(R.mipmap.ic_volume);
            }
            return false;
        } else return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeCallbacks();
        closeMove();//关闭时间抽的timer
        stopScreenHideTimer();//关闭屏幕控件自动hide计时器
    }

    /**
     * *******************时间滑动条***************************
     */

    //开始移动
    public void openMove() {
        if (moveTimer != null) {
            moveTimer.cancel();
            moveTimer = null;
        }
        moveTimer = new Timer();
        moveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                moveTo();
            }
        }, 0, 1000 * 60);//一分钟轮询一次
    }

    @UiThread
    void moveTo() {
        isAutoScroll = true;
        int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
        if (firstVisibleItem < 0) return;
        linearLayoutManager.scrollToPositionWithOffset(currentItemPosition++, 0);
        canvasHours(firstVisibleItem); //绘制时间点和偏移量
    }

    //结束移动
    public void closeMove() {
        if (moveTimer != null) {
            moveTimer.cancel();
            moveTimer = null;
        }
    }

    //日期列表，是否选择日期列表
    private void timeList(List<TimeBean> list, boolean isSelectedDate) {
        list.clear();
        TimeBean bean;
        if (isSelectedDate) {//选择日期
            for (int i = 0; i < minutesTotal; i += 60) {
                bean = new TimeBean();
                bean.setDate(threeDaysBeforeSeconds + i);
                list.add(bean);
            }
        } else {//当前日期
            for (int i = 0; i < minutesTotal; i += 60) {//10分钟一个item
                bean = new TimeBean();
                bean.setDate(threeDaysBeforeSeconds - currentSecond + i);
                list.add(bean);
            }
        }
    }

    /**
     * 时间列表
     *
     * @param isSelectedDate 是否选择日期列表
     */
    @UiThread
    void showTimeList(boolean isSelectedDate, List<ApCloudTimeBean> apCloudList) {
        //添加list
        timeList(list, isSelectedDate);
        //日历DateAdapter
        adapter = new DateAdapter(context, list, apCloudList);
        recyclerView.setAdapter(adapter);

        //滑动到选择日期的0.00点
        if (isSelectedDate) scrollSelectedDate0AM();

        //开启控件隐藏倒计时
        startScreenHideTimer();
    }

    @UiThread
    void initDateList() {
        llDateView.setVisibility(View.VISIBLE);
    }

    //渲染时间轴并滚动到指定时间
    @UiThread
    void timeCanvasList(final List<ApCloudTimeBean> apCloudList) {
        adapter = new DateAdapter(context, list, apCloudList);
        recyclerView.setAdapter(adapter);
        if (!isFirstScroll && !isSelectedDate) {
            LogCat.e(TAG, "888888 time 11");
            selectedTimeIsHaveVideo(firstLeftScrollCurrentTime); //初始化左滑渲染及回放
        } else {
            if (isSelectedDate) {
                scrollSelectedDate0AM();  //滑动到选择日期的0.00点
            } else {
                scrollCurrentTime(); //滚动到当前时间
            }
        }
    }

    //选择日历日期回调
    @SuppressLint("DefaultLocale")
    public void onSureButton(Date date) {
        resetCountdown();//重置隐藏控件计时
        long currentTime = System.currentTimeMillis() / 1000;//当前时间戳秒
        scrollTime = date.getTime();//选择日期的时间戳毫秒
        long time = scrollTime / 1000; //设置日期的秒数
        if (time > currentTime) {//未来时间或当前--滑动当前直播
            isSelectedDate = false;
            scrollTime = System.currentTimeMillis();
            int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            tvCalender.setText(day > 9 ? day + "" : "0" + day);
            switch2Live();
        } else {//回放时间
            isFirstScroll = false;//非首次滑动
            isSelectedDate = true;
            isCurrentLive = false; //回放
            isAutoScroll = false;//非自动滚动
            ivPlay.setBackgroundResource(R.mipmap.pause_normal);
            ivLive.setVisibility(View.VISIBLE);

            String strDate = DateTimeUtils.secondToDate(time, "yyyy-MM-dd");
            int year = Integer.valueOf(strDate.substring(0, 4));
            int month = Integer.valueOf(strDate.substring(5, 7));
            int day = Integer.valueOf(strDate.substring(8, 10));
            //显示日历天数
            tvCalender.setText(String.format("%td%n", date));

            //设置选择日期的年月日0时0分0秒
            calendar.clear();
            calendar.set(year, month - 1, day, 0, 0, 0);//设置时候月份减1即是当月
            long selectedDate = calendar.getTimeInMillis() / 1000;//设置日期的秒数
            //当前时间秒数
            currentDateSeconds = System.currentTimeMillis() / 1000;
            //选择日期三天前的秒数
            threeDaysBeforeSeconds = selectedDate - threeDaysSeconds;
            //区间总共秒数
            minutesTotal = currentDateSeconds - selectedDate + threeDaysSeconds + twelveHoursSeconds;
            //加载时间轴无渲染
            listAp.clear();
            showTimeList(true, null);
            //滑动到选择日期的0.00点
            //scrollSelectedDate0AM();
            refreshCanvasList();//渲染
        }
    }

    private void initRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        //获取RecyclerView Width
        ViewTreeObserver vto = recyclerView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                rvWidth = recyclerView.getWidth();
            }
        });
    }

    //中间距离左侧屏幕的分钟
    private long leftToCenterMinutes() {
        //时间轴的一半除去px2dp的比例
        //return CommonHelper.px2dp(this, rvWidth / 2);
        return rvWidth / 2 / getResources().getDimensionPixelSize(R.dimen.dp_1);
    }

    //滑动选择日期的0点
    private void scrollSelectedDate0AM() {
        long threeDaysBeforeDate = 3 * 24 * 60;//3天分钟数
        currentItemPosition = (int) (threeDaysBeforeDate - leftToCenterMinutes());
        linearLayoutManager.scrollToPositionWithOffset(currentItemPosition, 0);
        openMove();
    }

    //获取视频跳转播放的currentItemPosition
    private void videoSkipScrollPosition(long currentTimeMinutes) {
        isAutoScroll = true;
        currentItemPosition = (int) (currentTimeMinutes / 60 - threeDaysBeforeSeconds / 60 - leftToCenterMinutes());
        linearLayoutManager.scrollToPositionWithOffset(currentItemPosition, 0);
    }

    //滑动回放定位的中间 position
    private void scrollCurrentPlayBackTime(long currentTimeMinutes) {
        currentItemPosition = (int) (currentTimeMinutes / 60 - threeDaysBeforeSeconds / 60 - leftToCenterMinutes());
        linearLayoutManager.scrollToPositionWithOffset(currentItemPosition, 0);
        openMove();
    }

    //直播boolean状态
    private void setPlayBooleanStatus() {
        isDevPlayBack = false;//dev
        isCloudPlayBack = false;//cloud
        isCurrentLive = true; //直播
    }

    //初始化延时滑动当前时间
    private void scrollCurrentTime() {
        setPlayBooleanStatus();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //中间距离左侧屏幕的分钟
                long currentMinutes = (minutesTotal - twelveHoursSeconds) / 60 - leftToCenterMinutes();//初始化无偏移量
                currentItemPosition = (int) currentMinutes;//当前的item
                //linearLayoutManager.scrollToPositionWithOffset(currentItemPosition, 0);
                openMove();
            }
        }, 500);
    }

    /**
     * 点击直播按钮滑动到当前时间
     */
    private void scrollCurrentLive() {
        setPlayBooleanStatus();
        //当前时间秒数
        long nowMinute = System.currentTimeMillis() / 1000;
        //初始化当前的秒数和现在的秒数时间戳对比相差的偏移量--比对分钟数
        long offsetMinutes = nowMinute / 60 - currentDateSeconds / 60;
        //中间距离左侧屏幕的分钟
        long currentMinutes = (minutesTotal - twelveHoursSeconds) / 60 - leftToCenterMinutes() + offsetMinutes;//点击直播+偏移量offsetMinutes
        currentItemPosition = (int) currentMinutes;//当前的item
        //linearLayoutManager.scrollToPositionWithOffset(currentItemPosition, 0);
        openMove();
    }

    private void rightNowScrollCurrentPosition(long currentTimeSecond) {
        ivLive.setVisibility(View.GONE);
        //当前时间秒数
        //long currentTimeSecond = System.currentTimeMillis() / 1000;
        //初始化当前的秒数和现在的秒数时间戳对比相差的偏移量--比对分钟数
        long offsetMinutes = currentTimeSecond / 60 - currentDateSeconds / 60;
        long currentMinutes = (minutesTotal - twelveHoursSeconds) / 60 - leftToCenterMinutes() + offsetMinutes;//点击直播+偏移量offsetMinutes
        currentItemPosition = (int) currentMinutes;//当前的item
        linearLayoutManager.scrollToPositionWithOffset(currentItemPosition, 0);
    }

    //拖动或选择的时间是否有video（ap或cloud）
    private void selectedTimeIsHaveVideo(long currTime) {
        int apSize = listAp.size();
        if (apSize == 0) {
            switch2Live();//跳转直播
            return;
        }
        long mStartTime = threeDaysBeforeSeconds, mEndTime = currentDateSeconds;
        for (int i = 0; i < apSize + 1; i++) {
            long startOpposite = 0, endOpposite = 0, start = 0, end = 0;
            //不包含ap时间轴内的时间
            if (i == 0) {
                startOpposite = mStartTime;
                endOpposite = listAp.get(i).getStartTime();
            } else if (i < apSize) {
                startOpposite = listAp.get(i - 1).getEndTime();
                endOpposite = listAp.get(i).getStartTime();
            } else if (i == apSize) {
                startOpposite = listAp.get(i - 1).getEndTime();
                endOpposite = mEndTime;
            }
            //包含ap时间内
            if (i < apSize) {
                start = listAp.get(i).getStartTime();
                end = listAp.get(i).getEndTime();
            }
            if (currTime >= startOpposite && currTime < endOpposite) {//空白区域
                if (i == apSize) {//最后一个无视频区域跳转直播
                    switch2Live();
                    return;
                }
                boolean isCloud = !listAp.get(i).isApPlay();
                //当前的视频片段是否小于一分钟
                isVideoLess1Minute = listAp.get(i).getEndTime() - listAp.get(i).getStartTime() <= 60;
                if (isCloud) {
                    switch2CloudPlayback(endOpposite, endOpposite + tenMinutes);
                } else {
                    switch2DevPlayback(endOpposite);
                }
                scrollCurrentPlayBackTime(endOpposite);//回放到拖动的时间点
                break;
            } else if (currTime >= start && currTime < end) {//视频区域
                boolean isCloud = !listAp.get(i).isApPlay();
                //当前的视频片段是否小于一分钟
                isVideoLess1Minute = listAp.get(i).getEndTime() - currTime <= 60;
                if (isCloud) {
                    switch2CloudPlayback(currTime, currTime + tenMinutes);
                } else {
                    switch2DevPlayback(currTime);
                }
                scrollCurrentPlayBackTime(currTime);//回放到拖动的时间点
                break;
            }
        }
    }

    //非第一次滑动停止
    private boolean isFirstScroll = true;
    //第一次向左滑动的中心点时间
    private long firstLeftScrollCurrentTime;

    //可视时间轴中间对应的时间
    private long centerCurrentTime(int firstVisibleItem) {
        int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
        int center = (lastVisibleItem - firstVisibleItem) / 2 + firstVisibleItem + 1;
        TimeBean bs = list.get(center);
        return bs.getDate();
    }

    private void removeCallbacks() {
        handler.removeCallbacksAndMessages(null);
    }

    //recyclerView 滑动监听
    private void recyclerViewAddOnScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {//停止滑动
                    LogCat.e("TAG", "onScrolled00 " + ", isCloudPlayBack=" + isCloudPlayBack + " ,isDevPlayBack=" + isDevPlayBack);
                    resetCountdown();//重置隐藏控件计时
                    //center date
                    int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
                    long currTime = centerCurrentTime(firstVisibleItem);//当前中间轴时间
                    //首次向左滑动请求+渲染
                    if (isFirstScroll && isLeftScroll) {
                        firstLeftScrollCurrentTime = currTime;
                        isFirstScroll = false;
                        isCurrentLive = false; //回放
                        refreshCanvasList();//渲染
                        return;
                    }
                    String strDate = DateTimeUtils.secondToDate(currTime, "yyyy-MM-dd HH:mm:ss");
                    String day = strDate.substring(8, 11);
                    tvCalender.setText(String.format(" %s", day));  //滑动停止显示日期
                    scrollTime = currTime * 1000;//滑动日历的时间戳毫秒
                    String hourMinuteSecond = strDate.substring(11, strDate.length());
                    canvasHours(firstVisibleItem);//绘制时间轴
                    long currentSeconds = System.currentTimeMillis() / 1000;//当前时间戳秒
                    //停止到未来时间
                    if (currTime > currentSeconds && currTime - currentSeconds > 1) {
                        switch2Live();  //滚动到当前直播
                        return;
                    }
                    toastForShort(VideoPlayActivity.this, hourMinuteSecond, isLeftScroll);//toast显示时间
                    //无回放视频跳转当前
                    if (listAp == null || listAp.size() == 0) {
                        rightNowScrollCurrentPosition(currentSeconds);
                        return;
                    }
                    //拖动或选择的时间是否有video（ap或cloud）
                    selectedTimeIsHaveVideo(currTime);
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING ||
                        newState == RecyclerView.SCROLL_STATE_SETTLING) {//拖动和自动滑动
                    LogCat.e(TAG, "onScrolled11");
                    removeCallbacks();
                    isSelectedDate = false;//手动拖动或自动滑动
                    isAutoScroll = false;//非自动滑动
                    int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
                    long currTime = centerCurrentTime(firstVisibleItem);//当前中间轴时间
                    long currentSeconds = System.currentTimeMillis() / 1000;//当前时间戳秒
                    if (currTime < currentSeconds && currentSeconds - currTime > 1) {
                        //回放时间
                        ivPlay.setBackgroundResource(R.mipmap.pause_normal);
                        ivLive.setVisibility(View.VISIBLE);
                        isCurrentLive = false;
                    } else {
                        //当前时间、未来时间
                        ivPlay.setBackgroundResource(R.mipmap.play_disable);
                        ivLive.setVisibility(View.GONE);
                        isCurrentLive = true;
                    }
                    isPaused = false;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                isLeftScroll = dx <= 0; //dx < 0左边滑动  dx > 0右边滑动
                int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
                long currTime = centerCurrentTime(firstVisibleItem);//当前中间轴时间
                String strDate = DateTimeUtils.secondToDate(currTime, "yyyy-MM-dd HH:mm:ss");
                String day = strDate.substring(8, 11);
                tvCalender.setText(String.format(" %s", day));
                scrollTime = currTime * 1000;//滑动日历的时间戳毫秒

                canvasHours(firstVisibleItem);//绘制时间
                if (listAp == null || listAp.size() == 0) return;
                if (isAutoScroll || isVideoLess1Minute) {
                    switch2Playback(currTime);//自动滑动时下一个视频ap还是cloud播放
                    isVideoLess1Minute = false;
                }
                if (isSelectedDate) {
                    isSelectedDate = false;
                    selectedTimeIsHaveVideo(currTime);
                }
            }
        });
    }

    private void switch2Playback(long currTime) {
        if (!isCloudPlayBack && !isDevPlayBack && isCurrentLive) return;
        int availableVideoSize = listAp.size();
        for (int i = 0; i < availableVideoSize; i++) {
            ApCloudTimeBean bean = listAp.get(i);
            long start = bean.getStartTime();
            long end = bean.getEndTime();
            LogCat.e(TAG, "888888 11");
            //当滑动到最后前后一分钟时，判断下一个视频片段ap还是cloud
            if (end - currTime < 60 && currTime >= start && currTime < end) {
                if (i == availableVideoSize - 1) {//todo 最后一个，需要渲染后面的数据
//                    refreshCanvasList();//i是最后一个，基于i的end作为start再拉7天的数据。
                    LogCat.e(TAG, "888888 22");
                } else {
                    boolean isCloud = !listAp.get(i + 1).isApPlay();
                    final int delayMillis = (int) end - currTime < 0 ? 1 : (int) (end - currTime);
                    final int finalI = i;
                    if (isCloud) {
                        LogCat.e(TAG, "888888 33 delayMillis=" + delayMillis);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                switch2CloudPlayback(listAp.get(finalI + 1).getStartTime(),
                                        listAp.get(finalI + 1).getStartTime() + tenMinutes);
                                videoSkipScrollPosition(listAp.get(finalI + 1).getStartTime()); //偏移跳转
                            }
                        }, delayMillis * 1000);
                        break;
                    } else {
                        LogCat.e(TAG, "888888 44 11delayMillis=" + delayMillis);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                switch2DevPlayback(listAp.get(finalI + 1).getStartTime());
                                videoSkipScrollPosition(listAp.get(finalI + 1).getStartTime());//偏移跳转
                            }
                        }, delayMillis * 1000);
                        break;
                    }
                }
            }
        }
    }

    //绘制时间
    private void canvasHours(int firstVisibleItem) {
        if (firstVisibleItem < 0) return;
        TimeBean bs = list.get(firstVisibleItem);
        String str = DateTimeUtils.secondToDate(bs.getDate(), "HH:mm:ss");
        int hour = Integer.valueOf(str.substring(0, 2));
        int minute = Integer.valueOf(str.substring(3, 5));
        //可见第一个item距离第一个可见长条小时的偏移量
        int offsetPx;
        if (minute == 0) {
            offsetPx = 0;
        } else {
            offsetPx = (60 - minute) * getResources().getDimensionPixelSize(R.dimen.dp_1);
            hour++;
        }
        //绘制下方时间
        dateList.clear();
        for (int i = 0; i < 10; i++) {
            if (hour <= 23) {
                dateList.add((hour++) + ":00");
            } else {
                hour = 0;
            }
            timeView.refresh(dateList, offsetPx);
        }
    }

    //自定义toast
    private Toast mToast;
    private Drawable drawableLeft, drawableRight;

    private void setTextViewTimeDrawable() {
        drawableLeft = getResources().getDrawable(R.mipmap.ic_fast_forward);
        drawableLeft.setBounds(0, 0, drawableLeft.getMinimumWidth(), drawableLeft.getMinimumHeight());
        drawableRight = getResources().getDrawable(R.mipmap.ic_forward);
        drawableRight.setBounds(0, 0, drawableRight.getMinimumWidth(), drawableRight.getMinimumHeight());
    }

    @UiThread
    void toastForShort(final Context context, final String msg, final boolean isLeft) {
        if (context == null || TextUtils.isEmpty(msg)) {
            return;
        }
        View layout = LayoutInflater.from(context).inflate(R.layout.toast_item, null);
        TextView tvDate = layout.findViewById(R.id.tv_date);
        tvDate.setText(msg);
        if (isLeft)
            tvDate.setCompoundDrawables(drawableLeft, null, null, null);
        else
            tvDate.setCompoundDrawables(null, null, drawableRight, null);
        if (mToast == null) {
            mToast = new Toast(context);
            mToast.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL, 0, 0);
            mToast.setDuration(Toast.LENGTH_SHORT);
            mToast.setView(layout);
            mToast.show();
            mToast = null;
        }
    }

    /*
     ****************************绘制时间轴*******************************************
     */
    private List<ApCloudTimeBean> listAp = new ArrayList<>();
    private List<ApCloudTimeBean> listCloud = new ArrayList<>();

    //发送请求获取组合时间轴
    private void refreshCanvasList() {
        showLoadingDialog();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                IOTCClient.getPlaybackList(threeDaysBeforeSeconds, currentDateSeconds); //获取AP回放时间列表
            }
        }, 3000);
    }

    @Override
    public void IOTCResult(String result) {
        LogCat.e(TAG, "888888 time ap get result = " + result);
        try {
            JSONObject object = new JSONObject(result);
            int errcode = object.getInt("errcode");
            if (errcode == 0) {
                JSONArray array = object.getJSONArray("data");
                JSONObject object1 = (JSONObject) array.opt(0);
                int cmd = object1.getInt("cmd");
                if (cmd == 32) {//ap回放时间轴
                    if (!object1.has("result")) return;
                    JSONArray array1 = object1.getJSONArray("result");
                    ApCloudTimeBean ap;
                    listAp.clear();
                    for (int i = 0; i < array1.length(); i++) {
                        JSONObject object2 = (JSONObject) array1.opt(i);
                        ap = new ApCloudTimeBean();
                        ap.setStartTime(object2.getLong("start_time"));
                        ap.setEndTime(object2.getLong("end_time"));
                        ap.setApPlay(true);
                        listAp.add(ap);
                    }
                    ///获取cloud回放时间轴
                    getTimeList(deviceId, threeDaysBeforeSeconds, currentDateSeconds);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void cloudListNullOrFail() {
        if (listAp == null || listAp.size() == 0) {
            switch2Live();//无ap且无cloud的时间列表
        } else {
            timeCanvasList(listAp); //ap时间列表>0且cloud列表=0
        }
        hideLoadingDialog();
    }

    //获取cloud回放时间轴
    public void getTimeList(int deviceId, final long startTime, final long endTime) {
        IPCCloudApi.getTimeSlots(deviceId, startTime, endTime, new RetrofitCallback<JsonObject>() {
            @Override
            public void onSuccess(int code, String msg, JsonObject data) {
                LogCat.e(TAG, "888888 time cloud getTimeSlots==" + data.toString());
                if (code == 1) {
                    try {
                        JSONObject object = new JSONObject(data.toString());
                        int total_count = object.getInt("total_count");
                        if (total_count == 0) {
                            cloudListNullOrFail();
                            return;
                        }
                        JSONArray jsonArray = object.getJSONArray("timeslots");
                        ApCloudTimeBean cloud;
                        listCloud.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object2 = (JSONObject) jsonArray.opt(i);
                            cloud = new ApCloudTimeBean();
                            cloud.setStartTime(object2.getLong("start_time"));
                            cloud.setEndTime(object2.getLong("end_time"));
                            listCloud.add(cloud);
                        }
                        getCanvasList(startTime, endTime);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFail(int code, String msg, JsonObject data) {
                LogCat.e(TAG, "888888 cloud getTimeSlots onFail");
                cloudListNullOrFail();
            }
        });
    }

    //时间轴组合
    private void getCanvasList(long mStartTime, long mEndTime) {
        int apSize = listAp.size();
        int cloudSize = listCloud.size();
        if (apSize == 0 && cloudSize > 0) {
            listAp = listCloud;
            timeCanvasList(listAp);//组合时间轴
            hideLoadingDialog();
            return;
        }
        ApCloudTimeBean bean;
        //AP时间
        for (int i = 0; i < apSize + 1; i++) {
            long startAp = 0, endAp = 0;
            //不包含ap时间轴内的时间
            if (i == 0) {
                startAp = mStartTime;
                endAp = listAp.get(i).getStartTime();
            } else if (i < apSize) {
                startAp = listAp.get(i - 1).getEndTime();
                endAp = listAp.get(i).getStartTime();
            } else if (i == apSize) {
                startAp = listAp.get(i - 1).getEndTime();
                endAp = mEndTime;
            }
            //cloud时间
            for (int j = 0; j < cloudSize; j++) {
                bean = new ApCloudTimeBean();
                long startCloud = listCloud.get(j).getStartTime();
                long endCloud = listCloud.get(j).getEndTime();

                if (startCloud >= startAp && endAp > startCloud && endCloud >= endAp) {
                    bean.setStartTime(startCloud);
                    bean.setEndTime(endAp);
                    bean.setApPlay(false);
                    listAp.add(bean);
                } else if (startAp >= startCloud && endCloud > startAp && endAp >= endCloud) {
                    bean.setStartTime(startAp);
                    bean.setEndTime(endCloud);
                    bean.setApPlay(false);
                    listAp.add(bean);
                } else if (startAp != endAp && startAp >= startCloud && endAp <= endCloud) {
                    bean.setStartTime(startAp);
                    bean.setEndTime(endAp);
                    bean.setApPlay(false);
                    listAp.add(bean);
                } else if (startCloud != endCloud && startCloud >= startAp && endCloud <= endAp) {
                    bean.setStartTime(startCloud);
                    bean.setEndTime(endCloud);
                    bean.setApPlay(false);
                    listAp.add(bean);
                }
            }
        }
        if (listAp.size() > 1) {
            listAp = duplicateRemoval(listAp);//去重
            Collections.sort(listAp);//正序比较
        }
        LogCat.e(TAG, "888888 time all list");
        timeCanvasList(listAp);//组合时间轴
        hideLoadingDialog();
    }

    //去重
    private List<ApCloudTimeBean> duplicateRemoval(List<ApCloudTimeBean> list) {
        LinkedHashSet<ApCloudTimeBean> tmpSet = new LinkedHashSet<>(list.size());
        tmpSet.addAll(list);
        list.clear();
        list.addAll(tmpSet);
        return list;
    }

    @Override
    public void onStartPlay() {
        hideLoadingDialog();
    }

    @Override
    public void onPlayComplete() {
        //获取当前播放完毕时间判断是否cloud or ap
        long currTime = centerCurrentTime(linearLayoutManager.findFirstVisibleItemPosition());//当前中间轴时间
        selectedTimeIsHaveVideo(currTime);
    }

}
