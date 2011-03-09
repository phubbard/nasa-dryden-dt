
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.Client;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.Source;

public class XPlanePacketParser {
    
    public static final void main(String[] argsI) throws Exception {
	
	if (argsI.length != 1) {
	    System.err.println("Usage: java XPlanePacketParser <RBNB host:port>");
	    System.exit(-1);
	}
	
	Source src = new Source(1000,"none",0);
	src.OpenRBNBConnection(argsI[0], "XPlaneParserSource");
	
	Sink snk = new Sink();
	snk.OpenRBNBConnection(argsI[0], "XPlaneParserSink");
	ChannelMap rmap = new ChannelMap();
	rmap.Add("xplane-cap/UDP");
	snk.Subscribe(rmap);
	
	while (true) {
	    ChannelMap dataMap = snk.Fetch(1000);
	    if (dataMap.GetIfFetchTimedOut()) {
		continue;
	    }
	    byte[] packetBytes = dataMap.GetData(0);
	    if (packetBytes.length != 77) {
		System.err.println("Packet size = " + packetBytes.length + ", expected 77; ignoring packet");
		continue;
	    } else {
		ByteBuffer packetBB = ByteBuffer.wrap(packetBytes);
		packetBB = packetBB.order(ByteOrder.LITTLE_ENDIAN);
		
		try {
		    
		    System.err.println("New packet:");
		    
		    // Read the 5 character header
		    byte[] header = new byte[5];
		    header[0] = packetBB.get();
		    header[1] = packetBB.get();
		    header[2] = packetBB.get();
		    header[3] = packetBB.get();
		    header[4] = packetBB.get();
		    String headerStr = new String(header);
		    System.err.println("    Header: " + headerStr);
		    
		    // Read the ID of the first 36-byte structure (should be 18; pitch/roll/headings)
		    int id = packetBB.getInt();
		    if (id != 18) {
			throw new Exception("ID of first structure is " + id + ", expected 18");
		    }
		    System.err.println("    ID of first structure is " + id);
		    
		    // Read 8 single precision floats
		    float[] orientationData = new float[8];
		    for (int i=0; i<orientationData.length; ++i) {
			orientationData[i] = packetBB.getFloat();
			System.err.println("    orientationData[" + i + "] = " + orientationData[i]);
		    }
		    
		    // Read the ID of the second 36-byte structure (should be 20; lat/lon/altitude)
		    id = packetBB.getInt();
		    if (id != 20) {
			throw new Exception("ID of first structure is " + id + ", expected 20");
		    }
		    System.err.println("    ID of second structure is " + id);
		    
		    // Read 8 single precision floats
		    float[] posData = new float[8];
		    for (int i=0; i<posData.length; ++i) {
			posData[i] = packetBB.getFloat();
			System.err.println("    posData[" + i + "] = " + posData[i]);
		    }
		    
		    // Flush parsed data back to the RBNB
		    ChannelMap srcMap = new ChannelMap();
		    double time = System.currentTimeMillis()/1000.0;
		    srcMap.PutTime(time,0.0);
		    // Alt
		    int chanNum = srcMap.Add("Alt");
		    float[] altData = new float[1];
		    altData[0] = posData[2];
		    srcMap.PutDataAsFloat32(chanNum,altData);
		    // Lat
		    chanNum = srcMap.Add("Lat");
		    float[] latData = new float[1];
		    latData[0] = posData[0];
		    srcMap.PutDataAsFloat32(chanNum,latData);
		    // Lon
		    chanNum = srcMap.Add("Lon");
		    float[] lonData = new float[1];
		    lonData[0] = posData[1];
		    srcMap.PutDataAsFloat32(chanNum,lonData);
		    // Heading
		    chanNum = srcMap.Add("Heading");
		    float[] headingData = new float[1];
		    headingData[0] = orientationData[2];
		    srcMap.PutDataAsFloat32(chanNum,headingData);
		    // Pitch
		    chanNum = srcMap.Add("Pitch");
		    float[] pitchData = new float[1];
		    pitchData[0] = orientationData[0];
		    srcMap.PutDataAsFloat32(chanNum,pitchData);
		    // Roll
		    chanNum = srcMap.Add("Roll");
		    float[] rollData = new float[1];
		    rollData[0] = orientationData[1];
		    srcMap.PutDataAsFloat32(chanNum,rollData);
		    src.Flush(srcMap);
		    
		    // Flush a CSV string to the RBNB
		    ChannelMap csvMap = new ChannelMap();
		    csvMap.PutTime(time,0.0);
		    csvMap.Add("_CSV");
		    String format = new String("yyyy-MM-dd'T'HH:mm:ss.SSS z");
		    SimpleDateFormat sdf = new SimpleDateFormat(format);
		    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		    // Convert time from seconds since epoch to milliseconds since epoch
		    Date date = new Date( (long)(time * 1000.0) );
		    String dateStr = sdf.format(date);
		    String csvStr = new String("XPLANE," + dateStr + "," + altData[0] + "," + latData[0] + "," + lonData[0] + "," + headingData[0] + "," + pitchData[0] + "," + rollData[0] + "\r\n");
		    csvMap.PutDataAsString(0,csvStr);
		    src.Flush(csvMap);
		    
		} catch (BufferUnderflowException bue) {
		    System.err.println("    Error parsing packet: " + bue);
		} catch (Exception e) {
		    System.err.println("    Error parsing packet: " + e);
		}
		
	    }
	}
	
    }
    
}

