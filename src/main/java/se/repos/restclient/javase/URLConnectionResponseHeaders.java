package se.repos.restclient.javase;

import java.io.IOException;
import java.net.HttpURLConnection;
import se.repos.restclient.base.ResponseHeadersMap;

class URLConnectionResponseHeaders extends ResponseHeadersMap {

	private String contentType;
	private int status;

	public URLConnectionResponseHeaders(HttpURLConnection con) throws IOException {
		super(con.getHeaderFields());
		try {
			this.status = con.getResponseCode();
		} catch (IOException e) {
			throw e;
		}
		this.contentType = con.getContentType();
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public int getStatus() {
		return status;
	}

}
