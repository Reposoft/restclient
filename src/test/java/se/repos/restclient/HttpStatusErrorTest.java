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
/*
   Copyright 2009 repos.se

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package se.repos.restclient;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Test;

public class HttpStatusErrorTest {

	@Test
	public void testHttpStatusError() throws MalformedURLException {
		ResponseHeaders headers = mock(ResponseHeaders.class);
		when(headers.getStatus()).thenReturn(401);
		HttpStatusError e = new HttpStatusError("http://localhost/x/", headers, "<html/>");
		assertTrue("Should be an IOException subclass like the other errors in java.net",
				IOException.class.isAssignableFrom(e.getClass()));
		assertEquals("Message should be same as from HttpURLConnection.getInputStream",
				"Server returned HTTP response code: 401 for URL: http://localhost/x/", e.getMessage());
		assertSame(headers, e.getHeaders());
		assertEquals("Response should be the page body from server", "<html/>", e.getResponse());
	}

}
