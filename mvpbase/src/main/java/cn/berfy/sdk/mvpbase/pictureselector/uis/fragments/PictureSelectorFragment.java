package cn.berfy.sdk.mvpbase.pictureselector.uis.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.berfy.sdk.mvpbase.R;
import cn.berfy.sdk.mvpbase.pictureselector.entities.ImageEntity;
import cn.berfy.sdk.mvpbase.pictureselector.entities.ImageFolderEntity;
import cn.berfy.sdk.mvpbase.pictureselector.entities.ImagesModel;
import cn.berfy.sdk.mvpbase.pictureselector.entities.PageChangeEntity;
import cn.berfy.sdk.mvpbase.pictureselector.listeners.OnItemClickListener;
import cn.berfy.sdk.mvpbase.pictureselector.listeners.OnItemSelectedListener;
import cn.berfy.sdk.mvpbase.pictureselector.uis.adapters.PictureSelectorAdapter;
import cn.berfy.sdk.mvpbase.pictureselector.utils.PSConfigUtil;
import cn.berfy.sdk.mvpbase.pictureselector.utils.PSConstanceUtil;
import cn.berfy.sdk.mvpbase.pictureselector.utils.PSTakePhotoUtil;
import cn.berfy.sdk.mvpbase.pictureselector.views.DividerGridItemDecoration;
import cn.berfy.sdk.mvpbase.pictureselector.views.FolderDialog;
import io.reactivex.observers.DisposableObserver;

/**
 * created by arvin on 16/8/28 00:02
 * email：1035407623@qq.com
 */
public class PictureSelectorFragment extends BaseFragment implements OnItemClickListener, OnItemSelectedListener,
        FolderDialog.OnFolderSelectedListener, PSTakePhotoUtil.OnTakePhotoSuccessListener {

    private final static String TAG = "图片查看器";
    private TextView tvReview;
    private TextView tvSelectedImageFolderName;

    private PictureSelectorAdapter mAdapter;
    //用于显示的图集
    private ArrayList<ImageEntity> mItems;
    private List<ImageFolderEntity> mImageFolders;
    private PSTakePhotoUtil takePhotoUtil;

    private FolderDialog folderDialog;
    private int mSelectedCount;

    @Override
    protected int setContentViewId() {
        return R.layout.ps_fragment_picture_selector;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        initView();
        initEvent();
        initRlImages();
        loadData();
    }

    private void initView() {
        takePhotoUtil = new PSTakePhotoUtil(getActivity(), this);
        tvReview = getView(R.id.tv_review);
        tvSelectedImageFolderName = getView(R.id.tv_selected_image_folder_name);

        if (!PSConfigUtil.getInstance().canReview()) {
            tvEnsure.setVisibility(View.GONE);
            tvReview.setVisibility(View.GONE);
        }
        tvEnsure.setText(PSConfigUtil.getInstance().getRightBtnTitleRes());
    }

    private void initEvent() {
        tvReview.setOnClickListener(this);
        tvSelectedImageFolderName.setOnClickListener(this);
    }

    private void initRlImages() {
        mItems = new ArrayList<>();
        mSelectedImages = new ArrayList<>();

        RecyclerView rlImages = getView(R.id.rl_images);
        rlImages.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        rlImages.addItemDecoration(new DividerGridItemDecoration(getActivity()));
        mAdapter = new PictureSelectorAdapter(getActivity(), mItems);
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnItemSelectedListener(this);
        rlImages.setAdapter(mAdapter);
    }

    private void loadData() {
        final ArrayList<ImageEntity> temp = getArguments().getParcelableArrayList(PSConstanceUtil.PASS_SELECTED);
        ImagesModel.getImageFolders(getContext()).subscribe(new DisposableObserver<List<ImageFolderEntity>>() {
            @Override
            public void onNext(@NonNull List<ImageFolderEntity> imageFolderEntities) {
                mImageFolders = imageFolderEntities;
                if (mImageFolders != null && mImageFolders.size() > 0) {
                    if (temp != null && temp.size() > 0) {
                        for (ImageEntity entity : mImageFolders.get(0).getImages()) {
                            for (ImageEntity mSelectedImage : temp) {
                                if (entity.getPath().equals(mSelectedImage.getPath())) {
                                    entity.setSelected(true);
                                    entity.setNumber(mSelectedImage.getNumber());
                                    mSelectedImages.add(entity);
                                    break;
                                }
                            }
                        }
                    }
                    mItems.addAll(mImageFolders.get(PSConfigUtil.getInstance().getSelectedFolderPos()).getImages());
                }
                mAdapter.notifyDataSetChanged();
                asyncShow(mSelectedImages.size());
            }

            @Override
            public void onError(@NonNull Throwable e) {
                mImageFolders = new ArrayList<>();
                ImageFolderEntity imageFolder = new ImageFolderEntity();
                imageFolder.setCount(0);
                imageFolder.setFirstImagePath("");
                imageFolder.setFolderName("相册");
                imageFolder.setImages(new ArrayList<ImageEntity>());
                mImageFolders.add(imageFolder);
                mAdapter.notifyDataSetChanged();
                asyncShow(mSelectedImages.size());
            }

            @Override
            public void onComplete() {}
        });
    }

    @Override
    public void update(Bundle data) {
        //没有数据表示不裁剪,重新选择图片
        if (data == null) {
            return;
        }

        List<ImageEntity> selectedData = data.getParcelableArrayList(PSConstanceUtil.PASS_SELECTED);
        if (selectedData == null) {
            selectedData = new ArrayList<>();
        }

        mItems.clear();
        mSelectedImages.clear();

        for (ImageEntity entity : mImageFolders.get(0).getImages()) {
            for (ImageEntity mSelectedImage : selectedData) {
                if (entity.getPath().equals(mSelectedImage.getPath())) {
                    entity.setSelected(true);
                    mSelectedImages.add(entity);
                    break;
                } else {
                    entity.setSelected(false);
                }
            }
        }
        mItems.addAll(mImageFolders.get(PSConfigUtil.getInstance().getSelectedFolderPos()).getImages());
        mAdapter.notifyDataSetChanged();
        asyncShow(mSelectedImages.size());
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.tv_review) {
            toReview(0, true);
            return;
        }

        if (v.getId() == R.id.tv_selected_image_folder_name) {
            showImageFolder();
        }
    }

    @Override
    public void onItemClick(View v, int position) {
        boolean canTakePhoto = PSConfigUtil.getInstance().isCanTakePhoto();
        if (canTakePhoto && position == 0) {
            takePhotoUtil.choosePhotoFromCamera();
            return;
        }
        position -= canTakePhoto ? 1 : 0;
        if (!PSConfigUtil.getInstance().canReview()) {
            if (PSConfigUtil.getInstance().isCanCrop()) {
                toCrop(mItems.get(position));
                return;
            }
            mSelectedImages.add(mItems.get(position));
            onEnsureClicked();
            return;
        }
        toReview(position, false);
    }

    /**
     * @param item 裁剪该图片
     */
    private void toCrop(ImageEntity item) {
        Bundle data = new Bundle();
        //这是用于显示的数据
        data.putParcelable(PSConstanceUtil.PASS_SHOW, item);
        PageChangeEntity entity = new PageChangeEntity(PageChangeEntity.PageId.Crop, data);
        EventBus.getDefault().post(entity);
    }

    /**
     * @param currentPos 显示的位置
     * @param filter     true表示只要选中的,否则就是当前页面全部图片
     */
    private void toReview(int currentPos, boolean filter) {
        Bundle data = new Bundle();
        //这是用于显示的数据
        data.putParcelableArrayList(PSConstanceUtil.PASS_SHOW, getReviewImages(filter));
        //这是已选中的数据
        data.putParcelableArrayList(PSConstanceUtil.PASS_SELECTED, mSelectedImages);
        data.putInt(PSConstanceUtil.PASS_CURRENT_POS, currentPos);
        PageChangeEntity entity = new PageChangeEntity(PageChangeEntity.PageId.Review, data);
        EventBus.getDefault().post(entity);
    }

    /**
     * @param filter true表示只要选中的,否则就是当前页面全部图片
     */
    private ArrayList<ImageEntity> getReviewImages(boolean filter) {
        if (!filter) {
            return mItems;
        }
        return mSelectedImages;
    }

    /**
     * 显示可显示的文件夹
     */
    private void showImageFolder() {
        if (folderDialog == null) {
            folderDialog = new FolderDialog(getActivity(), mImageFolders);
            folderDialog.setOnFolderSelectedListener(this);
        } else {
            folderDialog.setImageFolders(mImageFolders);
        }
        folderDialog.show();
    }

    @Override
    public void onFolderSelected(View v, int position) {
        if (position == PSConfigUtil.getInstance().getSelectedFolderPos()) {
            return;
        }
        PSConfigUtil.getInstance().setSelectedFolderPos(position);
        PSConfigUtil.getInstance().setCanTakePhoto(position == 0);
        mItems.clear();
        mItems.addAll(mImageFolders.get(position).getImages());
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemSelected(View v, int position, boolean isSelected) {
        if (isSelected) {
            mSelectedCount = PSConfigUtil.getInstance().addSelectedCount(1);
            mSelectedImages.add(mItems.get(position));
        } else {
            mSelectedCount = PSConfigUtil.getInstance().addSelectedCount(-1);
            mSelectedImages.remove(mItems.get(position));
        }
        asyncShow(mSelectedCount);
    }

    private void asyncShow(int selectedCount) {
        PSConfigUtil.getInstance().setSelectedCount(selectedCount);
        tvCheck.setVisibility(selectedCount > 0 ? View.VISIBLE : View.GONE);
        tvCheck.setText(selectedCount + "");
        tvReview.setText(getReviewText(selectedCount));
        boolean enabled = selectedCount > 0;
        tvReview.setEnabled(enabled);
    }

    private String getReviewText(int selectedCount) {
        return "预览";
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        takePhotoUtil.onActivityResult(requestCode, resultCode, data, getActivity());
    }

    @Override
    public void onTakePhotoSuccess(String path) {
        File temp = new File(path);
        if (!temp.exists()) {
            return;
        }
        //show in list
        ImageEntity entity = new ImageEntity(path, temp.lastModified(), temp.length());
        mItems.add(0, entity);
        mAdapter.notifyDataSetChanged();

        saveToFolder(entity);
    }

    private void saveToFolder(ImageEntity entity) {
        //先加入到所有文件中
        try {
            ImageFolderEntity allFolder = mImageFolders.get(0);
            allFolder.getImages().add(0, entity);
            allFolder.setFirstImagePath(entity.getPath());
            allFolder.setCount(allFolder.getCount() + 1);

            //加入到相应的文件夹中
            boolean exists = false;
            for (ImageFolderEntity folder : mImageFolders) {
                if (folder.getFolderName().equals(PSConstanceUtil.FROM_CAMERA)) {
                    folder.getImages().add(0, entity);
                    folder.setCount(folder.getCount() + 1);
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                List<ImageEntity> images = new ArrayList<>();
                images.add(entity);
                mImageFolders.add(1, new ImageFolderEntity(PSConstanceUtil.FROM_CAMERA, 1, entity.getPath(), images));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackClicked() {
        getActivity().finish();
    }
}
