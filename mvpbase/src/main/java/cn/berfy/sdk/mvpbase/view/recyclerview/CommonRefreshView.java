package cn.berfy.sdk.mvpbase.view.recyclerview;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;
import cn.berfy.sdk.mvpbase.R;
import cn.berfy.sdk.mvpbase.config.Constant;
import cn.berfy.sdk.mvpbase.util.CommonUtil;
import cn.berfy.sdk.mvpbase.util.DeviceUtils;
import cn.berfy.sdk.mvpbase.util.LogF;

/**
 * 通用的下拉和自动获取更多的滑动View
 *
 * @author Berfy
 */
public class CommonRefreshView extends LinearLayout {

    private static final String TAG = "CommonRefreshView下拉刷新";
    private Scroller mScroller;
    private View mHeaderView;
    private TextView mDownTextView;
    private OnRefreshListener mOnRefreshListener;
    private long mRefreshTime;
    private int mRefreshHeight, mMaxHeight;//最大高度
    private final long MIN_FINISH_REFRESH_TIME = 100;//最小刷新时间,刷新操作至少1.5秒，显示美观
    private final long MIN_TO_REFRESH_TIME = 1000;
    private final static int SCROLL_DURATION = 500;
    private int mLastY;

    private boolean mIsEnableRefresh = true;//开启和禁止下拉刷新
    private boolean mIsEnableLoadMore = true;//开启和禁止获取更多
    // 在刷新中标记
    private boolean mIsRefreshing = false;

    private Context mContext;

    //如果视图包含ViewPger需要屏蔽滑动事件
    private float mStartY;
    private float mStartX;
    // 记录viewPager是否拖拽的标记
    private boolean mIsVpDragger;

    private RecyclerView mRecyclerView;
    private View mFootView;
    private int mLastVisibleItem;
    private boolean mIsLastPage;
    private boolean mIsCanLoadMore = true;//避免频繁加载

    public CommonRefreshView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public CommonRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public CommonRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        // TODO Auto-generated method stub
        // 滑动对象
        setOrientation(LinearLayout.VERTICAL);
        mRefreshHeight = DeviceUtils.dpToPx(mContext, 60);
        mMaxHeight = DeviceUtils.dpToPx(mContext, 120);
        mScroller = new Scroller(mContext, new DecelerateInterpolator());
        // 刷新视图顶端的的view
        mHeaderView = LayoutInflater.from(mContext).inflate(R.layout.view_refresh_listview_head, null);
        LinearLayout layout_anim = mHeaderView.findViewById(R.id.layout_anim);
        // 下拉显示text
        mDownTextView = (TextView) mHeaderView
                .findViewById(R.id.head_tipsTextView);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
        addView(mHeaderView, lp);
        LogF.d(TAG, "子布局数量" + getChildCount());
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view == mHeaderView) {
                LogF.d(TAG, "布局位置" + i + "  类别：头布局");
            } else if (view instanceof RecyclerView) {
                LogF.d(TAG, "布局位置" + i + "  类别：RecyclerView");
            }
        }
        post(new Runnable() {
            @Override
            public void run() {
                Type type = checkView(CommonRefreshView.this);
                if (null != type) {
                    if (type.type == Type.TYPE_RECYCLERVIEW) {
                        RecyclerView recyclerView = ((RecyclerView) type.viewGroup);
                        mRecyclerView = recyclerView;
                        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                            @Override
                            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                super.onScrollStateChanged(recyclerView, newState);
                                LogF.d(TAG, "isLastPage===" + isLastPage());
                                if (isLastPage()) {
                                    return;
                                }
                                if (newState == RecyclerView.SCROLL_STATE_IDLE && mLastVisibleItem + 1 >= mRecyclerView.getAdapter().getItemCount() && !mIsLastPage) {
                                    updateText(false);
                                    LogF.d(TAG, "获取更多" + mIsCanLoadMore);
                                    if (mIsCanLoadMore) {
                                        LogF.d(TAG, "获取更多ing");
                                        mIsCanLoadMore = false;
                                        if (null != mOnRefreshListener)
                                            mOnRefreshListener.loadMore(mRecyclerView);
                                    }
                                }
                            }

                            @Override
                            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                super.onScrolled(recyclerView, dx, dy);
                                if (null == mRecyclerView.getLayoutManager()) {
                                    return;
                                }
                                LogF.d(TAG, "滑动===" + dx + "," + dy);
                                if (Math.abs(dy) > 0) {//列表滑动后才可以获取更多，避免不满一屏也显示加载更多
                                    if (mRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                                        mLastVisibleItem = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findLastVisibleItemPosition();
                                    }
                                    if (mRecyclerView.getLayoutManager() instanceof GridLayoutManager) {
                                        mLastVisibleItem = ((GridLayoutManager) mRecyclerView.getLayoutManager()).findLastVisibleItemPosition();
                                    }
                                }
                            }
                        });
//                    }
                    } else if (type.type == Type.TYPE_LISTVIEW) {
                        ListView listView = (ListView) type.viewGroup;
                        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                            @Override
                            public void onScrollStateChanged(AbsListView absListView, int i) {
                                LogF.d(TAG, "isLastPage===" + isLastPage());
                                if (isLastPage()) {
                                    return;
                                }
                                //footerview不为空说明Adapter包含了footerview的itemType mLastVisibleItem+1  否则不+1
//                                boolean isScrollBottom = (null != mFootView ? mLastVisibleItem + 1 : mLastVisibleItem) >= mRecyclerView.getAdapter().getItemCount();

                                if (i == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && mLastVisibleItem + 1 >= listView.getAdapter().getCount() && !mIsLastPage) {
                                    updateText(false);
                                    LogF.d(TAG, "获取更多" + mIsCanLoadMore);
                                    if (mIsEnableLoadMore && mIsCanLoadMore) {
                                        LogF.d(TAG, "获取更多ing");
                                        mIsCanLoadMore = false;
                                        if (null != mOnRefreshListener)
                                            mOnRefreshListener.loadMore(mRecyclerView);
                                    }
                                }
                            }

                            @Override
                            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                                mLastVisibleItem = firstVisibleItem + visibleItemCount;
                            }
                        });
                    } else if (type.type == Type.TYPE_SCROLLVIEW) {
                        ScrollView scrollView = (ScrollView) type.viewGroup;
                    }
                }
            }
        });
    }

    public void setEnabled(boolean isEnableRefresh, boolean isEnableLoadMore) {
        mIsEnableRefresh = isEnableRefresh;
        mIsEnableLoadMore = isEnableLoadMore;
    }

    /**
     * 没有更多数据调用
     */
    public void noData() {
        LogF.d(TAG, "没有数据了");
        if (null != mRecyclerView) {
            LogF.d(TAG, "没有数据了");
            mIsLastPage = true;
            if (mFootView == null) {
                mFootView = mRecyclerView.findViewWithTag("foot");
            }
            if (mFootView == null) {
                return;
            }
            mFootView.setVisibility(View.VISIBLE);
            TextView tv = mFootView.findViewById(R.id.no_more_textView);
            ProgressBar bar = mFootView.findViewById(R.id.load_more_progressBar);
            bar.setVisibility(GONE);
            tv.setText("没有更多啦~");
        }
    }

    public boolean isLastPage() {
        return mIsLastPage;
    }

    public void setBgColor(int colorResId) {
        LogF.d(TAG, "设置背景色" + colorResId);
        mHeaderView.findViewById(R.id.layout_header_bg).setBackgroundColor(CommonUtil.getColor(colorResId));
    }

    public int getVisibleHeight() {
        return mHeaderView.getHeight();
    }

    public void setVisiableHeight(int height) {
        doMovement(height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int y = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 记录下y坐标
                mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i(TAG, "ACTION_MOVE" + "  " + mIsRefreshing);
                if (!mIsRefreshing) {
                    // y移动坐标
                    double m = y - mLastY;
                    if (m == 0) {
                        m = 1;
                    }
                    double speed = ((mMaxHeight - getVisibleHeight()) * 0.1) / (mMaxHeight * 0.1);
                    m *= speed;
                    if (getVisibleHeight() > 0 || m > 0) {
                        LogF.d("距离", getVisibleHeight() + "  " + m + "  速度" + speed);
                        doMovement(getVisibleHeight() + m);
                    }
                    // 记录下此刻y坐标
                    mLastY = y;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                Log.i(TAG, "ACTION_UP" + "  " + mIsRefreshing);
                if (!mIsRefreshing) {
                    mIsRefreshing = true;
                    fling();
                }
                break;
        }
        return true;
    }

    public void startRefreshing(final int deplayTime) {
        Constant.EXECUTOR_ANIM.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(deplayTime);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                while (!((Activity) mContext).isFinishing() && !mIsRefreshing) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (getVisibleHeight() < mRefreshHeight) {
                                doMovement(getVisibleHeight() + 10);
                            }
                        }
                    });
                    if (getVisibleHeight() >= mRefreshHeight) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mIsRefreshing = true;
                                LogF.d("循环", getVisibleHeight() + " " + mRefreshHeight);
                                refresh();
                            }
                        });
                        break;
                    }
                    try {
                        Thread.sleep(10);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void startRefreshing() {
        Constant.EXECUTOR_ANIM.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                while (!((Activity) mContext).isFinishing() && !mIsRefreshing) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (getVisibleHeight() < mRefreshHeight) {
                                doMovement(getVisibleHeight() + 10);
                            }
                        }
                    });
                    if (getVisibleHeight() >= mRefreshHeight) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mIsRefreshing = true;
                                LogF.d("循环", getVisibleHeight() + " " + mRefreshHeight);
                                refresh();
                            }
                        });
                        break;
                    }
                    try {
                        Thread.sleep(10);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * up事件处理
     */
    private void fling() {
        // TODO Auto-generated method stub
        LayoutParams lp = (LayoutParams) mHeaderView
                .getLayoutParams();
        Log.i(TAG, "fling()" + lp.height);

        if (lp.height >= mRefreshHeight) {// 拉到了触发可刷新事件
            refresh();
        } else {
            returnInitState();
        }
    }

    /**
     * 结束刷新事件
     */
    public void finishRefresh() {
        updateText(true);
        mIsCanLoadMore = true;
        Log.i(TAG, "执行====finishRefresh");
        long time = System.currentTimeMillis();
        if (time - mRefreshTime >= MIN_FINISH_REFRESH_TIME) {
            setUp();
        } else {
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    setUp();
                }
            }, MIN_FINISH_REFRESH_TIME - (time - mRefreshTime));
        }
    }

    private void updateText(boolean isFinishLoad) {
        if (null != mRecyclerView) {
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
    }

    private void setUp() {
        mScroller.startScroll(0, getVisibleHeight(), 0, -getVisibleHeight(), SCROLL_DURATION);
        postInvalidate();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mIsRefreshing = false;
            }
        }, SCROLL_DURATION);
    }

    private void returnInitState() {
        // TODO Auto-generated method stub
        mScroller.startScroll(0, getVisibleHeight(), 0, -getVisibleHeight(), SCROLL_DURATION);
        postInvalidate();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mIsRefreshing = false;
            }
        }, SCROLL_DURATION);
    }

    private void refresh() {
        // TODO Auto-generated method stub
        mIsRefreshing = true;
//        mDownTextView.setVisibility(View.VISIBLE);
        mDownTextView.setText(R.string.tip_refresh_state_refreshing);
        mScroller.startScroll(0, getVisibleHeight(), 0, mRefreshHeight - getVisibleHeight(), SCROLL_DURATION / 2);
        postInvalidate();
        LogF.d("刷新", "=======>");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRefreshTime = System.currentTimeMillis();
                mIsLastPage = false;
                if (mOnRefreshListener != null) {
                    mOnRefreshListener.onRefresh(CommonRefreshView.this);
                }
            }
        }, MIN_TO_REFRESH_TIME);
    }

    private Handler mHandler = new Handler() {

    };

    /**
     *
     */
    @Override
    public void computeScroll() {
        // TODO Auto-generated method stub
        if (mScroller.computeScrollOffset()) {
            LayoutParams lp = (LayoutParams) mHeaderView.getLayoutParams();
            lp.height = mScroller.getCurrY();
            mHeaderView.setLayoutParams(lp);
            postInvalidate();
        }
    }

    /**
     * 下拉move事件处理
     *
     * @param height
     */
    private void doMovement(double height) {
        // TODO Auto-generated method stub
        mScroller.abortAnimation();
        if (height < 0)
            height = 0;
        if (height > mMaxHeight) {
            height = mMaxHeight;
        }
        LayoutParams lp = (LayoutParams) mHeaderView.getLayoutParams();
        lp.height = (int) height;
        mHeaderView.setLayoutParams(lp);
//        mDownTextView.setVisibility(View.VISIBLE);
//        float progress = (float) ((height * 0.1) / (mRefreshHeight * 0.1));//进度
        if (lp.height >= mRefreshHeight) {
            Log.i(TAG, "执行====松开刷新");
            mDownTextView.setText(R.string.tip_refresh_state_release_to_refresh);
        } else {
            Log.i(TAG, "执行====下拉刷新");
            mDownTextView.setText(R.string.tip_refresh_state_pull_to_refresh);
        }
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        mOnRefreshListener = listener;
    }

    /*
     * 该方法一般和ontouchEvent (non-Javadoc)
     *
     * @see
     * android.view.ViewGroup#onInterceptTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        // TODO Auto-generated method stub
        int action = e.getAction();
        int y = (int) e.getRawY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastY = y;
                /*=============ViewPager避免冲突==============*/
                // 记录手指按下的位置
                mStartX = e.getX();
                mStartY = e.getY();
                // 初始化标记
                mIsVpDragger = false;
                /*=============ViewPager避免冲突==============*/
                break;

            case MotionEvent.ACTION_MOVE:
                // y移动坐标
                int m = y - mLastY;
                // 记录下此刻y坐标
                mLastY = y;
                if (mIsEnableRefresh && m > 6 && canScroll(e)) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:

                break;

            case MotionEvent.ACTION_CANCEL:

                break;
        }
        return false;
    }

    private boolean canScroll(MotionEvent e) {
        // TODO Auto-generated method stub
        Type type = checkView(this);
        LogF.d(TAG, "布局类型" + type.type);
        if (type.type == Type.TYPE_LISTVIEW) {
            if (null != type.viewGroup) {
                int top = type.viewGroup.getTop();
                int pad = ((ListView) type.viewGroup).getListPaddingTop();
                if ((Math.abs(top - pad)) < 3
                        && ((ListView) type.viewGroup).getFirstVisiblePosition() == 0) {
                    return true;
                } else {
                    return false;
                }
            }
        } else if (type.type == Type.TYPE_SCROLLVIEW) {
            if (((ScrollView) type.viewGroup).getScrollY() == 0) {
                return true;
            } else {
                return false;
            }
        } else if (type.type == Type.TYPE_RECYCLERVIEW) {
//            int count = ((RecyclerView) type.viewGroup).getChildCount();
            LogF.d(TAG, "RecyclerView可以下拉" + !canScrollVertically(((RecyclerView) type.viewGroup), -1));
            if (!canScrollVertically(((RecyclerView) type.viewGroup), -1)) {
                return true;
            }
        } else if (type.type == Type.TYPE_VIEWPAGER) {
            /*=============ViewPager避免冲突==============*/
            // 如果viewpager正在拖拽中，那么不拦截它的事件，直接return false；
            if (mIsVpDragger) {
                return false;
            }
            // 获取当前手指位置
            float endY = e.getY();
            float endX = e.getX();
            float distanceX = Math.abs(endX - mStartX);
            float distanceY = Math.abs(endY - mStartY);
            // 如果X轴位移大于Y轴位移，那么将事件交给viewPager处理。
            if (distanceX > distanceY) {
                mIsVpDragger = true;
                return false;
            }
            /*=============ViewPager避免冲突==============*/
        }
        return false;
    }

    public boolean canScrollVertically(RecyclerView recyclerView, int direction) {
        final int offset = recyclerView.computeVerticalScrollOffset();
        final int range = recyclerView.computeVerticalScrollRange() - recyclerView.computeVerticalScrollExtent();
        LogF.d(TAG, "RecyclerView滑动检测canScrollVertically  direction=" + direction + "  offset=" + offset + "  range=" + range);
        if (range == 0) return false;
        if (direction < 0) {
            return offset > 0;
        } else {
            return offset < range - 1;
        }
    }

    /**
     * 刷新监听接口
     *
     * @author Nono
     */
    public interface OnRefreshListener {
        void onRefresh(ViewGroup view);

        void loadMore(ViewGroup view);
    }

    private Type checkView(ViewGroup rootView) {
        int count = rootView.getChildCount();
        LogF.d(TAG, "要查找的布局数量" + count);
        Type type = new Type();
        for (int i = 0; i < count; i++) {
            View view = rootView.getChildAt(i);
            LogF.d(TAG, "查找布局" + i);
            if (null != view && view instanceof ViewGroup) {
                if (view instanceof RecyclerView) {
                    LogF.d(TAG, "查找布局 找到666 " + i + " === RecyclerView " + view.getTag());
                    type.type = Type.TYPE_RECYCLERVIEW;
                    type.viewGroup = (ViewGroup) view;
                    break;
                } else if (view instanceof ListView) {
                    type.type = Type.TYPE_LISTVIEW;
                    type.viewGroup = (ViewGroup) view;
                    break;
                } else if (view instanceof ScrollView) {
                    LogF.d(TAG, "查找布局 找到666 " + i + " === ScrollView " + view.getTag());
                    type.type = Type.TYPE_SCROLLVIEW;
                    type.viewGroup = (ViewGroup) view;
                    break;
                } else if (view instanceof ViewPager) {
                    LogF.d(TAG, "查找布局 找到666 " + i + " === ViewPager " + view.getTag());
                    type.type = Type.TYPE_VIEWPAGER;
                    type.viewGroup = (ViewGroup) view;
                    break;
                } else {
                    if (view instanceof LinearLayout) {
                        LogF.d(TAG, "查找布局 找到" + i + " === LinearLayout " + view.getTag());
                        type = checkView((ViewGroup) view);
                    } else if (view instanceof RelativeLayout) {
                        LogF.d(TAG, "查找布局 找到" + i + " === RelativeLayout " + view.getTag());
                        type = checkView((ViewGroup) view);
                    } else if (view instanceof FrameLayout) {
                        LogF.d(TAG, "查找布局 找到" + i + " === FrameLayout " + view.getTag());
                        type = checkView((ViewGroup) view);
                    } else {
                        LogF.d(TAG, "查找布局 没有用的布局 " + view.getTag() + i);
                        type = checkView((ViewGroup) view);
                    }
                }
            } else {
                LogF.d(TAG, "不是ViewGroup" + i);
            }
        }
        if (type.type == Type.TYPE_NULL) {
            LogF.d(TAG, "没有找到 ");
        }
        return type;
    }

}
