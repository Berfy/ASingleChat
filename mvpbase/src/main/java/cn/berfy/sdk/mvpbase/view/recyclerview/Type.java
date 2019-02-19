package cn.berfy.sdk.mvpbase.view.recyclerview;

import android.view.ViewGroup;

public class Type {

    public static final int TYPE_NULL = -1;
    public static final int TYPE_RECYCLERVIEW = 0;
    public static final int TYPE_LISTVIEW = 1;
    public static final int TYPE_SCROLLVIEW = 2;
    public static final int TYPE_VIEWPAGER = 3;

    public int type = TYPE_NULL;
    public ViewGroup viewGroup;
}