
    /*************************************************************************
     *
     * ChannelFilter
     *
     * This class specifies a filter on a specific channel.  In order to pass
     * the filter, the channel data must be a number which is between the
     * specified min/max values.  That is, the data point for this channel must
     * be in the range:
     *
     *     minValue <= dataPoint <= maxValue
     *
     ************************************************************************/
    
    public class ChannelFilter {
	
	private String chanName = null;
	private int chanIdx = -1;
	private double minValue = -Double.MAX_VALUE;
	private double maxValue = Double.MAX_VALUE;
	
	/*
	 * Constructor
	 */
	public ChannelFilter(String chanNameI, int chanIdxI, double minValueI, double maxValueI) throws Exception {
	    
	    if ( (chanNameI == null) || (chanNameI.trim().equals("")) ) {
		throw new Exception("Empty channel name");
	    }
	    if (chanIdxI < 0) {
		throw new Exception(new String("Illegal channel index: " + chanIdxI));
	    }
	    if (minValueI == Double.NaN) {
		throw new Exception("Illegal min value (NaN)");
	    }
	    if (maxValueI == Double.NaN) {
		throw new Exception("Illegal max value (NaN)");
	    }
	    chanName = chanNameI;
	    chanIdx = chanIdxI;
	    minValue = minValueI;
	    maxValue = maxValueI;
	    if (minValue > maxValue) {
		// Just swap the 2 values
		double temp = minValue;
		minValue = maxValue;
		maxValue = temp;
	    }
	    
	}
	
	/*
	 * Constructor
	 */
	public ChannelFilter(String chanNameI, double minValueI, double maxValueI) throws Exception {
	    
	    if ( (chanNameI == null) || (chanNameI.trim().equals("")) ) {
		throw new Exception("Empty channel name");
	    }
	    if (minValueI == Double.NaN) {
		throw new Exception("Illegal min value (NaN)");
	    }
	    if (maxValueI == Double.NaN) {
		throw new Exception("Illegal max value (NaN)");
	    }
	    chanName = chanNameI;
	    minValue = minValueI;
	    maxValue = maxValueI;
	    if (minValue > maxValue) {
		// Just swap the 2 values
		double temp = minValue;
		minValue = maxValue;
		maxValue = temp;
	    }
	    
	}
	
	public String getChanName() {
	    return chanName;
	}
	
	public int getChanIdx() {
	    return chanIdx;
	}
	
	public void setChanIdx(int chanIdxI) throws Exception {
	    if (chanIdxI < 0) {
		throw new Exception(new String("Illegal channel index: " + chanIdxI));
	    }
	    chanIdx = chanIdxI;
	}
	
	/*
	 * Check if the string associated with this filter represents a number which will pass this filter.
	 */
	public boolean checkValue(String[] valuesI) throws Exception {
	    
	    if ( (valuesI == null) || (valuesI.length == 0) ) {
		throw new Exception("Given value string array is empty");
	    }
	    
	    if ( (chanIdx < 0) || (chanIdx >= valuesI.length) ) {
		throw new Exception(new String("Filter is using illegal channel index: " + chanIdx)); 
	    }
	    
	    String valStr = valuesI[chanIdx].trim();
	    
	    // See if the value string represents a number
	    double val = 0;
	    try {
		val = Double.parseDouble(valStr);
	    } catch (NumberFormatException nfe) {
	    	throw new Exception(new String("Cannot apply filter: data string (" + valStr + ") does not represent a number"));
	    }
	    
	    if ( (minValue <= val) && (val <= maxValue) ) {
		return true;
	    }
	    
	    return false;
	    
	}
	
	/*
	 * Return a description for this filter.
	 */
	public String toString() {
	    String str =
	        new String(
	            "Filter on chan \"" + chanName +
	            "\" (index=" +
	            chanIdx +
	            "): values must be in the range " +
	            minValue +
	            " <= X <= " +
	            maxValue);
	    return str;
	}
	
    }

