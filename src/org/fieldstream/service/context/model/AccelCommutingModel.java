package org.fieldstream.service.context.model;

import java.util.ArrayList;
import java.util.HashMap;
import org.fieldstream.Constants;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensor.ContextBus;

public class AccelCommutingModel extends ModelCalculation{
	private static final String TAG="AccelCommutingModel";
	private static double currentMotion=0;
	private ArrayList<Integer> featureLabels;

	public AccelCommutingModel(){
		if(Log.DEBUG) Log.d(TAG,"Created"); 
		featureLabels=new ArrayList<Integer>();
		featureLabels.add(Constants.getId(Constants.FEATURE_MAX, Constants.SENSOR_VIRTUAL_ACCELCOMMUTING));
		//featureLabels.add(Constants.getId(Constants.FEATURE_MAX, Constants.SENSOR_ACCELPHONEZ));
	}
	
	protected void finalize(){}
	public int getID(){return Constants.MODEL_ACCELCOMMUTING;}
	public int getCurrentClassifier(){return (int) currentMotion;}
	public ArrayList<Integer> getUsedFeatures(){return featureLabels;}
	@Override
	public void computeContext(FeatureSet fs) {
		if(Log.DEBUG) Log.d(TAG,"Sent one decision");
		currentMotion=fs.getFeature(Constants.getId(Constants.FEATURE_MAX, Constants.SENSOR_VIRTUAL_ACCELCOMMUTING));
		ContextBus.getInstance().pushNewContext(getID(), (int) currentMotion, fs.getBeginTime(), fs.getEndTime());	
	}
//	@Override
	public HashMap<Integer, String> outputdescription = new HashMap<Integer, String>() {
		{
			put(0,"Not moving");
			put(1,"Moving");
		}
	};

	public HashMap<Integer, String> getOutputDescription() {
		return outputdescription;
	}	
}
