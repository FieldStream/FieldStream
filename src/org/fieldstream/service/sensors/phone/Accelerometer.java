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
//    * Neither the names of Carnegie Mellon University nor the names of its contributors may be used to 
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
package org.fieldstream.service.sensors.phone;

import org.fieldstream.Constants;
import org.fieldstream.service.InferrenceService;
import org.fieldstream.service.sensors.api.AbstractSensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
/**
 * Wrapper class that manages the communication with the phone accelerometer. has the actual sensors (instances of {@link AbstractSensor}) as inner classes.
 * @author Patrick Blitz
 *
 */
public class Accelerometer  implements SensorEventListener{
	private SensorManager mSensorManager;
	/**
	 * The multiplier we use to get integer values instead of floats!
	 */
	private static final int MULTIPLIER = 100;
	//	private static final String TAG = "AccelPhone";
	static Accelerometer INSTANCE;
	//	private 
	/**
	 * magnitude Sensors (see {@link AccelerometerSensor} ). If this is non-null, magnitude values will be passed to the  Magnitude Sensor.
	 */
	protected AccelerometerSensor magnitude ;
	/**
	 * X values Sensors (see {@link AccelerometerSensor} ). If this is non-null, the read X values will be passed to the Sensor.
	 */
	protected AccelerometerSensor accelX ;
	protected AccelerometerSensor accelY ;
	protected AccelerometerSensor accelZ ;
	protected int active=0;
	static Accelerometer getInstance() {
		if (INSTANCE==null) {
			INSTANCE = new Accelerometer();
		} 
		return INSTANCE;
	}
	protected Accelerometer() {


	}






	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		//Nothing
	}
	/**
	 * Overrides {@link SensorEventListener} to retrieve sensor Readings.
	 * @param event 
	 */
	public void onSensorChanged(SensorEvent event) {
		// TODO what about the multiplier
		
		// I don't think we can use event.timestamp.
		// It seems event.timestamp is the # of nanoseconds since the device was booted, 
		// rather than # of nanoseconds since the epoch.
		// long timestamp = event.timestamp / 1000000;   // convert to milliseconds
		
		long timestamp = System.currentTimeMillis();  // assume this is close to the real timestamp
		
		if (accelX!= null) {
			accelX.addValue((int)(event.values[SensorManager.DATA_X]*MULTIPLIER), timestamp);	
		}
		if (accelY!= null) {
			accelY.addValue((int)(event.values[SensorManager.DATA_Y]*MULTIPLIER), timestamp);	
		}
		if (accelZ!= null) {
			accelZ.addValue((int)(event.values[SensorManager.DATA_Z]*MULTIPLIER), timestamp);	
		}
		if (magnitude!= null) {
			magnitude.addValue((int)Math.floor(
					Math.sqrt(event.values[SensorManager.DATA_X]*event.values[SensorManager.DATA_X]+
							event.values[SensorManager.DATA_Y]*event.values[SensorManager.DATA_Y]+
							event.values[SensorManager.DATA_Z]*event.values[SensorManager.DATA_Z])
							*MULTIPLIER), timestamp);	
		}
	}


	public void activateSensor() {
		mSensorManager = (SensorManager) InferrenceService.INSTANCE.getSystemService(Context.SENSOR_SERVICE);
		mSensorManager.registerListener(this,
				mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(Constants.ACCELEROMETER_LOCATION), //accel is 0 here, may change
				SensorManager.SENSOR_DELAY_FASTEST);

	}

	public void deactivateSensor() {
		if (mSensorManager != null) {
			mSensorManager.unregisterListener(this);
		}
		

	}

}
