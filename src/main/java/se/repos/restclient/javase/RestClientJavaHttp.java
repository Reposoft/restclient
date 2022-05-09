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
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
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
 * REST client using java.net.http introduced in Java 11.
 * 
 * Always using Keep-Alive, no configuration.
 * Basic Authentication is forced if the RestAuthentication provides a username (no retry on 401).
 * Default proxy config.
 * Default CookieManager.
 */
public class RestClientJavaHttp extends RestClientUrlBase {

	private static final Logger logger = LoggerFactory.getLogger(RestClientJavaHttp.class);

	public static final String ACCEPT_HEADER_NAME = "Accept"; 
	public static final String AUTH_HEADER_NAME = "Authorization"; 
	public static final String AUTH_HEADER_PREFIX = "Basic ";
	
	/**
	 * Timeout in milliseconds.
	 * Default: {@value #DEFAULT_CONNECT_TIMEOUT}.
	 */
	public static final int DEFAULT_CONNECT_TIMEOUT = 5000;
	// No method for setting connect timeout per-request, only the total timeout.
	// https://bugs.openjdk.java.net/browse/JDK-8209075
	// TODO: Consider method for setting timeout, need to rebuild the clients.
	

	private RestAuthentication auth;
	private boolean authenticationForced = true;
	
	private HttpClient clientRedirectNormal;
	private HttpClient clientRedirectNever;
	
	
	@Inject
	public RestClientJavaHttp(
			@Named("config:se.repos.restclient.serverRootUrl") String serverRootUrl,
			RestAuthentication auth) {
		super(serverRootUrl);
		this.auth = auth;
		
		HttpClient.Builder builderRedirectNormal = HttpClient.newBuilder()
				.connectTimeout(Duration.ofMillis(DEFAULT_CONNECT_TIMEOUT))
				.followRedirects(HttpClient.Redirect.NORMAL);
		
		HttpClient.Builder builderRedirectNever = HttpClient.newBuilder()
				.connectTimeout(Duration.ofMillis(DEFAULT_CONNECT_TIMEOUT))
				.followRedirects(HttpClient.Redirect.NEVER);

		SSLContext sslContext = (auth == null) ? null : auth.getSSLContext(serverRootUrl);
		if (sslContext != null) {
			builderRedirectNormal.sslContext(sslContext);
			builderRedirectNever.sslContext(sslContext);
		}
		this.clientRedirectNormal = builderRedirectNormal.build();
		this.clientRedirectNever = builderRedirectNever.build();
	}
	
	
	// Public API, performs BASIC authentication if RestAuthentication has username.
	@Override
	public void get(URL url, RestResponse response) throws IOException, HttpStatusError {
		Map<String,String> requestHeaders = new HashMap<String, String>(2);
		if (response instanceof RestResponseAccept) {
			requestHeaders.put(ACCEPT_HEADER_NAME, ((RestResponseAccept) response).getAccept());
		}
		
		try {
			// There are 2 approaches to making BASIC Auth efficient:
			// - Remembering that Auth was needed after the first request. Per path? Per user?
			// - Indicating to the implementation to always send auth. Inherently per host unless multiple Restclient instances are created. 
			if (authenticationForced && isAuthBasic()) {
				String username = auth.getUsername(null, null, null);
				logger.debug("Authenticating user {}, forced", username);
				setAuthHeaderBasic(requestHeaders, username, auth.getPassword(null, null, null, username));
			}
			get(url, response, requestHeaders);
		} catch (HttpStatusError e) {
			// No longer support BASIC auth following 401, using forced auth instead.
			throw e;
		}
	}
	
	/**
	 * 
	 * @param url
	 * @param response Will only be written to after status 200 is received,
	 *  see {@link HttpStatusError#getResponse()} for error body.
	 * @param requestHeaders Can be used for authentication, no BASIC authentication performed by this method 
	 * @throws IOException
	 * @throws HttpStatusError
	 */
	public void get(URL url, RestResponse restResponse, Map<String,String> requestHeaders) throws IOException, HttpStatusError {
		HttpResponse<InputStream> response;
		try {
			HttpRequest.Builder builder = HttpRequest.newBuilder()
					.uri(url.toURI())
					.GET();

			for (String h : requestHeaders.keySet()) {
				builder.header(h, requestHeaders.get(h));
			}
			
			logger.debug("GET connection to: {}", url);
			response = clientRedirectNormal.send(builder.build(), BodyHandlers.ofInputStream());
			
			// response should be ok regardless of status
			ResponseHeaders headers = new ResponseHeadersJavaHttp(response);
			int responseCode = response.statusCode();
		
			// NOTE these comments are from original JavaNet implementation (before Java 11 http
			// check status code before trying to get response body
			// to avoid the unclassified IOException
			// Currently getting body only for 200 OK. 
			// There might be more 2xx responses with a valuable body.
			if (responseCode == HttpURLConnection.HTTP_OK) {
				OutputStream receiver = restResponse.getResponseStream(headers);
				try {
					InputStream body = response.body();
					pipe(body, receiver);
					body.close();
					// Should NOT close the receiver, must be handled by calling class.
					// See HttpClient BasicHttpEntity.writeTo(..) for consistency btw http clients.
					//receiver.close();
				} catch (IOException e) {
					throw check(e);
				}
			} else if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
				// redirect within the same protocol will be handled transparently, typically ending up here when redirected btw http/https
				logger.info("Server responded with redirect ({}): {}", responseCode, headers.get("Location"));
				ByteArrayOutputStream b = new ByteArrayOutputStream();
				try {
					InputStream body = response.body();
					if (body == null) {
						logger.warn("Redirect did not contain a body.");
					}
					pipe(body, b);
					body.close();
				} catch (IOException e) {
					throw check(e);
				}
				throw new HttpStatusError(url.toString(), headers, b.toString());
				
			} else if (responseCode < 400) { // Other non-error responses.
				// Do we need to consume a stream if the server sends one? Important when using keep-alive?
				// Might need to handle a category of responses where we attempt to get the Body but allow failure.
				
				logger.warn("Unsupported HTTP response code: {}", responseCode);
				//throw new RuntimeException("Unsupported HTTP response code: " + responseCode);
				
			} else { // Error stream expected for 4xx and 5xx.
				ByteArrayOutputStream b = new ByteArrayOutputStream();
				try {
					InputStream body = response.body();
					if (body == null) {
						throw new RuntimeException("Response error could not be read for status " + responseCode);
					}
					pipe(body, b);
					body.close();
				} catch (IOException e) {
					throw check(e);
				}
				throw new HttpStatusError(url.toString(), headers, b.toString());
			} 
			
			
		} catch (HttpStatusError e) {
			throw e;
		} catch (IOException e) {
			throw check(e);
		} catch (InterruptedException e) {
			logger.warn("RestClient was interrupted: {}", url);
			throw new IOException("interrupted", e);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Non-HTTP protocols not supported. Got URL: " + url);
		}
	}

	
	private static void setAuthHeaderBasic(Map<String, String> requestHeaders, String username, String password) {
		
		requestHeaders.put(AUTH_HEADER_NAME,
				AUTH_HEADER_PREFIX + Codecs.base64encode(
						username + ":" + password));
	}
	
	public boolean isAuthenticationForced() {
		return authenticationForced;
	}

	/**
	 * Makes the http client send authentication in the initial request without first getting a 401.
	 * TODO: Consider making this an inject settor.
	 * @param authenticationForced
	 */
	public void setAuthenticationForced(boolean authenticationForced) {
		if (authenticationForced && auth == null) {
			throw new IllegalArgumentException("Authentication forced assumes an authentication instance is provided.");
		}
		this.authenticationForced = authenticationForced;
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
		try {
			HttpRequest.Builder builder = HttpRequest.newBuilder()
					.uri(url.toURI())
					.method("HEAD", HttpRequest.BodyPublishers.noBody());

			ResponseHeaders head = null;
			logger.debug("attempting HEAD request with java http client: {}", url);
			HttpResponse<Void> response = clientRedirectNever.send(builder.build(), BodyHandlers.discarding());
			
			logger.trace("HEAD {} connection done", url);
			head = new ResponseHeadersJavaHttp(response);
			logger.trace("HEAD {} headers read", url);
			// Intentionally not checking the status code.
			return head;
		} catch (IOException e) {
			throw check(e);
		} catch (InterruptedException e) {
			logger.warn("RestClient was interrupted: {}", url);
			throw new IOException("interrupted", e);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Non-HTTP protocols not supported. Got URL: " + url);
		}
	}
	
	private boolean isAuthBasic() {
		
		if (auth != null && auth.getUsername(null, null, null) != null) {
			return true;
		}
		return false;
	}
	
}
