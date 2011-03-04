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
// @author Andrew Raij


package org.fieldstream.service;

import java.util.ArrayList;
import java.util.HashMap;

import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensor.FeatureBusSubscriber;
import org.fieldstream.service.sensor.SensorBus;
import org.fieldstream.service.sensor.SensorBusSubscriber;
import org.fieldstream.service.sensors.api.AbstractFeature;
import org.fieldstream.service.sensors.api.AbstractSensor;
import org.fieldstream.service.sensors.mote.bluetooth.Reader;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * FeatureCalculation Class that executes the features for Sensors. Initialized by the {@link ActivationManager}.
 * @author blitz
 *
 */


public class FeatureCalculation implements SensorBusSubscriber {
	/**
	 * contains a mapping from SensorID (the input) to a list of features that will be calculated on this sensor (if active)
	 */
	public static HashMap<Integer,ArrayList<AbstractFeature>> mapping= new HashMap<Integer, ArrayList<AbstractFeature>>();;

	private static FeatureCalculation INSTANCE;

	private final static String TAG = "FeatureCalc";

	private FeatureRunner featureRunner;

	public boolean active;
	
	private FeatureCalculation() {
		active = true;
		SensorBus.getInstance().subscribe(this);
	}

	static public FeatureCalculation getInstance() {
		if (INSTANCE==null) {
			INSTANCE = new FeatureCalculation();
		}
		return INSTANCE;
	}
	
	public void finalize() {
		active = false;
		INSTANCE = null;
		SensorBus.getInstance().unsubscribe(this);
		featureRunner= null;
		
		Log.d(TAG, "Destroyed FeatureCalculation");
	}

	protected synchronized void setMap(HashMap<Integer, ArrayList<AbstractFeature>> newMap) { 
		mapping = newMap;
	}


	/**
	 * Receives a window worth of sensor values, usually from any subclass of {@link AbstractSensor}. 
	 * @param sensorID the ID of the calling Sensor
	 * @param buffer an array of ints containing the sensor values
	 */

	public void receiveBuffer(int sensorID, int[] buffer, long[] timestamps,
			int startNewData, int endNewData) {
		if (Log.DEBUG) Log.d("FeatureCalculation", "sensor ID = " + sensorID);

		if (active) {
			if (mapping!=null && !mapping.isEmpty()) {
				if (featureRunner== null) {
					Log.d("FeatureCalculation","Init FeatureRunner");
					featureRunner = FeatureRunner.getInstance();
				}
				
				featureRunner.addBuffer(new FeatureData(sensorID,buffer,timestamps));		
			}
		}
	}

}
