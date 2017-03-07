package bgu.spl.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;
import bgu.spl.mics.MicroService;

public class WebsiteClientService extends MicroService {
	/**
	 * This micro-service describes one client connected to the web-site
	 */
	private List<PurchaseSchedule> PurchaseSchedule;
	private Set<String> wishList;
	private int currentTick=-1;
    private CountDownLatch start;
    private CountDownLatch stop;
	private final static Logger LOGGER = Logger.getLogger(ShoeStoreRunner.class.getName()); 
	/**
	 * 
	 * @param name
	 * @param PurchaseSchedule ontains purchases that the client needs to make (every purchase has a corresponding time tick to send the
		PurchaseRequest). The list does not guaranteed to be sorted. Important: The WebsiteClientService will make the purchase on
		the tick specied on the schedule irrelevant of the discount on that item
	 * @param wishList he client wish list contains name of shoe types that the client
		will buy only when there is a discount on them (and immidiatly when he found out of such
		discount). Once the client bought a shoe from its wishlist - he removes it from the list
	 * @param start countDownLatch to start the threads
	 * @param stop countDownLatch to terminate the threads
	 */
	public WebsiteClientService (String name, List<PurchaseSchedule> PurchaseSchedule, Set<String> wishList,CountDownLatch start,CountDownLatch stop){
		super (name);
		this.PurchaseSchedule = sort(PurchaseSchedule);	
		this.wishList=wishList;
        this.start=start;
        this.stop=stop;
	}

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
        	
        	//every second we check if there is Purchase scheduled
    		int counter=0;
    		for(int i=0; i<PurchaseSchedule.size(); i++){
    			if(currentTick==PurchaseSchedule.get(i).getTick()){
    				counter++;
    			}
    		}
    		for(int i=0; i<counter; i++){
	        	PurchaseSchedule now = PurchaseSchedule.get(0);
	        	if (currentTick==now.getTick()){
	      		  boolean success = sendRequest(new PurchaseOrderRequest(this.getName(), now.getShoe(), false, currentTick), v -> {
	                  if (v==null){
	                	  LOGGER.info("|||TIME:"+this.currentTick+"||||Service " + getName() + " FAILED to buy shoe: "+now.getShoe()+" recipt==null");
	                  }else{
	                	  LOGGER.info("|||TIME:"+this.currentTick+"||||Service " + getName() + " just bought shoe: "+v.getShoeType()+", discount:"+v.getDiscount()+"");
	                      PurchaseSchedule.remove(v.getShoeType());
	                      }  
	              });
	
	              if (success) {
                	  LOGGER.info("|||TIME:"+this.currentTick+"||||Service " + getName() + " send a request to buy shoe: "+now.getShoe()+" discount: no");
	              }else {
                	  LOGGER.info("|||TIME:"+this.currentTick+"||||Service " + getName() + " tried to send a request to buy but no one cares");
	              }
	           }
	     PurchaseSchedule.remove(0);
    	}

		});


    	//this is the possess that listen to discounts
        subscribeBroadcast(NewDiscountBroadcast.class, req -> {
        	String shoe = req.getShoe();
        	if (wishList.contains(shoe) && req.getAmount()>0){
        		  boolean success = sendRequest(new PurchaseOrderRequest(this.getName(), shoe, true, currentTick), v -> {
	                  if (v==null){
	                	  LOGGER.info("|||TIME:"+this.currentTick+"||||Service " + getName() + " FAILED to buy shoe: "+req.getShoe()+" recipt==null");
	                  }else{
	                	  LOGGER.info("|||TIME:"+this.currentTick+"||||Service " + getName() + " just bought shoe: "+v.getShoeType()+", discount:"+v.getDiscount()+"");
	                      wishList.remove(shoe);
	                  }
                  });

                  if (success) {
                	  LOGGER.info("|||TIME:"+this.currentTick+"||||Service " + getName() + " send a request to buy shoe: "+req.getShoe()+" discount: yes");
                  }else {
                	  LOGGER.info("|||TIME:"+this.currentTick+"||||Service " + getName() + " tried to send a request to buy but no one cares");
                  }
           	}        
        });
        
       start.countDown();
    }

    
    //sort algorithm
	private List<PurchaseSchedule> sort (List<PurchaseSchedule> PurchaseSchedule){
		PurchaseSchedule [] arr = new PurchaseSchedule [PurchaseSchedule.size()];
		for (int i=0; i<PurchaseSchedule.size(); i++){
			arr [i] = PurchaseSchedule.get(i) ;
		}
		
		for (int i = 1; i < arr.length; i++) {
			PurchaseSchedule temp = arr[i];
			int j = i;
			while (j > 0 && arr[j - 1].getTick() > arr[i].getTick()) {
				arr[j] = arr[j - 1];
				j--;
			}
			arr[j] = temp;
		}
		
		List<PurchaseSchedule> soredlist = new ArrayList<PurchaseSchedule>();
		for (int i=0; i<arr.length; i++){
			soredlist.add(arr[i]);	
		}
		return soredlist;
	}
}
	
	

