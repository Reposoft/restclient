package se.repos.restclient;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

public class RestURLTest {

	@Test
	public void testRestURLString() {
		RestURL u = new RestURL("http://www.repos.se/?a=b");
		assertEquals("http://www.repos.se/?a=b", u.toString());
		assertEquals("shorthand", "http://www.repos.se/?a=b", u.s());
		try {
			assertEquals(new URL("http://www.repos.se/?a=b"), u.getURL());
		} catch (MalformedURLException e) { fail("" + e); }
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
		System.out.println(q);
		assertTrue(q.contains("key=val"));
		assertTrue("Both types of space encoding are ok", q.contains("a+b") || q.contains("a%20b"));
		assertTrue("Encoding should be UTF-8", q.contains("t%C3%A5t"));
		// keys should never be encoded, if they need to it is the web service that should be changed
		assertTrue("Should not encode keys", q.contains("list[]=1"));
	}

}
