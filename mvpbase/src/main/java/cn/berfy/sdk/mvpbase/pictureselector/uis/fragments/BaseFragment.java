package cn.berfy.sdk.mvpbase.pictureselector.uis.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import cn.berfy.sdk.mvpbase.R;
import cn.berfy.sdk.mvpbase.pictureselector.entities.ImageEntity;


/**
 * created by arvin on 16/8/28 18:08
 * email：1035407623@qq.com
 */
public abstract class BaseFragment extends Fragment implements View.OnClickListener{
    protected View mRoot;
    protected TextView tvTitle;
    protected TextView tvEnsure;
    protected TextView tvCheck;

    protected ArrayList<ImageEntity> mSelectedImages;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRoot = inflater.inflate(setContentViewId(), null);
        initHeader();
        init(savedInstanceState);
        return mRoot;
    }

    private void initHeader() {
        tvTitle = getView(R.id.tv_title);
        tvEnsure = getView(R.id.tv_ensure);
        tvCheck = getView(R.id.tv_check);

        getView(R.id.img_back).setOnClickListener(this);
        tvEnsure.setOnClickListener(this);

    }

    @SuppressWarnings("unchecked")
    protected <T extends View> T getView(int viewId) {
        return (T) mRoot.findViewById(viewId);
    }

    @SuppressWarnings({"unchecked", "unused"})
    protected final <T extends ViewGroup> T getLayout(int layoutResId) {
        return (T) LayoutInflater.from(getActivity()).inflate(layoutResId, null);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.img_back){
            onBackClicked();
            return ;
        }

        if(v.getId()==R.id.tv_ensure){
            onEnsureClicked();
        }
    }

    protected String getEnsureText(int selectedCount) {
        if (selectedCount == 0) {
            return "确定";
        }
        String s = selectedCount < 10 ? selectedCount + "     确定" : selectedCount + "    确定";
        return  s;
    }

    /**
     * 发送给Activity让其处理返回
     */
    protected void onEnsureClicked() {
        EventBus.getDefault().post(mSelectedImages);
    }

    protected abstract int setContentViewId();

    protected abstract void init(Bundle savedInstanceState);

    public abstract void update(Bundle data);

    public abstract void onBackClicked();

}
