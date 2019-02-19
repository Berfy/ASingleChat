package cn.berfy.sdk.http.http.interceptor;

import com.google.gson.reflect.TypeToken;

import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import cn.berfy.sdk.http.callback.OnStatusListener;
import cn.berfy.sdk.http.http.okhttp.utils.GsonUtil;
import cn.berfy.sdk.http.http.okhttp.utils.HLogF;
import cn.berfy.sdk.http.model.HttpParams;
import okio.Buffer;

/**
 * 接口签名
 */
public class Md5SignInterceptor implements Interceptor {

    private String TAG = "Md5SignInterceptor";
    private OnStatusListener mOnStatusListener;

    public Md5SignInterceptor(String tag, OnStatusListener onStatusListener) {
        TAG = tag;
        mOnStatusListener = onStatusListener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if ("GET".equals(request.method())) {
            request = addGetParams(request);
        } else if ("POST".equals(request.method())) {
            RequestBody requestBody = request.body();
            boolean hasRequestBody = requestBody != null;
            if (!hasRequestBody) {
                HLogF.d(TAG, "------" + request.method());
            } else if (bodyEncoded(request.headers())) {
                HLogF.d(TAG, "------" + request.method() + " (encoded body omitted)");
            } else {
                if (request.body().contentType().toString().contains("application/json")) {
                    request = addPostParamsJson(request);
                } else if (request.body().contentType().toString().contains("text/plain")) {
                    request = addPostParams(request);
                }
            }
        }
        return chain.proceed(request);
    }

    private Request addGetParams(Request request) throws UnsupportedEncodingException {
        //添加时间戳
        HttpUrl.Builder httpBuilder = request.url().newBuilder();
        HttpUrl httpUrl = httpBuilder
                .build();
        Request.Builder builder = request.newBuilder();
        //添加签名
        Set<String> nameSet = httpUrl.queryParameterNames();
        ArrayList<String> nameList = new ArrayList<>();
        nameList.addAll(nameSet);
        HttpParams httpParams = new HttpParams();
        Headers oldHeaders = request.headers();
        for (int i = 0; i < oldHeaders.size(); i++) {
            httpParams.putHeader(oldHeaders.name(i), oldHeaders.value(i));
        }
//        Collections.sort(nameList);
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < nameList.size(); i++) {
            String name = nameList.get(i);
            String value = httpUrl.queryParameterValues(name) != null &&
                    httpUrl.queryParameterValues(name).size() > 0 ?
                    URLEncoder.encode(httpUrl.queryParameterValues(name).get(0), "UTF-8") : "";
            if (i > 0) buffer.append("&");
            httpParams.putParam(name, value);
            buffer.append(name).append("=").append(value);
        }
        if (null != mOnStatusListener) {
            //处理增加的参数
            HttpParams formatParams = mOnStatusListener.addParamsOrHeaders(httpParams);
            if (null != formatParams) {
                if (null != formatParams.getParams()) {//需要添加参数
//                    //删除旧参数
//                    Iterator<Map.Entry<String, Object>> oldParams = httpParams.getParams().entrySet().iterator();
//                    while (oldParams.hasNext()) {
//                        Map.Entry<String, Object> entry = oldParams.next();
//                        httpBuilder.removeAllQueryParameters(entry.getKey().trim());
//                    }
                    //添加新参数
                    Iterator<Map.Entry<String, Object>> params = formatParams.getParams().entrySet().iterator();
                    while (params.hasNext()) {
                        Map.Entry<String, Object> entry = params.next();
                        httpBuilder.addQueryParameter(entry.getKey().trim(), entry.getValue().toString());
                    }
                }
                if (null != formatParams.getHeaders()) {//需要添加头部
                    //添加新头部
                    Iterator<Map.Entry<String, Object>> headers = formatParams.getHeaders().entrySet().iterator();
                    while (headers.hasNext()) {
                        Map.Entry<String, Object> entry = headers.next();
                        builder.addHeader(entry.getKey().trim(), entry.getValue().toString());
                    }
                }
            }
        }
        httpUrl = httpBuilder
                .build();
        request = builder
                .url(httpUrl)
                .build();
        return request;
    }

    private Request addPostParams(Request request) throws IOException {
        Request.Builder builder = request.newBuilder();
        Buffer buffer = new Buffer();//赋值缓冲区
        //form表单
        FormBody.Builder bodyBuilder = new FormBody.Builder();
        FormBody formBody;
        if (request.body() instanceof FormBody) {
            formBody = (FormBody) request.body();
            for (int i = 0; i < formBody.size(); i++) {
//                LogF.d("有没有东西", formBody.encodedName(i) + "====" + formBody.encodedValue(i));
                bodyBuilder.addEncoded(formBody.encodedName(i), formBody.encodedValue(i));
            }
            formBody = bodyBuilder.build();
            formBody.writeTo(buffer);
        }

        String bodys = new String(buffer.readByteArray());
        String[] keyvalues = bodys.split("&");
        HttpParams httpParams = new HttpParams();
        Headers oldHeaders = request.headers();
        for (int i = 0; i < oldHeaders.size(); i++) {
            httpParams.putHeader(oldHeaders.name(i), oldHeaders.value(i));
        }
        if (keyvalues.length > 0) {
            for (int i = 0; i < keyvalues.length; i++) {
                String[] param = keyvalues[i].split("=");
                if (param.length == 2) {
                    httpParams.putParam(param[0], param[1]);
                }
            }
        }
        if (null != mOnStatusListener) {
            //处理增加的参数
            HttpParams formatParams = mOnStatusListener.addParamsOrHeaders(httpParams);
            if (null != formatParams) {
                if (null != formatParams.getParams()) {//需要添加参数
                    Iterator<Map.Entry<String, Object>> params = formatParams.getParams().entrySet().iterator();
                    while (params.hasNext()) {
                        Map.Entry<String, Object> entry = params.next();
                        bodyBuilder.addEncoded(entry.getKey().trim(), entry.getValue().toString());
                    }
                }
                if (null != formatParams.getHeaders()) {//需要添加头部
                    Iterator<Map.Entry<String, Object>> headers = formatParams.getHeaders().entrySet().iterator();
                    while (headers.hasNext()) {
                        Map.Entry<String, Object> entry = headers.next();
                        builder.addHeader(entry.getKey().trim(), entry.getValue().toString());
                    }
                }
            }
        }
        if (request.body() instanceof FormBody) {
            formBody = bodyBuilder.build();
            request = builder
                    .post(formBody).build();
        } else if (request.body() instanceof MultipartBody) {
            formBody = bodyBuilder.build();
            request = builder
                    .post(formBody).build();
        }


//        //md5签名
//        String sign = Hmac.md5(buffer.readByteArray());

        return request;
    }

    private Request addPostParamsJson(Request request) throws IOException {
        Buffer buffer = new Buffer();
        //取出原始json参数
        request.body().writeTo(buffer);
        Request.Builder builder = request.newBuilder();
        //解析成json
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(new String(buffer.readByteArray(), "UTF-8"));
            //添加sign_ts
//            jsonObject.put("sign_ts", String.valueOf(System.currentTimeMillis() / 1000));
            if (null != mOnStatusListener) {
                HttpParams httpParams = new HttpParams();
                Headers oldHeaders = request.headers();
                for (int i = 0; i < oldHeaders.size(); i++) {
                    httpParams.putHeader(oldHeaders.name(i), oldHeaders.value(i));
                }
                Type type = new TypeToken<LinkedHashMap<String, Object>>() {
                }.getType();
                LinkedHashMap<String, Object> map = GsonUtil.getInstance().toClass(jsonObject.toString(), type);
                httpParams.setParams(map);
                //处理增加的参数
                HttpParams formatParams = mOnStatusListener.addParamsOrHeaders(httpParams);
                if (null != formatParams) {
                    if (null != formatParams.getParams()) {//需要添加参数
                        Iterator<Map.Entry<String, Object>> params = formatParams.getParams().entrySet().iterator();
                        while (params.hasNext()) {
                            Map.Entry<String, Object> entry = params.next();
                            jsonObject.put(entry.getKey().trim(), entry.getValue().toString());
                        }
                    }
                    if (null != formatParams.getHeaders()) {//需要添加头部
                        Iterator<Map.Entry<String, Object>> headers = formatParams.getHeaders().entrySet().iterator();
                        while (headers.hasNext()) {
                            Map.Entry<String, Object> entry = headers.next();
                            builder.addHeader(entry.getKey().trim(), entry.getValue().toString());
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (null != jsonObject) {
            RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
            //md5签名
//            String sign = Hmac.md5(buffer.readByteArray());
            request = builder
                    .post(requestBody)
                    .build();
        }
        return request;
    }

    private boolean bodyEncoded(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null && !contentEncoding.equalsIgnoreCase("identity");
    }
}
