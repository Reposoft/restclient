package se.repos.restclient;

/**
 * For requests that make use of the 
 * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.1">Accept header</a>.
 */
public interface RestResponseAccept extends RestResponse {
	
	/**
	 * Specifies for example a single mime type or a comma separated list with quality factors.
	 * @return the value of the Accept header.
	 */
	String getAccept();
	
}
