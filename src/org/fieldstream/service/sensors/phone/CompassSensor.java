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

//@author Patrick Blitz
//@author Somnath Mitra

package org.fieldstream.service.sensors.phone;

import org.fieldstream.Constants;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensors.api.AbstractSensor;

/**
 * Implementation of the Phone compass. Extends {@link AbstractSensor} to really the sensor values. 
 * Based on the given {@link AbstractSensor#ID}, either the X, Y, Z or Magnitude readings will be passed on.
 * @author Somnath Mitra
 *
 */
public class CompassSensor  extends AbstractSensor {

	private static final boolean COMPASSSCHEDULER = true;
	private static final int COMPASSWINDOWSIZE = 10000;
	private Compass compassClass;
	/**
	 * 
	 * @param SensorID
	 */
	public CompassSensor(int SensorID) {
		super(SensorID);
		
		
		initalize(COMPASSSCHEDULER,COMPASSWINDOWSIZE,COMPASSWINDOWSIZE);
		compassClass = Compass.getInstance();
		switch (SensorID) {
		case Constants.SENSOR_COMPASSPHONEMAG:
			compassClass.magnitude = this;
			break;
		case Constants.SENSOR_COMPASSPHONEX:
			compassClass.compassX = this;
			break;
		case Constants.SENSOR_COMPASSPHONEY:
			compassClass.compassY = this;
			break;
		case Constants.SENSOR_COMPASSPHONEZ:
			compassClass.compassZ = this;
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
		if (compassClass.active==0) {
			compassClass.activateSensor();
		}
		compassClass.active++;
		this.active = true;
		if (Log.DEBUG) Log.d("CompassSensor","acitvated");
	}
	/**
	 * deactivate this sensor. If all phone accelerometer sensors are deactive, deactivate the complete sensor.
	 */
	@Override
	public void deactivate() {
		compassClass.active--;
		if (compassClass.active==0) {
			compassClass.deactivateSensor();
		}
		this.active=false;
		if (Log.DEBUG) Log.d("CompassSensor","deacitvated");
	}


}
