package org.fieldstream.service.sensors.mote;

import org.fieldstream.Constants;

public class PacketLoss {
	
	public static final int MISSING_TOKEN_ECG = -100;
	public static final int MISSING_TOKEN_ACCEL_X = -101;
	public static final int MISSING_TOKEN_ACCEL_Y = -102;
	public static final int MISSING_TOKEN_ACCEL_Z = -103;
	public static final int MISSING_TOKEN_TEMP_BODY = -104;
	public static final int MISSING_TOKEN_TEMP_AMBIENT = -105;
	public static final int MISSING_TOKEN_GSR = -106;
	
	public static final int MISSING_TOKEN_RIP = -110;
	
	
	public static final int MISSING_TOKEN_ALCOHOL = -210;
	
	public static int getMissingToken(int sensorID)
	{
		int missingToken = 0;
		
		switch(sensorID)
		{
			case Constants.SENSOR_ACCELCHESTX:
				missingToken = MISSING_TOKEN_ACCEL_X;
				break;
			
			case Constants.SENSOR_ACCELCHESTY:
				missingToken = MISSING_TOKEN_ACCEL_Y;
				break;
			
			case Constants.SENSOR_ACCELCHESTZ:
				missingToken = MISSING_TOKEN_ACCEL_Z;
				break;
			
			case Constants.SENSOR_ECK:
				missingToken = MISSING_TOKEN_ECG;
				break;
				
			case Constants.SENSOR_AMBIENT_TEMP:
				missingToken = MISSING_TOKEN_TEMP_AMBIENT;
				break;
				
			case Constants.SENSOR_BODY_TEMP:
				missingToken = MISSING_TOKEN_TEMP_BODY;
				break;
				
			case Constants.SENSOR_GSR:
				missingToken = MISSING_TOKEN_GSR;
				break;
				
			case Constants.SENSOR_RIP:
				missingToken = MISSING_TOKEN_RIP;
				break;
				
			case Constants.SENSOR_ALCOHOL:
				missingToken = MISSING_TOKEN_ALCOHOL;
				break;
		}
		return missingToken;
	}
	
	
}
