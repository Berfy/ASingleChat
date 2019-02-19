package cn.berfy.service.im.manager.i

interface IManager {

    val isConnectting: Boolean//是否连接ing

    val isConnected: Boolean//是否连接

    val currentStatus: Int//当前连接状态

    val isManualClose: Boolean//是否手动关闭

    fun startConnect() //开始连接

    fun stopConnect() //结束连接

    fun destroy() //彻底销毁服务

    fun updateCurrentStatus(currentStatus: Int) //更新状态

    fun addConnectListener(listener: OnConnectStatusCallback)//增加连接状态监听

    fun removeConnectListener(listener: OnConnectStatusCallback)//移除连接状态监听

    fun addMessageListener(listener: OnMessageListener)//增加消息监听

    fun removeMessageListener(listener: OnMessageListener)//移除消息监听
}
