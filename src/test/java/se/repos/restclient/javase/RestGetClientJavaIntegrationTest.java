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
package se.repos.restclient.javase;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import se.repos.restclient.HttpStatusError;
import se.repos.restclient.ResponseHeaders;
import se.repos.restclient.RestGetClient;
import se.repos.restclient.RestClient;
import se.repos.restclient.RestResponse;
import se.repos.restclient.RestResponseBean;
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
		return new RestClientJavaNet(server.getRoot().toString(), null);
	}

	@Test public void testGet() throws IOException {
		server.start();
		RestClient client = client();
		client.get("/a/b.txt?c=d&e=f&e=g", new RestResponse() {
			@Override
			public OutputStream getResponseStream(ResponseHeaders headers) {
				assertEquals(200, headers.getStatus());
				assertEquals("text/plain", headers.getContentType());
				return System.out;
			}
		});
		System.out.flush();
		assertEquals("should have done 1 request", 1, server.getLog().size());
	}

	@Test public void testGetServerError() {
		final String body = "<html><body>\n<h1>Server error</h1><p>This error occurred</p></body></html>";
		server.createContext("/").setHandler(new HttpHandler() {
			@Override
			public void handle(HttpExchange e) throws IOException {
				e.sendResponseHeaders(500, 0);
				OutputStream out = e.getResponseBody();
				out.write(body.getBytes());
				out.close();
				e.close();
			}
		});
		server.start();
		RestClient client = client();
		RestResponseBean response = new RestResponseBean();
		
		try {
			client.get("/", response);
			fail("Should throw status error on 500");
		} catch (HttpStatusError e) {
			assertEquals(500, e.getHttpStatus());
			assertEquals(body, e.getResponse());
		} catch (IOException e1) {
			fail("Should throw status error not IOException");
		}
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
		ResponseHeaders head = client.head("/");
		assertEquals("should return the status code, not follow the redirect", 302, head.getStatus());
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
			client.get("/a/b.txt?c=d&e=f&e=g", null);
			fail("Should have thrown custom exception for 401 status");
		} catch (HttpStatusError e) {
			assertEquals(401, e.getHttpStatus());
		}
		assertEquals(1, server.getLog().size());
	}

	@Test public void testHeadAccessDenied() throws IOException {
		server.createContext("/").setHandler(new HttpHandler() {
			@Override
			public void handle(HttpExchange e) throws IOException {
				e.sendResponseHeaders(403, 0);
				e.close();
			}
		});
		server.start();		
		assertEquals(403, client().head("/").getStatus());
	}	
	

	@Test public void testGetAfterHead() throws IOException {
		server.start();
		RestClient client = client();
		assertEquals(200, client.head("/").getStatus());
		RestResponseBean r2 = new RestResponseBean();
		try {
			client.get("/", r2);
			assertEquals(200, r2.getHeaders().getStatus());
		} catch (HttpStatusError e) {
			fail("GET after HEAD failed: " + e);
		}
	}
	

	@Test public void testHeadRepeated() throws IOException {
		server.start();
		RestClient client = client();
		assertEquals("first HEAD ", 200, client.head("/").getStatus());
		assertEquals("second HEAD", 200, client.head("/").getStatus());
		assertEquals("third HEAD ", 200, client.head("/").getStatus());
	}
	

	@Test public void testFollowRedirect() throws IOException {
		server.createContext("/1").setHandler(new HttpHandler() {
			@Override
			public void handle(HttpExchange e) throws IOException {
				e.getResponseHeaders().put("Location", Arrays.asList("/2")); // Relative URL.
				e.sendResponseHeaders(302, 0); // Must set responseheaders before sending response code.
				e.close();
			}
		});
		server.createContext("/2").setHandler(new HttpHandler() {
			@Override
			public void handle(HttpExchange e) throws IOException {
				e.getResponseHeaders().put("Location", Arrays.asList(server.getRoot() + "/3")); // Absolute URL.
				e.sendResponseHeaders(301, 0);
				e.close();
			}
		});
		server.createContext("/3").setHandler(new HttpHandler() {
			@Override
			public void handle(HttpExchange e) throws IOException {
				e.getResponseHeaders().set("Content-Type", "text/plain");
				e.sendResponseHeaders(200, 0);
				e.getResponseBody().write("yes".getBytes()); // close is not needed because stream is not wrapped
				e.close();
			}
		});
		server.start();
		RestClient client = client();
		final OutputStream out = new ByteArrayOutputStream();
		// GET should follow redirects
		client.get("/1", new RestResponse() {
			@Override
			public OutputStream getResponseStream(ResponseHeaders headers) {
				return out;
			}
		});
		assertEquals("should have been redirected on both 301 and 302", "yes", out.toString());
	}
	
	@Test public void testFollowRedirectHttps() throws IOException {
		// Java will not follow redirect across protocols.
		// Could be a future feature of restclient to transparently follow redirect from http to https (not the other direction).
		server.createContext("/1").setHandler(new HttpHandler() {
			@Override
			public void handle(HttpExchange e) throws IOException {
				e.getResponseHeaders().put("Location", Arrays.asList(new URL("https", server.getRoot().getHost(), server.getRoot().getPort(), "/2").toString()));
				e.sendResponseHeaders(302, 0); // Must set responseheaders before sending response code.
				e.close();
			}
		});
		server.createContext("/2").setHandler(new HttpHandler() {
			@Override
			public void handle(HttpExchange e) throws IOException {
				e.getResponseHeaders().set("Content-Type", "text/plain");
				e.sendResponseHeaders(200, 0);
				e.getResponseBody().write("yes".getBytes()); // close is not needed because stream is not wrapped
				e.close();
			}
		});
		server.start();
		RestClient client = client();
		final OutputStream out = new ByteArrayOutputStream();
		// GET should not follow redirect to HTTPS.
		try {
			client.get("/1", new RestResponse() {
				@Override
				public OutputStream getResponseStream(ResponseHeaders headers) {
					return out;
				}
			});
			fail("should not redirect to HTTPS");
		} catch (HttpStatusError e) {
			assertEquals("", 302, e.getHttpStatus());
		}
	}
	
}
