package se.repos.restclient.base;

import java.io.IOException;
import java.net.URL;

import se.repos.restclient.HttpStatusError;
import se.repos.restclient.ResponseHeaders;
import se.repos.restclient.RestClient;
import se.repos.restclient.RestResponse;

/**
 * Base class for clients that operate on full URLs
 * rather than resources at a predefined server root.
 */
public abstract class RestClientMultiHostBase implements RestClient {

	private String root;

	/**
	 * @param serverRootUrl protocol, domain and possibly port number, no trailing slash
	 */
	public RestClientMultiHostBase(String serverRootUrl) {
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
