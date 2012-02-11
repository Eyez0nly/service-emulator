/**
 * 
 */
package org.darkimport.qsle.http;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.DefaultClientConnectionOperator;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;

/**
 * @author user
 * 
 */
public class MyDefaultClientConnectionOperator extends DefaultClientConnectionOperator {
	private static final Log	log	= LogFactory.getLog(MyDefaultClientConnectionOperator.class);

	/**
	 * @param schemes
	 */
	public MyDefaultClientConnectionOperator(final SchemeRegistry schemes) {
		super(schemes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.http.impl.conn.DefaultClientConnectionOperator#resolveHostname
	 * (java.lang.String)
	 */
	@Override
	protected InetAddress[] resolveHostname(final String host) throws UnknownHostException {
		Lookup lookup;
		try {
			lookup = new Lookup(host);
			// lookup.setResolver(new ExtendedResolver(new String[] { "8.8.8.8",
			// "8.8.4.4" }));
		} catch (final TextParseException e) {
			log.warn("The host name was unable to be parsed: " + host, e);
			return super.resolveHostname(host);
		}
		final Record[] records = lookup.run();
		final List<InetAddress> resolvedHostNames = new ArrayList<InetAddress>();
		if (records != null) {
			for (final Record _record : records) {
				final ARecord record = (ARecord) _record;
				log.debug(record);
				resolvedHostNames.add(record.getAddress());
			}
		}

		return resolvedHostNames.toArray(new InetAddress[resolvedHostNames.size()]);
	}

	public static void main(final String[] args) throws UnknownHostException {
		final MyDefaultClientConnectionOperator clientConnectionOperator = new MyDefaultClientConnectionOperator(
				SchemeRegistryFactory.createDefault());
		clientConnectionOperator.resolveHostname("www.google.com");
	}
}
