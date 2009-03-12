package indsBean;

import com.rbnb.inds.exec.Remote;
import java.lang.reflect.Method;

/**
 *  Create a bean to handle the requests to the INDS execution manager.
 * 
 *  ---  History  ---
 *  2009/03/03 Added getCommandName
 *  2009/03/09 Added an attempt to reconnect to Execution Manager if certain errors are caught
 *  2009/03/10 Added an ExecutionMangerException class to handle errors
 *  
 *  --- To Do ---
 *  Still need to improve error handling.
 *
 * Jesse Norris, Creare Inc.
 * Version 0.9
 */


public class ExecutionManagerBean implements java.io.Serializable 
{
	// Declare class variables
	private static Remote remoteIndsObject;
	private static Class<?> remoteClass;
	private static Method[] actions;
	private String queryAction;
	private String queryCommand;
	private String queryDisplay;
	
	/** 
	  * Constructor method initializes the remoteIndsObject and remoteClass
	  */
	public ExecutionManagerBean()
		throws java.rmi.RemoteException, java.rmi.NotBoundException, java.lang.ClassNotFoundException, indsBean.ExecutionManagerException
	{
		ExecutionManagerConnect();
	}
	
	/**
	 * Break this out from the constructor so that it may be called to reconnect
	 */
	public void ExecutionManagerConnect()
		throws java.rmi.RemoteException, java.rmi.NotBoundException, java.lang.ClassNotFoundException, indsBean.ExecutionManagerException
	{
		try 
		{
			// Display a message
			System.out.print("Attempting to connect to Execution Manager...");
			
			// Connect using RMI:
			java.rmi.registry.Registry reg
				= java.rmi.registry.LocateRegistry.getRegistry();
			
			String[] names = reg.list();
			
			int index = 0;
			
			// Initialize the remoteIndsObject & class object for com.rbnb.inds.exec.Remote
			remoteIndsObject = (Remote) reg.lookup(names[index]);
			remoteClass = Class.forName("com.rbnb.inds.exec.Remote");
			
			//Determine available actions
			actions = remoteClass.getDeclaredMethods();
			
			// Defaults
			queryAction = "getConfiguration";
			queryCommand = null;
			queryDisplay = null;
			
			// Display a message
			System.out.println("Connection established!");
		}
		catch (java.rmi.ConnectException e)
		{
			String message="Connection failed!\nCheck that an instance of the Execution Manager is running.\n";
			System.out.println(message);
			throw new ExecutionManagerException(message);
		}
	}
	
	/**
	 * Return the HTML formatted response for the current queryCommand and queryAction
     */
    public String getActionResponse() 
		throws java.rmi.RemoteException, java.rmi.NotBoundException, java.lang.ClassNotFoundException,  indsBean.ExecutionManagerException
	{
		String remoteResult = new String();
		
		try
		{
			if (queryCommand != null) 
			{
				Method action = remoteClass.getMethod(queryAction,queryCommand.getClass());
				remoteResult = action.invoke(remoteIndsObject,queryCommand).toString();
			} 
			// This else was for actions that did require a queryCommand - not needed by the indsViewer
			//else 
			//{
			//	Method action = remoteClass.getMethod(queryAction);
			//	remoteResult = action.invoke(remoteIndsObject).toString();
			//}
		}
		
		// Need to update with meaningful responses
		catch (java.lang.NoSuchMethodException e) 
		{
		}
		catch (java.lang.IllegalAccessException e) 
		{
		}
		catch (java.lang.reflect.InvocationTargetException e)
		{
		}
		catch (java.lang.NullPointerException e)
		{
			// Attempt to reconnect to execution manager
			ExecutionManagerConnect();
			remoteResult = getActionResponse();
		}
		
		return remoteResult;
    }
	
	/**
	* Get list of available actions
	*/
	public String getActionList()
		throws java.rmi.RemoteException, java.rmi.NotBoundException, java.lang.ClassNotFoundException,  indsBean.ExecutionManagerException
	{
		String actionListHTML;
		
		try 
		{
			
			actionListHTML = "<ul>";
			for (int i=0; i<actions.length; i++) {
				if ((actions[i].getName().compareTo("isComplete")!=0) &&
					(actions[i].getName().compareTo("getCommandList")!=0) && 
					(actions[i].getName().compareTo("getRootConfiguration")!=0) &&
					(actions[i].getName().compareTo("getName")!=0))
					if (actions[i].getName().compareTo(queryAction)==0)
						actionListHTML = actionListHTML+"<li class='current'><a href='action.jsp?&action="+actions[i].getName()+"'>"+actions[i].getName()+"</a></li>";
					else
						actionListHTML = actionListHTML+"<li><a href='action.jsp?&action="+actions[i].getName()+"'>"+actions[i].getName()+"</a></li>";
			}
		}
		catch (java.lang.NullPointerException e)
		{
			// Attempt to reconnect to execution manager
			ExecutionManagerConnect();
			actionListHTML = getActionList();
		}
			
		return actionListHTML+"</ul>";
	}
	
	/**
	* Return the command list (formatted for HTML)
	*/
	public String getCommandList()
		throws java.rmi.RemoteException, java.rmi.NotBoundException, java.lang.ClassNotFoundException, indsBean.ExecutionManagerException
	{
		String commandListHTML="<ul>";
		try 
		{
			for (String command : remoteIndsObject.getCommandList()) {
				
				if (!remoteIndsObject.isComplete(command)) {
					commandListHTML=commandListHTML+"<li><a target='right' href='action.jsp?command="+command+"'>"+remoteIndsObject.getName(command)+"</a></li>";
				} else if (queryDisplay == null) {
					commandListHTML=commandListHTML+"<li><a target='right' href='action.jsp?command="+command+"' class='complete'>"+remoteIndsObject.getName(command)+"</a></li>";
				}
			}		
			commandListHTML=commandListHTML+"</ul>";
		}
		catch (java.lang.NullPointerException e)
		{
			// Attempt to reconnect to execution manager
			ExecutionManagerConnect();
			commandListHTML = getCommandList();
		}
		catch (java.rmi.ConnectException e)
		{
			// Attempt to reconnect to execution manager
			ExecutionManagerConnect();
			commandListHTML = getCommandList();
		}
		return commandListHTML;
	}
	
	/**
	* Set the query action
	*/
	public void setQueryAction(String queryAction)
	{
		this.queryAction = queryAction;
	}
	
	public String getQueryAction()
	{
		return queryAction;
	}
	
	/**
	* Set the query command
	*/
	public void setQueryCommand(String queryCommand)
	{
		this.queryCommand = queryCommand;
	}
	
	public String getQueryCommand() 
	{
		return queryCommand;
	}
	
	/** 
	* Return the pretty name as opposed to command id
	*/
	public String getCommandName()
		throws java.rmi.RemoteException, java.rmi.NotBoundException, java.lang.ClassNotFoundException, indsBean.ExecutionManagerException
	{
		String commandName = null;
		try
		{
			if (queryCommand!=null) {
				commandName = remoteIndsObject.getName(queryCommand);
			}
		}
		catch (java.rmi.ConnectException e)
		{
			// Attempt to reconnect to execution manager
			ExecutionManagerConnect();
			commandName = getCommandName();
		}
	    return commandName; //(queryCommand!=null ? remoteIndsObject.getName(queryCommand) : null);
	}
	
	/**
	* queryDisplay is used to switch between showing all commands and only the current commands
	*/
	public void setQueryDisplay(String queryDisplay)
	{
		this.queryDisplay = queryDisplay;
	}
	
	public String getQueryDisplay()
	{
		return queryDisplay;
	}
}
