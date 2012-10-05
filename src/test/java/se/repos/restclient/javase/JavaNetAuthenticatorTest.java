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
