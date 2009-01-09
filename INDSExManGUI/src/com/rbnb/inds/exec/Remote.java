/*
	Remote.java
	
	Copyright 2008 Creare Inc.
	
	Licensed under the Apache License, Version 2.0 (the "License"); 
	you may not use this file except in compliance with the License. 
	You may obtain a copy of the License at 
	
	http://www.apache.org/licenses/LICENSE-2.0 
	
	Unless required by applicable law or agreed to in writing, software 
	distributed under the License is distributed on an "AS IS" BASIS, 
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
	See the License for the specific language governing permissions and 
	limitations under the License.
	
	
	---  History  ---
	2008/12/19  WHF  Created.
	2009/01/09  WHF  Added getChildConfiguration and getName.
*/

package com.rbnb.inds.exec;

/**
  * Defines methods available from the RMI interface.
  */
public interface Remote extends java.rmi.Remote
{
	/**
	  * Get the list of commands which have been started.
	  */
	public String[] getCommandList() throws java.rmi.RemoteException;
	/**
	  * Get the current output log for this command.
	  */
	public String getCommandOut(String cmd) throws java.rmi.RemoteException;
	/**
	  * Get the current error log for this command.
	  */
	public String getCommandError(String cmd) throws java.rmi.RemoteException;
	/**
	  * Get a portion of the configuration document that generated this 
	  *   command.
	  */
	public String getConfiguration(String cmd) throws java.rmi.RemoteException;
	
	/**
	  * Get the completion state of the command.
	  * @return true  if the command has ceased execution.
	  */
	public boolean isComplete(String cmd) throws java.rmi.RemoteException;
	
	/**
	  * Gets the contents of any configuration files referenced by the command's
	  *  configuration.
	  *
	  * @since 2009/01/09
	  */
	public String getChildConfiguration(String cmd) 
			throws java.rmi.RemoteException;
			
	/**
	  * Gets a user-friendly yet concise description of the command.  It is 
	  *  not guaranteed to be unique.
	  *
	  * @since 2009/01/09
	  */
	public String getName(String cmd) throws java.rmi.RemoteException;
}

