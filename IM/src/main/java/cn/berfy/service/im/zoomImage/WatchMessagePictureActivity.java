package cn.berfy.service.im.zoomImage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.berfy.sdk.mvpbase.base.CommonActivity;
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter;
import cn.berfy.sdk.mvpbase.util.AnimUtil;
import cn.berfy.sdk.mvpbase.util.FileUtils;
import cn.berfy.sdk.mvpbase.util.GsonUtil;
import cn.berfy.sdk.mvpbase.util.ImageUtil;
import cn.berfy.sdk.mvpbase.util.LogF;
import cn.berfy.service.download.DownloadListener;
import cn.berfy.service.download.DownloadManager;
import cn.berfy.service.im.R;


/**
 * 查看聊天消息原图
 * NorthStar修改优化
 *
 * @date 2019/1/17 14:40
 */
public class WatchMessagePictureActivity extends CommonActivity {
    private static final String TAG = "IMLogTag_查看大图";
    private static final String INTENT_EXTRA_LIST = "INTENT_EXTRA_LIST";
    private static final String INTENT_EXTRA_POSITION = "INTENT_EXTRA_POSITION";
    private List<HashMap<String, String>> paths = new ArrayList<>();
    private DownloadManager mDownloadManager;//文件下载管理器
    private static final int PIC_FILE = 100;
    private boolean mNewPageSelected = false;
    private ViewPager mImageViewPager;
    private BaseZoomableImageView mImage;
    private int mFirstDisplayImageIndex = 0;
    private View mLoadingLayout;
    private ProgressBar mProBar;
    private TextView mProTV;

    public static void start(Context context, ArrayList<HashMap<String, String>> paths, int position) {
        Intent intent = new Intent();
        intent.putExtra(INTENT_EXTRA_LIST, paths);
        intent.putExtra(INTENT_EXTRA_POSITION, position);
        intent.setClass(context, WatchMessagePictureActivity.class);
        if (AnimUtil.checkJump()) {
            context.startActivity(intent);
            if (context instanceof Activity)
                ((Activity) context).overridePendingTransition(R.anim.translate_to_show, R.anim.translate_to_hold);
        }
    }

    @Override
    public int getContentViewId() {
        return R.layout.im_watch_picture_activity;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

    }

    @Override
    public void initView() {
        mDownloadManager = DownloadManager.getInstance();
        paths = (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra(INTENT_EXTRA_LIST);
        LogF.d(TAG, "显示图片 paths" + GsonUtil.getInstance().toJson(paths));
        mFirstDisplayImageIndex = getIntent().getIntExtra(INTENT_EXTRA_POSITION, 0);
        findViews();
        queryImageMessages();
    }

    @Override
    public BasePresenter initPresenter() {
        return null;
    }

    @Override
    public void finish() {
        super.finish();
        AnimUtil.noAnim1(this);
    }


    private void queryImageMessages() {
        setViewPagerAdapter();
    }


    private void findViews() {
        mLoadingLayout = findViewById(R.id.loading_layout);
        mProBar = findViewById(R.id.pic_progress_bar);
        mProTV = findViewById(R.id.pic_progress_tv);
        mImageViewPager = findViewById(R.id.view_pager_image);
    }

    private void setViewPagerAdapter() {
        PagerAdapter adapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return paths == null ? 0 : paths.size();
            }

            @Override
            public void notifyDataSetChanged() {
                super.notifyDataSetChanged();
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                View layout = (View) object;
                BaseZoomableImageView iv = layout.findViewById(R.id.watch_image_view);
                iv.clear();
                container.removeView(layout);
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return (view == object);
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                ViewGroup layout;
                LayoutInflater inflater = LayoutInflater.from(WatchMessagePictureActivity.this);
                layout = (ViewGroup) inflater.inflate(R.layout.im_image_layout_multi_touch, null);
                layout.setBackgroundColor(Color.BLACK);
                container.addView(layout);
                layout.setTag(position);
                if (position == mFirstDisplayImageIndex) onViewPagerSelected(position);
                return layout;
            }

            @Override
            public int getItemPosition(@NonNull Object object) {
                return POSITION_NONE;
            }
        };

        mImageViewPager.setAdapter(adapter);
        mImageViewPager.setOffscreenPageLimit(2);
        LogF.d(TAG, "默认显示 " + mFirstDisplayImageIndex);
        mImageViewPager.setCurrentItem(mFirstDisplayImageIndex);
        mImageViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (positionOffset == 0f && mNewPageSelected) {
                    mNewPageSelected = false;
                    mLoadingLayout.setVisibility(View.GONE);
                    onViewPagerSelected(position);
                }
            }

            @Override
            public void onPageSelected(int position) {
                mNewPageSelected = true;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void onViewPagerSelected(int position) {
        updateCurrentImageView(position);
        mFirstDisplayImageIndex = position; // 下载成功之后，判断是否是同一条消息时需要使用
        mImageViewPager.setCurrentItem(mFirstDisplayImageIndex);
        onDownloadStart(position, paths.get(position));
    }

    // 初始化每个view的image
    protected void updateCurrentImageView(final int position) {
        View currentLayout = mImageViewPager.findViewWithTag(position);
        if (currentLayout == null) {
            ViewCompat.postOnAnimation(mImageViewPager,
                    () -> WatchMessagePictureActivity.this.updateCurrentImageView(position));
            return;
        }
        mImage = currentLayout.findViewById(R.id.watch_image_view);
        onImageViewFound(mImage);
    }


    // 设置图片点击事件
    protected void onImageViewFound(BaseZoomableImageView imageView) {
        imageView.setImageGestureListener(new ImageGestureListener() {

            @Override
            public void onImageGestureSingleTapConfirmed() {
                LogF.d(TAG, "mNewPageSelected==>" + mNewPageSelected);
                finish();// 图片单击
            }

            @Override
            public void onImageGestureLongPress() {
            }

            @Override
            public void onImageGestureFlingDown() {
            }
        });
    }


    /**
     * ********************************* 下载 ****************************************
     */
    private void downloadImage(int position, HashMap<String, String> hashMap) {
        LogF.d(TAG, "下载图片" + hashMap.get("msgId"));
        String msgId = hashMap.get("msgId");
        String url = hashMap.get("url");
        String path = FileUtils.getCacheRawFilePath(msgId);
        LogF.d(TAG, "msgId==>"+msgId+" ,getCacheRawFilePath===>" + path);
        String filePath = path + "/" + msgId + ".jpg";
        if (FileUtils.isExistFile(filePath)) {
            LogF.d(TAG, "本地存在 fileName==>" + filePath);
            if (mFirstDisplayImageIndex == position) {
                onDownloadSuccess(filePath);
            }
        } else {
            needDownload(url, path, msgId, position);
        }
    }

    private void needDownload(String fileUrl, String localPath, String fileId, int position) {
        LogF.d(TAG, "下载图片ing url==>" + fileUrl);//下载路径
        mDownloadManager.add(fileUrl, localPath, PIC_FILE, fileId, new DownloadListener() {
            @Override
            public void onStart(String url) {
                mProTV.setText("0");
                LogF.d(TAG, "onStart  fileUrl==>" + fileUrl + " ,url==>" + url);
                if (fileUrl.equals(url)) mLoadingLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(String errMsg) {
                LogF.d(TAG, "onError  errMsg==>" + errMsg);
                onDownloadFailed();
            }

            @Override
            public void onFinished(String url, String localPath) {
                LogF.d(TAG, "onFinished  url==>" + url);
                if (mFirstDisplayImageIndex == position) {
                    onDownloadSuccess(localPath);
                }
            }

            @Override
            public void onProgress(float progress) {
                if (mContext == null) return;
                int pro = getPro(progress);//当前下载进度
                mProBar.setProgress(pro);
                mProTV.setText(String.valueOf(pro));
                LogF.d(TAG, "当前下载进度为pro==>" + pro);
            }

            @Override
            public void onPause() {
            }

            @Override
            public void onCancel() {
                FileUtils.deleteFile(localPath);
            }
        });
        mDownloadManager.download(fileUrl);
    }

    //获取文件的下载进度
    private int getPro(float pro) {
        float progressF = pro * 100;
        int progressI = (int) Math.floor(progressF);
        LogF.d(TAG, "progressI==>" + progressI);
        return progressI;
    }

    private int getImageResOnFailed() {
        return R.drawable.im_image_download_failed;
    }

    private void onDownloadStart(int position, HashMap<String, String> hashMap) {
        downloadImage(position, hashMap);
    }

    private void onDownloadSuccess(String path) {
        LogF.d(TAG, "onDownloadSuccess->path" + path);
        mLoadingLayout.setVisibility(View.GONE);
        if (TextUtils.isEmpty(path)) {
            onDownloadFailed();
            LogF.d(TAG, "onDownloadSuccess-->路径不存在");
            return;
        }
        SimpleTarget<Bitmap> simpleTarget = new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                if (bitmap == null) {
                    onDownloadFailed();
                    mImage.setImageBitmap(ImageUtil.getBitmapFromDrawableRes(mContext, getImageResOnFailed()));
                } else {
                    mImage.setImageBitmap(bitmap);
                }
            }
        };
        if (mContext != null && !mContext.isDestroyed()) {
            Glide.with(mContext).load(path).asBitmap().into(simpleTarget);
        }
    }

    private void onDownloadFailed() {
        mLoadingLayout.setVisibility(View.GONE);
        mImage.setImageBitmap(ImageUtil.getBitmapFromDrawableRes(mContext, getImageResOnFailed()));
    }
}
