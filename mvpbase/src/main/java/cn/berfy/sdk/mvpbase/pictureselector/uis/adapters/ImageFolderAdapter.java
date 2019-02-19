package cn.berfy.sdk.mvpbase.pictureselector.uis.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.berfy.sdk.mvpbase.R;
import cn.berfy.sdk.mvpbase.pictureselector.entities.ImageFolderEntity;
import cn.berfy.sdk.mvpbase.pictureselector.listeners.OnClickListener;
import cn.berfy.sdk.mvpbase.pictureselector.listeners.OnItemClickListener;
import cn.berfy.sdk.mvpbase.pictureselector.utils.PSConfigUtil;
import cn.berfy.sdk.mvpbase.pictureselector.utils.PSGlideUtil;


/**
 * created by arvin on 16/9/3 12:05
 * email：1035407623@qq.com
 */
public class ImageFolderAdapter extends RecyclerView.Adapter {
    private Context mContext;
    private List<ImageFolderEntity> mItems;
    private OnItemClickListener onItemClickListener;

    public ImageFolderAdapter(Context context, List<ImageFolderEntity> items) {
        this.mContext = context;
        this.mItems = items;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageFolderHolder(LayoutInflater.from(mContext).inflate(R.layout.ps_item_image_folder, null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        holder.itemView.setOnClickListener(new OnClickListener(position) {
            @Override
            public void onClick(View v, int position) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, position);
                }
            }
        });
        ((ImageFolderHolder) holder).setData(position);
    }

    class ImageFolderHolder extends RecyclerView.ViewHolder {
        private ImageView imgContent;
        private TextView tvFileName;
        private TextView tvFileCount;
        private ImageView imgSelected;

        public ImageFolderHolder(View itemView) {
            super(itemView);
            imgContent = (ImageView) itemView.findViewById(R.id.img_first);
            imgSelected = (ImageView) itemView.findViewById(R.id.img_selected);
            tvFileName = (TextView) itemView.findViewById(R.id.tv_file_name);
            tvFileCount = (TextView) itemView.findViewById(R.id.tv_file_count);
        }

        public void setData(int position) {
            ImageFolderEntity item = mItems.get(position);
            PSGlideUtil.loadImage(mContext, "file://" + item.getFirstImagePath(), imgContent);
            tvFileName.setText(item.getFolderName());
            tvFileCount.setText("("+item.getCount() +")");

            if (position == PSConfigUtil.getInstance().getSelectedFolderPos()) {
                imgSelected.setVisibility(View.VISIBLE);
                PSGlideUtil.loadLocalImage(mContext, R.drawable.ps_checked, imgSelected);
            } else {
                imgSelected.setVisibility(View.GONE);
            }
        }
    }

}
