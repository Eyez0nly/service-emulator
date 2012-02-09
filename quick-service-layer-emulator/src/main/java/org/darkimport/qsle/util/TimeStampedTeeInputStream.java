/**
 * 
 */
package org.darkimport.qsle.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.lang.ArrayUtils;

/**
 * @author user
 * 
 */
public class TimeStampedTeeInputStream extends TeeInputStream {
	private List<Read> reads;

	public TimeStampedTeeInputStream(InputStream input, OutputStream branch) {
		super(input, branch);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.io.input.TeeInputStream#read(byte[])
	 */
	@Override
	public int read(byte[] bts) throws IOException {
		Read read = new Read();
		read.readStart = new Date();
		int bytesRead = super.read(bts);
		read.readEnd = new Date();
		read.readContent = ArrayUtils.subarray(bts, 0, bytesRead);
		if (reads == null) {
			reads = new Vector<Read>();
		}
		reads.add(read);
		return bytesRead;
	}

	/**
	 * @return the reads
	 */
	public List<Read> getReads() {
		return reads;
	}

	public static class Read {
		public Date readStart;
		public Date readEnd;
		public byte[] readContent;
	}
}
