package com.android.carair.net;


import android.os.AsyncTask;


public abstract class AsyncHttpHelper
{
	public static final int DEFAULT_VIEW = -1;

	public static final int DEFAULT_HTTP_ERROR = -1;
	public static final int VALID_ERROR = -50;

	private LoadViewContentTask mTaskHandler;
	private BizResponse mHttpResponse;

	public static final long TIME_HOURSE = 3600;
	public static final long TIME_DAY = TIME_HOURSE * 24;

	// Override for handle http result.
	protected abstract void onHttpSucceed(int type, BizResponse response);

	// Override for handle http failed status.
	protected abstract void onHttpFailed(int type, HttpErrorBean error);

	// Override for handle http loading status.
	protected void onHttpLoading(int viewType)
	{
		// ProgressDialog dialog = new ProgressDialog(context);
		// // dialog.setTitle("Indeterminate");
		// dialog.setMessage(msg);
		// dialog.setIndeterminate(true);
		// dialog.setCancelable(true);
		// dialog.show();
	}

	protected boolean onHttpResult(int type, BizResponse response)
	{
		return false;
	}

	// Override for back to previous view
	protected void onResumePreView(int viewType)
	{

	}

	public void loadHttpContent(BizRequest request)
	{
		mTaskHandler = (LoadViewContentTask) new LoadViewContentTask(DEFAULT_VIEW).execute(request);
	}
	

	public void loadHttpContent(BizRequest request, int viewType)
	{
		mTaskHandler = (LoadViewContentTask) new LoadViewContentTask(viewType).execute(request);
	}

	public boolean isRunning()
	{
		if (mTaskHandler != null)
		{
			return mTaskHandler.getStatus() == AsyncTask.Status.RUNNING || mTaskHandler.getStatus() == AsyncTask.Status.PENDING;
		}
		return false;
	}

	public void breakHttpTask()
	{
		if (mTaskHandler != null && isRunning())
		{
			mTaskHandler.cancel(true);
		}
		mTaskHandler = null;
	}

	class LoadViewContentTask extends AsyncTask<BizRequest, Void, BizResponse>
	{
		protected int mViewType = DEFAULT_VIEW;

		public LoadViewContentTask(int viewType)
		{
			mViewType = viewType;
		}

		@Override
		protected void onPreExecute()
		{
			onHttpLoading(mViewType);
		}

		@Override
		protected BizResponse doInBackground(BizRequest... args)
		{

			BizRequest request = args[0];
			return request.sendRequest();
		}

		@Override
		protected void onPostExecute(BizResponse response)
		{
			if (isCancelled()) return;
			if (onHttpResult(mViewType, response)) return;
			if (response == null)
			{
				onHttpFailed(mViewType, new HttpErrorBean(null));
			}
			else if (response.isSuccess())
			{
				onHttpSucceed(mViewType, response);
			}
			else
			{
				onHttpFailed(mViewType, new HttpErrorBean(response.getRawResponse()));
			}
		}

		@Override
		protected void onCancelled()
		{
			onResumePreView(mViewType);
			super.onCancelled();
		}
	}
	

}
