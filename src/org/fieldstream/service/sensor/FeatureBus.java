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
package org.fieldstream.service.sensor;

import java.util.ArrayList;

import org.fieldstream.Constants;
import org.fieldstream.service.logger.Log;


/**
 * Singleton class that relays all Feature update to the subscribers
 * @author Patrick Blitz
 * @author Andrew Raij
 */

public class FeatureBus {

	private ArrayList<FeatureBusSubscriber> subscribers;
	private static FeatureBus INSTANCE;

	public static FeatureBus getInstance() {
		if (INSTANCE==null) {
			INSTANCE = new FeatureBus();
		}
		return INSTANCE;
	}

	protected void finalize() {
		Log.d("FeatureBus", "Garbage Collected");
	}
	
	private FeatureBus() {
		subscribers = new ArrayList<FeatureBusSubscriber>();
	}

	/**
	 * 
	 * @param subscriber
	 */
	
	public void subscribe(FeatureBusSubscriber subscriber) {
		if (!subscribers.contains(subscriber)) {
			subscribers.add(subscriber);

		}

	}

	/**
	 * unsubscribe from this bus
	 * @param subscriber
	 */
	
	public void unsubscribe(FeatureBusSubscriber subscriber) {
		if (subscribers.contains(subscriber)) {
			subscribers.remove(subscriber); 
		}
	}
	/**
	 * propagates the received values to the subscribers
	 * @param featureID
	 * @param result
	 * @param beginTime
	 * @param endTime
	 */

	public void receiveUpdate(int featureID, double result, long beginTime, long endTime) {
		
		int realFeatureID = Constants.parseFeatureId(featureID);
		int sensorID = Constants.parseSensorId(featureID);
		Log.d("FeatureBus", Constants.getFeatureDescription(realFeatureID) + " " + Constants.getSensorDescription(sensorID) + " = " + result + ", timestamp = " + beginTime);
		if (subscribers!= null) {
			for (int i=0;  i<subscribers.size(); i++) {
				subscribers.get(i).receiveUpdate(featureID, result, beginTime, endTime);
			}
		}
	}


}
