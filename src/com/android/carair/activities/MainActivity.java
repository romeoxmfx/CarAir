
package com.android.carair.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.android.carair.R;
import com.android.carair.activities.base.BaseActivity;
import com.android.carair.api.Activity;
import com.android.carair.api.CarAirReqTask;
import com.android.carair.api.RespProtocolPacket;
import com.android.carair.common.CarAirManager;
import com.android.carair.fragments.HomeFragment;
import com.android.carair.fragments.MainBackMenuFragment;
import com.android.carair.fragments.base.BaseFragment;
import com.android.carair.fragments.base.FragmentPageManager;
import com.android.carair.net.AsyncHttpHelper;
import com.android.carair.net.BizResponse;
import com.android.carair.net.HttpErrorBean;
import com.android.carair.utils.Util;
import com.igexin.sdk.PushManager;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.umeng.analytics.MobclickAgent;

public class MainActivity extends BaseActivity implements OnMenuItemClickListener {
    FragmentPageManager manager;
    BaseFragment fragment;
    BaseFragment mActiveFragment;
    public static final int STATE_NORMAL = 1;
    public static final int STATE_OPEN = 2;
    public static final int STATE_STOP = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.carair_container_activity);
        CarAirManager.getInstance().init(this);
        manager = FragmentPageManager.getInstance();
        manager.setFragmentManager(getSupportFragmentManager());

        // manager.pushPageById(new MainFragment(),
        // MainFragment.class.getName(),
        // R.id.fragment_container);
        // manager.pushContentPage(new MainFragment(),
        // MainFragment.class.getName());
        manager.pushContentPage(new HomeFragment(), HomeFragment.class.getName());
        PushManager.getInstance().initialize(this.getApplicationContext());

        setBehindContentView(R.layout.carair_container_back);
        manager.pushPageById(new MainBackMenuFragment(), MainBackMenuFragment.class.getName(),
                R.id.fragment_container_back, false);
        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);

        getSupportActionBar().setIcon(R.drawable.icon_setting_selector);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setBackgroundDrawable(
                getResources().getDrawable(R.drawable.actionbar_background));
        // sendReg();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        CarAirManager.getInstance().setState(STATE_STOP);
        new CarAirReqTask() {
            
            @Override
            public void onCompleteSucceed(RespProtocolPacket packet) {
                CarAirManager.getInstance().setState(STATE_STOP);
            }
            
            @Override
            public void onCompleteFailed(int type, HttpErrorBean error) {
                CarAirManager.getInstance().setState(STATE_STOP);
            }
        }.query(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (TextUtils.isEmpty(Util.getDeviceId(this))) {
            finish();
        }
        CarAirManager.getInstance().setState(STATE_NORMAL);
//        if (Util.getBadge(this) > 0) {
//            refreshNoticeUI(true);
//        } else {
//            refreshNoticeUI(false);
//        }
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    // private void sendReg() {
    // new CarAirReqTask(){
    //
    // @Override
    // public void onCompleteSucceed(RespProtocolPacket packet) {
    // // TODO Auto-generated method stub
    // Toast.makeText(MainActivity.this, "succeed", Toast.LENGTH_SHORT).show();
    // }
    //
    // @Override
    // public void onCompleteFailed(int type, HttpErrorBean error) {
    // // TODO Auto-generated method stub
    // Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
    // }
    //
    // }.reg(this);
    // try {
    // JSONObject devinfo = new JSONObject();
    // devinfo.put("id", DeviceConfig.getIMSI(this));
    // devinfo.put("mac", DeviceConfig.getMac(this));
    // devinfo.put("ts", System.currentTimeMillis());
    //
    // JSONObject mobileInfo = new JSONObject();
    // mobileInfo.put("id", DeviceConfig.getIMSI(this));
    // mobileInfo.put("mac", DeviceConfig.getMac(this));
    // mobileInfo.put("model", DeviceConfig.getEmulatorValue());
    // mobileInfo.put("cpu", DeviceConfig.getCPU());
    // mobileInfo.put("os", DeviceConfig.getOsVersion());
    // String reso = DeviceConfig.getResolution(this);
    // String rest[] = reso.split("\\*");
    // mobileInfo.put("reso_weight", rest[1]);
    // mobileInfo.put("reso_height", rest[1]);
    // mobileInfo.put("type", "android");
    //
    // JSONObject appinfo = new JSONObject();
    // appinfo.put("ver", DeviceConfig.getAppVersionName(this));
    // appinfo.put("channel", "autocube");
    //
    // JSONObject message = new JSONObject();
    // message.put("devinfo", devinfo);
    // message.put("mobinfo", mobileInfo);
    // message.put("appinfo", appinfo);
    //
    // JSONObject jsonObj = new JSONObject();
    // jsonObj.put("cmd", 0)
    // .put("message", message)
    // .put("cs", "2185375313");
    //
    // RegRequest regRequest = new RegRequest(jsonObj.toString());
    // new RegRequestTask().loadHttpContent(regRequest);
    //
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }

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
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("main")
                .setIcon(R.drawable.icon_place_selector)
                .setOnMenuItemClickListener(this)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
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

    class RegRequestTask extends AsyncHttpHelper {

        @Override
        protected void onHttpSucceed(int type, BizResponse response) {
            Toast.makeText(MainActivity.this, "success", 1).show();
        }

        @Override
        protected void onHttpFailed(int type, HttpErrorBean error) {
            Toast.makeText(MainActivity.this, "fail", 1).show();
        }

    }

    public void refreshNoticeUI(boolean showNotice) {
        if (showNotice) {
            getSupportActionBar().setIcon(R.drawable.icon_setting_selector_newtap);
        } else {
            getSupportActionBar().setIcon(R.drawable.icon_setting_selector);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if ("main".equals(item.getTitle())) {
            // Toast.makeText(this, "main", 1).show();
            // FragmentPageManager.getInstance().setFragmentManager(getSupportFragmentManager());
            // FragmentPageManager.getInstance().pushContentPage(new
            // HomeFragment(),HomeFragment.class.getName(),null);
            // getSlidingMenu().showContent();
            startActivity(new Intent(this, MapActivity.class));
        }
        return false;
    }
}
