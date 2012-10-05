/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
