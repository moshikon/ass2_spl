package bgu.spl.mics.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.RequestCompleted;

public class MessageBusImpl implements MessageBus {
	
	private List<MicroService> RobinHood_ = (List<MicroService>) Collections.synchronizedList(new ArrayList<MicroService>());
	private ConcurrentHashMap<MicroService ,BlockingQueue<Message>> MapOfMicroServices_ = new ConcurrentHashMap<MicroService ,BlockingQueue<Message>>();	
	@SuppressWarnings("rawtypes")
	private ConcurrentHashMap<Class<? extends Request> ,List<MicroService>> MapOfRequest_ = new ConcurrentHashMap<Class<? extends Request> ,List<MicroService>>();	
	private ConcurrentHashMap<Class<? extends Broadcast> ,List<MicroService>> MapOfBroadcast_ = new ConcurrentHashMap<Class<? extends Broadcast> ,List<MicroService>>();	
	private ConcurrentHashMap<Request<?> ,MicroService> Sendto_ = new ConcurrentHashMap<Request<?> ,MicroService>();		

	
    private static class MessageBusHolder {
        private static MessageBusImpl instance = new MessageBusImpl();
    }
    private MessageBusImpl() {
        // initialization code..
    }
    public static MessageBusImpl getInstance() {
        return MessageBusHolder.instance;
    }
    
	@SuppressWarnings("rawtypes")
	public synchronized void  subscribeRequest(Class<? extends Request> type, MicroService m) {
		if (!MapOfRequest_.containsKey(type)){
			List<MicroService> List_ = (List<MicroService>) Collections.synchronizedList(new ArrayList<MicroService>());
			MapOfRequest_.put(type, List_);
			MapOfRequest_.get(type).add(m);
		}else{
			(MapOfRequest_.get(type)).add(m);
		}		
		if (!MapOfMicroServices_.containsKey(m)){
			register(m);
		}
	}

	public synchronized void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		if (!MapOfBroadcast_.containsKey(type)){
			List<MicroService> List_ = (List<MicroService>) Collections.synchronizedList(new ArrayList<MicroService>());
			MapOfBroadcast_.put(type, List_);
			MapOfBroadcast_.get(type).add(m);
		}else{
			(MapOfBroadcast_.get(type)).add(m);
		}
		if (!MapOfMicroServices_.containsKey(m)){
			register(m);
		}
	}

	public <T> void complete(Request<T> r, T result) {
		RequestCompleted<T> comp = new RequestCompleted<T> (r, result);
		if (MapOfMicroServices_.containsKey(Sendto_.get(r))){
			MapOfMicroServices_.get(Sendto_.get(r)).add(comp);
		}
	}

	public synchronized void sendBroadcast(Broadcast b) {
		List<MicroService> ListReq_ = MapOfBroadcast_.get(b.getClass());
		if (ListReq_ !=null){
			for(int i=0; i<ListReq_.size(); i++){
				MicroService m = ListReq_.get(i);
				MapOfMicroServices_.get(m).add(b);
			}
		}
	}

	public synchronized boolean sendRequest(Request<?> r, MicroService requester) {
		if (!MapOfMicroServices_.containsKey(requester)){
			register(requester);
		}
		Sendto_.put(r, requester);
		
		List<MicroService> ListReq_ = MapOfRequest_.get(r.getClass());
		if (ListReq_ != null){
			for(int i=0; i<ListReq_.size(); i++){
				MicroService m = ListReq_.get(i);
				if (RobinHood_.contains(m)){
					
					if (i<ListReq_.size()-1){
						RobinHood_.remove(m);
						RobinHood_.add(ListReq_.get(i+1));
					}else{
						RobinHood_.add(ListReq_.get(0));
						RobinHood_.remove(m);
					}
					if (MapOfMicroServices_.containsKey(m)){
						MapOfMicroServices_.get(m).add(r);
						return true;
					}
				}
			}
			for(int i=0; i<ListReq_.size(); i++){
				MicroService m = ListReq_.get(i);	
				if (i<ListReq_.size()-1){
					RobinHood_.remove(m);
					RobinHood_.add(ListReq_.get(i+1));
				}else{
					RobinHood_.add(ListReq_.get(0));
					RobinHood_.remove(m);
				}
				if (MapOfMicroServices_.containsKey(m)){
					MapOfMicroServices_.get(m).add(r);
					return true;
				}
			}
		}
		return false;
	}


	@Override
	public void register(MicroService m) {
		BlockingQueue<Message> q = new LinkedBlockingDeque<Message>();// create queue and arrylist and insert to hash map
		MapOfMicroServices_.put(m, q);
	}

	public synchronized void unregister(MicroService m) {
		if (!MapOfMicroServices_.containsKey(m)){
			return;
		}else{
			MapOfMicroServices_.remove(m);
		}
	}

	public Message awaitMessage(MicroService m) throws InterruptedException {
		if (!MapOfMicroServices_.containsKey(m)){
			register (m);
		}		
		return MapOfMicroServices_.get(m).take();
	}

}
