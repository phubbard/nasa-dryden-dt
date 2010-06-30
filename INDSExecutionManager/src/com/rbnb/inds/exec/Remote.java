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
	2009/01/15  WHF  Added getRootConfiguration and getCommandClassification.
	2009/06/08  WHF  Added terminate().
	2009/09/24  WHF  Added the page methods.
	2009/10/06  WHF  Added the page count methods.
	2010/06/30  JPW  Added terminateIEM().
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
	  * Get the display classification assigned to this command type.
	  *
	  * @since 2009/01/15
	  */
	public String getCommandClassification(String cmd)
		throws java.rmi.RemoteException;
	
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
	  * Gets a copy of the initial configuration file in its entirety.
	  *
	  * @since 2009/01/15
	  */
	public String getRootConfiguration() throws java.rmi.RemoteException;		
			
	/**
	  * Gets a user-friendly yet concise description of the command.  It is 
	  *  not guaranteed to be unique.
	  *
	  * @since 2009/01/09
	  */
	public String getName(String cmd) throws java.rmi.RemoteException;
	
	/**
	  * Stops command execution of the named task.  Nothing happens if the 
	  *  task has already stopped.
	  *
	  * @since 2009/06/08
	  */
	public void terminate(String cmd) throws java.rmi.RemoteException;
	
	/**
	  * Stops execution of all commands and finishes INDS Execution Manager.
	  *
	  * @since 2010/06/30
	  */
	public void terminateIEM() throws java.rmi.RemoteException;
	
	/**
	  * Returns the current page size in lines, for paged log retrieval.
	  */
	public int getPageSize() throws java.rmi.RemoteException;
	
	/**
	  * Changes the current page size, in lines, for paged log retrieval.
	  *  The default page size is 1000 lines.
	  */
	public void setPageSize(int newSize) throws java.rmi.RemoteException;
	
	/**
	  * Returns the current page number, with zero being the first page,
	  * one being the second page, negative one being the last page, and
	  * negative two being the second to last page, etc.
	  */
	public int getPage() throws java.rmi.RemoteException;
	
	/**
	  * Set the new log page.  The default page is -1.
	  */	
	public void setPage(int newPage) throws java.rmi.RemoteException;
	
	/**
	  * Get the number of pages for the standard output log for the specified
	  *   command.
	  */
	public int getCommandOutPageCount(String cmd)
			throws java.rmi.RemoteException;
			
	/**
	  * Get the number of pages for the standard error log for the specified
	  *   command.
	  */
	public int getCommandErrorPageCount(String cmd)
			throws java.rmi.RemoteException;
	
}

