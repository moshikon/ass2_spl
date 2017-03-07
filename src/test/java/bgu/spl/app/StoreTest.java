package bgu.spl.app;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import bgu.spl.app.BuyResult;
import bgu.spl.app.Receipt;
import bgu.spl.app.ShoeStorageInfo;
import bgu.spl.app.Store;

public class StoreTest {
	
	Store store1=Store.getInstance();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ShoeStorageInfo[] stock = {
				new ShoeStorageInfo ("adidas", 7, 0),
				new ShoeStorageInfo("nike", 9, 1),
				new ShoeStorageInfo("puma", 5, 0)
				};
		Store.getInstance().load(stock);
	}
	
	@Before
	public void setUp() throws Exception {	
		System.out.print("Testing : ");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetInstance() {
		System.out.println("Test get instance ");
		Store store2 = Store.getInstance();
		assertEquals(store1, store2);
	}

	@Test
	public void testLoad() {
		System.out.println("Test load ");
		ShoeStorageInfo[] stock2 = {
				new ShoeStorageInfo ("blundstone", 3, 2),
				new ShoeStorageInfo("palladium", 4, 0),
				};
		Store.getInstance().load(stock2);
		assertEquals(BuyResult.DISCOUNTED_PRICE, Store.getInstance().take("blundstone", true));
		assertEquals(BuyResult.DISCOUNTED_PRICE, Store.getInstance().take("blundstone", false));
		assertEquals(BuyResult.REGULAR_PRICE, Store.getInstance().take("blundstone", false));
		assertEquals(BuyResult.REGULAR_PRICE, Store.getInstance().take("palladium", false));
		assertEquals(BuyResult.NOT_ON_DISCOUNT, Store.getInstance().take("palladium", true));
	}

	@Test
	public void testTake() {
		System.out.println("Test take ");
		assertEquals(BuyResult.REGULAR_PRICE, Store.getInstance().take("adidas", false));
		assertEquals(BuyResult.REGULAR_PRICE, Store.getInstance().take("nike", false));
		assertEquals(BuyResult.NOT_ON_DISCOUNT, Store.getInstance().take("nike", true));
		assertEquals(BuyResult.NOT_IN_STOCK, Store.getInstance().take("all-star", false));
	}

	@Test
	public void testAdd() {
		System.out.println("Test add ");
		Store.getInstance().add("puma", 3);
		for (int i=0; i<7 ; i++){
			assertEquals(BuyResult.REGULAR_PRICE, Store.getInstance().take("puma", false));
		}
		assertEquals(BuyResult.NOT_ON_DISCOUNT, Store.getInstance().take("puma", true));
		assertEquals(BuyResult.REGULAR_PRICE, Store.getInstance().take("puma", false));
	}

	@Test
	public void testAddDiscount() {
		System.out.println("Test add discount ");
		Store.getInstance().addDiscount("nike", 2);
		assertEquals(BuyResult.DISCOUNTED_PRICE, Store.getInstance().take("nike", true));
		assertEquals(BuyResult.DISCOUNTED_PRICE, Store.getInstance().take("nike", false));
		assertEquals(BuyResult.DISCOUNTED_PRICE, Store.getInstance().take("nike", false));
		assertEquals(BuyResult.REGULAR_PRICE, Store.getInstance().take("nike", false));
	}
//
	@Test
	public void testFile() {
		System.out.println("Test file ");
		Receipt rec = new Receipt("seller1", "Or" ,"adidas", false, 10, 11, 1); 
		Store.getInstance().file(rec);
		Store.getInstance().print();
	}
}
