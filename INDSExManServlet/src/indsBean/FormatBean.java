package indsBean;

/**
 *  Create a bean to keep track of html page formatting.
 * 
 *  ---  History  ---
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
	
	/** 
	  * Constructor method initializes
	  */
	public FormatBean()
	{
		// Defaults
		leftWidth = "33%";
		centerWidth = "22%";
		rightWidth = "43%";
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
}
