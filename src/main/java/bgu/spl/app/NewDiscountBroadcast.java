package bgu.spl.app;

import bgu.spl.mics.Broadcast;

public class NewDiscountBroadcast implements Broadcast {
		/**
		 * a broadcast message that is sent when the manager of the store
			decides to have a sale on a specific shoe
		 */
	    private String shoe;
	    private int amount;

	    /**
	     * 
	     * @param shoe the type of shoe to add discount to
	     * @param amount the amount of shoe to add discount to
	     */
	    public NewDiscountBroadcast(String shoe, int amount) {
	        this.shoe = shoe;
	        this.amount = amount;
	    }

	    public String getShoe() {
	        return shoe;
	    }
	    
	    public int getAmount() {
	        return amount;
	    }
}
