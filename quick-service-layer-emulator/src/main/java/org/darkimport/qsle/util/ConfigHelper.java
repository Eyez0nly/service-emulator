/**
 * 
 */
package org.darkimport.qsle.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * For now, use file based configuration. Later updates will allow us more
 * configuration options.
 * 
 * TODO Use the Spring Resource class so that we know how to access (in/out)
 * more resource types.
 * 
 * @author user
 * 
 */
public class ConfigHelper {
	private static final String META_INF_DEFAULT_CONFIG_PROPERTIES = "/META-INF/defaultConfig.properties";
	private static final String DEFAULT_CONFIG_PROPERTIES = "config.properties";
	private static final Log log = LogFactory.getLog(ConfigHelper.class);
	private static boolean initialized = false;
	private static File configFile;
	private static Map<String, Properties> propertiesByGroup;

	public static void initialize(String configFileName) {
		// Set the configFile object appropriately. If the file does not
		// exist, throw an exception.
		File configFile = new File(configFileName);
		if (!configFile.exists()) {
			throw new RuntimeException(new FileNotFoundException(
					"Unable to locate the specified config file: "
							+ configFileName + "."));
		}
		ConfigHelper.configFile = configFile;
		Properties amalgamatedProperties = new Properties();
		InputStream configIn = null;
		try {
			configIn = new FileInputStream(configFile);
			amalgamatedProperties.load(configIn);
		} catch (Exception e) {
			log.warn("Unable to load the config file: " + configFileName + ".",
					e);
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(configIn);
		}
		propertiesByGroup = new Hashtable<String, Properties>();
		// Separate the config file by groups into a map.
		Set<Object> keys = amalgamatedProperties.keySet();
		for (Object key : keys) {
			String groupName = key.toString().split("\\.")[0];
			Properties properties = propertiesByGroup.get(groupName);
			if (properties == null) {
				properties = new Properties();
				propertiesByGroup.put(groupName, properties);
			}
			properties.setProperty(
					key.toString().substring(groupName.length() + 1),
					amalgamatedProperties.getProperty(key.toString()));
		}
		initialized = true;
	}

	/**
	 * No config file given. Take the default from the JAR if it doesn't already
	 * exist.
	 */
	public static void initialize() {
		// pull the default from the jar and put it into the base path.
		InputStream in = null;
		OutputStream out = null;

		if (!new File(DEFAULT_CONFIG_PROPERTIES).exists()) {
			try {
				in = ConfigHelper.class
						.getResourceAsStream(META_INF_DEFAULT_CONFIG_PROPERTIES);
				out = new FileOutputStream(DEFAULT_CONFIG_PROPERTIES);
				IOUtils.copy(in, out);
			} catch (Exception e) {
				log.warn("Unable to pull out the default.", e);
				throw new RuntimeException(e);
			} finally {
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(out);
			}
		}
		initialize(DEFAULT_CONFIG_PROPERTIES);
	}

	public static Properties getGroup(String groupName) {
		if (!initialized) {
			throw new IllegalStateException(
					"The config helper is not initialized.");
		}
		Properties p = propertiesByGroup.get(groupName);
		if (p == null) {
			p = new Properties();
			propertiesByGroup.put(groupName, p);
		}
		return p;
	}

	public static Map<String, Properties> getPropertiesByGroup() {
		if (!initialized) {
			throw new IllegalStateException(
					"The config helper is not initialized.");
		}
		return propertiesByGroup;
	}

	/**
	 * No-op if the config helper is initialized as read only.
	 * 
	 * @param groupName
	 * @param properties
	 */
	public static synchronized void updateGroup(String groupName,
			Properties properties) {
		if (!initialized) {
			throw new IllegalStateException(
					"The config helper is not initialized.");
		}
		propertiesByGroup.put(groupName, properties);
	}

	public static synchronized void saveConfiguration() {
		Set<String> groups = propertiesByGroup.keySet();
		Properties amalgamatedProperties = new Properties();
		for (String groupName : groups) {
			Properties groupProperties = propertiesByGroup.get(groupName);
			Set<Object> keys = groupProperties.keySet();
			for (Object key : keys) {
				StringBuffer keyBuffer = new StringBuffer(groupName)
						.append('.');
				keyBuffer.append(key.toString());
				amalgamatedProperties.setProperty(keyBuffer.toString(),
						groupProperties.getProperty(key.toString()));
			}

		}
		OutputStream out = null;
		try {
			out = new FileOutputStream(configFile);
			amalgamatedProperties.store(out, null);
		} catch (Exception e) {
			log.warn("An error occurred while saving the configuration.", e);
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	public static boolean isInitialized() {
		return initialized;
	}

}
