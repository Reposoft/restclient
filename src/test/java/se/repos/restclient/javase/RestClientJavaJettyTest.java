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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.embedded.HelloHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.repos.restclient.HttpStatusError;
import se.repos.restclient.ResponseHeaders;
import se.repos.restclient.RestAuthentication;
import se.repos.restclient.RestClient;
import se.repos.restclient.RestResponse;
import se.repos.restclient.RestResponseBean;


public class RestClientJavaJettyTest {

	int port = 49999; // TODO random test port
	private Server server;
	
	@Before
	public void setUp() throws Exception {
        server = new Server(port); // TODO random retry like UnitHttpServer
	}

	@After
	public void tearDown() throws Exception {
		server.stop();
	}
	
	
	@Test 
	public void testGet() throws Exception { 
//		Server server = new Server();
//		
//        SelectChannelConnector c1 = new SelectChannelConnector();
//        c1.setHost("127.0.0.1");
//        c1.setPort(49999);
//        c1.setThreadPool(new QueuedThreadPool(2));
//        c1.setName("test");
//
//		server.setConnectors(new Connector[] {c1});
		
		server.setHandler(new HelloHandler("repos", "restclient"));
		
		server.setStopAtShutdown(true);
		server.start();
		//wait until server closed://server.join();
		
		RestClient client = new RestClientJavaHttp("http://127.0.0.1:49999", null);
		
		RestResponseBean r1 = new RestResponseBean();
		client.get("/", r1);
		assertTrue("Got: " + r1.getBody(), r1.getBody().contains("<h1>repos</h1>"));
		
		// repeated HEAD requests
		boolean tryHead = false; // RestGetClientJava is known to hang when running GET after HEAD
		if (tryHead) {
			ResponseHeaders head = client.head("/");
			assertEquals(200, head.getStatus());
			assertEquals("text/html;charset=UTF-8", head.getContentType());
			assertEquals(200, client.head("/whatever").getStatus());
			assertEquals(200, client.head("/whatever2").getStatus());
		} else {
			client.get("/whatever", new RestResponseBean());
			client.get("/whatever2", new RestResponseBean());
		}
		
		// GET after HEAD (or GET after GET)
		RestResponseBean r2 = new RestResponseBean();
		client.get("/whatever2", r2);
		assertEquals("text/html;charset=UTF-8", r2.getHeaders().getContentType());
			
	}
	
	/* Some actual IOExceptions that URLConnnection might throw:
	java.net.UnknownHostException: pds-suse-svn2.pdsvision.net
		at java.net.PlainSocketImpl.connect(PlainSocketImpl.java:177)
		
	java.net.ProtocolException: Server redirected too many  times (20)
		at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1323)
		
	java.io.IOException: Server returned HTTP response code: 401 for URL: http://localhost/solr/svnrev/select/?q=x
		at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1313)
		
	java.io.FileNotFoundException: http://localhost/solr/svnrev/select/?q=md5:6784f2036f0686c5662981c7f229679f
		at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1311)
		
	java.io.IOException: Server returned HTTP response code: 500 for URL: http://localhost/solr/svnrev/select/?y
		at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1313)
			 */
	
	@Test
	public void testGet401() throws Exception {
 
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        
        context.addServlet(new ServletHolder(new HttpServlet() {
			private static final long serialVersionUID = 1L;
			@Override
			protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
					throws ServletException, IOException {
				resp.sendError(401, "Can this be custom?");
			}
		}), "/*");
        
        server.start();
        
        try {
        	RestResponse resp = new RestResponseBean();
        	new RestClientJavaHttp("http://localhost:" + port, null).get("/", resp);
        	fail("Expected status error");
        } catch (HttpStatusError e) {
        	assertEquals(401, e.getHttpStatus());
        	ResponseHeaders headers = e.getHeaders();
        	assertNotNull("Should contain HTTP headers sent", headers);
        	assertTrue(headers.size() > 0);
        	assertTrue(e.getResponse().contains("Can this be custom?")); // assuming jetty writes an error page body
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException("Error not handled", e);
		} 
	}
	
	
	@Test
	public void testAuthenticatorForced() throws Exception {
		   
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		
		final List<String> authHeaders = new LinkedList<String>();
		context.addServlet(new ServletHolder(new HttpServlet() {
			private static final long serialVersionUID = 1L;
			@Override
			protected void doGet(HttpServletRequest request, HttpServletResponse response) 
					throws ServletException, IOException {
				String authHeader = request.getHeader("Authorization");
				authHeaders.add(authHeader);
				if (authHeader == null) {
					response.sendError(403);
					return;
				}
			}
		}), "/*");
		
		server.start();
		
		RestAuthentication auth = mock(RestAuthentication.class);
		// TODO verify realm
		when(auth.getUsername(null, null, null)).thenReturn("demo").thenReturn("demo").thenReturn("admin").thenReturn("admin");
		when(auth.getPassword(null, null, null, "demo")).thenReturn("pdemo");
		when(auth.getPassword(null, null, null, "admin")).thenReturn("padmin");
		
		RestClient client = new RestClientJavaHttp("http://localhost:" + port, auth);
		((RestClientJavaHttp) client).setAuthenticationForced(true);
		
		RestResponse response = new RestResponseBean();
		client.get("/something", response);
		assertNotNull("First request should have credentials", authHeaders.get(0));
		//assertEquals("", authHeaders.get(0));
		client.get("/something", response);
		assertEquals("Should have authenticated again", 2, authHeaders.size());
		assertTrue("Should be different users in the two authentications", 
				!authHeaders.get(0).equals(authHeaders.get(1)));
		
	}
	
}
