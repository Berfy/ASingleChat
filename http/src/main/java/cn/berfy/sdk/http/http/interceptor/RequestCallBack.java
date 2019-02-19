package cn.berfy.sdk.http.http.interceptor;

import cn.berfy.sdk.http.model.NetResponse;

/**
 * 
* @ClassName: RequestCallBack 
* @Description: 接口回调处理
* @author Berfy
*
 */
public interface RequestCallBack<T> {
	/**
	 * 请求开始
	 */
	void start();

	/**
	 * 请求结束 回传需要处理的类型
	 * 
	 * @param result
	 */
	void finish(NetResponse<T> result);
}
