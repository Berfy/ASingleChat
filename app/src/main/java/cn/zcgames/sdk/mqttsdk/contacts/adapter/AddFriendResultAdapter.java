package cn.zcgames.sdk.mqttsdk.contacts.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import cn.berfy.sdk.mvpbase.util.NetworkUtil;
import cn.berfy.sdk.mvpbase.view.CircleImageView;
import cn.polaris.mqttuikit.holder.EmptyViewHolder;
import cn.zcgames.sdk.im.R;
import cn.zcgames.sdk.mqttsdk.contacts.model.SortModel;

public class AddFriendResultAdapter extends RecyclerView.Adapter {
    private static final int EMPTY_VIEW = 1;
    private static final int ITEM_VIEW = 2;

    private Context mContext;
    private LayoutInflater inflater;
    private List<SortModel> userList;
    private OnListener onListener;


    public AddFriendResultAdapter(Context context, List<SortModel> list) {
        this.mContext = context;
        this.userList = list;
        inflater = LayoutInflater.from(context);
    }


    public interface OnListener {
        void headClickListener(View v, int pos);

        void addClickListener(View v, int pos);
    }

    public void setOnListener(OnListener onListener) {
        this.onListener = onListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == EMPTY_VIEW) {
            return new EmptyViewHolder(inflater.inflate(R.layout.empty_view_holder, parent, false));
        } else {
            return new ResultViewHolder(inflater.inflate(R.layout.item_add_friend_result_layout, parent, false));
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ResultViewHolder) {
            ResultViewHolder itemHolder = (ResultViewHolder) holder;
            Glide.with(mContext).load(userList.get(position).getAvatar()).dontAnimate().
                    placeholder(R.drawable.placeholder).centerCrop().into(itemHolder.ivHead);
            itemHolder.tvNick.setText(userList.get(position).getNickname());

            //"status": "1:朋友请求，2:成为朋友，3:朋友关系解除"
            setAddState(itemHolder.tvAdd, "2".equals(userList.get(position).getStatus()) ? true : false);

            headClick(itemHolder.ivHead, holder.getAdapterPosition());

            if (!"2".equals(userList.get(position).getStatus())) {
                addClick(itemHolder.tvAdd, holder.getAdapterPosition());
            }
        } else if (holder instanceof EmptyViewHolder) {
            EmptyViewHolder emptyViewHolder = (EmptyViewHolder) holder;
            if (NetworkUtil.isNetAvailable(mContext)) {//判断网络状态
                emptyViewHolder.tv.setText(mContext.getString(R.string.no_data));
            } else {
                emptyViewHolder.tv.setText(mContext.getString(R.string.no_net));
            }
        }
    }


    private void setAddState(TextView addView, boolean isAdd) {
        if (isAdd) {//已添加
            addView.setText(mContext.getString(R.string.added));
            addView.setTextColor(mContext.getResources().getColor(R.color.color_DDDDDD));
            addView.setBackgroundResource(R.drawable.bg_added);
        } else {
            addView.setText(mContext.getString(R.string.add));
            addView.setTextColor(mContext.getResources().getColor(R.color.color_ffffff));
            addView.setBackgroundResource(R.drawable.bg_add);
        }
    }


    private void headClick(View view, int pos) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onListener.headClickListener(view, pos);
            }
        });
    }

    private void addClick(View view, int pos) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onListener.addClickListener(view, pos);
            }
        });
    }


    @Override
    public int getItemCount() {
        return userList.size() > 0 ? userList.size() : 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (userList.size() > 0) {
            return ITEM_VIEW;
        } else {
            return EMPTY_VIEW;
        }
    }


    public static class ResultViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView ivHead;
        private TextView tvNick;
        private TextView tvAdd;

        public ResultViewHolder(View itemView) {
            super(itemView);
            ivHead = itemView.findViewById(R.id.iv_head);
            tvNick = itemView.findViewById(R.id.tv_nick);
            tvAdd = itemView.findViewById(R.id.tv_add);
        }
    }
}
