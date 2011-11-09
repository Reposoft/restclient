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
