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

//@author Patrick Blitz
//@author Andrew Raij

package org.fieldstream.service.context.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.fieldstream.Constants;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensor.ContextBus;
import org.fieldstream.service.sensor.FeatureBus;


public class TestModel extends ModelCalculation{

	public TestModel() {
		Log.d("TestModel", "Created");
	}
	
	public int getCurrentClassifier() {
		return 0;
	}

	public int getID() {
		return Constants.MODEL_TEST;
	}

	private final static ArrayList<Integer> features = new ArrayList<Integer>() { 
		{
			add(Constants.getId(Constants.FEATURE_NULL, Constants.SENSOR_BATTERY_LEVEL));
			add(Constants.getId(Constants.FEATURE_NULL, Constants.SENSOR_ACCELCHESTX));
			add(Constants.getId(Constants.FEATURE_NULL, Constants.SENSOR_ACCELCHESTY));
			add(Constants.getId(Constants.FEATURE_NULL, Constants.SENSOR_ACCELCHESTZ));		
//			add(Constants.getId(Constants.FEATURE_NULL,Constants.SENSOR_GSR));
//			add(Constants.getId(Constants.FEATURE_NULL, Constants.SENSOR_BODY_TEMP));
//			add(Constants.getId(Constants.FEATURE_NULL, Constants.SENSOR_ECK));
//			add(Constants.getId(Constants.FEATURE_NULL, Constants.SENSOR_RIP));
//			add(Constants.getId(Constants.FEATURE_HR, Constants.SENSOR_VIRTUAL_RR));
//			add(Constants.getId(Constants.FEATURE_RESP_RATE, Constants.SENSOR_VIRTUAL_INHALATION));

			
			// hack to make sure all sensor accel values are logged (even though they're not used here)
//			add(Constants.getId(Constants.FEATURE_NULL, Constants.SENSOR_ACCELCHESTX));
//			add(Constants.getId(Constants.FEATURE_NULL, Constants.SENSOR_ACCELCHESTY));
//			add(Constants.getId(Constants.FEATURE_NULL, Constants.SENSOR_LOCATIONLATITUDE));
//			add(Constants.getId(Constants.FEATURE_NULL, Constants.SENSOR_LOCATIONLONGITUDE));
//			add(Constants.getId(Constants.FEATURE_NULL, Constants.SENSOR_LOCATIONSPEED));
//			
//			add(Constants.getId(Constants.FEATURE_MEAN, Constants.SENSOR_ACCELCHESTZ));

//			add(Constants.getId(Constants.FEATURE_VAR, Constants.SENSOR_ACCELCHESTZ));
//			add(Constants.getId(Constants.FEATURE_VAR, Constants.SENSOR_TEMP));

			
//		features.add(Constants.getId(Constants.FEATURE_NULL, Constants.SENSOR_LOCATIONLATITUDE));
//		features.add(Constants.getId(Constants.FEATURE_NULL, Constants.SENSOR_LOCATIONLONGITUDE));
//		add(Constants.getId(Constants.FEATURE_NULL, Constants.SENSOR_LOCATIONSPEED));

		
		// hack to make sure all sensor accel values are logged (even though they're not used here)
		//features.add(Constants.getId(Constants.FEATURE_NULL, Constants.SENSOR_ACCELCHESTX));
		//features.add(Constants.getId(Constants.FEATURE_NULL, Constants.SENSOR_ACCELCHESTY));

		
		
//		features.add(Constants.getId( Constants.FEATURE_NULL,Constants.SENSOR_ACCELPHONEMAG));
//		features.add(Constants.getId( Constants.FEATURE_MEAN,Constants.SENSOR_REPLAY_RESP));
//		features.add(Constants.getId( Constants.FEATURE_MEAN,Constants.SENSOR_REPLAY_ECK));
//		features.add(Constants.getId( Constants.FEATURE_NULL,Constants.SENSOR_REPLAY_ECK));
//		features.add(Constants.getId( Constants.FEATURE_HR_LF,Constants.SENSOR_VIRTUAL_RR));
//		features.add(Constants.getId( Constants.FEATURE_VAR,Constants.SENSOR_REPLAY_RESP));
//		features.add(Constants.getId( Constants.FEATURE_NULL,Constants.SENSOR_REPLAY_GSR));
//		features.add(Constants.getId( Constants.FEATURE_NULL,Constants.SENSOR_REPLAY_TEMP));
//		features.add(Constants.getId( Constants.FEATURE_NULL,Constants.SENSOR_REPLAY_RESP));

//			features.add(Constants.getId( Constants.FEATURE_MEAN,Constants.SENSOR_ACCELPHONEMAG));
//			features.add(Constants.getId( Constants.FEATURE_VAR,Constants.SENSOR_ACCELPHONEMAG));		

		/*	features.add(Constants.getId( Constants.FEATURE_MEAN,Constants.SENSOR_ACCELCHESTX));
			features.add(Constants.getId( Constants.FEATURE_VAR,Constants.SENSOR_ACCELCHESTX));		
			
			features.add(Constants.getId( Constants.FEATURE_MEAN,Constants.SENSOR_ACCELCHESTY));
			features.add(Constants.getId( Constants.FEATURE_VAR,Constants.SENSOR_ACCELCHESTY));		

			features.add(Constants.getId( Constants.FEATURE_MEAN,Constants.SENSOR_ACCELCHESTZ));
			features.add(Constants.getId( Constants.FEATURE_VAR,Constants.SENSOR_ACCELCHESTZ));	*/	
						
//			features.add(Constants.getId( Constants.FEATURE_MEAN,Constants.SENSOR_ECK));
//			features.add(Constants.getId( Constants.FEATURE_VAR,Constants.SENSOR_ECK));		

			/*features.add(Constants.getId( Constants.FEATURE_MEAN,Constants.SENSOR_GSR));
			features.add(Constants.getId( Constants.FEATURE_VAR,Constants.SENSOR_GSR));		

			features.add(Constants.getId( Constants.FEATURE_MEAN,Constants.SENSOR_RIP));
			features.add(Constants.getId( Constants.FEATURE_VAR,Constants.SENSOR_RIP));		

			features.add(Constants.getId( Constants.FEATURE_HR,Constants.SENSOR_ECK));
			features.add(Constants.getId( Constants.FEATURE_HR_LF,Constants.SENSOR_ECK));		
			features.add(Constants.getId( Constants.FEATURE_HR_RSA,Constants.SENSOR_ECK));		
			features.add(Constants.getId( Constants.FEATURE_HR_RATIO,Constants.SENSOR_ECK));		*/
					
		
		
			
		}
	};
	
	public ArrayList<Integer> getUsedFeatures() {
		return features;
	}

	private final static HashMap<Integer, String> outputDescription = new HashMap<Integer, String>() {
		{
			put(0, "Test model output is meaningless");
		}
	};
	
	public HashMap<Integer, String> getOutputDescription() {
		return outputDescription;
	}

	@Override
	public void computeContext(FeatureSet fs) {
		// TODO Auto-generated method stub
		ContextBus.getInstance().pushNewContext(Constants.MODEL_TEST, 0, fs.getBeginTime(), fs.getEndTime());		
	}	
}
