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

//@author Monowar Hossain

package org.fieldstream.service.context.model;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.fieldstream.Constants;
import org.fieldstream.service.ActivationManager;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensor.ContextBus;



public class StressDetectionModel extends ModelCalculation{
	private ArrayList<Integer> featureLabels;
	private int featureNum;
	private int[] featureFlag;
	// set of features
	private double[] features;
	private int stressClassification;

	public final static int NOTSTRESS = 1;
	public final static int STRESS = 2;
	static double maxFValue[]={ 4.7368    ,4.3080    ,5.3359    ,5.4816    ,5.9184    ,5.1250    ,4.0462    ,3.9805    ,4.3502    ,2.2025    ,2.6288    ,5.0921,2.7805};
	//static double maxFValue[]={0.857616870318324,	1,	0.773481233564947,	0.822607190334903,	
		//0.883397034082781,	0.966259721762748,	0.934228196085541,	0.849586209123200,
		//1,	0.680225607450200,	0.699561339352478,	1,	0.726831871873207};
	static double minFValue[]={-1.8694   ,-3.1003   ,-1.4935   ,-1.6805   ,-1.7398   ,-2.0703   ,-1.1872   ,-1.3573   ,-1.7656   ,-3.8432   ,-4.0433   ,-1.9772,  -4.1381};
	//static double minFValue[]={0.0115318849072266,	0,	0.0653520525011718,	0.0227653533647542,	0,	0,	0.00922018415997005,	0,	0.0560910901024818,	0,	0,	0,	0};
	static double meanFeature[]=new double [13];
	static double stdFeature[]=new double [13];
	int normNo=0;
	static float minFeature[]=new float[13];

	public int getCurrentClassifier() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getID() {
		
		return Constants.MODEL_STRESS;
	}
	
	public ArrayList<Integer> getUsedFeatures() {
		//Log.d("getUsedFeature","getUsedFeature");
		Log.d("Monowar","(M) getUsedFeatures -> start");
		featureLabels = new ArrayList<Integer>();
/*		MVVL=(f(1)-Min(1))/(Max(1)-Min(1));
		INMN=(f(2)-Min(2))/(Max(2)-Min(2));
		EXQD=(f(3)-Min(3))/(Max(3)-Min(3));
		RPQD=(f(4)-Min(4))/(Max(4)-Min(4));
		IRMD=(f(5)-Min(5))/(Max(5)-Min(5));
		STMD=(f(6)-Min(6))/(Max(6)-Min(6));
		STQD=(f(7)-Min(7))/(Max(7)-Min(7));
		ST80=(f(8)-Min(8))/(Max(8)-Min(8));
		HRP1=(f(9)-Min(9))/(Max(9)-Min(9));
		RRMN=(f(10)-Min(10))/(Max(10)-Min(10));
		RRMD=(f(11)-Min(11))/(Max(11)-Min(11));
		RRQD=(f(12)-Min(12))/(Max(12)-Min(12));
		RR80=(f(13)-Min(13))/(Max(13)-Min(13));
*/		
		// 0. Minute Ventilation
		featureLabels.add(Constants.getId(Constants.FEATURE_MEAN, Constants.SENSOR_VIRTUAL_MINUTEVENTILATION));
		// 1. Inspiration Duration (Mean)
		featureLabels.add(Constants.getId(Constants.FEATURE_MEAN, Constants.SENSOR_VIRTUAL_INHALATION));
		// 2. Expiration Duration (Quartile Deviation)
		featureLabels.add(Constants.getId(Constants.FEATURE_QRDEV, Constants.SENSOR_VIRTUAL_EXHALATION));
		// 3. Respiration Duration (Quardile Deviation)
		featureLabels.add(Constants.getId(Constants.FEATURE_QRDEV, Constants.SENSOR_VIRTUAL_RESPIRATION));
		// 4. IE Ratio (Median)
		featureLabels.add(Constants.getId(Constants.FEATURE_MEDIAN, Constants.SENSOR_VIRTUAL_IERATIO)); //5		 [6][4]	//1
		// 5. Stretch (Median)
		featureLabels.add(Constants.getId(Constants.FEATURE_MEDIAN,Constants.SENSOR_VIRTUAL_STRETCH));		
		// 6. Stretch (Quartile Deviation)
		featureLabels.add(Constants.getId(Constants.FEATURE_QRDEV,Constants.SENSOR_VIRTUAL_STRETCH));
		// 7. Stretch (80th Percentile)
		featureLabels.add(Constants.getId(Constants.FEATURE_PERCENTILE80,Constants.SENSOR_VIRTUAL_STRETCH));
		// 8. High Frequency Power1
		featureLabels.add(Constants.getId(Constants.FEATURE_HR_POWER_12,Constants.SENSOR_VIRTUAL_RR));		
		// 9. RR Interval (Mean)
		featureLabels.add(Constants.getId(Constants.FEATURE_MEAN,Constants.SENSOR_VIRTUAL_RR));
		// 10. RR Interval (Median)
		featureLabels.add(Constants.getId(Constants.FEATURE_MEDIAN,Constants.SENSOR_VIRTUAL_RR));
		
		// 11. RR Interval (Quantile Deviation)
		featureLabels.add(Constants.getId(Constants.FEATURE_QRDEV,Constants.SENSOR_VIRTUAL_RR));
		
		// 12. RR Interval (80th Percentile)
		featureLabels.add(Constants.getId(Constants.FEATURE_PERCENTILE80,Constants.SENSOR_VIRTUAL_RR));
		
/*		featureLabels.add(Constants.getId(Constants.FEATURE_NINETIETH_PERCENTILE, Constants.SENSOR_VIRTUAL_INHALATION));		//is it from virtual sensor or what???//1
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
		
		//featureLabels.add(Constants.getId(Constants.FEATURE_NULL, Constants.SENSOR_RIP));
		//featureLabels.add(Constants.getId(Constants.FEATURE_NULL, Constants.SENSOR_VIRTUAL_REALPEAKVALLEY));
*/		
		featureNum=featureLabels.size();																//actually it will be the count of total used features
		
		featureFlag = new int[featureNum];
		features = new double[featureNum];
		Log.d("Monowar","(M) getUsedFeatures -> end");		
		return featureLabels;
	}

	
	
	@Override
	public void computeContext(FeatureSet fs) {
		
		Log.d("Monowar","(M) compute context->start");

		for (int featureID : featureLabels) {			
			double result = fs.getFeature(featureID);

			//get values from the listener
			int index = featureLabels.indexOf(featureID);
			
			// save features
			features[index] = result;

			Log.d("Monowar", "(M) Computer Features: Feature ID = "+featureID+" value= "+result+" Begin= "+fs.getBeginTime()+" End="+fs.getEndTime());
		}
		
		//first remove the rounding effect of IEratio so that it will be again a floating point. divide by 10000 for mean, median and standard deviation
		features[0]=features[0]/10000;
		features[3]=features[3]/10000;
		
		features[4]=features[4]/10000;
//		features[4]=features[4]/IEratioCalculation.roundingMultiplier;
		//features[8]=features[8]/IEratioCalculation.roundingMultiplier;
		
		//push classification result to a higher layer
		stressClassification = predictionFromSVM();
		ContextBus.getInstance().pushNewContext(getID(),stressClassification, fs.getBeginTime(), fs.getEndTime());
		if (Log.DEBUG) Log.d("ConersationClassification","New Classification: "+((Integer)stressClassification).toString());
		//initialize all the flags to 0
		Arrays.fill(featureFlag, 0);
	
	}
	
	public int predictionFromSVM()
	{
		//ConversationPrediction conv=new ConversationPrediction();
		//int label=conv.getLabel(features[0], features[1], features[2], features[3], features[4], features[5], features[6], features[7], features[8]);
		//int label=conv.getLabel_20100511(features[0], features[2],features[4],features[8]);
		int label=getStressPrediction_SVM(features);
		return label;
	}
	public int getStressPrediction_SVM(double features[])
	{
		double v=0;
		int i;
		Log.d("Monowar","(M) SVM Starts");
		double coeff[]={2.3156,-2.7972,0.8428,0.6391,-0.7193,1.294,0.0966,0.2632,2.2966, -2.0124, -1.4619,-1.0124,-2.31,2.6698};
		//double coeff[]={0.8352,-0.8799,0.6578,0.1394,0.0466,0.7883,-0.0802,-0.2704,
		//		0.6172,0.1733,0.5563,-0.1336,-1.8749,-0.5235};
		normNo++;
		for(i=0;i<13;i++)
		{
			if(normNo==1){
				meanFeature[i]=features[i];
				stdFeature[i]=0;
				features[i]=0;
				
			}
			else {
			meanFeature[i]=(meanFeature[i]*(normNo-1)+features[i])/normNo;
			stdFeature[i]=stdFeature[i]+(meanFeature[i]-features[i])*(meanFeature[i]-features[i])*normNo/(normNo-1);
			
			features[i]=(features[i]-meanFeature[i])/Math.sqrt(stdFeature[i]/(normNo-1));
			}
			}
		for (i=0;i<13;i++){
			//if(features[i]>maxFValue[i]) maxFValue[i]=features[i];
			//if(features[i]<minFValue[i]) minFValue[i]=features[i];
			v=v+coeff[i]*(features[i]-minFValue[i])/(maxFValue[i]-minFValue[i]);
		}
		v+=coeff[13];
		String f="Stress Features:";
		for(i=0;i<13;i++){
			f=f+"       "+i+"=> "+features[i];
		}
		Log.d("(M) ",f);
		Log.d("Monowar","(M) SVM Ends ");
		if(v>0)
			return STRESS;
		else return NOTSTRESS;
	}
	
	private final static HashMap<Integer, String> outputDescription = new HashMap<Integer, String>() {
		{
			put(StressDetectionModel.NOTSTRESS,"Not Stress");
			put(StressDetectionModel.STRESS,"Stress");
			//put(ConversationPrediction.QUIET, "Quiet");
			//put(ConversationPrediction.SMOKING, "Smoking");
			//put(ConversationPrediction.SPEAKING, "Speaking");
		}
	};
	
	public HashMap<Integer, String> getOutputDescription() {
		return outputDescription;
	}
}
