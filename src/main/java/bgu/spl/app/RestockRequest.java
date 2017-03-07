package bgu.spl.app;
import bgu.spl.mics.Request;

public class RestockRequest implements Request<Boolean> {
	/**
	 * a request that is sent by the selling service to the store manager so that he
	will know that he need to order new shoes from a factory. Its response type expected to be a
	boolean where the result: true means that the order is complete and the shoe is reserved for
	the selling service and the result: false means that the shoe cannot be ordered (because there
	were no factories available).
	 */
	private String shoeType;
	private int amount;	//the amount of shoes sold
	private String sender;
	
	/**
	 * 
	 * @param sender the selling service
	 * @param shoeType the shoe that we want
	 * @param amount the amount that we want
	 */
    public RestockRequest(String sender, String shoeType, int amount) {
    	this.shoeType=shoeType;
    	this.sender=sender;
    }

    public String getShoe() {
        return shoeType;
    }
    
    public String getSender() {
        return sender;
    }
    
    public int getAmount() {
        return amount;
    }

}
