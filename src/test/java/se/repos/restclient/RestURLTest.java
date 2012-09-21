package se.repos.restclient;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class RestURLTest {

	@Test
	public void testJavaURLpartial() {
		try {
			new URL("/a%20b/c/?d=e");
			fail("Expected java.net.URL to throw exception on server-relative URL");
		} catch (MalformedURLException e) {
			// expected
		}
		URI i = null;
		try {
			i = new URI("/a%20b/c/?d=e");
		} catch (URISyntaxException e) {
			fail("java.net.URI should support server-relative URL");
		}
		assertEquals("/a b/c/", i.getPath());
		assertEquals("/a%20b/c/", i.getRawPath());
		assertEquals("d=e", i.getQuery());
	}
	
	@Test
	public void testRestURLStringFull() {
		RestURL u = new RestURL("http://www.repos.se/?a=b");
		assertEquals("http://www.repos.se/?a=b", u.toString());
		assertEquals("shorthand", "http://www.repos.se/?a=b", u.s());
		assertEquals("server-relative", "/?a=b", u.toStringPart());
		assertEquals("shorthand server-relative", "/?a=b", u.p());
		try {
			assertEquals(new URI("http://www.repos.se/?a=b"), u.getURI());
		} catch (URISyntaxException e) {
			fail("" + e);
		}
		assertEquals("http://www.repos.se", u.toStringRoot());
		assertEquals("http://www.repos.se", u.r());
	}

	@Test
	public void testRestURLStringPart() throws URISyntaxException {
		RestURL u = new RestURL("/a%20b/?x=y%20y&z=1&z=2");
		assertEquals("/a%20b/?x=y%20y&z=1&z=2", u.toString());
		assertEquals("shorthand", "/a%20b/?x=y%20y&z=1&z=2", u.s());
		assertEquals("server-relative", "/a%20b/?x=y%20y&z=1&z=2", u.toStringPart());
		assertEquals("shorthand server-relative", "/a%20b/?x=y%20y&z=1&z=2", u.p());
		try {
			u.getURL();
			fail("Expecting getURL to fail when there's no server root");
		} catch (Exception e) {
			// expected
		}
		assertEquals(new URI("/a%20b/?x=y%20y&z=1&z=2"), u.getURI());
		Map<String, List<String>> q = u.getQuery();
		assertEquals("Two query param keys", 2, q.size());
		assertEquals("y y", q.get("x").get(0));
		assertEquals("To string should be the param value", "y y", "" + q.get("x"));
		assertEquals("Two values for the same param name", 2, q.get("z").size());
		assertEquals("Should maintain ordering", "1", q.get("z").get(0));
		assertEquals("2", q.get("z").get(1));
		assertEquals("toString should comma separate without spaces", "1,2", "" + q.get("z"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testRestURLPartNotAbsolute() {
		new RestURL("x/");
	}
	
	@Test
	public void testRestURLWithFragment() {
		// this is not really a requirement but it makes handling of query string easier
		try {
			new RestURL("http://www.repos.se/#id");
			fail("fragment identifier not useful in REST service consumer because it won't reach the server anyway");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}
	
	@Test
	public void testRestURLStringValidateAtConstruction() {
		try {
			new RestURL("://www.repos.se/");
			fail("Should validate the URL immediately upon construction");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testRestURLWithParams() {
		Map<String,String> p = new LinkedHashMap<String, String>();
		p.put("k", "v");
		p.put("r", "s");
		assertEquals("http://www.repos.se/?k=v&r=s", new RestURL("http://www.repos.se/", p).s());
	}

	@Test
	public void testEquals() {
		
	}
	
	@Test
	public void testQ() {
		// chaining
		assertEquals("http://x.se/?k=&r=v", new RestURL("http://x.se/").q("k", "").q("r", "v").s());
		// query already in constructor string
		assertEquals("http://x.se/?k=&r=v", new RestURL("http://x.se/?k=").q("r", "v").s()); 
		// query already set as map in constructor
		Map<String,String> p = new HashMap<String, String>();
		p.put("k", "");
		assertEquals("http://x.se/?k=&r=v", new RestURL("http://x.se/", p).q("r", "v").s());
		// encoding of for example space
		
		// validation of parameter name? validation not in scope for this class
		
		// encoding of charset-dependent characters
		
	}
	
	@Test public void test1() {
		@SuppressWarnings("serial")
		Map<String, String> params = new HashMap<String, String>() {{ 
			put("key", "val");
			put("space", "a b");
			put("swe", "tåt");
			put("list[]", "1");
		}};
		
		RestURL url = new RestURL("http://somehost/r/a.txt", params);
		
		String q = url.toString();
		assertTrue(q.contains("key=val"));
		assertTrue("Both types of space encoding are ok", q.contains("a+b") || q.contains("a%20b"));
		assertTrue("Encoding should be UTF-8", q.contains("t%C3%A5t"));
		// keys should never be encoded, if they need to it is the web service that should be changed
		assertTrue("Should not encode keys", q.contains("list[]=1"));
	}
	
	@Test
	public void testAppendSameParam() {
		RestURL url = new RestURL("/?a=b&c=d");
		url.q("a", "e f").q("a", "3");
		assertEquals("/?a=b&c=d&a=e%20f&a=3", url.toString());
		Map<String, List<String>> p = url.getQuery();
		assertEquals(2, p.size());
		assertEquals("b,e f,3", "" + p.get("a"));
	}

}
