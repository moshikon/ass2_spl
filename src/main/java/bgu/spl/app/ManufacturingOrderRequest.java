package bgu.spl.app;
import bgu.spl.mics.Request;
public class ManufacturingOrderRequest implements Request<Receipt>{
	
	/**
	 * a request that is sent when the the store manager want that a
		shoe factory will manufacture a shoe for the store. Its response type expected to be a Receipt.
		On the case the manufacture was not completed successfully null should be returned as the
		request result
	 */
	private String shoe;
	private int amount;	
	private int tickToComplete=0;	
	private int tickPassed=0;	
	private int currentTick=-1;

	/**
	 * 
	 * @param shoe the type of shoe to manufacturing
	 * @param amount the amount of shoe to manufacturing
	 * @param currentTick the tick we are start manufacturing
	 */
	public ManufacturingOrderRequest (String shoe, int amount, int currentTick) {
        this.shoe = shoe;
        this.amount = amount;
        this.currentTick=currentTick;
	}
	
    public String getShoe() {
        return shoe;
    }
    
    public int getCurrentTick() {
        return currentTick;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public int getTickToComplete() {
        return tickToComplete;
    }
    
    public void setTickToComplete(int x) {
    	tickToComplete=x;
    }
    
    public int getTickPassed() {
        return tickPassed;
    }
    
    public void upTickPassed() {
    	tickPassed++;
    }
    

    
}