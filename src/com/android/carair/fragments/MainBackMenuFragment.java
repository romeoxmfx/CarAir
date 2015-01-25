
package com.android.carair.fragments;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.content.Context;
import android.content.Intent;
import android.content.pm.FeatureInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.carair.R;
import com.android.carair.activities.AboutUsActivity;
import com.android.carair.activities.CleanRatioActivity;
import com.android.carair.activities.CleanTimerActivity;
import com.android.carair.activities.CommonWebViewActivity;
import com.android.carair.activities.MainActivity;
import com.android.carair.activities.MyDeviceActivity;
import com.android.carair.activities.WarningValueSetActivity;
import com.android.carair.activities.base.BaseActivity;
import com.android.carair.api.Activity;
import com.android.carair.api.AppInfo;
import com.android.carair.api.CarAirReqTask;
import com.android.carair.api.RespProtocolPacket;
import com.android.carair.api.Store;
import com.android.carair.fragments.base.BaseFragment;
import com.android.carair.fragments.base.FragmentPageManager;
import com.android.carair.fragments.base.FragmentViewBase;
import com.android.carair.net.HttpErrorBean;
import com.android.carair.utils.AESUtils;
import com.android.carair.utils.RequestUtil;
import com.android.carair.utils.Util;
import com.tencent.mm.sdk.modelmsg.ShowMessageFromWX;
import com.umeng.fb.FeedbackAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;

public class MainBackMenuFragment extends BaseFragment {
    ListView mlist;
    ListView mlist1;
    FeedbackAgent agent;
    ArrayAdapter<String> menuAdapter1;
    private ArrayList<String> str1s;
    private boolean hasActivity;
    private boolean hasStore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainView = (FragmentViewBase) inflater.inflate(
                R.layout.carair_fragment_backmenu, null);
        requestActivity();
        agent = new FeedbackAgent(this.getActivity());
        agent.sync();
        mlist = (ListView) mMainView.findViewById(R.id.list);
        mlist1 = (ListView) mMainView.findViewById(R.id.list1);
        // String[] str = new String[] {
        // "我的净化器", "自动净化", "定时净化"
        // };
        String[] str = new String[] {
                "我的净化器"
        };
        ArrayAdapter<String> menuAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.carair_menu_list_item, android.R.id.text1, str);
        mlist.setAdapter(menuAdapter);
        mlist.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Bundle bundle = new Bundle();
                switch (arg2) {
                    case 0:
                        // 我的净化器
                        // changeContent(new HomeFragment(), bundle);
                        // changeContent(new MyDeviceFragment(), bundle);
                        // String sec = RequestUtil.getSecret();
                        // byte[] b = RequestUtil.getSecret();
                        // String a = RequestUtil.bytesToHexString(b);
                        // AESUtils.encrypt(b, "Hello world!");
                        // AESUtils.encrypt(sec[1], "Hello world!");
                        // try {
                        // String a = RequestUtil.encrypt();
                        // } catch (Exception e) {
                        // }
                        Intent intent = new Intent(getActivity(), MyDeviceActivity.class);
                        getActivity().startActivity(intent);
//                        ((BaseActivity) getActivity()).getSlidingMenu().showContent();
                        break;
                // case 1:
                // // 自动净化
                // Intent intent = new Intent(getActivity(),
                // CleanRatioActivity.class);
                // getActivity().startActivity(intent);
                // ((BaseActivity)
                // getActivity()).getSlidingMenu().showContent();
                // break;
                // case 1:
                // // 定时净化
                // Intent i1 = new Intent(getActivity(),
                // CleanTimerActivity.class);
                // getActivity().startActivity(i1);
                // ((BaseActivity)
                // getActivity()).getSlidingMenu().showContent();
                // break;
                //
                // case 2:
                // //预警值设置
                // Intent i2 = new
                // Intent(getActivity(),WarningValueSetActivity.class);
                // getActivity().startActivity(i2);
                // ((BaseActivity)
                // getActivity()).getSlidingMenu().showContent();
                // break;
                }
            }
        });

        str1s = new ArrayList<String>();
        str1s.add("使用说明");
        str1s.add("意见反馈");
        str1s.add("检查更新");
        str1s.add("关于");
        menuAdapter1 = new MyArrayAdapter(getActivity(),
                R.layout.carair_menu_list_item, android.R.id.text1, str1s);
        mlist1.setAdapter(menuAdapter1);
        mlist1.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Bundle bundle = new Bundle();
                switch (arg2) {
                    case 0:
                        // 新手提示
                        newUserActivity();
                        break;
                    case 1:
                        // 意见反馈
                        agent.startFeedbackActivity();
//                        ((BaseActivity) getActivity()).getSlidingMenu().showContent();
                        break;
                    case 2:
                        // 检查更新

                        UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {

                            // @Override
                            // public void onUpdateReturned(int updateStatus,
                            // UpdateResponse updateInfo) {
                            // if (updateStatus == 0 && updateInfo != null) {
                            // showUpdateDialog(updateInfo.path,
                            // updateInfo.updateLog);
                            // }
                            // // case 0: // has update
                            // // case 1: // has no update
                            // // case 2: // none wifi
                            // // case 3: // time out
                            // }

                            @Override
                            public void onUpdateReturned(int paramInt,
                                    UpdateResponse paramUpdateResponse) {
                                // case 0: // has update
                                // case 1: // has no update
                                // case 2: // none wifi
                                // case 3: // time out
                                if (getActivity() == null) {
                                    return;
                                }
                                if (2 == paramInt) {
                                    Toast.makeText(getActivity(), "没有网络，检测失败", Toast.LENGTH_SHORT)
                                            .show();
                                } else if (3 == paramInt) {
                                    Toast.makeText(getActivity(), "超时，检测失败", Toast.LENGTH_SHORT)
                                            .show();
                                } else if (1 == paramInt) {
                                    Toast.makeText(getActivity(), "没有最新版本", Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }
                        });

                        // UmengUpdateAgent.update(getActivity());
                        UmengUpdateAgent.forceUpdate(getActivity());
                        break;
                    case 3:
                        // 关于我们
                        // 更换设备
                        // changeContent(new AboutUsFragment(), bundle);
                        Intent intent = new Intent(getActivity(), AboutUsActivity.class);
                        getActivity().startActivity(intent);
//                        ((BaseActivity) getActivity()).getSlidingMenu().showContent();

                        // byte[] sec = RequestUtil.getSecret();
                        // String a = AESUtils.encrypt(sec, "Hello world!");
                        // Log.i("car", a);
                        break;
                    case 4:
                        //商城
                        if(hasActivity && !hasStore){
                            clickActivity();
                        }else{
                            clickStore();
                        }
                        break;
                    case 5:
                        // 活动页
                        clickActivity();
                        break;
                }

            }
        });
        return mMainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    
    private void newUserActivity(){
        Intent intent = new Intent();
        intent.setClass(getActivity(), CommonWebViewActivity.class);
        String url = "";
        AppInfo info = Util.getFeature(this.getActivity());
        if(info != null){
            url = info.getUsage_url();
        }
        intent.putExtra("url", url);
        intent.putExtra("title", "使用说明");
        getActivity().startActivity(intent);
//        ((BaseActivity) getActivity()).getSlidingMenu().showContent();
    }

    class MyArrayAdapter extends ArrayAdapter {
        Context myContext;
        int resource;
        int textResource;
        Object[] obj;
        List<String>list;

        public MyArrayAdapter(Context context, int resource, int textViewResourceId,
                Object[] objects) {
            super(context, resource, textViewResourceId, objects);
            this.myContext = context;
            this.resource = resource;
            this.textResource = textViewResourceId;
            this.obj = objects;
        }
        
        public MyArrayAdapter(Context context, int resource, int textViewResourceId,
                List<String> objects) {
            super(context, resource, textViewResourceId, objects);
            this.myContext = context;
            this.resource = resource;
            this.textResource = textViewResourceId;
            this.list = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(myContext).inflate(resource, null);
            TextView tv = (TextView) view.findViewById(android.R.id.text1);
            if (obj != null && obj.length > 0) {
                String text = (String) obj[position];
                tv.setText(text);
            }else{
                if(list != null && list.size() > 0){
                    String text = list.get(position);
                    tv.setText(text);
                }
            }
            ImageView iv = (ImageView) view.findViewById(R.id.ivNewTag);
            Activity activity = Util.getActivity(myContext);
            boolean isNewActivity = false;
            if (activity != null && !TextUtils.isEmpty(activity.getIs_new())
                    && "1".equals(activity.getIs_new())) {
                isNewActivity = true;
            }
            Store store = Util.getStore(myContext);
            boolean isNewStore = false;
            if(store != null && !TextUtils.isEmpty(store.getIs_new())
                    && "1".equals(store.getIs_new())) {
                isNewStore = true;
            }
            if (position == 4 && isNewStore) {
                iv.setVisibility(View.VISIBLE);
            }else if(position == 5 && isNewActivity){
                iv.setVisibility(View.VISIBLE);
            }else {
                iv.setVisibility(View.INVISIBLE);
            }
            return view;
            // return super.getView(position, convertView, parent);
        }
    }

    private void changeContent(Fragment frg, Bundle arg) {
        if (getActivity() == null) {
            return;
        }
        FragmentPageManager.getInstance().setFragmentManager(
                getActivity().getSupportFragmentManager());
        FragmentPageManager.getInstance().pushContentPage(frg, frg.getClass().getName(), arg);
//        ((BaseActivity) getActivity()).getSlidingMenu().showContent();
    }
    
    private void clickStore(){
        if (getActivity() == null) {
            return;
        }
        final Store activity = Util.getStore(getActivity());
        if (TextUtils.isEmpty(activity.getId())) {
            Toast.makeText(getActivity(), "没有新推荐", 1).show();
            return;
        }
        
        new CarAirReqTask() {

            @Override
            public void onCompleteSucceed(RespProtocolPacket packet) {
                try {
                    Log.i("carair", "status = click store==========" + packet.getStatus());
                    if (packet != null && packet.getStatus() == "0") {
                        Store activity = Util.getStore(getActivity());
                        activity.setIs_new("0");
                        Util.saveStore(activity, getActivity());
                        menuAdapter1.notifyDataSetChanged();
                        int badge = Util.getBadge(getActivity());
                        int newBadge = badge -1;
                        if(newBadge <= 0){
                            newBadge = 0;
                            Util.saveBadge(newBadge, getActivity());
                            ((MainActivity) getActivity()).refreshNoticeUI(false);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCompleteFailed(int type, HttpErrorBean error) {
                Log.i("carair", "failed in store click");
            }
        }.activityinfoClick(getActivity(),activity.getId(),activity.getType());
        
        Intent intent = new Intent();
        intent.setClass(getActivity(), CommonWebViewActivity.class);
        intent.putExtra("url", activity.getUrl());
        intent.putExtra("title", activity.getTitle());
        getActivity().startActivity(intent);
//        ((BaseActivity) getActivity()).getSlidingMenu().showContent();
    }

    private void clickActivity() {
        if (getActivity() == null) {
            return;
        }
        final Activity activity = Util.getActivity(getActivity());
        if (TextUtils.isEmpty(activity.getId())) {
            Toast.makeText(getActivity(), "没有新活动", 1).show();
            return;
        }
        
        new CarAirReqTask() {

            @Override
            public void onCompleteSucceed(RespProtocolPacket packet) {
                try {
                    if (packet != null && packet.getStatus() == "0") {
                        Activity activity = Util.getActivity(getActivity());
                        activity.setIs_new("0");
                        Util.saveActivity(activity, getActivity());
                        menuAdapter1.notifyDataSetChanged();
                        int badge = Util.getBadge(getActivity());
                        int newBadge = badge -1;
                        if(newBadge <= 0){
                            newBadge = 0;
                            Util.saveBadge(newBadge, getActivity());
                            ((MainActivity) getActivity()).refreshNoticeUI(false);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCompleteFailed(int type, HttpErrorBean error) {

            }
        }.activityinfoClick(getActivity(), activity.getId(),activity.getType());
        
        Intent intent = new Intent();
        intent.setClass(getActivity(), CommonWebViewActivity.class);
        intent.putExtra("url", activity.getUrl());
        intent.putExtra("title", activity.getTitle());
        getActivity().startActivity(intent);
//        ((BaseActivity) getActivity()).getSlidingMenu().showContent();
    }

    private void requestActivity() {
        if (getActivity() == null) {
            return;
        }
        new CarAirReqTask() {

            @Override
            public void onCompleteSucceed(RespProtocolPacket packet) {
                if (packet != null && packet.getRespMessage() != null) {
                    Util.saveBadge(packet.getRespMessage().getBadge(), getActivity());
                    if (packet.getRespMessage().getBadge() > 0) {
                        ((MainActivity) getActivity()).refreshNoticeUI(true);
                    } else {
                        ((MainActivity) getActivity()).refreshNoticeUI(false);
                    }
                    Activity activity = packet.getRespMessage().getActivity();
                    Store store = packet.getRespMessage().getStore();
                    if (activity != null) {
                        Util.saveActivity(activity, getActivity());
                        hasActivity = true;
                        str1s.add("精品推荐");
                    }else{
                        hasActivity = false;
                    }
                    if(store != null){
                        Util.saveStore(store, getActivity());
                        hasStore = true;
                        str1s.add("活动推荐");
                    }else{
                        hasStore = false;
                    }
                    menuAdapter1.notifyDataSetChanged();
                }
            }

            @Override
            public void onCompleteFailed(int type, HttpErrorBean error) {

            }
        }.activityinfo(getActivity());
    }

}
