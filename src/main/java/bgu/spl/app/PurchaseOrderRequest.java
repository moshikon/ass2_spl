package bgu.spl.app;

import bgu.spl.mics.Request;

public class PurchaseOrderRequest implements Request<Receipt> {
    /**
     * a request that is sent when the a store client wish to buy a shoe.
		Its response type expected to be a Receipt.  On the case the purchase was not completed
		successfully null should be returned as the request result
     */
	private String senderName;
	private Receipt result = null;
	private String shoeType;
	private boolean discount;
	private int	currentTick; 

	/**
	 * 
	 * @param senderName the name of the costumer
	 * @param shoeType the shoe that the costumer wants
	 * @param discount true iff the customer want the shoe only with discount
	 * @param currentTick tick in which the customer requested to buy the shoe
	 */
    public PurchaseOrderRequest (String senderName, String shoeType, boolean discount, int	currentTick) {
        this.senderName = senderName;
        this.shoeType=shoeType;
        this.discount=discount;
        this.currentTick=currentTick;        
    }

    public String getSenderName() {
        return senderName;
    }
   
    public String getShoeType() {
        return shoeType;
    }
    
    public Receipt getResult() {
        return result;
    }
    
    public boolean getDiscount() {
        return discount;
    }
    
    public int getCurrentTick() {
        return currentTick;
    }
    
}