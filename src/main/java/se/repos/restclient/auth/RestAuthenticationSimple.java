package se.repos.restclient.auth;

import javax.net.ssl.SSLSocketFactory;

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
	public SSLSocketFactory getSSLSocketFactory(String root) {
		return null;
	}

}
