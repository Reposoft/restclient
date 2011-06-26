package se.repos.restclient;

import java.io.IOException;

public interface RestGetClient {

	/**
	 * Requests a resource, validate that HTTP status is 200,
	 * writes body to provided output stream.
	 * 
	 * Argument and connection handling is up to the implementation.
	 * For example some implementations might take a domain name in the
	 * constructor and accept only URIs from server root.
	 * 
	 * Note that this flexible API means that implementations are not required
	 * to be interchangeable. Rather it is recommended that an application sets
	 * up the behavior of the HTTP client and provides as service to its classes.
	 * for instance using dependency injection.
	 * 
	 * @param uri resource address from server root, typically an encoded URI, for conversions see {@link RestURL}
	 * @param response response expectations and handling
	 * @throws IOException From the hierarchy in java.net on connection errors
	 * @throws HttpStatusError If connection succeeded but HTTP status is not 200
	 */
	void get(String uri, RestResponse response) throws IOException, HttpStatusError;
	
}
