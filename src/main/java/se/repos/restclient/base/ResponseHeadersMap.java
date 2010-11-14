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
	public String getContentType() {
		if (true) throw new UnsupportedOperationException("should probably be abstract");
		// TODO use this logic for getValue/getString method
		List<String> v = get("Content-Type");
		if (v == null || v.size() == 0) {
			return null;
		}
		return v.get(0);
	}
	
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
