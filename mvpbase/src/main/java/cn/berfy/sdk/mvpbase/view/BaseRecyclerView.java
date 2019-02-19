package cn.berfy.sdk.mvpbase.view;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;

import com.bumptech.glide.Glide;

/**
 * 基类recyclerView
 * Created by admin on 2017/12/14.
 */

public class BaseRecyclerView extends android.support.v7.widget.RecyclerView {
    private static final String TAG = "BaseRecyclerView";
    private LoadMoreDataListener mLoadMoreDataListener;
    private Handler handler = new Handler();
    private boolean stopLoadData = false;
    private Context mContext;

    public BaseRecyclerView(Context context) {
        super(context);
        init();
    }

    private void init() {
        this.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int lastShowPosition = -1;
                int firstShowPosition = -1;
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                    if (layoutManager instanceof GridLayoutManager) {
                        lastShowPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
                        firstShowPosition = ((GridLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
                    } else if (layoutManager instanceof LinearLayoutManager) {
                        lastShowPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                        firstShowPosition = ((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
                    } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                        int[] lastPositions = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                        ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(lastPositions);
                        lastShowPosition = findMax(lastPositions);
                        int[] firstPositions = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                        ((StaggeredGridLayoutManager) layoutManager).findFirstCompletelyVisibleItemPositions(firstPositions);
                        firstShowPosition = findMin(lastPositions);
                    }
                    if (lastShowPosition == recyclerView.getLayoutManager().getItemCount() - 1
                            && firstShowPosition != 0) {
                        if (mLoadMoreDataListener != null) {
                            handler.post(() -> {
                                if (!stopLoadData) {//表示允许刷新数据
                                    stopLoadData = true;
                                    mLoadMoreDataListener.loadMoreData();
                                } else {
                                    Log.i(TAG, "不刷新—————————" + stopLoadData);
                                }
                            });
                        }
                    }
                }
                //使Glide在RecyclerView滑动时不去加载图片,从而提高列表滑动流畅度
                setGlideLoadEvent(newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
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
                stopLoad();
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


    public BaseRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public BaseRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setLoadMoreDataListener(LoadMoreDataListener mLoadMoreDataListener) {
        this.mLoadMoreDataListener = mLoadMoreDataListener;
    }

    /**
     * 停止刷新的方法
     */
    public void finishLoadMore() {
        this.stopLoadData = false;
    }


    /**
     * 接口回调
     */
    public interface LoadMoreDataListener {
        void loadMoreData();
    }

    //找到数组中的最大值
    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    //找到数组中的最小值
    private int findMin(int[] firstPositions) {
        int min = firstPositions[0];
        for (int value : firstPositions) {
            if (value < min) {
                min = value;
            }
        }
        return min;
    }
}
