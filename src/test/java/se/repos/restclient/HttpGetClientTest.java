package se.repos.restclient;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.OutputStream;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.repos.restclient.HttpGetClient.Response;
import se.repos.restclient.javase.HttpGetClientJavaNet;
import se.repos.restclient.server.UnitHttpServer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Test any implementation for compliance with interface contract.
 */
public class HttpGetClientTest {

	UnitHttpServer server = null;
	
	@Before
	public void setUp() throws Exception {
		server = UnitHttpServer.create(); // select implementation to be tested
	}

	@After
	public void tearDown() throws Exception {
		server.stop(0);
	}
	
	HttpGetClient client() {
		return new HttpGetClientJavaNet();
	}

	@Test public void testGet() throws IOException {
		server.start();
		HttpGetClient client = client();
		client.read(server.getRoot() + "/a/b.txt?c=d&e=f&e=g", null, new Response() {
			@Override
			public OutputStream getResponseStream(String contentType) {
				return System.out;
			}
		});
		System.out.flush();
		assertEquals(1, server.getLog().size());
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
		HttpGetClient client = client();
		try {
			client.read(server.getRoot() + "/a/b.txt?c=d&e=f&e=g", null, null);
			fail("Should have thrown custom exception for 401 status");
		} catch (HttpStatusError e) {
			assertEquals(401, e.getHttpStatus());
		}
		assertEquals(1, server.getLog().size());
	}

}
