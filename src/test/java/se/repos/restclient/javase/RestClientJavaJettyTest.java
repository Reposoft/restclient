package se.repos.restclient.javase;

import static org.junit.Assert.*;

import org.eclipse.jetty.embedded.HelloHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.junit.Test;

import se.repos.restclient.ResponseHeaders;
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
		
		//server.setThreadPool(new QueuedThreadPool(2));
		
	server.setStopAtShutdown(true);
		server.start();
		//wait until server closed://server.join();
		
	//try {
		RestClient client = new RestGetClientJavaSingleHost("http://127.0.0.1:49999");
		
		RestResponseBean r1 = new RestResponseBean();
		client.get("/", r1);
		assertTrue("Got: " + r1.getBody(), r1.getBody().contains("<h1>repos</h1>"));
		
		// repeated HEAD requests
		ResponseHeaders head = client.head("/");
		assertEquals(200, head.getStatus());
		assertEquals("text/html;charset=UTF-8", head.getContentType());
		assertEquals(200, client.head("/whatever").getStatus());
		
		// GET after HEAD
		RestResponseBean r2 = new RestResponseBean();
		//hangs if preceded by a HEAD request//client.get("/", r2);
		assertEquals("text/html;charset=UTF-8", r2.getHeaders().getContentType());
		
	//} finally {
		server.stop();
	//}
	
	}
	
}
