
package com.android.carair.fragments;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.carair.R;
import com.android.carair.activities.CleanTimerActivity;
import com.android.carair.api.CarAirReqTask;
import com.android.carair.api.RespProtocolPacket;
import com.android.carair.api.Timer;
import com.android.carair.common.CarAirManager;
import com.android.carair.fragments.base.BaseFragment;
import com.android.carair.fragments.base.FragmentViewBase;
import com.android.carair.net.HttpErrorBean;
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
        lvTimer.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            }
            
        });
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
            List<Timer> list = null;
//            String timer = Util.getTimer(getActivity());
//            if (!TextUtils.isEmpty(timer)) {
//                list = new ArrayList<Timer>();
            new CarAirReqTask() {
                
                @Override
                public void onCompleteSucceed(RespProtocolPacket packet) {
//                  JSONObject jo = new JSONObject(timer);
//                  JSONArray ja = jo.getJSONArray("timer");
//                  for (int i = 0; i < ja.length(); i++) {
//                      task = new Timer();
//                      jo = ja.getJSONObject(i);
//                      task.setTitle(jo.getString("title"));
//                      task.setStart_time(jo.getString("start_time"));
//                      task.setRepeat(jo.getString("repeat"));
//                      list.add(task);
//                  }
                    if(packet != null && packet.getRespMessage() != null){
                        List<Timer> list = packet.getRespMessage().getDevinfo().getTimer();
                        if(list != null && list.size() > 0){
                            CarAirManager.getInstance().setTimer(list);
                            TimerTaskAdapter adapter = new TimerTaskAdapter(list);
                            lvTimer.setAdapter(adapter);
                        }
                    }
                }
                
                @Override
                public void onCompleteFailed(int type, HttpErrorBean error) {
                    
                }
            }.timer(this.getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    class TimerTaskAdapter extends BaseAdapter {
        List<Timer> timerList = null;

        public TimerTaskAdapter(List<Timer> list) {
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

            Timer task = timerList.get(position);
            holder.tvTime.setText(task.getHour()+":"+task.getMin());
            holder.tvTitle.setText(task.getTitle());
            holder.tvRepeat.setText(Util.convertRepeat(Integer.parseInt(task.getRepeat())));
            return convertView;
        }

    }

    class TimerTaskHolder {
        TextView tvTime;
        TextView tvTitle;
        TextView tvRepeat;
    }

}