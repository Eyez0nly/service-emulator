/**
 * 
 */
package org.darkimport.qsle.servlet;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.darkimport.qsle.constants.PropertiesConstants;
import org.darkimport.qsle.util.ConfigHelper;

/**
 * @author user
 * 
 */
public class PassThroughServlet extends HttpServlet {
	private static final Log	log					= LogFactory.getLog(PassThroughServlet.class);

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 4698700313474208924L;

	private Properties			properties;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
			IOException {
		doWork(req, resp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
			IOException {
		doWork(req, resp);
	}

	private void doWork(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
			IOException {
		// Get the path of the request
		final String requestPath = req.getPathInfo();
		log.info("Received request for " + requestPath + ".");

		// If checkForUpdates is true in config, check the real mesh server
		// (host in config file). Then if there is an update, download the new
		// mesh to the local mesh repo.
		final boolean checkForUpdates = new Boolean(properties.getProperty(PropertiesConstants.CHECK_FOR_UPDATES,
				"true"));
		final String localResourceBase = properties.getProperty(PropertiesConstants.LOCAL_MESH_RESOURCE_BASE, "meshes");
		final File localCopy = new File(localResourceBase + requestPath);
		if (checkForUpdates) {
			final String remoteHost = properties.getProperty(PropertiesConstants.REMOTE_MESH_HOST);
			final URL remoteResource = new URL("http", remoteHost, -1, requestPath);

			long localModDate = 0;
			if (localCopy.exists()) {
				localModDate = localCopy.lastModified();
			} else {
				localCopy.getParentFile().mkdirs();
			}
			final URLConnection urlConnection = remoteResource.openConnection();
			long remoteModDate = 0;
			try {
				remoteModDate = urlConnection.getLastModified();
			} catch (final Exception e) {
				log.warn("Unable to connect to the remote mesh host.", e);
			}
			if (remoteModDate > localModDate) {
				final OutputStream out = new FileOutputStream(localCopy);
				final InputStream in = urlConnection.getInputStream();
				IOUtils.copy(in, out);
				out.flush();
				out.close();
				in.close();
			}
		}

		// If the sought after mesh file exists, stream it out. Don't forget to
		// flush.
		doDownload(req, resp, localCopy.getAbsolutePath(), localCopy.getName());

		// If the mesh file does not exist, 404!
	}

	/**
	 * Sends a file to the ServletResponse output stream. Typically you want the
	 * browser to receive a different name than the name the file has been saved
	 * in your local database, since your local names need to be unique.
	 * 
	 * @param req
	 *            The request
	 * @param resp
	 *            The response
	 * @param filename
	 *            The name of the file you want to download.
	 * @param original_filename
	 *            The name the browser should receive.
	 */
	private void doDownload(final HttpServletRequest req, final HttpServletResponse resp, final String filename,
			final String original_filename) throws IOException {
		final File f = new File(filename);
		if (f.exists() && !f.isDirectory()) {
			final OutputStream out = resp.getOutputStream();
			final ServletContext context = getServletConfig().getServletContext();
			final String mimetype = context.getMimeType(filename);

			//
			// Set the response and go!
			//
			//
			resp.setContentType((mimetype != null) ? mimetype : "application/octet-stream");
			resp.setContentLength((int) f.length());
			resp.setHeader("Content-Disposition", "attachment; filename=\"" + original_filename + "\"");

			//
			// Stream to the requester.
			//
			final DataInputStream in = new DataInputStream(new FileInputStream(f));

			IOUtils.copy(in, out);

			in.close();
			out.flush();
			out.close();
		} else {
			resp.sendError(404);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(final ServletConfig config) throws ServletException {
		super.init(config);

		properties = ConfigHelper.getGroup(PropertiesConstants.GROUP_MESH_EMULATOR);
	}
}
