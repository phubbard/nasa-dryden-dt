package indsBean;

import com.rbnb.inds.exec.Remote;
import java.lang.reflect.Method;

/**
 * Create a bean to handle the requests to the INDS execution manager.
 *
 * Jesse Norris, Creare Inc.
 * Version 0.6
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
		throws java.rmi.RemoteException, java.rmi.NotBoundException, java.lang.ClassNotFoundException
	{
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
		queryAction = "getName";
		queryCommand = null;
		queryDisplay = null;
	}
	
	/**
	 * Return the HTML formatted response for the current queryCommand and queryAction
     */
    public String getActionResponse() 
	{
		String remoteResult = new String();
		
		try
		{
			if (queryCommand != null) 
			{
				Method action = remoteClass.getMethod(queryAction,queryCommand.getClass());
				remoteResult = action.invoke(remoteIndsObject,queryCommand).toString();
			} 
			// This else was for actions that did require a queryCommand
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
		
		return remoteResult;
    }
	
	/**
	* Get list of available actions
	*/
	public String getActionList()
	{
		String actionListHTML;
		
		actionListHTML = "<ul>";
		for (int i=0; i<actions.length; i++) {
			if ((actions[i].getName().compareTo("isComplete")!=0) &&
				(actions[i].getName().compareTo("getCommandList")!=0) && 
				(actions[i].getName().compareTo("getRootConfiguration")!=0))
				if (actions[i].getName().compareTo(queryAction)==0)
					actionListHTML = actionListHTML+"<li class='current'><a href=action.jsp?&action="+actions[i].getName()+">"+actions[i].getName()+"</a></li>";
				else
					actionListHTML = actionListHTML+"<li><a href=action.jsp?&action="+actions[i].getName()+">"+actions[i].getName()+"</a></li>";
		}
		return actionListHTML+"</ul>";
	}
	
	/**
	* Return the command list
	*/
	public String getCommandList() 
		throws java.rmi.RemoteException
	{
		String commandListHTML="<ul>";
		for (String command : remoteIndsObject.getCommandList()) {
			
			commandListHTML=commandListHTML+"<li><a target='right' href='action.jsp?command="+command;
			
			if (!remoteIndsObject.isComplete(command)) {
				commandListHTML=commandListHTML+"'>";
			} else if (queryDisplay == null) {
				commandListHTML=commandListHTML+"' class='complete'>";
			}
			commandListHTML=commandListHTML+remoteIndsObject.getName(command)+"</a></li>";
		}		
		commandListHTML=commandListHTML+"</ul>";
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
	* This is used to switch between showing all commands and only the current commands
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
