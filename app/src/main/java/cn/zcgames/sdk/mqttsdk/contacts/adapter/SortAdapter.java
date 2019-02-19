package cn.zcgames.sdk.mqttsdk.contacts.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import cn.berfy.sdk.mvpbase.util.NetworkUtil;
import cn.berfy.sdk.mvpbase.view.CircleImageView;
import cn.polaris.mqttuikit.holder.EmptyViewHolder;
import cn.zcgames.sdk.im.R;
import cn.zcgames.sdk.mqttsdk.contacts.model.SortModel;
import cn.zcgames.sdk.mqttsdk.personal.view.activity.PersonInfoActivity;

/**
 * @author: xp
 * @date: 2017/7/19
 */

public class SortAdapter extends RecyclerView.Adapter {
    private static final int EMPTY_VIEW = 1;
    private static final int ITEM_VIEW = 2;

    private LayoutInflater mInflater;
    private List<SortModel> mData;
    private Context mContext;
    private boolean isShowSelect;
    private List<String> selectUids = new ArrayList<>();//已选择的uid集合


    public SortAdapter(Context context, List<SortModel> data, boolean isShowSelect) {
        mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mContext = context;
        this.isShowSelect = isShowSelect;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == EMPTY_VIEW) {
            return new EmptyViewHolder(mInflater.inflate(R.layout.empty_view_holder, parent, false));
        } else {
            return new ItemViewHolder(mInflater.inflate(R.layout.search_user_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            int section = getSectionForPosition(position);
            //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
            if (position == getPositionForSection(section)) {
                itemHolder.tvTag.setVisibility(View.VISIBLE);
                itemHolder.tvTag.setText(mData.get(position).getLetters());
            } else {
                itemHolder.tvTag.setVisibility(View.GONE);
            }

            if (isShowSelect) {
                itemHolder.ivSelect.setVisibility(View.VISIBLE);
            } else {
                itemHolder.ivSelect.setVisibility(View.GONE);
            }

            //备注不为空则显示备注，否则显示昵称
            if(!TextUtils.isEmpty(mData.get(position).getRemark())){
                itemHolder.tvName.setText(mData.get(position).getRemark());
            }else {
                itemHolder.tvName.setText(mData.get(position).getNickname());
            }
            Glide.with(mContext).load(mData.get(position).getAvatar()).dontAnimate().
                    placeholder(R.drawable.placeholder).centerCrop().into(itemHolder.ivHead);
            itemHolder.ivSelect.setSelected(mData.get(position).isSelect());

            if (mOnItemClickListener != null) {
                itemHolder.rl_info.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnItemClickListener.onItemClick(holder.itemView, mData.get(position).getId());
                    }
                });
            }

            itemHolder.ivSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mData.get(position).isSelect()) {
                        mData.get(position).setSelect(false);
                        selectUids.remove(mData.get(position).getId());
                    } else {
                        mData.get(position).setSelect(true);
                        selectUids.add(mData.get(position).getId());
                    }
                    notifyItemChanged(position);
                    mOnItemClickListener.updateTitle(selectUids);
                }
            });
        } else if (holder instanceof EmptyViewHolder) {
            EmptyViewHolder emptyViewHolder = (EmptyViewHolder) holder;
            if (NetworkUtil.isNetAvailable(mContext)) {//判断网络状态
                emptyViewHolder.tv.setText(mContext.getString(R.string.no_data));
            } else {
                emptyViewHolder.tv.setText(mContext.getString(R.string.no_net));
            }
        }
    }


    @Override
    public int getItemCount() {
        return mData.size() > 0 ? mData.size() : 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (mData.size() > 0) {
            return ITEM_VIEW;
        } else {
            return EMPTY_VIEW;
        }
    }

    //**********************itemClick************************
    public interface OnItemClickListener {
        void onItemClick(View view, String uid);

        void updateTitle(List<String> selectUids);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }
    //**************************************************************


    /**
     * 提供给Activity刷新数据
     *
     * @param list
     */
    public void updateList(List<SortModel> list) {
        this.mData = list;
        notifyDataSetChanged();
    }

    public Object getItem(int position) {
        return mData.get(position);
    }

    /**
     * 根据ListView的当前位置获取分类的首字母的char ascii值
     */
    public int getSectionForPosition(int position) {
        return mData.get(position).getLetters().charAt(0);
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    public int getPositionForSection(int section) {
        for (int i = 0; i < getItemCount(); i++) {
            String sortStr = mData.get(i).getLetters();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }


    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTag, tvName;
        private CircleImageView ivHead;
        private ImageView ivSelect;
        private RelativeLayout rl_info;

        public ItemViewHolder(View itemView) {
            super(itemView);
            tvTag = itemView.findViewById(R.id.tag);
            tvName = itemView.findViewById(R.id.tv_name);
            ivHead = itemView.findViewById(R.id.iv_head);
            ivSelect = itemView.findViewById(R.id.iv_select);
            rl_info = itemView.findViewById(R.id.rl_info);
        }
    }
}

