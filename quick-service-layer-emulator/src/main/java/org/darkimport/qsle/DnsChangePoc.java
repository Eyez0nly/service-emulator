/**
 * 
 */
package org.darkimport.qsle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.darkimport.qsle.http.MyThreadSafeClientConnManager;

/**
 * @author user
 * 
 */
public class DnsChangePoc {
	private static final Log	log	= LogFactory.getLog(DnsChangePoc.class);

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		final MyThreadSafeClientConnManager clientConnManager = new MyThreadSafeClientConnManager();
		final HttpClient httpclient = new DefaultHttpClient(clientConnManager);
		try {
			final HttpGet httpget = new HttpGet("http://www.google.com/");

			System.out.println("executing request " + httpget.getURI());

			// Create a response handler
			final ResponseHandler<String> responseHandler = new BasicResponseHandler();

			final String responseBody = httpclient.execute(httpget, responseHandler);
			System.out.println("----------------------------------------");
			System.out.println(responseBody);
			System.out.println("----------------------------------------");

		} catch (final Exception e) {
			log.warn("Error.", e);
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
		}
		// First, make sure that the hosts file redirects our test host to
		// localHost
		// Next, try to connect to our test host. This should fail.
		URL url;
		InputStream in;
		ByteArrayOutputStream out;
		try {
			url = new URL("http://www.google.com/");
			in = IOUtils.toBufferedInputStream(url.openStream());
			out = new ByteArrayOutputStream();
			IOUtils.copy(in, out);
			log.info(new String(out.toByteArray()));
		} catch (final MalformedURLException e) {
			log.warn("The URL is malformed.", e);
		} catch (final IOException e) {
			log.warn("At this point, we expect the operation to fail.", e);
		}

		final Field[] fields = ClassLoader.class.getDeclaredFields();

		for (final Field field : fields) {
			try {
				field.setAccessible(true);
			} catch (final Exception e) {
				log.warn("Can't change accessibility of " + field.getName(), e);
			}
			try {
				final Object value = field.get(null);
				log.info(field.getName() + ": " + field.getType().toString() + ": "
						+ (value != null ? value.toString() : "null"));
			} catch (final IllegalArgumentException e) {
				log.warn("Passed the wrong number of parameters to get " + field.getName());
			} catch (final IllegalAccessException e) {
				log.warn("Don't have the authorization to get " + field.getName());
			} catch (final Exception e) {
				log.warn("Some other problem getting " + field.getName());
			}
		}
		// Override system DNS setting with Google free DNS server
		System.setProperty("sun.net.spi.nameservice.nameservers", "8.8.8.8,8.8.4.4");
		System.setProperty("sun.net.spi.nameservice.provider.1", "dns,sun");

		// Try connecting again. This should succeed.
		try {
			url = new URL("http://www.google.com/");
			in = IOUtils.toBufferedInputStream(url.openStream());
			out = new ByteArrayOutputStream();
			IOUtils.copy(in, out);
			log.info(new String(out.toByteArray()));
		} catch (final MalformedURLException e) {
			log.warn("The URL is malformed.", e);
		} catch (final IOException e) {
			log.warn("At this point, it should be unexpected if the operation failed.", e);
		}
	}

}
