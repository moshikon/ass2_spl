package bgu.spl.app;

public class DiscountSchedule {
/**
 * An object which describes a schedule of a single discount that the manager will add to a specific
shoe at a specific tick
 */
	private String shoeType;
	private int tick;
	private int amount;

/**
 * 
 * @param shoeType the type of shoe to add discount to
 * @param tick the tick number to send the add the discount at
 * @param amount the amount of items to put on discount
 */
	public DiscountSchedule (String shoeType, int tick, int amount){
		this.shoeType=shoeType;
		this.tick=tick;
		this.amount=amount;

	}
	
	public int getTick (){
		return tick;
	}
	
	public String getShoe (){
		return shoeType;
	}
	
	public int getAmount (){
		return amount;
	}
	
	
}
