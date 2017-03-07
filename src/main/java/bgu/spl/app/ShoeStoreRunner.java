package bgu.spl.app;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ShoeStoreRunner {
	/**
	 *  TheShoeStoreRunner add the initial storage to the store and create and start the micro-services. 
	 *  When the current tick number is larger than the duration given to the TimeService in the input file all the micro-services should gracefully terminate themselves
	 */
	private final static Logger LOGGER = Logger.getLogger(ShoeStoreRunner.class.getName()); 
	static Thread TIMER;
	private static CountDownLatch CountDownLatch=null;
	private static CountDownLatch stopCountDownLatch=null;
	
	@SuppressWarnings({ "unchecked" })
	public static void main(String[] args) {
		System.out.println("WELCOME TO OUR STORE!");
        String line = args[0];
        System.setProperty("java.util.logging.SimpleFormatter.format", 
                "%5$s%6$s%n");
		
		LOGGER.setLevel(Level.ALL); 
        LOGGER.info(">>>>>>>>>>>>>> SHOE STORE STARTING <<<<<<<<<<<<<<<<<<<");
 
        int serviceCounter=0;
    	LinkedList<Thread> servicesList = new LinkedList<Thread>();
       
        JSONParser parser = new JSONParser();
		//counting services
		try {
            Object obj = parser.parse(new FileReader(line+".json"));
            JSONObject jsonObject = (JSONObject) obj;
            JSONObject services = (JSONObject) jsonObject.get("services");
            Iterator<Object> iterator = services.entrySet().iterator();
            
            //scanning the services 
            while (iterator.hasNext()) {
            	String curr = iterator.next().toString();
            	if (curr.contains("manager")){
            		 serviceCounter++;
            	}
            	else if (curr.contains("factories")){
            		for (int i=0; i<Integer.parseInt(curr.substring(10)); i++){
                		serviceCounter++;
            		}
            	}
            	else if (curr.contains("sellers")){
            		for (int i=0; i<Integer.parseInt(curr.substring(8)); i++){
                		serviceCounter++;
            		}
            	}
            	else if (curr.contains("customers")){
           		 JSONArray customers = (JSONArray) services.get("customers");
           		 for (int i = 0 ; i<customers.size() ; i++){
             		serviceCounter++;
           		 }
            }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		stopCountDownLatch= new CountDownLatch(serviceCounter);
		CountDownLatch= new CountDownLatch(serviceCounter);
        try {
            Object obj = parser.parse(new FileReader(line+".json"));
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray companyList = (JSONArray) jsonObject.get("initialStorage");       
            ShoeStorageInfo [] shoes = new ShoeStorageInfo[companyList.size()];                    
            
        	//getting shoes and loading them to the store
            for (int i = 0 ; i<companyList.size() ; i++ ){
            	  String curr = companyList.get(i).toString();
                  int start_amount = curr.indexOf("amount")+8;
                  int finish_amount = curr.indexOf("shoeType")-2;
                  String amount = curr.substring(start_amount, finish_amount);
                  int start_shoeType = curr.indexOf("shoeType")+11;
                  int finish_shoeType = curr.indexOf("}")-1;
                  String type = curr.substring(start_shoeType, finish_shoeType);
                  shoes[i]= new ShoeStorageInfo(type, Integer.parseInt(amount), 0);
            }
            Store.getInstance().load(shoes);
            
            JSONObject services = (JSONObject) jsonObject.get("services");
        	List<DiscountSchedule> DiscountSchedule = new ArrayList <DiscountSchedule>();
            Iterator<Object> iterator = services.entrySet().iterator();
            
            //scanning the services 
            while (iterator.hasNext()) {
            	String curr = iterator.next().toString();
            	if (curr.contains("time")){
            		 int start_speed = curr.indexOf("speed")+7;
            		 int finsih_speed = curr.indexOf("}");
            		 String speed = curr.substring(start_speed, finsih_speed);
            		 int start_duration = curr.indexOf("duration")+10;
            		 int finish_duration = curr.indexOf(",");
            		 String duration = curr.substring(start_duration, finish_duration);
					 int speed1 = Integer.parseInt(speed); //casting string to int
					 int duration1 = Integer.parseInt(duration);
					 TimeService T = new TimeService(speed1,duration1,CountDownLatch,stopCountDownLatch);
                     TIMER = new Thread(T);
            	}
            	else if (curr.contains("manager")){
            		 JSONObject discount = (JSONObject) services.get("manager");
            		 JSONArray discountList = (JSONArray) discount.get("discountSchedule");
            		 for (int i = 0 ; i<discountList.size() ; i++ ){
                    	  String disc = discountList.get(i).toString();
                    	  int start_amount = disc.indexOf("amount")+8;
                          int finish_amount = disc.indexOf("shoeType")-2;
                          String amount = disc.substring(start_amount, finish_amount);
                          int start_shoeType = disc.indexOf("shoeType")+11;
                          int finish_shoeType = disc.indexOf("tick")-3;
                          String type = disc.substring(start_shoeType, finish_shoeType);
                          int start_tick = disc.indexOf("tick")+6;
                          int finish_tick = disc.indexOf("}");
                          String tick = disc.substring(start_tick, finish_tick);
                      	  DiscountSchedule.add(new DiscountSchedule (type, Integer.parseInt(tick), Integer.parseInt(amount)));
            		  }
                      servicesList.add(new Thread(new ManagementService(DiscountSchedule,CountDownLatch,stopCountDownLatch)));
            	}
            	else if (curr.contains("factories")){
            		for (int i=0; i<Integer.parseInt(curr.substring(10)); i++){
                		servicesList.add(new Thread(new ShoeFactoryService("factory" +(i+1),CountDownLatch,stopCountDownLatch)));
            		}
            	}
            	else if (curr.contains("sellers")){
            		for (int i=0; i<Integer.parseInt(curr.substring(8)); i++){
                		servicesList.add(new Thread(new SellingService("seller" +(i+1),CountDownLatch,stopCountDownLatch)));
            		}
            	}
            	else if (curr.contains("customers")){
           		 JSONArray customers = (JSONArray) services.get("customers");
           		 for (int i = 0 ; i<customers.size() ; i++){
                 	List<PurchaseSchedule> PurchaseSchedule = new ArrayList <PurchaseSchedule>();
           			Set<String> MyWishList = new HashSet<String>();
           			String customer = customers.get(i).toString();
          			int start_name = customer.indexOf("name")+7;
                    int finish_name = customer.indexOf("\"}");
                    String name = customer.substring(start_name, finish_name);
                    JSONObject wish_ = (JSONObject) customers.get(i);
                    JSONArray wishList = (JSONArray) wish_.get("wishList");
                    for (int k = 0 ; k<wishList.size() ; k++){
                    	String wish = wishList.get(k).toString();
                    	MyWishList.add(wish);
                    }
                    JSONObject purchase_ = (JSONObject) customers.get(i);
                    JSONArray purchaseSchedule = (JSONArray) purchase_.get("purchaseSchedule");
                    for (int j = 0 ; j<purchaseSchedule.size() ; j++ ){
                    	  String purchase = purchaseSchedule.get(j).toString();
                          int start_shoeType = purchase.indexOf("shoeType")+11;
                          int finish_shoeType = purchase.indexOf(",")-1;
                          String type = purchase.substring(start_shoeType, finish_shoeType);
                          int start_tick = purchase.indexOf("tick")+6;
                          int finish_tick = purchase.indexOf("}");
                          String tick = purchase.substring(start_tick, finish_tick);
                          PurchaseSchedule.add(new PurchaseSchedule(type, Integer.parseInt(tick)));
            		 }
                    servicesList.add(new Thread(new WebsiteClientService(name, PurchaseSchedule, MyWishList,CountDownLatch,stopCountDownLatch)));
           		 }
            }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		//starting the services
		for (int i=0; i<servicesList.size();i++){
        	servicesList.get(i).start();
        } 
        TIMER.start();
	}
}