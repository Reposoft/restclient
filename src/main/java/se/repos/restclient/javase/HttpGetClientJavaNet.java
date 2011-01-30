/*
   Copyright 2009 repos.se

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package se.repos.restclient.javase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.repos.restclient.HttpGetClient;
import se.repos.restclient.HttpStatusError;
import se.repos.restclient.ResponseHeaders;
import se.repos.restclient.RestClient;
import se.repos.restclient.RestResponse;
import se.repos.restclient.RestURL;
import se.repos.restclient.base.RestResponseWrapper;

public class HttpGetClientJavaNet implements HttpGetClient, RestClient {
	
	private static final Logger logger = LoggerFactory.getLogger(HttpGetClientJavaNet.class);
	
	/**
	 * Timeout in milliseconds.
	 * Default: {@value #DEFAULT_CONNECT_TIMEOUT}.
	 */
	public static final int DEFAULT_CONNECT_TIMEOUT = 5000;
	
	private int timeout = DEFAULT_CONNECT_TIMEOUT;
	
	@Override
	public void get(String encodedUrl, RestResponse response) throws IOException, HttpStatusError {
		read(new RestURL(encodedUrl).getURL(), new RestResponseWrapper(response){});
	}
	
	@Override
	public void read(String encodedUri, Map<String, String> queryParameters,
			Response response) throws HttpStatusError, IOException {			
		read(new RestURL(encodedUri, queryParameters).getURL(), response);
	}
	
	@Deprecated
	public void read(URL url, Response response) 
			throws IOException, HttpStatusError {
		read(url, new RestResponseWrapper(response){});
	}
	
	public void read(URL url, RestResponseWrapper response) throws IOException, HttpStatusError {
		// TODO support Accept response
		
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
			throw new HttpStatusError(conn.getResponseCode(), url, b.toString());
		}
		
		// response should be ok, get content
		ResponseHeaders headers = new URLConnectionResponseHeaders(conn);
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
	public ResponseHeaders head(String uri) throws IOException {
		URL url = new RestURL(uri).getURL();
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
			logger.warn("attempting HEAD request to {}", uri);
			con.connect();
			logger.trace("HEAD {} connected", uri);
			// gets rid of the EOF issue in Jetty test:
			InputStream b;
			if (con.getResponseCode() == 200) {
				b = con.getInputStream();
				logger.trace("HEAD {} output requested", uri);
				while (b.read() != -1) {}
				logger.trace("HEAD {} output read", uri);
				b.close();
			}
			logger.trace("HEAD {} output closed", uri);
			head = new URLConnectionResponseHeaders(con);
			logger.trace("HEAD {} headers read", uri);
		} catch (IOException e) {
			throw check(e);
		} finally {
			con.disconnect();
		}
		return head;
	}
	
}
