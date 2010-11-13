package se.repos.restclient.javase;

import java.io.IOException;

import se.repos.restclient.HttpStatusError;
import se.repos.restclient.ResponseHeaders;
import se.repos.restclient.RestGetClient;
import se.repos.restclient.RestClient;
import se.repos.restclient.RestResponse;

/**
 * Accepts requests to a single host.
 * 
 * Currently a generic implementation but named for JavaSE's client
 * to be able to add library specific features such as connection keep-alive.
 */
public class RestGetClientJavaSingleHost implements RestClient {

	private RestClient client;
	private String host;

	/**
	 * @param serverRootUrl protocol, domain and possibly port number, no trailing slash
	 */
	public RestGetClientJavaSingleHost(String serverRootUrl) {
		this.client = new HttpGetClientJavaNet();
		this.host = serverRootUrl;
	}

	@Override
	public void get(String uri, RestResponse response) throws IOException, HttpStatusError {
		if (!uri.startsWith("/")) {
			throw new IllegalArgumentException("URIs must be relative to server root starting with slash");
		} 
		client.get(host + uri, response);
	}

	@Override
	public ResponseHeaders head(String uri) throws IOException {
		return client.head(host + uri);
	}
	
}
