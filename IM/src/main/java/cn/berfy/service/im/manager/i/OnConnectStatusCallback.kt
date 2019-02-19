package cn.berfy.service.im.manager.i

    interface OnConnectStatusCallback {

        fun connectStart() //连接开始

        fun connectSuc() //连接成功

        fun connectFailed(exception: Throwable?) //连接失败

        fun disConnect(exception: Throwable?) //连接丢失

    }