基于MQTT的IM框架
---
**1.项目结构**
- **app**(主入口，调试Demo)
- **IM**(websocket协议核心库、mqtt协议核心库和对外接口)
- **IMUIkit**(IMUI构建帮助类)
- **mvpbase**(MVP基础框架)
- **http**(基于OkHttp3的网络扩展请求框架)

---

**2.IM核心库**
- **IM**(cn.berfy.service.im) 基于WebSocket的即时通讯核心库
     1. **cn.berfy.service.im.manager** 管理类 所有业务都通过manager管理
        - IMManager 核心管理类 初始化、登录注销和监听
        - ConversationManager 会话管理 获取会话 删除会话==,通过IMManager也可以获取会话管理类
        - ContactManager 联系人管理 添加删除好友、获取用户资料
        - PushManager 推送管理 免打扰、震动设置
        - CacheManager 缓存管理 会话消息缓存
        - FilterManager 消息拦截管理
     2. **cn.berfy.service.im.manager.i** 接口
        - IWsManager 管理类接口 
        - OnConnectStatusCallback 连接状态回调接口
        - OnMessageListener 消息监听回调接口
     3. **cn.berfy.service.im.model** 消息模型 消息结构 类型（文本 语音 图片 文件 视频）
        - MessageType消息会话类型 单聊 群聊 聊天室 系统消息 其他
        - MessageContentType 内容类型 文本 语音 图片 视频 文件 位置 自定义等等
        - RawMessage 消息解析器 根据协议转换成MessageType和MessageContentType对应消息
        - MessageText 文本消息
        - MessageImage 图片消息
        - MessageVoice 语音消息
        - MessageVideo 视频消息
        - MessageFile 文件消息
        - MessageLocation 位置消息
        - MessageCustom 自定义消息
        - Conversation 会话(每个非其他类型消息都包含唯一会话，指定对象ID或群id和会话类型也可以手动生成会话)
        - WsStatus IM连接状态
     4. **cn.berfy.service.im.service** 核心服务
- **mqtt**(cn.berfy.service.mqtt)暂时保留的MQTT基础协议
- **mqttTest**(cn.berfy.service.mqttTest)暂时保留的mqtt测试Demo
- **mqtt核心库**(org.eclipse.paho.android.service)暂时保留

---

**3.基本架构**

[架构图](http://naotu.baidu.com/file/d9647ea3517f545802af4f0600b68cb8?token=619e221bf82ae7da)

---

**4.使用**

- **初始化**

    
    Application初始化
    IMManager.init(mContext)
        .config(IMManager.Config()
        .url(url)
        .needReconnect(true)
        .autoLogin("username", "pwd")
        .callback(this))        
        //连接监听
        fun connectStart() //连接开始
        fun connectSuc() //连接成功
        fun connectFailed(exception: Throwable?) //连接失败
        fun disConnect(exception: Throwable?) //连接丢失
        
- **用户信息**


    IMManager.getInstance().getContactsManager().getUserInfo(uid, RequestCallBack<UserInfo>)
- **接收和发送消息**
    

    //消息监听
    IMManager.getInstance().addMessageListener(this)
        fun newMessage(message: Message?) //P2P消息
        fun newGroupMessage(message: Message?) //群聊
        fun systemMessage(msg: MessageSystem?) //系统消息 推送
        fun sendMessageStatus(message: String?, isSuc: Boolean) //发送消息 是否成功状态
        
    //消息状态获取
    messageVoice.getSendStatus()
        val STATUS_SEND = 0 //发送中
        val STATUS_SEND_SUC = 1//已发送
        val STATUS_SEND_FAILED = 2//发送失败
    }
    //开启会话
    IMManager.getInstance().startChat(conversation:Conversation)
    //结束会话
    IMManager.getInstance().stopChat()
    //发送消息
    Type:1(通过会话发送  推荐)**
    val message = MessageText()
    message.content = msg
    ssage.to_client_id = mToClientId
    conversation.sendMessage() //当前会话
    
    Type:2(自己创建会话发送消息)**
    val message = MessageText() 
    message.content = msg
    message.to_client_id = mToClientId
    mImManager.sendMessage(conversation, msg)//需要手动传入会话信息
    
    //删除消息
    message.delete()
    
    //发送语音、视频、文件状态回调
    message.sendMessageCallback(callback: OnMessageSenddingCallback)
        fun onStart()
        fun uploadProgress(pro: Float)//0-1 上传文件用
        fun onSuc(message: Message)
        fun onFailed(errMsg: String)
    
    //下载语音、视频、文件状态回调
    message.getRemoteUrl(callback: OnMessageDownloadCallback)
        fun onStart()
        fun downloadProgress(pro: Float)//0-1 下载
        fun onSuc(message: Message)
        fun onFailed(errMsg: String)
    //标记已读
    IMManager.getInstance().markRead(topic: String, msgId: String)
    
- **会话(ConversationManager)**


    **获取本地回话**
    //返回MutableList<Conversation>
    val conversations = ConversationManager.getInstance().getConversations()
    **获取会话最新一条消息描述**
    val summary = conversation.getSummaryText()
    **获取会话消息**
    //消息与新消息一同通过监听收取
    conversation.getMessage(起始位置的消息id:String, n:Int)//起始位置（消息id）、取n条更早的消息
    **删除本地回话(推荐)**
    val isSuc = conversation.delete()
    **删除本地会话**
    val isSuc = ConversationManager.getInstance().deleteConversation("toId",MessageType.TYPE_P2P)
    **删除所有本地会话**
    val isSuc = ConversationManager.getInstance().deleteAllConversation()\


 - **联系人管理(ContactManager)**


    **获取用户资料**
    //方法1
    IMManager.getInstance().getContactsManager().getUserInfo(uid: String, callback: RequestCallBack<UserInfo>)
    //方法2
    ContactManager.getInstance().getUserInfo(uid: String, callback: RequestCallBack<UserInfo>)
    **添加好友**
    
    **删除好友**
    **获取好友**
    **同意好友申请**
    **拒绝好友申请**
    **当前申请加你好友的列表**
    
 - **存储管理(CacheManager)**


    **消息表**
    id //主键
    user_id //用户标识
    msg_id //服务器消息id
    raw_id //创建id
    read //是否已读
    topic //来自的topic
    send_status //发送状态 0发送中 1发送成功 2发送失败
    type //p2p group chatroom
    create_time //创建时间
    last_update_time //最后更新时间
    chat_type //text聊天 image发送图片 voice语音 file文件 video视频
    sender_id //发送者UID
    sender_name //发送者昵称
    receiver_id //接收方ID
    content //消息json
    
    **会话表**
    id //主键
    user_id //用户标识
    peer //对方id
    type //会话类型
    title //会话标题（昵称、聊天室群聊名称）
    unread_count //未读数
    last_message //最后一条消息内容
    last_message_time //最后一条消息时间
    last_update_time //最后更新时间
    
    *联系人表
    uid //主键 用户id
    name //昵称
    avatar //头像url
    remark //备注
  
 - **推送管理**


    PushManager管理
    //收到消息的推送规则在这里
    fun notifyContent(message: Message)  
        
 - **扩展功能**

    

    **拦截消息**
    FilterManager管理
    //具体消息具体自己处理 return true 代表拦截消息，将不会被监听到 false正常监听
    fun filterMessage(msg: Message): Boolean
    

**5.相关逻辑**
 - **会话列表逻辑**


    **ConversationManager管理**

    **数据获取**
    api获取会话和本地表获取会话对比 
    (本地记录为空，则api数据插入本地记录，返回api列表；本地记录不为空，api记录覆盖本地已有记录，返回本地列表)
    **删除会话**
    本地记录删除，下次拉取api自动以本地为准
    **新消息存入会话**
    新消息插入前与本地记录对比 存在该会话则覆盖，否则插入
    **未来清除缓存或者退出登录 清空会话表**
    
 - **推送逻辑**


    **PushManager管理**
    默认前台只有震动，App进入后台才显示推送
    可以在管理器中任意拦截消息自己设置通知

 - **拦截器逻辑**


    **FilterManager管理**
    可以拦截所有消息自己处理，被拦截的消息将不会被监听到
    
    
 - **添加好友整体逻辑**


    **ContactsManager管理**
    A调用添加好友api
    B接收到自定义消息101，通讯录和新朋友 红点提示
    B进入列表调用同意或拒绝api，同意则显示在聊天列表，拒绝不处理
    A收到自定义消息102同意or103拒绝，同意则显示在聊天列表、红点提示；拒绝不处理