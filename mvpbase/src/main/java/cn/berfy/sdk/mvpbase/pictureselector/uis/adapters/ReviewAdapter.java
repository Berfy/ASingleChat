package cn.berfy.sdk.mvpbase.pictureselector.uis.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

import cn.berfy.sdk.mvpbase.pictureselector.entities.ImageEntity;
import cn.berfy.sdk.mvpbase.pictureselector.uis.fragments.ScaleImageFragment;


/**
 * created by arvin on 16/9/3 17:13
 * email：1035407623@qq.com
 */
public class ReviewAdapter extends FragmentStatePagerAdapter {

    private ArrayList<ImageEntity> mItems = new ArrayList<>();

    public ReviewAdapter(FragmentManager fm) {
        super(fm);
    }

    public ArrayList<ImageEntity> getData() {
        return mItems;
    }

    public void setData(ArrayList<ImageEntity> imageEntities) {
        mItems = imageEntities;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Fragment getItem(int position) {
        return new ScaleImageFragment(mItems.get(position));
    }


}
