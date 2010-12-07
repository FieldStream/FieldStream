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

// @author Mishfaq Ahmed
// @author Patrick Blitz
// @author Monowar Hossain
// @author Somnath Mitra
// @author Kurt Plarre
// @author Mahbub Rahman
// @author Andrew Raij


package org.fieldstream;

import java.util.HashMap;
import java.util.HashSet;

public class Constants {
	/**
	 * the subject used to identify the correct personalization attributes!
	 */
	public static String SUBJECT = "generic";
	
	public static String moteAddress;
	
	/**
	 * application debug status, writes a trace to file!
	 */
	public final static boolean WRITETRACE=false;
		
	public final static boolean BUZZ=true;
	
	public final static boolean NETWORK_LOGGING=false;
	
	public final static boolean GPS_LOGGING=true;
	
	/**
	 * file that is used to read and write the configuration
	 */
	public final static String APP_DIR="StressInferencePhone";
	public static final String LOG_DIR = APP_DIR + "/logs";
	public static final String CONFIG_DIR = APP_DIR + "/config";
	public static final String NETWORK_CONFIG_FILENAME = "network.xml";
	public static final String DEAD_PERIOD_CONFIG_FILENAME = "dead_periods.xml";	
	
	
	public static final int SENSOR_GSR = 11;
	public static final int SENSOR_ECK = 12;
	public static final int SENSOR_BODY_TEMP = 13;
	public static final int SENSOR_AMBIENT_TEMP = 14;
	public static final int SENSOR_ACCELPHONEY = 31;
	public static final int SENSOR_ACCELPHONEX = 30;
	public static final int SENSOR_ACCELPHONEZ = 32;
	public static final int SENSOR_ACCELPHONEMAG = 33;
	public static final int SENSOR_COMPASSPHONEX = 34;
	public static final int SENSOR_COMPASSPHONEY = 35;
	public static final int SENSOR_COMPASSPHONEZ = 36;
	public static final int SENSOR_COMPASSPHONEMAG = 37;
	public static final int SENSOR_ACCELCHESTMAG = 41;
	public static final int SENSOR_ACCELCHESTX = 18;
	public static final int SENSOR_ACCELCHESTY = 19;
	public static final int SENSOR_ACCELCHESTZ = 20;
	public static final int SENSOR_RIP = 21;
	public static final int SENSOR_VIRTUAL_RR = 22;
	public static final int SENSOR_LOCATIONLATITUDE = 23;
	public static final int SENSOR_LOCATIONLONGITUDE = 24;
	public static final int SENSOR_LOCATIONSPEED = 25;
	public static final int SENSOR_BATTERY_LEVEL = 26;
	public static final int SENSOR_ALCOHOL = 27;
	public static final int SENSOR_REPLAY_GSR = 91;
	public static final int SENSOR_REPLAY_ECK = 92;
	public static final int SENSOR_REPLAY_TEMP = 93;
	public static final int SENSOR_REPLAY_RESP = 94;		//this is actually SENSOR_VIRTUAL_PEAKVALLEY
	//added by mahbub for conversation from respiration
	public static final int SENSOR_VIRTUAL_INHALATION = 95;
	public static final int SENSOR_VIRTUAL_EXHALATION = 96;
	public static final int SENSOR_VIRTUAL_IERATIO = 97;
	public static final int SENSOR_VIRTUAL_REALPEAKVALLEY = 98;
	public static final int SENSOR_VIRTUAL_STRETCH = 99;
	public static final int SENSOR_VIRTUAL_BDURATION = 90;	
	public static final int SENSOR_VIRTUAL_EXHALATION_FIRSTDIFF = 89;
	public static final int SENSOR_VIRTUAL_RESPIRATION = 88;
	public static final int SENSOR_VIRTUAL_MINUTEVENTILATION = 87;
	
	public static final int SENSOR_ZEPHYR_ECG = 41;
	public static final int SENSOR_ZEPHYR_RSP = 42;
	public static final int SENSOR_ZEPHYR_TMP = 43;
	public static final int SENSOR_ZEPHYR_ACL = 44;
	public static final int SENSOR_VIRTUAL_ECK_QUALITY = 45;
	public static final int SENSOR_VIRTUAL_RIP_QUALITY = 46;
	public static final int SENSOR_VIRTUAL_TEMP_QUALITY = 47;
	public static final int SENSOR_VIRTUAL_FIRSTDIFF_EXHALATION_NEW = 50;
	
	
	// ema constants
	public static final String quietStart = "STRESSOR_START";
	public static final String quietEnd = "STRESSOR_STOP";
	public static final String sleepStart = "EOD";
	public static final String sleepEnd = "SOD";

	
	
	private final static HashMap<Integer, String> sensorDescriptions = new HashMap<Integer, String>() {
		{
			put(SENSOR_GSR, "Galvanic Skin Response");
			put(SENSOR_ECK, "Electrocardiogram");
			put(SENSOR_BODY_TEMP, "Body Temperature");
			put(SENSOR_AMBIENT_TEMP, "Ambient Temperature");
			put(SENSOR_ACCELPHONEX, "Phone Accelerometer X value");
			put(SENSOR_ACCELPHONEY, "Phone Accelerometer Y value");
			put(SENSOR_ACCELPHONEZ, "Phone Accelerometer Z value");
			put(SENSOR_ACCELPHONEMAG, "Phone Accelerometer magnitude");
			put(SENSOR_ACCELCHESTMAG, "Chestband Accelerometer");
			put(SENSOR_ACCELCHESTX, "Chestband Accelerometer X value");
			put(SENSOR_ACCELCHESTY, "Chestband Accelerometer Y value");
			put(SENSOR_ACCELCHESTZ, "Chestband Accelerometer Z value");
			put(SENSOR_RIP, "Respiration");
			put(SENSOR_VIRTUAL_RR, "RR Intervals from Heart Rate Signal");
			put(SENSOR_REPLAY_GSR, "Galvanic Skin Response (Replay)");
			put(SENSOR_REPLAY_ECK, "Electrocardiogram (Replay)");
			put(SENSOR_REPLAY_TEMP, "Body Temperature (Replay)");
			put(SENSOR_REPLAY_RESP, "Respiration (Replay)");
			put(SENSOR_VIRTUAL_INHALATION, "Inhalation (virtual)");
			put(SENSOR_VIRTUAL_EXHALATION, "Exhalation (virtual)");
			put(SENSOR_VIRTUAL_RESPIRATION, "Respiration (virtual)");
			put(SENSOR_VIRTUAL_MINUTEVENTILATION,"Minute Ventilation(virtual)");
			put(SENSOR_VIRTUAL_IERATIO, "IEratio (virtual)");
			put(SENSOR_LOCATIONLATITUDE, "Latitude from GPS");
			put(SENSOR_LOCATIONLONGITUDE, "Longitude from GPS");
			put(SENSOR_LOCATIONSPEED, "Speed from GPS");	
			put(SENSOR_BATTERY_LEVEL, "Changes in Battery Level");
			put(SENSOR_VIRTUAL_REALPEAKVALLEY, "Peaks and Valleys (virtual)");
			put(SENSOR_VIRTUAL_ECK_QUALITY, "ECK Quality (virtual)");
			put(SENSOR_VIRTUAL_RIP_QUALITY, "RIP Quality (virtual)");
			put(SENSOR_VIRTUAL_TEMP_QUALITY, "TEMP Quality (virtual)");
			put(SENSOR_VIRTUAL_STRETCH, "Stretch (virtual)");
			put(SENSOR_VIRTUAL_BDURATION, "Duration at the bottom of each stretch (virtual)");
			put(SENSOR_VIRTUAL_EXHALATION_FIRSTDIFF, "Virtual Sensor for Calculating First Difference of Exhalation");
			put(SENSOR_VIRTUAL_FIRSTDIFF_EXHALATION_NEW, "Virtual Sensor for Calculating First Difference of Exhalation");
			put(SENSOR_ALCOHOL, "Alcohol Consumption");			
		}
	};			
	
	public static final int FEATURE_MAD = 111;
	public static final int FEATURE_MEAN = 112;
	public static final int FEATURE_RMS = 113;
	public static final int FEATURE_VAR = 114;
	public static final int FEATURE_SD = 117;
	public static final int FEATURE_MEDIAN = 118;
	public static final int FEATURE_MIN = 119;
	public static final int FEATURE_MAX = 109;	
	
	public static final int FEATURE_NULL = 110;
	public static final int FEATURE_MEANCROSS = 115;
	public static final int FEATURE_ZEROCROSS = 116;
	public static final int FEATURE_HR = 120;
	public static final int FEATURE_HR_LF = 121;
	public static final int FEATURE_HR_RSA = 122;
	public static final int FEATURE_HR_RATIO = 123;
	public static final int FEATURE_HR_MF = 124;	
	public static final int FEATURE_HR_POWER_01 = 125;	
	public static final int FEATURE_HR_POWER_12 = 126;
	public static final int FEATURE_HR_POWER_23 = 127;
	public static final int FEATURE_HR_POWER_34 = 128;	
	
	public static final int FEATURE_GSRA = 130;	
	public static final int FEATURE_GSRD = 131;
	public static final int FEATURE_SRR = 132;
	public static final int FEATURE_SRA = 133;

	public static final int FEATURE_RESP_RATE = 140;
	public static final int FEATURE_RAMP = 141;
	public static final int FEATURE_RESP_SD = 142;
	
	public static final int FEATURE_SECOND_BEST=143;
	public static final int FEATURE_NINETIETH_PERCENTILE=144;
	public static final int FEATURE_QRDEV=145;
	public static final int FEATURE_PERCENTILE=146;
	public static final int FEATURE_PERCENTILE80=147;
	
	private final static HashMap<Integer, String> featureDescriptions = new HashMap<Integer, String>() {
		{
			put(FEATURE_MAD, "Mean Adjusted Deviation");
			put(FEATURE_MEAN, "Mean");
			put(FEATURE_RMS, "Root Mean Square");
			put(FEATURE_VAR, "Variance");
			put(FEATURE_SD, "Standard Deviation");			
			put(FEATURE_MIN, "Minimum");
			put(FEATURE_MAX, "Maximum");			
			put(FEATURE_NULL, "Null");
			put(FEATURE_MEANCROSS, "Mean Crossing Rate");
			put(FEATURE_ZEROCROSS, "Zero Crossing Rate");
			put(FEATURE_HR, "Heart Rate");
			put(FEATURE_HR_LF, "EKG - Integration over the power of the LF Band");			
			put(FEATURE_HR_RSA, "EKG - Respiratory sinus arrhythmia (RSA)");
			put(FEATURE_HR_RATIO, "EKG - Ratio between sympathetic / parasympathetic influences");									
			put(FEATURE_HR_MF, "EKG - Integration over the power of the MF Band");
			put(FEATURE_HR_POWER_01, "EKG - Integration over the power of the 0.0-0.1 Band");			
			put(FEATURE_HR_POWER_12, "EKG - Integration over the power of the 0.1-0.2 Band");
			put(FEATURE_HR_POWER_23, "EKG - Integration over the power of the 0.2-0.3 Band");									
			put(FEATURE_HR_POWER_34, "EKG - Integration over the power of the 0.3-0.4 Band");									
			put(FEATURE_GSRA, "GSR - Response Area");
			put(FEATURE_GSRD, "GSR - Response Duration");
			put(FEATURE_SRR, "GSR - Rate of nonspecific skin conductance responses");
			put(FEATURE_SRA, "GSR - Skin conductance response amplitude");
			put(FEATURE_RESP_RATE, "Respiration Rate");			
			put(FEATURE_RAMP, "Respiration Amplitude");
			put(FEATURE_RESP_SD, "Respiration Standard Deviation");									
			put(FEATURE_MEDIAN, "Median");
			put(FEATURE_SECOND_BEST,"Second best value");
			put(FEATURE_NINETIETH_PERCENTILE,"90th percentile");
			put(FEATURE_QRDEV, "Quartile Deviation");
			put(FEATURE_PERCENTILE, "Q'th Percentile");
			put(FEATURE_PERCENTILE80,"80th percentile");

		}
	};		
	
	public static final int MODEL_ACTIVITY = 1;
	public static final int MODEL_STRESS = 2;
	public static final int MODEL_TEST = 0;
	public static final int MODEL_DATAQUALITY = 4;
	public static final int MODEL_CONVERSATION=5;
	public static final int MODEL_COMMUTING=6;
	public static final int MODEL_STRESS_OLD = 7;
	public static final int MODEL_ACCUMULATION = 8;

// New model added for user self report
	
	public static final int MODEL_SELF_DRINKING = 21;
	public static final int MODEL_SELF_SMOKING = 22;
	
	private final static HashMap<Integer, String> modelDescriptions = new HashMap<Integer, String>() {
		{
			put(MODEL_ACTIVITY, "Physical Activity and Posture");
			put(MODEL_STRESS_OLD, "Stress (Intensity of Negative Affect)");
			put(MODEL_TEST, "NULL");
			put(MODEL_DATAQUALITY, "Data Quality Assessments");	
			put(MODEL_CONVERSATION,"Detect Conversation Based on Respiration Signal");
			put(MODEL_COMMUTING,"Commuting");
			put(MODEL_STRESS, "Minute Classifier of Stress");
			put(MODEL_SELF_DRINKING,"User self reported drinking event recording");
			put(MODEL_SELF_SMOKING,"User self reported smoking event recording");
			put(MODEL_ACCUMULATION,"Accumulation and Decay ofs Perceived Stress");
		}
	};			
	
	public static final int ACCELEROMETER_LOCATION = 0;
	public static final String DATALOG_FILENAME = "StressInferencePhone.db";
	public static final int COMPASS_LOCATION = 1;
	
	/**
	 * Log to Database (true) or to Flat file.
	 */
	public static final boolean LOGTODB = true;
	
	// everything listed in here will NOT be logged.  Make sure to put full feature-sensor combinations.
	public final static HashSet<Integer> DATALOG_FILTER = new HashSet<Integer>() {
		{
			// add(MODEL_NULL);
			// add(Constants.getId(FEATURE_NULL, SENSOR_NULL));
			// add(SENSOR_NULL);
		}
	};
	
	
	public static boolean isSensor(int id) {
		return (id > 9 && id < 100);
	}
	
	public static boolean isFeatureSensor(int id) {
		return (id >= 10000);
	}
	
	public static boolean isModel(int id) {
		return (id <= 9);
	}
	
	/**
	 * return a unique ID for each feature-sensor configuration.
	 * This allows us to use a single feature calculation on different sensors without reinstanciating or rewritting anything 
	 * <br/> 
	 * Really it just takes the feature ID (3 digits), multiplies it by 100, and adds the Sensorid (below 99) to it
	 * 
	 * @param feature the unique ID of this feature
	 * @param sensor the unique ID of a sensor as defined in this class
	 * @return
	 */
	public static int getId(int feature, int sensor)
	{
		return feature*100+sensor;
	}
	
	
	/**
	 * Return the Feature ID from a number constructed by the above defined combined ID (see {@link #getId(int, int)});
	 * @param featureSensorID
	 * @return the feature ID
	 */
	
	public static int parseFeatureId(int featureSensorID) {
		return (int)(featureSensorID/100);
	}
	
	/**
	 * Return the Sensor ID from a number constructed by the above defined combined ID (see {@link #getId(int, int)});
	 * @param featureSensorID
	 * @return the sensor ID
	 */
	
	public static int parseSensorId(int featureSensorID) {
		return featureSensorID%100;
	}
	
	/**
	 * Return the description of the specified sensor 
	 * @param sensorID
	 * @return the description (String)
	 */	
	
	public static String getSensorDescription(int sensorID) {
		return sensorDescriptions.get(sensorID);
	}

	/**
	 * Return the description of the specified feature 
	 * @param featureID
	 * @return the description (String)
	 */	
		
	public static String getFeatureDescription(int featureID) {
		return featureDescriptions.get(featureID);
	}	

	/**
	 * Return the description of the specified model 
	 * @param modelID
	 * @return the description (String)
	 */	

	public static String getModelDescription(int modelID) {
		return modelDescriptions.get(modelID);
	}	
}
