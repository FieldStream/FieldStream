//Copyright (c) 2010, University of Memphis
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.UUID;

import org.fieldstream.Constants;
import org.fieldstream.service.InferrenceService;
import org.fieldstream.service.logger.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import backport.android.bluetooth.BluetoothAdapter;
import backport.android.bluetooth.BluetoothDevice;
import backport.android.bluetooth.BluetoothSocket;
/**
 * Abstraction of a Bluetooth Connection.
 * Will connect, send, receive data.
 * @author mitra
 *
 */
public class BluetoothConnection {
	
	private static final String BRIDGE_SERVICE_UUID = "00001101-0000-1000-8000-00805f9b34fb";
	
	public final String deviceAddress;
		
	private ReceiveThread receiveThread = null;
	
	private BluetoothSocket btSocket;
	
	public volatile boolean stop = false;
	
	private static BluetoothConnection INSTANCE = null;
	
	private Reader reader = null;
	
	private String TAG;
	
	/*
	 * Bluetooth Raw Log File variables
	 */
	
	private FileOutputStream fosRaw = null;
	
	private PrintStream pRaw = null;
	
	private final static String RAW_LOG_FILENAME = "BT_RAW_LOG";
	
	private BluetoothConnection(String mDeviceAddress)
	{
		deviceAddress = mDeviceAddress;
		reader = Reader.getInstance();
	}
	
	public static BluetoothConnection getInstance(String mDeviceAddress)
	{
		if(INSTANCE == null)
		{
			INSTANCE = new BluetoothConnection(mDeviceAddress);
		}
		return INSTANCE;
	}
	
	public static BluetoothConnection getInstance()
	{
		return INSTANCE;
	}
		
		
	public void startReceiving()
	{
		try
		{
			stop = false;
			
			//connect to the bluetooth device
			if (connectToBTDevice(deviceAddress)) {
			
				Log.d("startReceived", "connected to " + deviceAddress);
				
				// now start the raw log file				
				initRawLog();
					
				// now start receiving from the bluetooth receiver thread
				receiveThread = new ReceiveThread();
				receiveThread.start();
			}			
		}
		catch(Exception e)
		{
			
		}
		
	}
		
	public void initRawLog()
	{
		if (Constants.NETWORK_LOGGING) {		
			if (fosRaw != null || pRaw != null) {
				closeRawLog();
			}
			
			try
			{
				String rawLogFileName = RAW_LOG_FILENAME + Long.toString(System.currentTimeMillis());
				fosRaw = new FileOutputStream("/sdcard/"+rawLogFileName);
				pRaw = new PrintStream(fosRaw);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void closeRawLog()
	{
		if (Constants.NETWORK_LOGGING) {
			try
			{
				pRaw.close();
				fosRaw.close();
				pRaw = null;
				fosRaw = null;			
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void logToRaw(byte[] buffer, int numBytes)
	{
		if (Constants.NETWORK_LOGGING) {
			if (pRaw == null) {
				initRawLog();
			}
			
			try {
				pRaw.write(buffer, 0, numBytes);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	

	public void stopReceiving()
	{
		try
		{
			if(receiveThread != null)
			{
				
	        	InferrenceService.INSTANCE.getBaseContext().unregisterReceiver(disconnectReceiver); // Don't forget to unregister during onDestroy   
	        	
				receiveThread.kill();
				receiveThread=null;
				
//				Method m = rfcommSocket.getClass().getMethod("destroy", new Class[] {});
//				m.invoke(rfcommSocket, new Object[] {});
//				rfcommSocket = null;
				
				closeRawLog();
				stop = true;
				TAG = "stopReceiving";
				if (Log.DEBUG) Log.d(TAG, "stopped");
			}
			
			INSTANCE = null;
		}
		catch(Exception e)
		{
			
		}
	}

//	private BufferedInputStream socketInputStream = null;
//	private FileInputStream fis = null;
//	Object rfcommSocket = null;
//	FileDescriptor socketFD = null;
//	public boolean connectToBTDevice(String deviceAddress) {
//    	Boolean connected = false;
//    	TAG = "connectToBTDevice";
//		try {
//			Class c = Class.forName("android.bluetooth.RfcommSocket");
//			rfcommSocket = c.newInstance();
//			
//			Method m = rfcommSocket.getClass().getMethod("create", new Class[] {});
//			socketFD = (FileDescriptor) m.invoke(rfcommSocket, new Object[] {});
//        	if (Log.DEBUG) Log.d(TAG, "created a socket"); 
//			
//			m = rfcommSocket.getClass().getMethod("getInputStream", new Class[] {});
//			fis = (FileInputStream) m.invoke(rfcommSocket, new Object[] {});
//			
////			socketInputStream = new BufferedInputStream(is, 1024 * 32);
//        	if (Log.DEBUG) Log.d(TAG, "got the inputstream"); 
//        	
//			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
//			InferrenceService.INSTANCE.getBaseContext().registerReceiver(disconnectReceiver, filter); // Don't forget to unregister during onDestroy
//						
//			m = rfcommSocket.getClass().getMethod("connect", new Class[] {String.class, int.class});
//			connected = (Boolean) m.invoke(rfcommSocket, new Object[] {deviceAddress, 1});
//			
//			if (connected) {
//	        	if (Log.DEBUG) Log.d(TAG, "**connected to the bridge**"); 
//			}
//			else {
//	        	if (Log.DEBUG) Log.d(TAG, "**problem connecting to bridge**"); 
//			}
//	        		
//			
//		} catch (ClassNotFoundException e) {
//        	if (Log.DEBUG) Log.e(TAG, "ClassNotFoundException: " + e.getLocalizedMessage()); 
//		} catch (IllegalAccessException e) {
//        	if (Log.DEBUG) Log.e(TAG, "IllegalAccessException: " + e.getLocalizedMessage()); 
//		} catch (InstantiationException e) {
//        	if (Log.DEBUG) Log.e(TAG, "InstantiationException: " + e.getLocalizedMessage()); 
//		} catch (SecurityException e) {
//        	if (Log.DEBUG) Log.e(TAG, "SecurityException: " + e.getLocalizedMessage()); 
//		} catch (NoSuchMethodException e) {
//        	if (Log.DEBUG) Log.e(TAG, "NoSuchMethodException: " + e.getLocalizedMessage()); 
//		} catch (IllegalArgumentException e) {
//        	if (Log.DEBUG) Log.e(TAG, "IllegalArgumentException: " + e.getLocalizedMessage()); 
//		} catch (InvocationTargetException e) {
//        	if (Log.DEBUG) Log.e(TAG, "InvocationTargetException: " + e.getLocalizedMessage()); 
//		}		
//				
//		return connected;
//	}
	
	
	public boolean connectToBTDevice(String deviceAddress)
	{
		TAG = "connectToBTDevice";
		btSocket = null;
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {        	
            // the UUID of the bridge's service
        	UUID uuid = UUID.fromString(BRIDGE_SERVICE_UUID);
            btSocket = device.createRfcommSocketToServiceRecord(uuid);
        } 
        catch (IOException e) { 
        	if (Log.DEBUG) Log.e(TAG, "IO Exception: " + e.getLocalizedMessage()); 
        }
               
        // just in case, always cancel discovery before trying to connect to a socket.  
        // discovery will slow down or prevent connections from being made
		btAdapter.cancelDiscovery();

        //socketInputStream = new BufferedInputStream(btSocket.getInputStream());	

		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		InferrenceService.INSTANCE.getBaseContext().registerReceiver(disconnectReceiver, filter); // Don't forget to unregister during onDestroy
		
        try {
        	// Connect the device through the socket. This will block
            // until it succeeds or throws an exception
        	TAG = "connectToBTDevice";
        	Log.d(TAG, "trying to connect to the bridge");
        	btSocket.connect();
        	Log.d(TAG, "connected to the bridge");
        		
        } catch (IOException connectException) {
        	if (Log.DEBUG) Log.d(TAG, "Could not connect to " + deviceAddress);
            try {
            	btSocket.close();
            } catch (IOException closeException) { }
            btSocket = null;
        }
        
		return btSocket != null;
	}

	// Create a BroadcastReceiver for finding a bt device
	private final BroadcastReceiver disconnectReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        // When discovery finds a device
	        if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
	        	TAG = "BluetoothConnection";
	        	Log.d(TAG, "*************connection severed***************");

				BluetoothStateManager.getInstance().requestBluetoothState(BluetoothConnectionStates.BT_STATE_BRIDGE_DISCONNECTED, System.currentTimeMillis(), System.currentTimeMillis());
				// BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	        }
	    }
	};

	
	// btRecv message handler
	private Handler receivedMsgHandler = new Handler() 
	{
		@Override
		public void handleMessage(Message msg)
		{
			try
			{
				if(!stop)
				{
					// Receiving Message
					byte[] receivedBytes = (byte[])(msg.obj);
					int numBytes = msg.arg1;
					TAG = "receivedMsgHandler";
					if (Log.DEBUG) Log.d(TAG,Integer.toString(numBytes));
					logToRaw(receivedBytes, numBytes);
					
				
					// we actually have a real packet so put it in the queue

					reader.addBytes(receivedBytes, numBytes);
			  } // end if(!stop)
				
			} // end try
				
			
			catch(Exception e)
			{
				// BtConnectionService.onReceive(e.toString());
			}
			
			return;
		}
	};
		
	public String getByteValue(Byte b)
	{
		byte value = b.byteValue();
		String s = (Integer.toHexString(value & 0xff).toUpperCase());
		return s;
	}
	
	private class ReceiveThread extends Thread
	{
		private volatile boolean keepAlive;
		
		
		ReceiveThread()
		{
			keepAlive = true;
		}
		
		public void kill()
		{
			TAG = "ReceiveThread.kill()";
			Log.d(TAG, "*******killing receive thread");
			
			keepAlive = false;		
			interrupt();

			try {
				Log.d(TAG, "*******closing socket");
				btSocket.close();
				Log.d(TAG, "*******socket closed");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.d(TAG, "*******failed to close the bt socket");			
				Log.d(TAG, "*******"+e.toString());
			}
		}
		
		
		public void run()
    	{
			TAG = "receiveThread";
			InputStream is = null;
			
			try {
				is = btSocket.getInputStream();
			} catch (IOException e1) {
				Log.d(TAG, "couldn't get an input stream from the socket");
				return;
			}
			
    		while (keepAlive)
    		{
	    		byte[] receivedBytes = new byte[256];
	    		int numBytes = 0;
	    		try {
    				if (Log.DEBUG) Log.d(TAG,"waiting for bytes");
	    			numBytes = is.read(receivedBytes, 0, 256);
    				if (Log.DEBUG) Log.d(TAG,"received " + numBytes + " bytes");
	    		}
	    		catch(IOException e) {
	    			Log.d(TAG, e.toString());
	    		}
	    		
    			if (numBytes > 0)
    				receivedMsgHandler.obtainMessage(0, numBytes, -1, receivedBytes).sendToTarget();
	    		
    		} // end while
    		
			Log.d(TAG, "********done with run()");
				
    	} // end run
	} // end Receive Thread
	
	
// 	public native int btConnect(String destAddress, int channel);
//    public native byte[] btRecv(int socket);
//    public native int btSend(String buf, int socket);
//    public native int btDisconnect();
//    static 
//    {
//        System.loadLibrary("btConnection");
//    }

	
}
