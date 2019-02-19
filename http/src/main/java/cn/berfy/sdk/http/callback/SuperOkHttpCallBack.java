package cn.berfy.sdk.http.callback;


import android.text.TextUtils;

import org.json.JSONObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import cn.berfy.sdk.http.config.Constant;
import cn.berfy.sdk.http.config.ServerStatusCodes;
import cn.berfy.sdk.http.http.okhttp.utils.GsonUtil;
import cn.berfy.sdk.http.http.okhttp.utils.HLogF;
import cn.berfy.sdk.http.model.NetError;
import cn.berfy.sdk.http.model.NetMessage;
import cn.berfy.sdk.http.model.NetResponse;

/**
 * Created by Berfy on 2017/12/15.
 * http接口封装回调处理类
 */

public class SuperOkHttpCallBack<T> {

    private RequestCallBack<T> mRequestCallBack;
    private RequestCallBackH5<T> mRequestCallBackH5;
    private String mRequestCode;

    public SuperOkHttpCallBack(RequestCallBack<T> requestCallBack) {
        mRequestCallBack = requestCallBack;
    }

    public SuperOkHttpCallBack(RequestCallBackH5<T> requestCallBack, String requestCode) {
        mRequestCallBackH5 = requestCallBack;
        mRequestCode = requestCode;
    }

    public void start() {
        if (null != mRequestCallBack)
            mRequestCallBack.onStart();
        if (null != mRequestCallBackH5) {
            mRequestCallBackH5.onStart();
        }
    }

    public void finish(int statusCode, long usedTime, String response, boolean needGson) {
        if (!needGson) {
            NetResponse<T> netResponse = new NetResponse<T>();
            netResponse.statusCode = statusCode;
            netResponse.data = (T) response;
            netResponse.usedTime = usedTime;
            if (null != mRequestCallBack) {
                mRequestCallBack.onFinish(netResponse);
            }
            if (null != mRequestCallBackH5)
                mRequestCallBackH5.onFinish(netResponse);
        } else {
            NetResponse<T> netResponse = new NetResponse<T>();
            netResponse.usedTime = usedTime;
            netResponse.statusCode = statusCode;
            try {
                JSONObject json = new JSONObject(response);
                NetMessage netMessage = new NetMessage();
                netMessage.code = json.optInt("code");
                netMessage.msg = json.optString("msg");
                netMessage.err_msg = json.optString("err_msg");
                netMessage.ts = json.optLong("ts");
                netResponse.netMessage = netMessage;
                try {
                    if (!TextUtils.isEmpty(mRequestCode)) {
                        netMessage.requestCode = mRequestCode;
                        netResponse.data = jsonToClass(response);
                    } else {
                        String data = json.optString("data");
                        T t = jsonToClass(data);
                        if (null == t) {
                            try {
                                Type type = ((ParameterizedType) getClass()
                                        .getGenericSuperclass()).getActualTypeArguments()[0];
//                                HLogF.d(Constant.HTTPTAG, "类型  " + type.getTypeName());
                                netResponse.data = (T) GsonUtil.getInstance().toClass(data, type);
                            } catch (Exception e) {
                                HLogF.d(Constant.HTTPTAG, "类型有问题");
                                netResponse.data = (T) data;
                                e.printStackTrace();
                            }
                        } else {
//                        LogF.d(Constant.HTTPTAG, "类型  null != t");
                            netResponse.data = t;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    HLogF.d(Constant.HTTPTAG, "data数据解析失败：\n" + e.getMessage() + "\n" + e.getCause());
                }
//                netResponse.netMessage = netMessage;
                if (null != mRequestCallBack)
                    mRequestCallBack.onFinish(netResponse);
                if (null != mRequestCallBackH5)
                    mRequestCallBackH5.onFinish(netResponse);
            } catch (Exception e) {
                NetError netError = new NetError();
                netError.statusCode = statusCode;
                netError.errMsg = "数据格式错误";
                netError.usedTime = usedTime;
                if (null != mRequestCallBack) {
                    mRequestCallBack.onError(netError);
                }
                if (null != mRequestCallBackH5) {
                    mRequestCallBackH5.onErrorDetail(statusCode, ServerStatusCodes.ERROR_CODE_UNKOWMN);
                    mRequestCallBack.onError(netError);
                }
                e.printStackTrace();
                HLogF.d(Constant.HTTPTAG, "数据解析失败：\n" + e.getMessage() + "\n" + e.getCause());
            }
        }
    }

    protected T jsonToClass(String json) {
        return null;
    }

    public void error(int statusCode, long usedTime, String errMsg) {
        NetError netError = new NetError();
        netError.statusCode = statusCode;
        netError.errMsg = errMsg;
        netError.usedTime = usedTime;
        if (null != mRequestCallBackH5) {
            mRequestCallBackH5.onError(netError);
        }
        if (null != mRequestCallBack)
            mRequestCallBack.onError(netError);
    }

    /**
     * @param statusCode 服务器状态码
     * @param errCode    0没有错误 -1没有网络 -2网络超时
     */
    public void errorDetail(int statusCode, int errCode) {
        if (null != mRequestCallBackH5)
            mRequestCallBackH5.onErrorDetail(statusCode, errCode);
    }
}
