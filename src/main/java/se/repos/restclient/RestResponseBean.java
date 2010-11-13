package se.repos.restclient;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class RestResponseBean implements RestResponse {

	private static final String DEFAULT_BODY_ENCODING = "UTF-8";
	
	private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	private ResponseHeaders headers;
	
	@Override
	public OutputStream getResponseStream(ResponseHeaders headers) {
		this.setHeaders(headers);
		return buffer;
	}

	protected void setHeaders(ResponseHeaders headers) {
		this.headers = headers;
	}

	public ResponseHeaders getHeaders() {
		return headers;
	}
	
	public String getBody() {
		return getBody(DEFAULT_BODY_ENCODING);
	}

	public String getBody(String encoding) {
		try {
			return new String(buffer.toByteArray(), encoding);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Invalid encoding identfier: " + encoding, e);
		}
	}

	/**
	 * @return page body using default encoding
	 */
	@Override
	public String toString() {
		return getBody();
	}
	
	
}
