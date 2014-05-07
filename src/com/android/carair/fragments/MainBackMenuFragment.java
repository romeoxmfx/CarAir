
package com.android.carair.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.carair.R;
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
                "更换设备", "意见反馈", "检查更新", "关于我们"
        };
        ArrayAdapter<String> menuAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, str);
        setListAdapter(menuAdapter);
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Bundle bundle  = new Bundle();
        switch (position) {
            case 0:
                bundle.putString("text", (String)l.getAdapter().getItem(position));
                //更换设备
                changeContent(new MainFragment(),bundle);
                break;
            case 1:
                //意见反馈
                bundle.putString("text", (String)l.getAdapter().getItem(position));
                //更换设备
                changeContent(new MainFragment(),bundle);
                break;
            case 2:
                //检查更新
                bundle.putString("text", (String)l.getAdapter().getItem(position));
                //更换设备
                changeContent(new MainFragment(),bundle);
                break;
            case 3:
                //关于我们
                bundle.putString("text", (String)l.getAdapter().getItem(position));
                //更换设备
                changeContent(new MainFragment(),bundle);
                break;    
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
