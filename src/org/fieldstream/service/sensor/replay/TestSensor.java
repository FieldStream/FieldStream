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
//@author Kurt Plarre
//@author Mahbub Rahman

package org.fieldstream.service.sensor.replay;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;

import org.fieldstream.Constants;
import org.fieldstream.service.StateManager;
import org.fieldstream.service.logger.Log;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.Toast;



public class TestSensor extends org.fieldstream.service.sensors.api.AbstractSensor {
	@SuppressWarnings("unused")
	private final static int WindowSize = 60 ; // in seconds
	@SuppressWarnings("unused")
	private final static int WindowsStep = 2 ; // in seconds
	@SuppressWarnings("unused")
	private int TYPE;
	private BufferedReader reader;
	private HandlerThread threader;
	private Handler myhandler;
	private int delay;
	private ReaderRunner readerRunner;
	
	/**
	 * constructor, finally called by {@link StateManager} to establish this sensors. Has to set the correct parameters for this sensor 
	 * @param SensorID
	 */
	
	public TestSensor(int SensorID ) {
		
		super(SensorID);
		//	, scheduler, windowLength, slidingWindowStep);
		switch (SensorID) {
		case Constants.SENSOR_REPLAY_ECK:
				TYPE = TestSensorStorage.ECG;
				// framerate for ECG is 64hz
				frameRate = 60;
			break;
		case Constants.SENSOR_REPLAY_GSR:
			TYPE = TestSensorStorage.GSR;
			// framerate for GSR is 16Hz
			frameRate = 10;
			break;
		case Constants.SENSOR_REPLAY_RESP:
			TYPE = TestSensorStorage.RESP;
			// framerate for RIP is 60hz
			frameRate = 60;
			
		break;
		case Constants.SENSOR_REPLAY_TEMP:
			TYPE = TestSensorStorage.TEMP;
			// framerate for ECG is 64hz
			frameRate = 10;
			
		break;
//		//case added by mahbub.........need to check whether it is required or not. because, these virtual classes are supposed to collect data from its previous sensor/virtual sensor
//		case Constants.SENSOR_VIRTUAL_INHALATION:
//			TYPE = TestSensorStorage.RESP;
//			// framerate for RIP is 60hz
//			frameRate = 60;
//		break;
//		//added by mahbub
//		case Constants.SENSOR_VIRTUAL_EXHALATION:
//			TYPE = TestSensorStorage.RESP;
//			// framerate for RIP is 60hz
//			frameRate = 60;
//			//added by mahbub
//		case Constants.SENSOR_VIRTUAL_IERATIO:
//			TYPE = TestSensorStorage.RESP;
//			// framerate for RIP is 60hz
//			frameRate = 60;
//		break;
//		//added by mahbub
//		case Constants.SENSOR_VIRTUAL_REALPEAKVALLEY:
//			TYPE = TestSensorStorage.RESP;
//			// framerate for RIP is 60hz
//			frameRate = 60;
//		break;
		default:
			frameRate=1;
			break;
		} 
		
		reader = TestSensorStorage.getInstance().getReader(TYPE);
		threader = new HandlerThread("ReplaySensor"+TYPE);
		threader.start();
		myhandler= new Handler(threader.getLooper());
		try {
			reader.mark(Integer.MAX_VALUE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// find out the delay time
		// it's 1/frameRate*1000(miliseconds)*WindowStep
		delay=(100*WindowsStep*frameRate)/frameRate;
		readerRunner = new ReaderRunner();
		initalize(false, WindowSize*frameRate, WindowSize*frameRate);
	}

	@Override
	public void activate() {
		active=true;
		myhandler.postDelayed(readerRunner,delay);
	}

	@Override
	public void deactivate() {
		active=false;
		myhandler.removeCallbacks(readerRunner);
		threader.getLooper().quit();
		
	}
	
	class ReaderRunner implements Runnable {
		String line;
		long lastTime;
//		DecimalFormat format = new DecimalFormat("0.################E0");
		public void run() {
			if (active) {
				if (reader!=null) {
					try {
						if ((line=reader.readLine())!=null) {
							String[] valuesString = line.split(";");
							int[] values = new int[valuesString.length];
							long[] timeStamps = new long[valuesString.length];
							for (int i=0;i<valuesString.length;i++) {
								timeStamps[i]=lastTime+delay/50;
								if (!valuesString[i].equals("")) {
//									try {
									
									values[i]=Integer.parseInt(valuesString[i].trim());
									
//										values[i]=.parse(valuesString[i]).intValue();
//									} catch (ParseException e) {
										// TODO Auto-generated catch block
//										values[i]=Integer.MIN_VALUE;
//									}
//								values[i]=Integer.parseInt(valuesString[i]);
								}
							}
							timeStamps[timeStamps.length-1]=System.currentTimeMillis();
							
							addValue(values, timeStamps);
//							if (Log.DEBUG) Log.d("TestSensor", "finished new replay window, time " + System.currentTimeMillis() + " and time since last call (in millis) " + (System.currentTimeMillis()-lastTime));
//							if (Log.VERBOSE) {
//								String output="";
//								for (int i=0;i<values.length;i++ ) {
//									output+=";"+values[i];
//								}
//								
//								Log.v("TestSensor","values:" +output);
//							}
							lastTime=System.currentTimeMillis();
						} else {
							reader.reset();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			myhandler.postAtTime(this,SystemClock.uptimeMillis()+delay);
		}
		
	}
	
}
