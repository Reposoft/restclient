package se.repos.restclient;

import java.io.OutputStream;

public interface RestResponse {

	/**
	 * Gets the stream to write output to.
	 * 
	 * Does not declare exceptions because the client can not know
	 * how to handle exceptions from the calling application.
	 * This means that exceptions must be thrown as RuntimeException
	 * if they should be passed to the http client's caller.
	 * 
	 * @param headers The response headers from the server 
	 * @return Where contents should be written. Stream will be closed at end of response.
	 */
	OutputStream getResponseStream(ResponseHeaders headers);	
	
}
