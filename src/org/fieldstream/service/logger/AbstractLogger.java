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
// @author Brian French
// @author Andrew Raij

package org.fieldstream.service.logger;

import org.fieldstream.service.sensor.ContextBus;
import org.fieldstream.service.sensor.ContextSubscriber;
import org.fieldstream.service.sensor.FeatureBus;
import org.fieldstream.service.sensor.FeatureBusSubscriber;
import org.fieldstream.service.sensor.SensorBus;
import org.fieldstream.service.sensor.SensorBusSubscriber;

public abstract class AbstractLogger implements SensorBusSubscriber, FeatureBusSubscriber, ContextSubscriber {
	
	protected AbstractLogger INSTANCE = null;
	
	public AbstractLogger(boolean automaticLogging) {
		if (automaticLogging) {
			SensorBus.getInstance().subscribe(this);
			FeatureBus.getInstance().subscribe(this);
			ContextBus.getInstance().subscribe(this);
		}
		
		INSTANCE = this;
	}
	
	/**
	 * Logs samples from the specified sensor to the DB.
	 * @param name The unique id of the sensor (from Constants).
	 * @param timestamps The timestamps of each sample.
	 * @param buffer The samples to log
	 * @param startNewData Starting index of new samples in the buffer
	 * @param endNewData Ending index of new samples in the buffer
	 */
	public abstract void logSensorData(int sensorID, long[] timestamps,
			int[] buffer, int startNewData, int endNewData);

	/**
	 * Logs samples from the specified sensor-feature pair to the DB.
	 * @param featureID The unique id of the sensor-feature pair (from Constants).
	 * @param timestamp The timestamp of the sample.
	 * @param value The feature value to log
	 */
	public abstract void logFeatureData(int featureID, long beginTime, long endTime,
			double value);

	/**
	 * Logs samples from the specified model to the DB.
	 * @param modelID The unique id of the model (from Constants).
	 * @param label The sample to log
	 * @param startTime The start time of the period in which this inference is valid
	 * @param endTime The end time of the period in which this inference is valid
	 */
	public abstract void logModelData(int modelID, int label, long startTime, long endTime);

	 /**
	  * Logs EMA responses and associated data
	  * @param triggerType the way the EMA was triggered (random, timed, etc.) 
	  * @param status 
	  * @param prompt timestamp of prompt appearance
	  * @param delayDuration amount of time that passed since the user delayed the EMA
	  * @param delayResponses an array of responses to questions asked after a delay
	  * @param delayResponseTimes an array of response times for questions asked after a delay
	  * @param start the time the user started the EMA
	  * @param responses an array of responses to each EMA question
	  * @param responseTimes an array of response times to each EMA question
	  */		
	public abstract void logEMA(int triggerType, String activeContexts, 
			int status, long prompt,
			long delayDuration, String[] delayResponses,
			long[] delayResponseTimes, long start, String[] responses,
			long[] responseTimes);
	
	 /**
	  * ALSO logs EMA responses and associated data TODO Remove this???
	  * @param data a string representation of EMA data
	  */		
	public abstract void logUIData(String data);

	 /**
	  * Logs incentives earned
	  * @param incentiveID id of incentive 
	  * @param timestamp time incentive earned
	  * @param comment some additional information about the incentive (e.g, what question from EMA led to this award)
	  * @param amount the amount earned for the latest incentive
	  * @param total the total amount earned so far
	  */			
	public abstract void logIncentiveEarned(int incentiveID, String comment, long timestamp, float amount, float total);

	
	 /**
	  * Logs a performance indicator
	  * @param location an int used to find the location in the code this timestamp was send out from 
	  * @param timestamp the timestamp this call was fired at 
	  * @param logString an optional log string
	  */	
	public abstract void logPerformance(int location, long timestamp, String logString) ;

			
	
	/**
	 * called to terminate the logging and flush everything out to memory, if needed (text files at least)
	 */
	public void close() {
		SensorBus.getInstance().unsubscribe(this);
		FeatureBus.getInstance().unsubscribe(this);
		ContextBus.getInstance().unsubscribe(this);		
	}
		
	/**
	 * Logs a feature result
	 * @param featureID The unique id of the feature-sensor pair (from Constants).
	 * @param result The value to log
	 */
	public void receiveUpdate(int featureID, double result, long timeBegin,
			long timeEnd) {
				logFeatureData(featureID, timeBegin, timeEnd, result);
			}

	/**
	 * Logs a model/context result
	 * @param modelID The unique id of the model (from Constants).
	 * @param label An integer label for the model
	 */
	public void receiveContext(int modelID, int label, long startTime, long endTime) {
		logModelData(modelID, label, startTime, endTime);
	}

	public abstract void logDeadPeriod(long eod, long sod);
	
	/**
	 * Logs the new values in a buffer of sensor values
	 * @param sensorID The unique id of the sensor (from Constants).
	 * @param data The buffer of new samples
	 * @param timestamps Timestamps for each sample in the buffer
	 * @param startNewData Starting index of new samples in the buffer
	 * @param endNewData Ending index of new samples in the buffer
	 */
	public void receiveBuffer(int sensorID, int[] data, long[] timestamps, int startNewData, int endNewData) {
		logSensorData(sensorID, timestamps, data, startNewData, endNewData);
	}

	public abstract double getTotalIncentivesEarned();
	
	public abstract void logResume(long timestamp);
	
	public abstract void logAnything(String name, String value, long timestamp);	
}
