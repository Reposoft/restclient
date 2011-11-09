package se.repos.restclient.javase;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.junit.Test;

import se.repos.restclient.HttpStatusError;
import se.repos.restclient.RestAuthentication;
import se.repos.restclient.RestResponse;

public class RestClientJavaNetTest {

	@Test
	public void testCallConfigure() throws HttpStatusError, IOException {
		RestClientJavaNet c = new RestClientJavaNet("http://localhost", null) {
			@Override
			protected void configure(HttpURLConnection conn) {
				throw new RuntimeException("Well done " + 
						(conn.getInstanceFollowRedirects() ? "R" : "N")); // should happen, and the test should be aborted
			}
		};
		try {
			c.get("/", mock(RestResponse.class));
			fail("Should have called configure");
		} catch (RuntimeException e) {
			assertTrue("Should have called configure at get", e.getMessage().startsWith("Well done"));
			assertEquals("GET should follow redirects", "Well done R", e.getMessage());
		}
		try {
			c.head("/");
			fail("Should have called configure");
		} catch (RuntimeException e) {
			assertTrue("Should have called configure at head", e.getMessage().startsWith("Well done"));
			assertEquals("HEAD should not follow redirects", "Well done N", e.getMessage());
		}
	}

	@Test
	public void testConfigure() {
		RestClientJavaNet c = new RestClientJavaNet("http://localhost", null);
		HttpURLConnection h = mock(HttpURLConnection.class);
		c.configure(h);
		verify(h).setConnectTimeout(RestClientJavaNet.DEFAULT_CONNECT_TIMEOUT);
	}

	@Test
	public void testConfigureSSL() throws MalformedURLException, NoSuchAlgorithmException, KeyManagementException {
		RestAuthentication a = mock(RestAuthentication.class);
		RestClientJavaNet c = new RestClientJavaNet("https://localhost:1443", a);
		
		SSLContext s = SSLContext.getInstance("TLS");
		s.init(new KeyManager[]{}, new TrustManager[]{}, new SecureRandom());
		
		when(a.getSSLContext("https://localhost:1443")).thenReturn(s);
		
		HttpsURLConnection h = mock(HttpsURLConnection.class);
		when(h.getURL()).thenReturn(new URL("https://localhost:1443"));
		c.configure(h);
		// difficult to verify factory instance
		verify(h).setSSLSocketFactory((SSLSocketFactory) any());
		
		when(a.getSSLContext("https://somethingelse:1443")).thenReturn(null);
		when(h.getURL()).thenReturn(new URL("https://somethingelse:1443"));
		// Shouldn't set context when not given one
		verify(h, times(1)).setSSLSocketFactory((SSLSocketFactory) any());
	}

}
