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

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

/**
 * 
 * @author Patrick Blitz
 * @author Andrew Raij
 * 
 */
public class FeatureRunner {
	private FeatureCalculation calculation2;
	private ArrayBlockingQueue<FeatureData> queue;
	private Thread thread;
	private Handler handler;

	private static FeatureRunner INSTANCE = null;
	
	private static String TAG = "FeatureRunner";
	
	private FeatureRunner() {
		calculation2 = FeatureCalculation.getInstance();
		queue = new ArrayBlockingQueue<FeatureData>(100);
		thread = new Thread() {
			public void run() {
				  try {
				    // preparing a looper on current thread
				    // the current thread is being detected implicitly
				    Looper.prepare();
				 				    
				    // now, the handler will automatically bind to the
				    // Looper that is attached to the current thread
				    // You don't need to specify the Looper explicitly
				    handler = new Handler() {
				    	public void handleMessage(Message msg) {
				    		FeatureData data = (FeatureData)msg.obj;
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
				    };
				    				    
				    // After the following line the thread will start
				    // running the message loop and will not normally
				    // exit the loop unless a problem happens or you
				    // quit() the looper (see below)
				    Looper.loop();
				  } catch (Throwable t) {
				    Log.e(TAG, "halted due to an error");
				  }
			}
		};
		
		thread.start();
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
		
	public synchronized void addBuffer(FeatureData featureData) {
		while (handler == null) {
			try {
				wait(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Message msg=handler.obtainMessage(0, featureData);
		handler.sendMessage(msg);

	}

}
