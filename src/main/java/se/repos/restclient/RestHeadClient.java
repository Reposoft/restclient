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

import java.io.IOException;

public interface RestHeadClient {

	/**
	 * Performs a HEAD request, possibly without authentication.
	 * Note that behavior when used in combination with Repos Authproxy is not fully defined.
	 * @param uri Resource URL from server root, encoded
	 * @return headers, can be 401 if RestAuthentication does not provide a username
	 * @throws IOException From the hierarchy in java.net on connection errors
	 */
	ResponseHeaders head(String uri) throws IOException;
	
}
