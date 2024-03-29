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
package se.repos.restclient.server;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

// TODO: Consider HttpTestServer used by Java 11 httpclient, see http://hg.openjdk.java.net/jdk/sandbox/rev/aa677dd4c174

/**
 * HTTP server suitable for unit testing
 * and simple embedded services.
 * 
 * The only serious disadvantage of java's {@link HttpHandler} API 
 * in the targeted use case, compared to the servlet API,
 * is that there is no access to decoded query parameters.
 * The closest is {@link HttpExchange#getRequestURI()}, {@link URI#getQuery()}.
 */
public class UnitHttpServer {
	
	private static final Logger logger = LoggerFactory.getLogger(UnitHttpServer.class);
	
	// See also https://src.springframework.org/svn/spring-maintenance/trunk/tiger/src/org/springframework/remoting/support/SimpleHttpServerFactoryBean.java

	private static final int PORT_RETRIES = 10;
	private static final int PORT_MIN = 33000;
	private static final int PORT_MAX = 64000;
	private static final int BIND_BACKLOG = 0;

	/**
	 * @see HttpServer#create()
	 */
	public static UnitHttpServer create() throws IOException {
		HttpServer sun = HttpServer.create();
		return new UnitHttpServer(sun);
	}

	private HttpServer server;
	private LogFilter logFilter;
	private MemoryLogFilter memoryLogFilter;
	
	private InetSocketAddress address = null;
	private boolean hasContext = false;
	private boolean started = false;
	
	protected UnitHttpServer(HttpServer server) {
		this.server = server;
		this.logFilter = new LogFilter();
		this.memoryLogFilter = new MemoryLogFilter();
		this.bindRandom();
	}

	/**
	 * @see HttpServer#createContext(String)
	 */
	public HttpContext createContext(String path) {
		HttpContext context = this.server.createContext(path);
		context.getFilters().add(memoryLogFilter);
		context.getFilters().add(logFilter);
		this.hasContext = true;
		return context;
	}

	/**
	 * @see HttpServer#start()
	 */	
	public void start() {
		if (!this.hasContext) {
			createContext("/").setHandler(new DefaultHandler());
		}
		this.started  = true;
		this.server.start();
	}
	
	/**
	 * @see HttpServer#stop(int)
	 */
	public void stop(int delay) {
		this.server.stop(delay);
	}
	
	/**
	 * Used to get current port number and to create test URLs.
	 * @return server root URL without trailing slash
	 * @throws IllegalArgumentException if the server has not been started, to avoid a gotcha
	 */
	public URL getRoot() {
		if (!this.started) {
			throw new IllegalArgumentException("Server not started yet");
		}
		try {
			return new URL("http", address.getHostName(), address.getPort(), "");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @return reference to the current request log, mutable
	 */
	public Queue<HttpExchange> getLog() {
		return memoryLogFilter.log;
	}
	
	/**
	 * Test a few randomly selected ports to find a free one for this server instance.
	 */
	protected void bindRandom() {
		int retries = 0;
		while (address == null && retries++ < PORT_RETRIES) {
			int port = PORT_MIN + (int) (Math.random() * (PORT_MAX - PORT_MIN));
			try {
				server.bind(new InetSocketAddress(port), BIND_BACKLOG); 
				address = server.getAddress();
				logger.debug("Server online at {}", address);
			} catch (BindException e) {
				if (retries == PORT_RETRIES) {
					throw new RuntimeException("Failed to set up server", e);
				}
				System.err.println("Bind to port " + port + " failed. Retrying.");
			} catch (IOException e) {
				throw new RuntimeException("HttpServer error", e);
			}
		}
	}
	
	protected class MemoryLogFilter extends Filter {
		Queue<HttpExchange> log = new LinkedList<HttpExchange>();
		@Override
		public String description() {
			return "Logs request parameters in memory";
		}
		@Override
		public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
			log.add(exchange);
			chain.doFilter(exchange);
		}
	}
	
	protected class LogFilter extends Filter {
		@Override
		public String description() {
			return "Logs requests using logging frameworkd";
		}
		@Override
		public void doFilter(HttpExchange e, Chain chain) throws IOException {
			chain.doFilter(e);
			logger.info("{} {} {} {}", new Object[] {e.getRequestMethod(), 
					e.getRemoteAddress(), e.getResponseCode(), e.getRequestURI()});
		}
	}
	
	static class DefaultHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange e) throws IOException {
			e.getResponseHeaders().set("Content-Type", "text/plain");
			e.getResponseHeaders().set("X-Test", "testing");
			e.sendResponseHeaders(200, 0);
			
			OutputStreamWriter body = new OutputStreamWriter(e.getResponseBody());
			body.write(e.getRequestMethod() + "\n");
			for (Map.Entry<String,List<String>> h : e.getRequestHeaders().entrySet()) {
				body.write(h.getKey() + "=" + h.getValue() + "\n");
			}
			body.write(e.getRequestURI().getQuery() + "\n"); // TODO parse to map?
			body.close(); // not needed if writing to the output stream directly			
			
			e.close();
		}
	}
	
}
