/*
   Copyright 2009 repos.se

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package se.repos.restclient.javase;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import org.junit.Test;

import se.repos.restclient.HttpGetClient;
import se.repos.restclient.HttpStatusError;
import se.repos.restclient.HttpGetClient.Response;

public class HttpGetClientJavaNetTest {

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
	public void testUrl() throws IOException {
		final Response r = mock(Response.class);
		final List<URL> urls = new LinkedList<URL>();
		HttpGetClientJavaNet client = new HttpGetClientJavaNet("http://localhost") {
			@Override
			public void read(URL url, Response response) throws IOException,
					HttpStatusError {
				urls.add(url);
				assertEquals(r, response);
			}
		};
		
		@SuppressWarnings("serial")
		Map<String, String> params = new HashMap<String, String>() {{ 
			put("key", "val");
			put("space", "a b");
			put("swe", "tåt");
			put("list[]", "1");
		}};
		
		client.read("/r/a.txt", params, r);
		assertEquals(1, urls.size());
		assertEquals("/r/a.txt", urls.get(0).getPath());
		String q = urls.get(0).getQuery();
		System.out.println(q);
		assertTrue(q.contains("key=val"));
		assertTrue("Both types of space encoding are ok", q.contains("a+b") || q.contains("a%20b"));
		assertTrue("Encoding should be UTF-8", q.contains("t%C3%A5t"));
		// keys should never be encoded, if they need to it is the web service that should be changed
		assertTrue("Should not encode keys", q.contains("list[]=1"));
	}
	
	@Test
	public void testNoQueryString() throws HttpStatusError, IOException {
		final Response r = mock(Response.class);
		final List<URL> urls = new LinkedList<URL>();
		HttpGetClientJavaNet client = new HttpGetClientJavaNet("http://localhost") {
			@Override
			public void read(URL url, Response response) throws IOException,
					HttpStatusError {
				urls.add(url);
				assertEquals(r, response);
			}
		};
		
		Map<String, String> params = new HashMap<String, String>();
		
		client.read("/r/a.txt", params, r);
		assertEquals(1, urls.size());
		assertEquals("/r/a.txt", urls.get(0).getPath());
		assertEquals(null, urls.get(0).getQuery());
	}

}
