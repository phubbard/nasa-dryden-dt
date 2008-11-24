/*
	CPLDemux.java
	
	Java application to translate Cloud-Physics-LIDAR CPL binary packets into 
	comma-separated-value (CSV) strings, both in RBNB channels.
	
	2007/07/09  WHF  Created.
*/

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.rbnb.sapi.*;

public class CPLDemux
{
//*****************************  Constants  *********************************//
	/**
	  * Default number of frames to use in the cache.
	  */
	public static final int DEFAULT_CACHE = 1000;
	
	/**
	  * Number of LIDAR intensity bins.
	  */
	public static final int N_BINS = 833;
	
	/**
	  * CPL packet size.
	  */
	public static final int PACKET_LENGTH = 1678;
	
//***************************  Construction  ********************************//
	public CPLDemux()
	{
		dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		dateFormat.setTimeZone(calendar.getTimeZone());
	}		
	
//***************************  Public Methods  ******************************//
	public void parseArgs(String[] args) throws SAPIException
	{
		boolean help = false;	
		
		try {
			for (int ii = 0; ii < args.length; ++ii) {
				if (args[ii].charAt(0) != '-') {
					help = true;
					break;
				}
				
				switch (args[ii].charAt(1)) {
					case '?':
					case 'h':
					help = true;
					ii = args.length;
					break;
					
					case 'a':
					inputHost = args[++ii];
					break;
					
					case 'A':
					outputHost = args[++ii];
					break;
					
					case 'c':
					cache = Integer.parseInt(args[++ii]);
					break;
					
					case 'i':
					inputChannel = args[++ii];
					break;
					
					case 'o':
					parseOutputChannel(args[++ii]);
					break;
					
					case 'r':
					reference = args[++ii];
					break;
					
					case 's':
					start = Double.parseDouble(args[++ii]);
					break;
					
					case 'k':
					archiveAppend = true;
					archive = Integer.parseInt(args[++ii]);
					break;
					
					case 'K':
					archiveAppend = false;
					archive = Integer.parseInt(args[++ii]);
					break;
					
					default:
					help = true;
					break;		
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			help = true;
		}
		
		if (help) {
			showHelp();
			return;
		}
		
		go();
	}

//**************************  Private Methods  ******************************//
	private void connect() throws SAPIException
	{
		sink.OpenRBNBConnection(inputHost, "CPLDemuxSink");
		
		ChannelMap inputMap = new ChannelMap();
		inputMap.Add(inputChannel);

		if ("frames".equals(reference))		
			sink.Subscribe(inputMap);
		else
			sink.Subscribe(inputMap, start, duration, reference);
		
		source.SetRingBuffer(
				cache,
				archive == 0 ? "none" : (archiveAppend ? "append":"create"),
				archive
		);
		source.OpenRBNBConnection(outputHost, outputSource);		
	}
	
	private void close()
	{
		sink.CloseRBNBConnection();
		source.CloseRBNBConnection();
	}
	
	private void go() throws SAPIException
	{
		try {
			connect();
			
			ChannelMap outputMap = new ChannelMap();
			outputMap.Add(outputChannel);
			outputMap.Add(latencyChannel);
			
			ChannelMap dataMap = new ChannelMap();
			while (true) {
				sink.Fetch(-1, dataMap);
				boolean flush = false;
				try {
					flush = parseData(dataMap, outputMap);
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("Recovered.");
				}
				
				if (flush) source.Flush(outputMap);
			}
		} finally {
			close();
		}
	}
	
	/**
	  * Converts a Binary-Coded-Decimal byte to straight binary.
	  */
	private byte bcd2bin(byte bcd)
	{
		return (byte) ((bcd >>> 4) * 10 + (bcd & 0x0f));
	}
	
	/**
	  * Assumes that only one blob of data is received at a time, which is
	  *  accurate for Subscribe by frames and by time with duration zero.
	  *
	  * @return true if parsing is successful and the data should be flushed.
	  */
	private boolean parseData(ChannelMap in, ChannelMap out)
		throws SAPIException
	{
		//byte[] b = in.GetDataAsByteArray(0)[0];
		byte[] b = in.GetData(0);
		if (b.length != PACKET_LENGTH) return false;
		
		ByteBuffer bb = ByteBuffer.wrap(b);
		bb.order(java.nio.ByteOrder.LITTLE_ENDIAN);
		
		// Check magic numbers in the start of the packet:
		if ((bb.getInt(0) & 0xFFFF00FF) != 0x434D00ED) return false;
	
		StringBuffer sb = new StringBuffer();
		sb.append("CPL1,");

		calendar.clear();
		// Note that the CPL time fields are binary coded decimal.
		// Thus 35 minutes is encoded as 0x35, or 53.
		calendar.set(
				2000 + bcd2bin(bb.get(10)), // year
				bcd2bin(bb.get(9)) - 1,    // Calendar month is zero based
				bcd2bin(bb.get(8)),
				bcd2bin(bb.get(6)),        // we skip day-of-week
				bcd2bin(bb.get(5)),
				bcd2bin(bb.get(4))         // seconds
		);
		
		sb.append(dateFormat.format(calendar.getTime()));
		
		for (int ii = 0; ii < N_BINS; ++ii) {
			short raw = bb.getShort(12 + ii*2);  // 12 is start of data
			int rawU = raw & 0xFFFF;
			sb.append(',');
			sb.append(rawU);
		}
		
		sb.append("\r\n");
		
		out.PutTime(in.GetTimeStart(0), in.GetTimeDuration(0));
		out.PutDataAsString(0, sb.toString());
		
		// Add latency channel:
		long cplTime = calendar.getTimeInMillis();
		double[] delta = { in.GetTimeStart(0) - cplTime*1e-3 };
		out.PutDataAsFloat64(1, delta);
		
		return true;
	}
	
	private void parseOutputChannel(String o)
	{
		int start;
		
		if (o.charAt(0) == '/') start = 1; else start = 0; 
		
		int slash = o.indexOf('/', start);		
		outputSource = o.substring(start, slash);
		outputChannel = o.substring(slash+1);
		
		slash = outputChannel.lastIndexOf('/');
		if (slash > 0) // multipart channel, replace last segment
			latencyChannel = outputChannel.substring(0, slash+1) + "_Latency";
		else // replace entire segment
			latencyChannel = "_Latency";
	}

//**************************  Static Methods  *******************************//	
	public static void main(String args[]) throws Exception
	{
		(new CPLDemux()).parseArgs(args);		
	}
	
	private static void showHelp()
	{
		System.err.println("CPLDemux <options>");
		System.err.println(" -a <server address> : input RBNB server address [localhost:3333]");
		System.err.println(" -A <server address> : output RBNB server [localhost:3333]");
		//System.err.println(" -b                  : if set, use native bytes (default = swap)");
		System.err.println(" -i <name>           : input channel name [REQUIRED]");
		System.err.println(" -o <name>           : output channel name [REQUIRED]");
		System.err.println(" -s                  : subscription start [0]");
		System.err.println(" -r                  : subscription reference (frames, newest, oldest) [frames]");
		System.err.println(" -c <num>            : cache frames ["+DEFAULT_CACHE+"]");
		System.err.println(" -k <num>            : archive frames (append) [0]");
		System.err.println(" -K <num>            : archive frames (create) [0]");
		System.err.println(" -h                  : print this usage info");
	}

//****************************  Static Data  ********************************//
	private final Sink sink = new Sink();
	private final Source source = new Source();
	
	private boolean archiveAppend = false;
	private int cache = DEFAULT_CACHE, archive = 0;
	private double start = 0.0, duration = 0.0;
	private String inputHost = "localhost:3333",
			inputChannel,
			outputHost = "localhost:3333",
			outputSource,
			outputChannel,
			latencyChannel,
			reference = "frames";
			
	private final SimpleDateFormat dateFormat;
	private final Calendar calendar = Calendar.getInstance(
			java.util.TimeZone.getTimeZone("GMT")
	);
}


