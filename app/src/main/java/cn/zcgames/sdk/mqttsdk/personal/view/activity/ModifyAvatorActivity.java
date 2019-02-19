package cn.zcgames.sdk.mqttsdk.personal.view.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import cn.berfy.sdk.mvpbase.base.CommonActivity;
import cn.zcgames.sdk.mqttsdk.personal.view.iView.IMineView;
import com.bumptech.glide.Glide;
import com.kevin.crop.UCrop;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import cn.berfy.sdk.http.HttpApi;
import cn.berfy.sdk.http.callback.HttpUploadCallBack;
import cn.berfy.sdk.http.model.NetError;
import cn.berfy.sdk.http.model.NetResponse;
import cn.berfy.sdk.mvpbase.config.CacheConstant;
import cn.berfy.sdk.mvpbase.util.GsonUtil;
import cn.berfy.sdk.mvpbase.util.JsonUtil;
import cn.berfy.sdk.mvpbase.util.LogF;
import cn.berfy.sdk.mvpbase.util.ToastUtil;
import cn.zcgames.sdk.im.R;
import cn.zcgames.sdk.mqttsdk.app.MyApplication;
import cn.zcgames.sdk.mqttsdk.contacts.event.UpdateUserInfoEvent;
import cn.zcgames.sdk.mqttsdk.personal.model.ImageBean;
import cn.zcgames.sdk.mqttsdk.personal.model.PersonInfoBean;
import cn.zcgames.sdk.mqttsdk.personal.presenter.MinePresenter;
import cn.zcgames.sdk.mqttsdk.personal.util.ImageCropUtils;

public class ModifyAvatorActivity extends CommonActivity<IMineView, MinePresenter> implements IMineView {
    protected static final String TAG = "ModifyAvatorActivity";

    private static final int REQUEST_LABEL_CAMERA_ICON = 1; // 相机拍照标记
    private static final int REQUEST_LABEL_ALBUM_ICON = 2; // 相册选图标记

    @BindView(R.id.iv_avatar)
    ImageView mIvAvatar;

    //图片剪裁类
    private ImageCropUtils mCropUtils;
    private File mDestDir;
    private File mFile;
    private Uri currentPath;
    //选取的地址
    private Uri iconUri;


    public static void luncher(Context context) {
        Intent intent = new Intent(context, ModifyAvatorActivity.class);
        context.startActivity(intent);
    }


    @Override
    public int getContentViewId() {
        return R.layout.activity_modify_avator_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mDestDir = new File(CacheConstant.DIR_PUBLIC_ROOT + "/TempMultiMedia");
        mCropUtils = new ImageCropUtils(this);
    }

    @Override
    public void initView() {
        LogF.d(TAG,"==="+MyApplication.getCurrLoginUser().getPlayer().getAvatar());

        showTitleBar();
        getTitleBar().setTitle(getString(R.string.modify_avatar));
        getTitleBar().setRightIcon(true, R.drawable.modify_avatar_more_icon, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup();
            }
        });

        Glide.with(this).load(MyApplication.getCurrLoginUser().getPlayer().getAvatar()).dontAnimate().
                placeholder(R.drawable.big_place_holder).centerCrop().into(mIvAvatar);
    }

    @Override
    public MinePresenter initPresenter() {
        return new MinePresenter();
    }


    //选择弹框
    private void showPopup() {
        View contentView = LayoutInflater.from(this).inflate(R.layout.popup_select_image_layout, null);
        TextView tvCamera = contentView.findViewById(R.id.tv_camera);
        TextView tvPhone = contentView.findViewById(R.id.tv_phone);
        TextView tvCancel = contentView.findViewById(R.id.tv_cancel);

        PopupWindow popupWindow = new PopupWindow(this);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setContentView(contentView);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.showAtLocation(contentView, Gravity.BOTTOM, 0, 0);

        tvCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                RxPermissions rxPermissions = new RxPermissions(ModifyAvatorActivity.this);
                rxPermissions.request(Manifest.permission.CAMERA)
                        .subscribe(granted -> {
                            if (granted) { // Always true pre-M
                                // I can control the camera now
                                Intent iconCameraIntent = new Intent();
                                iconCameraIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                                // 指定调用相机拍照后照片的储存路径
                                String name = DateFormat.format("yyyy-MM-dd--hh-mm-ss", Calendar.getInstance(Locale.CHINA)) + ".jpg";
                                if (!mDestDir.exists()) {
                                    mDestDir.mkdirs();
                                }
                                mFile = new File(mDestDir, name);
                                //7.0权限适配
                                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                                    currentPath = FileProvider.getUriForFile(mContext,
                                            mContext.getPackageName() + ".fileprovider", mFile);
                                } else {
                                    currentPath = Uri.fromFile(mFile);
                                }
                                iconCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPath);
                                startActivityForResult(iconCameraIntent, REQUEST_LABEL_CAMERA_ICON);
                            } else {// Oups permission denied
                                Intent settingIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                settingIntent.setData(uri);
                                startActivity(settingIntent);
                            }
                            popupWindow.dismiss();

                        });
            }
        });
        tvPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, REQUEST_LABEL_ALBUM_ICON);
                popupWindow.dismiss();
            }
        });
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_LABEL_CAMERA_ICON:   // 调用相机拍照
                    iconUri = currentPath;
                    mCropUtils.startCropActivity(this, iconUri, this, 0);
                    break;
                case REQUEST_LABEL_ALBUM_ICON:  // 直接从相册获取
                    if (data != null) {
                        Uri uri = data.getData();
                        iconUri = uri;
                        mCropUtils.startCropActivity(this, iconUri, this, 0);
                    }
                    break;
                case ImageCropUtils.REQUEST_ALBUM_ICON:    // 裁剪结果
                    iconUri = mCropUtils.handleCropResult(data, iconUri);
                    if (iconUri != null) {
                        uploadToServer(iconUri.getPath());
                    }
                    break;
                case UCrop.RESULT_ERROR:    // 裁剪图片错误
                    mCropUtils.handleCropError(data);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    //上传图片
    private void uploadToServer(String imgUrl) {
        LogF.d(TAG, "===" + imgUrl);

        HttpApi.getInstances().postFile("http://223.203.221.89:9080/", "upload/images", imgUrl, new HttpUploadCallBack() {
            @Override
            public void uploadProgress(float pro, boolean isDone, int id) {
                LogF.d(TAG, "=uploadProgress==" + pro + "==" + isDone + "==" + id);
            }

            @Override
            public void onFinish(NetResponse<String> response) {
                LogF.d(TAG, "=onFinish==" + response.statusCode + "==" + response.data);
                if (response.statusCode == 200) {
                    ImageBean bean = GsonUtil.getInstance().toClass(JsonUtil.getFieldValue(response.data, "data"), ImageBean.class);
                    String netImgUrl = bean.getUrl();
                    LogF.d(TAG, "=onFinish==" + netImgUrl);

                    Glide.with(ModifyAvatorActivity.this).load(netImgUrl).dontAnimate().
                            placeholder(R.drawable.big_place_holder).centerCrop().into(mIvAvatar);
                    mPresenter.modifyAvatar(netImgUrl);
                }
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onError(NetError error) {
                LogF.d(TAG, "=onError==" + error.errMsg);
            }
        });
    }

    @Override
    public void modifyNick(boolean isOk) {
    }

    @Override
    public void modifyAvatar(boolean isOk) {
        if (isOk) {
            //更新数据
            EventBus.getDefault().post(new UpdateUserInfoEvent(true));
            ToastUtil.getInstances().showShort(getString(R.string.modify_success));
            finish();
        } else {
            ToastUtil.getInstances().showShort(getString(R.string.modify_failure));
        }
    }


    @Override
    public void getMyInfo(boolean isOk, PersonInfoBean user) {
    }

    @Override
    public void hiddenLoadingView(@org.jetbrains.annotations.Nullable String msg) {

    }

    @Override
    public void showLoadingView(@org.jetbrains.annotations.Nullable String msg) {

    }
}
