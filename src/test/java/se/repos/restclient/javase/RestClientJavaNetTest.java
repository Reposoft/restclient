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
package se.repos.restclient.javase;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

public class RestClientJavaNetTest {

	/* Some actual IOExceptions that URLConnnection might throw:
	java.net.UnknownHostException: pds-suse-svn2.pdsvision.net
		at java.net.PlainSocketImpl.connect(PlainSocketImpl.java:177)
		
	java.net.ProtocolException: Server redirected too many  times (20)
		at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1323)
		
	java.io.IOException: Server returned HTTP response code: 401 for URL: http://localhost/solr/svnrev/select/?q=x
		at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1313)
		
	java.io.FileNotFoundException: http://localhost/solr/svnrev/select/?q=md5:6784f2036f0686c5662981c7f229679f
		at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1311)
		
	java.io.IOException: Server returned HTTP response code: 500 for URL: http://localhost/solr/svnrev/select/?y
		at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1313)
			 */
	
}
