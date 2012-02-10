/**
 * 
 */
package org.darkimport.qsle.util;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author user
 * 
 */
public class RouteEntry {

	private String	networkDestination;
	private String	netMask;
	private String	gateway;
	private String	interfaceAddress;
	private String	metric;

	public void setNetworkDestination(final String networkDestination) {
		this.networkDestination = networkDestination;
	}

	public void setNetMask(final String netMask) {
		this.netMask = netMask;
	}

	public void setGateway(final String gateway) {
		this.gateway = gateway;
	}

	public void setInterfaceAddress(final String interfaceAddress) {
		this.interfaceAddress = interfaceAddress;
	}

	public void setMetric(final String metric) {
		this.metric = metric;
	}

	/**
	 * @return the networkDestination
	 */
	public String getNetworkDestination() {
		return networkDestination;
	}

	/**
	 * @return the netMask
	 */
	public String getNetMask() {
		return netMask;
	}

	/**
	 * @return the gateway
	 */
	public String getGateway() {
		return gateway;
	}

	/**
	 * @return the interfaceAddress
	 */
	public String getInterfaceAddress() {
		return interfaceAddress;
	}

	/**
	 * @return the metric
	 */
	public String getMetric() {
		return metric;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj != null && obj.getClass().equals(getClass())) {
			final RouteEntry other = (RouteEntry) obj;
			final EqualsBuilder equalsBuilder = new EqualsBuilder();
			equalsBuilder.append(gateway, other.gateway).append(interfaceAddress, other.interfaceAddress)
					.append(metric, other.metric).append(netMask, other.netMask)
					.append(networkDestination, other.networkDestination);
			return equalsBuilder.isEquals();
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE, true, getClass());
	}

}
