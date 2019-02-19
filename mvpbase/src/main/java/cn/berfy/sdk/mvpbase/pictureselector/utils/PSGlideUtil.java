package cn.berfy.sdk.mvpbase.pictureselector.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;

import cn.berfy.sdk.mvpbase.R;


/**
 * Created by arvin on 2016/5/24
 */
public class PSGlideUtil {
    public static void loadLocalImage(Context context, int resId, ImageView imageView) {
        Glide.with(context).load(resId).placeholder(R.drawable.ps_img_loading).error(R.drawable.ps_img_loading).into(imageView);
    }

    public static void loadImage(Context context, String path, ImageView imageView) {
        Glide.with(context)
                .load(path)
                .placeholder(R.drawable.ps_img_loading)
                .error(R.drawable.ps_img_loading)
                .into(imageView);
    }

    public static void loadImage(Context context, String path, ImageView imageView, RequestListener<String, GlideDrawable> listener) {
        Glide.with(context).load(path).listener(listener).error(R.color.color_black).into(imageView);
    }

}
