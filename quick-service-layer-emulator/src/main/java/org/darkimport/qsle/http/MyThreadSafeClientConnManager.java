/**
 * 
 */
package org.darkimport.qsle.http;

import java.util.concurrent.TimeUnit;

import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

/**
 * @author user
 * 
 */
public class MyThreadSafeClientConnManager extends ThreadSafeClientConnManager {

	/**
	 * 
	 */
	public MyThreadSafeClientConnManager() {
		super();
	}

	/**
	 * @param schreg
	 */
	public MyThreadSafeClientConnManager(final SchemeRegistry schreg) {
		super(schreg);
	}

	/**
	 * @param schreg
	 * @param connTTL
	 * @param connTTLTimeUnit
	 */
	public MyThreadSafeClientConnManager(final SchemeRegistry schreg, final long connTTL, final TimeUnit connTTLTimeUnit) {
		super(schreg, connTTL, connTTLTimeUnit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager#
	 * createConnectionOperator(org.apache.http.conn.scheme.SchemeRegistry)
	 */
	@Override
	protected ClientConnectionOperator createConnectionOperator(final SchemeRegistry schreg) {
		return new MyDefaultClientConnectionOperator(schreg);// @ThreadSafe
	}

}
