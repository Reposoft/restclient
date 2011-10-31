package se.repos.restclient.javase;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.PasswordAuthentication;

import org.junit.Test;

import se.repos.restclient.RestAuthentication;

public class JavaNetAuthenticatorTest {

	@Test
	public void testGetPasswordAuthentication() {
		RestAuthentication ra = mock(RestAuthentication.class);
		when(ra.getUsername(null, null, null)).thenReturn("u1").thenReturn("u2");
		when(ra.getPassword(null, null, null, "u1")).thenReturn("p1");
		when(ra.getPassword(null, null, null, "u2")).thenReturn("p2");
		JavaNetAuthenticator auth = new JavaNetAuthenticator(ra);
		PasswordAuthentication c1 = auth.getPasswordAuthentication();
		assertEquals("u1", c1.getUserName());
		assertEquals('1', c1.getPassword()[1]);
		PasswordAuthentication c2 = auth.getPasswordAuthentication();
		assertEquals("u2", c2.getUserName());
		assertEquals('2', c2.getPassword()[1]);
	}

}
