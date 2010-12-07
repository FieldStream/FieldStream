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
// @author Andrew Raij

package org.fieldstream.service.context.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.fieldstream.Constants;
import org.fieldstream.service.StateManager;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensor.FeatureBus;
import org.fieldstream.service.sensor.FeatureBusSubscriber;
import org.fieldstream.service.sensor.SensorBus;

import android.os.Handler;



public abstract class ModelCalculation implements FeatureBusSubscriber{

	FeatureSetCache featureSetCache;
	long featureWaitTime;
	long lastWaitTimeCheck;
	boolean waitForAllFeatures;
	

	/**
	 * @waitForAllFeatures Whether the system should wait for all features to arrive
	 * @featureWaitTime The time the system should wait before releasing an incomplete set of features to computeContext().  
	 */	
	ModelCalculation(boolean waitForAllFeatures, long featureWaitTime) {
		featureSetCache = new FeatureSetCache();
		this.featureWaitTime = featureWaitTime;
		this.waitForAllFeatures = waitForAllFeatures;
		FeatureBus.getInstance().subscribe(this);
	}

	ModelCalculation() {
		featureSetCache = new FeatureSetCache();
		this.featureWaitTime = -1;
		this.waitForAllFeatures = true;
		FeatureBus.getInstance().subscribe(this);
	}
	
	
	/**
	 * Allows a pull of the current model value
	 * @return the current output of this model
	 */
	
	public abstract int getCurrentClassifier();
	
	/**
	 * get the Model ID (see {@link Constants}) . It just returns the matching ID, can be hard coded!
	 * (For example, the Activity Recognition Algorithm would return {@link Constants#MODEL_ACTIVITY} ) 
	 * @return the Model ID
	 */
	public abstract int getID();
	
	/**
	 * receive a new Label for online learning. not necessary for the first field study
	 * @param label TODO
	 */
	public void setLabel(float label) {
		
	}
	
	/**
	 * gives the list of features used by this Model, used by  {@link StateManager} to know which features belong to which model
	 * 
	 * Each implementation of this class should store an integer array of the used feature id's. A Feature ID is constructed from 
	 * the FeatureID, for example {@link Constants#FEATURE_MEAN} and {@link Constants#SENSOR_ACCELPHONE} would be combined using
	 * <pre>
	 * Constants.getId(Constants.FEATURE_MEAN,Constants.SENSOR_ACCELPHONE)
	 * </pre>
	 * Using this technique, we can use one calculation function (in this example, mean value) with multiple sensors. 
	 * In this example we bound it to the accelerometer in the phone. 
	 * @return an integer array of feature/Sensor IDs
	 */
	
	public abstract ArrayList<Integer> getUsedFeatures();
	
	public abstract HashMap<Integer, String> getOutputDescription();

	
	public class FeatureSet {
		long createTime;
		long beginTime;
		long endTime;

		HashMap<Integer, Double> readyFeatures;
		
		FeatureSet() {
			this.createTime = 0;
			this.beginTime = 0;
			this.endTime = 0;
			readyFeatures = new HashMap<Integer, Double>();

		}
		
		public void clear() {
			readyFeatures.clear();
			beginTime = 0;
			endTime = 0;
		}
		
		public void setBeginTime(long beginTime){
			this.beginTime = beginTime;
		}

		public void setEndTime(long endTime){
			this.endTime = endTime;
		}

		public void markCreateTime(long currentTimeMillis) {
			createTime = System.currentTimeMillis();
		}		
		
		public long getEndTime() {
			return endTime;
		}
		
		public long getBeginTime() {
			return beginTime;
		}

		public long getCreateTime() {
			return createTime;
		}
		
		public boolean complete() {
			if (Log.DEBUG) {
				String missingString="";
				ArrayList<Integer> used = getUsedFeatures();
				for (Integer i : used) {
					if (!readyFeatures.containsKey(i)) {
						missingString += i + " ";
					}
				}
				
				Log.d("isComplete",beginTime + " " + getID() + " -- " + readyFeatures.size() + "/" + getUsedFeatures().size() + " ready. Missing: " + missingString); //readyFeatures.size()==1 always
			}
			if (readyFeatures.size() == (getUsedFeatures().size()))
				return true;
			
			return false;
		}
		
		public void addOrUpdate(int featureID, double value) {				//check it
			if (getUsedFeatures().contains(featureID))
				readyFeatures.put(featureID, value);
		}
		
		public void updateTimeRange(long newBeginTime, long newEndTime) {
			if (newBeginTime < beginTime)
				beginTime = newBeginTime;
			
			if (newEndTime > endTime) 
				endTime = newEndTime;
		}
		
		float getTimeRangeOverlap(long begin, long end) {
			if (begin == 0 && end == 0)
				return 1.0f;
			
			
			float overlap = 0.0f;
			
			// just in case someone reverses the input time range
			if ( begin>end) {
				long temp = end;
				end = begin;
				begin = temp;
			}
			
//			if (end < beginTime || begin > endTime)
//				overlap = 0.0f;
//			
//			if (begin <= beginTime) {
//				if (end >= endTime) {
//					overlap = 1.0f;
//				}
//				else {
//					overlap = (end - beginTime)/(float)(endTime - beginTime);
//				}
//			}
//			else {
//				if (end >= endTime) {
//					overlap = (endTime - begin)/(float)(endTime - beginTime);
//				}
//				else {
//					overlap = (end - begin)/(float)(endTime - beginTime);				}
//				
//			}
			if((begin<beginTime && end>endTime)||(begin>beginTime && end<endTime))
				overlap=1.0f;
			else if(end<beginTime || begin>endTime)
				overlap=0.0f;
			else if(begin<beginTime && end<endTime)
				overlap=(end - beginTime)/(float)(endTime - beginTime);
			else
				overlap=(endTime - begin)/(float)(endTime - beginTime);

			if (overlap == Float.NaN) {
				overlap = 1.0f;
			}
			
			return overlap;
		}
		
		
		public boolean hasFeature(int featureID) {
			return readyFeatures.containsKey(featureID);
		}
		
		public double getFeature(int featureID) {
			if (readyFeatures.containsKey(featureID)) {
				return readyFeatures.get(featureID);
			}
			
			return 0.0f;
		}


	}
	
	private class FeatureSetCache {
		private static final float TIME_RANGE_OVERLAP = 0.75f;  // percent overlap
				
		LinkedList<FeatureSet> usedFeatureSets;
		LinkedList<FeatureSet> unusedFeatureSets;
		
		FeatureSetCache() {
			unusedFeatureSets = new LinkedList<FeatureSet>();
			usedFeatureSets = new LinkedList<FeatureSet>();
		}
		

		public FeatureSet get(long beginTime, long endTime) {
//			Log.d("getFeatureSet","beginTime= "+beginTime+" endTime= "+endTime);
			for (FeatureSet fs : usedFeatureSets) {	
				float overlap = fs.getTimeRangeOverlap(beginTime, endTime);
//				Log.d("Overlap","time overlap= "+overlap);
				if (overlap >= TIME_RANGE_OVERLAP) {
					fs.updateTimeRange(beginTime, endTime);
					return fs;
				}
			}
			
			return addNewFeatureSet(beginTime, endTime);
		}
		
		private FeatureSet addNewFeatureSet(long beginTime, long endTime) {
			FeatureSet fs = unusedFeatureSets.poll();
			if (fs == null) {
				fs = new FeatureSet();			
			}

			fs.clear();
			usedFeatureSets.addFirst(fs);
			fs.setBeginTime(beginTime);
			fs.setEndTime(endTime);
			fs.markCreateTime(System.currentTimeMillis());
			
			return fs;
		}
		
		public void invalidate(FeatureSet fs) {
			usedFeatureSets.remove(fs);
			unusedFeatureSets.add(fs);
			

		}	
		

		
	}
		
	public void receiveUpdate(int featureID, double result, long beginTime, long endTime) {
		if (getUsedFeatures().contains(featureID)) {
			// add feature to feature set
			FeatureSet fs = featureSetCache.get(beginTime, endTime);  //is it ok to create new each time????
			Log.d("receiveUpdate","modelID = " + getID() + ", " + "featureID = "+featureID);
			fs.addOrUpdate(featureID, result);			
			
			if (!this.waitForAllFeatures || fs.complete()) {
				Log.d("receiveUpdate","calling compute context");
				computeContext(fs);
				featureSetCache.invalidate(fs);
			}
		}
	}
	
	public abstract void computeContext(FeatureSet fs);
	
}
