package se.repos.restclient.javase;

import java.net.HttpURLConnection;
import se.repos.restclient.base.ResponseHeadersMap;

class URLConnectionResponseHeaders extends ResponseHeadersMap {

	private String contentType;

	public URLConnectionResponseHeaders(HttpURLConnection con) {
		super(con.getHeaderFields());
		this.contentType = con.getContentType();
	}

	@Override
	public String getContentType() {
		return contentType;
	}

}
