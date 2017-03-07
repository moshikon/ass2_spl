package bgu.spl.app;

public class PurchaseSchedule {
/**
 * An object which describes a schedule of a single client-purchase at a specific tick.
 */
	private String shoeType;
	private int tick;
/**
 * 
 * @param shoeType the type of shoe to purchase
 * @param tick the tick number to send the PurchaseOrderRequestat
 */
	public PurchaseSchedule (String shoeType, int tick){
		this.shoeType=shoeType;
		this.tick=tick;
	}
	
	public int getTick (){
		return tick;
	}
	
	public String getShoe (){
		return shoeType;
	}
	
	
}
