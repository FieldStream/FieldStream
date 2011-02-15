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

package org.fieldstream.service.sensor.virtual;


//@author Patrick Blitz
//@author Monowar Hossain


import java.util.Arrays;

import org.fieldstream.Constants;
import org.fieldstream.service.InferrenceService;
import org.fieldstream.service.features.Percentile;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensor.SensorBus;
import org.fieldstream.service.sensor.SensorBusSubscriber;
import org.fieldstream.service.sensors.api.AbstractSensor;
import org.fieldstream.service.sensors.mote.sensors.MoteSensorManager;
import org.fieldstream.service.sensors.mote.sensors.MoteUpdateSubscriber;

/**
 * This Class has two modes. One connects to the mote subsystem to receive updates for the ECG one connects to the replay server. 
 * @author mahbub
 *
 */
public class  RespirationVirtualSensor extends AbstractSensor implements MoteUpdateSubscriber, SensorBusSubscriber {

	private static final int FRAMERATE = 60;
	/**
	 * duration in seconds.
	 */
	private static final int WINDOW_DURATION=60;
	private static final int PVWINDOWSIZE = WINDOW_DURATION*FRAMERATE;
	private static final Boolean PVSCHEDULER = false;
	/**
	 * decide if the Replay sensor or the mote RIP sensor should be used! 
	 */
	//private static final Boolean REPLAY_SENSOR = true;
	private static final Boolean REPLAY_SENSOR = false;
	/**
	 *typically duration of respiration period is more or less four seconds. 
	 */
	private static int numberOfConsecutiveEmptyRealPeaks=0;
	/**
	 * if number of consecutive empty real peaks are more than tolerance than we are adjusting peak value peakThreshold.
	 * if the band is disconnected then it also updates its threshold unnecessarily. in that case, data quality output can be consulted.
	 */
	private static final int toleranceOfNotFindingPeaks=2;

	/**
	 * all peaks should be over this value
	 *threshold value is adaptive
	 */
	public static int peakThreshold=2550;								
	/**
	 * minimum distance between two real peaks, in terms of the number of samples
	 */
	public static final int durationThreshold=100;	

	public static final int numberOfPeakThreshold=WINDOW_DURATION*FRAMERATE/durationThreshold;
	public static final double quantile=65.0;
	/**
	 * False means last anchor of the previous window is valley
	 * True means last anchor of the previous window is peak
	 * default value is false. that means at the starting of calculation, it finds valley first.
	 */

	private RespirationRunner runner = new RespirationRunner(); 

	private Object lock = new Object();
	/**
	 * internal runner to execute the PVCalculation in a separate thread
	 * @author mahbub
	 *
	 */
	class RespirationRunner implements Runnable {
		//private PVCalculation pvCalculation;
		private RespirationCalculation RespirationCalculation;
		public RespirationRunner() {
			RespirationCalculation = new RespirationCalculation();
			//pvCalculation = new PVCalculation();
		}
		public boolean active=true;
		public int[] buffer;
		public long[] timestamps;
		public int startNewData;
		public int endData;
		public void run() {
			while (active) {
				synchronized (lock) {

					if (buffer!=null) {
						Log.d("RespirationBefore","BeginTS= "+timestamps[0]+"EndTS= "+timestamps[timestamps.length-1]);
						int[] calculate = RespirationCalculation.calculate(buffer, timestamps);
						//int[] calculate = pvCalculation.calculate(buffer, timestamps);
						if((calculate.length==0))
						{
							//first check the variance. if it is very low then the band might be off
							numberOfConsecutiveEmptyRealPeaks++;
							if(numberOfConsecutiveEmptyRealPeaks>=toleranceOfNotFindingPeaks)
							{
								//adjust threshold for peaks
								//should check data quality..........make sure that band is not loose
								//Log.d("Threshold"," no peak="+peakThreshold);
								peakThreshold=(int)Percentile.evaluate(buffer, quantile);
								//calculate = pvCalculation.calculate(buffer, timestamps);
								//continue;		//test wheather it works or not.
								numberOfConsecutiveEmptyRealPeaks=0;
							}
						}
						//check this condition again....it may be unnecessary
//						else if((calculate.length>4*numberOfPeakThreshold))	//too many real peaks, need to adjust the threshold to upper value; 5 is probable offset
//						{
//							//							Variance var=new Variance(Constants.FEATURE_VAR, false, 0);
//							//							if(var.calculate(buffer, timestamps, Constants.SENSOR_RIP)>1)
//							//							{
//							//Log.d("Threshold"," too many peaks="+peakThreshold);
//							peakThreshold=(int)Percentile.evaluate(buffer, quantile);
//							//calculate = pvCalculation.calculate(buffer, timestamps);
//							//continue;			//test whether it works or not
//							//							}
//						}
						else									//if the calculation function returns null.....
						{
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
							
							Log.d("RespirationAfter","BeginTS= "+timestampsNew[0]+"EndTS= "+timestampsNew[timestampsNew.length-1]);
							int start = 0;
							int end = calculate.length;
							
							String sensor = "";

							for (int i=0; i < calculate.length; i++) {
								sensor += calculate[i] + ",";
							}		
							Log.d("RespirationVirtualSensor", "RealPeakValley = " + sensor);	

							String timeStamp="";
							for(int i=0;i<timestampsNew.length;i++)
							{
								timeStamp+=timestampsNew[i]+",";
							}
							Log.d("RespirationVirtualSensor","RealPeakValley timestamp= "+timeStamp);

							sendBufferReal(calculate, timestampsNew, start, end);
						}							
					}

					try {
						if (active) {
							lock.wait();
						}
					} catch (InterruptedException e) {

					}
				}
			}
		}	
	};
	private Thread RespirationThread;
	private RespirationVirtualSensor INSTANCE;

	public RespirationVirtualSensor(int SensorID) {
		super(SensorID);
		//		try {
		//			outFile = new FileWriter("/mahbub/logDump.txt",true);
		//			out = new PrintWriter(outFile,true);
		//		} catch (IOException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}

		INSTANCE = this;
		initalize(PVSCHEDULER,PVWINDOWSIZE, PVWINDOWSIZE);	
	}	
	@Override
	public void activate() {
		active = true;
		runner.active = true;
		RespirationThread = new Thread(runner);
		RespirationThread.start();	
//		if (REPLAY_SENSOR) { 
//			SensorBus.getInstance().subscribe(this);
//		} else {
//			MoteSensorManager.getInstance().registerListener(this);
//		}
		//SensorBus.getInstance().subscribe(this);
		// as this depends on the ECG sensor to be active, i need to load it to make sure it's there!
		MoteSensorManager.getInstance().registerListener(this);
		if (REPLAY_SENSOR) {
			InferrenceService.INSTANCE.fm.activateSensor(Constants.SENSOR_REPLAY_RESP);
		} else {
			InferrenceService.INSTANCE.fm.activateSensor(Constants.SENSOR_RIP);
		}
	}

	@Override
	public void deactivate() {
//		if (REPLAY_SENSOR) {
//			SensorBus.getInstance().unsubscribe(this);
//		} else {
//			MoteSensorManager.getInstance().unregisterListener(this);
//		}
		//SensorBus.getInstance().unsubscribe(this);
		MoteSensorManager.getInstance().unregisterListener(this);
		if (REPLAY_SENSOR) {
			InferrenceService.INSTANCE.fm.deactivateSensor(Constants.SENSOR_REPLAY_RESP);
		} else {
			InferrenceService.INSTANCE.fm.deactivateSensor(Constants.SENSOR_RIP);	
		}


		active = false;
		synchronized(lock) {
			lock.notify();
			runner.active=false;
			RespirationThread=null;
		}

	}
	@Override
	protected void sendBuffer(int[] toSendSamples, long[] toSendTimestamps,
			int startNewData, int endNewData) {
		// TODO Auto-generated method stub
		//		super.sendBuffer(toSendSamples, toSendTimestamps, startNewData, endNewData);
		if (active) {
			synchronized (lock) {
				runner.buffer=toSendSamples;
				runner.timestamps=toSendTimestamps;
				runner.startNewData=startNewData;
				runner.endData=endNewData;
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
	protected void sendBufferReal(int[] toSendSamples, long[] toSendTimestamps,
			int startNewData, int endNewData) {
		// TODO Auto-generated method stub
		super.sendBuffer(toSendSamples, toSendTimestamps, startNewData, endNewData);
	}

	public void onReceiveData(int SensorID, int[] data, long[] timeStamps) {
		if(SensorID == Constants.SENSOR_RIP)
		{
//			int length=runner.timestamps.length;
//			for(int k=0;k<length;k++)
//				if(timestamp<runner.timestamps[k])
//				{
//					Log.d("DataRepeatation","Data Repeated");
//					return;
//				}
			//long[] timeStamps = new long[data.length];
			//Arrays.fill(timeStamps, 0, data.length, timestamp);
			addValue(data, timeStamps);	
			//Log.d("RealPeakValleyVirtualSensor", "raw value = " + data[0]);
			//comment out-mahbub
			//Log.d("RealPeakValleyVirtualSensor", "length of data array = " + data[0]);
//			String ripData="";
//			for(int i=0;i<data.length;i++)
//			{
//				ripData+=data[i]+",";
//			}
//			String checktimestamp="";
//			for(int i=0;i<timeStamps.length;i++)
//			{
//				checktimestamp+=timestamps[i]+",";
//			}
//			Log.d("RealPeakValleyVirtualSensor", "raw RIP data= "+ripData);
//			Log.d("RealPeakValleyVirtualSensor","raw RIP data timestamp= "+checktimestamp);
		}
	}

	public void receiveBuffer(int sensorID, int[] data, long[] timestamps,
			int startNewData, int endNewData) {
		if (sensorID==Constants.SENSOR_REPLAY_RESP) {
			addValue(data, timestamps);		
		}
	}
}

