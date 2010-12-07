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

package org.fieldstream.service.context.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.fieldstream.Constants;
import org.fieldstream.FixBandProblemActivity;
import org.fieldstream.service.InferrenceService;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensor.ContextBus;
import org.fieldstream.service.sensor.FeatureBus;

//import android.content.Intent;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;

//import edu.cmu.ices.stress.phone.FixBandProblemActivity;
//import edu.cmu.ices.stress.phone.service.InferrenceService;

/**
 * @author Andrew Raij and Kurt Plarre
 * 
 * Infers if there is a problem with the chestband or respiration band
 */
public class DataQualityCalculation extends ModelCalculation{

	public final static int DATA_QUALITY_GOOD = 0;
	public final static int DATA_QUALITY_NOISE = 1;    
	public final static int DATA_QUALITY_SENSOR_LOOSE = 2;
	public final static int DATA_QUALITY_SENSOR_OFF = 3;
	private static final String TAG = "DataQualityCalculation";
	private static int currentDataQuality=DATA_QUALITY_GOOD;
	Intent fixBandIntent;
	
	public DataQualityCalculation() {
		Log.d(TAG, "Created");
		fixBandIntent = new Intent(InferrenceService.INSTANCE.getBaseContext(), FixBandProblemActivity.class);
		fixBandIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		constructFeatureLabels();
		currentDataQuality=DATA_QUALITY_GOOD+10*DATA_QUALITY_GOOD;
	}
	
	protected void finalize() {
		fixBandIntent = null;
	}
	
	private ArrayList<Integer> featureLabels;
	
	public int getID(){
		return Constants.MODEL_DATAQUALITY;
	}

	public int getCurrentClassifier(){
		return currentDataQuality;
	}


	public ArrayList<Integer> getUsedFeatures(){
		return featureLabels;
	}


	private void constructFeatureLabels() {
		featureLabels = new ArrayList<Integer>();
		//featureLabels.add(Constants.getId(Constants.FEATURE_MEAN, Constants.SENSOR_ECK));
		featureLabels.add(Constants.getId(Constants.FEATURE_MAX, Constants.SENSOR_VIRTUAL_RIP_QUALITY));
		featureLabels.add(Constants.getId(Constants.FEATURE_MAX, Constants.SENSOR_VIRTUAL_ECK_QUALITY));
		featureLabels.add(Constants.getId(Constants.FEATURE_MAX, Constants.SENSOR_VIRTUAL_TEMP_QUALITY));
	}
	
	/*
	 * Update the data quality
	 */
	private void updateDataQuality() {
	}
	
	// ===================================================================================================
	/* (non-Javadoc)
	 * @see edu.cmu.ices.stress.phone.service.sensor.FeatureBusSubscriber#receiveUpdate(int, double, long, long)
	 */
	@Override
	public void computeContext(FeatureSet fs) {
		if (Log.DEBUG) Log.d("DataQualityCalculation", "Received data");		
		//if (Log.DEBUG) Log.d("DataQualityCalculation", "ECK mean "+fs.getFeature(Constants.getId(Constants.FEATURE_MEAN, Constants.SENSOR_ECK)));		
		//if (Log.DEBUG) Log.d("DataQualityCalculation", "Rip Quality "+fs.getFeature(Constants.getId(Constants.FEATURE_MAX, Constants.SENSOR_VIRTUAL_RIP_QUALITY)));		
		if (Log.DEBUG) Log.d("DataQualityCalculation", "Temp Quality "+fs.getFeature(Constants.getId(Constants.FEATURE_MAX, Constants.SENSOR_VIRTUAL_TEMP_QUALITY)));		
		//if (Log.DEBUG) Log.d("DataQualityCalculation", "Eck Quality "+fs.getFeature(Constants.getId(Constants.FEATURE_MAX, Constants.SENSOR_VIRTUAL_ECK_QUALITY)));		
	 
		//		ContextBus.getInstance().pushNewContext(getID(), currentDataQuality, fs.getBeginTime(), fs.getEndTime());	
//		startFixBandActivityIfNeeded(currentDataQuality);
	}
	
	// ===================================================================================================

	/*
	 * 
	 */
	private final static HashMap<Integer, String> outputDescription = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 1L;
		{
			put(DATA_QUALITY_GOOD + 10 * DATA_QUALITY_GOOD,"ECG and RIP Band Good");
			put(DATA_QUALITY_NOISE + 10 * DATA_QUALITY_GOOD,"ECG Band Data Noisy and RIP Band Good");
			put(DATA_QUALITY_SENSOR_LOOSE + 10 * DATA_QUALITY_GOOD,"ECG Band Loose and RIP Band Good");
			put(DATA_QUALITY_SENSOR_OFF + 10 * DATA_QUALITY_GOOD,"ECG Band Off and RIP Band Good");

			put(DATA_QUALITY_GOOD + 10 * DATA_QUALITY_NOISE,"ECG Band Good and RIP Data Noisy");
			put(DATA_QUALITY_NOISE + 10 * DATA_QUALITY_NOISE,"ECG Band Data Noisy and RIP Data Noisy");
			put(DATA_QUALITY_SENSOR_LOOSE + 10 * DATA_QUALITY_NOISE,"ECG Band Loose and RIP Data Noisy");
			put(DATA_QUALITY_SENSOR_OFF + 10 * DATA_QUALITY_NOISE,"ECG Band Off and RIP Data Noisy");

			put(DATA_QUALITY_GOOD + 10 * DATA_QUALITY_SENSOR_LOOSE,"ECG and RIP Band Loose");
			put(DATA_QUALITY_NOISE + 10 * DATA_QUALITY_SENSOR_LOOSE,"ECG Band Data Noisy and RIP Band Loose");
			put(DATA_QUALITY_SENSOR_LOOSE + 10 * DATA_QUALITY_SENSOR_LOOSE,"ECG Band Loose and RIP Band Loose");
			put(DATA_QUALITY_SENSOR_OFF + 10 * DATA_QUALITY_SENSOR_LOOSE,"ECG Band Off and RIP Band Loose");

			put(DATA_QUALITY_GOOD + 10 * DATA_QUALITY_SENSOR_OFF,"ECG and RIP Band Off");
			put(DATA_QUALITY_NOISE + 10 * DATA_QUALITY_SENSOR_OFF,"ECG Band Data Noisy and RIP Band Off");
			put(DATA_QUALITY_SENSOR_LOOSE + 10 * DATA_QUALITY_SENSOR_OFF,"ECG Band Loose and RIP Band Off");
			put(DATA_QUALITY_SENSOR_OFF + 10 * DATA_QUALITY_SENSOR_OFF,"ECG Band Off and RIP Band Off");
		}
	};
	
	public HashMap<Integer, String> getOutputDescription() {
		return outputDescription;
	}	

	
	private static final long FIXBAND_TIME_BEFORE_NEXT = 1800000; 
	private long lastTimeFixBandTriggered = System.currentTimeMillis() - (long)(0.9 * FIXBAND_TIME_BEFORE_NEXT);
	
	// START THE DATA QUALITY ACTIVITY
	void startFixBandActivityIfNeeded(int value) {
		long now = System.currentTimeMillis();
		
		if (lastTimeFixBandTriggered + FIXBAND_TIME_BEFORE_NEXT > now ) {
			return;
		}
		
		int eckDataQuality = value % 10;
		int ripDataQuality = value / 10;

		// is there a problem
		if (eckDataQuality == DataQualityCalculation.DATA_QUALITY_SENSOR_LOOSE
				|| eckDataQuality == DataQualityCalculation.DATA_QUALITY_SENSOR_OFF
				|| ripDataQuality == DataQualityCalculation.DATA_QUALITY_SENSOR_LOOSE
				|| ripDataQuality == DataQualityCalculation.DATA_QUALITY_SENSOR_OFF) {
			
			// there is a problem, let's make sure no other important activity is running right now
			
			ActivityManager mgr = (ActivityManager) InferrenceService.INSTANCE.getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningTaskInfo> info = mgr.getRunningTasks(1);
			String currentTask = info.get(0).topActivity.getShortClassName();
			Boolean mainRunning = currentTask.equals(".MainStudyActivity");
			Boolean fixBandRunningAlready = currentTask.equals(".FixBandProblemActivity");
			
			if (mainRunning && !fixBandRunningAlready) {
				
				lastTimeFixBandTriggered = System.currentTimeMillis();
				
				fixBandIntent.putExtra(
								"edu.cmu.ices.stress.phone.FixBandProblemActivity.currentDataQuality",
								value);
				InferrenceService.INSTANCE.startActivity(fixBandIntent);
			}
		}
	}
}
