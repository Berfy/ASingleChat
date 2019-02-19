package cn.berfy.sdk.mvpbase.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * 自定义的优化的WebView
 *
 * @author Berfy && NorthStar
 * @date 2018/9/6 10:11
 */
public class XWebView extends WebView {

    private final String TAG = "XWebView";
    private long mOpenTime = 0;
    private Context mContext;

    public XWebView(Context context) {
        super(context);
        init(context);
    }

    public XWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public XWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mOpenTime = System.currentTimeMillis();
        mContext = context;
        setOption();
        clearCache(true);
        setAcceptThirdPartyCookies();
        versionCheck();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setOption() {
        getSettings().setJavaScriptEnabled(true);//支持JS
        getSettings().setJavaScriptCanOpenWindowsAutomatically(true);//设置允许JS打开新窗口
        getSettings().setAllowFileAccess(true);// 设置允许访问文件数据
        getSettings().setUseWideViewPort(true);//扩大比例的缩放
        getSettings().setSupportZoom(true);//启用缩放
        getSettings().setBuiltInZoomControls(true);//启动缩放工具
        setHorizontalScrollBarEnabled(false);//水平滑动条不显示
        setVerticalScrollBarEnabled(false); //垂直滑动条不显示

        //自适应屏幕
        getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        getSettings().setLoadWithOverviewMode(true);

        getSettings().setDomStorageEnabled(true);//设置是否启用了DOM storage API
        getSettings().setDatabaseEnabled(true);//设置是否启用了数据库存储API
        getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
        getSettings().setAppCacheEnabled(true);
        getSettings().setGeolocationEnabled(true);
        getSettings().getUserAgentString();
        getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);//设置不缓存
    }

    /**
     * 版本适配
     */
    private void versionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
    }

    /**
     * 设置跨域cookie读取
     */
    private void setAcceptThirdPartyCookies() {
        //target 23 default false, so manual set true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true);
        }
    }

    @Override
    public void onResume() {
        resumeTimers();
        //缩放工具恢复使用
        getSettings().setBuiltInZoomControls(true);
        getSettings().setDisplayZoomControls(true);
        super.onResume();
    }

    @Override
    public void onPause() {
        pauseTimers();
        //缩放工具取消使用
        getSettings().setBuiltInZoomControls(false);
        getSettings().setDisplayZoomControls(false);
        super.onPause();
    }

    /**
     * 是否一步步返回
     */
    public boolean close() {
        if (!canGoBack()) {
            return (System.currentTimeMillis() - mOpenTime > 500);
        } else {
            goBack();
            return false;
        }
    }

    @Override
    public void destroy() {
        getSettings().setBuiltInZoomControls(false);
        // call requies API Level 11 ( Android 3.0 + )
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB) {
            getSettings().setDisplayZoomControls(false);
        }
        setVisibility(View.GONE);
        pauseTimers();
        ViewGroup view = (ViewGroup) getRootView();
        view.removeAllViews();
        super.destroy();
    }


    private OnScrollBottomListener listener;
    private int calCount;

    public interface OnScrollBottomListener {
        void scrollToBottom();
    }

    public void registerOnBottomListener(OnScrollBottomListener l) {
        listener = l;
    }

    public void unRegisterOnBottomListener() {
        listener = null;
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        if (this.getHeight() + this.getScrollY() == getHeight()) {
            calCount++;
            if (calCount == 1) {
                if (listener != null) {
                    listener.scrollToBottom();
                }
            }
        } else {
            calCount = 0;
        }
    }
}
