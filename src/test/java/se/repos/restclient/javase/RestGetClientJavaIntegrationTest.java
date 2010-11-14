package se.repos.restclient.javase;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.repos.restclient.HttpStatusError;
import se.repos.restclient.ResponseHeaders;
import se.repos.restclient.RestGetClient;
import se.repos.restclient.RestClient;
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
		server = UnitHttpServer.create();
	}

	@After
	public void tearDown() throws Exception {
		server.stop(0);
	}
	
	RestClient client() {
		return new HttpGetClientJavaNet();
	}

	@Test public void testGetLog() throws IOException {
		server.start();
		RestClient client = client();
		client.get(server.getRoot() + "/a/b.txt?c=d&e=f&e=g", new RestResponse() {
			@Override
			public OutputStream getResponseStream(ResponseHeaders headers) {
				assertEquals(200, headers.getStatus());
				assertEquals("text/plain", headers.getContentType());
				return System.out;
			}
		});
		System.out.flush();
		assertEquals("should have done 1 request", 1, server.getLog().size());
		// test repeated requests
		assertEquals(200, client.head(server.getRoot() + "/").getStatus());
		assertEquals(200, client.head(server.getRoot() + "/").getStatus());
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

	@Test public void testFollowRedirect() throws IOException {
		server.createContext("/1").setHandler(new HttpHandler() {
			@Override
			public void handle(HttpExchange e) throws IOException {
				e.sendResponseHeaders(302, 0);
				e.getResponseHeaders().put("Location", Arrays.asList("/2"));
				e.close();
			}
		});
		server.createContext("/2").setHandler(new HttpHandler() {
			@Override
			public void handle(HttpExchange e) throws IOException {
				e.sendResponseHeaders(301, 0);
				e.getResponseHeaders().put("Location", Arrays.asList("/3"));
				e.close();
			}
		});
		server.createContext("/3").setHandler(new HttpHandler() {
			@Override
			public void handle(HttpExchange e) throws IOException {
				e.getResponseBody().write("yes".getBytes()); // close is not needed because stream is not wrapped
				e.close();
			}
		});
		server.start();
		RestClient client = client();
		final OutputStream out = new ByteArrayOutputStream();
		// HEAD to verify server
		assertEquals(302, client.head(server.getRoot() + "/1").getStatus());
		assertEquals(301, client.head(server.getRoot() + "/2").getStatus());
		//not sure how to write a handler for this-//assertEquals(200, client.head(server.getRoot() + "/3").getStatus());
		// GET should follow redirects
		client.get(server.getRoot() + "/1", new RestResponse() {
			@Override
			public OutputStream getResponseStream(ResponseHeaders headers) {
				return out;
			}
		});
		assertEquals("should have been redirected on both 301 and 302", "yes", out.toString());
	}	
	
	@Test public void testHead() throws IOException {
		server.createContext("/").setHandler(new HttpHandler() {
			@Override
			public void handle(HttpExchange e) throws IOException {
				e.sendResponseHeaders(302, 0);
				e.getResponseHeaders().put("Location", Arrays.asList("/start/"));
			}
		});
		server.start();
		RestClient client = client();
		ResponseHeaders head = client.head(server.getRoot() + "/");
		assertEquals("should return the status code, not follow the redirect", 302, head.getStatus());
	}
	
}
