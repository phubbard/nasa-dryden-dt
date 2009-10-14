package indsBean;

/**
 *  Create a bean to keep track of html page formatting.
 * 
 *  ---  History  ---
 *  2009/10/12 Added command list height
 *
 *  --- To Do ---
 *
 * Jesse Norris, Creare Inc.
 * Version 1.0
 */


public class FormatBean implements java.io.Serializable 
{
	// Declare class variables
	private String leftWidth;
	private String centerWidth;
	private String rightWidth;
	private String commandListHeight;
	
	/** 
	  * Constructor method initializes
	  */
	public FormatBean()
	{
		// Defaults
		leftWidth = "33%";
		centerWidth = "22%";
		rightWidth = "43%";
		commandListHeight = "60%";
	}
	
	public String getLeftWidth() 
	{
		return leftWidth;
    }
	
	public void setLeftWidth(String leftWidth)
	{
		this.leftWidth = leftWidth;
	}
	
	public String getCenterWidth()
	{
		return centerWidth;
	}
	
	public void setCenterWidth(String centerWidth)
	{
		this.centerWidth = centerWidth;
	}
	
	public String getRightWidth() 
	{
		return rightWidth;
	}
	
	public void setRightWidth(String rightWidth)
	{
		this.rightWidth = rightWidth;
	}
	
	public String getCommandListHeight()
	{
		return commandListHeight;
	}
	
	public void setCommandListHeight(String commandListHeight)
	{
		this.commandListHeight = commandListHeight;
	}
}
