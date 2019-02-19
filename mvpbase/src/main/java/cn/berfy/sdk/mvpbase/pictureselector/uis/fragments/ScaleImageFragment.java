package cn.berfy.sdk.mvpbase.pictureselector.uis.fragments;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;

import org.greenrobot.eventbus.EventBus;

import cn.berfy.sdk.mvpbase.R;
import cn.berfy.sdk.mvpbase.pictureselector.entities.ImageEntity;
import cn.berfy.sdk.mvpbase.pictureselector.subscaleview.ImageSource;
import cn.berfy.sdk.mvpbase.pictureselector.subscaleview.ImageViewState;
import cn.berfy.sdk.mvpbase.pictureselector.subscaleview.SubsamplingScaleImageView;
import cn.berfy.sdk.mvpbase.pictureselector.utils.PSGlideUtil;
import cn.berfy.sdk.mvpbase.pictureselector.views.PhotoViewAttacher;

/**
 * created by arvin on 16/9/3 17:16
 * email：1035407623@qq.com
 */
public class ScaleImageFragment extends Fragment implements PhotoViewAttacher.OnPhotoTapListener, View.OnClickListener {
    private View mRoot;
    private SubsamplingScaleImageView imgLong;
    private ImageView imgScale;
    private PhotoViewAttacher mAttacher;
    private ImageEntity mItem;

    public ScaleImageFragment() {
    }

    @SuppressLint("ValidFragment")
    public ScaleImageFragment(ImageEntity item) {
        this.mItem = item;
    }

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRoot = LayoutInflater.from(getActivity()).inflate(R.layout.ps_fragment_scale_image, null);
        init(savedInstanceState);
        return mRoot;
    }

    @SuppressWarnings("UnusedParameters")
    private void init(Bundle savedInstanceState) {
        imgLong = (SubsamplingScaleImageView) mRoot.findViewById(R.id.img_long);
        imgScale = (ImageView) mRoot.findViewById(R.id.img_normal);
        mRoot.findViewById(R.id.layout_touch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new OnImageClicked());
            }
        });
        mAttacher = new PhotoViewAttacher(imgScale);
        mAttacher.setOnPhotoTapListener(ScaleImageFragment.this);
        try {
            Glide.with(this).load(mItem.getPath()).asBitmap().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    if (resource.getWidth() * 3 <= resource.getHeight()) {
                        imgLong.setVisibility(View.VISIBLE);
                        imgScale.setVisibility(View.INVISIBLE);
                        imgLong.setImage(ImageSource.uri(mItem.getPath()), new ImageViewState(0.0f, new PointF(0, 0), 0));
                        imgLong.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);
                        imgLong.setOnClickListener(ScaleImageFragment.this);
                        mRoot.findViewById(R.id.layout_touch).setVisibility(View.GONE);
                    } else {
                        String path = mItem.getPath();
                        if (path.indexOf("http://") < 0 && path.indexOf("https://") < 0) {
                            path = "file://" + path;
                        }
                        PSGlideUtil.loadImage(getActivity(), path, imgScale, new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                imgLong.setVisibility(View.INVISIBLE);
                                imgScale.setVisibility(View.VISIBLE);
                                mAttacher = new PhotoViewAttacher(imgScale);
                                mAttacher.setOnPhotoTapListener(ScaleImageFragment.this);
                                mRoot.findViewById(R.id.layout_touch).setVisibility(View.GONE);
                                return false;
                            }
                        });
                    }
                }
            });
        } catch (Exception e) {
        }
    }

    @Override
    public void onPhotoTap(View view, float x, float y) {
        EventBus.getDefault().post(new OnImageClicked());
    }

    @Override
    public void onOutsidePhotoTap() {
    }

    @Override
    public void onClick(View v) {
        EventBus.getDefault().post(new OnImageClicked());

    }

    public class OnImageClicked {
    }
}
