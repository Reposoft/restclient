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
package se.repos.restclient.base;

import java.util.Base64;

/**
 * Encode and decode strings to base64 without external libraries.
 * Uses Java 8+ Base64 class.
 */
public abstract class Codecs {

	public static String base64encode(String decoded) {
		return Base64.getEncoder().encodeToString(decoded.getBytes());
	}
	
	public static String base64decode(String encoded) {
		byte[] b = Base64.getDecoder().decode(encoded);
		return new String(b);
	}
	
}
