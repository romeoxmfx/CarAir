
package com.android.carair.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.android.carair.R;
import com.android.carair.common.CarairConstants;
import com.android.carair.fragments.AddCleanTimerFragment;
import com.android.carair.fragments.CleanTimerFragment;
import com.android.carair.fragments.MainFragment;
import com.android.carair.fragments.base.BaseFragment;
import com.android.carair.fragments.base.FragmentPageManager;
import com.android.carair.utils.Util;

public class CleanTimerActivity extends SherlockFragmentActivity implements OnMenuItemClickListener {
    FragmentPageManager manager;
    BaseFragment fragment;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.carair_container_activity);
        manager = FragmentPageManager.getInstance();
        manager.setFragmentManager(getSupportFragmentManager());
        manager.pushPageById(new CleanTimerFragment(),
                CleanTimerFragment.class.getName(), R.id.fragment_container, true, null);
        // setActionBar();

    }

    public void setActionBar() {
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setTitle("定时任务");
        bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_background));
        supportInvalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                this.finish();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
        if (getActiveFragment() != null && getActiveFragment() instanceof CleanTimerFragment) {
            menu.add("add")
                    .setIcon(R.drawable.app_panel_add_icon)
                    .setOnMenuItemClickListener(this)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setActionBar();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if ("add".equals(item.getTitle())) {
            // Toast.makeText(this, "add", 1).show();
            manager = FragmentPageManager.getInstance();
            manager.setFragmentManager(getSupportFragmentManager());
            manager.pushPageByIdWithAnimation(new AddCleanTimerFragment(),
                    AddCleanTimerFragment.class.getName(), R.id.fragment_container, null);
            return true;
        }
        return false;
    }

    public BaseFragment getActiveFragment()
    {
        FragmentManager manager = this.getSupportFragmentManager();
        int nCount = manager.getBackStackEntryCount();

        if (nCount > 0)
        {
            String tag = manager.getBackStackEntryAt(nCount - 1).getName();
            fragment = (BaseFragment) manager.findFragmentByTag(tag);
        }

        return fragment;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            if (FragmentPageManager.getInstance().canGoBack())
            {
                return super.onKeyDown(keyCode, event);
            } else
            {
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
