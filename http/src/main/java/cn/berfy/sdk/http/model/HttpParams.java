package cn.berfy.sdk.http.model;

import java.util.LinkedHashMap;

import static cn.berfy.sdk.http.model.HttpParams.POST_TYPE.POST_TYPE_JSON;

/**
 * Created by Berfy on 2017/12/19.
 * http参数
 */
public class HttpParams {

    private LinkedHashMap<String, Object> mHeaders;
    private LinkedHashMap<String, Object> mParams;
    private POST_TYPE mPostContentType = POST_TYPE_JSON;

    public HttpParams putParam(String key, Object value) {
        // TODO Auto-generated method stub
        if (null == mParams) {
            mParams = new LinkedHashMap<String, Object>();
        }
        if (!mParams.containsKey(key)) {
            mParams.put(key, value);
        }
        return this;
    }

    public LinkedHashMap<String, Object> getParams() {
        if (null == mParams) {
            mParams = new LinkedHashMap<String, Object>();
        }
        return mParams;
    }

    public void setParams(LinkedHashMap<String, Object> params) {
        mParams = params;
    }

    public HttpParams putHeader(String key, Object value) {
        if (null == mHeaders) {
            mHeaders = new LinkedHashMap<String, Object>();
        }
        if (!mHeaders.containsKey(key)) {
            mHeaders.put(key, value);
        }
        return this;
    }

    public LinkedHashMap<String, Object> getHeaders() {
        if (null == mHeaders) {
            mHeaders = new LinkedHashMap<String, Object>();
        }
        return mHeaders;
    }

    public void setHeaders(LinkedHashMap<String, Object> headers) {
        mHeaders = headers;
    }

    public enum POST_TYPE {
        POST_TYPE_FORM,//post表单请求
        POST_TYPE_JSON;//post body json
    }

    public HttpParams setContentType(POST_TYPE type) {
        mPostContentType = type;
        return this;
    }

    public POST_TYPE getContentType() {
        if (null == mPostContentType) {
            return POST_TYPE.POST_TYPE_FORM;
        }
        return mPostContentType;
    }
}
