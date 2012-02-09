/**
 * 
 */
package org.darkimport.qsle;

import java.io.FileInputStream;
import java.security.MessageDigest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author user
 * 
 */
public class SyncTest {
	private static final Log	log	= LogFactory.getLog(SyncTest.class);

	/**
	 * @param args
	 */
	public static void main(final String[] args) throws Exception {
		final FileInputStream syncIn = new FileInputStream("authRuns/102/preauthClient.step1");
		final byte[] syncRequest = new byte[48];
		final byte[] fullRequest = new byte[1024000];
		final int syncBytesRead = syncIn.read(fullRequest);
		log.info("read " + syncBytesRead);
		log.info("converts to string:\n\n" + new String(syncRequest));
		final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
		final byte[] md5Digest = messageDigest.digest(syncRequest);
		log.info("md5 digest as string is:\n\n" + new String(md5Digest));
	}

}
