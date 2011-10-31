package se.repos.restclient.base;

import static org.junit.Assert.*;

import org.junit.Test;

public class CodecsTest {

	@Test
	public void testBase64encode() {
		assertEquals("YXBhOmJlcGE=", Codecs.base64encode("apa:bepa"));
	}

	@Test
	public void testBase64decode() {
		assertEquals("apa:bepa", Codecs.base64decode("YXBhOmJlcGE="));
	}

}
