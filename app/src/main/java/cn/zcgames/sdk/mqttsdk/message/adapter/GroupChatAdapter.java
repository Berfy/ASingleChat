package cn.zcgames.sdk.mqttsdk.message.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import cn.berfy.sdk.mvpbase.view.CircleImageView;
import cn.polaris.mqttuikit.holder.EmptyViewHolder;
import cn.zcgames.sdk.im.R;
import cn.zcgames.sdk.mqttsdk.message.activity.ChatActivity;
import cn.zcgames.sdk.mqttsdk.message.model.GroupBean;

public class GroupChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private LayoutInflater inflater;
    private List<GroupBean.ListBean> datas;
    private final int EMPTY_ITEM = 0;
    private final int NORMAL_ITEM = 1;

    public GroupChatAdapter(Context context, List<GroupBean.ListBean> data){
        this.mContext = context;
        this.datas = data;
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(EMPTY_ITEM == viewType){
            return new EmptyViewHolder(inflater.inflate(R.layout.item_empty_view_holder, parent,false));
        }else{
            return new ItemHolder(inflater.inflate(R.layout.item_group_chat, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ItemHolder){
            GroupBean.ListBean group = datas.get(position);
            ItemHolder itemHolder = (ItemHolder) holder;
            Glide.with(mContext).load("").asBitmap()
                    .placeholder(R.drawable.placeholder).into(itemHolder.mIVHead);
//            itemHolder.mTVTitle.setText("快来吃电影啊");
            itemHolder.mTVTitle.setText(group.getName());
            itemHolder.mLLGroup.setOnClickListener(view ->{
                ChatActivity.launch((Activity) mContext,2,group.getGroup_id(),group.getName());
            });

        }else if(holder instanceof EmptyViewHolder){
            EmptyViewHolder emptyHolder = (EmptyViewHolder) holder;

        }
    }

    @Override
    public int getItemCount() {
        return datas.size() > 0 ? datas.size() : 1;
    }

    @Override
    public int getItemViewType(int position) {
        if(datas.size() > 0){
            return NORMAL_ITEM;
        }else{
            return EMPTY_ITEM;
        }
    }

    class ItemHolder extends RecyclerView.ViewHolder{
        CircleImageView mIVHead;
        TextView mTVTitle;
        LinearLayout mLLGroup;

        public ItemHolder(View itemView) {
            super(itemView);

            mIVHead = itemView.findViewById(R.id.iv_group_head);
            mTVTitle = itemView.findViewById(R.id.tv_group_title);
            mLLGroup = itemView.findViewById(R.id.ll_group);
        }
    }

}
