package se.repos.restclient;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * REST resource URL helper, toString returns RFC2396 encoded URL.
 * Helper class for {@link RestGetClient#get(URL, RestResponse)} etc.
 */
public class RestURL {
	
	/**
	 * Encoding for non-ascii characters when adding query parameters.
	 */
	public static final String PARAM_ENCODING = "UTF-8";
	
	private String url;
	private boolean hasQuery;
	
	public RestURL(String encodedUrl) {
		this(encodedUrl, new HashMap<String,String>());
	}
	
	public RestURL(String encodedUrl, Map<String,String> queryParametersNotEncoded) {
		this.url = encodedUrl;
		if (parse(url).getRef() != null) {
			throw new IllegalArgumentException("fragment identifiers not supported in this API");
		}
		this.hasQuery = encodedUrl.contains("?");
		for (Map.Entry<String, String> p : queryParametersNotEncoded.entrySet()) {
			q(p.getKey(), p.getValue());
		}
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
		url = url + (hasQuery ? '&' : '?') + paramName + "=" + v;
		hasQuery = true;
		return this;
	}
	
	/**
	 * Shorthand for {@link #addQueryParameter(String, String)}.
	 */
	public RestURL q(String paramName, String paramValueNotEncoded) {
		return this.addQueryParameter(paramName, paramValueNotEncoded);
	}
	
	protected URL parse(String url) throws IllegalArgumentException {
		try {		
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(url, e);
		}
	} 
	
	/**
	 * @return resource URL, URL-encoded
	 */
	public URL getURL() {
		return parse(url);
	}

	/**
	 * Client in java.net lacks built in conversion of map to query string.
	 */
	protected String getQueryStringEncoded(Map<String, String> queryParameters)
			throws UnsupportedEncodingException {
		if (queryParameters.isEmpty()) {
			return "";
		}
		StringBuffer q = new StringBuffer();
		for (String key : queryParameters.keySet()) {
			q.append("&");
			q.append(key);
			q.append("=");
			q.append(URLEncoder.encode(queryParameters.get(key), PARAM_ENCODING));
		}
		String encodedQueryString = q.substring(1);
		return encodedQueryString;
	}	
	
	@Override
	public String toString() {
		return url;
	}
	
	/**
	 * Shorthand for {@link #toString()}.
	 */
	public String s() {
		return this.toString();
	}
	
}
