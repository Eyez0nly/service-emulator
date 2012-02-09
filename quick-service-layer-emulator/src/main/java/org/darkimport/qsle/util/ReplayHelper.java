/**
 * 
 */
package org.darkimport.qsle.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.darkimport.qsle.services.ReplayEvent;

/**
 * @author user
 *
 */
public class ReplayHelper {
	private static final Log log = LogFactory.getLog(ReplayHelper.class);

	/**
	 * @param conversationBasePath
	 * @param conversationFileInputStream
	 * @param replays
	 * @return
	 */
	public static List<ReplayEvent> loadReplay(String conversationBasePath) {
		FileInputStream conversationFileInputStream = null;
		List<ReplayEvent> replay = new ArrayList<ReplayEvent>();
		try {
			conversationFileInputStream = new FileInputStream(
					conversationBasePath + "/conversation");
			List<String> conversationFile = IOUtils
					.readLines(conversationFileInputStream);
			
			for (String conversationLine : conversationFile) {
				String[] conversationParts = conversationLine.split(":");
				ReplayEvent replayEvent = new ReplayEvent();
				replayEvent.timeStamp = new Long(conversationParts[0]);
				replayEvent.fileSource = conversationBasePath + "/"
						+ conversationParts[1];
				replayEvent.length = new Integer(conversationParts[2]);
				if (conversationParts[1].startsWith("preauthClient")) {
					replayEvent.clientEvent = true;
				}
				replay.add(replayEvent);
			}
		} catch (FileNotFoundException e) {
			log.fatal("Unable to find the conversation file.", e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			log.fatal("Unable to read the conversation file.", e);
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(conversationFileInputStream);
		}
		return replay;
	}

}
