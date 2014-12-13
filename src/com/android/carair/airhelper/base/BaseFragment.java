
package com.android.carair.airhelper.base;

import com.android.carair.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class BaseFragment extends Fragment implements OnClickListener
{
    /** Standard activity result: operation succeeded. */
    public static final int RESULT_OK = -1;
    /** Standard activity result: operation canceled. */
    public static final int RESULT_CANCELED = 0;
    private int mRequireCode;
    private OnFragmentFinishListener mOnFragmentFinishListener;
    protected FragmentViewBase mMainView;
//    protected ActorBinder mActorBinder;
    private View mLoadingView;
    private View mFailedView;
    private TextView tvNote;
    private TextView tvGuide;
    private Button retry;
    private LayoutInflater inflater;
//    private ImageFetcher mImageFetcher;
    private Intent mResultBundle;
    private int mResultCode = 0;

    // FragmentActivityCallBackListener getActivity()CallBackListener;

    // @Override
    // public void onAttach(Activity activity) {
    // super.onAttach(activity);
    // try {
    // getActivity()CallBackListener = (FragmentActivityCallBackListener) activity;
    // } catch (ClassCastException e) {
    // e.printStackTrace();
    // // throw new ClassCastException(activity.toString() +
    // " must implement OnArticleSelectedListener");
    // }
    // }

    /**
     * 获取图片缓存imageFetcher接口
     * 
     * @return imageFetcher
     */
//    public ImageFetcher getImageFetcher() {
//        if (mImageFetcher == null) {
//            mImageFetcher = new ImageFetcher(getActivity(), getResources().getDimensionPixelSize(
//                    Res.dimen("munion_default_image_size")));
//            ImageCacheParams cacheParams = new ImageCacheParams(getActivity(), "thumbs");
//            cacheParams.setMemCacheSizePercent(MunionConstants.imageCacheSize); // Set memory cache to
//                                                       // 25% of
//            // app memory
//            mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
//        }
//        return mImageFetcher;
//    }
    
    /**
     * 设置imageFetcherSize 图片尺寸大小,不调用此函数默认大小100dp
     * @param int 图片尺寸
     */
//    public void initAndsetImageFetcherSize(int size){
//        mImageFetcher = new ImageFetcher(getActivity(), size);
//        ImageCacheParams cacheParams = new ImageCacheParams(getActivity(), "thumbs");
//        cacheParams.setMemCacheSizePercent(MunionConstants.imageCacheSize); // Set memory cache to
//                                                   // 25% of
//        // app memory
//        mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
//    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
//        mActorBinder = createActorBinderDelegate();
        inflater = getActivity().getLayoutInflater();
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (mImageFetcher != null) {
//            mImageFetcher.setExitTasksEarly(false);
//        }
    }

    @Override
    public void onPause() {
        super.onPause();
//        if (mImageFetcher != null) {
//            mImageFetcher.setPauseWork(false);
//            mImageFetcher.setExitTasksEarly(true);
//            mImageFetcher.flushCache();
//        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
//        if (mActorBinder != null)
//        {
//            mActorBinder.destroy();
//        }

        if (mOnFragmentFinishListener != null) {
            if (getRequireCode() == -1) {
                return;
            }
            mOnFragmentFinishListener
                    .onFragmentResult(getRequireCode(), mResultCode, mResultBundle);
        }
    }

//    protected ActorBinder createActorBinderDelegate()
//    {
//        return null;
//    };

    public void setRequireCode(int code)
    {
        mRequireCode = code;
    }

    public int getRequireCode()
    {
        return mRequireCode;
    }

    public void setFragmentFinishListener(OnFragmentFinishListener listener)
    {
        mOnFragmentFinishListener = listener;
    }

    public OnFragmentFinishListener getFragmentFinishListener()
    {
        return mOnFragmentFinishListener;
    }

    public interface OnFragmentFinishListener
    {
        public void onFragmentResult(int requestCode, int resultCode, Intent data);
    }

    @Override
    public void onClick(View v) {
        if (R.id.retry == v.getId()) {
            onRetry();
        }
    }

    public void setResult(int resultCode, Intent bundle) {
        this.mResultBundle = bundle;
        this.mResultCode = resultCode;
    }

    public void setResult(int resultCode) {
        this.mResultCode = resultCode;
        mResultBundle = null;
    }

    protected void onRetry() {
        stopFailedStatus();
    }

    public void startLoadingStatus(boolean... hiddenMarginTop) {
        // if(mMainView != null){
        // mMainView.startLoadingStatus(hiddenMarginTop);
        // }

        android.widget.LinearLayout.LayoutParams marginParams = null;
        if (mLoadingView == null)
        {
            mLoadingView = inflater.inflate(R.layout.carair_loading_mask,
                    null);
            // mLoadingView.setLayoutParams(new
            // LayoutParams(LayoutParams.MATCH_PARENT,
            // LayoutParams.MATCH_PARENT));
            // TextView tv = (TextView)
            // mLoadingView.findViewById(Res.id("common_mask_tips);
            // tv.setText(getString(R.string.wht_tm_str_empty_result_tip));
            mMainView.addView(mLoadingView, new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));
        }
        if (hiddenMarginTop.length >= 1 && hiddenMarginTop[0] == true) {
            View view = mLoadingView.findViewById(R.id.loading_body);
            marginParams = (android.widget.LinearLayout.LayoutParams)
                    view.getLayoutParams();
            marginParams.topMargin = 0;
            view.setLayoutParams(marginParams);
        }
        if (mLoadingView.getVisibility() != View.VISIBLE)
        {
            mLoadingView.setVisibility(View.VISIBLE);
        }
    }

    public void stopLoadingStatus() {
        // if(mMainView != null){
        // mMainView.stopLoadingStatus();
        // }
        if (mLoadingView != null)
        {
            // ViewGroup vg = (ViewGroup) (mLoadingView.getParent());
            // vg.removeView(mLoadingView);
            // mLoadingView = null;
            if (mLoadingView.getVisibility() != View.GONE)
            {
                mLoadingView.setVisibility(View.GONE);
            }
        }
    }

    public void setFailedStatus(String note, String guide, boolean bRetry, float marginTop) {
        // if(mMainView != null){
        // mMainView.setFailedStatus(note,guide,bRetry,marginTop);
        // }
        stopFailedStatus();
        LinearLayout.LayoutParams marginParams = null;
        if (mFailedView == null)
        {
            mFailedView = inflater.inflate(R.layout.carair_failed_mask, null);
            mMainView.addView(mFailedView, new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));
        }
        // marginParams = new LinearLayout.LayoutParams(
        // LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        if (marginTop != 0 || marginTop != -1)
        {
            View view = mFailedView.findViewById(R.id.error_layout);
            marginParams = (android.widget.LinearLayout.LayoutParams) view.getLayoutParams();
            marginParams.topMargin = Math.round(marginTop);
            view.setLayoutParams(marginParams);
        }

        if (note != null && !"".equals(note))
        {
            if (tvNote == null) {
                tvNote = (TextView) mFailedView.findViewById(R.id.common_mask_tips);
            }
            tvNote.setText(note);
        }

        if (guide != null && !"".equals(guide))
        {
            if (tvGuide == null) {
                tvGuide = (TextView) mFailedView.findViewById(R.id.common_mask_guide);
            }
            tvGuide.setText(guide);
        }

        // ImageView flag = (ImageView)
        // mFailedView.findViewById(Res.id("common_mask_icon);
        // flag.setImageResource(R.drawable.tips);
        if (retry == null)
        {
            retry = (Button) mFailedView.findViewById(R.id.retry);
            retry.setOnClickListener(this);
        }
        if (bRetry)
        {
            retry.setVisibility(View.VISIBLE);
        } else
        {
            retry.setVisibility(View.INVISIBLE);
        }
        // addContentView(mFailedView, marginParams);
        mFailedView.setVisibility(View.VISIBLE);
    }

    public void stopFailedStatus() {
        // if(mMainView != null){
        // mMainView.stopFailedStatus();
        // }
        if (mFailedView != null)
        {
            // ViewGroup vg = (ViewGroup) (mFailedView.getParent());
            // vg.removeView(mFailedView);
            // mFailedView = null;
            mFailedView.setVisibility(View.GONE);
        }
    }

    public void setDefaultFailedStatus() {
        // if(mMainView != null){
        // mMainView.setDefaultFailedStatus();
        // }
        setFailedStatus(getResources().getString(R.string.munion_webview_error_common_title),
                getResources().getString(R.string.munion_webview_error_common_subtitle), true,
                -1);
    }

    public void setDefaultFailedStatus(float marginTop) {
        // if(mMainView != null){
        // mMainView.setDefaultFailedStatus(marginTop);
        // }
        setFailedStatus(getResources().getString(R.string.munion_webview_error_common_title),
                getResources().getString(R.string.munion_webview_error_common_subtitle), true,
                marginTop);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    /**
     * fragment与fragmentActivity回调 listener
     */
    public interface FragmentActivityCallBackListener {
        public void callBack();
    }
}
