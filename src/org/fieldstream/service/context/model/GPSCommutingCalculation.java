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

/**
 * 
 */
package org.fieldstream.service.context.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.fieldstream.Constants;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensor.ContextBus;
import org.fieldstream.service.sensors.phone.LocatioN;


/**
 * @author Andrew Raij
 *
 */
public class GPSCommutingCalculation extends ModelCalculation {

	int lastClassification = -1;
	int feature1 = Constants.getId(Constants.FEATURE_MEDIAN, Constants.SENSOR_LOCATIONSPEED);
	int feature2 = Constants.getId(Constants.FEATURE_NULL, Constants.SENSOR_LOCATIONLATITUDE);
	int feature3 = Constants.getId(Constants.FEATURE_NULL, Constants.SENSOR_LOCATIONLONGITUDE);
	int feature4 = Constants.getId(Constants.FEATURE_NULL, Constants.SENSOR_COMPASSPHONEX);
	int feature5 = Constants.getId(Constants.FEATURE_NULL, Constants.SENSOR_COMPASSPHONEY);
	int feature6 = Constants.getId(Constants.FEATURE_NULL, Constants.SENSOR_COMPASSPHONEZ);
	

	public final static int NOT_COMMUTING = 0;
	public final static int COMMUTING = 1;
	
	public static float COMMUTING_THRESHOLD = 2.2352f;  // 2.2352 meters/second == 5 mph

	
	
	/* (non-Javadoc)
	 * @see edu.cmu.ices.stress.phone.service.context.model.ModelCalculation#getCurrentClassifier()
	 */
	public int getCurrentClassifier() {		
		return lastClassification;
	}

	/* (non-Javadoc)
	 * @see edu.cmu.ices.stress.phone.service.context.model.ModelCalculation#getID()
	 */
	public int getID() {
		return Constants.MODEL_GPSCOMMUTING;
	}
	
	private final static HashMap<Integer, String> outputDescription = new HashMap<Integer, String>() {
		{
			put(NOT_COMMUTING, "Not Commuting");
			put(COMMUTING, "Commuting");
		}
	};
	
	/* (non-Javadoc)
	 * @see edu.cmu.ices.stress.phone.service.context.model.ModelCalculation#getOutputDescription()
	 */
	public HashMap<Integer, String> getOutputDescription() {
		return outputDescription;
	}

	/* (non-Javadoc)
	 * @see edu.cmu.ices.stress.phone.service.context.model.ModelCalculation#getUsedFeatures()
	 */
	public ArrayList<Integer> getUsedFeatures() {
		ArrayList<Integer> array = new ArrayList<Integer>();
		array.add(feature1);
		array.add(feature2);
		array.add(feature3);
//		array.add(feature4);
//		array.add(feature5);
//		array.add(feature6);
		return array;
	}


	/* (non-Javadoc)
	 * @see edu.cmu.ices.stress.phone.service.sensor.FeatureBusSubscriber#receiveUpdate(int, double, long, long)
	 */
	@Override
	public void computeContext(FeatureSet fs) {
		Log.d("CommutingCalculation","in compute context");
			int context = NOT_COMMUTING;
			
			double f = fs.getFeature(feature1);
			if (f != Double.NaN) {
				if (f/LocatioN.SPEED_MULTIPLIER > COMMUTING_THRESHOLD) {
					context = COMMUTING;
				}
			}
			
			lastClassification = context;
			ContextBus.getInstance().pushNewContext(this.getID(), context, fs.getBeginTime(), fs.getEndTime());
	}
}
