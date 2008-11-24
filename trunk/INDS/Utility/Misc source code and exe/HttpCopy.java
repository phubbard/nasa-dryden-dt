//package com.rbnb.web;
// some edits by mjm 7/19/2005

import java.io.*;
import java.net.*;

/**
  * Utility to copy a resource from one URL to another.  Requires the 
  *   destination to support HTTP PUT.
  */
public class HttpCopy
{
    static boolean state = false;  // success state for debug

    public static void copy(String source, String dest)
	throws IOException
    {
	copy(new URL(source), new URL(dest));
    }
    
    public static void copy(URL source, URL dest)
	throws IOException
    {
	HttpURLConnection srcCon = (HttpURLConnection) source.openConnection(),
	    destCon = (HttpURLConnection) dest.openConnection();
	
	InputStream input = srcCon.getInputStream();
	destCon.setDoOutput(true); // necessary or getOutputStream() fails.
	destCon.setRequestMethod("PUT");
	OutputStream output = destCon.getOutputStream();
	
	try {
	    byte[] ba = new byte[srcCon.getContentLength()];
	    int bytesRead = 0;
	    do {
		bytesRead += input.read(ba, bytesRead, ba.length-bytesRead);
	    } while (bytesRead != ba.length);
	    output.write(ba);
	    output.flush();
	} finally {
	    input.close();
	    output.close();
	}
	int response = destCon.getResponseCode(); // also commits output
	// Input routines throw, output does not.
	if (response >= 200 && response < 300) {  // success
	    if(state == false) System.out.print("Response: Successful ("
			       +response+' '+destCon.getResponseMessage()+')');
	    else               System.out.print(".");
	    state = true;
	} else {
	    state = false;
	    throw new IOException
	 // System.out.println
		("Error in PUT to "+dest+": "+response+' '+destCon.getResponseMessage());
	}
    }
    
    public static void main(String[] args)
    {
	try {
	    int interval;
	    
	    if (args.length < 3)
		interval = 60;
	    else interval = Integer.parseInt(args[2]);
	    if (args.length < 2) {
		System.err.println(
			   "HttpCopy source-url dest-url [interval-sec=60]\n"
			   +"\tAn interval of zero only copies once.");
	    } else {
		if (interval == 0)
		    copy(args[0], args[1]);
		else while (true) {
		    copy(args[0], args[1]);
		    Thread.sleep(1000*interval);
		}
		return;				
	    }
	} catch (Exception e) {
			e.printStackTrace();
	}
	System.err.println("Exiting...");
    }
}

