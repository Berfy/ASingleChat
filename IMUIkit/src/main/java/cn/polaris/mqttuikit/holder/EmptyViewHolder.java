package cn.polaris.mqttuikit.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.polaris.mqttuikit.R;


/**
 * 当RecyclerView的数据为空时，显示的布局
 * @author NorthStar
 * @date  2019/1/4 10:14
 */
public class EmptyViewHolder extends RecyclerView.ViewHolder {

    public LinearLayout empty_layout;
    public ImageView imageView;
    public TextView tv;

    public EmptyViewHolder(View itemView) {
        super(itemView);
        empty_layout = itemView.findViewById(R.id.empty_layout);
        imageView = itemView.findViewById(R.id.iv_not_data_icon);
        tv = itemView.findViewById(R.id.tv_not_data_tip);
    }
}
