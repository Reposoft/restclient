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
package se.repos.restclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class HttpGetClientJavaNet implements HttpGetClient {
	
	// for URLEncoder
	private String encoding = "UTF-8";
	
	@Override
	public void read(String encodedUri, Map<String, String> queryParameters,
			Response response) throws HttpStatusError, IOException {
		URL url = new URL(encodedUri + '?' + getQueryStringEncoded(queryParameters));
		read(url, response);
	}
	
	@Override
	public void read(String encodedUrl, Response response) 
			throws IllegalArgumentException, IOException, HttpStatusError {
		URL url;
		try {
			url = new URL(encodedUrl);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(encodedUrl, e);
		}
		read(url, response);
	}
		
	public void read(URL url, Response response) 
			throws IOException, HttpStatusError {
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) url.openConnection();
		} catch (ClassCastException e) {
			throw new RuntimeException("Non-HTTP protocols not supported. Got URL: " + url);
		} catch (IOException e) {
			throw check(e);
		}
		try {
			conn.connect();
		} catch (IOException e) {
			throw check(e);
		}
		
		// check status code before trying to get response body
		// to avoid the unclassified IOException
		if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
			throw new HttpStatusError(conn.getResponseCode(), url);
		}
		
		// response should be ok, get content
		String httpContentType = conn.getContentType();
		OutputStream receiver = response.getResponseStream(httpContentType);
		try {
			InputStream body = conn.getInputStream();
			pipe(body, receiver);
			body.close();
		} catch (IOException e) {
			throw check(e);
		}
	}

	/**
	 * Client in java.net lacks built in conversion of map to query string.
	 */
	protected String getQueryStringEncoded(Map<String, String> queryParameters)
			throws UnsupportedEncodingException {
		if (queryParameters.isEmpty()) {
			return "";
		}
		StringBuffer q = new StringBuffer();
		for (String key : queryParameters.keySet()) {
			q.append("&");
			q.append(key);
			q.append("=");
			q.append(URLEncoder.encode(queryParameters.get(key), this.encoding));
		}
		String encodedQueryString = q.substring(1);
		return encodedQueryString;
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
	
}
