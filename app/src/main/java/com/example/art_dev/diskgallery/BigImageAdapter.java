package com.example.art_dev.diskgallery;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

//Стандартный адаптер для ViewPager
public class BigImageAdapter extends FragmentPagerAdapter {
    private int mPageCnt;

    public BigImageAdapter(FragmentManager fm, int pageCnt) {
        super(fm);
        mPageCnt = pageCnt;
    }

    @Override
    public Fragment getItem(int position) {
        return PageBigImage.newInstance(position);
    }

    @Override
    public int getCount() {
        return mPageCnt;
    }

}
