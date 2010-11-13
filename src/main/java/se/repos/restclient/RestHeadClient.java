package se.repos.restclient;

import java.io.IOException;

public interface RestHeadClient {

	/**
	 * 
	 * @param uri
	 * @return
	 * @throws IOException From the hierarchy in java.net on connection errors
	 */
	ResponseHeaders head(String uri) throws IOException;
	
}
