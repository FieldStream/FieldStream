package org.fieldstream.service.sensors.mote;

import java.util.ArrayList;

import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensors.mote.tinyos.TOSOscopeIntPacket;

public class MoteDeviceManager {
	
	private static MoteDeviceManager INSTANCE;
	
	public ArrayList<MoteReceiverInterface> motePacketSubscribers;
	
	public MoteDeviceManager() {
		motePacketSubscribers = new ArrayList<MoteReceiverInterface>();
	}
	
	public static MoteDeviceManager getInstance()
	{
		if(INSTANCE == null)
		{
			INSTANCE = new MoteDeviceManager();
			String TAG = "MoteDeviceManager.getInstance";
			
			if (Log.DEBUG) Log.d(TAG, "created New");
		}
		return INSTANCE;
	}
	
	public void subsribe(MoteReceiverInterface subscriber)
	{
		Log.d("MoteDeviceManager.subsribe", subscriber.toString());
		motePacketSubscribers.add(subscriber);
	}
	
	public void unsubscribe(MoteReceiverInterface subscriber)
	{
		motePacketSubscribers.remove(subscriber);
	}
	
	/*
	 * This distributes packets to motes
	 */
	public void onReceive(TOSOscopeIntPacket toip)
	{
		Log.d("MoteDeviceManager", "onReceive() called");
		for( MoteReceiverInterface item: motePacketSubscribers )
		{
			Log.d("MoteDeviceManager", "sending to receiver");
			item.onReceiveMotePacket(toip);
		}
		return;
	}
	
	public void sendNullPacketRequest(int moteType, int SensorID, int numberOfPackets)
	{
		for ( MoteReceiverInterface item: motePacketSubscribers)
		{
			item.onNullPacketRequest(moteType, SensorID, numberOfPackets);
		}
		
	}
	
	


}
