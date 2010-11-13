package se.repos.restclient.base;

import java.util.List;
import java.util.Map;

import se.repos.restclient.ResponseHeaders;

public abstract class ResponseHeadersReadOnly implements ResponseHeaders {

	@Override
	public List<String> put(String key, List<String> value) {
		throw new UnsupportedOperationException("read only");
	}

	@Override
	public List<String> remove(Object key) {
		throw new UnsupportedOperationException("read only");
	}

	@Override
	public void putAll(Map<? extends String, ? extends List<String>> m) {
		throw new UnsupportedOperationException("read only");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("read only");
	}

}
