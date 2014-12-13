
package com.android.airhelper.activities;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.android.airhelper.common.CarairConstants;
import com.android.airhelper.fragments.AddCleanTimerFragment;
import com.android.carair.R;
import com.android.carair.airhelper.base.FragmentPageManager;
import com.android.carair.utils.Util;
import com.umeng.analytics.MobclickAgent;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WarningValueSetActivity extends SherlockFragmentActivity implements OnClickListener {
    RelativeLayout rlPm;
    RelativeLayout rlHarmful;
    TextView tvPm;
    TextView tvHarmful;
    EditText etText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.carair_warning_set_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("预警值设置");
        getSupportActionBar().setBackgroundDrawable(
                getResources().getDrawable(R.drawable.actionbar_background));

        rlPm = (RelativeLayout) findViewById(R.id.rlWarningPM);
        rlPm.setOnClickListener(this);
        rlHarmful = (RelativeLayout) findViewById(R.id.rlWarningHarmful);
        rlHarmful.setOnClickListener(this);
        tvPm = (TextView) findViewById(R.id.tvpmWarning);
        tvHarmful = (TextView) findViewById(R.id.tvHarmfulWarning);
        // FragmentPageManager.getInstance().setFragmentManager(getSupportFragmentManager());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.rlWarningPM:
                setValueAlert(CarairConstants.TYPE_PM);
                break;
            case R.id.rlWarningHarmful:
                setValueAlert(CarairConstants.TYPE_HARMFUL);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    public void refresh() {
        tvPm.setText(Util.getWarningPM(this) + "");
        tvHarmful.setText(Util.getWarningHarmful(this) + "");
    }

    private void setValueAlert(final int type) {
        final Builder builder = new AlertDialog.Builder(this).setTitle("请输入预警值")
                .setPositiveButton("确认",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                String id = etText.getText().toString();
                                int value = -1;
                                try {
                                    value = Integer.parseInt(id);
                                } catch (Exception e) {
                                }
                                if (TextUtils.isEmpty(id)) {
                                    Toast.makeText(WarningValueSetActivity.this, "请输入正确预警值", 1)
                                            .show();
                                    return;
                                }
                                if (value != -1) {
                                    if (type == CarairConstants.TYPE_PM) {
                                        Util.saveWarningPM(value, WarningValueSetActivity.this);
                                    } else {
                                        Util.saveWarningHarmful(value, WarningValueSetActivity.this);
                                    }
                                    refresh();
                                }
                            }
                        })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        etText = new EditText(this);
        etText.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(etText);
        builder.show();
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
