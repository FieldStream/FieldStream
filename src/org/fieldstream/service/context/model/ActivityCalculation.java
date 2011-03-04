//Copyright (c) 2010, Carnegie Mellon University
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
//    * Neither the name of Carnegie Mellon University nor the names of its contributors may be used to 
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
import java.util.Arrays;
import java.util.HashMap;

import org.fieldstream.Constants;
import org.fieldstream.service.ActivationManager;
import org.fieldstream.service.context.model.ModelCalculation.FeatureSet;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensor.ContextBus;
import org.fieldstream.service.sensor.FeatureBus;




/**
 * 
 * @author Scott Fisk
 *
 */
public class ActivityCalculation extends ModelCalculation {
	
	// set of features
	private double[] features;

	//number of features
	private int featureNum;
	
	private int[] featureFlag;
	//activity level
	private int activityClassification;

	private ArrayList<Integer> featureLabels;
	
	
	public ActivityCalculation() {
		
		// number of features in this model. 
		featureNum = 2;
		
		// features in the model.
		featureLabels = new ArrayList<Integer>(featureNum);
		featureLabels.add(Constants.getId(Constants.FEATURE_MEANCROSS, Constants.SENSOR_ACCELCHESTZ));
		featureLabels.add(Constants.getId(Constants.FEATURE_MAD, Constants.SENSOR_ACCELCHESTZ));
		featureFlag = new int[featureNum];
		features = new double[featureNum];		
	}		

	@Override
	public void computeContext(FeatureSet fs) {

		for (int featureID : featureLabels) {	
			double result = fs.getFeature(featureID);		
		
			//get values from the listener
			int index = featureLabels.indexOf(featureID);
			
			// save features
			features[index] = result;
		}		

		//push classification result to a higher layer
		activityClassification = doDecisionTree();
		ContextBus.getInstance().pushNewContext(getID(),activityClassification, fs.getBeginTime(), fs.getEndTime());
		if (Log.DEBUG) Log.d("ActivityClassification","New Classification: "+((Integer)activityClassification).toString());
		//initialize all the flags to 0
		Arrays.fill(featureFlag, 0);
	}
	
	private boolean isFullFeatureSet(){
		int i = 0;
		for(i = 0; i < featureNum; i++){
			// check for empties
			if(featureFlag[i] == 0){
				return false;
			}
		}
		// if we get here then there were no empties
		return true;
	}
	
	/**
	 * Returns the most recent activity classification... This is a public variable can be accessed directly. 
	 */
	public int getCurrentClassifier() {
		return activityClassification;
	}

	/**
	 * @see org.fieldstream.service.context.model.ModelCalculation#getID()
	 */
	public int getID() {
		return Constants.MODEL_ACTIVITY;
	}

	public ArrayList<Integer> getUsedFeatures() {
		return featureLabels;
	}
	
	private int doDecisionTree() {
		float[] DTentry = new float [4];
		
		int next = 0;   // root node

		do {
			DTentry = ActivityModel.dtChest[-next]; // get next node
			next  = (int) ((features[(int) DTentry[0]] <= DTentry[1]) ? DTentry[2] :DTentry[3]);
		} while (next<0); // while next is a node

		return next; 
	}
	public final static int SIT = 0;
	public final static int STAND = 1;
	public final static int WALK = 2;
	
	private final static HashMap<Integer, String> outputDescription = new HashMap<Integer, String>() {
		{
			put(SIT, "Sit");
			put(STAND, "Stand");
			put(WALK, "Walk");
		}
	};
	
	public HashMap<Integer, String> getOutputDescription() {
		return outputDescription;
	}
	
}


