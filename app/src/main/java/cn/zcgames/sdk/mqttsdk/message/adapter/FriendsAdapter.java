package cn.zcgames.sdk.mqttsdk.message.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.util.List;
import cn.berfy.sdk.mvpbase.model.User;
import cn.berfy.sdk.mvpbase.view.CircleImageView;
import cn.polaris.mqttuikit.holder.EmptyViewHolder;
import cn.zcgames.sdk.im.R;
import cn.zcgames.sdk.mqttsdk.app.MyApplication;
import cn.zcgames.sdk.mqttsdk.message.model.FriendBean;
import cn.zcgames.sdk.mqttsdk.message.presenter.FriendPresenter;

public class FriendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private LayoutInflater inflater;
    private List<FriendBean.ContactBean> datas;
    private final int EMPTY_ITEM = 0;
    private final int NORMAL_ITEM = 1;
    private FriendPresenter friendPresenter;

    public FriendsAdapter(Context context, List<FriendBean.ContactBean> data, FriendPresenter friendPresenter) {
        this.mContext = context;
        this.datas = data;
        inflater = LayoutInflater.from(context);
        this.friendPresenter = friendPresenter;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (EMPTY_ITEM == viewType) {
            return new EmptyViewHolder(inflater.inflate(R.layout.item_empty_view_holder, parent, false));
        } else {
            return new ItemHolder(inflater.inflate(R.layout.item_friend, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemHolder) {
            FriendBean.ContactBean person = datas.get(position);
            ItemHolder itemHolder = (ItemHolder) holder;
            Glide.with(mContext).load(person.getAvatar()).asBitmap()
                    .placeholder(R.drawable.placeholder)
                    .into(itemHolder.mIVHead);
            itemHolder.mTVMessage.setText(person.getNickname());
            //"status": "1:朋友请求，2:成为朋友，3:朋友关系解除"
            if (1 == person.getStatus()) {
                itemHolder.mTVPass.setVisibility(View.VISIBLE);
                itemHolder.mTVAlreadyPass.setVisibility(View.GONE);
            } else if (2 == person.getStatus()) {
                itemHolder.mTVPass.setVisibility(View.GONE);
                itemHolder.mTVAlreadyPass.setVisibility(View.VISIBLE);
            }

            //同意好友请求 1:接受，2:拒绝
            itemHolder.mTVPass.setOnClickListener(view -> {
                User user = MyApplication.getCurrLoginUser();
                friendPresenter.acceptRequestFriend(user.getPlayer().getMerchid(), person.getId(),
                        user.getPlayer().getId(), 1, person.getRemark(), person);
            });

        } else if (holder instanceof EmptyViewHolder) {
            EmptyViewHolder emptyHolder = (EmptyViewHolder) holder;

        }
    }

    @Override
    public int getItemCount() {
        return datas.size() > 0 ? datas.size() : 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (datas.size() > 0) {
            return NORMAL_ITEM;
        } else {
            return EMPTY_ITEM;
        }
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        CircleImageView mIVHead;
        TextView mTVMessage, mTVPass, mTVAlreadyPass;

        public ItemHolder(View itemView) {
            super(itemView);

            mIVHead = itemView.findViewById(R.id.iv_friend_head);
            mTVMessage = itemView.findViewById(R.id.tv_message);
            mTVPass = itemView.findViewById(R.id.tv_pass);
            mTVAlreadyPass = itemView.findViewById(R.id.tv_already_pass);
        }
    }

}
