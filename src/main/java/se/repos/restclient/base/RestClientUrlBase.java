package se.repos.restclient.base;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import se.repos.restclient.HttpStatusError;
import se.repos.restclient.ResponseHeaders;
import se.repos.restclient.RestClient;
import se.repos.restclient.RestResponse;

/**
 * Helper class for implementing the single host {@link RestClient} interface 
 * using a library that operates on full URLs rather than resources at a predefined server root.
 */
public abstract class RestClientUrlBase implements RestClient {

	private String root;

	/**
	 * @param serverRootUrl protocol, domain and possibly port number, no trailing slash
	 */
	public RestClientUrlBase(String serverRootUrl) {
		if (serverRootUrl == null) {
			throw new IllegalArgumentException("Server root URL is required, this is a single host implementation");
		}
		if (serverRootUrl.length() == 0) {
			throw new IllegalArgumentException("Server root URL can not be empty");
		}
		if (serverRootUrl.endsWith("/")) {
			// Be picky as we deal with strings and URIs must start with slash according to interface
			throw new IllegalArgumentException("Server root URL must not end with slash, got " + serverRootUrl);
		}
		try {
			new URL(serverRootUrl);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Invalid server root URL " + serverRootUrl, e);
		}
		this.root = serverRootUrl;
	}
	
	@Override
	public void get(String uri, RestResponse response) throws IOException,
			HttpStatusError {
		if (!uri.startsWith("/")) {
			throw new IllegalArgumentException("URIs must be relative to server root starting with slash. Got " + uri);
		} 
		get(new URL(root + uri), response);
	}
	
	public abstract void get(URL url, RestResponse response)
			throws IOException, HttpStatusError;

	@Override
	public ResponseHeaders head(String uri) throws IOException {
		if (!uri.startsWith("/")) {
			throw new IllegalArgumentException("URIs must be relative to server root starting with slash");
		}
		return head(new URL(root + uri));
	}
	
	public abstract ResponseHeaders head(URL url) throws IOException;

}