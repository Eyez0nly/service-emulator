/**
 * 
 */
package org.darkimport.qsle.bundles;

import java.util.Map;

import org.darkimport.qsle.util.ConfigHelper;

/**
 * @author user
 * 
 */
public class Bundle {
	private ConfigHelper				configHelper;
	private BundleLoader				bundleLoader;
	private Map<String, Conversation>	conversations;
}
