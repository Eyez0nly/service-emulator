/**
 * 
 */
package org.darkimport.qsle.bundles;

import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

/**
 * Handles the extraction of bundles AND the creation of bundles.
 * 
 * Bundle files are zip files containing the elements necessary to instruct the
 * application how to emulate a service or services.
 * 
 * The root of the bundle contains the config.properties for the bundle (see
 * CONFIG.PROPERTIES for details) that defines the services that will be
 * activated when the user "Starts" the bundle.
 * 
 * The root also contains a SCRIPT file (see SCRIPT for details) that contians
 * instructions about the order in which services and any external programs will
 * be started.
 * 
 * The root may also contain a bundleLoader.properties (see
 * BUNDLELOADER.PROPERTIES) file that contains information on which
 * {@link BundleLoader} to use. Specified bundles that are not amongst the
 * defaults registered in the registry.properties file must extend
 * ExternalBundleLoader. Additionally, the root of the bundle must also contain
 * an identifying certificate.
 * 
 * If repeaters are used, the root should also contain a conversation file for
 * each of the conversations (see CONVERSATIONS) that need to be repeated by the
 * configured repeaters. There is a correspondence between the names of the
 * conversation files and the conversationNames property used in
 * config.properties.
 * 
 * Bundle files may be encrypted (see ENCRYPTED-BUNDLE). If the file is
 * encrypted, have your key ready when you load the bundle.
 * 
 * @author user
 * 
 */
public class BundleHandler {
	public static Bundle loadBundle(final InputStream bundleInput) throws Exception {
		return loadBundle(bundleInput, false);
	}

	public static Bundle loadBundle(final String fileName) throws Exception {
		FileInputStream bundleInput = null;
		try {
			bundleInput = new FileInputStream(fileName);
			return loadBundle(bundleInput, false);
		} finally {
			IOUtils.closeQuietly(bundleInput);
		}
	}

	public static Bundle loadBundle(final InputStream bundleInput, final boolean isEncrypted) throws Exception {
		// TODO
		return null;
	}
}
