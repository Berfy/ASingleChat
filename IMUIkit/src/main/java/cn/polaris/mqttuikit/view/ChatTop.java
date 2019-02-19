package cn.polaris.mqttuikit.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import cn.polaris.mqttuikit.R;


/**
 * 聊天界面顶部标题栏
 */
public class ChatTop extends RelativeLayout {

    private static final String TAG = "IMLogTag_IM顶部标题栏";
    private LinearLayout mLlContent;

    public ChatTop(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.im_chat_top, this);
        initView();
    }

    public ChatTop(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.im_chat_top, this);
        initView();
    }

    private void initView() {
        mLlContent = findViewById(R.id.im_chat_top_content);
    }

    public void setView(View view) {
        mLlContent.removeAllViews();
        mLlContent.addView(view);
    }
}
