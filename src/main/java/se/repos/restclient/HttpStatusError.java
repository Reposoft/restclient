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
import java.net.URL;

/**
 * Error thrown if we got a connection but the server returned a non-200 status code.
 * Looks like the IOException that HttpURLConnection.getInputStream throws
 * but provides access to the status code.
 */
public class HttpStatusError extends IOException {

	private static final long serialVersionUID = 1L;
	private int status;
	private URL url;

	/**
	 * @param httpStatus <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">status code</a> from the server
	 * @param cause from the underlying http client
	 */
	public HttpStatusError(int httpStatus, URL url) {
		super("Server returned HTTP response code: " + httpStatus + " for URL: " + url);
		this.status = httpStatus;
		this.url = url;
	}

	/**
	 * @return HTTP status code, see constants in 
	 */
	public int getHttpStatus() {
		return status;
	}
	
	/**
	 * @return The URL used to make the connection
	 */
	public URL getUrl() {
		return this.url;
	}
	
}
