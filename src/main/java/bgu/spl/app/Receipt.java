package bgu.spl.app;


public class Receipt {
	/**
	 * An object representing a receipt that should be sent to a client after buying a shoe (when the client’s PurchaseRequest completed
	 */
	
	private String seller; 
	private String customer; 
	private String shoeType; 
	private boolean discount; 
	private int issuedTick; 
	private int	requestTick; 
	private int amountSold;	
	
	/**
	 * 
	 * @param seller the name of the service which issued the receipt
	 * @param customer the name of the service this receipt issued to (the client name or “store”)
	 * @param shoeType the shoe type bought
	 * @param discount indicating if the shoe was sold at a discounted price
	 * @param issuedTick tick in which this receipt was issued
	 * @param requestTick tick in which the customer requested to buy the shoe
	 * @param amountSold the amount of shoes sold
	 */
	
	public Receipt (String seller, String customer, String shoeType, boolean discount, int issuedTick, int requestTick, int amountSold){
		this.seller=seller;
		this.customer=customer;
		this.shoeType=shoeType;
		this.discount=discount;
		this.issuedTick=issuedTick;
		this.requestTick=requestTick;
		this.amountSold=amountSold;
	}
	
	public String getSeller() {
        return seller;
    }
	public String getCustome() {
        return customer;
    }
	public String getShoeType() {
        return shoeType;
    }
	
	public boolean getDiscount() {
        return discount;
    }
	
	public int getIssuedTick() {
        return issuedTick;
    }
	
	public int getRequestTick() {
        return requestTick;
    }
	
	public int getAmountSold() {
        return amountSold;
    }
	
	public void printDetails(){
		System.out.println("seller = " + this.seller);
		System.out.println("customer = " + this.customer);
		System.out.println("shoeType = " + this.shoeType);
		System.out.println("discount = " + this.discount);
		System.out.println("issuedTick = " + this.issuedTick);
		System.out.println("requestTick = " + this.requestTick);
		System.out.println("amountSold = " + this.amountSold);
        System.out.println();

	}
}