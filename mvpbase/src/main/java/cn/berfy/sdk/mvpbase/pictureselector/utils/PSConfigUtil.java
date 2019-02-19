package cn.berfy.sdk.mvpbase.pictureselector.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;

import java.util.ArrayList;

import cn.berfy.sdk.mvpbase.R;
import cn.berfy.sdk.mvpbase.base.EasyPermissions;
import cn.berfy.sdk.mvpbase.pictureselector.entities.ImageEntity;
import cn.berfy.sdk.mvpbase.pictureselector.uis.PictureSelectorActivity;
import cn.berfy.sdk.mvpbase.pictureselector.uis.PictureShowActivity;
import cn.berfy.sdk.mvpbase.pictureselector.uis.PicturesShowActivity;
import cn.berfy.sdk.mvpbase.util.AnimUtil;


/**
 * created by arvin on 16/8/30 22:36
 * email：1035407623@qq.com
 */
public class PSConfigUtil {

    private static final String endName = ".takephoto.fileprovider";
    private static String sAuthorities;

    private static PSConfigUtil sInstance;
    /**
     * 多选图片上限
     */
    private int mMaxCount;
    /**
     * 是否能拍照
     */
    private boolean canTakePhoto;
    /**
     * 是否能裁剪
     */
    private boolean canCrop;
    /**
     * 选中的数量
     */
    private int mSelectedCount;

    /**
     * 选中的文件夹的位置
     */
    private int mSelectedFolderPos;

    /**
     * 状态栏颜色
     */
    private int mStatusBarColor;

    private int mRightBtnTitleRes;//确定按钮自定义文字

    private PSConfigUtil() {
        this.mMaxCount = 9;
        this.canTakePhoto = true;
        this.canCrop = false;
        this.mSelectedCount = 0;
        this.mSelectedFolderPos = 0;
        this.mStatusBarColor = R.color.ps_colorPrimaryDark;
    }

    public static PSConfigUtil getInstance() {
        if (sInstance == null) {
            sInstance = new PSConfigUtil();
        }
        return sInstance;
    }

    public static String getAuthorities() {
        return sAuthorities;
    }

    public static void setAuthorities(String sAuthorities) {
        PSConfigUtil.sAuthorities = sAuthorities;
    }

    /**
     * @return 是否能继续选中图片
     */
    public boolean canAdd() {
        return mMaxCount > mSelectedCount;
    }

    public boolean canReview() {
//        return mMaxCount != 1;
        return true;
    }

    /**
     * 清除本次数据
     */
    public void clear() {
        sInstance = null;
    }

    public int getMaxCount() {
        return mMaxCount;
    }

    public PSConfigUtil setMaxCount(int maxCount) {
        this.mMaxCount = maxCount;
        return getInstance();
    }

    public PSConfigUtil setRightTitle(int titleResId) {
        mRightBtnTitleRes = titleResId;
        return getInstance();
    }

    public int getRightBtnTitleRes() {
        return mRightBtnTitleRes;
    }

    public boolean isCanTakePhoto() {
        return canTakePhoto;
    }

    public PSConfigUtil setCanTakePhoto(boolean canTakePhoto) {
        this.canTakePhoto = canTakePhoto;
        return getInstance();
    }

    public boolean isCanCrop() {
        return canCrop;
    }

    /**
     * @param canCrop 只有当maxCount == 1时有效
     */
    public PSConfigUtil setCanCrop(boolean canCrop) {
        this.canCrop = canCrop;
        return getInstance();
    }

    public int getSelectedCount() {
        return mSelectedCount;
    }

    public int addSelectedCount(int count) {
        mSelectedCount += count;
        return mSelectedCount;
    }

    public PSConfigUtil setSelectedCount(int count) {
        mSelectedCount = count;
        return getInstance();
    }

    public int getSelectedFolderPos() {
        return mSelectedFolderPos;
    }

    public void setSelectedFolderPos(int selectedFolderPos) {
        this.mSelectedFolderPos = selectedFolderPos;
    }

    public int getStatusBarColor() {
        return mStatusBarColor;
    }

    public PSConfigUtil setStatusBarColor(int mStatusBarColor) {
        this.mStatusBarColor = mStatusBarColor;
        return getInstance();
    }

    public void showSelector(Activity activity, int requestCode, ArrayList<ImageEntity> selectedImages) {
        String perms[] = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(activity, perms)) {
            initAuthorities(activity);
            Intent intent = new Intent(activity, PictureSelectorActivity.class);
            if (selectedImages != null) {
                if (selectedImages.size() > getMaxCount()) {
                    throw new RuntimeException("selectedImages' size can not more than maxCount!");
                }
            }
            intent.putParcelableArrayListExtra(PSConstanceUtil.PASS_SELECTED, selectedImages);
            if (AnimUtil.checkJump())
                activity.startActivityForResult(intent, requestCode);
        } else {
            EasyPermissions.requestPermissions(activity, activity.getString(R.string.ps_permision_tip), 123, perms);
        }
    }

    public void showSelector2(Fragment fromFragment, int requestCode, ArrayList<ImageEntity> selectedImages) {
        String perms[] = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        FragmentActivity activity = fromFragment.getActivity();
        if (EasyPermissions.hasPermissions(activity, perms)) {
            initAuthorities(activity);
            Intent intent = new Intent(activity, PictureSelectorActivity.class);
            if (selectedImages != null) {
                if (selectedImages.size() > getMaxCount()) {
                    throw new RuntimeException("selectedImages' size can not more than maxCount!");
                }
            }
            intent.putParcelableArrayListExtra(PSConstanceUtil.PASS_SELECTED, selectedImages);
            if (AnimUtil.checkJump())
                fromFragment.startActivityForResult(intent, requestCode);
        } else {
            EasyPermissions.requestPermissions(activity, activity.getString(R.string.ps_permision_tip), 123, perms);
        }
    }

    public void showSelector(Activity activity, int requestCode) {
        showSelector(activity, requestCode, null);
    }

    public void showSelector(Fragment fromFragment, int requestCode) {
        showSelector2(fromFragment, requestCode, null);
    }



    public void showPicture(Activity activity, String path) {
        PictureShowActivity.launcher(activity, path);
    }

    public void showPictures(Activity activity, ArrayList<ImageEntity> imageEntities, int position) {
        showPictures(activity, imageEntities, false, position, 0, null);
    }

    public void showPictures(Activity activity, ArrayList<ImageEntity> imageEntities, boolean isCanDelete, int position, int requestCode) {
        showPictures(activity, imageEntities, false, position, 0, null);
    }

    public void showPictures(Activity activity, ArrayList<ImageEntity> imageEntities, boolean isCanDelete, int position, int requestCode, View clickView) {
        Intent intent = new Intent(activity, PicturesShowActivity.class);
        intent.putParcelableArrayListExtra(PicturesShowActivity.INTENT_EXTRA_IMG, imageEntities);
        intent.putExtra(PicturesShowActivity.INTENT_EXTRA_POSITION, position);
        intent.putExtra(PicturesShowActivity.INTENT_EXTRA_IS_DELETE, isCanDelete);
        if (AnimUtil.checkJump()) {
            if (null != clickView) {
                clickView.post(new Runnable() {
                    @Override
                    public void run() {
                        int[] xys = new int[2];
                        clickView.getLocationOnScreen(xys);
                        int x = xys[0];
                        int y = xys[1];
                        int width = clickView.getWidth();
                        int height = clickView.getHeight();
                        intent.putExtra(PicturesShowActivity.INTENT_EXTRA_CLICKVIEW_X, x);
                        intent.putExtra(PicturesShowActivity.INTENT_EXTRA_CLICKVIEW_Y, y);
                        intent.putExtra(PicturesShowActivity.INTENT_EXTRA_CLICKVIEW_WIDTH, width);
                        intent.putExtra(PicturesShowActivity.INTENT_EXTRA_CLICKVIEW_HEIGHT, height);
                        if (isCanDelete) {
                            activity.startActivityForResult(intent, requestCode);
                        } else {
                            activity.startActivity(intent);
                        }
                        activity.overridePendingTransition(R.anim.translate_to_hold_show, R.anim.translate_to_hold);
                    }
                });
            } else {
                if (isCanDelete) {
                    activity.startActivityForResult(intent, requestCode);
                } else {
                    activity.startActivity(intent);
                }
                activity.overridePendingTransition(R.anim.translate_to_hold, R.anim.translate_to_hold);
            }
        }
    }

    private void initAuthorities(Activity activity) {
        if (TextUtils.isEmpty(PSConfigUtil.sAuthorities)) {
            sAuthorities = activity.getPackageName() + endName;
        }
    }

    /**
     * 清除缓存:包含裁剪的图片和通过PictureSelector拍照所得的图片
     */
    public static void clearCache() {
        PSCropUtil.clear();
        PSTakePhotoUtil.clear();
    }
}
