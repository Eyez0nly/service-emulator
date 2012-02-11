package org.darkimport.qsle.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;

public class HostsEditor {
	// public static final String HOSTS_FILE =
	// "%windir%\system32\drivers\etc\hosts";
	private static final String HOSTS_FILE = System.getenv("SystemRoot")
			+ "\\System32\\drivers\\etc\\hosts";
	private static final String MARK_DEMARCATOR = "HBB:";
	private static final int MARK_LENGTH = 3;
	private static final String COMMENT_DEMARCATION = "#";
	private List<HostEntry> hostEntries;
	private String hostsFile;

	public HostsEditor() throws IOException {
		this(HOSTS_FILE);
	}

	public HostsEditor(String hostsFile) throws IOException {
		hostEntries = parseHostsFile(hostsFile);
		this.hostsFile = hostsFile;
	}

	private List<HostEntry> parseHostsFile(String hostsFile) throws IOException {
		List<String> hostLines = FileUtils.readLines(new File(hostsFile));
		List<HostEntry> hostEntries = new ArrayList<HostEntry>();
		for (String hostEntryString : hostLines) {
			HostEntry hostEntry = new HostEntry();
			String mark = null;
			String ipAddress = null;
			List<String> hostNames = null;
			String comment = null;

			if (hostEntryString.length() > 0) {
				int commentStartIndex = hostEntryString
						.indexOf(COMMENT_DEMARCATION);
				if (commentStartIndex != -1) {
					if (commentStartIndex != hostEntryString.length() - 1) {
						String rawComment = hostEntryString
								.substring(commentStartIndex + 1);
						int markIndex = rawComment.indexOf(MARK_DEMARCATOR);
						if (markIndex != -1
								&& markIndex + 3 + MARK_LENGTH == rawComment
										.length() - 1) {
							comment = rawComment.substring(0, markIndex);
							mark = rawComment.substring(markIndex
									+ MARK_DEMARCATOR.length());
						} else {
							comment = rawComment;
						}
					} else {
						comment = "";
					}
				}
				if (commentStartIndex != 0) {
					String rawData;
					if (commentStartIndex == -1) {
						rawData = hostEntryString.trim();
					} else {
						rawData = hostEntryString.substring(0,
								commentStartIndex).trim();
					}
					if (rawData.length() > 0) {
						String[] dataParts = rawData.split("\\s");
						if (dataParts.length > 1 && dataParts[0].length() > 0) {

							hostNames = new ArrayList<String>();
							for (int i = 1; i < dataParts.length; i++) {
								String hostName = dataParts[i].trim();
								if (hostName.length() > 0) {
									hostNames.add(hostName);
								}
							}
							if (hostNames.size() > 0) {
								ipAddress = dataParts[0];
							} else {
								hostNames = null;
							}
						}
					}
				}
			}
			hostEntry.setMark(mark);
			hostEntry.setIp(ipAddress);
			hostEntry.setHostNames(hostNames);
			hostEntry.setComment(comment);
			hostEntries.add(hostEntry);
		}

		return hostEntries;
	}

	public void updateHosts() throws IOException {
		List<String> hostLines = convertHostEntriesToTxt(hostEntries);
		FileUtils.writeLines(new File(hostsFile), hostLines);
	}

	private List<String> convertHostEntriesToTxt(List<HostEntry> hostEntries) {
		List<String> hostLines = new ArrayList<String>();
		for (HostEntry hostEntry : hostEntries) {
			StringBuffer hostLine = new StringBuffer();
			if (hostEntry.getIp() != null) {
				hostLine.append(hostEntry.getIp()).append(" ");
			}
			if (hostEntry.getHostNames() != null
					&& hostEntry.getHostNames().size() > 0) {
				for (String hostName : hostEntry.getHostNames()) {
					hostLine.append(hostName).append(" ");
				}
			}
			if (hostEntry.getComment() != null || hostEntry.getMark() != null) {
				hostLine.append(COMMENT_DEMARCATION);
				if (hostEntry.getComment() != null) {
					hostLine.append(hostEntry.getComment());
				}
				if (hostEntry.getMark() != null) {
					hostLine.append(MARK_DEMARCATOR)
							.append(hostEntry.getMark());
				}
			}
			hostLines.add(hostLine.toString());
		}

		return hostLines;
	}

	public void addHost(String ip, String comment, String mark, String... hosts) {
		HostEntry hostEntry = new HostEntry();
		hostEntry.setIp(ip);
		hostEntry.setComment(comment);
		hostEntry.setMark(mark);
		List<String> hostsList = new ArrayList<String>();
		CollectionUtils.addAll(hostsList, hosts);
		hostEntry.setHostNames(hostsList);
		hostEntries.add(hostEntry);
	}

	public void removeMarkedHosts(String mark) {
		List<HostEntry> hostEntriesToRemove = new ArrayList<HostEntry>();
		for (HostEntry hostEntry : hostEntries) {
			if (mark.equals(hostEntry.getMark())) {
				hostEntriesToRemove.add(hostEntry);
			}
		}

		for (HostEntry hostEntry : hostEntriesToRemove) {
			hostEntries.remove(hostEntry);
		}
	}

	/**
	 * @return the hostEntries
	 */
	public List<HostEntry> getHostEntries() {
		return hostEntries;
	}

}