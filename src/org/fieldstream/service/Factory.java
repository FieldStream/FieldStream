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


package org.fieldstream.service;

import java.security.InvalidParameterException;

import org.fieldstream.Constants;
import org.fieldstream.service.context.model.AccelCommutingModel;
import org.fieldstream.service.context.model.AccumulationModel;
import org.fieldstream.service.context.model.ActivityCalculation;
import org.fieldstream.service.context.model.CommutingCalculation;
import org.fieldstream.service.context.model.ConversationDetectionModel;
import org.fieldstream.service.context.model.DataQualityCalculation;
import org.fieldstream.service.context.model.ModelCalculation;
import org.fieldstream.service.context.model.SelfDrinkingModel;
import org.fieldstream.service.context.model.SelfSmokingModel;
import org.fieldstream.service.context.model.StressCalculation;
import org.fieldstream.service.context.model.StressDetectionModel;
import org.fieldstream.service.context.model.TestModel;
import org.fieldstream.service.features.GSRA;
import org.fieldstream.service.features.GSRD;
import org.fieldstream.service.features.HeartRate;
import org.fieldstream.service.features.HeartRateLF;
import org.fieldstream.service.features.HeartRateMF;
import org.fieldstream.service.features.HeartRatePower01;
import org.fieldstream.service.features.HeartRatePower12;
import org.fieldstream.service.features.HeartRatePower23;
import org.fieldstream.service.features.HeartRatePower34;
import org.fieldstream.service.features.HeartRateRSA;
import org.fieldstream.service.features.HeartRateRatio;
import org.fieldstream.service.features.Mad;
import org.fieldstream.service.features.Max;
import org.fieldstream.service.features.Mean;
import org.fieldstream.service.features.Median;
import org.fieldstream.service.features.Min;
import org.fieldstream.service.features.NinetiethPercentile;
import org.fieldstream.service.features.NullFeature;
import org.fieldstream.service.features.Percentile80;
import org.fieldstream.service.features.QuartileDeviation;
import org.fieldstream.service.features.RespirationAMP;
import org.fieldstream.service.features.RespirationMED;
import org.fieldstream.service.features.RespirationRate;
import org.fieldstream.service.features.Rms;
import org.fieldstream.service.features.SD;
import org.fieldstream.service.features.SRA;
import org.fieldstream.service.features.SRR;
import org.fieldstream.service.features.SecondBest;
import org.fieldstream.service.features.Variance;
import org.fieldstream.service.features.meanCrossings;
import org.fieldstream.service.features.zeroCrossings;
import org.fieldstream.service.sensor.replay.TestSensor;
import org.fieldstream.service.sensor.virtual.AccelCommutingVirtualSensor;
import org.fieldstream.service.sensor.virtual.BdurationVirtualSensor;
import org.fieldstream.service.sensor.virtual.EckQualityVirtualSensor;
import org.fieldstream.service.sensor.virtual.ExhalationFirstDiffVirtualSensor;
import org.fieldstream.service.sensor.virtual.ExhalationFirstDiffVirtualSensorNew;
import org.fieldstream.service.sensor.virtual.ExhalationVirtualSensorNew;
import org.fieldstream.service.sensor.virtual.IEratioVirtualSensorNew;
import org.fieldstream.service.sensor.virtual.InhalationVirtualSensorNew;
import org.fieldstream.service.sensor.virtual.MinuteVentilationVirtualSensor;
import org.fieldstream.service.sensor.virtual.RRIntervalVirtualSensor;
import org.fieldstream.service.sensor.virtual.RealPeakValleyVirtualSensor;
import org.fieldstream.service.sensor.virtual.RespirationVirtualSensor;
import org.fieldstream.service.sensor.virtual.RipQualityVirtualSensor;
import org.fieldstream.service.sensor.virtual.StretchVirtualSensor;
import org.fieldstream.service.sensor.virtual.TempQualityVirtualSensor;
import org.fieldstream.service.sensors.api.AbstractFeature;
import org.fieldstream.service.sensors.api.AbstractSensor;
import org.fieldstream.service.sensors.mote.GenericMoteSensor;
import org.fieldstream.service.sensors.phone.AccelerometerSensor;
import org.fieldstream.service.sensors.phone.BatteryLevelSensor;
import org.fieldstream.service.sensors.phone.CompassSensor;
import org.fieldstream.service.sensors.phone.LocationSensor;



//import edu.cmu.ices.stress.phone.service.sensor.virtual.ExhalationVirtualSensor;

//import edu.cmu.ices.stress.phone.service.sensor.virtual.IEratioVirtualSensor;
//import edu.cmu.ices.stress.phone.service.sensor.virtual.InhalationVirtualSensor;

public class Factory {
	
	/**
	 * Factory to create Features, has a hardcoded switch from feature constants (see {@link Constants}) to feature classes
	 * Each Feature class must extend {@link AbstractFeature} 
	 * @param featureID
	 * @return
	 */

	static AbstractFeature featureFactory(int featureID) {
		AbstractFeature feature = null;
		switch (featureID) {
		case Constants.FEATURE_MEAN:
			feature = new Mean(featureID, false, 0);
			break;
		case Constants.FEATURE_MAD:
			feature = new Mad(featureID, false, 0);
			break;
		case Constants.FEATURE_RMS:
			feature = new Rms(featureID, false, 0);
			break;
		case Constants.FEATURE_VAR:
			feature = new Variance(featureID, false, 0);
			break;
		case Constants.FEATURE_MEDIAN:
			feature = new Median(featureID, false, 0);
			break;
		case Constants.FEATURE_NULL:
			feature = new NullFeature(featureID);
			break;
		case Constants.FEATURE_MEANCROSS:
			feature = new meanCrossings(featureID);
			break;
		case Constants.FEATURE_ZEROCROSS:
			feature = new zeroCrossings(featureID);
			break;
		case Constants.FEATURE_HR:
			feature = new HeartRate(featureID, false, 0);
			break;
		case Constants.FEATURE_HR_LF:
			feature = new HeartRateLF(featureID, false, 0);
			break;
		case Constants.FEATURE_HR_RSA:
			feature = new HeartRateRSA(featureID, false, 0);
			break;
		case Constants.FEATURE_HR_RATIO:
			feature = new HeartRateRatio(featureID, false, 0);
			break;
		case Constants.FEATURE_HR_MF:
			feature = new HeartRateMF(featureID, false, 0);
			break;
		case Constants.FEATURE_HR_POWER_01:
			feature = new HeartRatePower01(featureID, false, 0);
			break;
		case Constants.FEATURE_HR_POWER_12:
			feature = new HeartRatePower12(featureID, false, 0);
			break;
		case Constants.FEATURE_HR_POWER_23:
			feature = new HeartRatePower23(featureID, false, 0);
			break;			
		case Constants.FEATURE_HR_POWER_34:
			feature = new HeartRatePower34(featureID, false, 0);
			break;						
		case Constants.FEATURE_GSRA:
			feature = new GSRA(featureID,false,0);
			break;
		case Constants.FEATURE_GSRD:
			feature = new GSRD(featureID,false,0);
			break;
		case Constants.FEATURE_SRA:
			feature = new SRA(featureID,false,0);
			break;
		case Constants.FEATURE_SRR:
			feature = new SRR(featureID,false,0);
			break;
		case Constants.FEATURE_SD:
			feature = new SD(featureID, false, 0);
			break;
		case Constants.FEATURE_RESP_RATE:
			feature = new RespirationRate(featureID, false,0);
			break;
		case Constants.FEATURE_RAMP:
			feature = new RespirationAMP(featureID, false,0);
			break;
		case Constants.FEATURE_RESP_SD:
			feature = new RespirationMED(featureID, false,0);
			break;
		case Constants.FEATURE_MAX:
			feature = new Max(featureID, false,0);
			break;
		case Constants.FEATURE_MIN:
			feature = new Min(featureID, false,0);
			break;
		case Constants.FEATURE_SECOND_BEST:
			feature = new SecondBest(featureID, false,0);
			break;
		case Constants.FEATURE_NINETIETH_PERCENTILE:
			feature = new NinetiethPercentile(featureID, false, 0);
			break;
		case Constants.FEATURE_PERCENTILE80:
			feature = new Percentile80(featureID, false, 0);
			break;
		case Constants.FEATURE_QRDEV:
			feature = new QuartileDeviation(featureID, false, 0);
			break;

		default:
			throw new InvalidParameterException("Wrong/Non-Existing Feature ID: " + featureID );
		}
		return feature;
	}
	
	
	/**
	 * load a model specified by it's ID
	 * @param modelID
	 * @return
	 */
	static ModelCalculation modelFactory(int modelID) {
		ModelCalculation model = null;
		switch (modelID) {
		case Constants.MODEL_ACTIVITY:
			model = new ActivityCalculation();
			break;
		case Constants.MODEL_TEST:
			model = new TestModel();
			break;
		case Constants.MODEL_STRESS_OLD:
			model = new StressCalculation(Constants.SUBJECT);
			break;
		case Constants.MODEL_STRESS:
			model= new StressDetectionModel();
			break;
		case Constants.MODEL_CONVERSATION:
			model=new ConversationDetectionModel();
			break;
		case Constants.MODEL_DATAQUALITY:
			model = new DataQualityCalculation();
			break;
		case Constants.MODEL_COMMUTING:
			model = new CommutingCalculation();
			break;
		// added drinking and smoking models
		case Constants.MODEL_SELF_DRINKING:
			model = new SelfDrinkingModel();
			break;
		case Constants.MODEL_SELF_SMOKING:
			model = new SelfSmokingModel();
			break;
		case Constants.MODEL_ACCUMULATION:
			model = new AccumulationModel();
			break;
		case Constants.MODEL_ACCELCOMMUTING:
			model = new AccelCommutingModel();
			break;

		default:
			throw new InvalidParameterException("Wrong/Non-Existing Feature ID");
		}
		return model;
	}	
	
	
	/**
	 * load a Sensors identified by it SensorID. Uses a Case statement for this
	 * @param sensorID
	 * @return
	 */
	static AbstractSensor sensorFactory(int sensorID) {
		AbstractSensor sensor = null;
		switch (sensorID) {
		case Constants.SENSOR_ACCELPHONEMAG:
			sensor = new AccelerometerSensor(sensorID);
			break;
		case Constants.SENSOR_ACCELPHONEX:
			sensor = new AccelerometerSensor(sensorID);
			break;
		case Constants.SENSOR_ACCELPHONEY:
			sensor = new AccelerometerSensor(sensorID);
			break;
		case Constants.SENSOR_ACCELPHONEZ:
			sensor = new AccelerometerSensor(sensorID);
			break;
		case Constants.SENSOR_COMPASSPHONEMAG:
			sensor = new CompassSensor(sensorID);
			break;
		case Constants.SENSOR_COMPASSPHONEX:
			sensor = new CompassSensor(sensorID);
			break;
		case Constants.SENSOR_COMPASSPHONEY:
			sensor = new CompassSensor(sensorID);
			break;
		case Constants.SENSOR_COMPASSPHONEZ:
			sensor = new CompassSensor(sensorID);
			break;
		case Constants.SENSOR_ACCELCHESTX:
		case Constants.SENSOR_ACCELCHESTY:
		case Constants.SENSOR_ACCELCHESTZ:
		case Constants.SENSOR_ECK:
		case Constants.SENSOR_RIP:
		case Constants.SENSOR_BODY_TEMP:
		case Constants.SENSOR_AMBIENT_TEMP:
		case Constants.SENSOR_GSR:
		case Constants.SENSOR_ALCOHOL:			
			sensor = new GenericMoteSensor(sensorID);
			break;
		case Constants.SENSOR_REPLAY_RESP:
			sensor = new TestSensor(sensorID);
			break;
		case Constants.SENSOR_VIRTUAL_INHALATION:						//mahbub
			sensor = new InhalationVirtualSensorNew(sensorID);
			break;
		case Constants.SENSOR_VIRTUAL_EXHALATION:						//.....
			sensor = new ExhalationVirtualSensorNew(sensorID);
			break;
		case Constants.SENSOR_VIRTUAL_RESPIRATION:						//.....
			sensor = new RespirationVirtualSensor(sensorID);
			break;
		case Constants.SENSOR_VIRTUAL_MINUTEVENTILATION:						//.....
			sensor = new MinuteVentilationVirtualSensor(sensorID);
			break;

		case Constants.SENSOR_VIRTUAL_IERATIO:							//.....
			sensor = new IEratioVirtualSensorNew(sensorID);
			break;
		case Constants.SENSOR_VIRTUAL_REALPEAKVALLEY:					//mahbub
			sensor = new RealPeakValleyVirtualSensor(sensorID);
			break;
		case Constants.SENSOR_VIRTUAL_STRETCH:					//mahbub
			sensor = new StretchVirtualSensor(sensorID);
			break;
		case Constants.SENSOR_VIRTUAL_BDURATION:					//mahbub
			sensor = new BdurationVirtualSensor(sensorID);
			break;
		case Constants.SENSOR_VIRTUAL_EXHALATION_FIRSTDIFF:					//mahbub
			sensor = new ExhalationFirstDiffVirtualSensor(sensorID);
			break;
		case Constants.SENSOR_VIRTUAL_FIRSTDIFF_EXHALATION_NEW:					//mahbub
			sensor = new ExhalationFirstDiffVirtualSensorNew(sensorID);
			break;	
		case Constants.SENSOR_REPLAY_ECK:
			sensor = new TestSensor(sensorID);
			break;
		case Constants.SENSOR_REPLAY_GSR:
			sensor = new TestSensor(sensorID);
			break;
		case Constants.SENSOR_REPLAY_TEMP:
			sensor = new TestSensor(sensorID);
			break;
		case Constants.SENSOR_VIRTUAL_RR:
			sensor = new RRIntervalVirtualSensor(sensorID);
			break;
		case Constants.SENSOR_VIRTUAL_ECK_QUALITY:
			sensor = new EckQualityVirtualSensor(sensorID);
			break;
		case Constants.SENSOR_VIRTUAL_RIP_QUALITY:
			sensor = new RipQualityVirtualSensor(sensorID);
			break;
		case Constants.SENSOR_VIRTUAL_TEMP_QUALITY:
			sensor = new TempQualityVirtualSensor(sensorID);
			break;
		case Constants.SENSOR_LOCATIONLATITUDE:
		case Constants.SENSOR_LOCATIONLONGITUDE:
		case Constants.SENSOR_LOCATIONSPEED:
			sensor = new LocationSensor(sensorID);
			break;
		case Constants.SENSOR_BATTERY_LEVEL:
			sensor = new BatteryLevelSensor(sensorID);
			break;
		case Constants.SENSOR_VIRTUAL_ACCELCOMMUTING:
			sensor = new AccelCommutingVirtualSensor(sensorID);
			break;
		default:
			throw new InvalidParameterException("Wrong/Non-Existing Sensor ID");
		}
		return sensor;
	}	
}
