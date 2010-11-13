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
import java.io.OutputStream;
import java.util.Map;

/**
 * Wraps the java.net APIs to provide a decent HTTP client
 * for GET requests to RESTful services without additional jars.
 * 
 * <ul>
 * <li>Provides a response interface</li>
 * <li>Avoids the IOException mess and throws meaningful exceptions</li>
 * <li>Encourages use of http content type</li>
 * </ul>
 * 
 * Does not support authentication /yet) so the calling application
 * must use {@link java.net.Authenticator} to set static credentials.
 * 
 * @deprecated Use {@link RestGetClient}
 */
public interface HttpGetClient {

	/**
	 * Requests REST servce response.
	 * 
	 * Caller may implement error handling by analyzing which subclass
	 * of IOException is thrown, including {@link HttpStatusError}.
	 * 
	 * @param encodedUri Service URL without query string, urlencoded
	 * @param queryParameters Request parameters, not urlencoded, 
	 *  null to allow parameters in URI, empty map to disallow query parameters
	 * @param response Response handler implemented by the caller
	 * @throws IOException From the hierarchy in java.net on connection errors
	 * @throws HttpStatusError If connection succeeded but HTTP status is not 200
	 */
	public void read(String encodedUri, Map<String,String> queryParameters, Response response) 
		throws IOException, HttpStatusError;
	
	/**
	 * Response API that {@link HttpGetClient} uses to communicate the http response to the caller.
	 */
	interface Response {
		
		/**
		 * Gets the stream to write output to.
		 * 
		 * Does not declare exceptions because the client can not know
		 * how to handle exceptions from the calling application.
		 * This means that exceptions must be thrown as RuntimeException
		 * if they should be passed to the http client's caller.
		 * 
		 * @param contentType The value of the content-type http header, can be ignored 
		 * @return Where contents should be written. Stream will be closed at end of response.
		 */
		OutputStream getResponseStream(String contentType);
		
	}
	
}
