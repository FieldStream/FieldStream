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



package org.fieldstream.service.sensors.phone;

import org.fieldstream.Constants;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensors.api.AbstractSensor;

/**
 * Implementation of the Phone gps. Extends {@link AbstractSensor}. 
 * Based on the given {@link AbstractSensor#ID}, either the lat, long or speed will be passed on.
 * @author Patrick Blitz
 * @author Somnath Mitra
 *
 */

public class LocationSensor extends AbstractSensor{

	private static final boolean LOCATIONSCHEDULER = true;
	private static final int LOCATIONFRAMERATE = 1; // 1Hz update rate
	private static final int LOCATIONWINDOWSIZE = 30000 * LOCATIONFRAMERATE;   // milliseconds to send out data
	private LocatioN  locationClass;
	/**
	 * 
	 * @param SensorID
	 */
	public LocationSensor(int SensorID) {
		super(SensorID);
				
		initalize(LOCATIONSCHEDULER,LOCATIONWINDOWSIZE,LOCATIONWINDOWSIZE);
		locationClass = LocatioN.getInstance();
		switch (SensorID) {
		case Constants.SENSOR_LOCATIONLATITUDE:
			locationClass.locationLatitude = this;
			break;
		case Constants.SENSOR_LOCATIONLONGITUDE:
			locationClass.locationLongitude = this;
			break;
		case Constants.SENSOR_LOCATIONSPEED:
			locationClass.locationSpeed = this;
		default:
			break;
		} 
	}
	/**
	 * active this sensor. if this is the first GPS sensor to be turned on, activate the sensor listener.
	 */
	@Override
	public void activate() {
		if (locationClass.active==0) {
			locationClass.activateSensor();
		}
		locationClass.active++;
		this.active = true;
	}
	/**
	 * deactivate this sensor. If all GPS sensors are deactive, deactivate the complete sensor.
	 */
	@Override
	public void deactivate() {
		locationClass.active--;
		if (locationClass.active==0) {
			locationClass.deactivateSensor();
		}
		this.active=false;
		if (Log.DEBUG) Log.d("LocationSensor","deactivated");
	}

	
	

}
