
package com.android.carair.activities;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.android.carair.R;
import com.android.carair.api.CarAirReqTask;
import com.android.carair.api.RespProtocolPacket;
import com.android.carair.fragments.HistoryFragment;
import com.android.carair.fragments.Item;
import com.android.carair.fragments.base.FragmentPageManager;
import com.android.carair.net.HttpErrorBean;
import com.android.carair.utils.SerializableMap;

public class HistoryActivity extends SherlockFragmentActivity {
    int type;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.carair_container_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (getIntent() != null && getIntent().hasExtra("type")) {
            type = getIntent().getIntExtra("type", 0);
        }
        // FragmentPageManager.getInstance().setFragmentManager(this.getSupportFragmentManager());
        // FragmentPageManager.getInstance().pushPageById(new HistoryFragment(),
        // HistoryFragment.class.getName(), R.id.fragment_container);
        requestHistory();
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

    private void requestHistory() {
        new CarAirReqTask() {

            @Override
            public void onCompleteSucceed(RespProtocolPacket packet) {
                try {
                    if (packet != null) {
                        String pm25 = null;
                        if (Item.ITEM_IN_CAR == type) {
                            pm25 = packet.getRespMessage().getDevinfo().getPm25();
                        } else {
                            pm25 = packet.getRespMessage().getAir().getOpm25();
                        }

                        SerializableMap myMap = new SerializableMap();
                        Map<Double,Double> map = new HashMap<Double,Double>();

                        if (!TextUtils.isEmpty(pm25)) {

                            // 拆分数据
                            String[] items = pm25.split(",");
                            String[] keys = new String[items.length];
                            String[] values = new String[items.length];

                            for (int i = 0; i < items.length; i++) {
                                keys[i] = items[i].substring(0, items[i].indexOf(":"));
                                values[i] = items[i].substring(items[i].indexOf(":") + 1,
                                        items[i].length());
                                double key = Double.parseDouble(keys[i]);
                                double value = Double.parseDouble(values[i]);
                                map.put(key, value);
                            }

                            myMap.setMap(map);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("map", myMap);

                            // 打开fragment
                            FragmentPageManager.getInstance().setFragmentManager(
                                    HistoryActivity.this.getSupportFragmentManager());
                            FragmentPageManager.getInstance().pushPageById(new HistoryFragment(),
                                    HistoryFragment.class.getName(), R.id.fragment_container,bundle);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                Toast.makeText(HistoryActivity.this,"无历史数据", 1).show();
            }

            @Override
            public void onCompleteFailed(int type, HttpErrorBean error) {
                Toast.makeText(HistoryActivity.this,"读取数据失败", 1).show();
            }

        }.history(this);

    }
}
