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
package org.fieldstream.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.fieldstream.Constants;
import org.fieldstream.service.context.model.ActivityCalculation;
import org.fieldstream.service.context.model.ModelCalculation;
import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensor.ContextBus;
import org.fieldstream.service.sensor.ContextSubscriber;
import org.fieldstream.service.sensors.api.AbstractFeature;
import org.fieldstream.service.sensors.api.AbstractSensor;
import org.fieldstream.service.sensors.mote.bluetooth.BluetoothStateManager;
import org.fieldstream.service.sensors.mote.bluetooth.BluetoothStateSubscriber;


/**
 * This Class is the currently combined Feature and Context Manager, managing all context models and their associated features
 * 
 * Makes extensive use of the Constants class to identify sensor/feature pairs that are needed. 
 * All Features needed by a model are split up in the corresponding feature calculation and the sensor. 
 * Depending on this, all necessary features are than started and stopped as needed 
 * 
 * @author Patrick Blitz
 * @author Andrew Raij
 *
 */

public class StateManager implements ContextSubscriber {
/**
 * List that contains all available sensors
 */
	private DatabaseLogger db;
	
	public static HashMap<Integer,AbstractSensor> sensors;
	public static HashMap<Integer,AbstractFeature> features;
	public static HashMap<Integer, ArrayList<Integer>> modelToSFMapping;
	/*
	 * all available and loaded context modules, aka models
	 */
	public static HashMap<Integer,ModelCalculation>  models;
	/**
	 * the map from sensor IDs to Feature Objects needed by the FeatureCalcualtion class to quickly lookup which features need to be calculated on which sensor data
	 */
	public static HashMap<Integer,ArrayList<Integer>> sensorFeature;
/**
 * list of Sensor/Feature Combinations currently in place
 */
	public static ArrayList<Integer> SFlist;
	private FeatureCalculation featureCalculation;
	
	static private StateManager INSTANCE = null;
	
	public static StateManager getInstance()
	{
		if(INSTANCE == null)
		{
			INSTANCE = new StateManager();
		}
		return INSTANCE;
	}	
	
	private StateManager() {
		models = new HashMap<Integer, ModelCalculation>();
		modelToSFMapping = new HashMap<Integer, ArrayList<Integer>>();
		SFlist = new ArrayList<Integer>();
		sensors = new HashMap<Integer, AbstractSensor>();
		sensorFeature = new HashMap<Integer, ArrayList<Integer>>();
		features = new HashMap<Integer, AbstractFeature>();
		featureCalculation = FeatureCalculation.getInstance();
		
//		ContextBus.getInstance().subscribe(this);		
		db = DatabaseLogger.getInstance(this);
	}
	

	private void addFeatures(ArrayList<Integer> newFeatures) {
		if (newFeatures!= null && !newFeatures.isEmpty()) {
			int i = 0;
			HashMap<Integer, ArrayList<AbstractFeature>> newSensorFeature = FeatureCalculation.mapping;
			for (; i< newFeatures.size(); i++) {

				int sensor = Constants.parseSensorId(newFeatures.get(i));
				int feature = Constants.parseFeatureId(newFeatures.get(i));
				if (!features.keySet().contains(feature)) {
					features.put(feature,Factory.featureFactory(feature));
					features.get(feature).active=true;
				}
				if (!sensors.containsKey(sensor)) {
					sensors.put(sensor, Factory.sensorFactory(sensor));
					sensors.get(sensor).activate();
				}
				// now all needed features and sensors are there, just need to construct the "mapping" array
				if (sensorFeature.containsKey(sensor)) {
					sensorFeature.get(sensor).add(feature); // even if this thing was already there, it would just get added
				} else {
					ArrayList<Integer> tmp = new ArrayList<Integer>();
					tmp.add(feature);
					sensorFeature.put(sensor, tmp);	
				}
				// add all new feature/sensor combinations to the mapping list for the feature Calculation
				if (newSensorFeature.containsKey(sensor)) {
					if (!newSensorFeature.get(sensor).contains(features.get(feature))) {
						newSensorFeature.get(sensor).add(features.get(feature));
					}
				} else {
					ArrayList<AbstractFeature> tmp = new ArrayList<AbstractFeature>();
					tmp.add(features.get(feature));
					newSensorFeature.put(sensor, tmp);
				}
			}
			featureCalculation.setMap(newSensorFeature);
		}
	}
//	/**
//	 * adds a single new feature to the necessary arrays
//	 * @param newFeature a constants feature sensor id
//	 */
//	private void addFeatures(Integer newFeature) {
//		if (newFeature!= null) {
//			HashMap<Integer, ArrayList<AbstractFeature>> newSensorFeature = FeatureCalculation.mapping;
//			
//
//				int sensor = Constants.parseSensorId(newFeature);
//				int feature = Constants.parseFeatureId(newFeature);
//				if (!features.keySet().contains(feature)) {
//					features.put(feature,Factory.featureFactory(feature));
//					features.get(feature).active=true;
//				}
//				if (!sensors.containsKey(sensor)) {
//					sensors.put(sensor, Factory.sensorFactory(sensor));
//					sensors.get(sensor).activate();
//				}
//				// now all needed features and sensors are there, just need to construct the "mapping" array
//				if (sensorFeature.containsKey(sensor)) {
//					sensorFeature.get(sensor).add(feature); // even if this thing was already there, it would just get added
//				} else {
//					ArrayList<Integer> tmp = new ArrayList<Integer>();
//					tmp.add(feature);
//					sensorFeature.put(sensor, tmp);	
//				}
//				// add all new feature/sensor combinations to the mapping list for the feature Calculation
//				if (newSensorFeature.containsKey(sensor)) {
//					if (!newSensorFeature.get(sensor).contains(features.get(feature))) {
//						newSensorFeature.get(sensor).add(features.get(feature));
//					}
//				} else {
//					ArrayList<AbstractFeature> tmp = new ArrayList<AbstractFeature>();
//					tmp.add(features.get(feature));
//					newSensorFeature.put(sensor, tmp);
//				}
//			
//			featureCalculation.setMap(newSensorFeature);
//		}
//	}

	/**
	 * 
	 * @param model
	 * @return
	 */
	public ArrayList<Integer> updateFeatureList(int model, ArrayList<Integer> newFeatures) {
		ArrayList<Integer> newSF = new ArrayList<Integer>();
		modelToSFMapping.put(model, newFeatures);
		for (int i = 0;i<newFeatures.size();i++) {
			if (!SFlist.contains(newFeatures.get(i))){ 
				SFlist.add(newFeatures.get(i));
				newSF.add(newFeatures.get(i));
			}
		}
		if (!newSF.isEmpty()) {
			addFeatures(newSF);
			return newSF;
		} 
		return null;
	}
	
	/**
	 * activate a specific model. Automatically starts/activates all needed sensors and features
	 * @param modelID the IntegerID of a Model as defined in {@link Constants}
	 */
	public void activate(int modelID) {
		if (!models.containsKey(modelID)) { 
			db.logAnything("activation", "activate model"+modelID, System.currentTimeMillis());
			models.put(modelID, Factory.modelFactory(modelID));
			
			// load sensors for this model
			ArrayList<Integer> newFeatures = updateFeatureList(modelID, models.get(modelID).getUsedFeatures());
			
			Log.i("StateManager","loading Model "+((Integer)modelID).toString());
		}
	}

	/**
	 * deactivate a specific model. All Features and sensors that are only needed by this model will be turned off/deactivated
	 * @param modelID the IntegerID of a Model as defined in {@link Constants}
	 */
	public void deactivate(int modelID) {
//		ArrayList<Integer> modelsToRemove = new ArrayList<Integer>();
		
		if (models.containsKey(modelID)) {
			db.logAnything("activation", "deactivate model"+modelID, System.currentTimeMillis());
			// get list of SF to deactivte
			ArrayList<Integer> otherSF  = new ArrayList<Integer>();
			ArrayList<Integer> tosStop = new ArrayList<Integer>();
			for (Integer model : modelToSFMapping.keySet()) {
				if (model!=modelID) {
					otherSF.addAll(modelToSFMapping.get(model));
				}
			}
			for (Integer model : modelToSFMapping.get(modelID)) {
				if (!otherSF.contains(model)) {
					//deactivate this SF
					tosStop.add(model);
				}
			}
			if (!tosStop.isEmpty()) {
				unloadFeatures(tosStop);
			}
			models.remove(modelID);
		}
		if (Log.DEBUG) Log.d("StateManager","unloading Model "+((Integer)modelID).toString());
	}
		
	/**
	 * deactivates all currently loaded models
	 */
	public void deactivate() {
//		ContextBus.getInstance().unsubscribe(this);
		DatabaseLogger.releaseInstance(this);
		HashMap<Integer, ModelCalculation> tempModels = new HashMap<Integer, ModelCalculation>();
		tempModels.putAll(models);
		for (int model : tempModels.keySet()) {
			deactivate(model);
		}
		featureCalculation.finalize();
		featureCalculation=null;
		db = null;

	}
	/**
	 * dynamicly add/delete a sensor / feature combination (from a model),<br /> the model has to be loaded already
	 * for this to work.
	 * @param modelID
	 * @param featureSensorID
	 */
	public void addFeature(int modelID, int featureSensorID) {
		if (models.containsKey(modelID)) {
			if (!modelToSFMapping.get(modelID).contains(featureSensorID) ) {
				modelToSFMapping.get(modelID).add(featureSensorID);
				if (!SFlist.contains(featureSensorID) ) {
					SFlist.add(featureSensorID);
					addFeature(modelID, featureSensorID);
				}
			}
			
		}
		
	}
//TODO: this functions seems wrong, it unloads all sensors even if they are still needed by other features!
	private void unloadFeatures(ArrayList<Integer> tosStop) {
		if (tosStop!= null && !tosStop.isEmpty()) {
			int i = 0;
			HashMap<Integer, ArrayList<AbstractFeature>> newSensorFeature = FeatureCalculation.mapping;
			for (; i< tosStop.size(); i++) {

				int sensor = Constants.parseSensorId(tosStop.get(i));
				int feature = Constants.parseFeatureId(tosStop.get(i));
				if (features.keySet().contains(feature)) {
					features.remove(feature);
				}

				ArrayList<Integer> sf = sensorFeature.get(sensor);
				if (sf != null) {
					if (sf.size()<2) {
						sensorFeature.remove(sensor);
					} else {
	//					sensorFeature.get(sensor).removeAll(Arrays.asList(new Integer[]{feature}));
						sensorFeature.get(sensor).remove(sensorFeature.get(sensor).indexOf(feature));
					}
				}
				// add all new feature/sensor combinations to the mapping list for the feature Calculation
				
				if (newSensorFeature.containsKey(sensor)) {
					if (newSensorFeature.get(sensor).size()<2) {
						newSensorFeature.remove(sensor);
					} else {
						newSensorFeature.get(sensor).removeAll(Arrays.asList(new Integer[]{feature}));
					}
				}
				
				if (sensors.containsKey(sensor) && !sensorFeature.containsKey(sensor)) {
					sensors.get(sensor).deactivate();
					sensors.remove(sensor);
				}

			}
			featureCalculation.setMap(newSensorFeature);
			
		}
		
	}

	/**
	 * used to dynamically activate sensors that are needed, but not direclty by a classifier. Necessary for some Virtual Sensors
	 * @param sensorid
	 */
	public void activateSensor(int sensorid) {
		if (!sensors.containsKey(sensorid)) {
			if (sensorFeature.get(sensorid) == null) {
				sensorFeature.put(sensorid, new ArrayList<Integer>());
			}
			sensors.put(sensorid, Factory.sensorFactory(sensorid));
			sensors.get(sensorid).activate();
		}		
	}

    
    public void deactivateSensor(int sensorid) {
            if (sensors.containsKey(sensorid)) {
                    if (sensorFeature.get(sensorid).isEmpty()) {
                            sensors.get(sensorid).deactivate();
                            sensors.remove(sensorid);
                    }
            }              
    }
    
	/**
	 * publish a new ground truth label (from EMA for example) to the model
	 * @param modelStress
	 * @param newLabel
	 */

	public void publishNewGroundTruth(int modelStress, float newLabel) {
		if (models.containsKey(modelStress)) {
			models.get(modelStress).setLabel(newLabel);
		}
	}


	// activation/deactivation of contexts	
	private HashMap<Integer, HashMap<Integer, ArrayList<Integer> > > activationTriggers = new HashMap<Integer, HashMap<Integer, ArrayList<Integer> > >() {
		{
			put(Constants.MODEL_ACTIVITY, new HashMap<Integer, ArrayList<Integer> >());
			get(Constants.MODEL_ACTIVITY).put(ActivityCalculation.SIT, new ArrayList<Integer>());
			get(Constants.MODEL_ACTIVITY).get(ActivityCalculation.SIT).add(Constants.MODEL_DATAQUALITY);
			get(Constants.MODEL_ACTIVITY).get(ActivityCalculation.SIT).add(Constants.MODEL_STRESS);
			get(Constants.MODEL_ACTIVITY).get(ActivityCalculation.SIT).add(Constants.MODEL_ACCUMULATION);
			
			get(Constants.MODEL_ACTIVITY).put(ActivityCalculation.STAND, new ArrayList<Integer>());			
			get(Constants.MODEL_ACTIVITY).get(ActivityCalculation.STAND).add(Constants.MODEL_DATAQUALITY);
			get(Constants.MODEL_ACTIVITY).get(ActivityCalculation.STAND).add(Constants.MODEL_STRESS);
			get(Constants.MODEL_ACTIVITY).get(ActivityCalculation.STAND).add(Constants.MODEL_ACCUMULATION);

			get(Constants.MODEL_ACTIVITY).put(ActivityCalculation.WALK, new ArrayList<Integer>());						
			get(Constants.MODEL_ACTIVITY).get(ActivityCalculation.WALK).add(Constants.MODEL_COMMUTING);
		}
	};

	private HashMap<Integer, HashMap<Integer, ArrayList<Integer> > > deactivationTriggers = new HashMap<Integer, HashMap<Integer, ArrayList<Integer> > >() {
		{
			put(Constants.MODEL_ACTIVITY, new HashMap<Integer, ArrayList<Integer> >());
			get(Constants.MODEL_ACTIVITY).put(ActivityCalculation.WALK, new ArrayList<Integer>());									
			get(Constants.MODEL_ACTIVITY).get(ActivityCalculation.WALK).add(Constants.MODEL_DATAQUALITY);
			get(Constants.MODEL_ACTIVITY).get(ActivityCalculation.WALK).add(Constants.MODEL_STRESS);
			get(Constants.MODEL_ACTIVITY).get(ActivityCalculation.WALK).add(Constants.MODEL_ACCUMULATION);
			get(Constants.MODEL_ACTIVITY).put(ActivityCalculation.SIT, new ArrayList<Integer>());									
			get(Constants.MODEL_ACTIVITY).get(ActivityCalculation.SIT).add(Constants.MODEL_COMMUTING);			
			get(Constants.MODEL_ACTIVITY).put(ActivityCalculation.STAND, new ArrayList<Integer>());									
			get(Constants.MODEL_ACTIVITY).get(ActivityCalculation.STAND).add(Constants.MODEL_COMMUTING);						
		}
	};

	int lastContext = -1;
	int[] initbuffer = {-1, -1, -1, -1, -1};
	HashMap<Integer, int[]> contextBuffers = new HashMap<Integer, int[]>() {
		{
			put(Constants.MODEL_ACTIVITY, initbuffer.clone());
		}
	};
	
	HashMap<Integer, Integer> contextBuffersIndex = new HashMap<Integer, Integer>() {
		{
			put(Constants.MODEL_ACTIVITY, 0);
		}
	};
	
	// MJRTY linear time majority function, from Boyer and Moore
	private int majority(int[] contexts) {
		int maj = -1, cnt = 0;
		for (int i=0; i<contexts.length; i++) {
			// buffer hasn't been filled yet
			if (contexts[i] == -1) {
				return -1;
			}
			if (cnt == 0) {
				maj = contexts[i];
				cnt = 1;
			} else {
				if (contexts[i] == maj) {
					cnt ++;
				} else {
					cnt --;
				}
			}
		}
		return maj;		
	}

	
	public void receiveContext(int modelID, int label, long startTime,
			long endTime) {
		
		// add the latest label to the context buffer;
		int[] buffer = contextBuffers.get(modelID);
		if (buffer != null) {
			Integer index = contextBuffersIndex.get(modelID);
			buffer[index] = label;
			index++;
			if (index == initbuffer.length)
				index = 0;
			
			// compute the majority from the buffer
			int context = majority(buffer);		
			if (context != -1 && context != lastContext) {
				checkActivationRules(modelID, context);
				checkDeactivationRules(modelID, context);
				lastContext = context;
			}
		}
	}

	/**
	 * @param modelID
	 * @param context
	 */
	private void checkDeactivationRules(int modelID, int context) {
		if (deactivationTriggers.containsKey(modelID)) {
			HashMap<Integer, ArrayList<Integer> > modelRules = deactivationTriggers.get(modelID);
			if (modelRules.containsKey(context)) {
				ArrayList<Integer> rules = modelRules.get(context);
				
				for (Integer modelToDeactivate : rules) {
					this.deactivate(modelToDeactivate);
				}
			}
		}
	}

	/**
	 * @param modelID
	 * @param context
	 */
	private void checkActivationRules(int modelID, int context) {
		if (activationTriggers.containsKey(modelID)) {
			HashMap<Integer, ArrayList<Integer> > modelRules = activationTriggers.get(modelID);
			if (modelRules.containsKey(context)) {
				ArrayList<Integer> rules = modelRules.get(context);
				
				for (Integer modelToActivate : rules) {
					this.activate(modelToActivate);
				}
			}
		}
	}

	
	
	
}
