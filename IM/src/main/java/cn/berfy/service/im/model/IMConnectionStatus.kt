package cn.berfy.service.im.model

object IMConnectionStatus {

    val CONNECTED = 1
    val CONNECTTING = 0
    val RECONNECT = 2
    val DISCONNECTED = -1

    object CODE {

        val NORMAL_CLOSE = 1000
        val ABNORMAL_CLOSE = 1001
    }

    object TIP {

        val NORMAL_CLOSE = "normal close"
        val ABNORMAL_CLOSE = "abnormal close"
    }
}
