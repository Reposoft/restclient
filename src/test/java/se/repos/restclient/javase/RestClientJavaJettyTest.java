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
import org.junit.Test;

import se.repos.restclient.HttpStatusError;
import se.repos.restclient.ResponseHeaders;
import se.repos.restclient.RestAuthentication;
import se.repos.restclient.RestClient;
import se.repos.restclient.RestResponse;
import se.repos.restclient.RestResponseBean;


public class RestClientJavaJettyTest {

	@Test public void testGet() throws Exception {
		Server server = new Server(49999); // TOOD random retry like UnitHttpServer
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
		
		RestClient client = new RestClientJavaNet("http://127.0.0.1:49999", null);
		
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
		
		server.stop();
	
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
		int port = 49999; // TODO random test port
        Server server = new Server(port);
 
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
        	new RestClientJavaNet("http://localhost:" + port, null).get("/", resp);
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
		} finally {
        	server.stop();
        }
	}
	
	@Test
	public void testAuthenticator() throws Exception {
		int port = 49999; // TODO random test port		
		
		Server server = new Server(port);
		   
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
					response.addHeader("WWW-Authenticate", "Basic realm=\"test\"");
					response.sendError(401);
					return;
				}
			}
		}), "/*");
		
		server.start();
		
		RestAuthentication auth = mock(RestAuthentication.class);
		// TODO verify realm
		when(auth.getUsername(null, null, null)).thenReturn("demo").thenReturn("admin");
		when(auth.getPassword(null, null, null, "demo")).thenReturn("pdemo");
		when(auth.getPassword(null, null, null, "admin")).thenReturn("padmin");
		
		RestClient client = new RestClientJavaNet("http://localhost:" + port, auth);
		
		RestResponse response = new RestResponseBean();
		client.get("/something", response);
		assertEquals("First request should be without credentials", null, authHeaders.get(0));
		assertEquals("Should have authenticated", 2, authHeaders.size());
		client.get("/something", response);
		// It is OK if second request authenticates immediately, as long as the Authenticator instance is asked for credentials
		assertTrue("Should have authenticated again", 3 <= authHeaders.size());
		assertTrue("Should have checked for authentication again instead of just sending it", 4 == authHeaders.size());
		assertTrue("Should be different users in the two authentications", 
				!authHeaders.get(1).equals(authHeaders.get(authHeaders.size() - 1)));
		// TODO maybe we should test for preemptive sending of auth the second time _if_ username is unchanged,
		//  that would probably save some requests and still be thread-safe
	}
	
}
