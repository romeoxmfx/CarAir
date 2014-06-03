
package com.android.carair.activities;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.android.carair.R;
import com.android.carair.common.CarairConstants;
import com.android.carair.utils.Util;

public class CleanRatioActivity extends SherlockFragmentActivity {
    RadioButton rbLow;
    RadioButton rbNormal;
    RadioButton rbHigh;
    RadioGroup rgRatio;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.carair_cleanratio_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        rgRatio = (RadioGroup) findViewById(R.id.rgRatio);
        rbLow = (RadioButton) findViewById(R.id.rblow);
        rbNormal = (RadioButton) findViewById(R.id.rbnormal);
        rbHigh = (RadioButton) findViewById(R.id.rbhigh);
        
        int ratio = Util.getRatio(this);
        switch (ratio) {
            case CarairConstants.RATIO_LOW:
                rbLow.setChecked(true);
                break;
            case CarairConstants.RATIO_NORMAL:
                rbNormal.setChecked(true);
                break;
            case CarairConstants.RATIO_HIGH:
                rbHigh.setChecked(true);
                break;
            default:
                break;
        }
        
        rgRatio.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rblow:
                        Util.saveRatio(CarairConstants.RATIO_LOW, CleanRatioActivity.this);
                        break;
                    case R.id.rbnormal:
                        Util.saveRatio(CarairConstants.RATIO_NORMAL, CleanRatioActivity.this);
                        break;
                    case R.id.rbhigh:
                        Util.saveRatio(CarairConstants.RATIO_HIGH, CleanRatioActivity.this);
                        break;
                    default:
                        break;
                }
            }
        });
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
