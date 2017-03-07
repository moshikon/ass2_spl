package bgu.spl.app;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;
import bgu.spl.mics.MicroService;

public class TimeService extends MicroService {
		/**
		 * This micro-service is our global system timer (handles the clock ticks in the system). It is responsible
			for counting how much clock ticks passed since the beggining of its execution and notifying every
			other microservice (thats intersted) about it using the TickBroadcast.
			The TimeService receives the number of milliseconds each clock tick takes (speed:int) toogether
			with the number of ticks before termination (duration:int) as a constructor arguments.
			Be careful that you are not blocking the event loop of the timer micro-service. You can use the
			Timer class in java to help you with that.
			The current time always start from 1.
		 */
	    private int speed=0;
	    private int duration=0;
	    private int timePassed = 1;
	    private CountDownLatch start;
	    private CountDownLatch stop;
		private final static Logger LOGGER = Logger.getLogger(ShoeStoreRunner.class.getName()); 
    	Timer Timer = new Timer();

    	/**
    	 * 
    	 * @param speed the number of milliseconds each clock tick takes 
    	 * @param duration the number of ticks before termination
    	 * @param start countDownLatch to start the threads
    	 * @param stop countDownLatch to terminate the threads
    	 */
	    public TimeService(int speed, int duration,CountDownLatch start,CountDownLatch stop) {
	        super("timer");
	        this.speed = speed;
	        this.duration = duration;
	        this.start=start;
	        this.stop=stop;
	    }

  	  class RemindTask extends TimerTask {
	        public void run() {
		        sendBroadcast(new TickBroadcast(timePassed));
		        timePassed++; 
		        if (timePassed==duration+1){
		        	try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
		        	
			        sendBroadcast(new TickBroadcast(0));
			    	try {
			    		stop.await();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					};
					Store.getInstance().print();
		            Timer.cancel(); //Terminate the timer thread
		        }	            
	        }
	    }

	    
	    @Override
	    protected void initialize() {
	    	try {
	    		start.await();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			};
	    	LOGGER.info("|||TIME:"+this.timePassed+"||||Service " + getName() + " started");
	    	
	    	
	        subscribeBroadcast(TickBroadcast.class, req -> {
	        	
	        	//if tick == 0 then its time to stop
	        	if (req.getCurrentTick()==0){
	        		LOGGER.info("|||TIME:0||||Service " + getName() + " terminate");
	          	   terminate();
	         	}
	        });	        	        
	    	Timer.schedule(new RemindTask(), 0, speed);
      }
}

	
