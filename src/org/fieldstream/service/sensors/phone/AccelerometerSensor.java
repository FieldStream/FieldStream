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
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensors.api.AbstractSensor;

/**
 * Implementation of the Phone accelerometer. Extends {@link AbstractSensor} to really the sensor values. 
 * Based on the given {@link AbstractSensor#ID}, either the X, Y, Z or Magnitude readings will be passed on.
 * @author Patrick Blitz
 *
 */
public class AccelerometerSensor  extends AbstractSensor {

	private static final boolean ACCELSCHEDULER = true;
	private static final int ACCELWINDOWSIZE = 10000;
	private Accelerometer accelClass;
	/**
	 * 
	 * @param SensorID
	 */
	public AccelerometerSensor(int SensorID) {
		super(SensorID);
		
		
		initalize(ACCELSCHEDULER,ACCELWINDOWSIZE,ACCELWINDOWSIZE);
		accelClass = Accelerometer.getInstance();
		switch (SensorID) {
		case Constants.SENSOR_ACCELPHONEMAG:
			accelClass.magnitude = this;
			break;
		case Constants.SENSOR_ACCELPHONEX:
			accelClass.accelX = this;
			break;
		case Constants.SENSOR_ACCELPHONEY:
			accelClass.accelY = this;
			break;
		case Constants.SENSOR_ACCELPHONEZ:
			accelClass.accelZ = this;
			break;
		default:
			break;
		} 
	}
	/**
	 * active this sensor. if this is the first phone accelerometer sensor to be turned on, activate the sensor listener.
	 */
	@Override
	public void activate() {
		if (accelClass.active==0) {
			accelClass.activateSensor();
		}
		accelClass.active++;
		this.active = true;
	}
	/**
	 * deactivate this sensor. If all phone accelerometer sensors are deactive, deactivate the complete sensor.
	 */
	@Override
	public void deactivate() {
		accelClass.active--;
		if (accelClass.active==0) {
			accelClass.deactivateSensor();
		}
		this.active=false;
		if (Log.DEBUG) Log.d("AccelSensor","deacitvated");
	}


}
