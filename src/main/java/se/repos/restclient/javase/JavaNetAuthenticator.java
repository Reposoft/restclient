package se.repos.restclient.javase;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.repos.restclient.RestAuthentication;

public class JavaNetAuthenticator extends Authenticator {

	private static final Logger logger = LoggerFactory.getLogger(JavaNetAuthenticator.class);
	
	private RestAuthentication restAuthentication;

	public JavaNetAuthenticator(RestAuthentication restAuthentication) {
		this.restAuthentication = restAuthentication;
	}
	
	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		String username = restAuthentication.getUsername(null, null, null);
		logger.debug("Authenticating as user {}", username); // debug level motivated by the static config
		return new PasswordAuthentication(username, 
				restAuthentication.getPassword(null, null, null, username).toCharArray());
	}

	/**
	 * Statically enable this instance for java.net.URL connections.
	 * We can't verify that this remains the default instance.
	 * @deprecated Not only is the instance set statically, the returned credentials are cached per JVM and can not be reset.
	 */
	void activateStatic() {
		Authenticator.setDefault(this);
	}
	
}
