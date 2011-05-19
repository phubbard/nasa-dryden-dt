// FlightWatch
// subscribe to an RBNB channel, update Google Calendar upon new data
// Matt Miller 04/27/2011

import com.google.gdata.client.calendar.*;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.calendar.*;
import com.google.gdata.data.extensions.*;
import com.google.gdata.data.extensions.Reminder.Method;
import java.net.URL;
import java.util.TimeZone;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.rbnb.sapi.*;

public class FlightWatch {

//    private static String rbnbServer = "localhost:3333";
//    private static String monitorChan = "host-chat/chat/user";
    private static String rbnbServer ="indscore.dfrc.nasa.gov";

    // flights:
//    private static String monitorChan = "INDS_VM_ER2-806/ER2-806/_IWG1";
//    private static String monitorChan = "INDS_VM_DC8/DC8-817-TC4_IWG1/_IWG1";  
    private static String monitorChan = "INDS_VM_G3/N502/_IWG1";
    private static String flightName = "";

    private static String gmailUser = "dfrc.reveal.beacon";		// separately append @gmail.com suffix
    private static String gmailPW = "r3v3al#1";        // bleh

    private static double updateInc = 60.;        	// update/check interval (sec)
    private static double idleTimeOut = 600.;		// idle period timeout (sec)
    private static CalendarService myService;
    private static URL postUrl;
    private static boolean activeState=false;

    //---------------------------------------------------------------------------------
    // constructor
    public FlightWatch() {}

    public final static void main(String[] args) {
        Timer myTimer;

        int i=0;
        if(args.length>i) rbnbServer 	= args[i++];
        if(args.length>i) flightName 	= args[i++];
        if(args.length>i) monitorChan 	= args[i++];		// simple position dependent args for now
        if(args.length>i) updateInc 	= Double.parseDouble(args[i++]);
        if(args.length>i) idleTimeOut 	= Double.parseDouble(args[i++]);
        if(args.length>i) gmailUser 	= args[i++];
        if(args.length>i) gmailPW 		= args[i++];

        System.err.println("FlightWatch, rbnbServer: "+rbnbServer);
        System.err.println("FlightWatch, flightName: "+flightName);
        System.err.println("FlightWatch, monitorChan: "+monitorChan);
        System.err.println("FlightWatch, updateInc: "+updateInc);
        System.err.println("FlightWatch, idleTimeOut: "+idleTimeOut);
        System.err.println("FlightWatch, gmailUser: "+gmailUser);
        System.err.println("FlightWatch, gmailPW: "+gmailPW.charAt(0)+"*****");		// bleh
        
        initCalendar();
        activeState=false;

        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override public void run() { TimerMethod(); }
        }, 0,(int)(updateInc*1000.));         // check interval
    }

    //---------------------------------------------------------------------------------
    public static void initCalendar() {
        // Create a CalenderService and authenticate
        try {
            myService = new CalendarService(gmailUser);
            myService.setUserCredentials(gmailUser+"@gmail.com", gmailPW);
//            postUrl = new URL("http://www.google.com/calendar/feeds/"+gmailUser+"@gmail.com/private/full");
//            postUrl = new URL("http://www.google.com/calendar/feeds/edjnt3885jqm8qfio0eu28druc@group.calendar.google.com/private/full");// try secondary calendar
            postUrl = null;
            
         // Get list of calendars you own, find the ID of the one with title matching spec
            URL feedUrl = new URL("https://www.google.com/calendar/feeds/default/owncalendars/full");
            CalendarFeed resultFeed = myService.getFeed(feedUrl, CalendarFeed.class);
            System.err.println("Calendars you own:");
            for (int i = 0; i < resultFeed.getEntries().size(); i++) {
              CalendarEntry entry = resultFeed.getEntries().get(i);
              String calTitle = entry.getTitle().getPlainText();
              System.err.println("\t" + calTitle);
//              System.err.println("\t" + entry.getId());
              if(flightName.equals(calTitle)) {	// form postUrl for this titled entry
//            	  postUrl = new URL(entry.getId());		// bleh need to massage...
            	  postUrl = new URL(entry.getId().replace("/default/calendars","") + "/private/full");
            	  System.err.println("PostUrl for "+flightName+": "+postUrl);
              }
            }
            if(postUrl == null) {
            	System.err.println("ERROR: could not find calendar feed for: "+flightName);
            	System.exit(-1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    //---------------------------------------------------------------------------------
    public static void updateCalendar(String calTitle, String calMsg, double eTime) {
        final boolean useCurrentTime=true;	// use RT clock, ignore eTime from data

        try {
            // create event
            CalendarEventEntry myEntry = new CalendarEventEntry();
            myEntry.setTitle(new PlainTextConstruct(calTitle));
            myEntry.setContent(new PlainTextConstruct(calMsg));

            // timestamp at event time
            DateTime startTime; Date dTime;
            long tOffset = 0;    // msec offset for reminder to fire?
            if(useCurrentTime) {
                dTime = new Date((long)System.currentTimeMillis() + tOffset);
            } else {        // (may be lagged from current time)
                dTime = new Date((long)eTime + tOffset);
            }
            startTime = new DateTime(dTime, TimeZone.getDefault());    // avoid UTC-EDT time shift

            DateTime endTime = startTime;
            When eventTimes = new When();
            eventTimes.setStartTime(startTime);
            eventTimes.setEndTime(endTime);
            myEntry.addTime(eventTimes);
            
/*		// setting reminder to ALL enables web-set reminders for this event?
            int reminderMinutes = 0;
            Method methodType = Method.ALL;
            Reminder reminder = new Reminder();
            reminder.setMinutes(reminderMinutes);
            reminder.setMethod(methodType);
            myEntry.getReminder().add(reminder);
*/
//          myEntry.update();

            // Send the request (ignore the response):
            myService.insert(postUrl, myEntry);
            System.err.println("Update Calendar: " + calTitle + ", "+dTime);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    //---------------------------------------------------------------------------------
    static double lastTime=0.;
    static double clockTime=0.;
    static double lastClockTime=0.;

    private static void TimerMethod()
    {
    	long iclockTime = System.currentTimeMillis();
        clockTime = iclockTime / 1000.;        // seconds

        if(lastClockTime == 0.) {
            lastClockTime = clockTime;
            updateCalendar(flightName+": Monitoring", "Startup", clockTime);
        }

        try {
            // Create a sink
            Sink sink=new Sink();
            sink.OpenRBNBConnection(rbnbServer,"FlightWatch");

            // Pull data from the server:
            ChannelMap rMap = new ChannelMap();
            rMap.Add(monitorChan);

            sink.Request(rMap, 0., 0., "newest");
            ChannelMap gMap = sink.Fetch(10000);
            sink.CloseRBNBConnection();        // open/close every time

            String calMsg = "null";
            double eTime = 0.; double dTime = 0.; double idleTime = 0.;

            if(gMap != null && gMap.NumberOfChannels() > 0) {    // got data
                calMsg = gMap.GetDataAsString(0)[0];
                eTime = gMap.GetTimeStart(0);

                if(lastTime == 0.) lastTime = eTime;
                dTime = eTime - lastTime;

                if(gMap != null && gMap.NumberOfChannels() > 0 && dTime > 0.) {
                    lastClockTime = clockTime;    // active data, update time
                }
                idleTime = clockTime - lastClockTime;
            }

            System.err.println("FlightWatch("+flightName+
            		"), active: "+activeState+
            		", "+new Date(iclockTime)+
            		", idle: "+(float)idleTime);

            if((activeState == false) && (dTime>0.)) {
                System.err.println(flightName+": Active!");
                updateCalendar(flightName+": Active!", calMsg, eTime*1000.);
                activeState=true;
            }
            if((activeState==true) && (idleTime > idleTimeOut)) {
                System.err.println(flightName+": Idle.");
                updateCalendar(flightName+": Idle.", calMsg, eTime*1000.);
                activeState=false;
            }

            lastTime = eTime;

        } catch (Exception e){ 
        	System.err.println("OOPS, Exception in RBNB fetch: "+e); };
        	System.exit(-1);
    }
}
