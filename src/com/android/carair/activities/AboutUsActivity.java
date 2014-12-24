
package com.android.carair.activities;

import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.android.goodhelpercarair.R;
import com.android.carair.api.AppInfo;
import com.android.carair.api.Copyright;
import com.android.carair.utils.Util;
import com.umeng.analytics.MobclickAgent;

public class AboutUsActivity extends SherlockActivity {
    TextView title;
    TextView subTitle;
    TextView about;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.carair_aboutus_fragment);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("关于");
        getSupportActionBar().setBackgroundDrawable(
                getResources().getDrawable(R.drawable.actionbar_background));
        TextView tvVer = (TextView) this.findViewById(R.id.tvVer);
        title = (TextView) findViewById(R.id.tvabouttitle);
        subTitle = (TextView) findViewById(R.id.tvaboutright);
        about = (TextView) findViewById(R.id.tvabout);
        try {
            String version = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
            tvVer.setText(version);
            AppInfo info = Util.getFeature(this);
            if(info != null){
                Copyright right = info.getCopyright();
                if(right != null){
                    title.setText(right.getTitle());
                    subTitle.setText(right.getSubtitle());
                    about.setText(right.getTitle()+"保留所有权利");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
