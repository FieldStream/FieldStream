package org.fieldstream.service.sensors.api;

import org.fieldstream.Constants;
import org.fieldstream.service.logger.Log;

public abstract class AbstractMote {
	
	private static final String TAG = "AbstractMote";
	
	/*This is the mote type 
	 * AutoSense Versions 1 ECG and so on
	 * mote types come from the constant file
	 */
	public int MoteType;
	
	/*
	 * This is the mote id 
	 * that is reeived from the actual 
	 * physical mote 
	 */
	public int MoteID;
	
	
	
	protected AbstractMote INSTANCE;
	
	/*
	 * Every mote either has a device address
	 * or a bridge address
	 */
	
	public String DeviceAddress;
	
		
	/*
	 * A standard constructor based on the moteype
	 * and the device Address
	 */
	public AbstractMote(int moteType)
	{
		MoteType = moteType;
		INSTANCE = this;
		
		Log.d(TAG + Constants.getMoteDescription(moteType), "Created");
		
	}
	
	/*
	 * This constructor should be used if
	 * device Address
	 */
	
	public AbstractMote(int moteType, String deviceAddress)
	{
		MoteType = moteType;
		DeviceAddress = deviceAddress;
		
		INSTANCE = this;
		
		Log.d("AbstractMote - " + Constants.getMoteDescription(moteType), "Created");
	}
	

	public abstract void initialize();
	
	public abstract void finalize();
	
	public abstract void activate();
	
	public abstract void deactivate();
	
	public abstract void sendCommand(int command);
	
	public abstract void sendCommand(String command);
	
	
	
	
}
