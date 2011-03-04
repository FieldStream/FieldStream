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

//@author Mahbub Rahman
//@author Amin Ahsan Ali
package org.fieldstream.service.context.model;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.fieldstream.Constants;
import org.fieldstream.service.ActivationManager;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensor.ContextBus;
import org.fieldstream.service.sensor.virtual.IEratioCalculation;



public class ConversationDetectionModel extends ModelCalculation{

	
	private ArrayList<Integer> featureLabels;
	private int featureNum;
	private int[] featureFlag;
	// set of features
	private double[] features;
	private int conversationClassification;
	public int getCurrentClassifier() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getID() {
		
		return Constants.MODEL_CONVERSATION;
	}
	
	public ArrayList<Integer> getUsedFeatures() {
		//Log.d("getUsedFeature","getUsedFeature");
		featureLabels = new ArrayList<Integer>();
		featureLabels.add(Constants.getId(Constants.FEATURE_NINETIETH_PERCENTILE, Constants.SENSOR_VIRTUAL_INHALATION));		//is it from virtual sensor or what???//1
		featureLabels.add(Constants.getId(Constants.FEATURE_SD, Constants.SENSOR_VIRTUAL_INHALATION));												  //1
		
		//featureLabels.add(Constants.getId(Constants.FEATURE_MEDIAN, Constants.SENSOR_VIRTUAL_INHALATION)); //11  [2][1]								//1
		featureLabels.add(Constants.getId(Constants.FEATURE_MEAN, Constants.SENSOR_VIRTUAL_EXHALATION));//10     [3][2]	//1
		//featureLabels.add(Constants.getId(Constants.FEATURE_SD, Constants.SENSOR_VIRTUAL_EXHALATION));					//1
		//featureLabels.add(Constants.getId(Constants.FEATURE_MEDIAN, Constants.SENSOR_VIRTUAL_EXHALATION)); //3   [5][3]	//1
		featureLabels.add(Constants.getId(Constants.FEATURE_MEAN, Constants.SENSOR_VIRTUAL_IERATIO)); //5		 [6][4]	//1
		//featureLabels.add(Constants.getId(Constants.FEATURE_SD, Constants.SENSOR_VIRTUAL_IERATIO));						//1
		featureLabels.add(Constants.getId(Constants.FEATURE_MEDIAN, Constants.SENSOR_VIRTUAL_IERATIO)); //0      [8][5]	//1
		//featureLabels.add(Constants.getId(Constants.FEATURE_SECOND_BEST,Constants.SENSOR_VIRTUAL_STRETCH));//1   [9][6]	//1
		//featureLabels.add(Constants.getId(Constants.FEATURE_MEDIAN,Constants.SENSOR_VIRTUAL_STRETCH)); //2       [10][7]//1
		featureLabels.add(Constants.getId(Constants.FEATURE_MEAN,Constants.SENSOR_VIRTUAL_BDURATION)); //4	 [11][8]//1
		featureLabels.add(Constants.getId(Constants.FEATURE_SECOND_BEST,Constants.SENSOR_VIRTUAL_BDURATION));
		featureLabels.add(Constants.getId(Constants.FEATURE_SD,Constants.SENSOR_VIRTUAL_STRETCH));
		
//		featureLabels.add(Constants.getId(Constants.FEATURE_MEAN,Constants.SENSOR_VIRTUAL_FIRSTDIFF_EXHALATION_NEW));//7   [12][9]
//		featureLabels.add(Constants.getId(Constants.FEATURE_SECOND_BEST,Constants.SENSOR_VIRTUAL_INHALATION)); //6	   [13][10]//1
//		featureLabels.add(Constants.getId(Constants.FEATURE_MEDIAN,Constants.SENSOR_VIRTUAL_FIRSTDIFF_EXHALATION_NEW));//8 [14][11]
//		featureLabels.add(Constants.getId(Constants.FEATURE_SECOND_BEST,Constants.SENSOR_VIRTUAL_EXHALATION));//9	   [15][12]//1
		
		featureLabels.add(Constants.getId(Constants.FEATURE_NULL, Constants.SENSOR_RIP)); //to activate the RIP sensor
		//featureLabels.add(Constants.getId(Constants.FEATURE_NULL, Constants.SENSOR_VIRTUAL_REALPEAKVALLEY));

		//featureLabels.add(Constants.getId(Constants.FEATURE_MAX, Constants.SENSOR_ACCELPHONEZ));
		
		featureNum=featureLabels.size();																//actually it will be the count of total used features
		
		featureFlag = new int[featureNum];
		features = new double[featureNum];
		
		return featureLabels;
	}

	
	
	@Override
	public void computeContext(FeatureSet fs) {
		
		//Log.d("computeContext","in compute context");
		Log.d("ConversationDetection","in compute context");
		
		
		for (int featureID : featureLabels) {			
			double result = fs.getFeature(featureID);

			//get values from the listener
			int index = featureLabels.indexOf(featureID);
			
			// save features
			features[index] = result;

			Log.d("ConversationDetectionModel", "Feature ID = "+featureID+" value= "+result+" Begin= "+fs.getBeginTime()+" End="+fs.getEndTime());
		}
		
		//first remove the rounding effect of IEratio so that it will be again a floating point. divide by 10000 for mean, median and standard deviation

		features[3]=features[3]/IEratioCalculation.roundingMultiplier;
		features[4]=features[4]/IEratioCalculation.roundingMultiplier;
		//features[8]=features[8]/IEratioCalculation.roundingMultiplier;
		
		//push classification result to a higher layer
		conversationClassification = predictionFromDT();
		ContextBus.getInstance().pushNewContext(getID(),conversationClassification, fs.getBeginTime(), fs.getEndTime());
		if (Log.DEBUG) Log.d("ConersationClassification","New Classification: "+((Integer)conversationClassification).toString());
		//initialize all the flags to 0
		Arrays.fill(featureFlag, 0);

		//need to be varified from Dr Raij
		ActivationManager.getInstance().updateFeatureList(getID(), getUsedFeatures());  

	}
	
	public int predictionFromDT()
	{
		ConversationPrediction conv=new ConversationPrediction();
		//int label=conv.getLabel(features[0], features[1], features[2], features[3], features[4], features[5], features[6], features[7], features[8]);
		//int label=conv.getLabel_20100511(features[0], features[2],features[4],features[8]);
		int label=conv.getLablelSpeakingSmokingSilentFromArrayVersion(features[0], features[1],features[2],features[3],features[4],features[5],features[6],features[7]);
		return label;
	}
	
	private final static HashMap<Integer, String> outputDescription = new HashMap<Integer, String>() {
		{
			put(ConversationPrediction.QUIET, "Quiet");
			put(ConversationPrediction.SMOKING, "Smoking");
			put(ConversationPrediction.SPEAKING, "Speaking");
		}
	};
	
	public HashMap<Integer, String> getOutputDescription() {
		return outputDescription;
	}


}
