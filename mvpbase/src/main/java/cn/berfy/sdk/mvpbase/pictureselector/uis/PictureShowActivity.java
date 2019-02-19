package cn.berfy.sdk.mvpbase.pictureselector.uis;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cn.berfy.sdk.mvpbase.R;
import cn.berfy.sdk.mvpbase.base.CommonActivity;
import cn.berfy.sdk.mvpbase.pictureselector.entities.ImageEntity;
import cn.berfy.sdk.mvpbase.pictureselector.uis.fragments.ScaleImageFragment;
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter;
import cn.berfy.sdk.mvpbase.util.AnimUtil;

public class PictureShowActivity extends CommonActivity {

    public static void launcher(Activity context, String path) {
        Intent intent = new Intent(context, PictureShowActivity.class);
        intent.putExtra("path", path);
        if (AnimUtil.checkJump()) {
            context.startActivity(intent);
            context.overridePendingTransition(R.anim.translate_to_show, R.anim.translate_to_hold);
        }
    }

    @Override
    public int getContentViewId() {
        return R.layout.ps_picture_show;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
    }

    @Override
    public void initView() {
        ImageEntity imageEntity = new ImageEntity(getIntent().getStringExtra("path"));
        ScaleImageFragment scaleImageFragment = new ScaleImageFragment(imageEntity);
        getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, scaleImageFragment).commit();
    }

    @Override
    public BasePresenter initPresenter() {
        return null;
    }

    /**
     * Page中的图片被点击
     */
    @SuppressWarnings("UnusedParameters")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ScaleImageFragment.OnImageClicked onImageClicked) {
        close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void close() {
        finish();
        overridePendingTransition(R.anim.translate_to_show, R.anim.translate_to_dismiss);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            close();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
