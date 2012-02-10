/**
 * 
 */
package org.darkimport.qsle.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ipv4 compatible only.
 * 
 * The adding and deleting of routes requires privilege escalation. To that end,
 * this class uses the escala
 * 
 * @author user
 * 
 */
public class RoutesHelper {
	private static final Log	log					= LogFactory.getLog(RoutesHelper.class);
	public static final String	SELF				= "COMPUTERNAME";
	private static final String	DEFAULT_NET_MASK	= "255.255.255.255";

	public static boolean addRoute(final String routeFromIp) {
		return addRoute(routeFromIp, getMyIp(), DEFAULT_NET_MASK);
	}

	public static boolean addRoute(final String routeFromIp, final String routeToIp) {
		return addRoute(routeFromIp, routeToIp, DEFAULT_NET_MASK);
	}

	/**
	 * Adds the specified route to the routing table (windows XP+ only). First
	 * checks that the given route does not already exist. Then, tries to add
	 * the route.
	 * 
	 * @param routeFromIp
	 * @param routeToIp
	 * @param subNetMask
	 * @return true if the route was added successfully or if the route already
	 *         exists. Otherwise false.
	 */
	public static boolean addRoute(final String routeFromIp, final String routeToIp, final String subNetMask) {
		final List<RouteEntry> routeEntries = findRoutes(routeFromIp);
		final boolean routeExists = routeExists(routeFromIp, routeToIp, subNetMask, routeEntries);
		if (routeExists) {
			return true;
		}

		// If we get here, the route needs to be added.
		EscalationHelper.escalate(true, true, "route.exe", "add", routeFromIp, "mask", subNetMask, routeToIp, "metric",
				"1");

		return routeExists(routeFromIp, routeToIp, subNetMask, routeEntries);
	}

	/**
	 * @param routeFromIp
	 * @param routeToIp
	 * @param subNetMask
	 * @param routeEntries
	 */
	private static boolean routeExists(final String routeFromIp, final String routeToIp, final String subNetMask,
			final List<RouteEntry> routeEntries) {
		if (routeEntries.size() != 0) {
			// Check if the entry already exists
			for (final RouteEntry routeEntry : routeEntries) {
				if (routeEntry.getInterfaceAddress().equals(routeToIp) && routeEntry.getNetMask().equals(subNetMask)
						&& routeEntry.getNetworkDestination().equals(routeFromIp)) {
					// Entry exists. No need to add again.
					log.debug("The specified route exists: " + routeEntry);
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Gets the IP address associated with the PC running this process. Assumes
	 * that the COMPUTERNAME environment variable is set.
	 * 
	 * @return
	 */
	public static String getMyIp() {
		String myIp;
		final String computerName = System.getenv(SELF);
		InetAddress thisComputer;
		byte[] address;

		// get the bytes of the IP address
		try {
			thisComputer = InetAddress.getByName(computerName);
			address = thisComputer.getAddress();

			if (isHostname(computerName)) {
				final StringBuffer myIpBuffer = new StringBuffer();
				// Print the IP address
				for (int i = 0; i < address.length; i++) {
					final int unsignedByte = address[i] < 0 ? address[i] + 256 : address[i];
					myIpBuffer.append(unsignedByte);
					if (i < address.length - 1) {
						myIpBuffer.append(".");
					}
				}
				myIp = myIpBuffer.toString();
			} else { // this is an IP address
				myIp = thisComputer.toString();
			}
		} catch (final UnknownHostException ue) {
			log.warn("Cannot find host " + computerName);
			myIp = null;
		}

		return myIp;
	}

	private static boolean isHostname(final String s) {

		final char[] ca = s.toCharArray();
		// if we see a character that is neither a digit nor a period
		// then s is probably a hostname
		for (int i = 0; i < ca.length; i++) {
			if (!Character.isDigit(ca[i])) {
				if (ca[i] != '.') {
					return true;
				}
			}
		}

		// Everything was either a digit or a period
		// so s looks like an IP address in dotted quad format
		return false;

	}

	/**
	 * Deletes the specified routes from the routing table. Then verifies that
	 * the entries are gone.
	 * 
	 * @param pattern
	 * @return true if the deletion was successful or if no action was required.
	 *         Otherwise false.
	 */
	public static boolean deleteRoute(final String pattern) {
		List<RouteEntry> routeEntries = findRoutes(pattern);
		if (routeEntries.size() == 0) {
			return true;
		}

		EscalationHelper.escalate(true, true, "route.exe", "delete", pattern);
		routeEntries = findRoutes(pattern);
		if (routeEntries.size() == 0) {
			return true;
		}

		return false;
	}

	/**
	 * Returns a list of {@link RouteEntry}s that match the specified pattern.
	 * If the pattern is null or an empty string, all entries are returned.
	 * 
	 * If pattern contains a * or ?, it is treated as a shell pattern, and only
	 * matching destination routes are printed. The '*' matches any string, and
	 * '?' matches any one char. Examples: 157.*.1, 157.*, 127.*, *224*.
	 * 
	 * @param pattern
	 * @return null if there was a problem executing the route command.
	 *         Otherwise return a list of {@link RouteEntry}s
	 */
	public static List<RouteEntry> findRoutes(final String pattern) {
		final List<String> arguments = new ArrayList<String>();
		arguments.add("route.exe");
		arguments.add("print");
		arguments.add("-4");
		if (!StringUtils.isEmpty(pattern)) {
			arguments.add(pattern);
		}
		final ProcessBuilder builder = new ProcessBuilder(arguments);
		Process p;
		try {
			p = builder.start();
		} catch (final IOException e) {
			log.warn("Unable to execute the route print process.", e);
			return null;
		}
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		InputStream in = null;
		try {
			in = IOUtils.toBufferedInputStream(p.getInputStream());
			IOUtils.copy(in, out);
		} catch (final IOException e) {
			log.warn("Unable to read the output from the route print command.", e);
			return null;
		} finally {
			IOUtils.closeQuietly(in);
		}

		final List<RouteEntry> routeEntries = new ArrayList<RouteEntry>();
		try {
			final LineIterator lineIterator = IOUtils.lineIterator(new ByteArrayInputStream(out.toByteArray()), null);
			boolean foundRoutes = false;
			while (lineIterator.hasNext()) {
				final String rawLine = lineIterator.next();
				if (!foundRoutes) {
					if (rawLine.indexOf("Network Destination") == 0) {
						foundRoutes = true;
					}
				} else {
					if (rawLine.startsWith("=")) { // The line that starts with
													// '=' marks the end of the
													// routes
						break;
					}
					final String[] lineParts = StringUtils.trim(rawLine).split("\\s+");
					if (lineParts.length != 5) {
						throw new RuntimeException("An error occurred parsing the route, "
								+ Arrays.asList(lineParts).toString());
					}
					final RouteEntry routeEntry = new RouteEntry();

					// 0 is networkDestination
					routeEntry.setNetworkDestination(lineParts[0]);

					// 1 is netMask
					routeEntry.setNetMask(lineParts[1]);

					// 2 is gateWay
					routeEntry.setGateway(lineParts[2]);

					// 3 is interface
					routeEntry.setInterfaceAddress(lineParts[3]);

					// 4 is metric
					routeEntry.setMetric(lineParts[4]);

					log.debug("Configured routeEntry: " + routeEntry);
					routeEntries.add(routeEntry);
				}
			}
		} catch (final IOException e) {
			log.fatal(
					"If you are seeing this message, then the impossible happened: a ByteArrayInputStream threw an IOException.",
					e);
		}

		// TODO
		return routeEntries;
	}

	// public static void main(final String[] args) throws IOException {
	// RoutesHelper.addRoute("184.173.115.42", getMyIp(), "255.255.255.255");
	// RoutesHelper.deleteRoute("184.173.115.42");

	// final ProcessBuilder builder = new ProcessBuilder("elevate.cmd",
	// "cmd.exe", "/C", "route.exe", "add",
	// "184.173.115.42", "mask", "255.255.255.255", "192.168.1.22",
	// "metric", "1");
	// final Process p = builder.start();
	//
	// final InputStream in = p.getInputStream();
	// final ByteArrayOutputStream out = new ByteArrayOutputStream();
	// IOUtils.copy(in, out);
	// System.out.println(new String(out.toByteArray()));
	// }
}
