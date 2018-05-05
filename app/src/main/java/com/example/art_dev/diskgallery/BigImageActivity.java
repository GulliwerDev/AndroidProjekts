package com.example.art_dev.diskgallery;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
//активность, отображающая большие картинки
public class BigImageActivity extends FragmentActivity {
    public static final String EXTRA_POS = "position";

    private ViewPager viewPager;
    private DiskInfo mInfo;
    private PagerAdapter adapter;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //делаем на полный экран
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_big_image);
        mInfo = DiskInfo.getContext();
        //получаем номер картинки
        Intent i = getIntent();
        int position = i.getIntExtra(EXTRA_POS, 0);
        final int count = mInfo.getItemCnt();
        //отображаем картинки во ViewPager
        viewPager = (ViewPager) findViewById(R.id.pager);
        adapter = new BigImageAdapter(getSupportFragmentManager(),count);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position);
        final android.app.ActionBar actionBar = getActionBar();
        //прописываем заголовок ActionBar
        String title = (position+1)+"/"+count;
        actionBar.setTitle(title);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            //слушатель сменвы страницы
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                //прописываем новый заголовок
                String newTitle = (position+1)+"/"+count;
                actionBar.setTitle(newTitle);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //обрабатываем нажатие кнопки назад
        switch (item.getItemId()){
            case  R.id.menu_item_back:
                finish();
                return true;
                default:
                    return super.onOptionsItemSelected(item);

        }

    }
}
