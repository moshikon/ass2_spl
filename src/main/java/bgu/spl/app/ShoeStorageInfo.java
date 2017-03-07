package bgu.spl.app;

public class ShoeStorageInfo {
	/**
	 * An object which represents information about a single type of shoe in the store 
	 */
	private String shoeType; 
	private int amountOnStorage; 
	private int discountedAmount; 
    
	/**
	 * 
	 * @param shoeType the type of the shoe
	 * @param amountOnStorage the number of shoes of shoeType currently on the storage
	 * @param discountedAmount amount of shoes in this storage that can be sale in a discounted price
	 */
    public ShoeStorageInfo(String shoeType, int amountOnStorage, int discountedAmount) {
        this.shoeType = shoeType;
        this.amountOnStorage = amountOnStorage;
        this.discountedAmount = discountedAmount;
    }
    public String getShoeType() {
        return shoeType;
    }
    
    public int getAmountOnStorage() {
        return amountOnStorage;
    }
    
    public int getDiscountedAmount() {
        return discountedAmount;
    }
    
    public void setDiscountedAmount(int discountedAmount) {
        this.discountedAmount = discountedAmount;
    }
    
    public void setAmountOnStorage(int AmountOnStorage) {
        this.amountOnStorage = AmountOnStorage;
    }
    
    public void printDetails(){
		System.out.println("shoeType = " + this.shoeType);
		System.out.println("amountOnStorage = " + this.amountOnStorage);
		System.out.println("discountedAmount = " + this.discountedAmount);
        System.out.println();

	}
}