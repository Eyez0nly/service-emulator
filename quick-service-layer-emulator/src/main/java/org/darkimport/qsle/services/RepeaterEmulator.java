/**
 * 
 */
package org.darkimport.qsle.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.darkimport.qsle.constants.PropertiesConstants;
import org.darkimport.qsle.util.ReplayHelper;

/**
 * @author user
 * 
 */
public class RepeaterEmulator implements Runnable, StartStoppable {
	/**
	 * @author user
	 * 
	 */
	public class RepeaterConnectionHandler implements Runnable {
		private Socket connection;

		public RepeaterConnectionHandler(Socket connection) {
			this.connection = connection;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			try {
				log.info("Taking the first read to determine which replay to use.");
				byte[] primingRead = new byte[1048576];
				int primingBytesRead = connection.getInputStream().read(
						primingRead);
				Set<Integer> replayKeys = replays.keySet();
				List<ReplayEvent> replay = null;
				for (int key : replayKeys) {
					List<ReplayEvent> candidateReplay = replays.get(key);
					if (primingBytesRead == candidateReplay.get(0).length) {
						replay = candidateReplay;
						break;
					}
				}
				if (replay != null) {
					for (int i = 1; i < replay.size(); i++) {
						ReplayEvent replayEvent = replay.get(i);
						log.info("Preparing to begin "
								+ (replayEvent.clientEvent ? "client"
										: "server") + " event, "
								+ replayEvent.fileSource + ".");
						if (replayEvent.clientEvent) {
							byte[] clientEvent = new byte[1048576];
							int bytesRead = connection.getInputStream().read(
									clientEvent);
							if (bytesRead != -1) {
								log.info(new String(ArrayUtils.subarray(
										clientEvent, 0, bytesRead)));
							} else {
								log.warn("No response received.");
							}
							if (bytesRead != replayEvent.length) {

								log.warn("Expected " + replayEvent.length
										+ " bytes. Found " + bytesRead
										+ " bytes instead.");
							}
						} else {
							byte[] serverEvent = new byte[replayEvent.length];
							FileInputStream serverEventFileInputStream = new FileInputStream(
									replayEvent.fileSource);
							int bytesRead = serverEventFileInputStream
									.read(serverEvent);
							if (bytesRead != replayEvent.length) {
								log.warn("The length of the server side of the conversation does not match.");
							}
							connection.getOutputStream().write(serverEvent);
						}
					}
				}
			} catch (FileNotFoundException e) {
				log.fatal(
						"Unable to find one of the conversation component files.",
						e);
			} catch (IOException e) {
				log.fatal("An IO error. But where???", e);
			} finally {
				try {
					connection.close();
					connectionHandlers.remove(this);
				} catch (IOException e) {
					log.warn("An IO error occurred while closing the socket.",
							e);
				}
			}
		}

	}

	public static final Log log = LogFactory
			.getLog(RepeaterEmulator.class);
	private Map<Integer, List<org.darkimport.qsle.services.ReplayEvent>> replays;
	// private int port;
	// private String[] conversationPaths;

	private ServerSocket serverSocket;
	private boolean running;
	private List<RepeaterConnectionHandler> connectionHandlers = new Vector<RepeaterEmulator.RepeaterConnectionHandler>();

	// public RepeaterEmulator(int port, String... conversationPaths) {
	// this.port = port;
	// this.conversationPaths = conversationPaths;
	// }

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String conversationPaths = ArrayUtils.toString(
				ArrayUtils.subarray(args, 1, args.length)).substring(
				1,
				ArrayUtils.toString(ArrayUtils.subarray(args, 1, args.length))
						.length() - 1);

		Properties properties = new Properties();
		properties.setProperty(PropertiesConstants.PORT_NUMBER, args[0]);
		properties.setProperty(PropertiesConstants.CONVERSATION_PATHS,
				conversationPaths);

		StartStoppable authEmulator = new RepeaterEmulator();
		authEmulator.start(properties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.darkimport.qsle.services.StartStoppable#start()
	 */
	public void start(Properties properties) throws Exception {
		int port = new Integer(
				properties.getProperty(PropertiesConstants.PORT_NUMBER));
		boolean isSecure = new Boolean(
				properties.getProperty(PropertiesConstants.IS_SECURE));
		String[] conversationPaths = properties.getProperty(
				PropertiesConstants.CONVERSATION_PATHS).split(
				"\\" + File.pathSeparator);
		replays = loadReplay(conversationPaths);

		ServerSocket serverSocket;
		try {
			if (isSecure) {
				serverSocket = SSLServerSocketFactory.getDefault()
						.createServerSocket(port);
			} else {
				serverSocket = ServerSocketFactory.getDefault()
						.createServerSocket(port);
			}
			this.serverSocket = serverSocket;
		} catch (IOException e) {
			log.fatal("Unable to bind to port " + port + ".", e);
			throw new Exception(e);
		}

		new Thread(this).start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.darkimport.qsle.services.StartStoppable#stop()
	 */
	public void stop() {
		if (isStarted()) {
			running = false;
			for (RepeaterConnectionHandler authConnectionHandler : connectionHandlers) {
				try {
					authConnectionHandler.connection.close();
				} catch (Exception e) {
					log.warn(
							"An error occurred while closing open connection.",
							e);
				}
			}
			connectionHandlers = new Vector<RepeaterEmulator.RepeaterConnectionHandler>();
			try {
				serverSocket.close();
			} catch (IOException e) {
				log.warn("An error occurred while closing the server.", e);
			}
		} else {
			log.warn("The service is not started. No need to stop.");
		}
	}

	public void run() {
		running = true;
		while (running) {
			Socket connection;
			try {
				connection = serverSocket.accept();
			} catch (IOException e) {
				log.warn("An error occurred during client connect.", e);
				continue;
			}
			RepeaterConnectionHandler authConnectionHandler = new RepeaterConnectionHandler(
					connection);
			connectionHandlers.add(authConnectionHandler);
			new Thread(authConnectionHandler).start();
		}
	}

	private static Map<Integer, List<ReplayEvent>> loadReplay(
			String... conversationPaths) {

		Map<Integer, List<ReplayEvent>> replays = new HashMap<Integer, List<ReplayEvent>>();
		for (int i = 0; i < conversationPaths.length; i++) {
			replays.put(i + 1, ReplayHelper.loadReplay(conversationPaths[i]));
		}
		return replays;
	}

	public boolean isStarted() {
		if (serverSocket != null) {
			return serverSocket.isBound();
		}
		return false;
	}
}
