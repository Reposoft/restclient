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

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public interface RestAuthentication {

	/**
	 * Called to get username when server prompts for authentication.
	 * @param root protocol, host and possibly port number, no trailing slash
	 * @param resource resource from server root starting with slash, query string but no fragment (fragments make no sense in REST)
	 * @param realm realm as specified by the server
	 * @return The username to authenticat with
	 */
	public String getUsername(String root, String resource, String realm);
	
	/**
	 * Called to get password when server prompts for authentication.
	 * @param root protocol, host and possibly port number, no trailing slash
	 * @param resource resource from server root starting with slash, query string but no fragment (fragments make no sense in REST)
	 * @param realm realm as specified by the server
	 * @param username the username that will be used for authentication
	 * @return The username to authenticat with
	 */	
	public String getPassword(String root, String resource, String realm, String username);
	
	/**
	 * Customizes SSLContext that can deliver {@link SSLSocketFactory} for HTTPS connections.
	 * Called per connection so initialization should be cached.
	 * @param root root protocol, host and possibly port number for the server to connect to
	 * @return Custom SSL setup, null to use Java's default one
	 */
	public SSLContext getSSLContext(String root);
	
}
