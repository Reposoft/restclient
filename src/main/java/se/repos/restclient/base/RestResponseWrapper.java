package se.repos.restclient.base;

import java.io.OutputStream;

import se.repos.restclient.HttpGetClient;
import se.repos.restclient.ResponseHeaders;
import se.repos.restclient.RestResponse;

public class RestResponseWrapper implements RestResponse {

	private RestResponse response;

	protected RestResponseWrapper(RestResponse response) {
		this.response = response;
	}
	
	/**
	 * @deprecated
	 */
	protected RestResponseWrapper(final HttpGetClient.Response r) {
		this(new RestResponse() {
			@Override
			public OutputStream getResponseStream(ResponseHeaders headers) {
				return r.getResponseStream(headers.getContentType());
			}
		});
	}
	
	@Override
	public OutputStream getResponseStream(ResponseHeaders headers) {
		return response.getResponseStream(headers);
	}

}
