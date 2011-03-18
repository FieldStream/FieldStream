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
//package org.fieldstream.service.sensors.mote.tinyos;
//
///*
// * A tinyos oscilloscope byte packet implementation for the Stress Project Tinyos Packet type
// * @author Somnath Mitra
// */
//public class TOSOscopeBytePacket 
//{
//	
//	byte junk[]; // 1-10
//	byte moteID[];	// 11-12
//	byte lastSample[]; // 13-14
//	byte chan[]; // 15-16
//	byte data[]; // 17-116
//	byte crc[]; // 117-118
//	long receivedTimeStamp; 
//	
//	public final int  JUNK_BYTE_SIZE = 10;
//	public final int  MOTEID_BYTE_SIZE = 2;
//	public final int  LASTSAMPLE_BYTE_SIZE = 2;
//	public final int  CHAN_BYTE_SIZE = 2;
//	public final int  DATA_SIZE = 50;
//	public final int  DATA_BYTE_SIZE = DATA_SIZE * 2;
//	public final int  CRC_BYTE_SIZE = 2;
//	
//	int currentPosition;
//	int junkPosition;
//	int moteIDPosition;
//	int lastSamplePosition;
//	int chanPosition;
//	int dataPosition;
//	int crcPosition;
//		
//	/*
//	 * A variable to hold the current status of the packet and its delivery 
//	 * to the framework
//	 */
//	int packetStatus;
//	
//	
//	public byte[] getJunk() 
//	{
//		return junk;
//	}
//	public void setJunk(byte[] junk) 
//	{
//		this.junk = junk;
//	}
//	
//	public void setJunkByteAtPosition(int position, byte dataByte)
//	{
//		if(position < this.JUNK_BYTE_SIZE)
//			this.junk[position] = dataByte;
//	}
//	
//	public byte getJunkByteAtPosition(int position)
//	{
//		byte data = -1;
//		if(position < this.JUNK_BYTE_SIZE)
//			data = this.junk[position];
//		return data;
//	}
//	
//	public void setNextJunkByte(byte dataByte)
//	{
//		if(junkPosition < this.JUNK_BYTE_SIZE)
//			junk[++junkPosition] = dataByte;
//	}
//	
//	public byte[] getMoteID() 
//	{
//		return moteID;
//	}
//	
//	public void setMoteID(byte[] moteID) 
//	{
//		this.moteID = moteID;
//	}
//	
//	public void setMoteIDByteAtPosition(int position, byte dataByte)
//	{
//		if(position < this.MOTEID_BYTE_SIZE)
//			this.moteID[position] = dataByte;
//	}
//	
//	public byte getMoteIDByteAtPosition(int position)
//	{
//		byte data = -1;
//		if(position < this.MOTEID_BYTE_SIZE)
//			data = this.moteID[position];
//		return data;
//	}
//	
//	public void setNextMoteIDByte(byte dataByte)
//	{
//		if(moteIDPosition < this.MOTEID_BYTE_SIZE)
//			moteID[++moteIDPosition] = dataByte;
//	}
//	
//	public byte[] getLastSample() 
//	{
//		return lastSample;
//	}
//	public void setLastSample(byte[] lastSample) 
//	{
//		this.lastSample = lastSample;
//	}
//	
//	public void setLastSampleByteAtPosition(int position, byte dataByte)
//	{
//		this.lastSample[position] = dataByte;
//	}
//	
//	public byte getLastSampleByteAtPosition(int position)
//	{
//		byte data = this.lastSample[position];
//		return data;
//	}
//	
//	public void setNextLastSampleByte(byte dataByte)
//	{
//		if(lastSamplePosition < this.LASTSAMPLE_BYTE_SIZE)
//			lastSample[++lastSamplePosition] = dataByte;
//	}
//	
//	public byte[] getChan() 
//	{
//		return chan;
//	}
//	
//	public void setChan(byte[] chan) 
//	{
//		this.chan = chan;
//	}
//	
//	public void setChanByteAtPosition(int position, byte dataByte)
//	{
//		if(chanPosition < this.CHAN_BYTE_SIZE)
//			this.chan[position] = dataByte;
//	}
//	
//	public byte getChanByteAtPosition(int position)
//	{
//		byte data = -1;
//		if(position < this.CHAN_BYTE_SIZE)
//			data = this.chan[position];
//		return data;
//	}
//	
//	public void setNextChanByte(byte dataByte)
//	{
//		if(chanPosition < this.CHAN_BYTE_SIZE)
//			chan[++chanPosition] = dataByte;
//	}
//	
//	
//	public byte[] getData() 
//	{
//		return data;
//	}
//	public void setData(byte[] data) 
//	{
//		this.data = data;
//	}
//	
//	public void setDataByteAtPosition(int position, byte dataByte)
//	{
//		if(position < this.DATA_BYTE_SIZE)
//		this.data[position] = dataByte;
//	}
//	
//	public byte getDataByteAtPosition(int position)
//	{
//		byte dataByte = -1;
//		if(position < this.DATA_BYTE_SIZE)
//			dataByte = this.data[position];
//		return dataByte;
//	}
//	
//	public byte[] getCrc() {
//		return crc;
//	}
//	public void setCrc(byte[] crc) {
//		this.crc = crc;
//	}
//	
//	public void setCrcByteAtPosition(int position, byte dataByte)
//	{
//		this.crc[position] = dataByte;
//	}
//	
//	public byte getCrcByteAtPosition(int position)
//	{
//		byte dataByte = this.crc[position];
//		return dataByte;
//	}
//	
//	public int getJUNK_BYTE_SIZE() 
//	{
//		return JUNK_BYTE_SIZE;
//	}
//	
//	public int getMOTEID_BYTE_SIZE() 
//	{
//		return MOTEID_BYTE_SIZE;
//	}
//	public int getLASTSAMPLE_BYTE_SIZE() 
//	{
//		return LASTSAMPLE_BYTE_SIZE;
//	}
//	public int getCHAN_BYTE_SIZE() 
//	{
//		return CHAN_BYTE_SIZE;
//	}
//	public int getDATA_SIZE() 
//	{
//		return DATA_SIZE;
//	}
//	public int getDATA_BYTE_SIZE() 
//	{
//		return DATA_BYTE_SIZE;
//	}
//	public int getCRC_BYTE_SIZE() {
//		return CRC_BYTE_SIZE;
//	}
//	
//	public long getReceivedTimeStamp() 
//	{
//		return receivedTimeStamp;
//	}
//	
//	public void setReceivedTimeStamp(long receivedTimeStamp) 
//	{
//		this.receivedTimeStamp = receivedTimeStamp;
//	}
//	
//	public int getCurrentPosition() {
//		return currentPosition;
//	}
//
//	public void setCurrentPosition(int currentPostion) {
//		this.currentPosition = currentPostion;
//	}
//
//	
//	public TOSOscopeBytePacket()
//	{
//		junk = new byte[this.JUNK_BYTE_SIZE];
//		moteID = new byte[this.MOTEID_BYTE_SIZE];
//		lastSample = new byte[this.LASTSAMPLE_BYTE_SIZE];
//		chan = new byte[this.CHAN_BYTE_SIZE];
//		data = new byte[this.DATA_BYTE_SIZE];
//		crc = new byte[this.CRC_BYTE_SIZE];
//		
//		currentPosition = 0;
//		junkPosition = 0;
//		moteIDPosition = 0;
//		lastSamplePosition = 0;
//		chanPosition = 0;
//		dataPosition = 0;
//		crcPosition = 0;
//		
//		receivedTimeStamp = 0;
//		
//	}
//
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
package org.fieldstream.service.sensors.mote.tinyos;

/*
 * A tinyos oscilloscope byte packet implementation for the Stress Project Tinyos Packet type
 * @author Somnath Mitra
 */
public class TOSOscopeBytePacket 
{
	
	// all ranges inclusive
	// 114 bytes total
	byte junk[]; 		// 0-5
	byte moteID[];		// 6-7
	byte lastSample[];  // 8-9
	byte chan[]; 		// 10-11
	byte data[]; 		// 12-111
	byte crc[]; 		// 112-113
	long receivedTimeStamp; 
	
	public final int JUNK_BYTE_SIZE = 6;
	public final int MOTEID_BYTE_SIZE = 2;
	public final int LASTSAMPLE_BYTE_SIZE = 2;
	public final int CHAN_BYTE_SIZE = 2;
	public final int DATA_SIZE = 50;
	public final int DATA_BYTE_SIZE = DATA_SIZE * 2;
	public final int CRC_BYTE_SIZE = 2;
	public static final int TOTAL_OSCOPE_BYTE_SIZE = 114; // Sum of above
	
	int currentPosition;
	int junkPosition;
	int moteIDPosition;
	int lastSamplePosition;
	int chanPosition;
	int dataPosition;
	int crcPosition;
		
	/*
	 * A variable to hold the current status of the packet and its delivery 
	 * to the framework
	 */
	int packetStatus;
	
	
	public byte[] getJunk() 
	{
		return junk;
	}
	public void setJunk(byte[] junk) 
	{
		this.junk = junk;
	}
	
	public void setJunkByteAtPosition(int position, byte dataByte)
	{
		if(position < this.JUNK_BYTE_SIZE)
			this.junk[position] = dataByte;
	}
	
	public byte getJunkByteAtPosition(int position)
	{
		byte data = -1;
		if(position < this.JUNK_BYTE_SIZE)
			data = this.junk[position];
		return data;
	}
	
	public void setNextJunkByte(byte dataByte)
	{
		if(junkPosition < this.JUNK_BYTE_SIZE)
			junk[++junkPosition] = dataByte;
	}
	
	public byte[] getMoteID() 
	{
		return moteID;
	}
	
	public void setMoteID(byte[] moteID) 
	{
		this.moteID = moteID;
	}
	
	public void setMoteIDByteAtPosition(int position, byte dataByte)
	{
		if(position < this.MOTEID_BYTE_SIZE)
			this.moteID[position] = dataByte;
	}
	
	public byte getMoteIDByteAtPosition(int position)
	{
		byte data = -1;
		if(position < this.MOTEID_BYTE_SIZE)
			data = this.moteID[position];
		return data;
	}
	
	public void setNextMoteIDByte(byte dataByte)
	{
		if(moteIDPosition < this.MOTEID_BYTE_SIZE)
			moteID[++moteIDPosition] = dataByte;
	}
	
	public byte[] getLastSample() 
	{
		return lastSample;
	}
	public void setLastSample(byte[] lastSample) 
	{
		this.lastSample = lastSample;
	}
	
	public void setLastSampleByteAtPosition(int position, byte dataByte)
	{
		this.lastSample[position] = dataByte;
	}
	
	public byte getLastSampleByteAtPosition(int position)
	{
		byte data = this.lastSample[position];
		return data;
	}
	
	public void setNextLastSampleByte(byte dataByte)
	{
		if(lastSamplePosition < this.LASTSAMPLE_BYTE_SIZE)
			lastSample[++lastSamplePosition] = dataByte;
	}
	
	public byte[] getChan() 
	{
		return chan;
	}
	
	public void setChan(byte[] chan) 
	{
		this.chan = chan;
	}
	
	public void setChanByteAtPosition(int position, byte dataByte)
	{
		if(chanPosition < this.CHAN_BYTE_SIZE)
			this.chan[position] = dataByte;
	}
	
	public byte getChanByteAtPosition(int position)
	{
		byte data = -1;
		if(position < this.CHAN_BYTE_SIZE)
			data = this.chan[position];
		return data;
	}
	
	public void setNextChanByte(byte dataByte)
	{
		if(chanPosition < this.CHAN_BYTE_SIZE)
			chan[++chanPosition] = dataByte;
	}
	
	
	public byte[] getData() 
	{
		return data;
	}
	public void setData(byte[] data) 
	{
		this.data = data;
	}
	
	public void setDataByteAtPosition(int position, byte dataByte)
	{
		if(position < this.DATA_BYTE_SIZE)
		this.data[position] = dataByte;
	}
	
	public byte getDataByteAtPosition(int position)
	{
		byte dataByte = -1;
		if(position < this.DATA_BYTE_SIZE)
			dataByte = this.data[position];
		return dataByte;
	}
	
	public byte[] getCrc() {
		return crc;
	}
	public void setCrc(byte[] crc) {
		this.crc = crc;
	}
	
	public void setCrcByteAtPosition(int position, byte dataByte)
	{
		this.crc[position] = dataByte;
	}
	
	public byte getCrcByteAtPosition(int position)
	{
		byte dataByte = this.crc[position];
		return dataByte;
	}
	
	public int getJUNK_BYTE_SIZE() 
	{
		return JUNK_BYTE_SIZE;
	}
	
	public int getMOTEID_BYTE_SIZE() 
	{
		return MOTEID_BYTE_SIZE;
	}
	public int getLASTSAMPLE_BYTE_SIZE() 
	{
		return LASTSAMPLE_BYTE_SIZE;
	}
	public int getCHAN_BYTE_SIZE() 
	{
		return CHAN_BYTE_SIZE;
	}
	public int getDATA_SIZE() 
	{
		return DATA_SIZE;
	}
	public int getDATA_BYTE_SIZE() 
	{
		return DATA_BYTE_SIZE;
	}
	public int getCRC_BYTE_SIZE() {
		return CRC_BYTE_SIZE;
	}
	
	public long getReceivedTimeStamp() 
	{
		return receivedTimeStamp;
	}
	
	public void setReceivedTimeStamp(long receivedTimeStamp) 
	{
		this.receivedTimeStamp = receivedTimeStamp;
	}
	
	public int getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(int currentPostion) {
		this.currentPosition = currentPostion;
	}

	
	public TOSOscopeBytePacket()
	{
		junk = new byte[this.JUNK_BYTE_SIZE];
		moteID = new byte[this.MOTEID_BYTE_SIZE];
		lastSample = new byte[this.LASTSAMPLE_BYTE_SIZE];
		chan = new byte[this.CHAN_BYTE_SIZE];
		data = new byte[this.DATA_BYTE_SIZE];
		crc = new byte[this.CRC_BYTE_SIZE];
		
		currentPosition = 0;
		junkPosition = 0;
		moteIDPosition = 0;
		lastSamplePosition = 0;
		chanPosition = 0;
		dataPosition = 0;
		crcPosition = 0;
		
		receivedTimeStamp = 0;
		
	}

}

