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

/**
 * Encode and decode strings to base64 without external libraries.
 * Works in most Java6 environments and falls back to commons-codec if not.
 */
public abstract class Codecs {

	public static String base64encode(String decoded) {
		try {
			Class<?> c = Class.forName("javax.xml.bind.DatatypeConverter");
			if (c != null) {
				return (String) c.getMethod("printBase64Binary", byte[].class).invoke(null, decoded.getBytes());
			}
		} catch (Exception e) {
			e.printStackTrace();
			// continue
		}
		// We'll probably end up here in java < 1.6
		try {
			Class<?> c = Class.forName("org.apache.commons.codec.binary.Base64");
			if (c != null) {
				return (String) c.getMethod("encodeBase64", byte[].class).invoke(null, decoded.getBytes());
			}
		} catch (Exception e2) {
			// continue
		}
		throw new RuntimeException("Failed to find a base64 encoder. Try adding commons-codec lib.");
	}
	
	public static String base64decode(String encoded) {
		try {
			Class<?> c = Class.forName("javax.xml.bind.DatatypeConverter");
			if (c != null) {
				byte[] b = (byte[]) c.getMethod("parseBase64Binary", String.class).invoke(null, encoded);
				return new String(b);
			}
		} catch (Exception e) {
			// continue
		}
		// We'll probably end up here in java < 1.6
		try {
			Class<?> c = Class.forName("org.apache.commons.codec.binary.Base64");
			if (c != null) {
				byte[] b = (byte[]) c.getMethod("decodeBase64", String.class).invoke(null, encoded);
				return new String(b);
			}
		} catch (Exception e2) {
			// continue
		}
		throw new RuntimeException("Failed to find a base64 decoder. Try adding commons-codec lib.");
	}
	
}
