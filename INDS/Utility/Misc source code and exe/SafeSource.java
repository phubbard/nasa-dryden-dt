	//SafeSource 
	//EMF/JPB 6.8.05
	
	// SafeSource flushes the cache after an idle time to ensure that data will be written to the disk.
	// Idle time is set by command line arg in program using SafeSource	


   import java.util.TimerTask;
   import java.util.Timer;
   import java.util.Date;
   import com.rbnb.sapi.Source;
   import com.rbnb.utility.ArgHandler;
   import com.rbnb.sapi.ChannelMap;
   import com.rbnb.sapi.Sink;
   import com.rbnb.sapi.Source;
   import com.rbnb.utility.ByteConvert;


    public class SafeSource extends Source {
    
      long dur;  //idle time
      long lastFlush;
   	
   	public SafeSource(int cache,String mode,int archive, long timerDuration)
		{
			super(cache,mode,archive);
         TimerClass t = new TimerClass(timerDuration);
		
		}    
		 
		 public SafeSource(long timerDuration) {
         super();
         TimerClass t = new TimerClass(timerDuration);
      }
   
       public int Flush(ChannelMap cm) throws com.rbnb.sapi.SAPIException{
         return(Flush(cm,false));
      }
   
       public int Flush(ChannelMap cm, boolean doSync) throws com.rbnb.sapi.SAPIException {
         lastFlush=System.currentTimeMillis();
         return(super.Flush(cm,doSync));
      }
       
       private class TimerClass extends TimerTask {
       
       	//set up timer			
          public TimerClass(long dur)
         {
            Timer timer = new Timer();        
            timer.schedule(this, dur, dur);
         }
      
      
      	 //check time, call ClearCache
          public void run()  {
          
            System.err.println("Idle time reached, flushing cache.");
          
            if(System.currentTimeMillis() - lastFlush > dur)
               try{
                  ClearCache();
               }
                   catch(Exception e){System.err.println(e);}           
         }
      }
   
   }




		
		
