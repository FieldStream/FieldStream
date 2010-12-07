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
package org.fieldstream.service.sensor.virtual;

//@author Patrick Blitz
//@author Kurt Plarre
//@author Andrew Raij
//@author Siddharth Shah


import org.fieldstream.Constants;
import org.fieldstream.service.InferrenceService;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensor.SensorBus;
import org.fieldstream.service.sensor.SensorBusSubscriber;
import org.fieldstream.service.sensors.api.AbstractSensor;
/**
 * This Class has two modes. One connects to the mote subsystem to receive updates for the ECG one connects to the replay server. 
 * @author blitz
 *
 */

public class RRIntervalVirtualSensor extends AbstractSensor implements SensorBusSubscriber {

	private static final String TAG = "RRVirtualSensor";
	// windows are 1 minute worth of samples at 64 samples/second = 3850 samples

	/**
	 * decide if the Replay sensor or the mote ECG sensor should be used! 
	 */
	private static final Boolean REPLAY_SENSOR = false;
	private RRRunner runner = new RRRunner(); 
	
	private Object lock = new Object();
	/**
	 * internal runner to execute the RRCalculation in a separate thread
	 * @author blitz
	 *
	 */
		class RRRunner implements Runnable {
			private RRIntervalCalculation rrCalculation;
			public boolean active=true;
			public int[] buffer;
			public long[] timestamps;

			public RRRunner() {
				rrCalculation = new RRIntervalCalculation();
			}
			
			public void run() {
				Log.d(TAG, "run(): starting run()");
				while (active) {
					Log.d(TAG, "run(): active");
					synchronized (lock) {
						Log.d(TAG, "run(): got the lock");
						if (buffer!=null) {
							Log.d(TAG, "run(): buffer is not null");
							int[] calculate = rrCalculation.calculate(buffer, timestamps);
							Log.d(TAG, "run(): calculating rr intervals");
							if (calculate.length != 0) {
								Log.d(TAG, "run(): calculate result > 0");
								long[] timestampsNew = new long[calculate.length];
								if(calculate.length<=1)
								{
									timestampsNew[0]=timestamps[0];
								}
								else if (calculate.length<timestamps.length) {
									float fakeIndex = 0;
									float factor = timestamps.length/((float)calculate.length - 1);	
									timestampsNew[0]=timestamps[0];
									for (int i=1; i<calculate.length;i++) {
										fakeIndex = i * factor;
										timestampsNew[i]=timestamps[(int)Math.floor(fakeIndex)-1];
									}
								} else {
									for (int i=0; i<calculate.length;i++) {
										timestampsNew[i]=timestamps[i];
									}
								}

								Log.d(TAG, "run(): timestamps calculated");
								
								// using the current time as the timestamp when the calculation finished!
//								long timestamp = System.currentTimeMillis();
//								long[] timestampsNew= new long[calculate.length];
//								for (int i=0; i<calculate.length;i++) {
//									timestampsNew[i]=timestamp;	
//								}
								
								
								int start = 0;
								int end = calculate.length;
								if (Log.DEBUG) Log.d(TAG,"Sending buffer with length "+calculate.length+ ", end "+ end+ ", start "+start+", and timestamp length"+timestampsNew.length );
								if (Log.VERBOSE) {
									String output="";
									for (int i=0; i<calculate.length;i++) {
										output+=";"+calculate[i];
										
									}
									String input="";
									for (int i=0; i<buffer.length;i++) {
										input+=";"+buffer[i];
										if (i%250==0 && i>1) {
											Log.v("TestRRComputation","input: "+input);
											input="";
											
										}
									}
									Log.v("TestRRComputation","input: "+input);
									Log.v("TestRRComputation","values: "+output);
								}
		//						String rr = "RR intervals = ";
		//						for (int i=0; i<calculate.length; i++) {
		//							rr+= calculate[i] + " ";
		//						}
		//						Log.d("RRVirtualSensorRunner", rr);
								
			
								sendBufferReal(calculate, timestampsNew,start,end);
								if (Log.DEBUG) Log.d("RRVirtualSensorRunner", "Send buffer after calculation");
							}
						}
					try {
						if (active)
							Log.d(TAG, "run(): waiting for the lock");
							lock.wait();
							Log.d(TAG, "run(): notified the lock is free");
						} catch (Exception e) {
							Log.d(TAG, "run(): exception! " + e.toString());
						}
					}
				}
			}
	};
	private Thread RRThread;
	private RRIntervalVirtualSensor INSTANCE;

	public RRIntervalVirtualSensor(int SensorID) {
		super(SensorID);
		Log.d(TAG, "constructor(): started");
		INSTANCE = this;
	//	initalize(HRSCHEDULER,HRWINDOWSIZE, HRWINDOWSIZE);		
	}
	
	@Override
	public void activate() {
		Log.d(TAG, "activate(): activated");
		active = true;
		runner.active=true;
		if (Log.DEBUG) Log.d(TAG, "activate");
		RRThread = new Thread(runner);
		RRThread.start();

		SensorBus.getInstance().subscribe(this);
		// as this depends on the ECG sensor to be active, i need to load it to make sure it's there!
		if (REPLAY_SENSOR) {
			InferrenceService.INSTANCE.fm.activateSensor(Constants.SENSOR_REPLAY_ECK);
		} else {
			InferrenceService.INSTANCE.fm.activateSensor(Constants.SENSOR_ECK);
		}		
	}

	@Override
	public void deactivate() {
		
		SensorBus.getInstance().unsubscribe(this);
		if (REPLAY_SENSOR) {
			InferrenceService.INSTANCE.fm.deactivateSensor(Constants.SENSOR_REPLAY_ECK);
		} else {
			InferrenceService.INSTANCE.fm.deactivateSensor(Constants.SENSOR_ECK);	
		}
		
		active = false;
		synchronized (lock) {
			runner.active=false;
			RRThread=null;
			lock.notify();
		}		
	}


//	@Override
//	protected void sendBuffer(int[] toSendSamples, long[] toSendTimestamps,
//			int startNewData, int endNewData) {
//		// TODO Auto-generated method stub
////		super.sendBuffer(toSendSamples, toSendTimestamps, startNewData, endNewData);
//		if (Log.DEBUG) Log.d(TAG,"sendBuffer called");
//		if (active) {
//			synchronized (lock) {
//				if (Log.DEBUG) Log.d(TAG,"sendBuffer in lock");
//				runner.buffer=toSendSamples;
//				runner.timestamps=toSendTimestamps;
//				runner.startNewData=startNewData;
//				runner.endData=endNewData;
//				lock.notify();	
//			}
//		}
//		
//	}
	
	protected void calculate(int[] toSendSamples, long[] timestamps) {
		// TODO Auto-generated method stub
//		super.sendBuffer(toSendSamples, toSendTimestamps, startNewData, endNewData);
		if (Log.DEBUG) Log.d(TAG,"calculate(): calculate called");
		if (active) {
			if (Log.DEBUG) Log.d(TAG,"calculate(): active is true");
			synchronized (lock) {
				if (Log.DEBUG) Log.d(TAG,"calculate(): grabbed the lock");
				runner.buffer=toSendSamples;
				runner.timestamps=timestamps;

				if (Log.DEBUG) Log.d(TAG,"calculate(): notifying others that the lock is free");
				lock.notify();	
			}
		}
		
	}
	/**
	 * called from the Runner thread to actually send the new buffer to the AbstractSensor
	 * @param toSendSamples
	 * @param toSendTimestamps
	 * @param startNewData
	 * @param endNewData
	 */
	protected void sendBufferReal(int[] toSendSamples, long[] toSendTimestamps, int startNewData, int endNewData) {
		// TODO Auto-generated method stub
				
		super.sendBuffer(toSendSamples, toSendTimestamps, startNewData, endNewData);
	}


//
//	public void onReceiveData(int SensorID, int[] data) {
//		if(SensorID == Constants.SENSOR_ECK)
//		{
//			long t = System.currentTimeMillis();
//			long[] timeStamps = new long[data.length];
//			Arrays.fill(timeStamps, 0, data.length, t);
//			if (Log.DEBUG) Log.d("RRInterval Sensor", "received "+data.length+" samples");
//			addValue(data, timeStamps);		
//			
//			//Log.d("RRIntervalVirtualSensor", "raw value = " + data[0]);
//		}
//		
//	}



	public void receiveBuffer(int sensorID, int[] data, long[] timestamps,
			int startNewData, int endNewData) {
		Log.d(TAG, "activate(): got a buffer from " + sensorID);
		if (sensorID==Constants.SENSOR_REPLAY_ECK) {
			calculate(data, timestamps);		
		} else if (sensorID == Constants.SENSOR_ECK) {
			Log.d(TAG, "activate(): got a buffer from SENSOR_ECK");
			calculate(data, timestamps);
		}
	}
}
