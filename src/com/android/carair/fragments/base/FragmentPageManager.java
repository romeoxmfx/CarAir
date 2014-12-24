
package com.android.carair.fragments.base;

import com.android.carair.fragments.base.BaseFragment.OnFragmentFinishListener;
import com.android.carair.utils.BeanWrapper;
import com.android.goodhelpercarair.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

public class FragmentPageManager
{

    private FragmentManager mManager;
    private static FragmentPageManager sPageManager;

    public static FragmentPageManager getInstance()
    {
        if (null == sPageManager)
        {
            sPageManager = new FragmentPageManager();
        }

        return sPageManager;
    }

    private FragmentPageManager()
    {

    }

    public void setFragmentManager(FragmentManager man)
    {
        mManager = man;
    }

    public boolean canGoBack() {
        if (mManager.getBackStackEntryCount() == 1) {
            return false;
        }
        return true;
    }

    public boolean canGoBackWithActivity() {
        if (mManager.getBackStackEntryCount() == 0) {
            return false;
        }
        return true;
    }

    public void popToBack()
    {
        mManager.popBackStack();
    }

    public void gotoPage(String tag)
    {
        boolean bFind = false;
        int nCount = mManager.getBackStackEntryCount();
        for (int i = nCount - 1; i >= 0; i--)
        {
            String name = mManager.getBackStackEntryAt(i).getName();
            if (name.endsWith(tag))
            {
                bFind = true;
                break;
            }
        }

        if (bFind)
        {
            popToPage(tag);
        }
        else
        {
            popToBack();
            Fragment newFrg = (Fragment) BeanWrapper.findObject(tag);
            pushPage(newFrg, tag);
        }
    }

    public void gotoPage(String tag, Bundle bundle)
    {
        if (TextUtils.isEmpty(tag))
        {
            return;
        }
        boolean bFind = false;
        int nCount = mManager.getBackStackEntryCount();
        for (int i = nCount - 1; i >= 0; i--)
        {
            String name = mManager.getBackStackEntryAt(i).getName();
            if (name.endsWith(tag))
            {
                bFind = true;
                break;
            }
        }

        if (bFind)
        {
            mManager.popBackStackImmediate(tag, 0);
            // nCount = mManager.getBackStackEntryCount();
            // if (nCount > 0)
            // {
            // String name = mManager.getBackStackEntryAt(nCount - 1).getName();
            // Fragment top = mManager.findFragmentByTag(name);
            // ((EWallBaseFragment) top).onFragmentDataReset(bundle);
            // }
        } else
        {
            popToBack();
            Fragment newFrg = (Fragment) BeanWrapper.findObject(tag);
            pushPage(newFrg, tag, bundle);
        }
    }

    public void pushPageById(Fragment frg, String tag, int id, boolean addBack,Bundle... args) {
        FragmentTransaction ft = mManager.beginTransaction();
        // ft.setCustomAnimations(R.anim.munion_push_in_right,
        // R.anim.munion_push_out_left,
        // R.anim.munion_push_in_left, R.anim.munion_push_out_right);
        if (args != null && args.length > 0) {
            frg.setArguments(args[0]);
        }
        ft.replace(id, frg, tag);
        if (addBack) {
            ft.addToBackStack(tag);
        }
        ft.commit();
    }

    public void pushPageByIdWithAnimation(Fragment frg, String tag, int id, Bundle... args) {
        FragmentTransaction ft = mManager.beginTransaction();
        ft.setCustomAnimations(R.anim.munion_push_in_right, R.anim.munion_push_out_left,
                R.anim.munion_push_in_left, R.anim.munion_push_out_right);
        if (args != null && args.length > 0) {
            frg.setArguments(args[0]);
        }
        ft.replace(id, frg, tag);
        ft.addToBackStack(tag);
        ft.commit();
    }

    public void pushContentPage(Fragment frg, String tag, Bundle... args) {
        FragmentTransaction ft = mManager.beginTransaction();
        if (args != null && args.length > 0) {
            frg.setArguments(args[0]);
        }
        ft.replace(R.id.fragment_container, frg, tag);
        ft.commit();
    }

    private void pushPage(Fragment frg, String tag)
    {
        FragmentTransaction ft = mManager.beginTransaction();
        ft.setCustomAnimations(R.anim.munion_push_in_right, R.anim.munion_push_out_left,
                R.anim.munion_push_in_left, R.anim.munion_push_out_right);
        // ft.setCustomAnimations(android.R.anim.slide_in_left,
        // android.R.anim.slide_out_right);
        ft.add(R.id.fragment_container, frg, tag);
        ft.addToBackStack(tag);
        ft.commit();
    }

    public void pushPage(String mFragmentName)
    {
        try
        {
            Fragment frg = (Fragment) Class.forName(mFragmentName).newInstance();
            pushPage(frg, mFragmentName);
        } catch (Exception e)
        {
        }
    }

    public void pushPageNotAddToBackStack(String mFragmentName)
    {
        try
        {
            Fragment frg = (Fragment) Class.forName(mFragmentName).newInstance();
            FragmentTransaction ft = mManager.beginTransaction();
            ft.add(R.id.fragment_container, frg, mFragmentName);
            ft.commit();
        } catch (Exception e)
        {
        }

    }

    public void pushPageNoAnimation(String mFragmentName) {
        try
        {
            Fragment frg = (Fragment) Class.forName(mFragmentName).newInstance();
            FragmentTransaction ft = mManager.beginTransaction();
            ft.add(R.id.fragment_container, frg, mFragmentName);
            ft.addToBackStack(mFragmentName);
            ft.commit();
        } catch (Exception e)
        {
        }
    }

    public void pushPageObject(Fragment frg)
    {
        try
        {
            FragmentTransaction ft = mManager.beginTransaction();
            ft.add(R.id.fragment_container, frg, frg.getClass().getName());
            ft.addToBackStack(frg.getClass().getName());
            ft.commit();
        } catch (Exception e)
        {
        }
    }

    private void pushPage(Fragment frg, String tag, Bundle bundle)
    {
        frg.setArguments(bundle);
        pushPage(frg, tag);
    }

    public void pushPage(String mFragmentName, Bundle bundle)
    {
        // for(MappingInfo temp:mMappingList){
        // if(temp.getName().equals(mFragmentName)){
        // mFragmentName = temp.getClassName();
        // break;
        // }
        // }

        Fragment frg = null;
        try
        {
            frg = (Fragment) Class.forName(mFragmentName).newInstance();
        } catch (Exception e)
        {
            return;
        }

        pushPage(frg, mFragmentName, bundle);
    }

    public void pushPageWithAnimation(String mFragmentName, Bundle bundle, int enter, int exit,
            int popEnter,
            int popExit)
    {
        Fragment frg = null;
        try
        {
            frg = (Fragment) Class.forName(mFragmentName).newInstance();
        } catch (Exception e)
        {
            return;
        }

        if (null != bundle)
            frg.setArguments(bundle);

        FragmentTransaction ft = mManager.beginTransaction();
        ft.setCustomAnimations(enter, exit, popEnter, popExit);
        ft.add(R.id.fragment_container, frg, mFragmentName);
        ft.addToBackStack(mFragmentName);
        ft.commit();

    }

    public BaseFragment pushPageForResult(String tag, int code, OnFragmentFinishListener listener)
    {
        BaseFragment frg = (BaseFragment) BeanWrapper.findObject(tag);
        if (null == frg)
            return null;
        frg.setFragmentFinishListener(listener);
        frg.setRequireCode(code);
        FragmentTransaction ft = mManager.beginTransaction();
        ft.setCustomAnimations(R.anim.munion_push_in_right, R.anim.munion_push_out_left,
                R.anim.munion_push_in_left, R.anim.munion_push_out_right);
        ft.add(R.id.fragment_container, frg, tag);
        ft.addToBackStack(tag);
        ft.commit();

        return frg;
    }

    public BaseFragment pushPageForResult(String tag, int code, OnFragmentFinishListener listener,
            Bundle args)
    {
        BaseFragment frg = (BaseFragment) BeanWrapper.findObject(tag);
        if (null == frg)
            return null;
        frg.setFragmentFinishListener(listener);
        frg.setArguments(args);
        frg.setRequireCode(code);
        FragmentTransaction ft = mManager.beginTransaction();
        ft.setCustomAnimations(R.anim.munion_push_in_right, R.anim.munion_push_out_left,
                R.anim.munion_push_in_left, R.anim.munion_push_out_right);
        ft.add(R.id.fragment_container, frg, tag);
        ft.addToBackStack(tag);
        ft.commit();

        return frg;
    }

    public BaseFragment pushPageForResult(String tag, int code)
    {
        BaseFragment frg = (BaseFragment) BeanWrapper.findObject(tag);
        if (null == frg)
            return null;

        frg.setRequireCode(code);
        FragmentTransaction ft = mManager.beginTransaction();
        ft.add(R.id.fragment_container, frg, tag);
        ft.addToBackStack(tag);
        ft.commit();

        return frg;
    }

    public BaseFragment pushPageForResult(String tag, Bundle bundle, int code)
    {
        BaseFragment frg = (BaseFragment) BeanWrapper.findObject(tag);
        if (null == frg)
            return null;

        frg.setArguments(bundle);

        frg.setRequireCode(code);
        FragmentTransaction ft = mManager.beginTransaction();
        ft.add(R.id.fragment_container, frg, tag);
        ft.addToBackStack(tag);
        ft.commit();

        return frg;
    }

    public BaseFragment pushPageForResultWithAnimation(String tag, Bundle bundle, int code,
            int enter, int exit,
            int popEnter, int popExit)
    {

        BaseFragment frg = (BaseFragment) BeanWrapper.findObject(tag);
        if (null == frg)
            return null;

        if (null != bundle)
            frg.setArguments(bundle);
        frg.setRequireCode(code);

        FragmentTransaction ft = mManager.beginTransaction();
        ft.setCustomAnimations(enter, exit, popEnter, popExit);
        ft.add(R.id.fragment_container, frg, tag);
        ft.addToBackStack(tag);
        ft.commit();
        return frg;
    }

    public void popToPage(String name)
    {
        mManager.popBackStack(name, 0);
    }
}
