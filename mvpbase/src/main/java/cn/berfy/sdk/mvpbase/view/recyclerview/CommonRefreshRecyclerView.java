package cn.berfy.sdk.mvpbase.view.recyclerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.berfy.sdk.mvpbase.R;
import cn.berfy.sdk.mvpbase.util.LogF;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.RecyclerView.*;

/**
 * 自适应的下拉刷新 自动加载更多容器
 * Created by ghost on 15/8/5.
 * Berfy修改  2018.1.31
 */
public class CommonRefreshRecyclerView extends RelativeLayout {

    private final String TAG = "下拉刷新";
    private RefreshableView mRefreshableView;
    private RecyclerView mRecyclerView;

    private int mLastVisibleItem;
    private RecyclerView.LayoutManager mManager;
    private View mFootView;
    private RefreshAdapter mAdapter;
    private boolean mIsLastPage;
    private boolean mIsCanLoadMore = true;//避免频繁加载
    private RelativeLayout mRlEmpty;
    private RelativeLayout mRlLayout;
    private ImageView mIvEmpty;
    private TextView mTvEmpty;
    private boolean mIsEnableRefresh = true;//开启和禁止下拉刷新
    private boolean mIsEnableLoadMore = true;//开启和禁止获取更多

    private RefreshableView.OnRefreshListener mOnRefreshListener;
    private Context mContext;

    public CommonRefreshRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    public CommonRefreshRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
                R.styleable.refreshView);
        //获取自定义属性和默认值
        mRefreshableView = new RefreshableView(getContext());
        mRefreshableView.setOverScrollMode(RefreshableView.OVER_SCROLL_NEVER);
        int color = mTypedArray.getColor(R.styleable.refreshView_bg_color, -1);
        if (color != -1)
            mRefreshableView.setBgColor(color);
        mRlEmpty = new RelativeLayout(getContext());
        mRlLayout = new RelativeLayout(getContext());
        mRlLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mRlEmpty.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mIvEmpty = new ImageView(getContext());
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mRlEmpty.setGravity(Gravity.CENTER);


        mRlEmpty.setVisibility(GONE);
        mRefreshableView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mRecyclerView = new RecyclerView(getContext());
        mRecyclerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mRlLayout.addView(mRlEmpty);
        mRlLayout.addView(mRecyclerView);
        mRefreshableView.setView(mRlLayout);

        View empty = LayoutInflater.from(getContext()).inflate(R.layout.item_empty_view_holder, mRlEmpty);
        mIvEmpty = empty.findViewById(R.id.iv_not_data_icon);
        mTvEmpty = empty.findViewById(R.id.tv_not_data_tip);
        mRecyclerView.setHasFixedSize(false);
        addView(mRefreshableView);
    }

    public void setBgColor(int color) {
        mRefreshableView.setBgColor(color);
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        mRecyclerView.setLayoutManager(layoutManager);
        mManager = layoutManager;
    }

    public RecyclerView.LayoutManager getLayoutManager() {
        return mManager;
    }

    public boolean isLastPage() {
        return mIsLastPage;
    }

    public void startRefreshing() {
        mRefreshableView.startRefreshing();
    }

    public void setEnable(boolean isEnableRefresh, boolean isEnableLoadMore) {
        mIsEnableRefresh = isEnableRefresh;
        mIsEnableLoadMore = isEnableLoadMore;
        mRefreshableView.setEnable(mIsEnableRefresh);
    }

    /**
     * 没有更多数据调用
     */
    public void noData() {
        mIsLastPage = true;
        if (mFootView == null) {
            mFootView = mRecyclerView.findViewWithTag("foot");
        }
        if (mFootView == null) {
            return;
        }
        mFootView.setVisibility(View.VISIBLE);
        TextView tv = (TextView) mFootView.findViewById(R.id.no_more_textView);
        ProgressBar bar = (ProgressBar) mFootView.findViewById(R.id.load_more_progressBar);
        bar.setVisibility(GONE);
        tv.setText("没有更多啦~");
    }

    /**
     * 设置空数据
     *
     * @param isEmpty 是否为空
     * @param res     空数据图
     */
    public void setEmpty(boolean isEmpty, String emptyMsg, int res) {
        if (isEmpty) {
            mRlEmpty.setVisibility(VISIBLE);
            mRecyclerView.setVisibility(GONE);
            mIvEmpty.setImageResource(res);
            mTvEmpty.setText(emptyMsg);
        } else {
            mRlEmpty.setVisibility(GONE);
            mRecyclerView.setVisibility(VISIBLE);
        }
        postInvalidate();
    }

    public RefreshAdapter getAdapter() {
        return (RefreshAdapter) mRecyclerView.getAdapter();
    }

    public void setAdapter(RefreshAdapter adapter) {
        mAdapter = adapter;
        if (mRecyclerView.getLayoutManager() == null) {
            mManager = new LinearLayoutManager(getContext());
            ((LinearLayoutManager) mManager).setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(mManager);
        }
        mRecyclerView.setAdapter(adapter);
        mFootView = mRecyclerView.findViewWithTag("foot");
    }

    public void setOnRefreshListener(final RefreshableView.OnRefreshListener refreshListener) {
        mOnRefreshListener = refreshListener;
        mRefreshableView.setOnRefreshListener(new RefreshableView.OnRefreshListener() {
            @Override
            public void onRefresh(ViewGroup view) {
                mIsLastPage = false;
                if (null != mOnRefreshListener)
                    mOnRefreshListener.onRefresh(view);
            }

            @Override
            public void loadMore(ViewGroup view) {

            }
        });
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView,
                                             int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LogF.d(TAG, "isLastPage===" + isLastPage());
                if (isLastPage()) {
                    return;
                }
                if (newState == SCROLL_STATE_IDLE && mLastVisibleItem + 1 >= mAdapter.getItemCount() && !mIsLastPage) {
                    updateText(false);
                    if (mIsEnableLoadMore && mIsCanLoadMore) {
                        mIsCanLoadMore = false;
                        if (null != mOnRefreshListener)
                            mOnRefreshListener.loadMore(mRecyclerView);
                    }
                }
                //使Glide在RecyclerView滑动时不去加载图片,从而提高列表滑动流畅度
                setGlideLoadEvent(newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LogF.d(TAG, "滑动===" + dx + "," + dy);
                if (Math.abs(dy) > 0) {//列表滑动后才可以获取更多，避免不满一屏也显示加载更多
                    if (mManager instanceof LinearLayoutManager) {
                        mLastVisibleItem = ((LinearLayoutManager) mManager).findLastVisibleItemPosition();
                    }
                    if (mManager instanceof GridLayoutManager) {
                        mLastVisibleItem = ((GridLayoutManager) mManager).findLastVisibleItemPosition();
                    }
                }
            }
        });
    }

    //使Glide在RecyclerView滑动时不去加载图片,从而提高列表滑动流畅度
    private void setGlideLoadEvent(int newState) {
        mContext = getContext();//获取上下文
        switch (newState) {
            case SCROLL_STATE_IDLE://当屏幕停止滚动,加载图片
                loadUrl();
                break;
            case SCROLL_STATE_DRAGGING://当屏幕滚动且用户手指还在屏幕上,停止加载
                loadUrl();
                break;
            case SCROLL_STATE_SETTLING://用于用户操作,屏幕产生惯性滚动,停止加载
                stopLoad();
                break;
            default:
                loadUrl();
                break;
        }
    }

    //停止加载图片
    private void stopLoad() {
        try {
            if (mContext != null) Glide.with(mContext).pauseRequests();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //加载图片
    private void loadUrl() {
        try {
            if (mContext != null) Glide.with(mContext).resumeRequests();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnRefreshListener {
        void onRefreshListener();
    }

    public void finishRefresh() {
        updateText(true);
        mIsCanLoadMore = true;
        mRefreshableView.finishRefresh();
    }

    private void updateText(boolean isFinishLoad) {
        if (mFootView == null) {
            mFootView = mRecyclerView.findViewWithTag("foot");
        }
        if (mFootView == null) {
            return;
        }
        TextView tv = (TextView) mFootView.findViewById(R.id.no_more_textView);
        ProgressBar bar = (ProgressBar) mFootView.findViewById(R.id.load_more_progressBar);
        if (isFinishLoad) {
            if (isLastPage()) {
                mFootView.setVisibility(VISIBLE);
                bar.setVisibility(GONE);
                tv.setText("没有更多啦~");
            } else {
                mFootView.setVisibility(GONE);
            }
        } else {
            if (isLastPage()) {
                mFootView.setVisibility(VISIBLE);
                bar.setVisibility(GONE);
                tv.setText("没有更多啦~");
            } else {
                mFootView.setVisibility(VISIBLE);
                bar.setVisibility(VISIBLE);
                tv.setText("正在加载更多信息...");
            }
        }
    }

    public void addItemDecoration(RecyclerView.ItemDecoration decor) {
        if (mRecyclerView != null) {
            mRecyclerView.addItemDecoration(decor);
        }
    }

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

    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {
        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }
}
