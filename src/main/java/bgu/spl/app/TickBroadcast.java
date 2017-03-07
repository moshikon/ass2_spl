package bgu.spl.app;
import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast{
    /**
     * a broadcast messages that is sent at every passed clock tick. 
     * This message must contain the current tick
     */
	private int currentTick;
      
    public TickBroadcast(int currentTick) {
        this.currentTick=currentTick;
    }
    
    public int getCurrentTick(){
    	return currentTick;
    }
}
