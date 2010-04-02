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
 *  2009/06/21 Updated getActionResponse to handle methods that return different types
 *  2009/06/30 Added a javascript onclick to generate a confirmation popup before terminating
 *  2009/07/08 Added id elements to html commandlist for accessing with javascript
 *  2009/10/04 Updated to ignore the new getPage, setPage, getPageSize and setPageSize functions for the actionlist
 *  
 *  --- To Do ---
 *  Still need to improve error handling.
 *
 * Jesse Norris, Creare Inc.
 * Version 1.0
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
			String indsHost = System.getProperty("inds.host");	// mjm
			java.rmi.registry.Registry reg
				= java.rmi.registry.LocateRegistry.getRegistry(indsHost); // mjm
			
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
			queryDisplay = "all";
			
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
				if (action.getGenericReturnType() == String.class) {
					remoteResult = action.invoke(remoteIndsObject,queryCommand).toString();
				} else if (action.getGenericReturnType() == void.class) {
					action.invoke(remoteIndsObject,queryCommand);
					remoteResult = queryAction+" executed successfully.";
					// Terminate behaves differently than the other commands.
					if (queryAction.compareTo("terminate")==0)
						queryAction = "getConfiguration"; // Reset to default action
				} else {
					remoteResult = "Action "+queryAction+" not invoked!  INDS Viewer does not support return type of "+action.getReturnType().toString();
				}
			}
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
			System.out.println("getActionResponse: java.lang.NullPointerException");
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
				// Go through actions and don't bother creating a link unless it is not one of these
				if ((actions[i].getName().compareTo("isComplete")!=0) &&
					(actions[i].getName().compareTo("getCommandList")!=0) && 
					(actions[i].getName().compareTo("getRootConfiguration")!=0) &&
					(actions[i].getName().compareTo("getName")!=0) &&
					(actions[i].getName().compareTo("getPage")!=0) &&
					(actions[i].getName().compareTo("setPage")!=0) &&
					(actions[i].getName().compareTo("getPageSize")!=0) &&
					(actions[i].getName().compareTo("setPageSize")!=0) &&
					(actions[i].getName().compareTo("getCommandOutPageCount")!=0) &&
					(actions[i].getName().compareTo("getCommandErrorPageCount")!=0)) {
						
						// Determine if it is the current selected queryAction
						actionListHTML = actionListHTML+"\n\t\t\t";
						if ((queryAction!=null) && (actions[i].getName().compareTo(queryAction)==0))
							actionListHTML = actionListHTML+"<li class='current' ";
						else
							actionListHTML = actionListHTML+"<li ";
						
						// If it's the terminate command, need to force a pop-up for confirming
						// This is done using the Javascript in action.jsp
						if (actions[i].getName().compareTo("terminate")==0) {
							if ((queryCommand!=null) && (!remoteIndsObject.isComplete(queryCommand)))
								actionListHTML = actionListHTML+"onclick='confirmTerminate(\"Terminate "+remoteIndsObject.getName(queryCommand)+"?\",\""+queryCommand+"\");' onmouseover='this.style.color=\"#0000c9\";' onmouseout='this.style.color=\"#000000\";'";
						}
						else
							actionListHTML = actionListHTML+"onclick='window.location.replace(\"index.jsp?action="+actions[i].getName()+"\");' onmouseover='this.style.color=\"#0000c9\";' onmouseout='this.style.color=\"#000000\";'";
						
						// Close out the hyperlink
						actionListHTML = actionListHTML+">"+actions[i].getName()+"</li>";
					}
			}
		}
		catch (java.lang.NullPointerException e)
		{
			// Attempt to reconnect to execution manager
			System.out.println("getActionList: java.lang.NullPointerException");
			ExecutionManagerConnect();
			actionListHTML = getActionList();
		}
		
		return actionListHTML+"\n\t\t</ul>";
	}
	
	/**
	* Return the command list (formatted for HTML)
	*/
	public String getCommandList()
		throws java.rmi.RemoteException, java.rmi.NotBoundException, java.lang.ClassNotFoundException, indsBean.ExecutionManagerException
	{
		String commandListHTML="<ul>";
		String currentHTML=null;
		try 
		{
			for (String command : remoteIndsObject.getCommandList()) {
				
				// mark the current command
				currentHTML="";
				if (queryCommand != null)
					if (command.compareTo(queryCommand)==0)
						currentHTML = " class='current'";
				
				if (!remoteIndsObject.isComplete(command)) {
					commandListHTML=commandListHTML+"\n\t\t\t\t<li"+currentHTML+" id='"+command+"' onclick='window.location.replace(\"index.jsp?command="+command+"\");' onmouseover='this.style.color=\"#0000c9\";' onmouseout='this.style.color=\"#000000\";'>"+remoteIndsObject.getName(command)+"</li>";
				} else if (queryDisplay.compareTo("all") == 0) {
					commandListHTML=commandListHTML+"\n\t\t\t\t<li"+currentHTML+" id='"+command+"' style='color: #990000' onclick='window.location.replace(\"index.jsp?command="+command+"\");' onmouseover='this.style.color=\"#ff0000\";' onmouseout='this.style.color=\"#990000\";'>"+remoteIndsObject.getName(command)+"</li>";
				}
			}		
			commandListHTML=commandListHTML+"\n\t\t\t</ul>";
		}
		catch (java.lang.NullPointerException e)
		{
			// Attempt to reconnect to execution manager
			System.out.println("getCommandList: java.lang.NullPointerException");
			ExecutionManagerConnect();
			commandListHTML = getCommandList();
		}
		catch (java.rmi.ConnectException e)
		{
			// Attempt to reconnect to execution manager
			System.out.println("getCommandList: java.rmi.ConnectException");
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
			System.out.println("hit the catch in getCommandName");
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
	
	/**
	* setPageSize sets the number of lines to be returned for paginated log responses
	*/
	public void setPageSize(int newPageSize) throws java.rmi.RemoteException
	{
		remoteIndsObject.setPageSize(newPageSize);
	}
	
	public int getPageSize() throws java.rmi.RemoteException
	{
		return remoteIndsObject.getPageSize();
	}
	
	/**
	* setPage sets the current page to be returned for paginated log responses
	*/
	public void setPage(int newPage) throws java.rmi.RemoteException
	{
		remoteIndsObject.setPage(newPage);
	}
	
	public int getPage() throws java.rmi.RemoteException
	{
		return remoteIndsObject.getPage();
	}
	
	/**
	* getCommandOutPageCount retrieves the page count for the command out log file 
	*/
	public int getCommandOutPageCount() throws java.rmi.RemoteException
	{
		return remoteIndsObject.getCommandOutPageCount(queryCommand);
	}
	
	/**
	* getCommandErrorPageCount retrieves the page count for the command error log file
	*/
	public int getCommandErrorPageCount() throws java.rmi.RemoteException
	{
		return remoteIndsObject.getCommandErrorPageCount(queryCommand);
	}
	
	/** 
	*
	*/
}
