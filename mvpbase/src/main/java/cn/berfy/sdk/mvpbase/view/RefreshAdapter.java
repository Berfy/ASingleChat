package cn.berfy.sdk.mvpbase.view;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cn.berfy.sdk.mvpbase.R;

/**
 * Created by ghost on 15/6/26.
 */
public abstract class RefreshAdapter<T> extends RecyclerView.Adapter {
    public List<T> data;
    public static final int TYPE_FOOTER = 100;

    private View footView;
    public Context context;
    public RecyclerView recyclerView;
    private boolean hasFoot = true;

    public RefreshAdapter(Context context, boolean hasFoot) {
        this.context = context;
        this.data = new ArrayList<>();
        this.hasFoot = hasFoot;
    }

    public RefreshAdapter(Context context, List<T> data, boolean hasFoot) {
        this.context = context;
        if (data == null) {
            this.data = new ArrayList<>();
        }
        this.hasFoot = hasFoot;
        this.data = data;
    }

    public RefreshAdapter(Context context, List<T> data) {
        this.context = context;
        if (data == null) {
            this.data = new ArrayList<>();
        }
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (i == TYPE_FOOTER) {
            footView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.load_more_footer, null);
            footView.setVisibility(View.GONE);
            FooterViewHolder foot = new FooterViewHolder(footView);
            return foot;
        }
        return onCreateItem(viewGroup, i);
    }

    public List<T> getData() {
        return data;
    }

    public void add(T data) {
        this.data.add(data);
        notifyDataSetChanged();
    }

    public void addAll(List<T> datas) {
        this.data.addAll(datas);
        notifyDataSetChanged();
    }

    public void clear() {
        this.data.clear();
        notifyDataSetChanged();
    }

    public abstract RecyclerView.ViewHolder onCreateItem(ViewGroup parent, int viewType);

    public abstract void onBindItem(RecyclerView.ViewHolder holder, int position);

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FooterViewHolder) {
            holder.itemView.setTag("foot");
            return;
        }
        onBindItem(holder, position);
    }

    public abstract int setItemViewType(int position);

    public abstract int setItemCount();

    @Override
    public int getItemViewType(int position) {
        // 最后一个item设置为footerView
        if (position + 1 >= getItemCount() && data.size() > 0) {//列表有数据且是最后一项
            return TYPE_FOOTER;
        }
        return setItemViewType(position);
    }

    @Override
    public int getItemCount() {
        int dataSize = data.size();
        int count = setItemCount();
        if (count < 0) {
            count = dataSize;
        }
        if (hasFoot && dataSize > 0) {
            return count + 1;
        }
        return count;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (getItemViewType(position) == TYPE_FOOTER) {
                        return gridManager.getSpanCount();
                    }
                    return 1;
                }
            });
        }
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {
        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }

}
