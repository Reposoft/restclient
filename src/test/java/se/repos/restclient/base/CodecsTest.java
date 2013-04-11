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

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;

import org.junit.Test;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.util.SVNBase64;

public class CodecsTest {

	@Test
	public void testBase64encode() {
		assertEquals("YXBhOmJlcGE=", Codecs.base64encode("apa:bepa"));
	}

	@Test
	public void testBase64decode() {
		assertEquals("apa:bepa", Codecs.base64decode("YXBhOmJlcGE="));
	}
	
	@Test
	public void testBehaveLikeSvnkit() throws UnsupportedEncodingException {
		DAVRepositoryFactory.setup(); // not sure what would set the system property
		String charset = System.getProperty("svnkit.http.encoding", "UTF-8");
		
		String auth = "nåt:lösen";
		String byteArrayToBase64 = SVNBase64.byteArrayToBase64(auth.getBytes(charset));
		assertEquals("Should encode non-asclii like svnkit does", byteArrayToBase64, Codecs.base64encode(auth));
	}

}
