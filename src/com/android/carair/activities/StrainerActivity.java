
package com.android.carair.activities;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.android.carair.R;
import com.android.carair.fragments.CleanTimerFragment;
import com.android.carair.fragments.StrainHowToUseFragment;
import com.android.carair.fragments.base.FragmentPageManager;
import com.umeng.analytics.MobclickAgent;

public class StrainerActivity extends SherlockFragmentActivity {
    FragmentPageManager manager;
    Button btReset;
    LinearLayout llHowToUse;
    
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.carair_home_item_strainer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(
                getResources().getDrawable(R.drawable.actionbar_background));
        
        btReset = (Button) findViewById(R.id.btStrainerReset);
        llHowToUse = (LinearLayout) findViewById(R.id.strainerAlert);
        manager = FragmentPageManager.getInstance();
        manager.setFragmentManager(getSupportFragmentManager());
        manager.pushPageById(new StrainHowToUseFragment(),
                StrainHowToUseFragment.class.getName(), R.id.fragment_container, true, null);
        btReset.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                
            }
        });
        
        llHowToUse.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
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
}
