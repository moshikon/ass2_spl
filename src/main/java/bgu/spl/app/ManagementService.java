package bgu.spl.app;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;
import bgu.spl.mics.MicroService;

public class ManagementService extends MicroService{
	
	/**
	 * This micro-service can add discount to shoes in the store and send NewDiscountBroadcast to notify
	clients about them.  In order to do so, this service expects to get a list of DiscountSchedule as
	argument to its constructor (the list does not guaranteed to be ordered). In addition, the ManagementService
	handles RestockRequests that is being sent by the SellingService.
 	Whenever a RestockRequest of a specific shoe type recived the service first check that
	this shoe type is not already on order (and if it does, it checks that there are enough ordered to
	give one to the seller) if it doesnt (or the ordered amount was not enough) it will send a ManufacturingOrderRequest
	for (current-tick %5) + 1 shoes of this type, when this order completes - it update the store stock, file the receipt and only then complete the RestockRequest (and not before)
	with the result of true. If there were no one that can handle the ManufacturingOrderRequest
	 */
	private ConcurrentHashMap<String ,Object[]> Orders_ = new ConcurrentHashMap<String ,Object[]>();//[0] is how much people want this shoe, [1] is how much order	
	private List<DiscountSchedule> DiscountSchedule;
	private int currentTick=-1;
    private CountDownLatch start;
    private CountDownLatch stop;
	private final static Logger LOGGER = Logger.getLogger(ShoeStoreRunner.class.getName()); 

	/**
	 * 
	 * @param DiscountSchedule An object which describes a schedule of a single discount that the manager will add to a specificshoe at a specific tick
	 * @param start countDownLatch to start the threads
	 * @param stop countDownLatch to terminate the threads
	 */
	public ManagementService(List<DiscountSchedule> DiscountSchedule,CountDownLatch start,CountDownLatch stop) {
		super("manager");
		this.DiscountSchedule=sortD(DiscountSchedule);
        this.start=start;
        this.stop=stop;
	}
	
	@SuppressWarnings("unchecked")
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
	        	}
	        	
	        	//every second we check if there is discount scheduled
        		int counter=0;
        		for(int i=0; i<DiscountSchedule.size(); i++){
        			if(currentTick==DiscountSchedule.get(i).getTick()){
        				counter++;
        			}
        		}
        		for(int i=0; i<counter; i++){
			        		Store.getInstance().addDiscount(DiscountSchedule.get(0).getShoe(), DiscountSchedule.get(0).getAmount());
			        		sendBroadcast(new NewDiscountBroadcast(DiscountSchedule.get(0).getShoe(), DiscountSchedule.get(0).getAmount()));
			        		DiscountSchedule.remove(0);       			
        		}

        });
	        
        	//this is the process that send manufacture requests
	        subscribeRequest(RestockRequest.class, req -> {

	        	//first we check if we order a shoe at least one time
	        	if (!Orders_.containsKey(req.getShoe())){
	        		Object[] shoe = new Object[3];
	        		shoe[0]=1;
	        		shoe[1]=0;
	        		List<ManufacturingOrderRequest> Queue = new ArrayList<ManufacturingOrderRequest>();
	        		shoe[2]=Queue;
	        		Orders_.put(req.getShoe(), shoe);
	        	}else{
	        		Orders_.get(req.getShoe())[0]= (int)Orders_.get(req.getShoe())[0]+1;
	        	}
        	    ((ArrayList<RestockRequest>)Orders_.get(req.getShoe())[2]).add(req);

	        	//then we check if there are enough shoes already
	        	if ((int)Orders_.get(req.getShoe())[1]<(int)Orders_.get(req.getShoe())[0]){
	        		int willBeOrdered = (currentTick%5) + 1;
		        	boolean success = sendRequest(new ManufacturingOrderRequest(req.getShoe(), willBeOrdered, this.currentTick), v -> {
		                  if (v==null){
		                	  LOGGER.info("|||TIME:"+this.currentTick+"||||Service " + getName() + " FAILED to got new stock. shoe: "+req.getShoe()+" recipt==null");
		                  }else{
			                   LOGGER.info("|||TIME:"+this.currentTick+"||||Service " + getName() + " got new stock! shoe: "+v.getShoeType()+" ammount: "+v.getAmountSold()+"");
			                   Store.getInstance().file(v);
			                   decreaseAmount(v.getShoeType(),v.getAmountSold());
		                  }  
		        	});

			          if (success) {
                    	  LOGGER.info("|||TIME:"+this.currentTick+"||||Service " + getName() + " send a request to create shoes "+req.getShoe()+" anmount:"+willBeOrdered+"");
      	        			Orders_.get(req.getShoe())[1]= (int)Orders_.get(req.getShoe())[1]+willBeOrdered;


			          }else {
			        	  	LOGGER.info("|||TIME:"+this.currentTick+"||||Service " + getName() + " tried to send a request but no one cares");
			        	  complete(req, false);	        			
			          }
	        	}	
	        });  
	        start.countDown();
	}

    /**
     * 	sort algorithm by tick for the Discount Schedule
     */
	private List<DiscountSchedule> sortD (List<DiscountSchedule> DiscountSchedule){
		DiscountSchedule [] arr = new DiscountSchedule [DiscountSchedule.size()];
		for (int i=0; i<DiscountSchedule.size(); i++){
			arr [i] = DiscountSchedule.get(i) ;
		}
		DiscountSchedule temp;
	       for (int i = 1; i < arr.length; i++) {
	            for(int j = i ; j > 0 ; j--){
	                if(arr[j].getTick() < arr[j-1].getTick()){
	                    temp = arr[j];
	                    arr[j] = arr[j-1];
	                    arr[j-1] = temp;
	                }
	            }
	        }
				
		List<DiscountSchedule> soredlist = new ArrayList<DiscountSchedule>();
		for (int i=0; i<arr.length; i++){
			soredlist.add(arr[i]);	
		}
		return soredlist;
	}
	
	/**
	 * 
	 * @param ShoeType the function decrease the amount of this ShoeType
	 * @param AmountSold the function decrease this amount from the ShoeType
	 */
	private void decreaseAmount(String ShoeType, int AmountSold){
		int amount = Math.min(AmountSold, ((ArrayList<?>)Orders_.get(ShoeType)[2]).size());
        for (int i=0; i<amount; i++){
        	RestockRequest now = (RestockRequest) ((ArrayList<?>)Orders_.get(ShoeType)[2]).remove(0);
            complete(now, true);
        	}

	  	Orders_.get(ShoeType)[1]=(int)Orders_.get(ShoeType)[1]-amount;
	  	Orders_.get(ShoeType)[0]=(int)Orders_.get(ShoeType)[0]-amount;
		Store.getInstance().add(ShoeType, AmountSold-(amount));

	}
}