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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ResponseHeadersMap extends ResponseHeadersReadOnly {

	private Map<String, List<String>> map;

	public ResponseHeadersMap(Map<String, List<String>> map) {
		this.map = map;
	}
	
	@Override
	public abstract int getStatus();
	
	@Override
	public abstract String getContentType();
	
	@Override
	public String toString() {
		String separator = ", ";
		StringBuffer h = new StringBuffer();
		for (Map.Entry<String, List<String>> e : this.entrySet()) {
			for (String v : e.getValue()) {
				h.append(separator);
				if (e.getKey() != null) {
					h.append(e.getKey()).append(": ");
				}
				h.append(v);
			}
		}
		if (h.length() > separator.length()) return h.substring(separator.length()); 
		return "(empty headers)";
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	@Override
	public List<String> get(Object key) {
		return map.get(key);
	}

	@Override
	public Set<String> keySet() {
		return map.keySet();
	}

	@Override
	public Collection<List<String>> values() {
		return map.values();
	}

	@Override
	public Set<java.util.Map.Entry<String, List<String>>> entrySet() {
		return map.entrySet();
	}
	
}
