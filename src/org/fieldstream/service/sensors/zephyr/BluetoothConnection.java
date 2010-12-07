//Copyright (c) 2010, University of Memphis, Carnegie Mellon University
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
//    * Neither the names of the University of Memphis and Carnegie Mellon University nor the names of its 
//      contributors may be used to endorse or promote products derived from this software without specific 
//      prior written permission.
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

//@author Patrick Blitz
//@author Somnath Mitra

package org.fieldstream.service.sensors.zephyr;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.concurrent.BlockingQueue;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BluetoothConnection {

	public final String deviceAddress;
	private int socketNumber = -1;
	private final BlockingQueue<Byte> byteQueue;
	private ReceiveThread receiveThread = null;
	public volatile boolean stop = false;
	public static BluetoothConnection INSTANCE;
	private String TAG;

	/*
	 * Bluetooth Raw Log File variables
	 */

	private FileOutputStream fosRaw = null;
	private PrintStream pRaw = null;
	private String rawLogFileName = "BT_RAW_LOG";
	public BluetoothConnection(BlockingQueue<Byte> mByteQueue , String mDeviceAddress)
	{
		byteQueue = mByteQueue;		
		deviceAddress = mDeviceAddress;
	}

	public static BluetoothConnection getInstance(BlockingQueue<Byte> mByteQueue , String mDeviceAddress)
	{
		if(INSTANCE == null)
		{
			INSTANCE = new BluetoothConnection(mByteQueue, mDeviceAddress);
		}
		return INSTANCE;
	}

	public int getSocketNumber() {
		return socketNumber;
	}

	public void setSocketNumber(int socketNumber) {
		this.socketNumber = socketNumber;
	}

	/*MS Debug End*/
	public void startReceiving()
	{
		try
		{
			stop = false;
			//connect to the bluetooth device
			socketNumber = connectToBTDevice(deviceAddress);
			initRawLog();
			receiveThread = new ReceiveThread(socketNumber);
			receiveThread.start();
			new Thread(){
				public void run()
				{

					byte[] sendBytes = {(byte)0x02,/*STX*/
							(byte)0x23,/*Message ID*/
							(byte)0x00,/*DLC*/
							(byte)0x00,/*CRC*/
							(byte)0x03 /*ETX*/
					};

/*					byte[] sendData = {(byte)0x02,STX
							(byte)0x14,Message ID
							(byte)0x01,DLC
							(byte)0x01,Payload
							(byte)0x5e,CRC
							(byte)0x03 ETX
					};*/

					byte[] sendData_ACL = {(byte)0x02,/*STX*/
							(byte)0x1E,/*Message ID*/
							(byte)0x01,/*DLC*/
							(byte)0x01,/*Payload*/
							(byte)0x5e,/*CRC*/
							(byte)0x03 /*ETX*/
					};

					byte[] sendData_ECG = {(byte)0x02,/*STX*/
							(byte)0x16,/*Message ID*/
							(byte)0x01,/*DLC*/
							(byte)0x01,/*Payload*/
							(byte)0x5e,/*CRC*/
							(byte)0x03 /*ETX*/
					};

					byte[] sendData_RSP = {(byte)0x02,/*STX*/
							(byte)0x15,/*Message ID*/
							(byte)0x01,/*DLC*/
							(byte)0x01,/*Payload*/
							(byte)0x5e,/*CRC*/
							(byte)0x03 /*ETX*/
					};

					byte[] sendData_TMP = {(byte)0x02,/*STX*/
							(byte)0x42,/*Message ID*/
							(byte)0x00,/*DLC*/							
							(byte)0x00,/*CRC*/
							(byte)0x03 /*ETX*/
					};




					String sendStr = new String(sendBytes);
					String sendStr_ECG = new String(sendData_ECG);
					String sendStr_TMP = new String(sendData_TMP);
					String sendStr_RSP = new String(sendData_RSP);
					String sendStr_ACL = new String(sendData_ACL);

					btSend(sendStr_ECG, socketNumber);
					btSend(sendStr_RSP, socketNumber);
					btSend(sendStr_ACL, socketNumber);

					while(true)
					{	
						TAG = "Lifesign";
						Log.d(TAG,"-------->>>>>>>>Sending Lifesign and temperature request Packet<<<<<<<<------");
						btSend(sendStr, socketNumber);
						btSend(sendStr_TMP, socketNumber);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

			}.start();
		}
		catch(Exception e)
		{

		}

	}

	public void initRawLog()
	{
		try
		{
			fosRaw = new FileOutputStream("/sdcard/"+rawLogFileName);
			pRaw = new PrintStream(fosRaw);
		}
		catch(Exception e)
		{

		}
		return;
	}

	public void closeRawLog()
	{
		try
		{
			pRaw.close();
			fosRaw.close();
			pRaw = null;
			fosRaw = null;			
		}
		catch(Exception e)
		{

		}
	}

	public void logToRaw(String s)
	{
		pRaw.print(s);
		return;
	}


	public void stopReceiving()
	{
		try
		{
			if(receiveThread != null)
			{
				receiveThread.kill();
				socketNumber = -1;
				stop = true;
				TAG = "stopReceiving";
				Log.d(TAG, "stopped");
			}
		}
		catch(Exception e)
		{

		}
	}


	public int connectToBTDevice(String deviceAddress)
	{
		int mSocketNumber = -1;

		try
		{
			// Start a SPP connnection to the device if possible
			mSocketNumber = btConnect(deviceAddress,1);
		}
		catch(Exception e)
		{

		}

		return mSocketNumber;
	}

	// btRecv message handler
	private Handler receivedMsgHandler = new Handler() 
	{
		@Override
		public void handleMessage(Message msg)
		{
			/*MS debug start*/
			TAG = "packet received";
			/*MS debug end*/
			try
			{
				if(!stop)
				{
					// Receiving Message
					byte[] receivedBytes = (byte[])(msg.obj);
					String s = new String(receivedBytes);
					TAG = "receivedMsgHandler";
					logToRaw(s);
					boolean nullpacket = true;

					for(int i = 0; i < receivedBytes.length; i++)
					{
						if(receivedBytes[i] != -128)
						{
							nullpacket = false;
						}

					} // end for

					if(!nullpacket)
					{
						for(int i = 0; i < receivedBytes.length; i++)
						{
							if(receivedBytes[i] != -128)
							{
								Byte b = new Byte(receivedBytes[i]);
								byteQueue.put(b);
							}

						} // end for
					} // end if null packet
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
		public int tSocket;
		public boolean tKeepAlive;

		ReceiveThread(int mSocket)
		{
			tSocket = mSocket;
			tKeepAlive = true;
		}

		public synchronized void kill()
		{
			tKeepAlive = false;
			btDisconnect();
			interrupt();
		}


		public void run()
		{
			try
			{
				byte[] receivedBytes;

				while (tKeepAlive)
				{
					receivedBytes = btRecv(tSocket);
					if (receivedBytes == null)
					{
						return;
					}
					else
					{
						receivedMsgHandler.sendMessage(Message.obtain(receivedMsgHandler, 1, receivedBytes));
					} // end else

				} // end while
			} // end try
			catch(Exception e)
			{

			}

		} // end run


	} // end Receive Thread

	public native int btConnect(String destAddress, int channel);
	public native byte[] btRecv(int socket);
	public native int btSend(String buf, int socket);
	public native int btDisconnect();
	static 
	{
		System.loadLibrary("btConnection12");
	}
}
