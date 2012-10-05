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
package se.repos.restclient.auth;

import javax.net.ssl.SSLContext;

import se.repos.restclient.RestAuthentication;

/**
 * Username and password, default Java SSL setup,
 * no validation of resource URL or realm.
 */
public class RestAuthenticationSimple implements RestAuthentication {

	private String password;
	private String username;

	/**
	 * Sets up static username and password for all resources.
	 * @param username
	 * @param password
	 */
	public RestAuthenticationSimple(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	public String getUsername(String root, String resource, String realm) {
		return username;
	}

	@Override
	public String getPassword(String root, String resource, String realm,
			String username) {
		return password;
	}

	@Override
	public SSLContext getSSLContext(String root) {
		return null;
	}

}
