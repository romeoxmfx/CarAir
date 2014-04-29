
package com.android.carair.net;

import android.support.v4.app.Fragment;

public abstract class FragmentAsyncHttpHelper extends AsyncHttpHelper
{
    private Fragment mFragment;

    public FragmentAsyncHttpHelper(Fragment mFragment) {
        this.mFragment = mFragment;
    }

    @Override
    protected void onHttpSucceed(int type, BizResponse response) {
        if (mFragment.isAdded()) {
            doHttpsuccsed(type, response);
        }
    }

    public abstract void doHttpsuccsed(int type, BizResponse response);

    public abstract void doHttpFailed(int type, HttpErrorBean error);

    @Override
    protected void onHttpFailed(int type, HttpErrorBean error) {
        if (mFragment.isAdded()) {
            doHttpFailed(type, error);
        }
    }

}
