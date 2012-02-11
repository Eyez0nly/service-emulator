/**
 * 
 */
package org.darkimport.qsle.util;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

/**
 * @author user
 * 
 */
public class TestRoutesHelper {
	private static final Log	log	= LogFactory.getLog(TestRoutesHelper.class);

	/**
	 * Test method for {@link org.darkimport.qsle.util.RoutesHelper#getMyIp()}.
	 */
	@Test
	public final void testGetMyIp() {
		try {
			// First get output from native nslookup using the COMPUTERNAME as
			// an
			// argument.
			// The IP address should be contained therein.
			final String computerName = System.getenv(RoutesHelper.SELF);
			final ProcessBuilder builder = new ProcessBuilder("nslookup", computerName);
			final Process p = builder.start();

			final InputStream in = p.getInputStream();
			final BufferedReader bIn = new BufferedReader(new InputStreamReader(in));
			final StringBuffer output = new StringBuffer();
			String line = bIn.readLine();
			while (line != null) {
				output.append(line);
				line = bIn.readLine();
			}

			// Call getMyIp. If the returned IP address is contained in the
			// nslookup
			// output string, then we have passed.
			final String ipAddress = RoutesHelper.getMyIp();
			log.debug("IP address: " + ipAddress);
			log.debug("nslookup output: " + output.toString());
			assertTrue(output.indexOf(ipAddress) != -1);
		} catch (final Exception e) {
			log.warn("An error occurred.", e);
			throw new RuntimeException(e);
		}
	}
}
