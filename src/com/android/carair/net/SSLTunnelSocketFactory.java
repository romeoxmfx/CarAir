package com.android.carair.net;
/**
 * @author baiyi.hwj E-mail:baiyi.hwj@taobao.com
 * @version 创建时间：2013-4-3 下午7:31:58
 * 类说明
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * SSLSocketFactory for tunneling sslsockets through a proxy
 */
class SSLTunnelSocketFactory extends SSLSocketFactory {

    private SSLSocketFactory dfactory;

    private String tunnelHost;

    private int tunnelPort;
    
    private String useragent;

    public SSLTunnelSocketFactory(String proxyhost, int proxyport,
			SSLSocketFactory socketfactory, String user_agent) {
    	
    	tunnelHost = proxyhost;
        tunnelPort = proxyport;
        
        if(socketfactory != null)
        	dfactory = socketfactory;
        else
        	dfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        useragent = user_agent;
	}

	public Socket createSocket(String host, int port) throws IOException,
            UnknownHostException {
        return createSocket(null, host, port, true);
    }

    public Socket createSocket(String host, int port, InetAddress clientHost,
                               int clientPort) throws IOException, UnknownHostException {
        return createSocket(null, host, port, true);
    }

    public Socket createSocket(InetAddress host, int port) throws IOException {
        return createSocket(null, host.getHostName(), port, true);
    }

    public Socket createSocket(InetAddress address, int port,
                               InetAddress clientAddress, int clientPort) throws IOException {
        return createSocket(null, address.getHostName(), port, true);
    }

    public Socket createSocket(Socket s, String host, int port,
                               boolean autoClose) throws IOException, UnknownHostException {

        Socket tunnel = new Socket(tunnelHost, tunnelPort);

        doTunnelHandshake(tunnel, host, port);

        SSLSocket result = (SSLSocket) dfactory.createSocket(tunnel, host,
                port, autoClose);

        result.addHandshakeCompletedListener(new HandshakeCompletedListener() {
            public void handshakeCompleted(HandshakeCompletedEvent event) {
            }
        });

        result.startHandshake();

        return result;
    }

    private void doTunnelHandshake(Socket tunnel, String host, int port)
            throws IOException {
        OutputStream out = tunnel.getOutputStream();
        String msg = "CONNECT " + host + ":" + port + " HTTP/1.1\n"
                + "User-Agent: "
                + useragent + "\n"
                + "Host:" + host 
//                + "\n"
//                + "Proxy-Connection: Keep-Alive\n"    // Always set the Proxy-Connection to Keep-Alive for the benefit of HTTP/1.0 proxies like Squid.
                + "\r\n\r\n";
        byte b[];
        try {
            /*
                * We really do want ASCII7 -- the http protocol doesn't change
                * with locale.
                */
            b = msg.getBytes("ASCII7");
        } catch (UnsupportedEncodingException ignored) {
            /*
                * If ASCII7 isn't there, something serious is wrong, but
                * Paranoia Is Good (tm)
                */
            b = msg.getBytes();
        }
        out.write(b);
        out.flush();

        /*
           * We need to store the reply so we can create a detailed
           * error message to the user.
           */
        byte reply[] = new byte[200];
        int replyLen = 0;
        int newlinesSeen = 0;
        boolean headerDone = false; /* Done on first newline */

        InputStream in = tunnel.getInputStream();

        while (newlinesSeen < 2) {
            int i = in.read();
            if (i < 0) {
                throw new IOException("Unexpected EOF from proxy");
            }
            if (i == '\n') {
                headerDone = true;
                ++newlinesSeen;
            } else if (i != '\r') {
                newlinesSeen = 0;
                if (!headerDone && replyLen < reply.length) {
                    reply[replyLen++] = (byte) i;
                }
            }
        }

        /*
           * Converting the byte array to a string is slightly wasteful
           * in the case where the connection was successful, but it's
           * insignificant compared to the network overhead.
           */
        String replyStr;
        try {
            replyStr = new String(reply, 0, replyLen, "ASCII7");
        } catch (UnsupportedEncodingException ignored) {
            replyStr = new String(reply, 0, replyLen);
        }

        /* Look for 200 connection established */
        if (replyStr.toLowerCase().indexOf("200 connection established") == -1) {
            throw new IOException("Unable to tunnel through " + tunnelHost
                    + ":" + tunnelPort + ".  Proxy returns \"" + replyStr
                    + "\"");
        }

        /* tunneling Handshake was successful! */
    }

    public String[] getDefaultCipherSuites() {
        return dfactory.getDefaultCipherSuites();
    }

    public String[] getSupportedCipherSuites() {
        return dfactory.getSupportedCipherSuites();
    }
}