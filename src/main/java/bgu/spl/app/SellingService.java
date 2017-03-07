package bgu.spl.app;

import bgu.spl.mics.MicroService;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;
import bgu.spl.app.BuyResult;

public class SellingService extends MicroService {
	/**
	 * This micro-service handles PurchaseOrderRequest. When the SellingService receives a Purchase-OrderRequest
	, it handles it by trying to take the required shoe from the storage. If it succedded
	it creates a recipt, file it in the store and pass it to the client (as the result of completing the
	PurchaseOrderRequest). If there were no shoes on the requested type on stock, the selling service
	will send RestockRequest, if the request completed with the value “false” (see ManagementService)
	the SellingService will complete the PurchaseOrderRequest with the value of “null” (to indicate to
	the client that the purchase was unsuccessfull). If the client indicates in the order that he wish to
	get this shoe only on discount and no more discounted shoes are left then it will complete the client
	request with null result.
	 */
	
	int currentTick=-1;
    private CountDownLatch start;
    private CountDownLatch stop;
	private final static Logger LOGGER = Logger.getLogger(ShoeStoreRunner.class.getName()); 

	/**
	 * 
	 * @param name of the seller
	 * @param start countDownLatch to start the threads
	 * @param stop countDownLatch to terminate the threads
	 */
	public SellingService (String name,CountDownLatch start,CountDownLatch stop){
		super (name);
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
        });

    	//this is the process that listen to PurchaseOrderRequest
        subscribeRequest(PurchaseOrderRequest.class, req -> {
        	BuyResult a = Store.getInstance().take(req.getShoeType(), req.getDiscount());

        		if ( a == BuyResult.REGULAR_PRICE || a == BuyResult.DISCOUNTED_PRICE){
        			boolean discount;
        			if(a == BuyResult.REGULAR_PRICE){
        				discount=false;
        			}else{
        				discount=true;
        			}
              	  LOGGER.info("|||TIME:"+this.currentTick+"||||Service " + getName() + " sold a shoe: "+req.getShoeType()+"");
         			Receipt curr = new Receipt (this.getName(), req.getSenderName(), req.getShoeType(), discount, this.currentTick, req.getCurrentTick(), 1);
         			Store.getInstance().file(curr);
                    complete(req, curr);
        		}else if (a == BuyResult.NOT_ON_DISCOUNT){
                	  LOGGER.info("|||TIME:"+this.currentTick+"||||Service " + getName() + " didnt sold shoe: "+req.getShoeType()+" NOT_ON_DISCOUNT");
                    complete(req, null);
        		}else{
            		  boolean success = sendRequest(new RestockRequest(req.getSenderName(), req.getShoeType(), 1), v -> {
            			  if (v==false){
    	                	  LOGGER.info("|||TIME:"+this.currentTick+"||||Service " + getName() + " got NO ans from manager to stock request. shoe: "+req.getShoeType()+"");
                               complete(req, null);    			       			
                          }else{
    	                	  LOGGER.info("|||TIME:"+this.currentTick+"||||Service " + getName() + " got YES ans from manager to stock request. shoe: "+req.getShoeType()+"");
                   			Receipt curr = new Receipt (this.getName(), req.getSenderName(), req.getShoeType(), false, this.currentTick, req.getCurrentTick(), 1);
                 			Store.getInstance().file(curr);
                            complete(req, curr);
                          }

                      });

                      if (success) {
                    	  LOGGER.info("|||TIME:"+this.currentTick+"||||Service " + getName() + " send a request to refill stock with shoe: "+req.getShoeType()+"");
                      }else {
                    	  LOGGER.info("|||TIME:"+this.currentTick+"||||Service " + getName() + " tried to send a request but no one cares");
                      }
        		}
             });
		start.countDown();
    }

}
	
	
