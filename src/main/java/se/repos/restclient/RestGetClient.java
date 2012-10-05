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

import java.io.IOException;

public interface RestGetClient {

	/**
	 * Requests a resource, validate that HTTP status is 200,
	 * writes body to provided output stream.
	 * 
	 * Argument and connection handling is up to the implementation.
	 * For example some implementations might take a domain name in the
	 * constructor and accept only URIs from server root.
	 * 
	 * Note that this flexible API means that implementations are not required
	 * to be interchangeable. Rather it is recommended that an application sets
	 * up the behavior of the HTTP client and provides as service to its classes.
	 * for instance using dependency injection.
	 * 
	 * @param uri resource address from server root, typically an encoded URI, for conversions see {@link RestURL}
	 * @param response response expectations and handling
	 * @throws IOException From the hierarchy in java.net on connection errors
	 * @throws HttpStatusError If connection succeeded but HTTP status is not 200
	 */
	void get(String uri, RestResponse response) throws IOException, HttpStatusError;
	
}
