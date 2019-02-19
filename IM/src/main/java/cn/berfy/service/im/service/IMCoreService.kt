package cn.berfy.service.im.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import cn.berfy.sdk.mvpbase.util.AppUtils
import cn.berfy.sdk.mvpbase.util.LogF
import cn.berfy.service.im.manager.IMManager
import java.lang.NullPointerException

import java.util.Timer
import java.util.TimerTask

/**
 * 绑定mqtT连接心跳服务
 *
 * @author NorthStar
 * @date 2018/12/19 16:38
 */
class IMCoreService : Service() {

    private var mTimer: Timer? = null
    private var mTimerTask: TimerTask? = null
    private var mCount = 0

    override fun onCreate() {
        super.onCreate()
        LogF.d(TAG, "启动服务HeartBeatService" + AppUtils.getCurrentProcessName()!!)
        startHeart()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        LogF.d(TAG, "线程onStartCommand intent")
        if (intent.hasExtra("test")) {
            LogF.d(TAG, "线程onStartCommand test = " + intent.getStringExtra("test"))
        } else {
            LogF.d(TAG, "线程onStartCommand 没有extra")
        }
        return Service.START_REDELIVER_INTENT
    }

    private fun startHeart() {
        val period = (30 * 1000).toLong() //30秒为一个周期
        if (null == mTimerTask) {
            mTimerTask = object : TimerTask() {
                override fun run() {
                    //发送心跳
                    mCount++
                    LogF.d(TAG, "心跳" + mCount + "次 checking start")
                    try {
                        val im: IMManager = IMManager.instance
                        if (null == im.getClient()) {
                            LogF.d(TAG, "心跳检测 连接不存在 重新创建")
                            im.startConnect(true)
                        } else if (!im.isConnected && !im.isManualClose) {
                            LogF.d(TAG, "心跳检测 连接存在 已断开 重新连接")
                            im.startConnect(false)
                        } else {
                            LogF.d(TAG, "连接完好，checking end")
                            im.retrySub()
                        }
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    }
                    LogF.d(TAG, "心跳" + mCount + "次 checking end")
//                    sendBroadcast(Intent(RECEIVE_HEARTBEAT))
                }
            }
        }
        if (null == mTimer) {
            mTimer = Timer()
        } else {
            mTimer!!.cancel()
        }
        mTimer!!.schedule(mTimerTask, period, period)
    }

    //取消校验连接心跳
    private fun cancelTimer() {
        LogF.d(TAG, "+++++++++++停止心跳+++++++++")
        if (null != mTimerTask) {
            mTimerTask!!.cancel()
            mTimerTask = null
        }
        if (null != mTimer) {
            mTimer!!.cancel()
            mTimer = null
        }
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onUnbind(intent: Intent): Boolean {
        //        cancelTimer();
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelTimer()
    }

    companion object {

        private val TAG = "IMCoreService"
        val RECEIVE_HEARTBEAT = "ws_heartbeat"
    }
}
