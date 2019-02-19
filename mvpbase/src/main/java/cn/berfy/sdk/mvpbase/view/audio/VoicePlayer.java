package cn.berfy.sdk.mvpbase.view.audio;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import cn.berfy.sdk.mvpbase.base.BaseApplication;
import cn.berfy.sdk.mvpbase.config.Constant;
import cn.berfy.sdk.mvpbase.util.LogF;

/**
 * Berfy 修改 2018.6.13
 * SoundPool 铃声尽量不要超过1M
 * 在不同的系统下 SoundPool 表现可能存在不一致
 */
public class VoicePlayer {

    public static final String TAG = "VoicePlayer";
    private static VoicePlayer mInstance = null;
    private MediaPlayer mMediaPlayer;
    private int mPlayState = 0;//0未播放 1播放 2暂停 3停止
    private boolean mLoop;
    private AudioManager mAudioManager;
    private int mSysVol;//系统音量
    private int mUserVol = -1;//用户设置音量
    protected OnCompleteListener onCompleteListener;
    private int mRetryNum = 0, mRetryMaxNum = 1;

    private String mLocalPath;


    public static VoicePlayer instance() {
        if (null == mInstance) {
            synchronized (VoicePlayer.class) {
                if (null == mInstance) {
                    mInstance = new VoicePlayer();
                }
            }
        }
        return mInstance;
    }

    private VoicePlayer() {
        mAudioManager = (AudioManager) BaseApplication.getContext().getSystemService(Context.AUDIO_SERVICE);
    }

    //恢复系统音量
    private void releaseVol() {
        LogF.d(TAG, "恢复系统音量");
        int vol = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
        if (mUserVol == vol) {//用户没有改动过音量 帮助用户恢复系统音量   否则跟随用户的
            mAudioManager.setStreamVolume(AudioManager.STREAM_RING, mSysVol, 0);
        }
    }

    /**
     * 设置音量为当前系统音量的百分比
     */
    public void setVol(float percent) {
        if (null != mMediaPlayer && mMediaPlayer.isPlaying()) {
            setVol(false, 0, percent);
        }
    }

    private void setVol(boolean isSetNewVol, int newVol, float percent) {
        //缓存系统之前的音量
        int vol = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
        LogF.d(TAG, "设置音量 isSetNewVol=" + isSetNewVol + " newVol=" + newVol + " percent=" + percent);
        if (mUserVol == vol) {//这是设置过得音量 不能更新缓存的音量
        } else {
            mSysVol = vol;
        }
        if (isSetNewVol) {
            mUserVol = (int) (newVol * percent);
        } else {
            mUserVol = (int) (mSysVol * percent);
        }
        mAudioManager.setStreamVolume(AudioManager.STREAM_RING, mUserVol, 0);
    }

    public void play(String localPath) {
        play(localPath, 1, false, 0);
    }

    public void play(String localPath, float volPercent, boolean isSetNewVol, int newVol) {
        mLocalPath = localPath;
        releaseVol();
        setVol(isSetNewVol, newVol, volPercent);
        initAndPlay(false, -1, localPath);
    }

    private void initAndPlay(boolean isPlayRes, int ringId, String localPath) {
        //开启子线程播放音乐
        Constant.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //销毁后重新创建
                    if (null == mMediaPlayer) {
                        LogF.d(TAG, "初始化播放器");
                        mMediaPlayer = new MediaPlayer();
                    } else {
                        LogF.d(TAG, "重置播放器");
                        reset();
                    }
                    LogF.d(TAG, "设置资源");
                    if (isPlayRes) {
                        AssetFileDescriptor fd = BaseApplication.getContext().getResources().openRawResourceFd(ringId);
                        mMediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
                    } else {
                        mMediaPlayer.setDataSource(localPath);
                    }
                    //音量跟随打电话
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
                    mMediaPlayer.setLooping(mLoop);
                    LogF.d(TAG, "准备ing");
                    mMediaPlayer.prepare();
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            initComplete();
                            if (!mMediaPlayer.isPlaying()) {
//                                LogF.d(TAG, "播放");
                                mMediaPlayer.seekTo(0);
                                mMediaPlayer.start();
                                mPlayState = 1;
                            }
                        }
                    });
                    if (mRetryNum >= mRetryMaxNum) {
                        mRetryNum = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (mRetryNum < 1) {
                        BaseApplication.getMainThreadHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mRetryNum++;
                                initAndPlay(isPlayRes, ringId, localPath);
                            }
                        }, 1000);
                    }
                }
            }
        });
    }

    public void destroy() {
        LogF.d(TAG, "销毁");
        if (null != mMediaPlayer) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void reset() {
        LogF.d(TAG, "reset");
        if (null != mMediaPlayer) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.reset();
        }
    }

    private boolean startAlarm(Context context) {//提示音
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (notification != null) {
                Ringtone r = RingtoneManager.getRingtone(context, notification);
                r.play();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public interface OnCompleteListener {
        void completed();
    }

    public void setComListener(OnCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    private void initComplete() {
        if (mMediaPlayer != null) {
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    releaseVol();
                    mPlayState = 3;
                    LogF.d(TAG, "播放完毕");
                    if (onCompleteListener != null)
                        onCompleteListener.completed();
                }
            });
        }
    }

    public void stop() {
        LogF.d(TAG, "停止");
        if (null != mMediaPlayer) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                mPlayState = 3;
            }
        }
    }

    public void pause() {
        if (null != mMediaPlayer) {
            if (mMediaPlayer.isPlaying() && mPlayState <= 1) {
                LogF.d(TAG, "暂停");
                mMediaPlayer.pause();
                mPlayState = 2;
            }
        }
    }

    public void resume() {
        if (null != mMediaPlayer) {
            if (mPlayState == 2) {
                if (!mMediaPlayer.isPlaying()) {
                    LogF.d(TAG, "暂停恢复播放" + mPlayState);
                    mMediaPlayer.start();
                }
            } else if (mPlayState == 3) {
                reset();
                play(mLocalPath);
            }
        } else {
            play(mLocalPath);
        }
        mPlayState = 1;
    }
}
