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
 * Implementation of the Battery Level Sensor. Extends {@link AbstractSensor} to really the sensor values. 
 * Sends updates to the battery level on to the next level (percentages)
 * @author Patrick Blitz
 * @author Andrew Raij
 * 
 *
 */

public class BatteryLevelSensor extends AbstractSensor {

		private static final boolean BATTSCHEDULER = false;
		private static final int BATTWINDOWSIZE = 1;
		private BatteryLevel battClass;
		/**
		 * 
		 * @param SensorID
		 */
		public BatteryLevelSensor(int SensorID) {
			super(SensorID);
			
			initalize(BATTSCHEDULER,BATTWINDOWSIZE,BATTWINDOWSIZE);
			battClass = BatteryLevel.getInstance();
			switch (SensorID) {
			case Constants.SENSOR_BATTERY_LEVEL:
				battClass.batteryLevelSensor = this;
				break;
			default:
				break;
			} 
		}

		/**
		 * active this sensor. 
		 */
		@Override
		public void activate() {
			if (!battClass.isActive()) {
				battClass.activate();
			}
			this.active = true;
			if (Log.DEBUG) Log.d("BatteryLevelSensor","activated");
		}
		/**
		 * deactivate this sensor. 
		 */
		@Override
		public void deactivate() {
			if (battClass.isActive()) {
				battClass.deactivate();
			}
			this.active=false;
			if (Log.DEBUG) Log.d("BatteryLevelSensor","deacitvated");
		}


	}
