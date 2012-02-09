/**
 * 
 */
package org.darkimport.qsle.services;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.darkimport.qsle.util.ConfigHelper;

/**
 * @author user
 * 
 */
public class StartStoppableConfigWatcher implements StartStoppable, Runnable {
	private static final Log log = LogFactory
			.getLog(StartStoppableConfigWatcher.class);

	private StartStoppable backingStartStoppable;
	private String groupName;
	private boolean running;

	public StartStoppableConfigWatcher(StartStoppable backingStartStoppable,
			String groupName) {
		this.backingStartStoppable = backingStartStoppable;
		this.groupName = groupName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.darkimport.qsle.services.StartStoppable#start(java.util.Properties
	 * )
	 */
	public void start(Properties properties) throws Exception {
		backingStartStoppable.start(properties);
		new Thread(this).start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.darkimport.qsle.services.StartStoppable#stop()
	 */
	public void stop() {
		running = false;
		backingStartStoppable.stop();
	}

	/**
	 * Returns the isStarted state of the
	 * {@link StartStoppableConfigWatcher#backingStartStoppable} regardless of
	 * the state of the config watcher thread (e.g. if the config watcher thread
	 * has crashed).
	 */
	public boolean isStarted() {
		return backingStartStoppable.isStarted();
	}

	public void run() {
		running = true;
		while (running) {
			// using the groupName + "*" as a pattern for a filter, search
			// for files matching the pattern in the temp dir.
			IOFileFilter wildCardFileFilter = new WildcardFileFilter(groupName + "*");
			final Collection<File> updatedConfigFiles = FileUtils.listFiles(
					new File(System.getProperty("java.io.tmpdir")),
					wildCardFileFilter, null);
			if (updatedConfigFiles != null && updatedConfigFiles.size() > 0) {

				// if we find some files, we expect them to be named
				// groupname
				// + timeStamp (timeStamp is a date in long (the primitive)
				// format).
				File candidateConfigFile = null;
				long latestTimeStamp = 0;
				for (File file : updatedConfigFiles) {
					String fileName = file.getName();
					long timeStamp = new Long(fileName.substring(groupName
							.length()));
					// the file with the latest timestamp is considered.
					if (timeStamp > latestTimeStamp) {
						candidateConfigFile = file;
						latestTimeStamp = timeStamp;
					}
				}

				// Load
				// it as
				// a properties.
				Properties properties = new Properties();
				InputStream in = null;
				try {
					in = FileUtils.openInputStream(candidateConfigFile);
					properties.load(in);
				} catch (Exception e) {
					log.warn(
							"Ran into an error opening the updated config values. Trying again later.",
							e);
					continue;
				} finally {
					IOUtils.closeQuietly(in);
				}

				// Then delete the files returned.
				for (File file : updatedConfigFiles) {
					if (!file.delete()) {
						log.warn(file.getAbsolutePath() + " not deleted.");
					}
				}

				// send the properties to the config helper to update the
				// main
				// properties file for this group.
				ConfigHelper.updateGroup(groupName, properties);
				ConfigHelper.saveConfiguration();

				// stop the backingStartStoppable and restart it with the
				// new
				// settings.
				backingStartStoppable.stop();
				try {
					backingStartStoppable.start(properties);
				} catch (Exception e) {
					// if any errors occur, log it, but don't exit the thread.
					// The
					// user might be able to recover.
					log.warn("Unable to restart the " + groupName
							+ " service. Try sending a new configuration.", e);
				}

			}

			// when we're done updating the config or if no files are returned,
			// sleep for a bit
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				log.warn("What the snap!!!", e);
			}

		}
	}

}
