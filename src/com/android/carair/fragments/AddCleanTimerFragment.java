
package com.android.carair.fragments;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
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
import com.android.carair.utils.Util;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class AddCleanTimerFragment extends BaseFragment {
    TimePicker timepicker;
    TextView tvRepeat;
    EditText etTitle;
    Button delete;
    LinearLayout llrepeat;
    int index;
    int repeat;
    int state;
    String title;
    long time;
    boolean hasData;
    int[] days = new int[] {
            0, 0, 0, 0, 0, 0, 0
    };
    private ChooseRepeatDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mMainView = (FragmentViewBase) inflater.inflate(
                R.layout.carair_cleantimer_add_fragment, null);
        ((CleanTimerActivity) getActivity()).setActionBar();
        timepicker = (TimePicker) mMainView.findViewById(R.id.timerPicker);
        tvRepeat = (TextView) mMainView.findViewById(R.id.tvRepeatDetail);
        etTitle = (EditText) mMainView.findViewById(R.id.etTitle);
        delete = (Button) mMainView.findViewById(R.id.btDelete);
        llrepeat = (LinearLayout) mMainView.findViewById(R.id.llrepeat);
        Bundle data = getArguments();
        if (data != null) {
            index = data.getInt("index");
            repeat = data.getInt("repeat");
            state = data.getInt("state");
            title = data.getString("title");
            time = data.getLong("time") * 1000;
            Date date = new Date(time);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int second = cal.get(Calendar.SECOND);
            timepicker.setCurrentHour(hour);
            timepicker.setCurrentMinute(second);
        }
        delete.setText("确认添加");
        timepicker.setIs24HourView(true);

        llrepeat.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showRepeatDialog();
            }
        });

        delete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!hasData) {
                    // add
                    try {
                        if (CarAirManager.getInstance().getTimer() != null
                                && CarAirManager.getInstance().getTimer().size() > 0) {
                            // generate id
                            List<Timer> list = CarAirManager.getInstance().getTimer();
                            Collections.sort(list);
                            Timer timer = list.get(list.size() - 1);
                            int index = timer.getIndex() + 1;

                            Gson gson = new Gson();
                            String gstr = gson.toJson(list);
                            JSONArray ja = new JSONArray(gstr);

                            JSONObject jo = new JSONObject();
                            jo.put("index", index);
                            jo.put("title", etTitle.getText());
                            jo.put("hour", timepicker.getCurrentHour());
                            jo.put("min", timepicker.getCurrentMinute());
                            StringBuffer sb = new StringBuffer();
                            sb.append("" + days[0] + days[1] + days[2] + days[3] + days[4]
                                    + days[5] + days[6]);
                            jo.put("repeat", Integer.valueOf(sb.toString(), 2));
                            ja.put(jo);
                            
                            new CarAirReqTask() {

                                @Override
                                public void onCompleteSucceed(RespProtocolPacket packet) {
                                    Toast.makeText(getActivity(), "添加成功", 1).show();
                                    FragmentPageManager.getInstance().popToBack();
                                }

                                @Override
                                public void onCompleteFailed(int type, HttpErrorBean error) {
                                    Toast.makeText(getActivity(), "添加失败", 1).show();
                                    FragmentPageManager.getInstance().popToBack();
                                }
                            }.timerset(getActivity(), ja);
                        } else {
                            JSONArray ja = new JSONArray();
                            JSONObject jo = new JSONObject();
                            jo.put("index", 0);
                            jo.put("title", etTitle.getText());
                            jo.put("hour", timepicker.getCurrentHour());
                            jo.put("min", timepicker.getCurrentMinute());
                            StringBuffer sb = new StringBuffer();
                            sb.append("" + days[0] + days[1] + days[2] + days[3] + days[4]
                                    + days[5] + days[6]);
                            jo.put("repeat", Integer.valueOf(sb.toString(), 2));
                            ja.put(jo);
                            // Util.saveTimer(jo.toString(), getActivity());
                            new CarAirReqTask() {

                                @Override
                                public void onCompleteSucceed(RespProtocolPacket packet) {
                                    Toast.makeText(getActivity(), "添加成功", 1).show();
                                    FragmentPageManager.getInstance().popToBack();
                                }

                                @Override
                                public void onCompleteFailed(int type, HttpErrorBean error) {
                                    Toast.makeText(getActivity(), "添加失败", 1).show();
                                    FragmentPageManager.getInstance().popToBack();
                                }
                            }.timerset(getActivity(), ja);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    // delete
                }
            }
        });
        return mMainView;
    }

    public void setRepeatDetail() {
        String detail = "";
        int alldays = 0;
        for (int i = 0; i < days.length; i++) {
            if (days[i] == 1) {
                alldays += 1;
                switch (i) {
                    case 0:
                        detail += "星期一,";
                        break;
                    case 1:
                        detail += "星期二,";
                        break;
                    case 2:
                        detail += "星期三,";
                        break;
                    case 3:
                        detail += "星期四,";
                        break;
                    case 4:
                        detail += "星期五,";
                        break;
                    case 5:
                        detail += "星期六,";
                        break;
                    case 6:
                        detail += "星期日,";
                        break;
                    default:
                        break;
                }
            }
        }

        if (alldays == 7) {
            detail = "每天";
        } else {
            if (!TextUtils.isEmpty(detail)) {
                detail = detail.substring(0, detail.lastIndexOf(","));
            }
        }

        tvRepeat.setText(detail);
    }

    private void showRepeatDialog() {
        if (dialog == null) {
            dialog = new ChooseRepeatDialog(getActivity());
            dialog.setTitle("请选择时间");
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "确认",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setRepeatDetail();
                            dialog.dismiss();
                        }

                    });
        }
        dialog.show();
    }

    class ChooseRepeatDialog extends AlertDialog implements android.view.View.OnClickListener {
        View view;

        protected ChooseRepeatDialog(Context context) {
            super(context);
            view = LayoutInflater.from(context).inflate(R.layout.carair_repeat_chooser, null);
            view.findViewById(R.id.llday1).setOnClickListener(this);
            view.findViewById(R.id.llday2).setOnClickListener(this);
            view.findViewById(R.id.llday3).setOnClickListener(this);
            view.findViewById(R.id.llday4).setOnClickListener(this);
            view.findViewById(R.id.llday5).setOnClickListener(this);
            view.findViewById(R.id.llday6).setOnClickListener(this);
            view.findViewById(R.id.llday7).setOnClickListener(this);

            setView(view);
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.llday1:
                    CheckBox cb1 = ((CheckBox) view.findViewById(R.id.cbday1));
                    if (cb1.isChecked()) {
                        cb1.setChecked(false);
                        days[0] = 0;
                    } else {
                        cb1.setChecked(true);
                        days[0] = 1;
                    }
                    break;
                case R.id.llday2:
                    CheckBox cb2 = ((CheckBox) view.findViewById(R.id.cbday2));
                    if (cb2.isChecked()) {
                        cb2.setChecked(false);
                        days[1] = 0;
                    } else {
                        cb2.setChecked(true);
                        days[1] = 1;
                    }
                    break;
                case R.id.llday3:
                    CheckBox cb3 = ((CheckBox) view.findViewById(R.id.cbday3));
                    if (cb3.isChecked()) {
                        cb3.setChecked(false);
                        days[2] = 0;
                    } else {
                        cb3.setChecked(true);
                        days[2] = 1;
                    }
                    break;
                case R.id.llday4:
                    CheckBox cb4 = ((CheckBox) view.findViewById(R.id.cbday4));
                    if (cb4.isChecked()) {
                        cb4.setChecked(false);
                        days[3] = 0;
                    } else {
                        cb4.setChecked(true);
                        days[3] = 1;
                    }
                    break;
                case R.id.llday5:
                    CheckBox cb5 = ((CheckBox) view.findViewById(R.id.cbday5));
                    if (cb5.isChecked()) {
                        cb5.setChecked(false);
                        days[4] = 0;
                    } else {
                        cb5.setChecked(true);
                        days[4] = 1;
                    }
                    break;
                case R.id.llday6:
                    CheckBox cb6 = ((CheckBox) view.findViewById(R.id.cbday6));
                    if (cb6.isChecked()) {
                        cb6.setChecked(false);
                        days[5] = 0;
                    } else {
                        cb6.setChecked(true);
                        days[5] = 1;
                    }
                    break;
                case R.id.llday7:
                    CheckBox cb7 = ((CheckBox) view.findViewById(R.id.cbday7));
                    if (cb7.isChecked()) {
                        cb7.setChecked(false);
                        days[6] = 0;
                    } else {
                        cb7.setChecked(true);
                        days[6] = 1;
                    }
                    break;
                default:
                    break;
            }
        }

    }

}
