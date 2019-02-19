package cn.berfy.sdk.mvpbase.view.recyclerview;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;
import cn.berfy.sdk.mvpbase.R;
import cn.berfy.sdk.mvpbase.config.Constant;
import cn.berfy.sdk.mvpbase.util.DeviceUtils;
import cn.berfy.sdk.mvpbase.util.LogF;

/**
 * 下拉刷新view
 *
 * @author Berfy
 */
public class RefreshableView extends LinearLayout {

    private static final String TAG = "下拉刷新";
    private Scroller mScroller;
    private LinearLayout mLlSecondView;
    private View mHeaderView;
    private TextView mDownTextView;
    private OnRefreshListener mOnRefreshListener;
    private long mRefreshTime;
    private int mRefreshHeight, mMaxHeight;//最大高度
    private final long MIN_FINISH_REFRESH_TIME = 100;//最小刷新时间,刷新操作至少1.5秒，显示美观
    private final long MIN_TO_REFRESH_TIME = 1000;
    private final static int SCROLL_DURATION = 500;
    private int mLastY;

    private boolean mIsEnable = true;//开启和禁止下拉刷新
    // 在刷新中标记
    private boolean mIsRefreshing = false;

    private Context mContext;

    //如果视图包含ViewPger需要屏蔽滑动事件
    private float mStartY;
    private float mStartX;
    // 记录viewPager是否拖拽的标记
    private boolean mIsVpDragger;

    public RefreshableView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public RefreshableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public RefreshableView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        mHeaderView = LayoutInflater.from(mContext).inflate(
                R.layout.view_refresh_listview_head, null);
        LinearLayout layout_anim = mHeaderView.findViewById(R.id.layout_anim);
        // 下拉显示text
        mDownTextView = (TextView) mHeaderView
                .findViewById(R.id.head_tipsTextView);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
        addView(mHeaderView, lp);
        mLlSecondView = new LinearLayout(mContext);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(mLlSecondView, layoutParams);
        LogF.d(TAG, "子布局数量" + getChildCount());
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view == mHeaderView) {
                LogF.d(TAG, "布局位置" + i + "  类别：头布局");
            } else if (view instanceof RecyclerView) {
                LogF.d(TAG, "布局位置" + i + "  类别：RecyclerView");
            }
        }
    }

    public void setEnable(boolean isEnable) {
        mIsEnable = isEnable;
    }

    public void setBgColor(int color) {
        LogF.d(TAG, "设置背景色" + color);
        mHeaderView.findViewById(R.id.layout_header_bg).setBackgroundColor(color);
    }

    public void setView(View view) {
        mLlSecondView.addView(view);
        checkView(this);
    }

    public int getVisiableHeight() {
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
                    int m = y - mLastY;
                    if (m == 0) {
                        m = 1;
                    }
                    double speed = ((mMaxHeight - getVisiableHeight()) * 0.1) / (mMaxHeight * 0.1);
                    m *= speed;
                    if (getVisiableHeight() > 0 || m > 0) {
                        LogF.d("距离", getVisiableHeight() + "  " + m + "  速度" + speed);
                        doMovement(getVisiableHeight() + m);
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
                            if (getVisiableHeight() < mRefreshHeight) {
                                doMovement(getVisiableHeight() + 10);
                            }
                        }
                    });
                    if (getVisiableHeight() >= mRefreshHeight) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mIsRefreshing = true;
                                LogF.d("循环", getVisiableHeight() + " " + mRefreshHeight);
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
                    Thread.sleep(300);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                while (!((Activity) mContext).isFinishing() && !mIsRefreshing) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (getVisiableHeight() < mRefreshHeight) {
                                doMovement(getVisiableHeight() + 10);
                            }
                        }
                    });
                    if (getVisiableHeight() >= mRefreshHeight) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mIsRefreshing = true;
                                LogF.d("循环", getVisiableHeight() + " " + mRefreshHeight);
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

    private void setUp() {
        mScroller.startScroll(0, getVisiableHeight(), 0, -getVisiableHeight(), SCROLL_DURATION);
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
        mScroller.startScroll(0, getVisiableHeight(), 0, -getVisiableHeight(), SCROLL_DURATION);
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
        mScroller.startScroll(0, getVisiableHeight(), 0, mRefreshHeight - getVisiableHeight(), SCROLL_DURATION);
        postInvalidate();
        LogF.d("刷新", "=======>");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRefreshTime = System.currentTimeMillis();
                if (mOnRefreshListener != null) {
                    mOnRefreshListener.onRefresh(RefreshableView.this);
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
    private void doMovement(int height) {
        // TODO Auto-generated method stub
        if (height < 0)
            height = 0;
        if (height > mMaxHeight) {
            height = mMaxHeight;
        }
        LayoutParams lp = (LayoutParams) mHeaderView.getLayoutParams();
        lp.height = height;
        mHeaderView.setLayoutParams(lp);
//        mDownTextView.setVisibility(View.VISIBLE);
        float progress = (float) ((height * 0.1) / (mRefreshHeight * 0.1));//进度
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
                if (mIsEnable && m > 6 && canScroll(e)) {
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
//                RecyclerView.LayoutManager layoutManager = ((RecyclerView) type.viewGroup).getLayoutManager();
//                if(layoutManager instanceof LinearLayoutManager){
//                    ((LinearLayoutManager)layoutManager).findFirstVisibleItemPosition()
//                }
//            int count = ((RecyclerView) type.viewGroup).getChildCount();
            if (!canScrollVertically(((RecyclerView) type.viewGroup), -1)) {//滑动到底不可滑动了拦截事件
                return true;
            }
//                } else {
//                    LogF.d(TAG, "RecyclerView不可以下拉");
//                    return false;
//                }
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

    public boolean canScrollHorizontally(RecyclerView recyclerView, int direction) {
        final int offset = recyclerView.computeHorizontalScrollOffset();
        final int range = recyclerView.computeHorizontalScrollRange() - recyclerView.computeHorizontalScrollExtent();
        LogF.d(TAG, "RecyclerView滑动检测canScrollHorizontally  direction=" + direction + "  offset=" + offset + "  range=" + range);
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
//        LogF.d(TAG, "要查找的布局数量" + count);
        Type type = new Type();
        for (int i = 0; i < count; i++) {
            View view = rootView.getChildAt(i);
//            LogF.d(TAG, "查找布局" + i);
            if (null != view && view instanceof ViewGroup) {
                if (view instanceof RecyclerView) {
//                    LogF.d(TAG, "查找布局 找到666 " + i + " === RecyclerView " + view.getTag());
                    type.type = Type.TYPE_RECYCLERVIEW;
                    type.viewGroup = (ViewGroup) view;
                    break;
                } else if (view instanceof ListView) {
                    type.type = Type.TYPE_LISTVIEW;
                    type.viewGroup = (ViewGroup) view;
                    break;
                } else if (view instanceof ScrollView) {
//                    LogF.d(TAG, "查找布局 找到666 " + i + " === ScrollView " + view.getTag());
                    type.type = Type.TYPE_SCROLLVIEW;
                    type.viewGroup = (ViewGroup) view;
                    break;
                } else if (view instanceof ViewPager) {
//                    LogF.d(TAG, "查找布局 找到666 " + i + " === ViewPager " + view.getTag());
                    type.type = Type.TYPE_VIEWPAGER;
                    type.viewGroup = (ViewGroup) view;
                    break;
                } else {
                    if (view instanceof LinearLayout) {
//                        LogF.d(TAG, "查找布局 找到" + i + " === LinearLayout " + view.getTag());
                        type = checkView((ViewGroup) view);
                    } else if (view instanceof RelativeLayout) {
//                        LogF.d(TAG, "查找布局 找到" + i + " === RelativeLayout " + view.getTag());
                        type = checkView((ViewGroup) view);
                    } else if (view instanceof FrameLayout) {
//                        LogF.d(TAG, "查找布局 找到" + i + " === FrameLayout " + view.getTag());
                        type = checkView((ViewGroup) view);
                    } else {
//                        LogF.d(TAG, "查找布局 没有用的布局 " + view.getTag() + i);
                        type = checkView((ViewGroup) view);
                    }
                }
            } else {
//                LogF.d(TAG, "不是ViewGroup" + i);
            }
        }
        if (type.type == Type.TYPE_NULL) {
//            LogF.d(TAG, "没有找到 ");
        }
        return type;
    }

}
