package cn.zcgames.sdk.mqttsdk.home.view.fragment;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import butterknife.BindView;
import cn.berfy.sdk.http.callback.RequestCallBack;
import cn.berfy.sdk.http.model.NetError;
import cn.berfy.sdk.http.model.NetResponse;
import cn.berfy.sdk.mvpbase.base.CommonFragment;
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter;
import cn.berfy.sdk.mvpbase.util.AppUtils;
import cn.berfy.sdk.mvpbase.util.LogF;
import cn.berfy.sdk.mvpbase.view.XWebView;
import cn.zcgames.sdk.im.R;
import cn.zcgames.sdk.mqttsdk.home.model.HomeBean;
import cn.zcgames.sdk.mqttsdk.http.HomeApi;

/**
 * 首页主界面
 */
public class HomeFragment extends CommonFragment {
    public static final String TAG = "HomeFragment";
    @BindView(R.id.home_webView)
    XWebView mWebView;
    @BindView(R.id.webView_progressBar)
    ProgressBar progressBar;
    private String mUrl;

    @Override
    public void initData(Bundle savedInstanceState) {

    }

    @Override
    public int getContentViewId() {
        return R.layout.fragment_main_home;
    }


    @Override
    public void initView() {
        //设置标题
        setTitleBar();
        getUrl();
    }


    public void getUrl() {
        if (!TextUtils.isEmpty(mUrl)) return;
        HomeApi.Companion.getInstance().getAppHomeInfo(1, new RequestCallBack<HomeBean>() {
            @Override
            public void onStart() {

            }

            @NonNull
            @Override
            public void onFinish(NetResponse<HomeBean> response) {
                if (response.isOk() && null != response.data) {
                    mUrl = response.data.getHome();
                    if (!TextUtils.isEmpty(mUrl)) loadWebView();
                }
            }

            @Override
            public void onError(NetError error) {

            }
        });
    }

    private void setTitleBar() {
        showTitleBar();
        getTitleBar().setLeftIcon(false);
        darkStatusBar(false);
        getTitleBar().setTitleColor(ContextCompat.getColor(mContext, cn.polaris.mqttuikit.R.color.white_normal));
    }

    @Override
    public BasePresenter initPresenter() {
        return null;
    }


    /**
     * 加载webView
     */
    private void loadWebView() {
        if (mWebView == null) return;
        mWebView.loadUrl(mUrl);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.e(TAG, "onPageStarted====" + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                String title = mWebView.getTitle();
                getTitleBar().setTitle(title);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Log.e(TAG, "onReceivedError");
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
                Log.e(TAG, "onReceivedSslError");
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.e(TAG, "shouldOverrideUrlLoading url=" + url);
                return Build.VERSION.SDK_INT < 26 && load(view, url);
            }

            //是否在webview内加载页面
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
                        request.getUrl().toString() : request.toString();
                LogF.d(TAG, "shouldOverrideUrlLoading--Url" + url);
                return load(view, url);
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                Log.e(TAG, "onProgressChanged newProgress=" + newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);//加载完网页进度条消失
                } else {
                    progressBar.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
                    progressBar.setProgress(newProgress);//设置进度值
                }
            }

            // FILE UPLOAD >=21
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                return true;
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
//                if (TextUtils.isEmpty(mTitle) && !TextUtils.isEmpty(title)) {//传进来title了就不修改了
//                    mTitle = title;
//                    if (!TextUtils.isEmpty(mTitle))
//                        ((TextView) findViewById(R.id.wap_title_tv)).setText(mTitle);
//                }
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result) {
                return true;
            }
        });
    }

    private boolean load(WebView view, String url) {
        return false;
//        if (url.startsWith("http:") || url.startsWith("https:")) {
//            view.loadUrl(url);
//            return false;
//        } else {
//            try {
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                startActivity(intent);
//                return true;
//            } catch (Exception e) {
//                e.printStackTrace();
//                return false;
//            }
//        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (null != mWebView) {
            mWebView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (null != mWebView) mWebView.onPause();
    }

    @Override
    public void onDestroyView() {
        if (null != mWebView) mWebView.destroy();
        AppUtils.releaseAllWebViewCallback();
        super.onDestroyView();
    }
}
