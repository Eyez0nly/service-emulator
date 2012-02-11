/**
 * 
 */
package org.darkimport.qsle.util;

import java.util.List;

/**
 * @author user
 * 
 */
public class HostEntry {
	// A mark consists of three characters. A mark is contained in the comment
	// of a host line. A mark should be the last three characters of a comment.
	// A mark is demarcated with the string "HBB:"
	private String comment;
	private String mark;
	private String ip;
	private List<String> hostNames;

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment
	 *            the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * @param ip
	 *            the ip to set
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * @return the hostNames
	 */
	public List<String> getHostNames() {
		return hostNames;
	}

	/**
	 * @param hostNames
	 *            the hostNames to set
	 */
	public void setHostNames(List<String> hostNames) {
		this.hostNames = hostNames;
	}

	/**
	 * @return the mark
	 */
	public String getMark() {
		return mark;
	}

	/**
	 * @param mark
	 *            the mark to set
	 */
	public void setMark(String mark) {
		this.mark = mark;
	}
}
