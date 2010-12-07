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
package org.fieldstream.service.sensors.mote.tinyos;
/*
 * A tinyos oscilloscope integer packet implementation for the Stress Project Tinyos Packet type
 * @author Somnath Mitra
 */
public class TOSOscopeIntPacket {
	
	int moteID;
	int lastSample;
	int chan;
	int[] data;
	long receivedTimeStamp; 
	
	public static final int DATA_INT_SIZE = 50;
	
	private int dataPosition = -1;
	
	public TOSOscopeIntPacket()
	{
		moteID = -1;
		lastSample = -1;
		chan = -1;
		data = new int[DATA_INT_SIZE];
		receivedTimeStamp = -1;
		dataPosition = -1;
		for(int i=0; i < DATA_INT_SIZE; i ++)
			data[i] = -1;
	}
	
	public int getMoteID() {
		return moteID;
	}

	public void setMoteID(int moteID) {
		this.moteID = moteID;
	}

	public int getLastSample() {
		return lastSample;
	}

	public void setLastSample(int lastSample) {
		this.lastSample = lastSample;
	}

	public int getChan() {
		return chan;
	}

	public void setChan(int chan) {
		this.chan = chan;
	}

	public int[] getData() {
		return data;
	}

	public void setData(int[] data) {
		this.data = data;
	}
	
	public void setNextDataItem(int data)
	{
		if(dataPosition < this.DATA_INT_SIZE - 1)
			this.data[++dataPosition] = data;
	}
	

	public long getReceivedTimeStamp() {
		return receivedTimeStamp;
	}

	public void setReceivedTimeStamp(long receivedTimeStamp) {
		this.receivedTimeStamp = receivedTimeStamp;
	}

	public int getDATA_INT_SIZE() {
		return DATA_INT_SIZE;
	}
	

}
