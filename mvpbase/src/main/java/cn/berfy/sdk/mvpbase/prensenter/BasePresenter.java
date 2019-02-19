package cn.berfy.sdk.mvpbase.prensenter;

import android.content.Context;

public abstract class BasePresenter<T> {

    protected Context mContext;
    protected T mView;

    public void attach(Context context, T view) {
        mContext = context;
        mView = view;
    }

    protected T getView(){
        return mView;
    }

    public void detach() {
//        mView = null;
    }
}
