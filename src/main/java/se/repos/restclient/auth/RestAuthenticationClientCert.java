/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.repos.restclient.auth;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
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
	
	private static String sslProtocol = "TLS";

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
			logger.debug("Initializing SSL context with SSL protocol: {}", sslProtocol);
			ctx = SSLContext.getInstance(sslProtocol);
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
	public SSLContext getSSLContext(String root) {
		return sslContext;
	}
	
	public static void setSSLProtocol(String sslProtocol) {
		RestAuthenticationClientCert.sslProtocol = sslProtocol;
	}
}
