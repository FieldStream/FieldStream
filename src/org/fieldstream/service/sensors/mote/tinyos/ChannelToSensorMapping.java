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

//@author Somnath Mitra
//@author Andrew Raij


package org.fieldstream.service.sensors.mote.tinyos;

import org.fieldstream.Constants;

import android.util.Log;

public class ChannelToSensorMapping {
	private static int AUTOSENSE_VERSION = 1;   // change this to 1 if you are using the 
												// old hardware with RIP and ECG on different motes	
	public static final int ECG = 0;
	public static final int ACCELX = 1;
	public static final int ACCELY = 2;
	public static final int ACCELZ = 3;
	public static final int GSR = 4;
	public static final int BODY_TEMP = 5;
	public static final int AMBIENT_TEMP = 6;
	public static final int RIP = 7;
	public static final int ALCOHOL = 10;
	
	public static final int MIN_AGREED_MOTE_CHANNEL = 0;
	public static final int MAX_AGREED_MOTE_CHANNEL = 10;
	
	public static int mapMoteChannelToMoteSensor(int channel)
	{
		int map = -1;
		if (channel >=MIN_AGREED_MOTE_CHANNEL && channel <= MAX_AGREED_MOTE_CHANNEL)
			map = channel;
		return map;
	}
	
	public static int mapMoteChannelToPhoneSensor(int moteID, int channel)
	{
		int map = -1;

		if (AUTOSENSE_VERSION == 2) {
			switch(channel)
			{
			case(ECG):		map = Constants.SENSOR_ECK;
							break;
			case(GSR): 		map = Constants.SENSOR_GSR;
							break;
			case(ACCELX):	map = Constants.SENSOR_ACCELCHESTX;
							break;
			case(ACCELY):	map = Constants.SENSOR_ACCELCHESTY;
							break;	
			case(ACCELZ):	map = Constants.SENSOR_ACCELCHESTZ;
							break;
			case(BODY_TEMP):		map = Constants.SENSOR_BODY_TEMP;
	 								break;
			case(AMBIENT_TEMP):		map = Constants.SENSOR_AMBIENT_TEMP;
					 				break;
			case(RIP):		map = Constants.SENSOR_RIP;
	 						break;
			case(ALCOHOL):		map = Constants.SENSOR_ALCOHOL;
				break;	 							
			default:		break;
			}			
		}
		else {
			if (moteID % 10 == 1) {		
				switch(channel)
				{
				case(ECG):		map = Constants.SENSOR_ECK;
								break;
				case(GSR): 		map = Constants.SENSOR_GSR;
								break;
				case(ACCELX):	map = Constants.SENSOR_ACCELCHESTX;
								break;
				case(ACCELY):	map = Constants.SENSOR_ACCELCHESTY;
								break;	
				case(ACCELZ):	map = Constants.SENSOR_ACCELCHESTZ;
								break;
				case(BODY_TEMP):		map = Constants.SENSOR_BODY_TEMP;
		 								break;
				case(AMBIENT_TEMP):		map = Constants.SENSOR_AMBIENT_TEMP;
						 				break;
				case(RIP):		map = Constants.SENSOR_RIP;
		 						break;
				default:		break;
				}
			}
			else {
				map = Constants.SENSOR_RIP;   // Allows backwards compatibility with AutoSense hardware v1, 											  // which had a separate mote for respiration whose moteID % 10 == 2
			}
		}
		
//		if (moteID == 1){
//			map = Constants.SENSOR_RIP;
//		}
//		else if (moteID % 10 == 1) {		
//		switch(channel)
//		{
//		case(ECG):		map = Constants.SENSOR_ECK;
//						break;
//		case(GSR): 		map = Constants.SENSOR_GSR;
//						break;
//		case(ACCELX):	map = Constants.SENSOR_ACCELCHESTX;
//						break;
//		case(ACCELY):	map = Constants.SENSOR_ACCELCHESTY;
//						break;	
//		case(ACCELZ):	map = Constants.SENSOR_ACCELCHESTZ;
//						break;
//		case(BODY_TEMP):		map = Constants.SENSOR_BODY_TEMP;
// 		break;
//		case(AMBIENT_TEMP):		map = Constants.SENSOR_AMBIENT_TEMP;
//				 		break;
//		default:		break;
//		}
//	}
		
		return map;
	}

//  TODO: needs to take into account channels
//	public static int mapPhoneSensorToMoteChannel(int SensorID)
//	{
//		int map = -1;
//		
//		switch(SensorID)
//		{
//		case(Constants.SENSOR_ACCELCHESTX): 	map = ACCELX;
//												break;
//												
//		case(Constants.SENSOR_ACCELCHESTY):		map = ACCELY;
//												break;
//												
//		case(Constants.SENSOR_ACCELCHESTZ):		map = ACCELZ;
//												break;
//												
//		case(Constants.SENSOR_ECK): 			map = ECG;
//												break;
//												
//		case(Constants.SENSOR_GSR):				map = GSR;
//												break;
//		
//		case(Constants.SENSOR_RIP):				map = RIP;
//												break;
//												
//		case(Constants.SENSOR_TEMP):			map = TEMP;
//												break;											
//
//												
//		}
//		
//		return map;
//	}
	
}
