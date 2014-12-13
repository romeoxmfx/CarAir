
package com.android.carair.airhelper.base;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class FragmentViewBase extends FrameLayout
{
//    private View mLoadingView;
//    private View mFailedView;
//    protected ActionBar mActionBar;
//    private TextView tvNote;
//    private TextView tvGuide;
//    private Button retry;
//    private Context mContext;

//    private OnRetryListener mOnRetryListener;

    public FragmentViewBase(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setClickable(true);
//        mContext = context;
    }
    
    public FragmentViewBase(Context context){
        this(context,null);
    } 

//    public void initActionBar(String title, Action leftAction, Action rightAction,
//            OnClickListener listener)
//    {
//        mActionBar = (ActionBar) findViewById(Res.id("munion_actionbar"));
//        if (mActionBar == null) {
//            return;
//        }
//        mActionBar.setTitle(title);
//
//        if (leftAction != null)
//            mActionBar.setLeftAction(leftAction);
//        if (rightAction != null)
//        {
//            mActionBar.removeAction(rightAction);
//            mActionBar.addAction(rightAction);
//        }
//        mActionBar.setTitleBarListener(listener);
//    }

//    public void updateActionIcon(Action action, int resId)
//    {
//        if (mActionBar != null)
//        {
//            mActionBar.updateActionIcon(action, resId);
//        }
//    }

//    public void setOnRetryListener(OnRetryListener ls)
//    {
//        mOnRetryListener = ls;
//    }

//    public void startLoadingStatus(boolean... hiddenMarginTop)
//    {
//        android.widget.LinearLayout.LayoutParams marginParams = null;
//        if (mLoadingView == null)
//        {
//            mLoadingView = inflate(mContext, Res.layout("munion_loading_mask"),
//                    null);
//            // mLoadingView.setLayoutParams(new
//            // LayoutParams(LayoutParams.MATCH_PARENT,
//            // LayoutParams.MATCH_PARENT));
//            // TextView tv = (TextView)
//            // mLoadingView.findViewById(Res.id("common_mask_tips);
//            // tv.setText(getString(R.string.wht_tm_str_empty_result_tip));
//            addView(mLoadingView, new LayoutParams(LayoutParams.MATCH_PARENT,
//                    LayoutParams.MATCH_PARENT));
//        }
//        if (hiddenMarginTop.length >= 1 && hiddenMarginTop[0] == true) {
//            View view = mLoadingView.findViewById(Res.id("loading_body"));
//            marginParams = (android.widget.LinearLayout.LayoutParams)
//                    view.getLayoutParams();
//            marginParams.topMargin = 0;
//            view.setLayoutParams(marginParams);
//        }
//        if (mLoadingView.getVisibility() != View.VISIBLE)
//        {
//            mLoadingView.setVisibility(View.VISIBLE);
//        }
//    }

//    public void stopLoadingStatus()
//    {
//        if (mLoadingView != null)
//        {
//            // ViewGroup vg = (ViewGroup) (mLoadingView.getParent());
//            // vg.removeView(mLoadingView);
//            // mLoadingView = null;
//            if (mLoadingView.getVisibility() != View.GONE)
//            {
//                mLoadingView.setVisibility(View.GONE);
//            }
//        }
//    }

//    public void setFailedStatus(String note, String guide, boolean bRetry, float marginTop)
//    {
//        stopFailedStatus();
//        LinearLayout.LayoutParams marginParams = null;
//        if (mFailedView == null)
//        {
//            mFailedView = inflate(mContext, Res.layout("munion_failed_mask"), null);
//            addView(mFailedView, new LayoutParams(LayoutParams.MATCH_PARENT,
//                    LayoutParams.MATCH_PARENT));
//        }
//        // marginParams = new LinearLayout.LayoutParams(
//        // LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//        if (marginTop != 0 || marginTop != -1)
//        {
//            View view = mFailedView.findViewById(Res.id("error_layout"));
//            marginParams = (android.widget.LinearLayout.LayoutParams) view.getLayoutParams();
//            marginParams.topMargin = Math.round(marginTop);
//            view.setLayoutParams(marginParams);
//        }
//
//        if (note != null && !"".equals(note))
//        {
//            if (tvNote == null) {
//                tvNote = (TextView) mFailedView.findViewById(Res.id("common_mask_tips"));
//            }
//            tvNote.setText(note);
//        }
//
//        if (guide != null && !"".equals(guide))
//        {
//            if (tvGuide == null) {
//                tvGuide = (TextView) mFailedView.findViewById(Res.id("common_mask_guide"));
//            }
//            tvGuide.setText(guide);
//        }
//
//        // ImageView flag = (ImageView)
//        // mFailedView.findViewById(Res.id("common_mask_icon);
//        // flag.setImageResource(R.drawable.tips);
//        if (retry == null)
//        {
//            retry = (Button) mFailedView.findViewById(Res.id("retry"));
//            // retry.setOnClickListener(this);
//        }
//        if (bRetry)
//        {
//            retry.setVisibility(View.VISIBLE);
//        } else
//        {
//            retry.setVisibility(View.INVISIBLE);
//        }
//        // addContentView(mFailedView, marginParams);
//        mFailedView.setVisibility(View.VISIBLE);
//    }

//    public void stopFailedStatus()
//    {
//        if (mFailedView != null)
//        {
//            // ViewGroup vg = (ViewGroup) (mFailedView.getParent());
//            // vg.removeView(mFailedView);
//            // mFailedView = null;
//            mFailedView.setVisibility(View.GONE);
//        }
//    }

//    public void setDefaultFailedStatus()
//    {
//        setFailedStatus(getResources().getString(Res.string("munion_webview_error_common_title")),
//                getResources().getString(Res.string("munion_webview_error_common_subtitle")), true,
//                -1);
//    }

//    public void setDefaultFailedStatus(float marginTop)
//    {
//        setFailedStatus(getResources().getString(Res.string("munion_webview_error_common_title")),
//                getResources().getString(Res.string("munion_webview_error_common_subtitle")), true,
//                marginTop);
//    }

//    public interface OnRetryListener {
//        public void onRetry();
//    }
//
//    @Override
//    public void onClick(View v)
//    {
//        if (mOnRetryListener != null && Res.id("retry") == v.getId()) {
//            stopFailedStatus();
//            mOnRetryListener.onRetry();
//        }
//
//    }
}
