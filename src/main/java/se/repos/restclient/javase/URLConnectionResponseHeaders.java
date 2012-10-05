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
