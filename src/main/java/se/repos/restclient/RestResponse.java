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
package se.repos.restclient;

import java.io.OutputStream;

public interface RestResponse {

	/**
	 * Gets the stream to write output to.
	 * 
	 * Does not declare exceptions because the client can not know
	 * how to handle exceptions from the calling application.
	 * This means that exceptions must be thrown as RuntimeException
	 * if they should be passed to the http client's caller.
	 * 
	 * @param headers The response headers from the server 
	 * @return Where contents should be written. Stream will be closed at end of response.
	 */
	OutputStream getResponseStream(ResponseHeaders headers);	
	
}
