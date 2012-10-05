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
