package cn.berfy.sdk.mvpbase.pictureselector.uis.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.berfy.sdk.mvpbase.R;
import cn.berfy.sdk.mvpbase.pictureselector.listeners.OnClickListener;
import cn.berfy.sdk.mvpbase.pictureselector.listeners.OnItemClickListener;
import cn.berfy.sdk.mvpbase.pictureselector.listeners.OnItemSelectedListener;
import cn.berfy.sdk.mvpbase.pictureselector.entities.ImageEntity;
import cn.berfy.sdk.mvpbase.pictureselector.utils.PSConfigUtil;
import cn.berfy.sdk.mvpbase.pictureselector.utils.PSGlideUtil;
import cn.berfy.sdk.mvpbase.pictureselector.utils.PSScreenUtil;


/**
 * created by arvin on 16/8/29 21:58
 * email：1035407623@qq.com
 */
public class PictureSelectorAdapter extends RecyclerView.Adapter {
    private static final int TYPE_TAKE_PHOTO = 1;
    private static final int TYPE_NORMAL = 2;

    private final int imgHeight = PSScreenUtil.getScreenWidth() / 3;

    private Context mContext;
    private List<ImageEntity> mItems;
    private List<ImageEntity> mSelectItems = new ArrayList<>();

    private OnItemClickListener onItemClickListener;
    private OnItemSelectedListener onItemSelectedListener;


    public PictureSelectorAdapter(Context mContext, List<ImageEntity> mItems) {
        this.mContext = mContext;
        this.mItems = mItems;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (PSConfigUtil.getInstance().isCanTakePhoto() && position == 0) {
            return TYPE_TAKE_PHOTO;
        }
        return TYPE_NORMAL;
    }

    @Override
    public int getItemCount() {
        return mItems.size() + getCanTakePhoto();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_TAKE_PHOTO) {
            return new TakePhotoHolder(LayoutInflater.from(mContext).inflate(R.layout.ps_item_take_photo, parent, false));
        }
        return new PictureSelectorHolder(LayoutInflater.from(mContext).inflate(R.layout.ps_item_picture_selector, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        setItemClick(holder, position);

        if (getItemViewType(position) == TYPE_TAKE_PHOTO) {
            return;
        }
        if (PSConfigUtil.getInstance().isCanTakePhoto()) {
            position -= 1;
        }
        ((PictureSelectorHolder) holder).setData(mItems.get(position));
        ((PictureSelectorHolder) holder).setEvent(position);
    }

    private void setItemClick(RecyclerView.ViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(new OnClickListener(position) {
            @Override
            public void onClick(View v, int position) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, position);
                }
            }
        });
    }

    public class PictureSelectorHolder extends RecyclerView.ViewHolder {
        ImageView imgContent;
        ImageView imgSelector;
        FrameLayout layoutSelector;
        TextView number;
        View cover;

        public PictureSelectorHolder(View itemView) {
            super(itemView);
            imgContent = (ImageView) itemView.findViewById(R.id.img_content);
            ViewGroup.LayoutParams layoutParams = imgContent.getLayoutParams();
            layoutParams.height = imgHeight;
            imgContent.setLayoutParams(layoutParams);

            imgSelector = (ImageView) itemView.findViewById(R.id.img_selector);
            layoutSelector = (FrameLayout) itemView.findViewById(R.id.layout_selector);
            number = (TextView) itemView.findViewById(R.id.tv_number);   //选中的数量
            cover = itemView.findViewById(R.id.view);                    //选中遮罩
        }

        public void setData(ImageEntity item) {
            PSGlideUtil.loadImage(mContext, item.getPath(), imgContent);
            imgSelector.setSelected(item.isSelected());
            if (item.getNumber() != 0) {
                if (item.isSelected()) {
                    number.setVisibility(View.VISIBLE);
                    number.setText(item.getNumber() + "");
                    // cover.setVisibility(View.VISIBLE);
                }
            } else {
                number.setVisibility(View.INVISIBLE);
                // cover.setVisibility(View.INVISIBLE);
            }
        }

        public void setEvent(int position) {
            if (!PSConfigUtil.getInstance().canReview()) {
                layoutSelector.setVisibility(View.GONE);
            } else {
                layoutSelector.setVisibility(View.VISIBLE);
            }

            layoutSelector.setOnClickListener(new OnClickListener(position) {

                @Override
                public void onClick(View v, int position) {
                    ImageEntity item = mItems.get(position);
                    if (!PSConfigUtil.getInstance().canAdd() && !item.isSelected()) {
                        Toast.makeText(mContext, mContext.getString(R.string.ps_max_count_tips, "" + PSConfigUtil.getInstance().getMaxCount()),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    item.setSelected(!item.isSelected());
                    imgSelector.setSelected(item.isSelected());
                    //刷新数据
                    if (item.isSelected()) {
                        for (int i = 0; i < mItems.size(); i++) {
                            ImageEntity entity = mItems.get(i);
                            if (entity.isSelected()) {
                                if (!mSelectItems.contains(entity)) {
                                    mSelectItems.add(entity);
                                }
                            }
                        }
                        item.setNumber(mSelectItems.size());
                        number.setVisibility(View.VISIBLE);
                        number.setText(mSelectItems.size() + "");
                        //  cover.setVisibility(View.VISIBLE);
                    } else {
                        //取消选中，刷新大于当前的数据
                        if (mSelectItems.contains(item)) {
                            mSelectItems.remove(item);
                            for (int i = 0; i < mItems.size(); i++) {
                                ImageEntity imageEntity = mItems.get(i);
                                if (imageEntity.isSelected() && imageEntity.getNumber() > item.getNumber()) {
                                    imageEntity.setNumber(imageEntity.getNumber() - 1);
                                    mItems.set(i, imageEntity);
                                }
                            }
                            item.setNumber(0);
                            number.setVisibility(View.INVISIBLE);
                            // cover.setVisibility(View.INVISIBLE);
                        }
                        notifyDataSetChanged();
                    }

                    if (onItemSelectedListener != null) {
                        onItemSelectedListener.onItemSelected(v, position, item.isSelected());
                    }
                }
            });
        }
    }

    private int getCanTakePhoto() {
        return PSConfigUtil.getInstance().isCanTakePhoto() ? 1 : 0;
    }

    public class TakePhotoHolder extends RecyclerView.ViewHolder {
        public TakePhotoHolder(View itemView) {
            super(itemView);
        }
    }

}
