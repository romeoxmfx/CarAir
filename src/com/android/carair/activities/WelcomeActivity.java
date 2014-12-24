
package com.android.carair.activities;

import java.util.ArrayList;
import java.util.List;

import com.android.goodhelpercarair.R;
import com.android.carair.utils.Util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class WelcomeActivity extends Activity {
    ViewPager pagerWelcome;
    List<View> viewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.carair_welcome_activity);
        if(!Util.isFirstLogin(this)){
            startActivity(new Intent(WelcomeActivity.this,LogoActivity.class));
            finish();
            return;
        }
        pagerWelcome = (ViewPager) findViewById(R.id.vp_welcome);
        viewList = new ArrayList<View>();
        LayoutInflater lf = getLayoutInflater().from(this);
        ImageView iv = (ImageView) lf.inflate(R.layout.carair_welcome_item, null);
        iv.setImageResource(R.drawable.walkthroughs1);
        ImageView iv1 = (ImageView) lf.inflate(R.layout.carair_welcome_item, null);
        iv1.setImageResource(R.drawable.walkthroughs2);
        ImageView iv2 = (ImageView) lf.inflate(R.layout.carair_welcome_item, null);
        iv2.setImageResource(R.drawable.walkthroughs3);
        ImageView iv3 = (ImageView) lf.inflate(R.layout.carair_welcome_item, null);
        iv3.setImageResource(R.drawable.walkthroughs4);
        ImageView iv4 = (ImageView) lf.inflate(R.layout.carair_welcome_item, null);
        iv4.setImageResource(R.drawable.walkthroughs5);
        viewList.add(iv);
        viewList.add(iv1);
        viewList.add(iv2);
        viewList.add(iv3);
        viewList.add(iv4);
        final MyViewPagerAdapter adapter = new MyViewPagerAdapter(viewList);
        pagerWelcome.setAdapter(adapter);
        pagerWelcome.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                 if(arg0 == adapter.getCount() -1 && arg1 == 0){
//                Log.i("arg0=" + arg0 + " arg1 = " + arg1 + " arg2 = " + arg2);
                     Util.setFirstLogin(WelcomeActivity.this, false);
                     startActivity(new Intent(WelcomeActivity.this,LoginActivity.class));
                     finish();
                 }
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
    }

    public class MyViewPagerAdapter extends PagerAdapter {
        private List<View> mListViews;

        public MyViewPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;// 构造方法，参数是我们的页卡，这样比较方便。
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mListViews.get(position));// 删除页卡
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) { // 这个方法用来实例化页卡
            container.addView(mListViews.get(position), 0);// 添加页卡
            return mListViews.get(position);
        }

        @Override
        public int getCount() {
            return mListViews.size();// 返回页卡的数量
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;// 官方提示这样写
        }
    }
}
