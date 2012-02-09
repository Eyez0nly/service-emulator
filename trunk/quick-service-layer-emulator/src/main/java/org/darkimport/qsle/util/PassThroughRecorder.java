/**
 * 
 */
package org.darkimport.qsle.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Vector;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author user
 * 
 */
public class PassThroughRecorder implements Runnable {
	private static final Log	log	= LogFactory.getLog(PassThroughRecorder.class);
	private static int			baseRunNumber;
	private final Socket		connection;
	private final String		host;
	private final int			port;
	private final String		basePath;
	private final boolean		isSecure;

	public PassThroughRecorder(final Socket connection, final String host, final int port,
			final String basePath, final boolean isSecure) {
		this.connection = connection;
		this.host = host;
		this.port = port;
		this.basePath = basePath;
		this.isSecure = isSecure;
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		try {
			if (args.length > 3) {
				try {
					baseRunNumber = new Integer(args[4]);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
			final String host = args[0];
			final int port = new Integer(args[1]);
			final String basePath = args[2];
			final boolean isSecure = new Boolean(args[3]);
			ServerSocket serverSocket;
			if (isSecure) {
				serverSocket = SSLServerSocketFactory.getDefault().createServerSocket(port);
			} else {
				serverSocket = ServerSocketFactory.getDefault().createServerSocket(port);
			}
			while (true) {
				final Socket connection = serverSocket.accept();
				new Thread(new PassThroughRecorder(connection, host, port, basePath, isSecure)).start();
			}
		} catch (final IOException e) {
			log.fatal("IO error.", e);
		}
	}

	public void run() {
		try {
			int runNumber;
			synchronized (this) {
				runNumber = ++baseRunNumber;
			}
			Socket socket;
			if (isSecure) {
				socket = SSLSocketFactory.getDefault().createSocket(host, port);
			} else {
				socket = SocketFactory.getDefault().createSocket(host, port);
			}

			final InputStream serverInputStream = connection.getInputStream();
			final OutputStream serverOutputStream = connection.getOutputStream();
			final InputStream clientInputStream = socket.getInputStream();
			final OutputStream clientOutputStream = socket.getOutputStream();
			final TeeInputStream clientServer = new TeeInputStream(serverInputStream, clientOutputStream);
			final TeeInputStream serverClient = new TeeInputStream(clientInputStream, serverOutputStream);
			final Vector<String> conversation = new Vector<String>();
			final TeeInputStreamer clientServerTeeInputStreamer = new TeeInputStreamer("preauthClient", clientServer,
					runNumber, conversation, basePath);
			final TeeInputStreamer serverClientTeeInputStreamer = new TeeInputStreamer("preauthServer", serverClient,
					runNumber, conversation, basePath);
			new Thread(clientServerTeeInputStreamer).start();
			new Thread(serverClientTeeInputStreamer).start();
			new Thread(new ConversationWriter(conversation, runNumber, basePath)).start();
		} catch (final UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static class TeeInputStreamer implements Runnable {
		private final TeeInputStream	teeInputStream;
		private final String			id;
		private final int				runNumber;
		private final Vector<String>	conversation;
		private final String			basePath;

		public TeeInputStreamer(final String id, final TeeInputStream teeInputStream, final int runNumber,
				final Vector<String> conversation, final String basePath) {
			this.id = id;
			this.teeInputStream = teeInputStream;
			this.runNumber = runNumber;
			this.conversation = conversation;
			this.basePath = basePath;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			int counter = 0;
			while (true) {
				final byte[] bytesToRead = new byte[1048576];
				try {
					final int bytesRead = teeInputStream.read(bytesToRead);
					if (bytesRead > 0) {
						final byte[] bytesReadArray = ArrayUtils.subarray(bytesToRead, 0, bytesRead);
						final String baseFileName = id + "." + "step" + ++counter;
						final String fileName = basePath + "/" + runNumber + "/" + baseFileName;
						final StepWriter stepWriter = new StepWriter(fileName, bytesReadArray);
						new Thread(stepWriter).start();
						final long timeStamp = new Date().getTime();
						final String message = id + " sent " + bytesRead + " Bytes"
								+ (log.isDebugEnabled() ? ": " + new String(bytesReadArray) : ".");
						conversation.add(timeStamp + ":" + baseFileName + ":" + bytesRead);
						log.info(message);
					} else {
						try {
							Thread.sleep(100);
						} catch (final InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} catch (final IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

	}

	private static class StepWriter implements Runnable {
		private final String	fileName;
		private final byte[]	bytesReadArray;

		/**
		 * @param fileName
		 * @param bytesReadArray
		 */
		public StepWriter(final String fileName, final byte[] bytesReadArray) {
			this.fileName = fileName;
			this.bytesReadArray = bytesReadArray;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			FileOutputStream stepOutputStream = null;
			try {
				final File file = new File(fileName);
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				stepOutputStream = new FileOutputStream(fileName);
				stepOutputStream.write(bytesReadArray);
			} catch (final Exception e) {
				log.warn("Unable to write " + fileName + ".", e);
			} finally {
				if (stepOutputStream != null) {
					try {
						stepOutputStream.close();
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

	private static class ConversationWriter implements Runnable {
		private final Vector<String>	conversation;
		private final int				runNumber;
		private final String			basePath;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			try {
				Thread.sleep(30 * 1000);
			} catch (final InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			FileOutputStream fileOutputStream = null;
			try {
				fileOutputStream = new FileOutputStream(basePath + "/" + runNumber + "/conversation");
				IOUtils.writeLines(conversation, null, fileOutputStream);
			} catch (final Exception e) {
				e.printStackTrace();
			} finally {
				IOUtils.closeQuietly(fileOutputStream);
			}
		}

		/**
		 * @param conversation
		 * @param runNumber
		 * @param basePath
		 */
		public ConversationWriter(final Vector<String> conversation, final int runNumber, final String basePath) {
			this.conversation = conversation;
			this.runNumber = runNumber;
			this.basePath = basePath;
		}

	}
}
