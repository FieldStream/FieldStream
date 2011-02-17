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

//@author Amin Ahsan Ali
//@author Patrick Blitz
//@author Mahbub Rahman


import org.fieldstream.Constants;
import org.fieldstream.service.InferrenceService;
import org.fieldstream.service.features.Percentile;
import org.fieldstream.service.sensor.SensorBus;
import org.fieldstream.service.sensor.SensorBusSubscriber;
import org.fieldstream.service.sensors.api.AbstractSensor;
import org.fieldstream.service.sensors.mote.MoteSensorManager;
import org.fieldstream.service.sensors.mote.MoteUpdateSubscriber;

import android.util.Log;

//import edu.cmu.ices.stress.phone.service.logger.Log;
/**
 * This Class has two modes. One connects to the mote subsystem to receive updates for the ECG one connects to the replay server. 
 * @author mahbub
 *
 */
//public class  BdurationVirtualSensor extends AbstractSensor implements MoteUpdateSubscriber, SensorBusSubscriber {
public class  BdurationVirtualSensor extends AbstractSensor implements SensorBusSubscriber {

	private static final int FRAMERATE = 60;
	private static final int missingIndicator=-1; //-1 in the array indicates that the data is missing
	private static final float MISSINGRATETHRESHOLD=20/100; //20% missing rate is allowed
	
	//private static final int WINDOW_DURATION=30;
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
	public static int peakThreshold=2200;								
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

	private BdurationRunner runner = new BdurationRunner(); 

	private Object lock = new Object();
	/**
	 * internal runner to execute the PVCalculation in a separate thread
	 * @author mahbub
	 *
	 */
	class BdurationRunner implements Runnable {
		//private PVCalculation pvCalculation;
		private BdurationCalculation bdurationCalculation;
		public BdurationRunner() {
			bdurationCalculation = new BdurationCalculation();
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
						//search missing indicator
						int len=buffer.length;
						int numberOfMissing=0,numberOfAvailable=0;
						double []missingRemovedVal=new double[len];
						double []missingRemovedTS=new double[len];
						double []missingValuedTS=new double[len];
//						double []interpoletedValForMissingTS=new double[len]; //will contain interpolated values for the missing timestamp positions
						for(int i=0;i<len;i++)
						{
							if(buffer[i]!=missingIndicator)
							{
								missingRemovedVal[numberOfAvailable]=buffer[i];
								missingRemovedTS[numberOfAvailable]=timestamps[i];
								numberOfAvailable++;
							}
							else
								missingValuedTS[numberOfMissing++]=buffer[i];
						}
						if(numberOfMissing/len<MISSINGRATETHRESHOLD)
						{
							//missing rate is below the threshold. now interpolate the missing values
							//then send them for the further computation
							//otherwise discard or send null values to upward in the framework
							SplineInterpolation spline = new SplineInterpolation(missingRemovedTS, missingRemovedVal);
							for(int i=0;i<numberOfMissing;i++)
							{
								//interpoletedValForMissingTS[i]=spline.spline_value(missingValuedTS[i]);  //interpolating each signal value for corresponding timestamp
								for(int j=0;j<len;j++)
								{
									if(timestamps[j]==missingValuedTS[i])
									{
										buffer[j]=(int)spline.spline_value(missingValuedTS[i]);  //assign the interpolated missing values into the buffer
									}
								}
							}						int[] calculate = bdurationCalculation.calculate(buffer, timestamps);
						//Log.d("Bduration:Calculate: ","length= "+calculate.length);
						//int[] calculate = pvCalculation.calculate(buffer, timestamps);
						if((calculate.length==0))
						{
							//first check the variance. if it is very low then the band might be off
							numberOfConsecutiveEmptyRealPeaks++;
							if(numberOfConsecutiveEmptyRealPeaks>=toleranceOfNotFindingPeaks)
							{
								peakThreshold=(int)Percentile.evaluate(buffer, quantile);
							}
						}
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

							int start = 0;
							int end = calculate.length;
							sendBufferReal(calculate, timestampsNew, start, end);
						}	
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
	private Thread BdurationThread;
	private BdurationVirtualSensor INSTANCE;

	public BdurationVirtualSensor(int SensorID) {
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
		BdurationThread = new Thread(runner);
		BdurationThread.start();	
//		if (REPLAY_SENSOR) { 
//			SensorBus.getInstance().subscribe(this);
//		} else {
//			MoteSensorManager.getInstance().registerListener(this);
//		}
		SensorBus.getInstance().subscribe(this);
		//MoteSensorManager.getInstance().registerListener(this);
		// as this depends on the ECG sensor to be active, i need to load it to make sure it's there!
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
		SensorBus.getInstance().unsubscribe(this);
		//MoteSensorManager.getInstance().unregisterListener(this);
		if (REPLAY_SENSOR) {
			InferrenceService.INSTANCE.fm.deactivateSensor(Constants.SENSOR_REPLAY_RESP);
		} else {
			InferrenceService.INSTANCE.fm.deactivateSensor(Constants.SENSOR_RIP);	
		}

		active = false;
		synchronized(lock) {
			lock.notify();
			runner.active=false;
			BdurationThread=null;
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

	/*public void onReceiveData(int SensorID, int[] data, long[] timeStamps) {
		if(SensorID == Constants.SENSOR_RIP)
		{
//			int length=runner.timestamps.length;
//			for(int k=0;k<length;k++)
//				if(timestamp<runner.timestamps[k])
//				{
//					Log.d("DataRepeatation","Data Repeated");
//					return;
//				}
		//	long[] timeStamps = new long[data.length];
		//	Arrays.fill(timeStamps, 0, data.length, timestamp);
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
//			Log.d("BdurationVirtualSensor", "raw RIP data= "+ripData);
//			Log.d("BdurationVirtualSensor","raw RIP data timestamp= "+checktimestamp);
		}
	}*/

	public void receiveBuffer(int sensorID, int[] data, long[] timestamps,
			int startNewData, int endNewData) {
		if (sensorID==Constants.SENSOR_REPLAY_RESP) {
			addValue(data, timestamps);		
		}
		if(sensorID==Constants.SENSOR_RIP)		//date: 20th January 2011: now it receives data from the sensor bus
		{
			addValue(data, timestamps);
			String ripData="";
			for(int i=0;i<data.length;i++)
			{
				ripData+=data[i]+",";
			}
			String checktimestamp="";
			for(int i=0;i<timestamps.length;i++)
			{
				checktimestamp+=timestamps[i]+",";
			}
			Log.d("BdurationVirtualSensor", "raw RIP data for Stretch= "+ripData);
			Log.d("BdurationVirtualSensor","raw RIP data timestamp for stretch= "+checktimestamp);
		}
	}
}

