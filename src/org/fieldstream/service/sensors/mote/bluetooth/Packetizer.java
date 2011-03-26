////Copyright (c) 2010, University of Memphis
////All rights reserved.
////
////Redistribution and use in source and binary forms, with or without modification, are permitted provided 
////that the following conditions are met:
////
////    * Redistributions of source code must retain the above copyright notice, this list of conditions and 
////      the following disclaimer.
////    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
////      and the following disclaimer in the documentation and/or other materials provided with the 
////      distribution.
////    * Neither the name of the University of Memphis nor the names of its contributors may be used to 
////      endorse or promote products derived from this software without specific prior written permission.
////
////THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED 
////WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
////PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR 
////ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
////TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
////HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
////NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
////POSSIBILITY OF SUCH DAMAGE.
////
//
////@author Somnath Mitra
//
//
//package org.fieldstream.service.sensors.mote.bluetooth;
//
//import java.io.FileOutputStream;
//import java.io.PrintStream;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.LinkedBlockingQueue;
//
//import org.fieldstream.Constants;
//import org.fieldstream.service.logger.Log;
//import org.fieldstream.service.sensors.mote.MoteDeviceManager;
//import org.fieldstream.service.sensors.mote.tinyos.RawPacket;
//import org.fieldstream.service.sensors.mote.tinyos.TOSOscopeBytePacket;
//import org.fieldstream.service.sensors.mote.tinyos.TOSOscopeIntPacket;
//
///*
// * A parser to parse incoming bytes from the bluetooth into valid tinyos1.x packets
// * of predefined oscilloscope type tinyos message but with a slightly different payload
// * @author mitra
// */
//public class Packetizer extends Thread {
//
//	private final BlockingQueue<Byte> queue;
//	public Byte END_BYTE;
//
//	private RawPacket rp = null;
//	private TOSOscopeBytePacket tobp = null;
//	private TOSOscopeIntPacket toip = null;
//
//	private int localByteCount = 0;
//	private boolean synced = false;
//	private boolean escaped = false;
//
//	private byte SYNC_BYTE = 0x7E;
//	private byte ESCAPE_BYTE = 0x7D;
//
//	
//	private volatile boolean keepAlive;
//
//	private static Packetizer INSTANCE = null;
//
//	private String TAG;
//
//	private FileOutputStream fosTos;
//	private PrintStream pTos;
//	private String TOS_FILE_NAME = "/sdcard/TOS_PACKETS";
//
//
//	Packetizer() 
//	{ 
//		queue = new LinkedBlockingQueue<Byte>();	 
//		// packetQueue = pq;
//		rp = new RawPacket();
//		tobp = new TOSOscopeBytePacket();
//		// BtConnectionService.onReceive("Reader created");
//		keepAlive = true;
//	}
//
//	public static Packetizer getInstance()
//	{
//		if(INSTANCE == null)
//		{
//			INSTANCE = new Packetizer();  
//			INSTANCE.initTOSFile();
//		}
//		return INSTANCE;
//	}
//
//	public  void kill()
//	{
//		keepAlive = false;
//		closeTOSFile();
//		interrupt();  // necessary?
//		INSTANCE = null;		
//	}
//
//	public synchronized void run() 
//	{
//		TAG = "readerRun";
//		if (Log.DEBUG) Log.d(TAG, "started");
//		try 
//		{
//			while(keepAlive) 
//			{ 
//				consume(); 
//
//			}
//			
//			TAG = "Reader";
//			if (Log.DEBUG) Log.d(TAG,"Reader was killed");
//		} 
//		catch (Exception e) 
//		{
//			TAG = "Reader";
//			if (Log.DEBUG) Log.d(TAG,"Exception on consume() ---- " + e.getStackTrace().toString());		
//		}
//	}
//
//	public String getByteValue(Byte b)
//	{
//		byte value = b.byteValue();
//		String s = (Integer.toHexString(value & 0xff).toUpperCase());
//		return s;
//	}
//
//
//	public byte readOneByte()
//	{
//		Byte b = new Byte((byte) -1);
//		try
//		{		
//			b = queue.take();
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//		} 
//		// BtConnectionService.onReceive(getByteValue(b));
//		return (b.byteValue());
//	}
//
//	public void processByte(byte b)
//	{		 
//		if(!synced)
//		{
//			if(b != SYNC_BYTE)
//			{
//				return;
//			}
//			localByteCount = 0;
//			synced = true;
//
//		}
//		// synced
//		// look for an escape sequence
//		if(escaped)
//		{
//			byte data = (byte) (b^0x20);
//			rp.setNextByte(data);
//			placeData(data, localByteCount++);
//			escaped=false;
//		}
//		else if (b == ESCAPE_BYTE) 
//		{
//			escaped=true;
//		}
//		else if (b == SYNC_BYTE )
//		{
//			b = readOneByte();
//			if (localByteCount == 118) 
//			{
//				makeTOSPacket();
//			}
//			// create a new raw packet
//			rp = null;
//			rp = new RawPacket();
//
//			// create a new tos base byte packet
//			tobp = null;
//			tobp = new TOSOscopeBytePacket();
//
//			localByteCount = 0;
//			return;
//		}
//		else
//		{
//			rp.setNextByte(b);
//			placeData(b, localByteCount++);
//		}	
//
//	}
//
////	public void processByte(byte b)
////	{		 
//////		Log.d("processByte", "b = " + getByteValue(b));
////		
////		if (b==SYNC_BYTE) {		
////			synced = true;
////			escaped = false;
////			
////			Log.d("processByte", "synced");
////			
////			if (localByteCount == 118) {
////				// crc check
//////				byte[] crcBytes = tobp.getCrc();
//////				int crc = combine(crcBytes[0], crcBytes[1]);
//////				if (crc != crcCalc(rp.getRawPacket(), 0, 116)) {
//////					Log.d("processByte", "crc check failed");
//////				}
////				
////				makeTOSPacket();
////				Log.d("processByte", "packet made");
////			}			
////			
////			readOneByte();
////			rp = new RawPacket();
////			tobp = new TOSOscopeBytePacket();
////			localByteCount = 0;			
////		}
////		else if (synced) {
////			if (b == ESCAPE_BYTE) {
////				escaped = true;
////				return;
////			}
////			
////			if (escaped) {
////				b = (byte) (b^0x20);
////				escaped = false;
////			}
////			
////			rp.setNextByte(b);
////			placeData(b, localByteCount++);
////			
////			Log.d("processByte", "bytes in packet: " + localByteCount);
////		}
////	}
//	
//	public static int crcCalcHelper(int crc, int b)
//	{
//		crc = crc ^ (int)b << 8;
//		for (int i = 0; i < 8; i++)
//		{
//			if ((crc & 0x8000) == 0x8000)
//				crc = crc << 1 ^ 0x1021;
//			else
//				crc = crc << 1;
//		}
//		return crc & 0xffff;
//	}
//
//	public static int crcCalc(byte[] packet, int index, int count)
//	{
//		int crc = 0;
//		while (count > 0)
//		{
//			crc = crcCalcHelper(crc, packet[index++]);
//			count--;
//		}
//		return crc;
//	}	
//	
//	
//	private void placeData(byte data, int index) 
//	{
//		int j = index;
//
//		if (j<10)  
//		{ 
//			tobp.setJunkByteAtPosition(j, data);
//			return;
//		}
//
//		if (j<12)  
//		{ 
//			tobp.setMoteIDByteAtPosition((j)%2, data); 
//			return; 
//		}
//		if (j<14)  
//		{ 
//			tobp.setLastSampleByteAtPosition((j)%2,data); 
//			return; 
//		}
//		if (j<16)  
//		{ 
//			tobp.setChanByteAtPosition((j)%2,data); 
//			return; 
//		}
//		if (j<116) 
//		{ 
//			tobp.setDataByteAtPosition(j-16, data); 
//			return; 
//		}
//		if (j>=116) 
//		{ 
//			tobp.setCrcByteAtPosition((j)%2,data); 
//			return; 
//		} 
//	}
//
//	public void makeTOSPacket()
//	{
//		try
//		{
//			// Make a new Stress Oscope Integer Packet
//			toip = new TOSOscopeIntPacket();
//
//			// Temporary variable to hold the upper and lower bytes of all 16 bit values
//			byte lower = -1;
//			byte upper = -1;
//
//			// Set the Mote ID
//			lower = tobp.getMoteIDByteAtPosition(0);
//			upper = tobp.getMoteIDByteAtPosition(1);
//			int moteID = combine(lower , upper);
//			toip.setMoteID(moteID);
//
//			// Set the Last Sample Number 
//			lower = tobp.getLastSampleByteAtPosition(0);
//			upper = tobp.getLastSampleByteAtPosition(1);
//			int lastSample = combine(lower , upper);
//			toip.setLastSample(lastSample);
//
//			// Set the Channel Number
//			lower = tobp.getChanByteAtPosition(0);
//			upper = tobp.getChanByteAtPosition(1);
//			int chan = combine(lower , upper);
//			toip.setChan(chan);
//
//			//Set the data payload 
//			int data[] = new int[TOSOscopeIntPacket.DATA_INT_SIZE];
//			for(int i=0; i < tobp.DATA_SIZE; i++)
//			{
//				lower = tobp.getDataByteAtPosition(2*i);
//				upper = tobp.getDataByteAtPosition(2*i+1);
//				int value = combine(lower, upper);
//				data[i] = value;
//				toip.setNextDataItem(value);
//			}
//
//			//Set the timestamp
//			long timeStamp = getTimeMilliSec();
//			tobp.setReceivedTimeStamp(timeStamp);
//			toip.setReceivedTimeStamp(timeStamp);
//
//			// print to the tos packet file
//			logToTOSFile(toip);
//
//			// This is where the packet gets pushed to the bluetooth state manager
//			TAG = "makeTOSPacket";
//			if (Log.DEBUG) Log.d(TAG,"Channel " + Integer.toString(chan) + " packet made");
//			MoteDeviceManager.getInstance().onReceive(toip);
//
//		}
//		catch(Exception e)
//		{
//
//		}
//	}
//
//	private void initTOSFile()
//	{
//		if (Constants.NETWORK_LOGGING) {
//			try
//			{
//				fosTos = new FileOutputStream(TOS_FILE_NAME + Long.toString(System.currentTimeMillis()));
//				pTos = new PrintStream(fosTos);
//			}
//			catch(Exception e)
//			{
//	
//			}
//		}
//	}
//
//	private void closeTOSFile()
//	{
//		if (Constants.NETWORK_LOGGING) {
//			try
//			{
//				pTos.close();
//				fosTos.close();
//	
//			}
//			catch(Exception e)
//			{
//	
//			}
//		}
//	}
//
//	private void logToTOSFile(TOSOscopeIntPacket toip)
//	{
//		if (Constants.NETWORK_LOGGING) {
//			String s = printTOIP(toip);
//			
//			if(pTos != null)
//				pTos.print(s);
//		}
//	}
//
//	private String printTOIP(TOSOscopeIntPacket toip) 
//	{
//		StringBuffer buffer = new StringBuffer();
//
//		//print the moteid
//		buffer.append(toip.getMoteID()+",");
//
//		//print the last sample number
//		buffer.append(toip.getLastSample()+",");
//
//		//print the chan
//		buffer.append(toip.getChan()+",");
//
//		//print the data
//		int[] data = toip.getData();
//		for(int i=0; i < data.length; i++)
//		{
//			buffer.append(data[i] + ",");
//		}
//
//		//print the time stamp
//		buffer.append(toip.getReceivedTimeStamp()+",");
//		buffer.append("\n");
//		String s = buffer.toString();
//		return s;
//
//	}
//
//
//
//	void consume() 
//	{ 
//		byte data = readOneByte();
//		processByte(data);
//		return;
//	}
//
//	int combine(byte lower, byte upper)
//	{
//		int i = 0;
//		i |= upper & 0xFF;
//		i <<= 8;
//		i |= lower & 0xFF;
//		return i;
//	}
//
//	long getTimeMilliSec() {
//
//		long timeMilliSeconds;
//
//		timeMilliSeconds = java.lang.System.currentTimeMillis();
//
//		return timeMilliSeconds;
//	}
//
//	public void addBytes(byte[] receivedBytes, int numBytes) {
//		for(int i = 0; i < numBytes; i++)
//		{
//
//			Byte b = new Byte(receivedBytes[i]);
//			try {
//				queue.put(b);
//			} catch (InterruptedException e) {
//				
//				e.printStackTrace();
//			}				
//
//		} // end for		   
//	}
//}

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


package org.fieldstream.service.sensors.mote.bluetooth;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.fieldstream.Constants;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensors.mote.MoteDeviceManager;
import org.fieldstream.service.sensors.mote.tinyos.RawPacket;
import org.fieldstream.service.sensors.mote.tinyos.TOSOscopeBytePacket;
import org.fieldstream.service.sensors.mote.tinyos.TOSOscopeIntPacket;

/*
 * A parser to parse incoming bytes from the bluetooth into valid tinyos1.x packets
 * of predefined oscilloscope type tinyos message but with a slightly different payload
 * @author mitra
 */
public class Packetizer extends Thread {

	private final BlockingQueue<Byte> queue;
	public Byte END_BYTE;

	private RawPacket rp = null;
	private TOSOscopeBytePacket tobp = null;
	private TOSOscopeIntPacket toip = null;

	private int localByteCount = 0;
	private boolean synced = false;
	private boolean escaped = false;

	private byte SYNC_BYTE = 0x7E;
	private byte ESCAPE_BYTE = 0x7D;

	
	private volatile boolean keepAlive;

	private static Packetizer INSTANCE = null;

	private String TAG;

	private FileOutputStream fosTos;
	private PrintStream pTos;
	private String TOS_FILE_NAME = "/sdcard/TOS_PACKETS";


	Packetizer() 
	{ 
		queue = new LinkedBlockingQueue<Byte>();	 
		// packetQueue = pq;
		rp = new RawPacket();
		tobp = new TOSOscopeBytePacket();
		// BtConnectionService.onReceive("Reader created");
		keepAlive = true;
	}

	public static Packetizer getInstance()
	{
		if(INSTANCE == null)
		{
			INSTANCE = new Packetizer();  
			INSTANCE.initTOSFile();
		}
		return INSTANCE;
	}

	public  void kill()
	{
		keepAlive = false;
		closeTOSFile();
		interrupt();  // necessary?
		INSTANCE = null;		
	}

	public synchronized void run() 
	{
		TAG = "readerRun";
		if (Log.DEBUG) Log.d(TAG, "started");
		try 
		{
			while(keepAlive) 
			{ 
				consume(); 

			}
			
			TAG = "Reader";
			if (Log.DEBUG) Log.d(TAG,"Reader was killed");
		} 
		catch (Exception e) 
		{
			TAG = "Reader";
			if (Log.DEBUG) Log.d(TAG,"Exception on consume() ---- " + e.getStackTrace().toString());		
		}
	}

	public String getByteValue(Byte b)
	{
		byte value = b.byteValue();
		String s = (Integer.toHexString(value & 0xff).toUpperCase());
		return s;
	}


	public byte readOneByte()
	{
		Byte b = new Byte((byte) -1);
		try
		{		
			b = queue.take();
			if(Log.DEBUG)
			{
				Log.d("Packetizer","Read one Byte");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		} 
		// BtConnectionService.onReceive(getByteValue(b));
		return (b.byteValue());
	}

	public void processByte(byte b)
	{		 
		if(!synced)
		{
			if(b != SYNC_BYTE)
			{
				return;
			}
			localByteCount = 0;
			synced = true;

		}
		// synced
		// look for an escape sequence
		if(escaped)
		{
			byte data = (byte) (b^0x20);
			rp.setNextByte(data);
			placeData(data, localByteCount++);
			escaped=false;
		}
		else if (b == ESCAPE_BYTE) 
		{
			escaped=true;
		}
		else if (b == SYNC_BYTE )
		{
			b = readOneByte();
			// A TOSOscopeBytePacket and a RawPacket appear to be the same thing? -- NPS
			if (localByteCount == TOSOscopeBytePacket.TOTAL_OSCOPE_BYTE_SIZE)
			{
				makeTOSPacket();
			}
			// create a new raw packet
			rp = null;
			rp = new RawPacket();

			// create a new tos base byte packet
			tobp = null;
			tobp = new TOSOscopeBytePacket();

			localByteCount = 0;
			return;
		}
		else
		{
			rp.setNextByte(b);
			placeData(b, localByteCount++);
		}	

	}

//	public void processByte(byte b)
//	{		 
////		Log.d("processByte", "b = " + getByteValue(b));
//		
//		if (b==SYNC_BYTE) {		
//			synced = true;
//			escaped = false;
//			
//			Log.d("processByte", "synced");
//			
//			if (localByteCount == 118) {
//				// crc check
////				byte[] crcBytes = tobp.getCrc();
////				int crc = combine(crcBytes[0], crcBytes[1]);
////				if (crc != crcCalc(rp.getRawPacket(), 0, 116)) {
////					Log.d("processByte", "crc check failed");
////				}
//				
//				makeTOSPacket();
//				Log.d("processByte", "packet made");
//			}			
//			
//			readOneByte();
//			rp = new RawPacket();
//			tobp = new TOSOscopeBytePacket();
//			localByteCount = 0;			
//		}
//		else if (synced) {
//			if (b == ESCAPE_BYTE) {
//				escaped = true;
//				return;
//			}
//			
//			if (escaped) {
//				b = (byte) (b^0x20);
//				escaped = false;
//			}
//			
//			rp.setNextByte(b);
//			placeData(b, localByteCount++);
//			
//			Log.d("processByte", "bytes in packet: " + localByteCount);
//		}
//	}
	
	public static int crcCalcHelper(int crc, int b)
	{
		crc = crc ^ (int)b << 8;
		for (int i = 0; i < 8; i++)
		{
			if ((crc & 0x8000) == 0x8000)
				crc = crc << 1 ^ 0x1021;
			else
				crc = crc << 1;
		}
		return crc & 0xffff;
	}

	public static int crcCalc(byte[] packet, int index, int count)
	{
		int crc = 0;
		while (count > 0)
		{
			crc = crcCalcHelper(crc, packet[index++]);
			count--;
		}
		return crc;
	}	
	
	private void placeData(byte data, int j) 
	{
		
		// Constants below result from structure of oscope packet.
		// See: TOSOscopeBytePacket
		// Going for minimal change atm, could use a once over later. -- NPS
		
		// error, index too large
		if(Log.DEBUG)
		{
			Log.d("Packetizer","placedata with index = "+j);
		}
		if (j>=114) return;
		
		if (j<6) { 
			tobp.setJunkByteAtPosition(j, data);
		} else if (j<8) { 
			tobp.setMoteIDByteAtPosition(j%2, data); 
		} else if (j<10) { 
			tobp.setLastSampleByteAtPosition(j%2,data); 
		} else if (j<12) { 
			tobp.setChanByteAtPosition(j%2,data); 
		} else if (j<112) {
			// 12 --> index where data starts -- NPS
			tobp.setDataByteAtPosition(j-12, data); 
		} else { 
			tobp.setCrcByteAtPosition(j%2,data); 
		}
	}

	public void makeTOSPacket()
	{
		try
		{
			// Make a new Stress Oscope Integer Packet
			toip = new TOSOscopeIntPacket();

			// Temporary variable to hold the upper and lower bytes of all 16 bit values
			byte lower = -1;
			byte upper = -1;

			// Set the Mote ID
			lower = tobp.getMoteIDByteAtPosition(0);
			upper = tobp.getMoteIDByteAtPosition(1);
			int moteID = combine(lower , upper);
			toip.setMoteID(moteID);

			// Set the Last Sample Number 
			lower = tobp.getLastSampleByteAtPosition(0);
			upper = tobp.getLastSampleByteAtPosition(1);
			int lastSample = combine(lower , upper);
			toip.setLastSample(lastSample);

			// Set the Channel Number
			lower = tobp.getChanByteAtPosition(0);
			upper = tobp.getChanByteAtPosition(1);
			int chan = combine(lower , upper);
			toip.setChan(chan);

			//Set the data payload 
			int data[] = new int[toip.DATA_INT_SIZE];
			for(int i=0; i < tobp.DATA_SIZE; i++)
			{
				lower = tobp.getDataByteAtPosition(2*i);
				upper = tobp.getDataByteAtPosition(2*i+1);
				int value = combine(lower, upper);
				data[i] = value;
				toip.setNextDataItem(value);
			}

			//Set the timestamp
			long timeStamp = getTimeMilliSec();
			tobp.setReceivedTimeStamp(timeStamp);
			toip.setReceivedTimeStamp(timeStamp);

			// print to the tos packet file
			logToTOSFile(toip);

			// This is where the packet gets pushed to the bluetooth state manager
			TAG = "makeTOSPacket";
			if (Log.DEBUG) Log.d(TAG,"Channel " + Integer.toString(chan) + " packet made");
			MoteDeviceManager.getInstance().onReceive(toip);

		}
		catch(Exception e)
		{

		}
	}

	private void initTOSFile()
	{
		if (Constants.NETWORK_LOGGING) {
			try
			{
				fosTos = new FileOutputStream(TOS_FILE_NAME + Long.toString(System.currentTimeMillis()));
				pTos = new PrintStream(fosTos);
			}
			catch(Exception e)
			{
	
			}
		}
	}

	private void closeTOSFile()
	{
		if (Constants.NETWORK_LOGGING) {
			try
			{
				pTos.close();
				fosTos.close();
	
			}
			catch(Exception e)
			{
	
			}
		}
	}

	private void logToTOSFile(TOSOscopeIntPacket toip)
	{
		if (Constants.NETWORK_LOGGING) {
			String s = printTOIP(toip);
			
			if(pTos != null)
				pTos.print(s);
		}
	}

	private String printTOIP(TOSOscopeIntPacket toip) 
	{
		StringBuffer buffer = new StringBuffer();

		//print the moteid
		buffer.append(toip.getMoteID()+",");

		//print the last sample number
		buffer.append(toip.getLastSample()+",");

		//print the chan
		buffer.append(toip.getChan()+",");

		//print the data
		int[] data = toip.getData();
		for(int i=0; i < data.length; i++)
		{
			buffer.append(data[i] + ",");
		}

		//print the time stamp
		buffer.append(toip.getReceivedTimeStamp()+",");
		buffer.append("\n");
		String s = buffer.toString();
		return s;

	}



	void consume() 
	{ 
		byte data = readOneByte();
		processByte(data);
		return;
	}

	int combine(byte lower, byte upper)
	{
		int i = 0;
		i |= upper & 0xFF;
		i <<= 8;
		i |= lower & 0xFF;
		return i;
	}

	long getTimeMilliSec() {

		long timeMilliSeconds;

		timeMilliSeconds = java.lang.System.currentTimeMillis();

		return timeMilliSeconds;
	}

	public void addBytes(byte[] receivedBytes, int numBytes) {
		for(int i = 0; i < numBytes; i++)
		{

			Byte b = new Byte(receivedBytes[i]);
			try {
				queue.put(b);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}				

		} // end for		   
	}
	
	public void finalize() {
		if(Log.DEBUG)
		{
			Log.d("Packetizer","Garbage Collected");
		}
	}
}

