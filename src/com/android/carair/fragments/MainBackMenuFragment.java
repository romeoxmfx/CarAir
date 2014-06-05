
package com.android.carair.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.carair.R;
import com.android.carair.activities.CleanRatioActivity;
import com.android.carair.activities.CleanTimerActivity;
import com.android.carair.activities.base.BaseActivity;
import com.android.carair.fragments.base.FragmentPageManager;

public class MainBackMenuFragment extends ListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.carair_fragment_backmenu, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String[] str = new String[] {
                "我的净化器", "自动净化", "定时净化","意见反馈","检查更新","关于"
        };
        ArrayAdapter<String> menuAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.carair_menu_list_item, android.R.id.text1, str);
        setListAdapter(menuAdapter);
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Bundle bundle  = new Bundle();
        switch (position) {
            case 0:
                bundle.putString("text", (String)l.getAdapter().getItem(position));
                //我的净化器
                changeContent(new MainFragment(),bundle);
                break;
            case 1:
                //自动净化
                Intent intent = new Intent(getActivity(), CleanRatioActivity.class);
                getActivity().startActivity(intent);
                ((BaseActivity)getActivity()).getSlidingMenu().showContent();
//                bundle.putString("text", (String)l.getAdapter().getItem(position));
                //更换设备
//                changeContent(new HistoryFragment(),bundle);
                break;
            case 2:
                Intent i1 = new Intent(getActivity(), CleanTimerActivity.class);
                getActivity().startActivity(i1);
                ((BaseActivity)getActivity()).getSlidingMenu().showContent();
                //检查更新
//                bundle.putString("text", (String)l.getAdapter().getItem(position));
                //更换设备
//                changeContent(new MainFragment(),bundle);
                break;
            case 3:
                
                break;    
            case 4:
                //意见反馈
                break;
            case 5:
                //关于我们
                bundle.putString("text", (String)l.getAdapter().getItem(position));
                //更换设备
                changeContent(new AboutUsFragment(),bundle);
                ((BaseActivity)getActivity()).getSlidingMenu().showContent();
            default:
                break;
        }
        super.onListItemClick(l, v, position, id);
    }
    
    private void changeContent(Fragment frg,Bundle arg){
        if(getActivity() == null){
            return;
        }
        FragmentPageManager.getInstance().setFragmentManager(getActivity().getSupportFragmentManager());
        FragmentPageManager.getInstance().pushContentPage(frg,frg.getClass().getName(),arg);
        ((BaseActivity)getActivity()).getSlidingMenu().showContent();
    }
}
