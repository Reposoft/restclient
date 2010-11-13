package se.repos.restclient.javase;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.OutputStream;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.repos.restclient.HttpStatusError;
import se.repos.restclient.ResponseHeaders;
import se.repos.restclient.RestGetClient;
import se.repos.restclient.RestResponse;
import se.repos.restclient.HttpGetClient.Response;
import se.repos.restclient.javase.HttpGetClientJavaNet;
import se.repos.restclient.server.UnitHttpServer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Test any implementation for compliance with interface contract.
 */
public class RestGetClientJavaIntegrationTest {

	UnitHttpServer server = null;
	
	@Before
	public void setUp() throws Exception {
		server = UnitHttpServer.create(); // select implementation to be tested
	}

	@After
	public void tearDown() throws Exception {
		server.stop(0);
	}
	
	RestGetClient client() {
		return new HttpGetClientJavaNet();
	}

	@Test public void testGetLog() throws IOException {
		server.start();
		RestGetClient client = client();
		client.get(server.getRoot() + "/a/b.txt?c=d&e=f&e=g", new RestResponse() {
			@Override
			public OutputStream getResponseStream(ResponseHeaders headers) {
				return System.out;
			}
		});
		System.out.flush();
		assertEquals("should have done 1 request", 1, server.getLog().size());
	}	
	
	@Test public void testAuthenticationRequired() throws IOException {
		server.createContext("/").setHandler(new HttpHandler() {
			@Override
			public void handle(HttpExchange e) throws IOException {
				e.sendResponseHeaders(401, 0);
				e.close();
			}
		});
		server.start();
		RestGetClient client = client();
		try {
			client.get(server.getRoot() + "/a/b.txt?c=d&e=f&e=g", null);
			fail("Should have thrown custom exception for 401 status");
		} catch (HttpStatusError e) {
			assertEquals(401, e.getHttpStatus());
		}
		assertEquals(1, server.getLog().size());
	}

}
