// FlightWatch
// subscribe to an RBNB channel, update Google Calendar upon new data
// Matt Miller 04/27/2011

import com.google.gdata.client.calendar.*;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.calendar.*;
import com.google.gdata.data.extensions.When;
import java.net.URL;
import java.util.TimeZone;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.rbnb.sapi.*;
 
public class FlightWatch {
	
	private static String rbnbServer = "localhost:3333";
//	private static String gmailUser = "respira42";	
	private static String gmailUser = "matt.miller42";
	private static String gmailPW = "barkley1";		// bleh
//	private static String monitorChan = "RespiraWatch/event.txt";
	private static String monitorChan = "host-chat/chat/user";
	private static double updateInc = 10.;		// update interval (sec)
	private static CalendarService myService;
    private static URL postUrl;
	private static boolean activeState=false;
	
// constructor
    public FlightWatch() {}  

    public final static void main(String[] args) {
    	Timer myTimer;
    	
    	if(args.length>0) rbnbServer = args[0];
        System.err.println("FlightWatch, connecting to: "+rbnbServer);
        	
        initCalendar();
        activeState=false;
        
    	myTimer = new Timer();
    	myTimer.schedule(new TimerTask() {
    		@Override public void run() { TimerMethod(); }
    	}, 0,(int)(updateInc*1000.)); 		// check interval	
    }   

    public static void initCalendar() {
		// Create a CalenderService and authenticate
    	try {
        	myService = new CalendarService(gmailUser);
        	myService.setUserCredentials(gmailUser+"@gmail.com", gmailPW);
            postUrl = new URL("http://www.google.com/calendar/feeds/"+gmailUser+"@gmail.com/private/full");
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public static void updateCalendar(String calTitle, String calMsg, double eTime) {
    	try {
            // create event
            CalendarEventEntry myEntry = new CalendarEventEntry();
            myEntry.setTitle(new PlainTextConstruct(calTitle));
            myEntry.setContent(new PlainTextConstruct(calMsg));
                        
            // timestamp at event time (may be lagged from current time)
//          DateTime startTime = DateTime.now();
            Date dTime = new Date((long)eTime);
            DateTime startTime = new DateTime(dTime, TimeZone.getDefault());	// avoid UTC-EDT time shift
            DateTime endTime = startTime;
            When eventTimes = new When();
            eventTimes.setStartTime(startTime);
            eventTimes.setEndTime(endTime);
            myEntry.addTime(eventTimes);
//System.err.println("startTime: "+startTime);
                        
            // Send the request (ignore the response):
            myService.insert(postUrl, myEntry);          
    	} catch (Exception e) {
    		e.printStackTrace();
        }
    } 
        
    //---------------------------------------------------------------------------------	
	static double lastTime=0.;
	static double idleTimeOut=60.;	
	static double clockTime=0.;
	static double lastClockTime=0.;
	
    private static void TimerMethod()
    {
    	clockTime = System.currentTimeMillis() / 1000.;		// seconds
    	
    	if(lastClockTime == 0.) lastClockTime = clockTime;
    	
        try {
            // Create a sink
            Sink sink=new Sink();
            sink.OpenRBNBConnection(rbnbServer,"FlightWatch");
                        
            // Pull data from the server:
            ChannelMap rMap = new ChannelMap();
            rMap.Add(monitorChan);

            sink.Request(rMap, 0., 0., "newest");
            ChannelMap gMap = sink.Fetch(10000);
            sink.CloseRBNBConnection();		// open/close every time
            
        	String calMsg = gMap.GetDataAsString(0)[0];
        	double eTime = gMap.GetTimeStart(0);
        	if(lastTime == 0.) lastTime = eTime;
        	double dTime = eTime - lastTime;
        	
            if(gMap != null && gMap.NumberOfChannels() > 0 && dTime > 0.) {
            	lastClockTime = clockTime;	// active data, update time
            }
        	double idleTime = clockTime - lastClockTime;
            System.err.println("eTime: "+eTime+", deltaTime: "+dTime+", idleTime: "+idleTime);
            
            if((activeState == false) && (dTime>0.)) {
        		System.err.println("Going Active!");
        		updateCalendar("Active!", calMsg, eTime*1000.);
        		activeState=true;
            }
        	if((activeState==true) && (idleTime > idleTimeOut)) {
        		System.err.println("Going Idle.");
        		updateCalendar("Idle.", calMsg, eTime*1000.);
        		activeState=false;
        	}
        	
			lastTime = eTime;
        	
        } catch (Exception e){ System.err.println("OOPS, Exception in RBNB fetch"); };
            
    }
}
