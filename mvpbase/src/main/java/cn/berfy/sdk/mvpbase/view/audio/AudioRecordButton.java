package cn.berfy.sdk.mvpbase.view.audio;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;

import cn.berfy.sdk.mvpbase.R;
import cn.berfy.sdk.mvpbase.config.Constant;
import cn.berfy.sdk.mvpbase.util.CommonUtil;
import cn.berfy.sdk.mvpbase.util.FileUtils;
import cn.berfy.sdk.mvpbase.util.LogF;
import cn.berfy.sdk.mvpbase.view.audio.record.AudioManager;
import cn.berfy.sdk.mvpbase.view.audio.record.DialogManager;

//录音按钮核心类，包括点击、响应、与弹出对话框交互等操作。
public class AudioRecordButton extends android.support.v7.widget.AppCompatButton implements AudioManager.AudioStageListener {

    private static final String TAG = "IMLogTag_语音按钮";
    //三个对话框的状态常量
    public static final int STATE_NORMAL = 1;
    public static final int STATE_RECORDING = 2;
    public static final int STATE_WANT_TO_CANCEL = 3;

    //垂直方向滑动取消的临界距离
    private static final int DISTANCE_Y_CANCEL = 50;
    //取消录音的状态值
    private static final int MSG_VOICE_STOP = 4;
    //当前状态
    public int mCurrentState = STATE_NORMAL;
    // 正在录音标记
    private boolean isRecording = false;
    //录音对话框
    private DialogManager mDialogManager;
    //核心录音类
    private AudioManager mAudioManager;
    //当前录音时长
    private float mTime = 0;
    // 是否触发了onlongclick，准备好了
    private boolean mReady;
    //标记是否强制终止
    private boolean isOverTime = false;
    //最大录音时长（单位:s）。def:60s
    private int mMaxRecordTime = 59;

    //上下文
    Context mContext;
    //震动类
    private Vibrator vibrator;
    //提醒倒计时
    private int mRemainedTime = 10;
    //设置是否允许录音,这个是是否有录音权限
    private boolean mHasRecordPromission = true;
    //是否允许短时间内再次点击录音，主要是防止故意多次连续点击。
    private boolean canRecord = true;

    public boolean isHasRecordPromission() {
        return mHasRecordPromission;
    }

    public void setHasRecordPromission(boolean hasRecordPromission) {
        this.mHasRecordPromission = hasRecordPromission;
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }

    public AudioRecordButton(Context context) {
        this(context, null);
    }

    public AudioRecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;


        //初始化语音对话框
        mDialogManager = new DialogManager(getContext());

        //获取录音保存位置
        String dir = FileUtils.getAppRecordDir(mContext);
        //实例化录音核心类
        mAudioManager = new AudioManager(dir);

        mAudioManager.setOnAudioStageListener(this);
    }

    public interface AudioFinishRecorderListener {
        void onStart();

        void onFinished(float seconds, String filePath);
    }

    private AudioFinishRecorderListener mListener;

    public void setAudioFinishRecorderListener(AudioFinishRecorderListener listener) {
        mListener = listener;
    }

    // 获取音量大小的runnable
    private Runnable mGetVoiceLevelRunnable = new Runnable() {

        @Override
        public void run() {
            while (isRecording) {
                try {
                    //最长mMaxRecordTimes
                    if (mTime > mMaxRecordTime) {
                        mStateHandler.sendEmptyMessage(MSG_VOICE_STOP);
                        return;
                    }

                    Thread.sleep(100);
                    mTime += 0.1f;
                    mStateHandler.sendEmptyMessage(MSG_VOICE_CHANGE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    // 三个状态
    private static final int MSG_AUDIO_PREPARED = 0X110;
    private static final int MSG_VOICE_CHANGE = 0X111;
    private static final int MSG_DIALOG_DIMISS = 0X112;

    private Handler mStateHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AUDIO_PREPARED:
                    LogF.d(TAG, "开始录音");
                    if (null != mListener) {
                        mListener.onStart();
                    }
                    // 显示应该是在audio end prepare之后回调
                    mDialogManager.showRecordingDialog();
                    isRecording = true;
                    Constant.EXECUTOR.execute(mGetVoiceLevelRunnable);
                    // 需要开启一个线程来变换音量
                    break;
                case MSG_VOICE_CHANGE:
                    //剩余10s
                    showRemainedTime();
                    mDialogManager.updateVoiceLevel(mAudioManager.getVoiceLevel(7));
                    break;
                case MSG_DIALOG_DIMISS:
                    reset();
                    mDialogManager.dimissDialog();
                    break;
                case MSG_VOICE_STOP:
                    isOverTime = true;//超时
                    finishRecording();
                    break;

            }
        }
    };
    //是否触发过震动
    boolean isShcok;

    private void showRemainedTime() {
        //倒计时
        int remainTime = (int) (mMaxRecordTime - mTime);
        if (remainTime < mRemainedTime) {
            if (!isShcok) {
                isShcok = true;
                doShock();
            }
//            mDialogManager.getTipsTxt().setText("还可以说" + remainTime + "秒  ");
            mDialogManager.getText_time().setText(remainTime + "");
            mDialogManager.getVoice_iv().setVisibility(GONE);
        }

    }

    /*
     * 想设置震动大小可以通过改变pattern来设定，如果开启时间太短，震动效果可能感觉不到
     * */
    private void doShock() {
        vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {100, 400, 100, 400};   // 停止 开启 停止 开启
        vibrator.vibrate(pattern, -1);           //重复两次上面的pattern 如果只想震动一次，index设为-1
    }

    // 在这里面发送一个handler的消息
    @Override
    public void wellPrepared() {
        mStateHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
    }

    //手指滑动监听
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //响应DOWN事件进行录音准备。放到这里会有问题，比如用户故意连续点击多次，就会出现各种问题。
                // 所以和录制视频处理的思路一样，我们在短时间内只允许点击一次即可。
                if (isHasRecordPromission() && isCanRecord()) {
                    mReady = true;
                    setCanRecord(false);
                    startRecording();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isRecording) {
                    // 根据x，y来判断用户是否想要取消
                    if (wantToCancel(x, y)) {
                        changeState(STATE_WANT_TO_CANCEL);
                    } else {
                        if (!isOverTime)
                            changeState(STATE_RECORDING);
                    }

                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // 首先判断是否有触发onlongclick事件，没有的话直接返回reset
                if (!mReady) {
                    reset();
                    return super.onTouchEvent(event);
                }
                // 如果按的时间太短，还没准备好或者时间录制太短，就离开了，则显示这个dialog
                if (!isRecording || mTime < 0.8f) {
                    mDialogManager.tooShort();
                    mAudioManager.cancel();
                    mStateHandler.removeMessages(MSG_DIALOG_DIMISS);
                    mStateHandler.sendEmptyMessageDelayed(MSG_DIALOG_DIMISS, 1300);// 持续1.3s
                } else if (mCurrentState == STATE_RECORDING) {//正常录制结束
                    if (isOverTime) return super.onTouchEvent(event);//超时
                    finishRecording();
                } else if (mCurrentState == STATE_WANT_TO_CANCEL) {
                    // cancel
                    mAudioManager.cancel();
                    mDialogManager.dimissDialog();
                }
                reset();// 恢复标志位
                break;
        }
        return true;
    }

    private void startRecording() {
        mAudioManager.prepareAudio();
        //这里在短时间之后再允许点击
        Constant.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                setCanRecord(true);
            }
        });
        changeState(STATE_RECORDING);
    }

    private void finishRecording() {
        LogF.d(TAG, "完成录音");
        mDialogManager.dimissDialog();
        mAudioManager.release();// release释放一个mediarecorder
        if (mListener != null) {// 并且callbackActivity，保存录音
            mListener.onFinished(mTime, mAudioManager.getCurrentFilePath());
        }
    }

    public void stopVoice() {
        mAudioManager.cancel();
        mStateHandler.sendEmptyMessageDelayed(MSG_DIALOG_DIMISS, 1300);// 持续1.3s
        changeState(STATE_NORMAL);
    }

    /**
     * 回复标志位以及状态
     */
    private void reset() {
        LogF.d(TAG, "重置录音");
        if (mDialogManager.getVoice_iv() != null) {
            mDialogManager.getVoice_iv().setVisibility(VISIBLE);
        }
        isRecording = false;
        changeState(STATE_NORMAL);
        mReady = false;
        mTime = 0;
        isOverTime = false;
        isShcok = false;
    }

    private boolean wantToCancel(int x, int y) {
        if (x < 0 || x > getWidth()) {// 判断是否在左边，右边，上边，下边
            return true;
        }
        if (y < -DISTANCE_Y_CANCEL || y > getHeight() + DISTANCE_Y_CANCEL) {
            return true;
        }

        return false;
    }

    private void changeState(int state) {
        if (mCurrentState != state) {
            mCurrentState = state;
            switch (mCurrentState) {
                case STATE_NORMAL:
                    setTextColor(CommonUtil.getColor(mContext,R.color.color_666666));
                    setText(mContext.getString(R.string.long_click_record));//长按录音
                    setBackgroundResource(R.drawable.voice_button_normal);
                    break;
                case STATE_RECORDING:
                    setTextColor(CommonUtil.getColor(mContext,R.color.color_666666));
//                    setText(R.string.release_cancel);//松开结束
                    if (isRecording) {
                        // 复写dialog.recording();
                        mDialogManager.recording();
                    }
                    setBackgroundResource(R.drawable.voice_button_press);
                    break;
                case STATE_WANT_TO_CANCEL:
                    setTextColor(CommonUtil.getColor(mContext,R.color.color_666666));
//                    setText(R.string.release_cancel);//松开取消
                    // dialog want to cancel
                    mDialogManager.wantToCancel();
                    setBackgroundResource(R.drawable.voice_button_press);
                    break;

            }

        }

    }

    @Override
    public boolean onPreDraw() {
        return false;
    }

    public boolean isCanRecord() {
        return canRecord;
    }

    public void setCanRecord(boolean canRecord) {
        this.canRecord = canRecord;
    }

    public int getMaxRecordTime() {
        return mMaxRecordTime;
    }

    public void setMaxRecordTime(int maxRecordTime) {
        mMaxRecordTime = maxRecordTime;
    }
}
