
package com.android.carair.activities;

import org.json.JSONObject;

import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.android.carair.R;
import com.android.carair.activities.base.BaseActivity;
import com.android.carair.fragments.MainBackMenuFragment;
import com.android.carair.fragments.MainFragment;
import com.android.carair.fragments.base.FragmentPageManager;
import com.android.carair.net.AsyncHttpHelper;
import com.android.carair.net.BizResponse;
import com.android.carair.net.HttpErrorBean;
import com.android.carair.request.RegRequest;
import com.android.carair.utils.DeviceConfig;
import com.android.carair.utils.NetWork;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class MainActivity extends BaseActivity {
    FragmentPageManager manager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.carair_container_activity);
        manager = FragmentPageManager.getInstance();
        manager.setFragmentManager(getSupportFragmentManager());

        // manager.pushPageById(new MainFragment(),
        // MainFragment.class.getName(),
        // R.id.fragment_container);
        manager.pushContentPage(new MainFragment(), MainFragment.class.getName());

        setBehindContentView(R.layout.carair_container_back);
        manager.pushPageById(new MainBackMenuFragment(), MainBackMenuFragment.class.getName(),
                R.id.fragment_container_back);
        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

        getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setBackgroundDrawable(
                getResources().getDrawable(R.drawable.actionbar_background));
        
        sendReg();
    }

    private void sendReg() {
        try {
            JSONObject devinfo = new JSONObject();
            devinfo.put("id", DeviceConfig.getIMSI(this));
            devinfo.put("mac", DeviceConfig.getMac(this));
            devinfo.put("ts", System.currentTimeMillis());

            JSONObject mobileInfo = new JSONObject();
            mobileInfo.put("id", DeviceConfig.getIMSI(this));
            mobileInfo.put("mac", DeviceConfig.getMac(this));
            mobileInfo.put("model", DeviceConfig.getEmulatorValue());
            mobileInfo.put("cpu", DeviceConfig.getCPU());
            mobileInfo.put("os", DeviceConfig.getOsVersion());
            String reso = DeviceConfig.getResolution(this);
            String rest[] = reso.split("\\*");
            mobileInfo.put("reso_weight", rest[1]);
            mobileInfo.put("reso_height", rest[1]);
            mobileInfo.put("type", "android");

            JSONObject appinfo = new JSONObject();
            appinfo.put("ver", DeviceConfig.getAppVersionName(this));
            appinfo.put("channel", "autocube");

            JSONObject message = new JSONObject();
            message.put("devinfo", devinfo);
            message.put("mobinfo", mobileInfo);
            message.put("appinfo", appinfo);

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("cmd", 0)
                   .put("message", message)
                   .put("cs", "2185375313");

            RegRequest regRequest = new RegRequest(jsonObj.toString());
            new RegRequestTask().loadHttpContent(regRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
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
}
