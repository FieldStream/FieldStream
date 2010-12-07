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
// @author Andrew Raij

package org.fieldstream.service.sensors.mote;

import java.util.Arrays;

import org.fieldstream.Constants;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensors.api.AbstractSensor;

import android.text.InputFilter.LengthFilter;




public class GenericMoteSensor extends AbstractSensor implements
		MoteUpdateSubscriber {

	private static final String LOGTAG = "GenericMoteSensor";
	private static final boolean ACCELCHESTSCHEDULER = false;
	private static final int ACCELCHESTFRAMERATE = 10;
	private static final int ACCELCHESTWINDOWSIZE = 60 * ACCELCHESTFRAMERATE+40;// +
	
	private static final boolean ECGSCHEDULER = false;
	private static final int ECGFRAMERATE = 64; // 128;
	private static final int ECGWINDOWSIZE = 60 * ECGFRAMERATE;
	
	private static final boolean GSRSCHEDULER = false;
	private static final int GSRFRAMERATE = 10;
	private static final int GSRWINDOWSIZE = 60 * GSRFRAMERATE +40 ; 
	
	private static final boolean RIPSCHEDULER = false;
	private static final int RIPFRAMERATE = 64;
	private static final int RIPWINDOWSIZE = 60 * RIPFRAMERATE;

	private static final boolean BODYTEMPSCHEDULER = false;
	private static final int BODYTEMPFRAMERATE = 10;
	private static final int BODYTEMPWINDOWSIZE = 60 * BODYTEMPFRAMERATE+40; // this allows us to get closer to the real frequency of 10.67

	private static final boolean AMBIENTTEMPSCHEDULER = false;
	private static final int AMBIENTTEMPFRAMERATE = 10;
	private static final int AMBIENTTEMPWINDOWSIZE = 60 * AMBIENTTEMPFRAMERATE+40; // this allows us to get closer to the real frequency of 10.67

	private static final boolean ALCOHOLSCHEDULER = false;
	private static final int ALCOHOLFRAMERATE = 1;
	private static final int ALCOHOLWINDOWSIZE = 60 * ALCOHOLFRAMERATE; // 
	
	
	public GenericMoteSensor(int SensorID) {
		super(SensorID);
		sensorID=SensorID;
		switch (sensorID) {
			case Constants.SENSOR_ACCELCHESTX: 
				initalize(ACCELCHESTSCHEDULER,ACCELCHESTWINDOWSIZE, ACCELCHESTWINDOWSIZE);
				break;
			case Constants.SENSOR_ACCELCHESTY: 
				initalize(ACCELCHESTSCHEDULER,ACCELCHESTWINDOWSIZE, ACCELCHESTWINDOWSIZE);
				break;
			
			case Constants.SENSOR_ACCELCHESTZ: 
				initalize(ACCELCHESTSCHEDULER,ACCELCHESTWINDOWSIZE, ACCELCHESTWINDOWSIZE);
				break;
			
			case Constants.SENSOR_ECK: 
				initalize(ECGSCHEDULER,ECGWINDOWSIZE, ECGWINDOWSIZE);
				break;
			
			case Constants.SENSOR_GSR: 
				initalize(GSRSCHEDULER, GSRWINDOWSIZE, GSRWINDOWSIZE);
				break;
			case Constants.SENSOR_RIP: 
				initalize(RIPSCHEDULER, RIPWINDOWSIZE, RIPWINDOWSIZE);
				break;			
			case Constants.SENSOR_BODY_TEMP: 
				initalize(BODYTEMPSCHEDULER, BODYTEMPWINDOWSIZE, BODYTEMPWINDOWSIZE);
				break;
			case Constants.SENSOR_AMBIENT_TEMP: 
				initalize(AMBIENTTEMPSCHEDULER, AMBIENTTEMPWINDOWSIZE, AMBIENTTEMPWINDOWSIZE);
				break;				
			case Constants.SENSOR_ALCOHOL: 
				initalize(ALCOHOLSCHEDULER, ALCOHOLWINDOWSIZE, ALCOHOLWINDOWSIZE);
				break;								
		}
		if (Log.VERBOSE) Log.v(LOGTAG,"instanciating  "+sensorID);
	}

	private int sensorID;

	private MoteSensorManager mMoteSensorManager;

	
	
	public void onReceiveData(int SensorID, int[] data, long[] timeStamps) {
		if(SensorID == this.sensorID)
		{
			if (Log.VERBOSE) Log.v(LOGTAG,"received Data "+data.length);
				addValue(data, timeStamps);
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
		MoteSensorManager.getInstance().registerListener(this);
		if (Log.DEBUG) Log.d(LOGTAG,"Mote Sensor "+sensorID+" active!");
		active=true;
		
	}

	@Override
	public void deactivate() {
//		if (mMoteSensorManager != null) 
//		{
//			mMoteSensorManager.unregisterListener(this);
//		}
		MoteSensorManager.getInstance().unregisterListener(this);
		active=false;
		if (Log.DEBUG) Log.d(LOGTAG,"Mote Sensor "+sensorID+" deactivated!");
	}
	
	public MoteSensorManager getmMoteSensorManager() {
		return mMoteSensorManager;
	}


	public void setmMoteSensorManager(MoteSensorManager mMoteSensorManager) {
		this.mMoteSensorManager = mMoteSensorManager;
	}


	public static String getLogtag() {
		return LOGTAG;
	}


	public static boolean isAccelchestscheduler() {
		return ACCELCHESTSCHEDULER;
	}


	public static int getAccelchestframerate() {
		return ACCELCHESTFRAMERATE;
	}


	public static int getAccelchestwindowsize() {
		return ACCELCHESTWINDOWSIZE;
	}


	public static boolean isEcgscheduler() {
		return ECGSCHEDULER;
	}


	public static int getEcgframerate() {
		return ECGFRAMERATE;
	}


	public static int getEcgwindowsize() {
		return ECGWINDOWSIZE;
	}


	public static boolean isGsrscheduler() {
		return GSRSCHEDULER;
	}


	public static int getGsrframerate() {
		return GSRFRAMERATE;
	}


	public static int getGsrwindowsize() {
		return GSRWINDOWSIZE;
	}


	public static boolean isRipscheduler() {
		return RIPSCHEDULER;
	}


	public static int getRipframerate() {
		return RIPFRAMERATE;
	}


	public static int getRipwindowsize() {
		return RIPWINDOWSIZE;
	}


	public static boolean isBodytempscheduler() {
		return BODYTEMPSCHEDULER;
	}


	public static int getBodytempframerate() {
		return BODYTEMPFRAMERATE;
	}


	public static int getBodytempwindowsize() {
		return BODYTEMPWINDOWSIZE;
	}


	public static boolean isAmbienttempscheduler() {
		return AMBIENTTEMPSCHEDULER;
	}


	public static int getAmbienttempframerate() {
		return AMBIENTTEMPFRAMERATE;
	}


	public static int getAmbienttempwindowsize() {
		return AMBIENTTEMPWINDOWSIZE;
	}


	public static boolean isAlcoholscheduler() {
		return ALCOHOLSCHEDULER;
	}


	public static int getAlcoholframerate() {
		return ALCOHOLFRAMERATE;
	}


	public static int getAlcoholwindowsize() {
		return ALCOHOLWINDOWSIZE;
	}

}
