package org.darkimport.qsle.services;

import java.util.Properties;

public interface StartStoppable {

	public void start(Properties properties) throws Exception;

	public void stop();

	public boolean isStarted();

}