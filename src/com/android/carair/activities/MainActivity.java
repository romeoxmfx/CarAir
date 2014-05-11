
package com.android.carair.activities;

import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.android.carair.R;
import com.android.carair.activities.base.BaseActivity;
import com.android.carair.fragments.MainBackMenuFragment;
import com.android.carair.fragments.MainFragment;
import com.android.carair.fragments.base.FragmentPageManager;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class MainActivity extends BaseActivity {
    FragmentPageManager manager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.carair_container_activity);
        manager = FragmentPageManager.getInstance();
        manager.setFragmentManager(getSupportFragmentManager());

        manager.pushPageById(new MainFragment(), MainFragment.class.getName(),
                R.id.fragment_container);

        setBehindContentView(R.layout.carair_container_back);
        manager.pushPageById(new MainBackMenuFragment(), MainBackMenuFragment.class.getName(),
                R.id.fragment_container_back);
        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

        getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_background));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                getSlidingMenu().showMenu();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
