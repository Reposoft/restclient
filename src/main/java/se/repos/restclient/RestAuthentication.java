package se.repos.restclient;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

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
	 * For SSL connections.
	 * @param root protocol, host and possibly port number for the server to connect to
	 * @return trust manager
	 */
	public TrustManager getTrustManager(String root);
	
	/**
	 * For SSL connections.
	 * @param root protocol, host and possibly port number for the server to connect to
	 * @return trust manager
	 */
	public KeyManager getKeyManager(String root);
	
}
