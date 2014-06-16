
package com.android.carair.activities;

import java.util.List;

import com.actionbarsherlock.app.SherlockActivity;
import com.android.carair.R;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class WarningValueSetActivity extends SherlockActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.carair_warning_set_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("预警值设置");
        getSupportActionBar().setBackgroundDrawable(
        getResources().getDrawable(R.drawable.actionbar_background));
//        List<list> 
    }

    class WarningSetAdapter extends BaseAdapter {
        List<String> list;

        public WarningSetAdapter(List<String> list) {
            this.list = list;
        }

        public void setList(List<String> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return null;
        }

    }
}
