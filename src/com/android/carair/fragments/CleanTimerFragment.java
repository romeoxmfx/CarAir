
package com.android.carair.fragments;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.carair.R;
import com.android.carair.activities.CleanTimerActivity;
import com.android.carair.api.TimerTask;
import com.android.carair.fragments.base.BaseFragment;
import com.android.carair.fragments.base.FragmentViewBase;
import com.android.carair.utils.Log;
import com.android.carair.utils.Util;
import com.google.gson.JsonObject;

public class CleanTimerFragment extends BaseFragment {
    ListView lvTimer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mMainView = (FragmentViewBase) inflater.inflate(
                R.layout.carair_cleantimer_fragment, null);
        lvTimer = (ListView) mMainView.findViewById(R.id.lvTimer);
        ((CleanTimerActivity) getActivity()).setActionBar();
        getTasks();
        return mMainView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void getTasks() {
        try {
            List<TimerTask> list = null;
            String timer = Util.getTimer(getActivity());
            if (!TextUtils.isEmpty(timer)) {
                list = new ArrayList<TimerTask>();
                TimerTask task = null;
                JSONObject jo = new JSONObject(timer);
                JSONArray ja = jo.getJSONArray("timer");
                for (int i = 0; i < ja.length(); i++) {
                    task = new TimerTask();
                    jo = ja.getJSONObject(i);
                    task.setTitle(jo.getString("title"));
                    task.setStart_time(jo.getString("start_time"));
                    task.setRepeat(jo.getString("repeat"));
                    list.add(task);
                }
                TimerTaskAdapter adapter = new TimerTaskAdapter(list);
                lvTimer.setAdapter(adapter);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    class TimerTaskAdapter extends BaseAdapter {
        List<TimerTask> timerList = null;

        public TimerTaskAdapter(List<TimerTask> list) {
            this.timerList = list;
//            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return timerList.size();
        }

        @Override
        public Object getItem(int position) {
            return timerList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TimerTaskHolder holder = null;
            if (convertView == null) {
                holder = new TimerTaskHolder();
                convertView = LayoutInflater.from(CleanTimerFragment.this.getActivity()).inflate(
                        R.layout.carair_cleantimer_item, null);
                holder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
                holder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
                holder.tvRepeat = (TextView) convertView.findViewById(R.id.tvRepeat);
                convertView.setTag(holder);
            } else {
                holder = (TimerTaskHolder) convertView.getTag();
            }

            TimerTask task = timerList.get(position);
            holder.tvTime.setText(task.getStart_time());
            holder.tvTitle.setText(task.getTitle());
            holder.tvRepeat.setText(task.getRepeat());
            return convertView;
        }

    }

    class TimerTaskHolder {
        TextView tvTime;
        TextView tvTitle;
        TextView tvRepeat;
    }

}
