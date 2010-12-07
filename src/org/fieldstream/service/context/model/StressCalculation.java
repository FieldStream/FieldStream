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
/**
 * 
 */
package org.fieldstream.service.context.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.fieldstream.Constants;
import org.fieldstream.R;
import org.fieldstream.service.InferrenceService;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensor.ContextBus;
import org.fieldstream.service.sensor.FeatureBus;


import libsvm.*;
import android.os.SystemClock;

/**
 * @author Sang Won Lee Read pre-constructed SVM model from /svmModel.txt Model
 *         is loaded & other appropriate adjustments are made in CONSTRUCTOR
 * 
 *         'receiveUpdate' will classify only if all the features arrived
 *         (temporarily 10 features) when there is any missed features, the
 *         latest value will be used to predict
 * 
 *         Classification result can be accessed from the class field,
 *         'stressLevel'
 */
public class StressCalculation extends ModelCalculation {
	private static final int MAX_RETRAIN_FEATURESETS = 10;
	private static final String STRESS_CALULATION = "StressCalulation";
	private svm_model model;
	// temporary
	private int featureNum;
	// one complete set of features
	private svm_node[] data;
	// int array is used since no initialization is required, 0=NOT yet,
	// 1=Received

	private int[] lastResults;

	private int[] featureFlag;
	private int[] baseline;
	/**
	 * minimum value used for scaling features values between 0 and 1. A
	 * received feature value equal to this value will be a 0 loaded from the
	 * scale.txt file for the current participant
	 */
	private int[] minFeatureValue;
	/**
	 * maximum value used for scaling features values between 0 and 1. A
	 * received feature value equal to this value will be a 1 loaded from the
	 * scale.txt file for the current participant
	 */
	private int[] maxFeatureValue;
	/**
	 * signals to the scale function which features should return a real value
	 * and which should return 1
	 */
	private int[] useFeature;

	// stress level
	public int stressLevel;
	private ArrayList<Integer> featureLabels;
	private int count=0;
	private ArrayList<svm_node[]> receivedFeatureSets;
	private int emaLabel=-1;
	private Boolean emaHasBeenSet=false;
	private ArrayList<Integer> retrainLables;

	public StressCalculation(String subject) {
		Log.d("StressCalculation", "Created");
		// temporary setting
		labelConstruct();
		featureNum = featureLabels.size();
		receivedFeatureSets = new ArrayList<svm_node[]>();
		data = new svm_node[featureNum];
		for (int i = 0; i < featureNum; i++) {
			data[i] = new svm_node();
		}

		lastResults = new int[25];
		count = 0;
		featureFlag = new int[featureNum];
		
		
		int resourcemodel = 0;
		int resourcebaseline = 0;
		int resourcescale = 0;
		int resourceusedfeatures = 0;
		if (subject.equalsIgnoreCase("Generic")) {
			resourcemodel = R.raw.generalmodel;
			resourcebaseline = R.raw.generalbaseline;
			resourcescale = R.raw.generalscale;
			resourceusedfeatures = R.raw.generalusedfeatures;
		} else {
			if (Log.DEBUG)
				Log.d(STRESS_CALULATION, "Error opening model for " + subject);
		}

		long uptimeMillis = SystemClock.uptimeMillis();
		// if (!b) {
		try {
			BufferedReader modelReader = new BufferedReader(
					new InputStreamReader(InferrenceService.INSTANCE
							.getResources().openRawResource(resourcemodel)));
			BufferedReader baselinereader = new BufferedReader(
					new InputStreamReader(InferrenceService.INSTANCE
							.getResources().openRawResource(resourcebaseline)));
			if (resourcescale != 0) {
				BufferedReader scalereader = new BufferedReader(
						new InputStreamReader(InferrenceService.INSTANCE
								.getResources().openRawResource(resourcescale)));
				loadscale(scalereader);
			}
			if (resourceusedfeatures != 0) {
				BufferedReader scalereader = new BufferedReader(
						new InputStreamReader(InferrenceService.INSTANCE
								.getResources().openRawResource(
										resourceusedfeatures)));
				loadusedFeatures(scalereader);

			}
			model = svm.svm_load_model(modelReader);
			loadBaseline(baselinereader);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// }

		long modelBuilding = (SystemClock.uptimeMillis()) - uptimeMillis;
		if (Log.DEBUG)
			Log.d(STRESS_CALULATION, "it took " + (modelBuilding / 1000)
					+ " seconds to load the model");

	}

	/**
	 * loads the values in to the {@link StressCalculation#usedFeaturesed}
	 * 
	 * @param scalereader
	 *            a reader for a usedfeatures.txt file
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	private void loadusedFeatures(BufferedReader scalereader)
			throws NumberFormatException, IOException {
		String line;
		useFeature = new int[featureNum];
		int i = 0;
		while ((line = scalereader.readLine()) != null) {
			if (i < featureNum) {
				useFeature[i] = Integer.parseInt(line);
			}
			i++;
		}
	}

	private void loadscale(BufferedReader scalereader)
			throws NumberFormatException, IOException {
		String line;
		minFeatureValue = new int[featureNum];
		maxFeatureValue = new int[featureNum];
		int i = 0;
		while ((line = scalereader.readLine()) != null) {
			if (i < featureNum) {
				String[] split = line.split(" ");
				minFeatureValue[i] = Integer.parseInt(split[0]);
				maxFeatureValue[i] = Integer.parseInt(split[1]);
			}
			i++;
		}

	}

	private void loadBaseline(BufferedReader baselinereader)
			throws NumberFormatException, IOException {
		String line;
		baseline = new int[featureNum];
		int i = 0;
		while ((line = baselinereader.readLine()) != null) {
			if (i < featureNum)
				baseline[i] = Integer.parseInt(line);
			i++;
		}
	}

	private void labelConstruct() {
		featureLabels = new ArrayList<Integer>();
		// HR Sensor
		featureLabels.add(Constants.getId(Constants.FEATURE_MEAN,
				Constants.SENSOR_VIRTUAL_RR));
		featureLabels.add(Constants.getId(Constants.FEATURE_SD,
				Constants.SENSOR_VIRTUAL_RR));
		featureLabels.add(Constants.getId(Constants.FEATURE_VAR,
				Constants.SENSOR_VIRTUAL_RR));
		// ECG Sensor
		featureLabels.add(Constants.getId(Constants.FEATURE_HR_RSA,
				Constants.SENSOR_VIRTUAL_RR)); // RSA
		featureLabels.add(Constants.getId(Constants.FEATURE_HR_MF,
				Constants.SENSOR_VIRTUAL_RR)); // MF
		featureLabels.add(Constants.getId(Constants.FEATURE_HR_LF,
				Constants.SENSOR_VIRTUAL_RR)); // LF
		featureLabels.add(Constants.getId(Constants.FEATURE_HR_POWER_01,
				Constants.SENSOR_VIRTUAL_RR)); // HZ01
		featureLabels.add(Constants.getId(Constants.FEATURE_HR_POWER_12,
				Constants.SENSOR_VIRTUAL_RR)); // HZ12
		featureLabels.add(Constants.getId(Constants.FEATURE_HR_POWER_23,
				Constants.SENSOR_VIRTUAL_RR)); // HZ23
		featureLabels.add(Constants.getId(Constants.FEATURE_HR_POWER_34,
				Constants.SENSOR_VIRTUAL_RR)); // HZ34
		// featureLabels.add(Constants.getId(Constants.FEATURE_HR_RATIO,
		// Constants.SENSOR_VIRTUAL_RR));
		// featureLabels.add(Constants.getId(Constants.FEATURE_NULL,
		// Constants.SENSOR_VIRTUAL_RR)); // HFMFL

		// featureLabels.add(Constants.getId(Constants.FEATURE_HR_RSA,
		// Constants.SENSOR_REPLAY_ECK));
		// featureLabels.add(Constants.getId(Constants.FEATURE_HR_MF,
		// Constants.SENSOR_REPLAY_ECK)); // MF
		// featureLabels.add(Constants.getId(Constants.FEATURE_HR_LF,
		// Constants.SENSOR_REPLAY_ECK)); //LF
		// featureLabels.add(Constants.getId(Constants.FEATURE_HR_POWER_01,
		// Constants.SENSOR_REPLAY_ECK)); // HZ01
		// featureLabels.add(Constants.getId(Constants.FEATURE_HR_POWER_12,
		// Constants.SENSOR_REPLAY_ECK)); // HZ12
		// featureLabels.add(Constants.getId(Constants.FEATURE_HR_POWER_23,
		// Constants.SENSOR_REPLAY_ECK)); // HR23
		// featureLabels.add(Constants.getId(Constants.FEATURE_HR_POWER_34,
		// Constants.SENSOR_REPLAY_ECK)); // HR34
		// featureLabels.add(Constants.getId(Constants.FEATURE_HR_RATIO,
		// Constants.SENSOR_REPLAY_ECK));
		// featureLabels.add(Constants.getId(Constants.FEATURE_NULL,
		// Constants.SENSOR_REPLAY_ECK)); // HFMFL

		// GSR Sensor
		featureLabels.add(Constants.getId(Constants.FEATURE_MEAN,
				Constants.SENSOR_GSR));
		featureLabels.add(Constants.getId(Constants.FEATURE_SD,
				Constants.SENSOR_GSR));
		featureLabels.add(Constants.getId(Constants.FEATURE_VAR,
				Constants.SENSOR_GSR));
		featureLabels.add(Constants.getId(Constants.FEATURE_MAD,
				Constants.SENSOR_GSR));
		// featureLabels.add(Constants.getId(Constants.FEATURE_SRR,
		// Constants.SENSOR_GSR));
		// featureLabels.add(Constants.getId(Constants.FEATURE_SRA,
		// Constants.SENSOR_GSR));
		// featureLabels.add(Constants.getId(Constants.FEATURE_GSRA,
		// Constants.SENSOR_GSR));
		// featureLabels.add(Constants.getId(Constants.FEATURE_GSRD,
		// Constants.SENSOR_GSR));
		//		
//		 featureLabels.add(Constants.getId(Constants.FEATURE_MEAN,
//		 Constants.SENSOR_REPLAY_GSR));
//		 featureLabels.add(Constants.getId(Constants.FEATURE_VAR,
//		 Constants.SENSOR_REPLAY_GSR));
//		 featureLabels.add(Constants.getId(Constants.FEATURE_SD,
//		 Constants.SENSOR_REPLAY_GSR));
//		 featureLabels.add(Constants.getId(Constants.FEATURE_MAD,
//		 Constants.SENSOR_REPLAY_GSR));
//		 featureLabels.add(Constants.getId(Constants.FEATURE_SRR,
//		 Constants.SENSOR_REPLAY_GSR));
//		 featureLabels.add(Constants.getId(Constants.FEATURE_SRA,
//		 Constants.SENSOR_REPLAY_GSR));
//		 featureLabels.add(Constants.getId(Constants.FEATURE_GSRA,
//		 Constants.SENSOR_REPLAY_GSR));
//		 featureLabels.add(Constants.getId(Constants.FEATURE_GSRD,
//		 Constants.SENSOR_REPLAY_GSR));

		// Temperature Sensor
//		featureLabels.add(Constants.getId(Constants.FEATURE_MEAN,
//				Constants.SENSOR_TEMP));
//		featureLabels.add(Constants.getId(Constants.FEATURE_SD,
//				Constants.SENSOR_TEMP));
//		featureLabels.add(Constants.getId(Constants.FEATURE_VAR,
//				Constants.SENSOR_TEMP));
//		featureLabels.add(Constants.getId(Constants.FEATURE_MAD,
//				Constants.SENSOR_TEMP));

//		 featureLabels.add(Constants.getId(Constants.FEATURE_MEAN,
//		 Constants.SENSOR_REPLAY_TEMP));
//		 featureLabels.add(Constants.getId(Constants.FEATURE_VAR,
//		 Constants.SENSOR_REPLAY_TEMP));
//		 featureLabels.add(Constants.getId(Constants.FEATURE_SD,
//		 Constants.SENSOR_REPLAY_TEMP));
//		 featureLabels.add(Constants.getId(Constants.FEATURE_MAD,
//		 Constants.SENSOR_REPLAY_TEMP));

		// Respiration Sensor
		// featureLabels.add(Constants.getId(Constants.FEATURE_RESP_RATE,
		// Constants.SENSOR_RIP)); // RR
		// featureLabels.add(Constants.getId(Constants.FEATURE_RESP_SD,
		// Constants.SENSOR_RIP)); // RPMED
		// featureLabels.add(Constants.getId(Constants.FEATURE_RAMP,
		// Constants.SENSOR_RIP)); // RAMP
		//		
		// featureLabels.add(Constants.getId(Constants.FEATURE_RESP_RATE,
		// Constants.SENSOR_REPLAY_RESP)); // RR
		// featureLabels.add(Constants.getId(Constants.FEATURE_RESP_SD,
		// Constants.SENSOR_REPLAY_RESP)); // RPM
		// featureLabels.add(Constants.getId(Constants.FEATURE_RAMP,
		// Constants.SENSOR_REPLAY_RESP)); // RAMP

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeedu.cmu.ices.stress.phone.service.context.model.ModelCalculation#
	 * getCurrentClassifier()
	 */
	public int getCurrentClassifier() {
		return stressLevel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.cmu.ices.stress.phone.service.context.model.ModelCalculation#getID()
	 */
	public int getID() {
		return Constants.MODEL_STRESS_OLD;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.cmu.ices.stress.phone.service.context.model.ModelCalculation#setLabel
	 * ()
	 */
	public synchronized void setLabel(float label) {
   		
		//when stress level is same as label (should retrieve ema result in real app)
		emaLabel = (int)label;
		emaHasBeenSet=true;
		Log.d(STRESS_CALULATION,"Received new EMA label: "+label);
			
	}
	
	private void retrain(int newLabel) {
		// get the ones we need. For this, we iterate over the retrainLables list
		ArrayList<String> list = new ArrayList<String>();
		for (int i=0;i<retrainLables.size();i++) {
			if (retrainLables.get(i)!=newLabel && receivedFeatureSets.size()>i) {
				
				svm_node[] reTrainNodes = receivedFeatureSets.get(i);
				StringBuffer newLine = new StringBuffer();
				newLine.append(emaLabel);
				newLine.append(" ");
				for (int j = 0; j < reTrainNodes.length; j++) {
					svm_node svmNode = reTrainNodes[j];
					newLine.append(svmNode.index);
					newLine.append(":");
					newLine.append(svmNode.value);
					newLine.append(" ");
				}
				list.add(newLine.toString());
			}
		}
		//buffersize indicates when to retrain the model
		//if buffersize is 10, every 10 wrong classification, model will be retrained with 10 new vectors
			svm_train_mod.SVlist = new ArrayList<String>();
			try {
				/**DUBUG CODE**/
				long timeTrainS = System.currentTimeMillis(); // stop timin
				
				//retrain model, first argument is to tell svm that it's retrainng mode,
				//second argument is list of new vectors, each vector is in the following format
				//label		features
				//2 		1:0.48596122 2:0.78856732 3:0.69265633 4:0.56234436 5:0.24639338 6:0.37203857 7:0.38492230
				svm_train_mod.setModel(model);
				svm_train_mod.go(true, list.toArray(new String[list.size()]));
				
				/**DUBUG CODE**/
				long timeTrainE = System.currentTimeMillis(); // stop timin
				Log.i(STRESS_CALULATION, "Retraining on "+list.size()+" took "+ (timeTrainE-timeTrainS)+"ms");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//retrieve retrained model
			model=svm_train_mod.getModel();
			
			// this should save the model!
			
	}

	
	/**
	 * receives new feature values, {@link StressCalculation#scale(double, int)}
	 * them and orders them into {@link StressCalculation#data}. once all
	 * features have been received once, the current stress level is predicted
	 * 
	 * 
	 * {@see edu.cmu.ices.stress.phone.service.sensors.api.FeatureBusSubscriber#
	 * receiveUpdate(int, float)}
	 */
	@Override
	public void computeContext(FeatureSet fs) {
		// get values from the listener
		Log.d("StressCalculation","in compute context");

		int index = 0;
		for (int featureID : featureLabels) {
			
			double value = fs.getFeature(featureID);
 
			// svm_node takes index from 1 through 10, not 0 to 9 (need to be
			// verified though)
			data[index].index = index + 1;
			data[index].value = scale(value, index);
			index++;
		}
		
		
//		if (testCounter==15) {
//			testCounter=0;
//			emaLabel=2;
//			emaHasBeenSet=true;
//		} else 	if (testCounter>0) {
//			testCounter++;
//		} else if (testCounter==0) {
//			testCounter = 1;
//		} 
		
		predictLabel();
		ContextBus.getInstance().pushNewContext(getID(), stressLevel, fs.getBeginTime(), fs.getEndTime());

	}

	private void predictLabel() {
		// push classification result to a higher layer
		stressLevel = (int) svm.svm_predict(model, data);
		
		// add to the set of received values
		if (receivedFeatureSets.size()>=MAX_RETRAIN_FEATURESETS) {
			receivedFeatureSets.remove(0);
		}
		
		receivedFeatureSets.add(data);
		if (emaHasBeenSet) {
			if (Log.DEBUG) {
				Log.d(STRESS_CALULATION, "Have received EMA, colleceting data for retraining "+count);
			}
			// new EMA label has been retrieved!
			if (count==0) {
				count=1;
				retrainLables = new ArrayList<Integer>();
				ArrayList<Integer> previousContexts = ContextBus.getInstance().getPreviousContexts(this.getID());
				if (previousContexts!=null) {
					retrainLables.addAll(ContextBus.getInstance().getPreviousContexts(this.getID()));
				}
			} 
			retrainLables.add(stressLevel);
			count++;
			if (count>5) {
				count=0;
				retrain(emaLabel);
				emaHasBeenSet=false;
			}
		}
	}

	/**
	 * scales and normalized the given value based on it's index in the
	 * featureLabels array
	 * 
	 * @param value
	 * @param index
	 * @return
	 */
	// TODO: better way to get the baseline and the min/max features!
	private double scale(double value, int index) {
		// Log.d("scale","Scaleing feature "+index+ " with value "+
		// value+" with baseline "+ baseline[index]+ ", minimum" +
		// minFeatureValue[index] + " and maximum "+maxFeatureValue[index]);
		if (useFeature[index] == 1) {
			return (value - baseline[index] - minFeatureValue[index])
					/ (maxFeatureValue[index] - minFeatureValue[index]);
		} else {
			return 1;
			// TODO: FIXME
		}
	}

	public ArrayList<Integer> getUsedFeatures() {
		return featureLabels;
	}


	private final static HashMap<Integer, String> outputDescription = new HashMap<Integer, String>() {
		{
			put(1, "Not Stressed");
			put(2, "Stressed");
			// put(3, "Unknown/Timed");
		}
	};

	public HashMap<Integer, String> getOutputDescription() {
		return outputDescription;
	}

}
