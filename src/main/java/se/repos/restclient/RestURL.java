package se.repos.restclient;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * REST resource URL helper, utility for using the String-only {@link RestClient} methods.
 * 
 * We keep conversions to/from the single string passed to {@link RestClient#get(String, RestResponse)}
 * and {@link RestClient#head(String)} here because of the design goal to make mocking of RestClient easy.
 * With a minimum of methods it is easy to create implementations.
 * 
 * This class supports both full URLs and resource URI from server root
 * (the latter supporting the default behavior of RestClient).
 * For single-host client implementations use {@link #toStringPart()}. 
 */
public class RestURL {
	
	/**
	 * Encoding for non-ascii characters when adding query parameters.
	 */
	public static final String PARAM_ENCODING = "UTF-8";
	
	private String root = null;
	private String rest;
	private boolean hasQuery;
	
	public RestURL(String encodedUri) {
		this(encodedUri, new HashMap<String,String>());
	}
	
	/**
	 * @param encodedUri Full URL or absolute URI from server root starting with slash, URL-encoded
	 * @param queryParametersNotEncoded Query parameters, to have multiple params with the same name use {@link #q(String, String)},
	 * to get predictable order in query string use for example {@link LinkedHashMap}.
	 */
	public RestURL(String encodedUri, Map<String,String> queryParametersNotEncoded) {
		URI uri = parse(encodedUri);
		if (uri.getFragment() != null) {
			throw new IllegalArgumentException("fragment identifiers not supported in this API");
		}
		if (uri.getHost() != null) {
			this.root = encodedUri.substring(0, encodedUri.indexOf('/', uri.getScheme().length() + 3));
			this.rest = encodedUri.substring(this.root.length());
		} else {
			if ('/' != encodedUri.charAt(0)) {
				throw new IllegalArgumentException("URI must be absolute from server root");
			}
			this.rest = encodedUri;
		}
		this.hasQuery = encodedUri.contains("?");
		for (Map.Entry<String, String> p : queryParametersNotEncoded.entrySet()) {
			q(p.getKey(), p.getValue());
		}
	}
	
	/**
	 * @return Query string without the leasing question mark
	 */
	public String getQueryString() {
		return getURI().getRawQuery();
	}
	
	/**
	 * 
	 * @return The value list's toString returns comma separated list of values with no whitespaces
	 *  meaning that the common case, a single parameter per key, is retrieved using <code>"" + get(key)</code>
	 */
	public Map<String, List<String>> getQuery() {
		return getQueryParams(getQueryString());
	}
	
	/**
	 * Adds a query parameter to the URL.
	 * To avoid encoding of the value as {@value #PARAM_ENCODING} use
	 * constructor {@link #RestURL(String)}.
	 * 
	 * @param paramName
	 * @param paramValueNotEncoded
	 * @return this instance for chaining
	 */
	public RestURL addQueryParameter(String paramName, String paramValueNotEncoded) {
		String v;
		try {
			v = URLEncoder.encode(paramValueNotEncoded, PARAM_ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unexpected JVM behavior. Encoding: " + PARAM_ENCODING);
		}
		rest = rest + (hasQuery ? '&' : '?') + paramName + "=" + v;
		hasQuery = true;
		return this;
	}
	
	/**
	 * Shorthand for {@link #addQueryParameter(String, String)}.
	 */
	public RestURL q(String paramName, String paramValueNotEncoded) {
		return this.addQueryParameter(paramName, paramValueNotEncoded);
	}
	
	protected URI parse(String uri) throws IllegalArgumentException {
		try {
			return new URI(uri);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(uri, e);
		}
	}
	
	protected URL parseURL(String url) throws IllegalArgumentException {
		try {		
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(url, e);
		}
	} 
	
	public URI getURI() {
		return parse(toString());
	}
	
	/**
	 * @return resource URL, URL-encoded
	 * @deprecated Use {@link #getURI()} as it supports partial URLs
	 * @throws IllegalArgumentException if the URL has no protocol://server
	 */
	public URL getURL() {
		if (root == null) {
			throw new IllegalArgumentException("This RestURL has no server specified and can therefore not be converted to URL");
		}
		return parseURL(root + rest);
	}
	
	@Override
	public String toString() {
		return root == null ? rest : root + rest;
	}
	
	public String toStringPart() {
		return rest;
	}
	
	/**
	 * Shorthand for {@link #toString()}, i.e. the full URL if available.
	 */
	public String s() {
		return this.toString();
	}	

	/**
	 * Shorthand for {@link #toStringPart()}.
	 */
	public String p() {
		return toStringPart();
	}
	
	/**
	 * Parse query string. For serializing, see {@link ParamMap#toString()}.
	 */
	protected Map<String, List<String>> getQueryParams(String queryStringEncoded) {
		ParamMap p = new ParamMap();
		String rq = queryStringEncoded;
		if (rq == null) {
			return p;
		}
		String[] split = rq.split("&");
		for (String s : split) {
			if (s.length() == 0) {
				throw new IllegalArgumentException("Found empty query string part in " + rq);
			}
			int n = s.indexOf('=');
			if (n < 1) {
				throw new IllegalArgumentException("Query string part could not be parsed as key=value: " + s);
			}
			try {
				p.add(s.substring(0, n), URLDecoder.decode(s.substring(n + 1), PARAM_ENCODING));
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("Unexpected error for static encoding " + PARAM_ENCODING, e);
			}
		}
		return p;
	}
	
	public class ParamMap extends LinkedHashMap<String, List<String>> {
		
		private static final long serialVersionUID = 1L;

		public void add(String key, String valueDecoded) {
			if (containsKey(key)) {
				get(key).add(valueDecoded);
			} else {
				put(key, new ParamValues(valueDecoded));
			}
		}
		
		@Override
		public String toString() {
			if (this.isEmpty()) {
				return "";
			}
			StringBuffer q = new StringBuffer();
			for (String key : this.keySet()) {
				for (String v : this.get(key)) {
					q.append("&");
					q.append(key);
					q.append("=");
					try {
						q.append(URLEncoder.encode(v, PARAM_ENCODING));
					} catch (UnsupportedEncodingException e) {
						throw new RuntimeException("Unexpected error for static encoding " + PARAM_ENCODING, e);
					}
				}
			}
			String encodedQueryString = q.substring(1);
			return encodedQueryString;	
		}
		
	}
	
	public class ParamValues extends LinkedList<String> {
		
		private static final long serialVersionUID = 1L;

		ParamValues(String value) {
			super();
			add(value);
		}

		@Override
		public String toString() {
			if (size() == 0) {
				return "";
			}
			if (size() == 1) {
				return get(0);
			}
			StringBuffer b = new StringBuffer();
			for (String v : this) {
				b.append(',').append(v);
			}
			return b.substring(1);
		}
		
	}
	
}
