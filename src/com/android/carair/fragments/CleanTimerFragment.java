
package com.android.carair.fragments;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.carair.R;
import com.android.carair.activities.CleanTimerActivity;
import com.android.carair.api.CarAirReqTask;
import com.android.carair.api.RespProtocolPacket;
import com.android.carair.api.Timer;
import com.android.carair.common.CarAirManager;
import com.android.carair.fragments.base.BaseFragment;
import com.android.carair.fragments.base.FragmentPageManager;
import com.android.carair.fragments.base.FragmentViewBase;
import com.android.carair.net.HttpErrorBean;
import com.android.carair.utils.Log;
import com.android.carair.utils.Util;
import com.google.gson.Gson;
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
                Timer timer = (Timer) arg0.getAdapter().getItem(arg2);
                FragmentPageManager.getInstance().setFragmentManager(getFragmentManager());
                Bundle bundle = new Bundle();
                bundle.putInt("index", timer.getIndex());
                bundle.putString("hour", timer.getHour());
                bundle.putString("min", timer.getMin());
                bundle.putString("repeat", timer.getRepeat());
                bundle.putString("title", timer.getTitle());
                FragmentPageManager.getInstance().pushPageByIdWithAnimation(
                        new AddCleanTimerFragment(), AddCleanTimerFragment.class.getName(),
                        R.id.fragment_container, bundle);
            }

        });
        getTasks();
        return mMainView;
    }

    @Override
    public void onResume() {
        ((CleanTimerActivity) getActivity()).setActionBar(CleanTimerActivity.STATE_QUERY);
        super.onResume();
    }

    private void getTasks() {
        try {
            List<Timer> list = null;
            // String timer = Util.getTimer(getActivity());
            // if (!TextUtils.isEmpty(timer)) {
            // list = new ArrayList<Timer>();
            startLoadingStatus(false);
            new CarAirReqTask() {

                @Override
                public void onCompleteSucceed(RespProtocolPacket packet) {
                    // JSONObject jo = new JSONObject(timer);
                    // JSONArray ja = jo.getJSONArray("timer");
                    // for (int i = 0; i < ja.length(); i++) {
                    // task = new Timer();
                    // jo = ja.getJSONObject(i);
                    // task.setTitle(jo.getString("title"));
                    // task.setStart_time(jo.getString("start_time"));
                    // task.setRepeat(jo.getString("repeat"));
                    // list.add(task);
                    // }
                    stopLoadingStatus();
                    if (packet != null && packet.getRespMessage() != null) {
                        List<Timer> list = packet.getRespMessage().getDevinfo().getTimer();
                        if (list != null && list.size() > 0) {
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
            // notifyDataSetChanged();
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
                holder.cbIsTimerOn = (CheckBox) convertView.findViewById(R.id.cbIsTimerOn);
                convertView.setTag(holder);
            } else {
                holder = (TimerTaskHolder) convertView.getTag();
            }

            final Timer task = timerList.get(position);
            String min = "";
            try {
                boolean isNum = task.getMin().matches("[0-9]"); 
                if(isNum){
                    min = "0" + task.getMin();
                }
                else{
                    min = task.getMin();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.tvTime.setText(task.getHour() + ":" + min);
            holder.tvTitle.setText(task.getTitle());
            holder.tvRepeat.setText(Util.convertRepeat(Integer.parseInt(task.getRepeat())));
            holder.cbIsTimerOn
                    .setChecked(Util.convertIsTimerOn(Integer.parseInt(task.getRepeat())));
            holder.cbIsTimerOn.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // convert repeat
                    task.setRepeat(Util.setRepeatOn(isChecked, Integer.parseInt(task.getRepeat()))
                            + "");
                    CarAirManager.getInstance().setTimer(timerList);

                    Gson gson = new Gson();
                    String gstr = gson.toJson(timerList);
                    JSONArray ja = null;
                    try {
                        ja = new JSONArray(gstr);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    new CarAirReqTask() {

                        @Override
                        public void onCompleteSucceed(RespProtocolPacket packet) {
                            Toast.makeText(getActivity(), "修改成功", 1).show();
                        }

                        @Override
                        public void onCompleteFailed(int type, HttpErrorBean error) {
                            Toast.makeText(getActivity(), "修改失败", 1).show();
                        }
                    }.timerset(getActivity(), ja);
                }
            });
            return convertView;
        }

    }

    class TimerTaskHolder {
        TextView tvTime;
        TextView tvTitle;
        TextView tvRepeat;
        CheckBox cbIsTimerOn;
    }

}
