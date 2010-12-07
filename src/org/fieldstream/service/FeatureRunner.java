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
import java.util.concurrent.ArrayBlockingQueue;

import org.fieldstream.Constants;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensor.FeatureBus;
import org.fieldstream.service.sensors.api.AbstractFeature;

import android.os.SystemClock;

/**
 * 
 * @author Patrick Blitz
 * @author Andrew Raij
 * 
 */
public class FeatureRunner implements Runnable {
	private FeatureData data;
	private FeatureCalculation calculation2;
	private ArrayBlockingQueue<FeatureData> queue;
	private static boolean run;
	private Thread thread;

	private static FeatureRunner INSTANCE = null;
	
	private static String TAG = "FeatureRunner";
	
	private FeatureRunner() {
		calculation2 = FeatureCalculation.getInstance();
		queue = new ArrayBlockingQueue<FeatureData>(100);
		thread = new Thread(this);
	}
	
	public static FeatureRunner getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new FeatureRunner();
		}
		else if (INSTANCE.thread.getState() == Thread.State.TERMINATED) {
			INSTANCE = new FeatureRunner();
		}
		
		return INSTANCE;
	}

	public void start() {
		run = true;
		
		// if this is a new feature runner thread increase its priority so that it doesn't get killed by Android		
		if(thread.getState() == Thread.State.NEW)
		{
			thread.setPriority(Thread.MAX_PRIORITY);
			thread.start();
			if (Log.DEBUG) Log.d(TAG,"Starting Feature Runner");
		}
		else if (thread.getState() == Thread.State.TERMINATED) {
			if (Log.DEBUG) Log.e(TAG,"FeatureRunner Terminated...what happened?");
		}
	}
	
	public Boolean isAlive() {
		return thread.isAlive();
	}
	
	public void run() {
		while (run) {
			try {
				data = queue.take();
			} catch (InterruptedException e) {
				if (Log.DEBUG) Log.d("FeatureCalc","Got interrupted getting a new element, quitting");
				data = null;
			} // this is a blocking queue, so it will wait till new events
				// objects are available!
			if (data != null) {
				long beginTime = 0, endTime = 0;
				if (data.timestamps.length > 0) {
					beginTime = data.timestamps[0];
					endTime = data.timestamps[data.timestamps.length - 1];
				}
				
				if (calculation2.mapping != null
						&& !calculation2.mapping.isEmpty()) {
					ArrayList<AbstractFeature> abstractFeatures = calculation2.mapping.get(data.sensorID);
					if (abstractFeatures != null) {
						for (AbstractFeature f : abstractFeatures) {
							if (f.active) {
								long start = SystemClock.uptimeMillis();
								double result = f.calculate(data.buffer,
										data.timestamps, data.sensorID);
								if (Log.DEBUG) Log.d("FeatureCalc", "Feature "
										+ ((Integer) f.featureID).toString()
										+ " for Sensor "+((Integer)data.sensorID).toString() 
										+ " has value "
										+ ((Double) result).toString() + " for "
										+ data.buffer.length
										+ " bufferlenght, calculated in  "
										+ (SystemClock.uptimeMillis() - start)
										/ 1000);
							// for testing, have the last time equal the current time
							//endTime=System.currentTimeMillis();
								FeatureBus.getInstance()
										.receiveUpdate(
												Constants.getId(f.featureID,
														data.sensorID), result,
												beginTime, endTime);
							}
						}
					}
				}
			}
		}
	}

	public void stop() {
		run = false;
		thread.interrupt();
	}
	
	public synchronized void addBuffer(FeatureData featureData) {
		try {
			queue.put(featureData);
			if (Log.DEBUG) Log.d(TAG,"Queue is now "+queue.size()+" long");
		} catch (InterruptedException e) {
			
		}
	}

}
