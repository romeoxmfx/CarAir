
package com.android.carair.fragments;

import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.carair.R;
import com.android.carair.activities.CleanRatioActivity;
import com.android.carair.activities.CleanTimerActivity;
import com.android.carair.activities.WarningValueSetActivity;
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

public class AddCleanTimerFragment extends BaseFragment {
    TimePicker timepicker;
    TextView tvRepeat;
    EditText etTitle;
    Button btConfirm;
    Button btDelete;
    RelativeLayout llrepeat;
    int index;
    int repeat;
    String title;
    String hour;
    String min;
    boolean hasData;
//    TextView tvWarningPm;
//    TextView tvWarningHarmful;
//    RelativeLayout rlPm;
//    RelativeLayout rlHarmful;
    int[] days = new int[] {
            0, 0, 0, 0, 0, 0, 0, 0
    };
    private ChooseRepeatDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mMainView = (FragmentViewBase) inflater.inflate(
                R.layout.carair_cleantimer_add_fragment, null);
        timepicker = (TimePicker) mMainView.findViewById(R.id.timerPicker);
        timepicker.setIs24HourView(true);
//        tvWarningPm = (TextView) mMainView.findViewById(R.id.tvpmWarning);
//        tvWarningHarmful = (TextView) mMainView.findViewById(R.id.tvHarmfulWarning);
//        rlPm = (RelativeLayout) mMainView.findViewById(R.id.rlpm);
//        rlHarmful = (RelativeLayout) mMainView.findViewById(R.id.rlharmful);
//        rlPm.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                Intent i2 = new Intent(getActivity(), WarningValueSetActivity.class);
//                getActivity().startActivity(i2);
//            }
//        });
//        rlHarmful.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                Intent i2 = new Intent(getActivity(), WarningValueSetActivity.class);
//                getActivity().startActivity(i2);
//            }
//        });
        tvRepeat = (TextView) mMainView.findViewById(R.id.tvRepeatDetail);
        etTitle = (EditText) mMainView.findViewById(R.id.etTitle);
        btConfirm = (Button) mMainView.findViewById(R.id.btConfirm);
        btDelete = (Button) mMainView.findViewById(R.id.btdelete);
        llrepeat = (RelativeLayout) mMainView.findViewById(R.id.llrepeat);
        Bundle data = getArguments();
        try {
            if (data != null) {
                hasData = true;
                index = data.getInt("index");
                repeat = Integer.parseInt(data.getString("repeat"));
                title = data.getString("title");
                hour = data.getString("hour");
                min = data.getString("min");
                timepicker.setCurrentHour(Integer.parseInt(hour));
                timepicker.setCurrentMinute(Integer.parseInt(min));
                etTitle.setText(title);
                String binaryRepeat = Util.byteToBit((byte) repeat);
                char[] c = binaryRepeat.toCharArray();
                for (int i = 0; i < days.length; i++) {
                    days[i] = Integer.parseInt(c[i] + "");
                }
                tvRepeat.setText(Util.convertRepeat(repeat));
                btDelete.setVisibility(View.VISIBLE);
                btConfirm.setText("修改");
            } else {
                btDelete.setVisibility(View.INVISIBLE);
                btConfirm.setText("确认添加");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        llrepeat.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showRepeatDialog();
            }
        });

        btConfirm.setOnClickListener(new OnClickListener() {

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
                                    + days[5] + days[6] + days[7]);
                            jo.put("repeat", Integer.valueOf(sb.toString(), 2));
                            ja.put(jo);

                            sendTimerSet(ja);
                        } else {
                            JSONArray ja = new JSONArray();
                            JSONObject jo = new JSONObject();
                            jo.put("index", 0);
                            jo.put("title", etTitle.getText());
                            jo.put("hour", timepicker.getCurrentHour());
                            jo.put("min", timepicker.getCurrentMinute());
                            StringBuffer sb = new StringBuffer();
                            sb.append("" + days[0] + days[1] + days[2] + days[3] + days[4]
                                    + days[5] + days[6] + days[7]);
                            jo.put("repeat", Integer.valueOf(sb.toString(), 2));
                            ja.put(jo);
                            // Util.saveTimer(jo.toString(), getActivity());
                            sendTimerSet(ja);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    // delete
                    if (CarAirManager.getInstance().getTimer() != null
                            && CarAirManager.getInstance().getTimer().size() > 0) {
                        List<Timer> list = CarAirManager.getInstance().getTimer();
                        for (Timer timer : list) {
                            if (index == timer.getIndex()) {
                                timer.setHour(timepicker.getCurrentHour() + "");
                                timer.setMin(timepicker.getCurrentMinute() + "");
                                timer.setTitle(etTitle.getText().toString());
                                StringBuffer sb = new StringBuffer();
                                sb.append("" + days[0] + days[1] + days[2] + days[3] + days[4]
                                        + days[5] + days[6] + days[7]);
                                timer.setRepeat(Integer.valueOf(sb.toString(), 2) + "");
                                break;
                            }
                        }

                        Gson gson = new Gson();
                        String gstr = gson.toJson(list);
                        JSONArray ja = null;
                        try {
                            ja = new JSONArray(gstr);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        sendTimerSet(ja);

                    }
                }
            }
        });

        btDelete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                List<Timer> list = CarAirManager.getInstance().getTimer();
                Timer mTimer = null;
                for (Timer timer : list) {
                    if (index == timer.getIndex()) {
                        mTimer = timer;
                        break;
                    }
                }
                list.remove(mTimer);

                Gson gson = new Gson();
                String gstr = gson.toJson(list);
                JSONArray ja = null;
                try {
                    ja = new JSONArray(gstr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                sendTimerSet(ja);
            }
        });
        return mMainView;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((CleanTimerActivity) getActivity()).setActionBar(CleanTimerActivity.STATE_ADD);
//        tvWarningPm.setText(Util.getWarningPM(getActivity()) + "");
//        tvWarningHarmful.setText(Util.getWarningHarmful(getActivity()) + "");
    }

    private void sendTimerSet(JSONArray ja) {
        new CarAirReqTask() {

            @Override
            public void onCompleteSucceed(RespProtocolPacket packet) {
                Toast.makeText(getActivity(), "操作成功", 1).show();
                FragmentPageManager.getInstance().popToBack();
            }

            @Override
            public void onCompleteFailed(int type, HttpErrorBean error) {
                Toast.makeText(getActivity(), "操作失败", 1).show();
                FragmentPageManager.getInstance().popToBack();
            }
        }.timerset(getActivity(), ja);
    }

    public void setRepeatDetail() {
        String detail = "";
        int alldays = 0;
        for (int i = 0; i < days.length; i++) {
            if (days[i] == 1) {
                alldays += 1;
                switch (i) {
                    case 6:
                        detail += "一,";
                        break;
                    case 5:
                        detail += "二,";
                        break;
                    case 4:
                        detail += "三,";
                        break;
                    case 3:
                        detail += "四,";
                        break;
                    case 2:
                        detail += "五,";
                        break;
                    case 1:
                        detail += "六,";
                        break;
                    case 0:
                        detail += "日,";
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
//        if (dialog == null) {
            dialog = new ChooseRepeatDialog(getActivity());
            dialog.setTitle("请选择时间");
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "确认",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setRepeatDetail();
//                            String binaryRepeat = Util.byteToBit((byte) repeat);
//                            char[] c = binaryRepeat.toCharArray();
//                            for (int i = 0; i < days.length; i++) {
//                                days[i] = Integer.parseInt(c[i] + "");
//                            }
//                            tvRepeat.setText(Util.convertRepeat(repeat));
                            dialog.dismiss();
                        }

                    });
//        }
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

            ((CheckBox)view.findViewById(R.id.cbday1)).setChecked(days[6] == 1 ? true:false);
            ((CheckBox)view.findViewById(R.id.cbday2)).setChecked(days[5] == 1 ? true:false);
            ((CheckBox)view.findViewById(R.id.cbday3)).setChecked(days[4] == 1 ? true:false);
            ((CheckBox)view.findViewById(R.id.cbday4)).setChecked(days[3] == 1 ? true:false);
            ((CheckBox)view.findViewById(R.id.cbday5)).setChecked(days[2] == 1 ? true:false);
            ((CheckBox)view.findViewById(R.id.cbday6)).setChecked(days[1] == 1 ? true:false);
            ((CheckBox)view.findViewById(R.id.cbday7)).setChecked(days[0] == 1 ? true:false);
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
                        days[6] = 0;
                    } else {
                        cb1.setChecked(true);
                        days[6] = 1;
                    }
                    break;
                case R.id.llday2:
                    CheckBox cb2 = ((CheckBox) view.findViewById(R.id.cbday2));
                    if (cb2.isChecked()) {
                        cb2.setChecked(false);
                        days[5] = 0;
                    } else {
                        cb2.setChecked(true);
                        days[5] = 1;
                    }
                    break;
                case R.id.llday3:
                    CheckBox cb3 = ((CheckBox) view.findViewById(R.id.cbday3));
                    if (cb3.isChecked()) {
                        cb3.setChecked(false);
                        days[4] = 0;
                    } else {
                        cb3.setChecked(true);
                        days[4] = 1;
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
                        days[2] = 0;
                    } else {
                        cb5.setChecked(true);
                        days[2] = 1;
                    }
                    break;
                case R.id.llday6:
                    CheckBox cb6 = ((CheckBox) view.findViewById(R.id.cbday6));
                    if (cb6.isChecked()) {
                        cb6.setChecked(false);
                        days[1] = 0;
                    } else {
                        cb6.setChecked(true);
                        days[1] = 1;
                    }
                    break;
                case R.id.llday7:
                    CheckBox cb7 = ((CheckBox) view.findViewById(R.id.cbday7));
                    if (cb7.isChecked()) {
                        cb7.setChecked(false);
                        days[0] = 0;
                    } else {
                        cb7.setChecked(true);
                        days[0] = 1;
                    }
                    break;
                default:
                    break;
            }
        }

    }

}
