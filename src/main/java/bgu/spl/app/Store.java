package bgu.spl.app;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import bgu.spl.app.BuyResult;;

public class Store {
	/**
	 * This object must be implemented as a thread safe singleton 
	 * The store object holds a collection of ShoeStorageInfo : One for each shoe type the store offers. In
		addition, it contains a list of receipts issued to and by the store
	 */
		private final static Logger LOGGER = Logger.getLogger(ShoeStoreRunner.class.getName()); 
		private ConcurrentHashMap<String ,ShoeStorageInfo> Stock_ = new ConcurrentHashMap<String ,ShoeStorageInfo>();	
		private ArrayList<Receipt> Receipts_ = new ArrayList<Receipt>();

	   private static class StoreHolder {
	        private static Store instance = new Store();
	    }

	   private Store() {
	        // initialization code..
	    }
	    public static Store getInstance() {
	        return StoreHolder.instance;
	    }
	    
	    /**
	     * This method should be called in order to initialize the store storage before starting an execution
		(by the ShoeStoreRunner class defined later). The method will add the items in the given array to
		the store
	     * @param storage the store storage (shoes,amounts) before starting an execution
	     */
	    public void load (ShoeStorageInfo [] storage){	    	
	    	for (int i=0; i<storage.length ; i++){
	    		Stock_.put(storage[i].getShoeType(), storage[i]);
	            LOGGER.info("|||TIME:STORE||||shoe "+storage[i].getShoeType()+ " and ammount "+ storage[i].getAmountOnStorage()+" added to store");
	    	}	    	
	    }  
	/**
	 * This method will attempt to take a single showType from the store. It receives the shoeType to
		take and a boolean - onlyDiscount which indicates that the caller wish to take the item only if it is
		in discount
	 * @param shoeType the shoe the costumer want
	 * @param onlyDiscount true iff the costumer want the shoe in discount
	 * @return the answer of the buying
	 */
	    public BuyResult take ( String shoeType , boolean onlyDiscount){
	    	if (Stock_.containsKey(shoeType)){
	    		if (Stock_.get(shoeType).getAmountOnStorage()>0){
		    		if (Stock_.get(shoeType).getDiscountedAmount()>0){
		    			Stock_.get(shoeType).setDiscountedAmount(Stock_.get(shoeType).getDiscountedAmount()-1);
		    			Stock_.get(shoeType).setAmountOnStorage(Stock_.get(shoeType).getAmountOnStorage()-1);
			            LOGGER.info("|||TIME:STORE||||shoe "+shoeType+ " bought WITH discount from store");
		    			return BuyResult.DISCOUNTED_PRICE;
		    		}
		    		if (onlyDiscount){
		    			return BuyResult.NOT_ON_DISCOUNT;
		    		}else{
		    			Stock_.get(shoeType).setAmountOnStorage(Stock_.get(shoeType).getAmountOnStorage()-1);
			            LOGGER.info("|||TIME:STORE||||shoe "+shoeType+ " bought WITHOUT discount from store");
		    			return BuyResult.REGULAR_PRICE;
		    		}
	    		}
	    		if (onlyDiscount){
	    			return BuyResult.NOT_ON_DISCOUNT;
	    		}
    			return BuyResult.NOT_IN_STOCK;
	    	}
    		if (onlyDiscount){
    			return BuyResult.NOT_ON_DISCOUNT;
    		}
			return BuyResult.NOT_IN_STOCK;
	    }
		/**
		 * This method adds the given amount to the ShoeStorageInfo of the given shoeType
		 * @param shoeType the shoe to put in the storage  
		 * @param amount the amount of the shoe
		 */
	    public void add (String shoeType , int amount ){
	    	if (Stock_.containsKey(shoeType)){
    			Stock_.get(shoeType).setAmountOnStorage(Stock_.get(shoeType).getAmountOnStorage()+amount);
	    	}else{
	    		ShoeStorageInfo curr = new ShoeStorageInfo(shoeType, amount, 0);
	    		Stock_.put(shoeType, curr);
	    	}
	    }
	    /**
	     * Adds the given amount to the corresponding ShoeStorageInfo ’s discountedAmount field
	     * @param shoeType the shoe that is getting the discount
	     * @param amount the amount of shoes that are add discount
	     */
	    public void addDiscount (String shoeType , int amount) {   	
	    	if(Stock_.get(shoeType)!=null){
    			if (Stock_.get(shoeType).getAmountOnStorage()>0){
    				if (Stock_.containsKey(shoeType)){
    					int curr = Math.min(Stock_.get(shoeType).getAmountOnStorage()-Stock_.get(shoeType).getDiscountedAmount(), amount);
    					Stock_.get(shoeType).setDiscountedAmount(Stock_.get(shoeType).getDiscountedAmount()+curr);
		        		LOGGER.info("||||Service manager publish an Discount on shoe:"+shoeType+" and amount:"+amount+"");
    				}
    			}
	    	}
	    }
	    /**
	     * Save the given receipt in the store
	     * @param receipt the receipt for the purchase
	     */
	    public void file (Receipt receipt){
	    	Receipts_.add(receipt);
	    }
/**
 * this method prints to the standard output:
  	For each item on stock - its name, amount and discountedAmount
	For each receipt filed in the store - all its fields
 */
	    public void print (){
	        System.out.println();				
	        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>> STOCK <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
	        System.out.println("Our stock at the end of the day :");
	        System.out.println();				
	    	Set<String> keys = Stock_.keySet();
	        Iterator<String> itr = keys.iterator();
	        while (itr.hasNext()) {
	        	Stock_.get(itr.next()).printDetails();
	        }
	        System.out.println();				
	        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>> RECIPTS <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
	        System.out.println("List of all the receipts :");
	        System.out.println();				
			Iterator<Receipt> iterator = Receipts_.iterator();
			while (iterator.hasNext()) {
				iterator.next().printDetails();
			}
	        System.out.println("we have " + Receipts_.size() + " recipts in total");
	       
	    }
	    
	   }
