package com.android.carair.net;

import com.taobao.munion.common.MunionConfigManager;
import com.taobao.munion.utils.NetWork;
import com.umeng.newxp.common.persistence.PersistentCookieStore;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
//import java.net.CookieHandler;
//import java.net.CookieManager;
//import java.net.CookiePolicy;
//import java.net.HttpURLConnection;
//import javax.net.ssl.HostnameVerifier;
//import javax.net.ssl.HttpsURLConnection;
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.SSLHandshakeException;
//import javax.net.ssl.SSLSession;
//import javax.net.ssl.SSLSocketFactory;
//import javax.net.ssl.TrustManager;
//import javax.net.ssl.X509TrustManager;

/**
 * ApiConnector 包装了向Server发起一次API调用的逻辑
 */
public class ApiConnector
{

	// public static int m_readTimeout =5000;
	// public static int m_connectTimeout = 5000;

	// 链接情况的统计
	class ConnStat
	{
		long m_times = 0; // 一共成功连接的次数
		long m_failtimes = 0; // 一共连接失败的次数

		long m_conTime = 0; // 花费在TCP握手的总时间
		long m_firstData = 0; // 花费在首包到达的总时间
		long m_totalSize = 0; // 总共接收的数据量
		long m_totalCost = 0; // 花费在数据下载上的总时间

		void reset()
		{
			m_times = 0; // 一共成功连接的次数
			m_failtimes = 0; // 连接失败的次数

			m_conTime = 0; // 花费在TCP握手的总时间
			m_firstData = 0; // 花费在首包到达的总时间
			m_totalSize = 0; // 总共接收的数据量
			m_totalCost = 0; // 花费在数据下载上的总时间
		}

		String report()
		{
			String res = "" + m_times + "," + m_failtimes + ",";
			try
			{
				if (m_times > 0)

					res = res + m_conTime / (m_times) + "," + m_firstData / (m_times);
				else
					res = res + "0,0";

				res = res + ",";
				if (m_totalCost > 0)
				{
					res = res + m_totalSize / m_totalCost;
				} else
				{
					res = res + "0";
				}
			} catch (Exception e)
			{

			}
			return res;
		}
	};

	class API
	{
		String name;// API名称
		long time;// API连接时长
	}

	// API情况统计
	class APIStat
	{
		final static int MAX_SIZE = 10;
		long m_times = 0; // 一共(成功)连接的次数
		long m_totalCost = 0; // API连接总时长
		long m_average_value = 0; // API连接平均时长
		List<API> topApis = new ArrayList<API>();//

		/**
		 * 是否是TOP时长API
		 * 
		 * @param input
		 * @return
		 */
		Boolean isTop(long input)
		{
			if (input == 0)
			{
				return false;
			}
			if (topApis.size() < MAX_SIZE)
			{
				return true;
			} else
			{
				long smallest = 0;// 已缓存TOP时长API最小值
				for (API api : topApis)
				{// 找出最小值
					if (smallest == 0)
					{
						smallest = api.time;
					} else
					{
						if (smallest > api.time)
						{
							smallest = api.time;
						}
					}
				}
				for (API api : topApis)
				{
					if (input > api.time)
					{
						return true;
					} else if (input == api.time)
					{
						if (api.time > smallest)
						{
							return true;
						} else
						{
							if (topApis.size() < MAX_SIZE)
							{// TOP时长API未满上线
								return true;
							}
						}
					}
				}
				return false;
			}
		}

		void add(String name, long time)
		{
			int size = topApis.size();
			if (size < MAX_SIZE)
			{
				API api = new API();
				api.name = name;
				api.time = time;
				topApis.add(api);
			} else if (size == MAX_SIZE)
			{
				long smallest = 0;// 已缓存TOP时长API最小值
				int index = 0;// 已缓存TOP时长API最小值的位置
				for (int i = 0; i < size; i++)
				{// 找出最小值在列表的位置
					API api = topApis.get(i);
					if (smallest == 0)
					{
						smallest = api.time;
						index = i;
					} else
					{
						if (smallest > api.time)
						{
							smallest = api.time;
							index = i;
						}
					}
				}
				API api = topApis.get(index);// 替换最小值位置的值
				api.name = name;
				api.time = time;
			}
		}

		void reset()
		{
			m_average_value = 0;
			topApis.clear();
		}

		/**
		 * 输出API情况统计结果, 格式为 平均时间,API名称1_对应时长,API名称2_对应时长 例:
		 * 3,com.taobao.client.sys.login_9,com.taobao.search.api.getShopList_6
		 * 
		 * @return
		 */
		String report()
		{
			StringBuffer sb = new StringBuffer();
			sb.append(String.valueOf(m_average_value)).append(",");
			int size = topApis.size();
			for (int i = 0; i < size; i++)
			{
				API api = topApis.get(i);
				sb.append(api.name).append("_").append(String.valueOf(api.time));
				if (i != size - 1)
				{
					sb.append(",");
				}
			}
			return sb.toString();
		}
	};

	// 网络连接的统计分图片和API
	static ConnStat m_imgStat; // 图片的统计
	static ConnStat m_apiStat; // API的统计
	static APIStat m_apiTimeStat; // API时长的统计

	// http header
	// 拼写错误，must check one by one
	public static final String RESPONSE_CODE = "response-code";
	public static final String LASTMODIFIED_TIME = "last-modified";
	public static final String CONTENTTYPE = "content-type";
	public static final String REDIRECT_LOCATION = "location";
	public static final String ACCEPT_ENCODING = "accept-encoding";
	public static final String EXPIRES_TIME = "expires";
	public static final String CACHE_CONTROL = "cache-control";
	public static final String IF_MODIFY_SINCE = "if-modified-since";
	public static final String CONTENT_LENGTH = "content-length";
	public static final String CONTENT_RANGE = "content-range";
	public static final String SET_COOKIE = "set-cookie";

	/*
	 * Steve Hack for v3.0: Now Application don't support cancel process(For
	 * example Login), so if the retry time is larger, user must waiting for
	 * long time. And he/she can't cancel the process. So we hack there
	 * temprary.
	 * 
	 * MUST BE FIX in Next Version
	 */
	private static final int MAX_REDIRECT_TIME = 5;


	private ApiProperty apiProperty;
	private String fullUrl;

	// private Context context;
	private boolean cancelled;
	private int redirectTime = 0;
	private long requestStartTimestamp = 0l;
	private static ApiConnectorStatusListener m_stautsListener;

	/**
	 * 设置监听API完成等情况的listener
	 * 
	 * @param listener
	 *            全局唯一的listener
	 */
	static void setStatusListener(ApiConnectorStatusListener listener)
	{
		m_stautsListener = listener;
	}

	/**
	 * 构造函数
	 * 
	 * @param url
	 *            API访问的全URL地址
	 * @param property
	 *            连接的属性
	 */
	public ApiConnector(String url, ApiProperty property)
	{

		// TODO: persistent cookie
		// if(Build.VERSION.SDK_INT >= 9 )
		// {
		// MyCookieStore cs = new
		// MyCookieStore(MunionConfigManager.getInstance().getContext());
		// CookieHandler.setDefault( new CookieManager(cs,
		// CookiePolicy.ACCEPT_ALL ) );
		// }

		if (m_imgStat == null)
			m_imgStat = new ConnStat();

		if (m_apiStat == null)
		{
			m_apiStat = new ConnStat();
		}
		if (m_apiTimeStat == null)
		{
			m_apiTimeStat = new APIStat();
		}
		this.fullUrl = url;
		if (property != null)
		{
			this.apiProperty = property;
		} else
		{
			this.apiProperty = new ApiProperty(); // default property
		}
		// 限制HTTP链接不重复只用底层TCP链路，以防止TCP被错误关闭。 by 伯奎
		System.setProperty("http.keepAlive", "false");
	}

	/**
	 * 取消API调用操作 注：取消埋点在 域名解析前 http连接前 connectorhelper解析前 读取网络数据前
	 * 在asyncconnect方法调用时 如果中途取消 将不会有消息通知
	 */

	public void cancel()
	{
		cancelled = true;
	}

	/**
	 * 同步连接，连接的属性在apiProperty中
	 * 
	 * @return ApiResult
	 */
	public ApiResult syncConnect()
	{

		if (ApiRequestMgr.getInstance().m_Context != null)
		{

			if (!NetWork.isNetworkAvailable(ApiRequestMgr.getInstance().m_Context))
			{
				return new ApiResult(ErrorConstant.API_RESULT_NETWORK_ERROR);
			}
		} else
		{
			//
		}

		cancelled = false;
		redirectTime = 0;
		ApiResult apiResult = null;
		int i = 0;
		String errMessage = "";
		for (i = 0; i < apiProperty.m_retryTime; i++)
		{
			// 连接被取消
			if (cancelled)
				return ApiResult.Cancelled;

			String surl = this.fullUrl;
			if (surl == null || surl.length() <= 5)
			{// 无效的链接
				return ApiResult.BadParam;
			}

			try
			{
				return dataConnect();
			} catch (ApiNetWorkTimeoutException e)
			{
				// 超时引起的，设置超时时间
				errMessage = e.getMessage();
				e.printStackTrace();
				apiResult = new ApiResult(ErrorConstant.API_RESULT_NETWORK_ERROR, e.getMessage(), null);
				apiResult.timeoutTime = e.mTimeout;

			} catch (ApiNetWorkErrorException e)
			{
				errMessage = e.getMessage();
				e.printStackTrace();
				apiResult = new ApiResult(ErrorConstant.API_RESULT_NETWORK_ERROR, e.getMessage(), null);
				break; // 除了DNS和Timeout其他网错误，不重试
			} catch (ApiOverFlowException e)
			{
				// Adv.onCaughException(e);
				e.printStackTrace();
				apiResult = new ApiResult(ErrorConstant.API_RESULT_TOO_LARGE_RESPOSE, e.getMessage(), null);
				break;
			} catch (RedirectException e)
			{
				e.printStackTrace();
				apiResult = new ApiResult(ErrorConstant.API_RESULT_REDIRECT_MANY, e.getMessage(), null);
				break;
			}
			/*
			 * by Steve:Fix bug, 当网络超时时，连续的Retry需要做一下Sleep we should release CPU
			 * taken between retrying
			 */
			try
			{
				Thread.sleep((i + 1) * 2 * 1000); // sleep for retry
			} catch (InterruptedException e)
			{
			}

		}
		if (i >= apiProperty.m_retryTime)
		{
			try
			{
				throw new NetworkFail(errMessage);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		if (apiResult == null)
			apiResult = new ApiResult(ErrorConstant.API_RESULT_UNKNOWN, "", null);

		return apiResult;
	}

	/**
	 * 通知连接的结果和进度等的回调接口 注：只是在异步调用时有用
	 */
	private AsyncDataListener m_dataListener = null;

	/**
	 * 设置回调接口 注：只是在异步调用时有用
	 */
	public void setDataListener(AsyncDataListener listener)
	{
		this.m_dataListener = listener;
	}

	/**
	 * 通知Api调用完成 注：只是在异步调用时有用
	 */
	public void notifyDataArrive(ApiResult res)
	{

		if (m_dataListener != null)
			m_dataListener.onDataArrive(res);
	}

	// 用于HTTPS
	// always verify the host - dont check for certificate
	/*
	 * final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
	 * public boolean verify(String hostname, SSLSession session) { return true;
	 * } };
	 */

	private DefaultHttpClient getHttpClient()
	{
		BasicHttpParams httpParameters = new BasicHttpParams();
		httpParameters.setIntParameter(HttpConnectionParams.SO_TIMEOUT, 10000); // 超时设置
		httpParameters.setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 10000);
		httpParameters.setIntParameter(HttpConnectionParams.SOCKET_BUFFER_SIZE, 8192 * 4);
		HttpProtocolParams.setVersion(httpParameters, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(httpParameters, "UTF-8");

		DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
		httpClient.setRedirectHandler(new RedirectHandler()
		{
			@Override
			public boolean isRedirectRequested(HttpResponse response, HttpContext context)
			{
				return false;
			}

			@Override
			public URI getLocationURI(HttpResponse response, HttpContext context) throws ProtocolException
			{
				return null;
			}
		});
		String proxyHost = null;
		int proxyPort = -1;
		proxyHost = android.net.Proxy.getDefaultHost();
		proxyPort = android.net.Proxy.getDefaultPort();
		if (proxyHost != null)
		{
			httpParameters.setParameter(ConnRoutePNames.DEFAULT_PROXY, new HttpHost(proxyHost, proxyPort));
		}
		return httpClient;
	}

	protected void onPreExecute(HttpClient httpClient)
	{
		if (httpClient instanceof DefaultHttpClient)
		{
			DefaultHttpClient client = (DefaultHttpClient) httpClient;
			CookieStore cs = new PersistentCookieStore(MunionConfigManager.getInstance().getContext());
			client.setCookieStore(cs);
		}
	}

	// 发起数据连接
	private ApiResult dataConnect() throws ApiNetWorkErrorException, ApiOverFlowException, RedirectException,
			ApiNetWorkTimeoutException
	{

		String surl = fullUrl;
		requestStartTimestamp = System.currentTimeMillis();

		// Constants.myLog("ApiConnector---dataConnector-url", surl);
		boolean isHttps = ("https".compareToIgnoreCase(surl.substring(0, 5)) == 0);

		GZIPInputStream is = null;
		CounterInputStream cs = null;
		InputStream tmp = null;
		ByteArrayOutputStream bs = null;
		DataInputStream dis = null;
		byte[] data = null;
		String host = "";
		String ip = "";
		// HttpURLConnection conn = null;
		int responeCode = -1;
		byte[] sPostData = null;
		long time = 0;
		ConnStat curStat = null;

		int theTimeout = ApiRequestMgr.getInstance().getTimeout();

		try
		{

			if (apiProperty.m_bPost)
			{
				// 如果设置了post模式，而且已经指定post数据，则用指定的post数据 @dingtao
				if (null != apiProperty.m_postData)
				{
					sPostData = apiProperty.m_postData;
				} else
				{// 否则自动把url后面的数据当成post数据 @dingtao
					int pos = surl.indexOf('?');
					if (pos >= 0)
					{
						surl = fullUrl.substring(0, pos + 1);
						sPostData = fullUrl.substring(pos + 1).getBytes();
					}

				}
			} else
			{
				surl = surl + apiProperty.getGetData();
			}

			URL url = null;
			URL ourl = new URL(surl);
			host = ourl.getHost();
			// 从DNS中解析IP
			if (NetWork.proxy || isHttps)
			{
				url = ourl;
			} else
			{
				ip = DNSResolver.fetchHostIP(host, 15000);
				if (cancelled)
				{
					return ApiResult.Cancelled;
				}

				if (ip == null || ip.length() < 3)
				{
					throw new ApiNetWorkTimeoutException("DNS is failed", theTimeout);
				}
				url = new URL(ourl.getProtocol(), ip, ourl.getPort(), ourl.getFile());
			}

			DefaultHttpClient httpClient = getHttpClient();
			/*
			 * httpClient.getParams(); //
			 * 发起链接———————————————————————————————————
			 * ——————————————————————————— conn = (HttpURLConnection)
			 * url.openConnection();
			 * 
			 * //
			 * 设置属性------------------------------------------------------------
			 * -----
			 * 
			 * // 设置超时时间
			 * 
			 * conn.setConnectTimeout(theTimeout);
			 * conn.setReadTimeout(theTimeout);
			 */
			boolean bImageDownload = !(apiProperty.getPriority() == ApiRequestMgr.PRIORITY_IMM);
			if (!bImageDownload)
			{
				curStat = m_apiStat;
			} else
			{
				curStat = m_imgStat;
			}

			// // 关闭自动重定向
			// conn.setInstanceFollowRedirects(false);
			// // 设置http头
			// conn.setRequestProperty("Host", host);
			// conn.setRequestProperty("User-Agent", userAgent);
			// conn.setRequestProperty("TB-UA", userAgent);
			// conn.setRequestProperty("Connection", "close");
			// conn.setRequestProperty("Accept-Encoding", "gzip");
			// if (apiProperty.m_startPos > 0)
			// {
			// conn.setRequestProperty("Range", "bytes=" +
			// apiProperty.m_startPos + "-");
			// }

			// 连接被取消
			if (cancelled)
				return ApiResult.Cancelled;

			HttpUriRequest request = null;
			// 处理POST方法
			if (null != sPostData)
			{
				request = new HttpPost(fullUrl);
				InputStreamEntity inputStreamEntity = new InputStreamEntity(new ByteArrayInputStream(sPostData),
						sPostData.length);
				((HttpPost) request).setEntity(inputStreamEntity);

			} else
			{
				request = new HttpGet(surl);
			}
			if (apiProperty.m_connHeaders != null)
			{
				Iterator<Entry<String, String>> iter = apiProperty.m_connHeaders.entrySet().iterator();
				while (iter.hasNext())
				{
					Map.Entry<String, String> entry = (Entry<String, String>) iter.next();
					request.setHeader(entry.getKey(), entry.getValue());
				}

			}

			onPreExecute(httpClient);
			HttpResponse response = httpClient.execute(request);
			// 统计
			synchronized (curStat)
			{
				curStat.m_times++;
				curStat.m_conTime += ((System.nanoTime() - time) / 1000000L);
			}
			// 处理服务器Response
			if (response == null)
			{
				return new ApiResult(HttpStatus.SC_BAD_REQUEST, "failed", null);
			}
			responeCode = response.getStatusLine().getStatusCode();
			ApiRequestMgr.getInstance().onConnectSucc((System.nanoTime() - time) / 1000000000L);

			// 加入RANGE以后需要处理416
			if (HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE == responeCode)
			{
				// 可能是下载已经完成了
				return new ApiResult(HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE, "bad range", null);
			}

			// 鉴权失败
			if (HttpStatus.SC_FORBIDDEN == responeCode)
			{
				return new ApiResult(HttpStatus.SC_FORBIDDEN, "forbiden", null);
			}

			// 判断是否为重定向,————————————————————————————————————————————————————————————————————

			boolean isFinalRedirectHost = apiProperty.m_finalRedirectHost != null
					&& host.contains(apiProperty.m_finalRedirectHost);
			if (responeCode == 302 && apiProperty.m_redirectAuto && !isFinalRedirectHost)
			{
				// 重定向次数超出上限
				if (redirectTime > MAX_REDIRECT_TIME)
				{
					throw new RedirectException("too many redirect");
				}
				redirectTime++;
				String redirectUrl = response.getFirstHeader(REDIRECT_LOCATION).getValue();
				if(redirectUrl == null)
				{
					redirectUrl = response.getFirstHeader("Location").getValue();
				}
				if (redirectUrl != null)
				{
					// 如果存在重定向url，则做自动重定向
					if (!redirectUrl.toLowerCase().startsWith("http"))
						redirectUrl = new URL("http", host, redirectUrl).toString();

					// if(responeCode ==
					// ApiResule.API_RESULT_USE_PROXY)//305需要重新提交数据
					fullUrl = redirectUrl;
					return dataConnect();
				}
			}

			// 获取respone
			// header——————————————————————————————————————————————————————————————————
			// 取出所有respone header key转为小写
			if(responeCode == 302 && apiProperty.m_connHeaders == null)
			{
				apiProperty.m_connHeaders = new HashMap<String, String>();
			}
			if (apiProperty.m_connHeaders != null)
			{
				apiProperty.m_connHeaders.put(RESPONSE_CODE, Integer.toString(responeCode));
				// getHeaderField(0)is status line —— response code
				Header[] headers = response.getAllHeaders();
				for (int i = 0; i < headers.length; i++)
				{
					Header header = headers[i];
					if (header != null)
					{
						String key = header.getName();
						if (key == null)
							continue;
						String value = header.getValue();
						if (REDIRECT_LOCATION.equalsIgnoreCase(key))
						{
							if (value != null)
							{
								apiProperty.m_connHeaders.put(key.toLowerCase(), value);
							}
						}
					}
				}
				// if (key == null)
				// break;
				// i++;
				// String value = conn.getHeaderField(key);
				// if (REDIRECT_LOCATION.equalsIgnoreCase(key))
				// {
				// if (!value.toLowerCase().startsWith("http"))
				// {
				// URL redirect = new URL("http", host, value);
				// value = redirect.toString();
				// }
				// }
				// apiProperty.m_connHeaders.put(key.toLowerCase(), value);
				// } while (true);
			}

			// 获取respone
			// data————————————————————————————————————————————————————————————————————————

			// 检查是否超过长度限制
			int totalLen = 0;
			// String contentLen =
			// response.getFirstHeader(CONTENT_LENGTH).toString();
			// if (contentLen != null && (totalLen =
			// Integer.parseInt(contentLen)) > apiProperty.m_downMaxSize)
			// throw new ApiOverFlowException("The Content-Length is to large:"
			// + contentLen);
			// Map<String , List<String>> header = conn.getHeaderFields();

			// 开始读数据，记录时间
			time = System.nanoTime();

			Header header = response.getFirstHeader("Content-Encoding");
			tmp = response.getEntity().getContent();
			if (header != null && "gzip".equalsIgnoreCase(header.getValue()))
			{
				cs = new CounterInputStream(tmp);
				is = new GZIPInputStream(cs);
				dis = new DataInputStream(is);
			} else
			{
				dis = new DataInputStream(tmp);
			}
			// 读取数据
			int i = 0;
			byte[] b = new byte[2048];
			OutputStream os = null;
			if (null != apiProperty.m_outStream)
			{
				os = apiProperty.m_outStream;
			} else
			{
				os = bs = new ByteArrayOutputStream();
			}

			int notifystep = totalLen / apiProperty.m_ProgressStep;
			int notifypos = notifystep;

			int startPos = (int) apiProperty.m_startPos; // 这次开始读的开始地址
			// 通知进度 进度0
			if (null != m_dataListener)
			{
				m_dataListener.onProgress("下载中", (int) apiProperty.m_startPos, startPos + totalLen);
			}

			boolean bFirstResponse = true;

			while ((i = dis.read(b, 0, 2048)) != -1)
			{
				// 统计总的下载字节数
				synchronized (curStat)
				{
					curStat.m_totalSize += i;
				}
				// 统计第一次时间
				if (bFirstResponse)
				{
					synchronized (curStat)
					{
						curStat.m_firstData += ((System.nanoTime() - time) / 1000000);
					}
					bFirstResponse = false;
				}

				try
				{
					os.write(b, 0, i);

					// 这里是文件下载
					if (null != apiProperty.m_outStream)
					{
						try
						{
							Thread.sleep(5);
						} catch (Exception e)
						{
						}
					}
				} catch (IOException e)
				{
					e.printStackTrace();
					synchronized (curStat)
					{
						curStat.m_totalCost += ((System.nanoTime() - time) / 1000000L);
					}
					// sdcard 可能发生读写错误
					return new ApiResult(ErrorConstant.API_RESULT_SDCARD_WRITE_ERROR);
				}

				apiProperty.m_startPos += i;
				// 通知进度
				if (null != m_dataListener && apiProperty.m_startPos > notifypos)
				{
					m_dataListener.onProgress("下载中", (int) apiProperty.m_startPos, startPos + totalLen);
					notifypos += notifystep;
				}

				if (cancelled && null != apiProperty.m_outStream)
				{
					synchronized (curStat)
					{
						curStat.m_totalCost += ((System.nanoTime() - time) / 1000000L);
					}
					return ApiResult.Cancelled;
				}

			}
			if (null == apiProperty.m_outStream)
			{
				data = bs.toByteArray();
			} else
			{
				data = null;
			}

			synchronized (curStat)
			{
				curStat.m_totalCost += ((System.nanoTime() - time) / 1000000L);
			}
			return new ApiResult(HttpStatus.SC_OK, "success", data);

		} catch (SocketTimeoutException e)
		{
			if (null != curStat)
			{
				synchronized (curStat)
				{
					curStat.m_failtimes++;
				}
			}

			// 出现超时异常时，且超时时间达到了最大值时清楚Cache
			if (ApiRequestMgr.getInstance().getTimeout() >= ApiRequestMgr.getInstance().m_maxTimeout)
				DNSResolver.removeHost(host);

			ApiRequestMgr.getInstance().onTimeout();
			e.printStackTrace();

			throw new ApiNetWorkTimeoutException("host:" + host + ",e=" + e.getMessage(), theTimeout);

		} catch (FileNotFoundException e)
		{

			if (null != curStat)
			{
				synchronized (curStat)
				{
					curStat.m_failtimes++;
				}
			}
			// Adv.onCaughException(e);
			e.printStackTrace();

			return new ApiResult(HttpStatus.SC_NOT_FOUND, e.getMessage(), null);
		} catch (Exception e)
		{
			if (null != curStat)
			{
				synchronized (curStat)
				{
					curStat.m_failtimes++;
				}
			}
			ApiRequestMgr.getInstance().onTimeout();
			// Adv.onCaughException(e);
			// 线上反馈printStackTrace内部会抛空指针，因此屏蔽
			// e.printStackTrace();

			// 连接出现异常时，清理域名与ip的关联，重新解析。
			DNSResolver.removeHost(host);
			if (null != bs)
				bs.reset();

			if (apiProperty.m_connHeaders != null)
			{
				apiProperty.m_connHeaders.clear();
			}
			throw new ApiNetWorkErrorException("host:" + host + ",e=" + e.getMessage(), theTimeout);
		} finally
		{

			// 通知完成
			if (m_stautsListener != null)
			{

				int readsize = 0;
				if (bs != null)
				{
					readsize = bs.size();
				}
				if (cs != null)
				{
					readsize = cs.m_count;
				}
				m_stautsListener.onFinish(responeCode, readsize, System.currentTimeMillis() - requestStartTimestamp,
						surl);
			}

			// 关闭资源
			if (dis != null)
			{
				try
				{
					dis.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}

			if (tmp != null)
			{
				try
				{
					tmp.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}

			if (cs != null)
			{
				try
				{
					cs.close();
					cs = null;
				} catch (IOException e)
				{

					e.printStackTrace();
				}
			}
			if (is != null)
			{
				try
				{
					is.close();
					is = null;
				} catch (IOException e)
				{

					e.printStackTrace();
				}
			}

			if (bs != null)
			{

				try
				{
					bs.close();
					bs = null;
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}

	}

	@SuppressWarnings("serial")
	class ApiNetWorkErrorException extends Exception
	{
		int mTimeout;

		public ApiNetWorkErrorException(String message, int timeout)
		{
			super(message);
			mTimeout = timeout;
		}
	}

	@SuppressWarnings("serial")
	class ApiNetWorkTimeoutException extends Exception
	{
		int mTimeout;

		public ApiNetWorkTimeoutException(String message, int timeout)
		{
			super(message);
			mTimeout = timeout;
		}
	}

	@SuppressWarnings("serial")
	class ApiOverFlowException extends Exception
	{
		public ApiOverFlowException(String message)
		{
			super(message);
		}
	}

	@SuppressWarnings("serial")
	class RedirectException extends Exception
	{
		public RedirectException(String message)
		{
			super(message);
		}
	}

	@SuppressWarnings("serial")
	class NetworkFail extends Exception
	{
		public NetworkFail(String message)
		{
			super(message);
		}
	}

	ApiProperty getApiProperty()
	{
		return apiProperty;
	}
}
