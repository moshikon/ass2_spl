package bgu.spl.mics.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import bgu.spl.app.NewDiscountBroadcast;
import bgu.spl.app.PurchaseOrderRequest;
import bgu.spl.app.PurchaseSchedule;
import bgu.spl.app.RestockRequest;
import bgu.spl.app.SellingService;
import bgu.spl.app.WebsiteClientService;
import bgu.spl.mics.impl.MessageBusImpl;

public class MessageBusImplTest {

	MessageBusImpl MessageBus1 = MessageBusImpl.getInstance(); 
	PurchaseOrderRequest purchase = new  PurchaseOrderRequest("gal","adidas",true,5);
	RestockRequest restock = new RestockRequest("gil", "palladium", 1); 
	SellingService seller1 = new SellingService("seller1", null,  null);
	List<PurchaseSchedule> PurchaseSchedule = new ArrayList <PurchaseSchedule>();
	WebsiteClientService guy = new WebsiteClientService("guy",PurchaseSchedule,null,null,null);
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
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
		MessageBusImpl MessageBus2 = MessageBusImpl.getInstance(); 
		assertEquals(MessageBus1, MessageBus2);	
		}

	@Test
	public void testSubscribeRequest() {
		System.out.println("Test Subscribe + send Request + await Message");
		MessageBus1.subscribeRequest(purchase.getClass(), seller1);
		assertTrue(MessageBus1.sendRequest(purchase, guy));
		assertFalse(MessageBus1.sendRequest(restock, guy));
	}

	@Test
	public void testSubscribeBroadcast() throws InterruptedException {
		System.out.println("Test Subscribe + send Broadcast + await Message ");

		NewDiscountBroadcast disc = new NewDiscountBroadcast("n-balance", 2);
		MessageBus1.subscribeBroadcast(disc.getClass(), seller1);
		MessageBus1.sendBroadcast(disc);
		assertEquals(disc, MessageBus1.awaitMessage(seller1));
	}


	@Test
	public void testRegister() {
		System.out.println("Test Register + unregister ");
		SellingService seller2 = new SellingService("seller2", null,  null);
		MessageBus1.register(seller2);
		MessageBus1.unregister(seller2);


	}

}
