package cn.berfy.service.mqtt.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import cn.berfy.sdk.mvpbase.util.AppUtils
import cn.berfy.sdk.mvpbase.util.DeviceUtils
import cn.berfy.sdk.mvpbase.util.LogF

import java.util.Timer
import java.util.TimerTask

/**
 * 绑定mqtT连接心跳服务
 *
 * @author NorthStar
 * @date 2018/12/19 16:38
 */
class HeartBeatService : Service() {
    private var iCheckConnection: ICheckConnection? = null
    private var count = 0

    //通过binder实现调用者client与Service之间的通信
    private val binder = MyBinder()

    inner class MyBinder : Binder() {
        var timer: Timer? = null

        val service: HeartBeatService
            get() = this@HeartBeatService

        fun setICheckConnection(mCheckConnection: ICheckConnection) {
            iCheckConnection = mCheckConnection
            val period = (30 * 1000).toLong() //30秒为一个周期
            val task = object : TimerTask() {
                override fun run() {
                    //发送心跳
                    count++
                    LogF.d(TAG, "开启心跳" + count + "次")
                    sendBroadcast(Intent(RECEIVE_HEARTBEAT))
                    iCheckConnection!!.goCheckConnect()
                }
            }

            if (null == timer) {
                timer = Timer()
            }
            timer!!.schedule(task, 0, period)
        }
    }

    override fun onCreate() {
        super.onCreate()
        LogF.d(TAG, "启动服务HeartBeatService" + AppUtils.getCurrentProcessName()!!)
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

    //取消校验连接心跳
    private fun cancelTimer() {
        LogF.d(TAG, "+++++++++++取消校验连接心跳+++++++++")
        if (binder.timer != null) {
            binder.timer!!.cancel()
            binder.timer = null
        }
    }


    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onUnbind(intent: Intent): Boolean {
        //        cancelTimer();
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelTimer()
    }

    interface ICheckConnection {
        fun goCheckConnect()
    }

    companion object {

        private val TAG = "mq"
        val RECEIVE_HEARTBEAT = "mqtt_heartbeat"
    }
}
