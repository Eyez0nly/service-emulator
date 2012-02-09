/**
 * 
 */
package org.darkimport.qsle.services;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.darkimport.qsle.constants.PropertiesConstants;
import org.darkimport.qsle.servlet.PassThroughServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * @author user
 * 
 */
public class PassThroughEmulator implements StartStoppable {
	private static final Log log = LogFactory
			.getLog(PassThroughEmulator.class);
	Server server;

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Properties properties = new Properties();
		properties.setProperty(PropertiesConstants.PORT_NUMBER, "80");
		properties.setProperty(PropertiesConstants.CONTEXT_PATH, "/");
		properties.setProperty(PropertiesConstants.SERVLET_PATTERN, "/*");
		PassThroughEmulator meshEmulator = new PassThroughEmulator();
		meshEmulator.start(properties);
	}

	/**
	 * @param servletPattern
	 * @param contextPath
	 * @param port
	 * @return
	 */
	private Server generateServer(int port, String contextPath,
			String servletPattern) {
		Server server = new Server(port);

		ServletContextHandler context = new ServletContextHandler(
				ServletContextHandler.SESSIONS);

		context.setContextPath(contextPath);
		server.setHandler(context);

		context.addServlet(new ServletHolder(new PassThroughServlet()), servletPattern);
		return server;
	}

	public void start(Properties properties) throws Exception {
		if (server == null || !server.isStarted()) {
			int port = new Integer(
					properties.getProperty(PropertiesConstants.PORT_NUMBER));
			String contextPath = properties
					.getProperty(PropertiesConstants.CONTEXT_PATH);
			String servletPattern = properties
					.getProperty(PropertiesConstants.SERVLET_PATTERN);
			server = generateServer(port, contextPath, servletPattern);
			server.start();
//			server.join();
		} else {
			log.warn("The server is already started.");
		}
	}

	public void stop() {
		if (server.isStarted()) {
			try {
				server.stop();
				server.destroy();
			} catch (Exception e) {
				log.warn("An error occurred while stopping the server.", e);
			}
		} else {
			log.warn("The service is already started. No need to stop.");
		}
	}

	public boolean isStarted() {
		if (server != null) {
			return server.isStarted();
		}

		return false;
	}

}
