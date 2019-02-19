package cn.berfy.sdk.mvpbase.pictureselector.uis.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import cn.berfy.sdk.mvpbase.R;
import cn.berfy.sdk.mvpbase.pictureselector.entities.FinishEntity;
import cn.berfy.sdk.mvpbase.pictureselector.entities.ImageEntity;
import cn.berfy.sdk.mvpbase.pictureselector.entities.PageChangeEntity;
import cn.berfy.sdk.mvpbase.pictureselector.utils.PSConstanceUtil;
import cn.berfy.sdk.mvpbase.pictureselector.utils.PSCropUtil;
import cn.berfy.sdk.mvpbase.pictureselector.views.ClipImageLayout;
import cn.berfy.sdk.mvpbase.util.ToastUtil;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableObserver;


/**
 * created by arvin on 16/8/28 00:02
 * email：1035407623@qq.com
 */
public class CropFragment extends BaseFragment {
    private ClipImageLayout imgClip;
    private boolean isFromTakePhotoCrop = false;

    @Override
    protected int setContentViewId() {
        return R.layout.ps_fragment_crop;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        imgClip = getView(R.id.img_clip);
        tvTitle.setText("裁剪");
        tvEnsure.setEnabled(true);
        mSelectedImages = new ArrayList<>();
        update(getArguments());
    }

    @Override
    public void update(Bundle data) {
        isFromTakePhotoCrop = data.getBoolean(PSConstanceUtil.PASS_EXTRA, false);

        ImageEntity item = data.getParcelable(PSConstanceUtil.PASS_SHOW);
        if (item == null) {
            onBackClicked();
            return;
        }
        mSelectedImages.clear();
        mSelectedImages.add(item);

        final long time = System.currentTimeMillis();
        Glide.with(this).load(item.getPath()).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                Log.i("加载耗时", (System.currentTimeMillis() - time) + "");
                if (resource != null) {
                    imgClip.setImageBitmap(resource);
                    Log.i("显示", (System.currentTimeMillis() - time) + "");
                } else {
                    ToastUtil.getInstances().showShort("没有找到该图片~");
                    onBackClicked();
                }
            }
        });
    }

    @Override
    protected void onEnsureClicked() {
        PSCropUtil.crop(imgClip).subscribe(new DisposableObserver<String>() {
            @Override
            public void onNext(@NonNull String s) {
                mSelectedImages.clear();
                mSelectedImages.add(new ImageEntity(s, true));
                EventBus.getDefault().post(mSelectedImages);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    public void onBackClicked() {
        if (isFromTakePhotoCrop) {
            EventBus.getDefault().post(new FinishEntity());
            return;
        }
        EventBus.getDefault().post(new PageChangeEntity(PageChangeEntity.PageId.PictureSelector, null));
    }
}
