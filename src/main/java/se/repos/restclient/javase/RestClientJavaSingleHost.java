package se.repos.restclient.javase;

import se.repos.restclient.RestAuthentication;

/**
 * Accepts requests to a single host.
 * 
 * Currently a generic implementation but named for JavaSE's client
 * to be able to add library specific features such as connection keep-alive.
 * 
 * @deprecated This is now the default mode of operation, use {@link RestClientJavaNet}
 */
public class RestClientJavaSingleHost extends RestClientJavaNet {

	public RestClientJavaSingleHost(String serverRootUrl,
			RestAuthentication auth) {
		super(serverRootUrl, auth);
	}
	
}
