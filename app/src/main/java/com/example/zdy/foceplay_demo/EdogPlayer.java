package com.example.zdy.foceplay_demo;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;


import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by ZDY on 2017/6/6.
 */


public class EdogPlayer extends FrameLayout implements View.OnClickListener, View.OnTouchListener, SeekBar.OnSeekBarChangeListener,
        SurfaceHolder.Callback, GestureDetector.OnGestureListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener {
    private Context mContext;
    private boolean isFirstPlay = true;
    private boolean isPrepare = false;
    private String videoPath;
    public int video_position = 0;
    private boolean isPlaying = false;
    private int maxProgress = -100;

    //音量
    private int currentVolume;
    private int lastVolume;
    private int maxVolume;


    // SurfaceView的创建比较耗时，要注意
    private SurfaceHolder surfaceHolder;
    private MediaPlayer mediaPlayer;

    private Timer timer_controller;
    private TimerTask task_controller;


    private GestureDetector gestureDetector;
    private AudioManager audiomanager;

    //控件

    private RelativeLayout bottom_menu;
    private SurfaceView surfaceView;
    private ImageView mn_iv_play_pause;
    private SeekBar mn_seekBar;
    private TextView mn_tv_time;
    private ImageView mn_iv_back;


    private RelativeLayout light_layout;
    private RelativeLayout volume_layout;
    private RelativeLayout kuaijin_layout;
    private TextView tv_value_vol;

    private TextView tv_value_kuaijin;
    private TextView tv_value_light;


    private int GESTURE_FLAG = 0;// 1,调节进度，2，调节音量
    private static final int GESTURE_MODIFY_PROGRESS = 1;
    private static final int GESTURE_MODIFY_VOLUME = 2;
    private static final int GESTURE_MODIFY_BRIGHTNESS = 3;

    static final Handler myHandler = new Handler(Looper.getMainLooper()) {
    };
    private ImageView jumpView;
    private ImageView iv_vol;
    private ImageView iv_progress;
    private View ges_view;

    public boolean canSetLight = false;

    public EdogPlayer(Context context) {
        this(context, null);
    }

    public EdogPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EdogPlayer(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        //自定义属性相关
//        initAttrs(context, attrs);
        init();
    }

    private ScreenUtils screenUtils;

    private void init() {
        screenUtils = new ScreenUtils((Activity) mContext);
        View inflate = View.inflate(mContext, R.layout.gcs_foceplayer_layout, this);
        bottom_menu = (RelativeLayout) inflate.findViewById(R.id.mn_rl_bottom_menu);
        surfaceView = (SurfaceView) inflate.findViewById(R.id.mn_palyer_surfaceView);
        mn_iv_play_pause = (ImageView) inflate.findViewById(R.id.mn_iv_play_pause);
        mn_tv_time = (TextView) inflate.findViewById(R.id.mn_tv_time);
        mn_seekBar = (SeekBar) inflate.findViewById(R.id.mn_seekBar);
        //关闭按钮
        mn_iv_back = (ImageView) inflate.findViewById(R.id.mn_iv_back);
        //跳转按钮
        jumpView = (ImageView) inflate.findViewById(R.id.mn_iv_jump);
        //亮度那个布局
        light_layout = (RelativeLayout) inflate.findViewById(R.id.mn_gesture_light_layout);
        //声音布局
        volume_layout = (RelativeLayout) inflate.findViewById(R.id.mn_gesture_volume_layout);
        //快进那个布局
        kuaijin_layout = (RelativeLayout) inflate.findViewById(R.id.mn_gesture_progress_layout);

        //集中控制view
        ges_view = inflate.findViewById(R.id.ges_view);

        mn_seekBar.setOnSeekBarChangeListener(this);
        mn_iv_play_pause.setOnClickListener(this);
        mn_iv_back.setOnClickListener(this);
        jumpView.setOnClickListener(this);
//        surfaceView.setOnClickListener(this);


        initViews();

        initSurfaceView();

        initViewWithActions();
        showView(isVis);
        ges_view.setOnTouchListener(this);

    }


    public void showView(boolean isVis) {
        if (isVis) {
            jumpView.setVisibility(View.VISIBLE);
            mn_iv_back.setVisibility(View.VISIBLE);
            bottom_menu.setVisibility(View.VISIBLE);

        } else {
            jumpView.setVisibility(View.INVISIBLE);
            mn_iv_back.setVisibility(View.INVISIBLE);
            bottom_menu.setVisibility(View.INVISIBLE);
        }
    }


    private void initViewWithActions() {
        //声音相关
        tv_value_vol = (TextView) volume_layout.findViewById(R.id.mn_gesture_tv_volume_percentage);
        iv_vol = (ImageView) volume_layout.findViewById(R.id.iv_player_volume);

        //快进相关
        tv_value_kuaijin = (TextView) kuaijin_layout.findViewById(R.id.tv_progress_time);
        iv_progress = (ImageView) findViewById(R.id.iv_progress);

        //亮度相关
        tv_value_light = (TextView) light_layout.findViewById(R.id.mn_geture_tv_light_percentage);


        gestureDetector = new GestureDetector(getContext(), this);
        setLongClickable(true);
//        gestureDetector.setIsLongpressEnabled(true);
        audiomanager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audiomanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 获取系统最大音量
        currentVolume = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    private void initSurfaceView() {
        // 得到SurfaceView容器，播放的内容就是显示在这个容器里面
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setKeepScreenOn(true);
        // SurfaceView的一个回调方法
        surfaceHolder.addCallback(this);
    }

    public void resume(int position){
        mediaPlayer.start();
        mediaPlayer.seekTo(position);
    }

    private void initViews() {
        light_layout.setVisibility(View.GONE);
        volume_layout.setVisibility(View.GONE);
        kuaijin_layout.setVisibility(View.GONE);

        bottom_menu.setVisibility(View.VISIBLE);
    }

    //设置基本信息
    public void setVideoPath(String url) {
        videoPath = url;
    }

    private void resetMediaPlayer() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    mediaPlayer.stop();
                }
                //重置mediaPlayer
                mediaPlayer.reset();
                //添加播放路径
                mediaPlayer.setDataSource(videoPath);
                // 准备开始,异步准备，自动在子线程中
                mediaPlayer.prepareAsync();
                //视频缩放模式
                mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);

            } else {
//                Toast.makeText(mContext, "播放器初始化失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 播放视频
     */
    public void playvideo() {
        if (mediaPlayer != null) {
            if (stopPostion == -100) {
                mediaPlayer.start();
                mn_iv_play_pause.setImageResource(R.mipmap.player_pause);
                isPlaying = true;
            }else{
                mediaPlayer.start();
                mediaPlayer.seekTo(stopPostion);
                if (isPlaying) {
                    mediaPlayer.start();
                    mn_iv_play_pause.setImageResource(R.mipmap.player_pause);
                }else{
                    mediaPlayer.pause();
                    mn_iv_play_pause.setImageResource(R.mipmap.player_play);
                }
            }
        }
    }

    /**
     * 暂停视频
     */
    public void pauseVideo() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
            mn_iv_play_pause.setImageResource(R.mipmap.player_play);
            video_position = mediaPlayer.getCurrentPosition();
            isPlaying = false;
        }
    }


    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if (percent >= 0 && percent <= 100) {
            int secondProgress = mp.getDuration() * percent / 100;
            mn_seekBar.setSecondaryProgress(secondProgress);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        pauseVideo();
        if (onCloseOrJumpListener != null) {
            onCloseOrJumpListener.onJump();
        }

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        maxProgress = mediaPlayer.getDuration();
        mn_seekBar.setMax(maxProgress);
        playvideo();
        isPrepare = true;
        myHandler.postDelayed(bRunnable, 500);
    }
    private Runnable bRunnable = new Runnable() {
        @Override
        public void run() {
            initControllerTask();
            //跳转指定位置
            if (video_position > 0) {

                EdogPlayer.this.mediaPlayer.seekTo(video_position);
                video_position = 0;
            }
        }
    };

    private void initControllerTask() {
        timer_controller = new Timer();
        task_controller = new TimerTask() {
            @Override
            public void run() {

            }
        };
        initTimerTask();
    }

    private Timer timer_video_time;
    private TimerTask task_video_timer;

    private void initTimerTask() {
        timer_video_time = new Timer();
        task_video_timer = new TimerTask() {
            @Override
            public void run() {
                myHandler.post(aRunable);
            }
        };
        timer_video_time.schedule(task_video_timer, 0, 1000);

    }
    private Runnable aRunable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer == null) {
                return;
            }
            //设置时间
            mn_tv_time.setText(String.valueOf(converLongTimeToStr(mediaPlayer.getCurrentPosition()) + " / " + converLongTimeToStr(mediaPlayer.getDuration())));
            //进度条
            int progress = mediaPlayer.getCurrentPosition();
            mn_seekBar.setProgress(progress);
        }
    };

    private float downx = 0.0f;
    private float downy = 0.0f;

    @Override
    public boolean onDown(MotionEvent e) {
        downx = e.getX();
        downy = e.getY();
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        isVis = !isVis;
        showView(isVis);
        downx = 0.0f;
        downy = 0.0f;
        return true;
    }

    private float lastProgress = 0;
    private float currentProgress = 0;

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        int FLAG = 0;
        float movex = e2.getX();
        float movey = e2.getY();
        // 横向的距离变化大则调整进度，纵向的变化大则调整音量
        if (Math.abs(distanceX) >= Math.abs(distanceY)) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                FLAG = GESTURE_MODIFY_PROGRESS;
            }
        } else {
            int intX = (int) e1.getX();
            int screenWidth = screenUtils.getScreenOriginalWidth();
            if (intX > screenWidth / 2) {
                //声音
                FLAG = GESTURE_MODIFY_VOLUME;
            } else {
                //左边是亮度
                FLAG = GESTURE_MODIFY_BRIGHTNESS;
            }
        }

        if (GESTURE_FLAG != 0 && GESTURE_FLAG != FLAG) {
            return false;
        }

        GESTURE_FLAG = FLAG;

        if (FLAG == GESTURE_MODIFY_PROGRESS) {
            //表示是横向滑动,可以添加快进
            // distanceX=lastScrollPositionX-currentScrollPositionX，因此为正时是快进
            volume_layout.setVisibility(View.GONE);
            light_layout.setVisibility(View.GONE);
            kuaijin_layout.setVisibility(View.VISIBLE);
            try {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    if (Math.abs(distanceX) > Math.abs(distanceY)) {// 横向移动大于纵向移动
                        float huaJin = movex - downx;
//                        Log.w("lzqxx","相差"+huaJin);
                        int currentP = mediaPlayer.getCurrentPosition();
                        mediaPlayer.seekTo(currentP + (int) huaJin);
                        if (huaJin < 0) {
                            //快退
                            iv_progress.setImageResource(R.mipmap.player_backward);
                        } else {
                            //快进
                            iv_progress.setImageResource(R.mipmap.player_forward);
                        }

                    }
                    String timeStr = converLongTimeToStr(mediaPlayer.getCurrentPosition()) + " / "
                            + converLongTimeToStr(mediaPlayer.getDuration());
                    tv_value_kuaijin.setText(timeStr);

                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        // 如果每次触摸屏幕后第一次scroll是调节音量，那之后的scroll事件都处理音量调节，直到离开屏幕执行下一次操作
        else if (FLAG == GESTURE_MODIFY_VOLUME) {
            //右边是音量
            volume_layout.setVisibility(View.VISIBLE);
            light_layout.setVisibility(View.GONE);
            kuaijin_layout.setVisibility(View.GONE);
            currentVolume = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
            if (Math.abs(distanceY) > Math.abs(distanceX)) {// 纵向移动大于横向移动
                if (currentVolume == 0) {// 静音，设定静音独有的图片
                    iv_vol.setImageResource(R.mipmap.player_volume_close);
                }
                //向下滑动 是正的 所以取反
                float huaVolume = downy - movey;
                currentVolume = lastVolume + (int) huaVolume / 50;
                if (currentVolume < 0) {
                    currentVolume = 0;
                    iv_vol.setImageResource(R.mipmap.player_volume_close);
                } else {
                    if (currentVolume >= maxVolume) {
                        currentVolume = maxVolume;
                    }
                    iv_vol.setImageResource(R.mipmap.player_volume_open);
                }
                int percentage = (currentVolume * 100) / maxVolume;

                tv_value_vol.setText(String.valueOf(percentage + "%"));
                audiomanager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
            }
        }
        //调节亮度
        else if (FLAG == GESTURE_MODIFY_BRIGHTNESS) {
            volume_layout.setVisibility(View.GONE);
            light_layout.setVisibility(View.VISIBLE);
            kuaijin_layout.setVisibility(View.GONE);
            currentVolume = audiomanager
                    .getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
            if (Math.abs(distanceY) > Math.abs(distanceX)) {// 纵向移动大于横向移动
                // 亮度调大,注意横屏时的坐标体系,尽管左上角是原点，但横向向上滑动时distanceY为正
                int mLight = LightnessControl.GetLightness((Activity) mContext);
                if (canSetLight) {
                    if (mLight >= 0 && mLight <= 255) {
                        float huaLight = downy - movey;
                        LightnessControl.SetLightness((Activity) mContext, LightnessControl.GetLightness((Activity) mContext) + (int) (huaLight / 50));
                        int c = LightnessControl.GetLightness((Activity) mContext);
                        Log.w("haha", " c  " + c);

                    } else if (mLight < 0) {
                        LightnessControl.SetLightness((Activity) mContext, 0);
                    } else {
                        LightnessControl.SetLightness((Activity) mContext, 255);
                    }
                    //获取当前亮度
                    int currentLight = LightnessControl.GetLightness((Activity) mContext);
                    int percentage = (currentLight * 100) / 255;
                    tv_value_light.setText(String.valueOf(percentage + "%"));
//
                }
            }
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.w("lzqonfire","surfaceCreated");
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDisplay(holder);
        //播放完成的监听
        mediaPlayer.setOnCompletionListener(this);
        // 异步准备的一个监听函数，准备好了就调用里面的方法
        mediaPlayer.setOnPreparedListener(this);
        //播放错误的监听
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        //播放本地视频
        try {
            mediaPlayer.setDataSource(videoPath);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
    private int stopPostion = -100;
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //保存播放位置
        if (mediaPlayer!=null){
            mediaPlayer.pause();
        }

        if (mediaPlayer!=null) {
            stopPostion = mediaPlayer.getCurrentPosition();
        }else{
        }
    }

    private void destroyControllerTask(boolean isMainThread) {
        myHandler.removeCallbacks(aRunable);
        myHandler.removeCallbacks(bRunnable);
        if (timer_video_time!=null && task_video_timer!=null){
            timer_video_time.cancel();
            task_video_timer.cancel();
            timer_video_time = null;
            task_video_timer = null;
        }
        if (timer_controller != null && task_controller != null) {
            timer_controller.cancel();
            task_controller.cancel();
            timer_controller = null;
            task_controller = null;
        }
    }


    private boolean isVis = false;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // 手势里除了singleTapUp，没有其他检测up的方法
        if (event.getAction() == MotionEvent.ACTION_UP) {
            GESTURE_FLAG = 0;// 手指离开屏幕后，重置调节音量或进度的标志
            volume_layout.setVisibility(View.GONE);
            kuaijin_layout.setVisibility(View.GONE);
            light_layout.setVisibility(View.GONE);
            lastVolume = currentVolume;
            downx = 0.0f;
            downy = 0.0f;
        }
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mn_iv_back:
                if (onCloseOrJumpListener != null) {
                    onCloseOrJumpListener.onClose();
                }
                break;
            case R.id.mn_iv_play_pause:
                if (isPlaying) {
                    pauseVideo();
                } else {
                    playvideo();
                }
                break;
            case R.id.mn_iv_jump:
                if (onCloseOrJumpListener != null) {
                    onCloseOrJumpListener.onJump();
                }
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }


    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.w("lzqplay", "isPrepare" + isPrepare);
        if (mediaPlayer != null && isPrepare /*&& mediaPlayer.isPlaying()*/) {
            int maxCanSeekTo = seekBar.getMax() - 1 * 1000;
            if (seekBar.getProgress() < maxCanSeekTo) {
                mediaPlayer.seekTo(seekBar.getProgress());
            } else {
                //不能拖到最后
                mediaPlayer.seekTo(maxCanSeekTo);
            }
        } else {
        }

    }

    /**
     * 转换毫秒数成“分、秒”，如“01:53”。若超过60分钟则显示“时、分、秒”，如“01:01:30
     *
     * @param
     */
    public String converLongTimeToStr(long time) {
        int ss = 1000;
        int mi = ss * 60;
        int hh = mi * 60;

        long hour = (time) / hh;
        long minute = (time - hour * hh) / mi;
        long second = (time - hour * hh - minute * mi) / ss;

        String strHour = hour < 10 ? "0" + hour : "" + hour;
        String strMinute = minute < 10 ? "0" + minute : "" + minute;
        String strSecond = second < 10 ? "0" + second : "" + second;
        if (hour > 0) {
            return strHour + ":" + strMinute + ":" + strSecond;
        } else {
            return strMinute + ":" + strSecond;
        }
    }


    private onCloseOrJumpListener onCloseOrJumpListener;

    public void setOnCloseOrJumpListener(onCloseOrJumpListener onCloseOrJumpListener) {
        this.onCloseOrJumpListener = onCloseOrJumpListener;
    }

    public interface onCloseOrJumpListener {
        public void onClose();
        public void onJump();
    }
    public void destroyAll(){
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            surfaceView.destroyDrawingCache();
            surfaceView.getHolder().getSurface().release();
        }
        destroyControllerTask(true);
        destroyDrawingCache();
    }

}
