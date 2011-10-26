package se.repos.restclient.javase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import se.repos.restclient.HttpStatusError;
import se.repos.restclient.ResponseHeaders;
import se.repos.restclient.RestAuthentication;
import se.repos.restclient.RestResponse;
import se.repos.restclient.base.RestClientMultiHostBase;

/**
 * REST client using java.net.URLConnection.
 * Has severe limitations:
 * <ul>
 * <li>Repeated HEAD requests make the client hang</li>
 * <li>Authentication settings are static, so within a JVM
 *  <em>there can be only one user at a time</em></li>
 * </ul>
 * 
 * Might be possible to work around these limitations in a future
 * version, but for now consider using repos-restclient-hc.
 * <p>
 * This implementation was previously called {@link HttpGetClientJavaNet}.
 */
public class RestClientJavaNet extends RestClientMultiHostBase {

	private static final Logger logger = LoggerFactory.getLogger(RestClientJavaNet.class);
	
	/**
	 * Timeout in milliseconds.
	 * Default: {@value #DEFAULT_CONNECT_TIMEOUT}.
	 */
	public static final int DEFAULT_CONNECT_TIMEOUT = 5000;
	
	private int timeout = DEFAULT_CONNECT_TIMEOUT;	
	
	@Inject
	public RestClientJavaNet(
			@Named("config:se.repos.restclient.serverRootUrl") String serverRootUrl,
			RestAuthentication auth) {
		super(serverRootUrl);
		if (auth != null) {
			throw new UnsupportedOperationException("Authentication in this implementation" +
					" would be static due to java.net API, so only one user per JVM at a time." +
					" Consider using RestClientHc.");
			// We need checks that username never changes
		}
	}
	
	@Override
	public void get(URL url, RestResponse response) throws IOException, HttpStatusError {
		
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) url.openConnection();
		} catch (ClassCastException e) {
			throw new RuntimeException("Non-HTTP protocols not supported. Got URL: " + url);
		} catch (IOException e) {
			throw check(e);
		}
		// authentication and some settings is static for URLConnection, preserver current setting
		conn.setConnectTimeout(timeout);
		conn.setInstanceFollowRedirects(true);
		logger.info("GET connection to {}", url);
		try {
			conn.connect();
		} catch (IOException e) {
			throw check(e);
		}
		
		// response should be ok regardless of status, get content
		ResponseHeaders headers = new URLConnectionResponseHeaders(conn);
		
		// check status code before trying to get response body
		// to avoid the unclassified IOException
		// TODO for some reason this seems to stop followRedirects
		if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			try {
				InputStream body = conn.getErrorStream();
				pipe(body, b);
				body.close();
			} catch (IOException e) {
				throw check(e);
			} finally {
				conn.disconnect();
			}
			throw new HttpStatusError(url, headers, b.toString());
		}
		
		OutputStream receiver = response.getResponseStream(headers);
		try {
			InputStream body = conn.getInputStream();
			pipe(body, receiver);
			body.close();
		} catch (IOException e) {
			throw check(e);
		} finally {
			conn.disconnect();
		}
	}
	
	/**
	 * Makes post-processing possible.
	 */
	protected IOException check(IOException e) {
		return e;
	}

	private void pipe(InputStream source, OutputStream destination) throws IOException {
		// don't know if this is a good buffering strategy
		byte[] buffer = new byte[1024];
		int len = source.read(buffer);
		while (len != -1) {
		    destination.write(buffer, 0, len);
		    len = source.read(buffer);
		}
	}

	@Override
	public ResponseHeaders head(URL url) throws IOException {	
		HttpURLConnection con;
		try {
			con = (HttpURLConnection) url.openConnection();
		} catch (ClassCastException e) {
			throw new RuntimeException("Non-HTTP protocols not supported. Got URL: " + url);
		} catch (IOException e) {
			throw check(e);
		}
		con.setRequestMethod("HEAD");
		con.setConnectTimeout(timeout);
		ResponseHeaders head = null;
		try {
			logger.warn("attempting HEAD request to {}", url);
			con.connect();
			logger.trace("HEAD {} connected", url);
			// gets rid of the EOF issue in Jetty test:
			InputStream b;
			if (con.getResponseCode() == 200) {
				b = con.getInputStream();
				logger.trace("HEAD {} output requested", url);
				while (b.read() != -1) {}
				logger.trace("HEAD {} output read", url);
				b.close();
			}
			logger.trace("HEAD {} output closed", url);
			head = new URLConnectionResponseHeaders(con);
			logger.trace("HEAD {} headers read", url);
		} catch (IOException e) {
			throw check(e);
		} finally {
			con.disconnect();
		}
		return head;
	}
	
}
