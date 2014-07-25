
package com.android.carair.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.carair.R;
import com.android.carair.activities.AboutUsActivity;
import com.android.carair.activities.CleanRatioActivity;
import com.android.carair.activities.CleanTimerActivity;
import com.android.carair.activities.WarningValueSetActivity;
import com.android.carair.activities.base.BaseActivity;
import com.android.carair.fragments.base.BaseFragment;
import com.android.carair.fragments.base.FragmentPageManager;
import com.android.carair.fragments.base.FragmentViewBase;
import com.android.carair.utils.AESUtils;
import com.android.carair.utils.RequestUtil;
import com.umeng.fb.FeedbackAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;

public class MainBackMenuFragment extends BaseFragment {
    ListView mlist;
    ListView mlist1;
    FeedbackAgent agent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainView = (FragmentViewBase) inflater.inflate(
                R.layout.carair_fragment_backmenu, null);
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
//                         changeContent(new HomeFragment(), bundle);
                         changeContent(new MyDeviceFragment(), bundle);
//                        String sec = RequestUtil.getSecret();
//                        byte[] b = RequestUtil.getSecret();
//                        String a = RequestUtil.bytesToHexString(b);
//                        AESUtils.encrypt(b, "Hello world!");
//                        AESUtils.encrypt(sec[1], "Hello world!");
                        // try {
                        // String a = RequestUtil.encrypt();
                        // } catch (Exception e) {
                        // }
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

        String[] str1 = new String[] {
                "意见反馈", "检查更新", "关于"
        };
        ArrayAdapter<String> menuAdapter1 = new ArrayAdapter<String>(getActivity(),
                R.layout.carair_menu_list_item, android.R.id.text1, str1);
        mlist1.setAdapter(menuAdapter1);
        mlist1.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Bundle bundle = new Bundle();
                switch (arg2) {
                    case 0:
                        // 意见反馈
                        agent.startFeedbackActivity();
                        ((BaseActivity) getActivity()).getSlidingMenu().showContent();
                        break;
                    case 1:
                        // 检查更新
                        
                        UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {

//                            @Override
//                            public void onUpdateReturned(int updateStatus,
//                                    UpdateResponse updateInfo) {
//                                if (updateStatus == 0 && updateInfo != null) {
//                                    showUpdateDialog(updateInfo.path, updateInfo.updateLog);
//                                }
//                                // case 0: // has update
//                                // case 1: // has no update
//                                // case 2: // none wifi
//                                // case 3: // time out
//                            }

                            @Override
                            public void onUpdateReturned(int paramInt,
                                    UpdateResponse paramUpdateResponse) {
                             // case 0: // has update
                                // case 1: // has no update
                                // case 2: // none wifi
                                // case 3: // time out
                                if(getActivity() == null){
                                    return;
                                }
                                if(2 == paramInt){
                                    Toast.makeText(getActivity(), "没有网络，检测失败", Toast.LENGTH_SHORT).show();
                                }else if(3 == paramInt){
                                    Toast.makeText(getActivity(), "超时，检测失败", Toast.LENGTH_SHORT).show();
                                }else if(1 == paramInt){
                                    Toast.makeText(getActivity(), "没有最新版本", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

//                        UmengUpdateAgent.update(getActivity());
                        UmengUpdateAgent.forceUpdate(getActivity());
                        break;
                    case 2:
                        // 关于我们
                        // 更换设备
//                        changeContent(new AboutUsFragment(), bundle);
                        Intent intent = new Intent(getActivity(),AboutUsActivity.class);
                        getActivity().startActivity(intent);
                        ((BaseActivity) getActivity()).getSlidingMenu().showContent();
                        
//                      byte[] sec = RequestUtil.getSecret();
//                      String a = AESUtils.encrypt(sec, "Hello world!");
//                      Log.i("car", a);
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

    private void changeContent(Fragment frg, Bundle arg) {
        if (getActivity() == null) {
            return;
        }
        FragmentPageManager.getInstance().setFragmentManager(
                getActivity().getSupportFragmentManager());
        FragmentPageManager.getInstance().pushContentPage(frg, frg.getClass().getName(), arg);
        ((BaseActivity) getActivity()).getSlidingMenu().showContent();
    }

}
