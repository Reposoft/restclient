/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.repos.restclient.javase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.repos.restclient.HttpStatusError;
import se.repos.restclient.ResponseHeaders;
import se.repos.restclient.RestAuthentication;
import se.repos.restclient.RestResponse;
import se.repos.restclient.RestResponseAccept;
import se.repos.restclient.base.Codecs;
import se.repos.restclient.base.RestClientUrlBase;

/**
 * REST client using java.net.URLConnection.
 * Has severe limitations:
 * <ul>
 * <li>Repeated HEAD requests make the client hang</li>
 * <li>HTTP authentication BASIC supported, not Digest</li>
 * </ul>
 * 
 * Might be possible to work around these limitations in a future
 * version, but for now consider using repos-restclient-hc.
 * <p>
 * This implementation was previously called {@link HttpGetClientJavaNet}.
 */
public class RestClientJavaNet extends RestClientUrlBase {

	private static final Logger logger = LoggerFactory.getLogger(RestClientJavaNet.class);

	public static final String ACCEPT_HEADER_NAME = "Accept"; 
	public static final String AUTH_HEADER_NAME = "Authorization"; 
	public static final String AUTH_HEADER_PREFIX = "Basic ";
	
	/**
	 * Timeout in milliseconds.
	 * Default: {@value #DEFAULT_CONNECT_TIMEOUT}.
	 */
	public static final int DEFAULT_CONNECT_TIMEOUT = 5000;
	
	private int timeout = DEFAULT_CONNECT_TIMEOUT;

	private RestAuthentication auth;
	
	@Inject
	public RestClientJavaNet(
			@Named("config:se.repos.restclient.serverRootUrl") String serverRootUrl,
			RestAuthentication auth) {
		super(serverRootUrl);
		this.auth = auth;
	}
	
	@Override
	public void get(URL url, RestResponse response) throws IOException, HttpStatusError {
		Map<String,String> requestHeaders = new HashMap<String, String>(2);
		if (response instanceof RestResponseAccept) {
			requestHeaders.put(ACCEPT_HEADER_NAME, ((RestResponseAccept) response).getAccept());
		}
		
		try {
			get(url, response, requestHeaders);
		} catch (HttpStatusError e) {
			// Retry if prompted for BAIDC authentication, support per-request users unlike java.net.Authenticate
			if (auth != null && e.getHttpStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
				// TODO verify BASIC auth scheme
				List<String> challenge = e.getHeaders().get("WWW-Authenticate");
				if (challenge.size() == 0) {
					logger.warn("Got 401 status without WWW-Authenticate header");
					throw e;
				}
				// TODO verify realm
				String username = auth.getUsername(null, null, null);
				logger.debug("Authenticating user {} as retry for {}", username, challenge.get(0));
				requestHeaders.put(AUTH_HEADER_NAME,
						AUTH_HEADER_PREFIX + Codecs.base64encode(
								username + ":" + auth.getPassword(null, null, null, username)));
				get(url, response, requestHeaders);
			} else {
				// Not authentication, throw the error
				throw e;
			}
		}
	}
	
	/**
	 * 
	 * @param url
	 * @param response Will only be written to after status 200 is received,
	 *  see {@link HttpStatusError#getResponse()} for error body.
	 * @param requestHeaders Can be used for authentication, as the default Authenticator behavior is static
	 * @throws IOException
	 * @throws HttpStatusError
	 */
	public void get(URL url, RestResponse response, Map<String,String> requestHeaders) throws IOException, HttpStatusError {
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) url.openConnection();
		} catch (ClassCastException e) {
			throw new RuntimeException("Non-HTTP protocols not supported. Got URL: " + url);
		} catch (IOException e) {
			throw check(e);
		}
		// authentication and some settings is static for URLConnection, preserver current setting
		conn.setInstanceFollowRedirects(true);
		configure(conn);
		for (String h : requestHeaders.keySet()) {
			conn.setRequestProperty(h, requestHeaders.get(h));
		}
		logger.info("GET connection to {}", url);
		try {
			conn.connect();
		} catch (IOException e) {
			throw check(e);
		}
		
		// response should be ok regardless of status, get content
		ResponseHeaders headers = new URLConnectionResponseHeaders(conn);
		int responseCode = conn.getResponseCode();
		
		// check status code before trying to get response body
		// to avoid the unclassified IOException
		// Currently getting body only for 200 OK. 
		// There might be more 2xx responses with a valuable body.
		if (responseCode == HttpURLConnection.HTTP_OK) {
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
		} else if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
			logger.info("Server responded with redirect ({}): {}", responseCode, headers.get("Location"));
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			try {
				InputStream body = conn.getInputStream();
				if (body == null) {
					logger.warn("Redirect did not contain a body.");
				}
				pipe(body, b);
				body.close();
			} catch (IOException e) {
				throw check(e);
			} finally {
				conn.disconnect();
			}
			throw new HttpStatusError(url.toString(), headers, b.toString());
			
		} else if (responseCode < 400) { // Other non-error responses.
			// Do we need to consume a stream if the server sends one? Important when using keep-alive?
			// Might need to handle a category of responses where we attempt to get the Body but allow failure.
			
			conn.disconnect();
			logger.warn("Unsupported HTTP response code: {}", responseCode);
			//throw new RuntimeException("Unsupported HTTP response code: " + responseCode);
			
		} else { // Error stream expected for 4xx and 5xx.
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			try {
				InputStream body = conn.getErrorStream();
				if (body == null) {
					throw new RuntimeException("Response error could not be read for status " + conn.getResponseCode());
				}
				pipe(body, b);
				body.close();
			} catch (IOException e) {
				throw check(e);
			} finally {
				conn.disconnect();
			}
			throw new HttpStatusError(url.toString(), headers, b.toString());
		} 
		
		
	}
	
	/**
	 * Shared configuration for all request methods.
	 * @param conn opened but not connected
	 */
	protected void configure(HttpURLConnection conn) {
		conn.setConnectTimeout(timeout);
		if (conn instanceof HttpsURLConnection) {
			configureSSL((HttpsURLConnection) conn);
		}
	}
	
	protected void configureSSL(HttpsURLConnection conn) {
		if (auth == null) {
			return;
		}
		URL url = conn.getURL();
		String root = url.getProtocol() + "://" + url.getHost() + (url.getPort() > 0 ? ":" + url.getPort() : "");
		SSLContext ctx = auth.getSSLContext(root);
		if (ctx == null) {
			return;
		}
		SSLSocketFactory ssl = ctx.getSocketFactory();
		conn.setSSLSocketFactory(ssl);
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

	/**
	 * TODO head should authenticate of auth returns credentials, getUsername should provide method name
	 */
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
		con.setInstanceFollowRedirects(false);
		configure(con);
		ResponseHeaders head = null;
		try {
			logger.warn("attempting HEAD request with java.net client to {}", url); // still not sure if java.net client behaves well for HEAD requests
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
