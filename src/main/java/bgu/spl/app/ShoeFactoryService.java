package bgu.spl.app;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import bgu.spl.mics.MicroService;

public class ShoeFactoryService extends MicroService{
	/**
	 * This micro-service describes a shoe factory that manufacture shoes for the store. This micro-service 
	  	handles the ManufacturingOrderRequest it takes it exactly 1 tick to manufacture a single shoe, this
		means that completing a ManufacturingOrderRequest of 3 shoes will take it 3 ticks to complete
		(starting from tick following the request). When done manufacturing, this micro-service completes
		the request with a receipt (which has the value “store” in the customer field and “discount” = false).
T		he micro-service cannot manufacture more than one shoe per tic
	 */
	
	private int currentTick=-1;
    private CountDownLatch start;
    private CountDownLatch stop;
	private final static Logger LOGGER = Logger.getLogger(ShoeStoreRunner.class.getName()); 
    //Queue of manufacture requests
	private Queue<ManufacturingOrderRequest> Queue = new LinkedList<ManufacturingOrderRequest>();
	private boolean doNotCreate=false;
	
	/**
	 * 
	 * @param name of the factory
	 * @param start countDownLatch to start the threads
	 * @param stop countDownLatch to terminate the threads
	 */
	public ShoeFactoryService(String name,CountDownLatch start,CountDownLatch stop) {
		super(name);
        this.start=start;
        this.stop=stop;
	}
	
	@Override
	protected void initialize() {
			LOGGER.info("|||TIME:"+this.currentTick+"||||Service " + getName() + " started"); 

		  //tick update process
	        subscribeBroadcast(TickBroadcast.class, req -> {
	        	this.currentTick=req.getCurrentTick();

	        	//if tick == 0 then its time to stop
	        	if (currentTick==0){
	        		LOGGER.info("|||TIME:"+this.currentTick+"||||Service " + getName() + " terminate");
	        		stop.countDown();
	         	   terminate();
	         	  doNotCreate=true;
	        	}

	        	//every second we produce shoe. if there are enough to return the order we do so
	        	if (!Queue.isEmpty() &&!doNotCreate){
	        		if (Queue.peek().getTickToComplete()==Queue.peek().getTickPassed()+1){
	                	  LOGGER.info("|||TIME:"+this.currentTick+"||||Service " + getName() + " finished creating "+Queue.peek().getAmount()+" shoes type "+Queue.peek().getShoe()+"!!!");
	        			ManufacturingOrderRequest now = Queue.remove();
               			Receipt curr = new Receipt (this.getName(), "store", now.getShoe(), false, this.currentTick, now.getCurrentTick(), now.getAmount());
                        complete(now, curr);
                        if (!Queue.isEmpty() &&!doNotCreate){
                        	Queue.peek().upTickPassed();
                        	LOGGER.info("|||TIME:"+this.currentTick+"||||Service " + getName() + " created 1 more shoe type:"+Queue.peek().getShoe()+"");
                        }
        	        //if there are not enough shoes to return to the store the recipt
	        		}else{
	        			Queue.peek().upTickPassed();
	                	  LOGGER.info("|||TIME:"+this.currentTick+"||||Service " + getName() + " created 1 more shoe type:"+Queue.peek().getShoe()+"");
	        		}	        			        		
	        	}      	
	        });
	   
	        //every time we get a manufacture request we just add the request to queue
	        subscribeRequest(ManufacturingOrderRequest.class, req -> {
	        	req.setTickToComplete(req.getAmount()+1);
	        	Queue.add(req);
          	  LOGGER.info("|||TIME:"+this.currentTick+"||||Service " + getName() + " insert to queue aomunt: "+Queue.peek().getAmount()+" shoes type: "+Queue.peek().getShoe()+"!!!"); 	
	        	
            });	        
	      start.countDown();    	
	}
	
}