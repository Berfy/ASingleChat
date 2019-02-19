package cn.zcgames.sdk.mqttsdk.personal.view.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.zxing.WriterException;

import butterknife.BindView;
import cn.berfy.sdk.mvpbase.base.CommonActivity;
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter;
import cn.berfy.sdk.mvpbase.util.DisplayUtil;
import cn.berfy.sdk.mvpbase.view.CircleImageView;
import cn.zcgames.sdk.im.R;
import cn.zcgames.sdk.mqttsdk.app.MyApplication;
import cn.zcgames.sdk.mqttsdk.personal.util.QrCodeUtil;
import cn.zcgames.sdk.mqttsdk.zxing.encode.QRCodeEncoder;

public class MySconCodeActivity extends CommonActivity {

    @BindView(R.id.iv_avatar)
    CircleImageView mIvAvatar;
    @BindView(R.id.tv_nick)
    TextView mTvNick;
    @BindView(R.id.iv_code)
    ImageView mIvCode;


    public static void luncher(Context context){
        Intent intent=new Intent(context,MySconCodeActivity.class);
        context.startActivity(intent);
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_my_code_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

    }

    @Override
    public void initView() {
        showTitleBar();
        getTitleBar().setTitle(getString(R.string.my_scan_code));

        mTvNick.setText(MyApplication.getCurrLoginUser().getPlayer().getNickname());
        Glide.with(this).load(MyApplication.getCurrLoginUser().getPlayer().getAvatar()).dontAnimate().
                placeholder(R.drawable.placeholder).centerCrop().into(mIvAvatar);

        String uid= MyApplication.getCurrLoginUser().getPlayer().getId();
        Bitmap mBitmap = null;
        try {
            mBitmap = QRCodeEncoder.encodeAsBitmap(uid, DisplayUtil.dip2px(312));
            mIvCode.setImageBitmap(mBitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BasePresenter initPresenter() {
        return null;
    }
}
