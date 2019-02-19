package cn.berfy.sdk.mvpbase.pictureselector.views;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import cn.berfy.sdk.mvpbase.R;
import cn.berfy.sdk.mvpbase.pictureselector.entities.ImageFolderEntity;
import cn.berfy.sdk.mvpbase.pictureselector.listeners.OnItemClickListener;
import cn.berfy.sdk.mvpbase.pictureselector.uis.adapters.ImageFolderAdapter;
import cn.berfy.sdk.mvpbase.pictureselector.utils.PSScreenUtil;


/**
 * created by arvin on 16/9/3 15:00
 * email：1035407623@qq.com
 */
public class FolderDialog extends Dialog implements OnItemClickListener {
    private RecyclerView rlFolder;
    private List<ImageFolderEntity> mImageFolders;
    private ImageFolderAdapter mAdapter;
    private OnFolderSelectedListener onFolderSelectedListener;
    private ObjectAnimator downShowAnim;

    public FolderDialog(Context context, List<ImageFolderEntity> imageFolders) {
        super(context, R.style.normal_dialog_white);
        setContentView(R.layout.ps_dialog_folder);
        setImageFolders(imageFolders);
        layoutDialog(context);
    }

    private void layoutDialog(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        getWindow().setGravity(Gravity.START | Gravity.TOP);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.x = 0;
        lp.y = PSScreenUtil.dp2px(140);
        lp.width = metrics.widthPixels;
        //这个高度包括了状态栏,所以还要多减去24dp
        lp.height = metrics.heightPixels - PSScreenUtil.dp2px(68);
        getWindow().setAttributes(lp);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rlFolder = (RecyclerView) getWindow().findViewById(R.id.rl_folder);
        rlFolder.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new ImageFolderAdapter(getContext(), mImageFolders);
        rlFolder.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);
    }

    public void setOnFolderSelectedListener(OnFolderSelectedListener onFolderSelectedListener) {
        this.onFolderSelectedListener = onFolderSelectedListener;
    }

    public void setImageFolders(List<ImageFolderEntity> imageFolders) {
        if (mImageFolders == null) {
            mImageFolders = new ArrayList<>();
        } else {
            mImageFolders.clear();
        }
        mImageFolders.addAll(imageFolders);
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(View v, int position) {
        if (onFolderSelectedListener != null) {
            onFolderSelectedListener.onFolderSelected(v, position);
        }
        mAdapter.notifyDataSetChanged();
        dismiss();
    }

    public interface OnFolderSelectedListener {
        void onFolderSelected(View v, int position);
    }

}
