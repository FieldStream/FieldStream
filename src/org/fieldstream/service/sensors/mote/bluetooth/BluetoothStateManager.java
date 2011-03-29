﻿//Copyright (c) 2010, University of Memphis
//All rights reserved.
//
//Redistribution and use in source and binary forms, with or without modification, are permitted provided 
//that the following conditions are met:
//
//    * Redistributions of source code must retain the above copyright notice, this list of conditions and 
//      the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
//      and the following disclaimer in the documentation and/or other materials provided with the 
//      distribution.
//    * Neither the name of the University of Memphis nor the names of its contributors may be used to 
//      endorse or promote products derived from this software without specific prior written permission.
//
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED 
//WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
//PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR 
//ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
//TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
//HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
//NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
//POSSIBILITY OF SUCH DAMAGE.
//

//@author Somnath Mitra
//@author Andrew Raij


package org.fieldstream.service.sensors.mote.bluetooth;

import java.util.ArrayList;
import java.util.Iterator;

import org.fieldstream.service.InferrenceService;
import org.fieldstream.service.logger.Log;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import backport.android.bluetooth.BluetoothAdapter;
import backport.android.bluetooth.BluetoothDevice;



public class BluetoothStateManager extends Thread {
	
	private static BluetoothStateManager INSTANCE = null;
	
	private static String TAG = "BluetoothStateManager";
	
	public ArrayList<BluetoothStateSubscriber> btStateSubscribers;
	
	/* This variable holds an instance of the 
	 * BluetoothConnection. There is ideally only
	 * one Bluetooth Connection that is active.
	 * This should always be assigned to 
	 * BluetoothConnection.getInstance().
	 */
	
	private BluetoothConnection btConnection = null;
	
	/* This btScanAdapater should only be used for scanning purposes */
	private BluetoothAdapter btScanAdapter;
	private ArrayList<CharSequence> deviceArray;
	
	
	
	
	
	/*
	 * A reader variable to do the byte level reading.
	 * It reads from the byte queue to produce tinyos packets.
	 * There should be just one instance hence a static variable.
	 */
	
	private Packetizer reader = null;
	
	/*
	 * The address of the 
	 */
	private String BridgeAddress;
	
	private int requestedBluetoothState;

	private int currentBluetoothMode;
	
	private boolean dutyCycle = false;
	private long timeToNextDutyCycle = -1;
	
	private boolean periodicScanning = false;
	private long timeToNextScan = -1;
	
	
	//private long lastReceivedPacketTime = -1;
	
	private volatile boolean keepAlive = true;
	
	public static BluetoothStateManager getInstance()
	{
		if(INSTANCE == null)
		{
			INSTANCE = new BluetoothStateManager();
			INSTANCE.btStateSubscribers = new ArrayList<BluetoothStateSubscriber>();
			
			
			String msg = "BluetoothStateManager Created";
			if (Log.DEBUG) Log.d(TAG,msg);
		}
		return INSTANCE;
	}
	
	public void registerListener(BluetoothStateSubscriber subscriber)
	{		
		btStateSubscribers.add(subscriber);
	}
	
	public void unregisterListener(BluetoothStateSubscriber subscriber)
	{
		btStateSubscribers.remove(subscriber);
	}
	
	public void updateBluetoothState(int BluetoothState) 
	{
		for(BluetoothStateSubscriber item : btStateSubscribers)
		{
				// int BluetoothState = BluetoothConnectionStates.getCURRENT_BT_STATE();
				String TAG = "updateBluetoothState";
				String sendingTo = "sending To " + item.toString();
				if (Log.DEBUG) Log.d(TAG, sendingTo);
				item.onReceiveBluetoothStateUpdate(BluetoothState);
		}
		
		
		return;
	}
	
	
	public void setBridgeAddress(String mBridgeAddress)
	{
		BridgeAddress = mBridgeAddress;
		
	}
	
	
	public String getBridgeAddress()
	{
		return BridgeAddress;
	}
	

	/*
	 * This method changes states of the Bluetooth Connection.
	 * State changes are not meant to happen that often.
	 * State changes close existing connection, kill local variables
	 * and purge old state variables. Use with caution.
	 * Modes are meant to change pretty often, such as CONNECT to DISCONNECT
	 * during Duty Cycle. [In fact this is the only possible state 
	 * in which the mode can change].
	 * States such as periodic scanning , dead time , duty cycle and no duty cycle 
	 * can be requested.
	 */
	public synchronized void requestBluetoothState(int BluetoothState)
	{
		String TAG = "BluetoothManager";
		String msg = "Requesting bluetooth state = "+Integer.toString(BluetoothState);
		Log.d(TAG , msg);
		// Save the requested State
		requestedBluetoothState = BluetoothState;
				
		// stop the duty cycle if any
		stopDutyCycle();
		// stop the periodic scanning if any
		stopPeriodicScanning();
		
		// try to close the previous connection
		if(btConnection != null)
		{
			stopDown();
		}
		
		// try to close the reader instance
		if(reader != null) {
			reader.kill();
			reader=null;
		}
		
		// all killing and purging done done - change the state
		
		// if the requested state is no duty cycle
		if(requestedBluetoothState == BluetoothConnectionStates.BT_STATE_NO_DUTY_CYCLE)
		{
			// Start it up
			// try to start a new connection and init all queues again
			startUp();
			// Change the state
			BluetoothConnectionStates.setCURRENT_BT_STATE(BluetoothConnectionStates.BT_STATE_NO_DUTY_CYCLE);
			// change the state times
			
			TAG = "BluetoothStateManager";
			msg = "Started in Continuous / No Duty Cycle State";
			
			if(Log.DEBUG)
				Log.d(TAG,msg);
			 
			 disconnected = false;
			
		}
		
		// if the requested state is dead time
		if(requestedBluetoothState == BluetoothConnectionStates.BT_STATE_DEAD_TIME)
		{
			// Already stopped - so nothing to do
			//Change the state
			BluetoothConnectionStates.setCURRENT_BT_STATE(BluetoothConnectionStates.BT_STATE_DEAD_TIME);
			// change the state times			
			
			TAG = "BluetoothStateManager";
			msg = "Changed to Dead Time State";
			 
			if(Log.DEBUG)
				Log.d(TAG,msg);
		}
		
		// if the requested state is connection severed or bridge is disconnected
		if(requestedBluetoothState == BluetoothConnectionStates.BT_STATE_BRIDGE_DISCONNECTED)
		{
			// Already stopped - so nothing to do
			//Change the state
			BluetoothConnectionStates.setCURRENT_BT_STATE(BluetoothConnectionStates.BT_STATE_BRIDGE_DISCONNECTED);
			// change the state times			
			
			TAG = "BluetoothStateManager";
			msg = "Changed to Disconnected state";
			
			if(Log.DEBUG)
				Log.d(TAG,msg);			
		}
		
		// if the requested state is duty cycle
		if(requestedBluetoothState == BluetoothConnectionStates.BT_STATE_DUTY_CYCLE)
		{
			// Start it up
			// try to start a new connection and init all queues again
			startUp();
			// Now start the duty cycle
			startDutyCycle();
			// Change the state
			BluetoothConnectionStates.setCURRENT_BT_STATE(BluetoothConnectionStates.BT_STATE_DUTY_CYCLE);
			// change the state times
			
			TAG = "BluetoothStateManager";
			msg = "Started in Duty Cycle State";
			if(Log.DEBUG)
			 Log.d(TAG,msg);			
		}
		
		// if the requested state is BT STATE PERIODIC SCANNING
		// start a periodic scan of devices and search for our bridge
		if(requestedBluetoothState == BluetoothConnectionStates.BT_STATE_PERIODIC_SCANNING)
		{
			// Now start the periodic scanning
			startPeriodicScanning();
			// Change the state
			 BluetoothConnectionStates.setCURRENT_BT_STATE(BluetoothConnectionStates.BT_STATE_PERIODIC_SCANNING);
			// change the state times
			 timeToNextScan = System.currentTimeMillis() + BluetoothConnectionStates.BT_SCAN_PERIOD;
			
			TAG = "BluetoothStateManager";
			msg = "Bridge Problem State";
			if(Log.DEBUG)
				Log.d(TAG,msg);			
		}
		
		// update the Bluetooth State for all subscribers
		updateBluetoothState(BluetoothConnectionStates.getCURRENT_BT_STATE());
		
	}

	private void startDutyCycle() {
		setDutyCycle(true);
	}
	
	private void stopDutyCycle() {
		setDutyCycle(false);
	}
	
	private void startPeriodicScanning() {
		periodicScanning = true;
	}
	
	private void stopPeriodicScanning() {
		periodicScanning = false;
	}
	

	/*
	 * Starts a bluetooth connection to the specified bridge node 
	 * address and start receiving bytes. Then makes a blocking queue
	 * to buffer bytes into. Starts a reader that reads bytes from the 
	 * common blocking queue byte level buffer and tries to parse 
	 * tinyos oscope packets.
	 */
	public void startUp(){
		try
		{			
			btConnection = null;
			btConnection = BluetoothConnection.getInstance(BridgeAddress);
			btConnection.startReceiving();		
			
			reader = null;
			reader = Packetizer.getInstance();
			
			// if this is a new reader thread increase its priority so that it doesn't get killed by Android		
			if(reader.getState() == Thread.State.NEW)
			{
				reader.setPriority(MAX_PRIORITY);
				reader.start();
				if (Log.DEBUG) Log.d("BluetoothStateManager","Starting a new Reader");
			}
			
												
			// start the instance
			INSTANCE.start();		
			
			String TAG = "BluetoothStateManager.startUp()";
			String msg = "Started";
			if (Log.DEBUG) Log.d(TAG,msg);
		}
		catch(Exception e)
		{
			String TAG = "BluetoothStateManager.startUp()";
			if (Log.DEBUG) Log.d(TAG, "Exception: " + e.getMessage());
		}
	}
	
	public void restart()
	{
		btConnection.startReceiving();
	}
	
	public void pause()
	{
		btConnection.stopReceiving();
	}
	
	/*
	 * This function is to stop receiving data from the bluetooth bridge.
	 * But this function is problematic. This should not be a problem for
	 * now, as stopping is not used that often. 
	 */
	public void stopDown(){
		try
		{
			btConnection.stopReceiving();
						
			btConnection = null;
			reader.kill();
			reader = null;
			
			String TAG = "BluetoothStateManager";
			String msg = "Stopped";
			if (Log.DEBUG) Log.d(TAG,msg);
		
		}
		catch(Exception e)
		{
			if (Log.DEBUG) Log.d("BluetoothStateManager","exception in stopDown "+e.getMessage());
		}
	}
	
	

	
	boolean disconnected = false;
	//int problem = 0;
	public void run() 
	{
		while(keepAlive)
		{	
		
//			if(Log.DEBUG)
//				Log.d(TAG, "running");
		
			
		if (!disconnected) 
		{
			disconnected = detectBridgeDisconnection();
	
			// this is where periodic scanning kicks in
			if (disconnected) {
				if(Log.DEBUG)
					Log.d(TAG, "disconnected - going for periodic duty cycle");
				
				requestBluetoothState(BluetoothConnectionStates.BT_STATE_PERIODIC_SCANNING);
			}
	
			// if the reader dies (which sometimes does), then restart it
			if (reader == null)
			{
				if(Log.DEBUG)
				{
					Log.d("BluetoothStateManager","Reader was killed - restarting");
				}
				reader = Packetizer.getInstance();
			}
			
			// if this is a new reader thread increase its priority so that it doesn't get killed by Android		
			if(reader.getState() == Thread.State.NEW)
			{
				reader.setPriority(MAX_PRIORITY);
				reader.start();
				if (Log.DEBUG) Log.d("BluetoothStateManager","Starting and prioritizing Reader");
			}
			else if (reader.getState() == Thread.State.TERMINATED) {
				if (Log.DEBUG) Log.e("BluetoothStateManager","Reader Terminated...what happened?");
				reader.setPriority(MAX_PRIORITY);
				reader.start();
			}

		// if periodic Scanning is true
		if(periodicScanning)
		{
			String TAG = "BluetoothStateManager.run()";
			Log.d(TAG, "inside periodic scanning");
			long now = System.currentTimeMillis();
			if( now >= timeToNextScan)
			{
				// Do the periodic scanning part here				
				scan();
				
				timeToNextScan += 60 * 1000;
			}	// end if( now >= timeToNextScan)
		} // end if(periodicScanning)
		
			try
			{
				// Dont want to run these tests all the time
				// Have to find the right times to do all the checking
				sleep(BluetoothConnectionStates.getBT_CHECK_TIME());
			} // end try
			catch(Exception e)
			{
				String TAG = "BluetoothStateManager";
				String msg = "Exception while sleeping";
				if (Log.DEBUG) Log.d(TAG,msg);
			} // end catch
			} // end if not disconnected
		} // end while keepalive
	}

	
	//Make this detector as a part of separate class with static methods later 
	// This detector will do a lot of stuff such as checking for timeout
	// of packet reception from each of the motes , check for data loss (by looking at packet last sample 
	// numbers)
//	public int detectBridgeProblem() {
//		int problem = -1;
//		long now = System.currentTimeMillis();
//		if(lastReceivedPacketTime == -1)
//			return problem;
//	    if(now - lastReceivedPacketTime > BluetoothConnectionStates.BT_TIMEOUT);
//			problem = 1;
//		return problem;
//	}
	
	public boolean detectBridgeDisconnection() {
		boolean mdisconnected = false;
		if(BluetoothConnectionStates.getCURRENT_BT_STATE() == BluetoothConnectionStates.BT_STATE_BRIDGE_DISCONNECTED)
		{
			mdisconnected = true;
		}
		return mdisconnected;
	}
	
	public void kill()
	{
		keepAlive  = false;
		INSTANCE = null;
		interrupt();
	}
	
	public synchronized void scan()
	{
		deviceArray = null;
		deviceArray = new ArrayList<CharSequence>(); 
		
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		InferrenceService.INSTANCE.registerReceiver(deviceFoundReceiver, filter); // Don't forget to unregister during onDestroy

		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		InferrenceService.INSTANCE.registerReceiver(scanEndedReceiver, filter); // Don't forget to unregister during onDestroy

		deviceArray.clear();
		btScanAdapter = BluetoothAdapter.getDefaultAdapter();
		btScanAdapter.startDiscovery();
		
	}
	
	

	public void setCurrentBluetoothMode(int currentBluetoothMode) {
		this.currentBluetoothMode = currentBluetoothMode;
	}

	public int getCurrentBluetoothMode() {
		return currentBluetoothMode;
	}



	public void setDutyCycle(boolean dutyCycle) {
		this.dutyCycle = dutyCycle;
	}

	public boolean isDutyCycle() {
		return dutyCycle;
	}



	public void setTimeToNextDutyCycle(long timeToNextDutyCycle) {
		this.timeToNextDutyCycle = timeToNextDutyCycle;
	}

	public long getTimeToNextDutyCycle() {
		return timeToNextDutyCycle;
	}



	// Create a BroadcastReceiver for finding a bt device
	private final BroadcastReceiver deviceFoundReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        // When discovery finds a device
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	            // Get the BluetoothDevice object from the Intent
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            // Add the name and address to an array adapter for use by whoever wants to use
	            // Log this information 
	            deviceArray.add(device.getAddress()); 
	            String TAG = "BluetoothStateManager.deviceFoundReceiver.onReceive()";
	            String msg =  "new device found " + device.getAddress();
	            Log.d(TAG, msg);
	        }
	    }
	};
	
	
	// Create a BroadcastReceiver for catching when scanning ends
	private final BroadcastReceiver scanEndedReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        // When discovery finds a device
	        Log.d("BT STATE MANAGER", "scan ended");
	        if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
		        Log.d("BT STATE MANAGER", "action is what it should be");
	        	
	        	
	        	InferrenceService.INSTANCE.unregisterReceiver(deviceFoundReceiver);
	        	InferrenceService.INSTANCE.unregisterReceiver(scanEndedReceiver);

	        	if (deviceArray.size() == 0) {
			        Log.d("BT STATE MANAGER", "no bt devices found");
	        		// no bluetooth devices around;
	        	}	        	
	        	else {
			        Log.d("BT STATE MANAGER", "found bt devices ");
	        		 for (Iterator<CharSequence> i = deviceArray.iterator(); i.hasNext(); )
	        		 {
	        			 String nextDevice = (String)i.next();
	 			        Log.d("BT STATE MANAGER", "found " + nextDevice);
 	 			        Log.d("BT STATE MANAGER", "bridgeaddress == " + BridgeAddress);
	        			 if(nextDevice.equals(BridgeAddress))
	        			 {
	 	 			        Log.d("BT STATE MANAGER", nextDevice + " == " + BridgeAddress);
	        				 // We have found our bridge so we will request no duty cycle state for now
	        				 // and try to get lots of data out of bridge.
	        				 requestBluetoothState(BluetoothConnectionStates.BT_STATE_NO_DUTY_CYCLE);
	        				 deviceArray.clear();
	        				 break;
	        			 }
	        		 }
	        	}   		
	        }
	    }
	};
	

}


