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
package org.fieldstream.service.sensor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.fieldstream.Constants;
import org.fieldstream.service.logger.Log;


/**
 * Singleton class that relays all sensor update to the subscribers
 * @author Patrick Blitz
 * @author Andrew Raij
 * @author Mahbub Rahman
 */

public class SensorBus {
	
	//following two static variables are for debugging and cut out after debug
//	public static String sensorBusDataDump="";
//	public static String sensorBusTimestampDump="";

	private ArrayList<SensorBusSubscriber> subscribers;
	private static SensorBus INSTANCE;

	public static SensorBus getInstance() {
		if (INSTANCE==null) {
			INSTANCE = new SensorBus();
		}
		return INSTANCE;
	}

	protected void finalize() {
		Log.d("SensorBus", "Garbage Collected");
//		try {
//			file.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	private SensorBus() {
		subscribers = new ArrayList<SensorBusSubscriber>();
	}

	/**
	 * 
	 * @param subscriber
	 */
	
	public void subscribe(SensorBusSubscriber subscriber) {
		if (!subscribers.contains(subscriber)) {
			subscribers.add(subscriber);

		}

	}

	/**
	 * unsubscribe from this bus
	 * @param subscriber
	 */
	
	public void unsubscribe(SensorBusSubscriber subscriber) {
		if (subscribers.contains(subscriber)) {
			subscribers.remove(subscriber); 
		}
	}
	/**
	 * propagates a new buffer of values to the subscribers. 
	 * In the case of sliding windows, only some of this data is actually new
	 * @param sensorID
	 * @param data
	 * @param timestamps
	 * @param startNewData The starting index of the new data 
	 * @param endNewData The ending index of the new data
	 */

	public void receiveBuffer(int sensorID, int[] data, long[] timestamps, int startNewData, int endNewData) {
		if(sensorID==Constants.SENSOR_VIRTUAL_REALPEAKVALLEY && Log.DEBUG)
		{
			String sensor = "";
			for (int i=0; i < data.length; i++) {
				sensor += data[i] + ",";
			}
			//for debugging
			String sTimestamp="";
			for(int i=0;i<timestamps.length;i++)
			{
				sTimestamp+=timestamps[i]+",";
			}
			Log.d("SensorBus", Constants.getSensorDescription(sensorID) + " Data = " + sensor);	
			Log.d("SensorBus", Constants.getSensorDescription(sensorID) + " Timestamp = " + sTimestamp);
		}
		
		if (Log.DEBUG) {
			Long t = data.length > 0 ? timestamps[0] : 0;
			Log.d("SensorBus", "Got a " + data.length + " sample buffer of " + Constants.getSensorDescription(sensorID) + " with timestamp " + t);
		}
		if (subscribers!= null) {
			for (int i=0;  i<subscribers.size(); i++) {
//				Log.d("SensorBus", "sending buffer to " + subscribers.get(i).toString());
				subscribers.get(i).receiveBuffer(sensorID, data, timestamps, startNewData, endNewData);
			}
		}
	}
}
