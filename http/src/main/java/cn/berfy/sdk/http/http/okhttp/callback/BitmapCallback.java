package cn.berfy.sdk.http.http.okhttp.callback;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import cn.berfy.sdk.http.callback.Callback;

import okhttp3.Response;

/**
 * Created by zhy on 15/12/14.
 */
public abstract class BitmapCallback extends Callback<Bitmap>
{
    @Override
    public Bitmap parseNetworkResponse(Response response , int id) throws Exception
    {
        return BitmapFactory.decodeStream(response.body().byteStream());
    }

}
