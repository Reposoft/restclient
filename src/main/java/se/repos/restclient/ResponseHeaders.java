package se.repos.restclient;

import java.util.List;
import java.util.Map;

public interface ResponseHeaders extends Map<String, List<String>> {

	String getContentType();

	/**
	 * @return HTTP status
	 */
	int getStatus();
	
}
