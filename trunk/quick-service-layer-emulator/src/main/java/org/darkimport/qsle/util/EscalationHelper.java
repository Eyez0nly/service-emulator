/**
 * 
 */
package org.darkimport.qsle.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author user
 * 
 */
public class EscalationHelper {
	private static final String		TEMP_DIR				= System.getProperty("java.io.tmpdir");

	private static final Log		log						= LogFactory.getLog(EscalationHelper.class);

	private static final String		ELEVATION_CMD			= FilenameUtils.concat(TEMP_DIR, "elevate.cmd");
	private static final String[]	ELEVATION_COMPONENTS	= new String[] { "elevate.cmd", "elevate.vbs" };
	private static final String		SOURCE_DIR				= "META-INF";

	public static void escalate(final boolean cleanUp, final boolean retryWithoutEscalationOnFail,
			final String... arguments) {
		boolean escalated = true;
		try {
			provideEscalation();
		} catch (final Exception e) {
			log.warn("Unable to provide escalation service.", e);
			escalated = false;
		}
		final List<String> command = new ArrayList<String>();
		if (escalated) {
			command.add(ELEVATION_CMD);
		}
		command.addAll(Arrays.asList(arguments));

		try {
			boolean error = executeCommand(command);
			if (error) {
				if (retryWithoutEscalationOnFail && escalated) {
					log.warn("Retrying without escalation.");
					command.remove(ELEVATION_CMD);
					error = executeCommand(command);
				} else {
					log.warn("Giving up. Tossing an Exception.");
				}
				if (error) {
					throw new RuntimeException("The command was not completed successfully.");
				}
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (cleanUp) {
				cleanUp();
			}
		}
	}

	private static void cleanUp() {
		for (final String component : ELEVATION_COMPONENTS) {
			final String fileName = FilenameUtils.concat(TEMP_DIR, component);
			new File(fileName).deleteOnExit();
		}
	}

	/**
	 * @param command
	 */
	private static boolean executeCommand(final List<String> command) {
		boolean error = false;
		final ProcessBuilder processBuilder = new ProcessBuilder(command);
		InputStream in = null;
		try {
			final Process process = processBuilder.start();
			in = IOUtils.toBufferedInputStream(process.getInputStream());
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			IOUtils.copy(in, out);
			log.debug("Output of command, " + processBuilder.command() + ":\n\n" + new String(out.toByteArray()));
		} catch (final IOException e) {
			log.warn("An error occurred while executing the command", e);
			error = true;
		} finally {
			IOUtils.closeQuietly(in);
		}

		return error;
	}

	private static void provideEscalation() {
		for (final String component : ELEVATION_COMPONENTS) {
			final String sourcePath = FilenameUtils.concat(SOURCE_DIR, component);
			final String destinationPath = FilenameUtils.concat(TEMP_DIR, component);
			extractFileFromClassPath(destinationPath, sourcePath);
		}
	}

	/**
	 * @param destinationPath
	 * @param out
	 */
	private static void extractFileFromClassPath(final String destinationPath, final String sourcePath) {
		InputStream in = null;
		OutputStream out = null;
		try {
			in = Thread.currentThread().getContextClassLoader().getResourceAsStream(sourcePath);
			out = new FileOutputStream(destinationPath);
			IOUtils.copy(in, out);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(in);
		}
	}
}
