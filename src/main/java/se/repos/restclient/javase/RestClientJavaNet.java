package se.repos.restclient.javase;

import se.repos.restclient.RestAuthentication;

/**
 * The new name for {@link HttpGetClientJavaNet}.
 */
public class RestClientJavaNet extends HttpGetClientJavaNet {

	public RestClientJavaNet(String serverRootUrl, RestAuthentication auth) {
		super(serverRootUrl);
		if (auth != null) {
			throw new UnsupportedOperationException("auth not supported yet");
		}
	}
	
}
