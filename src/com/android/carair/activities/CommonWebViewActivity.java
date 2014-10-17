
package com.android.carair.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.RenderPriority;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.android.carair.R;
import com.umeng.analytics.MobclickAgent;

public class CommonWebViewActivity extends SherlockFragmentActivity {
    WebView wb;
    ProgressBar pb;
    String title;
    private ActionBar bar;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.carair_webview_activity);
        setActionBar();
        wb = (WebView) findViewById(R.id.wbActivity);
        wb.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                pb.setVisibility(View.INVISIBLE);
                super.onPageFinished(view, url);
            }
        });
        pb = (ProgressBar) findViewById(R.id.pb);
        init();
        if (getIntent().hasExtra("url")) {
            String url = getIntent().getStringExtra("url");
            if (url != null) {
                wb.loadUrl(url);
            }
        }
        
        if(getIntent().hasExtra("title")){
            title = getIntent().getStringExtra("title");
            bar.setTitle(title);
        }
    }

    private void init() {
        // 默认消除垂直滚动条，解决页面左右滑动问题
        wb.setVerticalScrollBarEnabled(false);

        WebSettings setting = wb.getSettings();
        // 设置访问文件权限
        setting.setAllowFileAccess(true);
        setting.setJavaScriptEnabled(true);
        setting.setAppCacheEnabled(true); // 默认是关闭的
        setting.setAppCacheMaxSize(1024 * 1024 * 5); // 缓存大小
        setting.setAllowFileAccess(true);// 设置允许访问文件数据
        setting.setSupportZoom(true);
        setting.setBuiltInZoomControls(false);
        setting.setJavaScriptCanOpenWindowsAutomatically(true);
        setting.setDomStorageEnabled(true);
        // 为windwave设置ua,保证回调成功
        setting.setUserAgentString(setting.getUserAgentString() + " WindVane/3.0.0");
        // 不让保存用户密码，保存的话会明文存放在/data/app/中，root的用户可以很容易拿到用户数据
        setting.setSavePassword(false);
        setting.setDatabaseEnabled(true);
        setting.setUseWideViewPort(true);
        setting.setLoadWithOverviewMode(true);
        setting.setRenderPriority(RenderPriority.HIGH);
        // setting.setBlockNetworkImage(true);

        // enable database
        if (android.os.Build.VERSION.SDK_INT >= 5) {
            setting.setDatabaseEnabled(true);
            String dbPath = this.getDir("database", Context.MODE_PRIVATE).getPath();
            setting.setDatabasePath(dbPath);

            // enable Geolocation
            setting.setGeolocationEnabled(true);
            setting.setGeolocationDatabasePath(dbPath);
        }
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

    public void setActionBar() {
        bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setTitle("活动页");
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setActionBar();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (wb.canGoBack()) {
                wb.goBack();
                return true;
            } else {
                return super.onKeyDown(keyCode, event);
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
