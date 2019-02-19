package cn.berfy.sdk.mvpbase.listener;

import android.view.View;

/**
 * item的点击事件
 *
 * @author NorthStar
 * @date 2018/8/27 12:20
 */
public interface ListenerItemClick {
    //条目的事件
    void itemListener(View view, int position);
}
