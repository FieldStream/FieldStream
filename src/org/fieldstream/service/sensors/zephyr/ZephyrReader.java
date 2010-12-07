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

// @author Patrick Blitz
// @author Somnath Mitra

package org.fieldstream.service.sensors.zephyr;

import java.util.concurrent.BlockingQueue;

import org.fieldstream.Constants;
import org.fieldstream.service.sensors.mote.tinyos.RawPacket;


import android.util.Log;

public class ZephyrReader extends Thread{

	private final BlockingQueue<Byte> queue;
	public Byte END_BYTE;

	private RawPacket rp = null;

	//Count of local data bytes
	private int localByteCount = 0;
	private int DLC = 0;
	private boolean synced = false;
	//private boolean escaped = false;

	private byte STX_BYTE = 0x02;
	private byte ETX_BYTE = 0x03;
	private byte ECG_BYTE = 0x22;
	private byte RSP_BYTE = 0x21;
	private byte TMP_BYTE = 0x42;
	private byte ACL_BYTE = 0x25;

	private int ECG_DLC = 88;
	private int RSP_DLC = 32;
	private int TMP_DLC = 02;
	private int ACL_DLC = 84;

	//private enum msgType {ECG, RSP, TMP, ACL, INVALID};
	public final static int ECGmsgType = Constants.SENSOR_ZEPHYR_ECG;
	public final static int RSPmsgType = Constants.SENSOR_ZEPHYR_RSP;
	public final static int TMPmsgType = Constants.SENSOR_ZEPHYR_TMP;
	public final static int ACLmsgType = Constants.SENSOR_ZEPHYR_ACL;
	public final static int  INVALIDmsgType= -1;

	private int pktType;

	private int [] ECGData;
	private int [] RSPData;
	private int [] TMPData;
	private int [] ACLData;

	//private byte ESCAPE_BYTE = 0x7D;

	private volatile boolean keepAlive;

	private static ZephyrReader INSTANCE;

	private String TAG;

	ZephyrReader(BlockingQueue<Byte> q){
		queue=q;
		rp = new RawPacket();
		keepAlive = true;

	}

	public static ZephyrReader getInstance(BlockingQueue<Byte> q)
	{
		if(INSTANCE == null)
		{
			INSTANCE = new ZephyrReader(q);  

		}
		return INSTANCE;
	}


	public synchronized void kill()
	{
		keepAlive = false;  	  
		interrupt();
	}


	public synchronized void run() 
	{
		TAG = "ZephyrReaderRun";
		Log.d(TAG, "started");
		try 
		{
			while(keepAlive) 
			{ 
				consume(); 

			}
		} 
		catch (Exception e) 
		{}
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
		}
		catch(Exception e)
		{
			e.printStackTrace();
		} 
		return (b.byteValue());
	}

	public void processByte(byte b)
	{		
		if(!synced)
		{
			if(b != STX_BYTE)
			{
				return;
			}
			localByteCount = 0;
			synced = true;

		}
		// synced
		else if (b == STX_BYTE )
		{
			// create a new raw packet which contains raw data
			rp = null;
			rp = new RawPacket();

			//Read the MsgID byte
			b = readOneByte();
			processMsgID(b);

			//Read the DLC byte
			b = readOneByte();
			processDLC(b);


			localByteCount = 0;
			return;
		}		
		else if(localByteCount<DLC)
		{
			rp.setNextByte(b);
			placeData(b, localByteCount++);

		}
		else{
			//Ignore CRC and read the next ACK/NAK byte
			b=readOneByte();			
		}
	}


	private void processDLC(byte b) {
		if(pktType==ECGmsgType){
			ECGData = new int[ECG_DLC];
			DLC=ECG_DLC;
		}
		else if(pktType==RSPmsgType){
			RSPData = new int[RSP_DLC];
			DLC=RSP_DLC;
		}
		else if(pktType==TMPmsgType){
			TMPData = new int[TMP_DLC];
			DLC=TMP_DLC;
		}
		else if(pktType==ACLmsgType){
			ACLData = new int[ACL_DLC];
			DLC=ACL_DLC;
		}
		else if(pktType==INVALIDmsgType){
			DLC=0;
		}
	}

	private void processMsgID(byte b) {
		if(b==ECG_BYTE)
			pktType=ECGmsgType;
		else if(b==RSP_BYTE)
			pktType=RSPmsgType;
		else if(b==TMP_BYTE)
			pktType=TMPmsgType;
		else if(b==ACL_BYTE)
			pktType=ACLmsgType;
		else
			pktType=INVALIDmsgType;

	}

	private void placeData(byte b, int count) {
		
		ZephyrManager.getINSTANCE().newSensorData(pktType, (int)b);
	}

	void consume() 
	{ 
		byte data = readOneByte();
		processByte(data);
		return;
	}



}
