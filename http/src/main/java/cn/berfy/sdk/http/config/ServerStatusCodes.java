package cn.berfy.sdk.http.config;

/**
 * Created by Berfy on 2017/12/28.
 * 服务器错误状态码
 */

public class ServerStatusCodes { /**常用code状态码(解释)*/

    /**
     * 成功码
     */
    public static final int RET_CODE_SUCCESS = 1;

    /**
     * 错误码(系统出错)
     */
    public static final int RET_CODE_SYSTEM_ERROR = -1;

    /**
     * 没有网
     */
    public static final int NO_NET = -2;

    /**
     * 新旧手机号相同
     */
    public static final int RET_CODE_PHONENUMBER_NEWOLD_SAME = -804;

    /**
     * 新手机号已绑定账号
     */
    public static final int RET_CODE_NEW_PHONE_HAS_BIND = -803;

    /**
     * 签名失败
     */
    public static final int RET_CODE_SIGNINVALID = -801;

    /**
     * 版本更新码
     */
    public static final int UPDATE_CODE = 58;

    /**
     * 没有登录
     */
    public static final int RET_CODE_NOT_LOGIN = -800;

    /**
     * 没有地址
     */
    public static final int RET_CODE_NOT_ADDRESS = -799;

    /**
     * 没有手机号
     */
    public static final int RET_CODE_NO_PHONE = -798;

    /**
     * 没有中奖
     */
    public static final int RET_CODE_NO_WINNING = -797;

    /**
     * 已经中过奖
     */
    public static final int RET_CODE_ALREADY_WINNING = -796;

    /**
     * 兑换奖品失败
     */
    public static final int RET_CODE_EXCHANGE_FAILURE = -795;

    /**
     * 用户名密码错误
     */
    public static final int RET_CODE_PASSWORD_ERROR = -794;

    /**
     * 密码不合法
     */
    public static final int RET_CODE_PASSWORD_ILLEGAL = -793;

    /**
     * 手机号已经注册过
     */
    public static final int RET_CODE_ALREADY_REGISTER = -792;

    /**
     * 绑定数据失败
     */
    public static final int RET_CODE_BIND_FAILURE = -791;


    /**
     * 数据为空
     */
    public static final int RET_CODE_DATA_EMPTY = -790;

    /**
     * 不能重复关注
     */
    public static final int RET_CODE_UNABLE_FOCUS_AGAIN = -763;

    /**
     * 获取验证码次数超过限制
     */
    public static final int GET_SMS_CODE = -700;

    /**
     * 获取验证时间过短
     */
    public static final int GET_SMS_SHORT = -699;

    /**
     * 数据库出错
     */
    public static final int RET_CODE_DATABASE_ERROR = -789;


    /**
     * 数据不合法
     */
    public static final int RET_CODE_PARAM_ILLEGAL = -788;

    /**
     * 重复记录
     */
    public static final int RET_CODE_REPEAT_RECORD = -787;

    /**
     * 删除失败
     */
    public static final int RET_CODE_DELETE_FAILURE = -786;

    /**
     * 号码不合法
     */
    public static final int RET_CODE_PHONE_ILLEGAL = -785;

    /**
     * 账户已经存在
     */
    public static final int RET_CODE_ACCOUNT_EXIST = -784;

    /**
     * 检测到您的账号在另外一台设备上登录,请确认是否重新登录？
     */
    public static final int RET_CODE_ROBBED = -783;

    /**
     * "您的用户已经被冻结，请联系公众号(猩猩同盟)解冻"
     */
    public static final int RET_CODE_FROZEN = -782;

    /**
     * 封号
     */
    public static final int RET_CODE_DISMISS_ACCOUNT = -781;

    /**
     * 红包余额不足
     */
    public static final int RET_CODE_BONUS_BALANCE_NOT_ENOUGH = -780;

    /**
     * 上传文件格式出错
     */
    public static final int RET_CODE_FORMAT_ERROR = -779;

    /**
     * 上传文件失败
     */
    public static final int RET_CODE_UPLOAD_FAILURE = -778;

    /**
     * redis存储系统出错
     */
    public static final int RET_CODE_REDIS_ERROR = -777;

    /**
     * 对方将你拉入黑名单
     */
    public static final int RET_CODE_DRAW_IN_BLACKLIST = -776;

    /**
     * 你将对方拉入黑名单
     */
    public static final int RET_CODE_DRAW_BLACKLIST = -772;
    /**
     * 参数错误
     */
    public static final int RET_CODE_PARAM_ERROR = -775;

    /**
     * 该用户设置了只允许会员邀请他
     */
    public static final int RET_CODE_ONLY_ACCEPT_VIP = -774;

    /**
     * 普通用户一天只能在榜单上约人两次,如想再约,请充值会员!
     */
    public static final int RET_CODE_TALK_ABOUT_LIMIT = -773;

    /**
     * 当前用户已经是会员了!
     */
    public static final int RET_CODE_IS_VIP = -771;

    /**
     * 有保底通话时长，扣款失败!
     */
    public static final int RET_CODE_DEDUCT_FAILURE = -768;

    /**
     * 24小时内该手机获取验证码次数超限|
     */
    public static final int RET_CODE_FREQUENCY_OVERRUN = -700;

    /**
     * 获取短信验证间隔时间过短
     */
    public static final int RET_CODE_INTERVAL_TOO_SHORT = -699;

    /**
     * 此手机号没有发送过验证码
     */
    public static final int RET_CODE_NO_SEND_VERIFICATION_CODE = -698;

    /**
     * 验证码错误
     */
    public static final int RET_CODE_VERIFICATION_ERROR = -697;

    /**
     * 须绑定用户信息
     */
    public static final int RET_CODE_NEED_BIND_INFO = -696;

    /**
     * 昵称中含有污秽词
     */
    public static final int RET_CODE_NICK_HAS_FILTHY = -695;


    /**
     * 主叫用户余额不足
     */
    public static final int RET_CODE_BALANCE_NOT_ENOUGH = -650;

    /**
     * 被叫(富豪)余额不足
     */
    public static final int RET_CODE_CALLEE_BALANCE_NOT_ENOUGH = -642;

    /**
     * 用户昵称过长
     */
    public static final int RET_CODE_USER_NICK = -764;

    /**
     * 没有绑定手机号
     */
    public static final int RET_CODE_NO_BIND_PHONE = -640;

    /**
     * 当前用户是声优无法进行此操作!
     */
    public static final int RET_CODE_VC_WAS_FORBID = -643;

    /**
     * 发布的任务超过最大限制!
     */
    public static final int RET_CODE_TASK_OVERRUN = -600;

    /**
     * 任务已被删除
     */
    public static final int RET_CODE_TASK_DELETE = -599;

    /**
     * 绑定微信失败
     */
    public static final int RET_CODE_WEIXIN = -550;

    /**
     * 绑定qq失败
     */
    public static final int RET_CODE_QQ = -548;

    /**
     * 绑定微bo失败
     */
    public static final int RET_CODE_WEIBO = -547;

    /**
     * 约聊余额不足，无法通话
     */
    //code -811 余额不足，无法通话。
    public static final int RET_CODE_CALLPHONE = -811;
    /**
     * 该账号已经绑定过
     */
    public static final int RET_CODE_ALREADY_BIND_WX = -550;

    /**
     * 强制更新
     */
    public static final int RET_CODE_FORCED_UPDATE = -1000;

    /**
     * 注册小于24小时，不能提现
     */
    public static final int RET_CODE_WITHDRAW_REGISTER_LESS_24 = -805;

    /**
     * 官方声优无法提现
     */
    public static final int RET_CODE_WITHDRAW_OFFICIAL_VOICE = -806;

    /**
     * 提现失败
     */
    public static final int RET_CODE_WITHDRAW_FAIL = -807;

    /**
     * 提现超过每日限制次数
     */
    public static final int RET_CODE_WITHDRAW_MORE_TIMES = -808;

    /**
     * 通话提示更新版本(5.1以上版本给5.0.0打)
     */
    public static final int RET_CODE_UPDATE_SIGN = -3001;

    /**
     * 提示被叫版本过低，无法通话(5.0给4.21打)
     */
    public static final int RET_CODE_VERSION_LOW = -812;

    /**
     * 提示被叫版本过低，无法通话(5.0给4.21打)
     */
    public static final int PARTY_PHONE_HAS_BIND = -1001;

    //车队已满
    public static final int FLEET_IS_FULL = -20001;

    //没有网络错误
    public static final int ERROR_CODE_OK = 0;

    //没有网络
    public static final int ERROR_CODE_NONET = -1;

    //超时
    public static final int ERROR_CODE_TIMEOUT = -2;

    //未知错误
    public static final int ERROR_CODE_UNKOWMN = -3;

}
