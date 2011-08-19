package se.repos.restclient.auth;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures client certificate connections using {@link TrustManager} and {@link KeyManager}.
 * TrustManager and KeyManager might be used for other SSL setups too,
 * but that should be another implementation of {@link RestAuthentication}
 * to allow this class to do specific validations (in the future).
 */
public class RestAuthenticationClientCert extends RestAuthenticationSimple {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private TrustManager trustManager;
	private KeyManager keyManager;
	private SSLContext sslContext;

	public RestAuthenticationClientCert(
			TrustManager trustManager, KeyManager keyManager,
			String username, String password) throws KeyManagementException {
		super(username, password);
		this.trustManager = trustManager;
		this.keyManager = keyManager;
		configure();
	}
	
	private void configure() throws KeyManagementException {
		SSLContext ctx;
		try {
			ctx = SSLContext.getInstance("TLS");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Java SSL initialization error", e);
		}
		ctx.init(new KeyManager[] {this.keyManager}, 
				new TrustManager[] {this.trustManager},
				null);
		this.sslContext = ctx;
		logger.info("Initialized SSL context using key manager {} and trust manager {}", keyManager, trustManager);
	}
	
	@Override
	public SSLSocketFactory getSSLSocketFactory(String root) {
		return sslContext.getSocketFactory();
	}
	
}