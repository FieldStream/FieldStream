//Copyright (c) 2010, Carnegie Mellon University
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
//    * Neither the name of Carnegie Mellon University nor the names of its contributors may be used to 
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
package org.fieldstream.service.sensors.zephyr;

import java.util.Arrays;

import org.fieldstream.Constants;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensors.api.AbstractSensor;

import android.text.InputFilter.LengthFilter;


//@author Patrick Blitz


public class GenericZephyrSensor extends AbstractSensor implements
		ZephyrUpdateSubscriber {

	private static final String LOGTAG = "GenericMoteSensor";
	private static final boolean ACCELCHESTSCHEDULER = false;
	private static final int ACCELCHESTFRAMERATE = 10;
	private static final int ACCELCHESTWINDOWSIZE = 60 * ACCELCHESTFRAMERATE;
	
	private static final boolean ECGSCHEDULER = false;
	private static final int ECGFRAMERATE = 60; // 128;
	private static final int ECGWINDOWSIZE = 60 * ECGFRAMERATE;
	
	private static final boolean GSRSCHEDULER = false;
	private static final int GSRFRAMERATE = 10;
	private static final int GSRWINDOWSIZE = 60 * GSRFRAMERATE;
	
	private static final boolean RIPSCHEDULER = false;
	private static final int RIPFRAMERATE = 60;
	private static final int RIPWINDOWSIZE = 60 * RIPFRAMERATE;

	private static final boolean TEMPSCHEDULER = false;
	private static final int TEMPFRAMERATE = 10;
	private static final int TEMPWINDOWSIZE = 60 * TEMPFRAMERATE;
	
	public GenericZephyrSensor(int SensorID) {
		super(SensorID);
		sensorID=SensorID;
		switch (sensorID) {
			case Constants.SENSOR_ZEPHYR_ACL: 
				initalize(ACCELCHESTSCHEDULER,ACCELCHESTWINDOWSIZE, ACCELCHESTWINDOWSIZE);
				break;
//			case Constants.SENSOR_ACCELCHESTY: 
//				initalize(ACCELCHESTSCHEDULER,ACCELCHESTWINDOWSIZE, ACCELCHESTWINDOWSIZE);
//				break;
//			
//			case Constants.SENSOR_ACCELCHESTZ: 
//				initalize(ACCELCHESTSCHEDULER,ACCELCHESTWINDOWSIZE, ACCELCHESTWINDOWSIZE);
//				break;
//			
			case Constants.SENSOR_ZEPHYR_ECG: 
				initalize(ECGSCHEDULER,ECGWINDOWSIZE, ECGWINDOWSIZE);
				break;
			
			case Constants.SENSOR_ZEPHYR_RSP: 
				initalize(RIPSCHEDULER, RIPWINDOWSIZE, RIPWINDOWSIZE);
				break;			
			case Constants.SENSOR_ZEPHYR_TMP: 
				initalize(TEMPSCHEDULER, TEMPWINDOWSIZE, TEMPWINDOWSIZE);
				break;
		}
		if (Log.VERBOSE) Log.v(LOGTAG,"instanciating  "+sensorID);
	}

	private int sensorID;



	
	
	public void receiveNewData(int SensorID, int data) {
		if(SensorID == this.sensorID)
		{
			long t = System.currentTimeMillis();
			//long[] timeStamps = new long[data.length]; 
//			Arrays.fill(timeStamps, 0, data.length, t);
			
			if (Log.VERBOSE) Log.v(LOGTAG,"received Data "+data);
			addValue(data, t);
		//	addFreeTextLog(((Integer)counter).toString());
		}
		
	}


	public void setSensorID(int sensorID) {
		this.sensorID = sensorID;
		
	}

	public int getSensorID() {
		return sensorID;
	}
	
	
	@Override
	public void activate() {
		ZephyrManager.getINSTANCE().register(this);
		if (Log.DEBUG) Log.d(LOGTAG,"Mote Sensor "+sensorID+" active!");
		active=true;
		
	}

	@Override
	public void deactivate() {
			ZephyrManager.getINSTANCE().unregister(this);
		
		active=false;
		
	}
}
