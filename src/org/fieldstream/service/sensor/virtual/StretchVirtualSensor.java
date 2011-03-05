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

//@author Amin Ahsan Ali
//@author Patrick Blitz
//@author Mahbub Rahman


import java.io.FileOutputStream;
import java.io.PrintStream;

import org.fieldstream.Constants;
import org.fieldstream.service.InferrenceService;
import org.fieldstream.service.features.Percentile;
import org.fieldstream.service.sensor.SensorBus;
import org.fieldstream.service.sensor.SensorBusSubscriber;
import org.fieldstream.service.sensors.api.AbstractSensor;
import org.fieldstream.service.sensors.mote.sensors.MoteSensorManager;
import org.fieldstream.service.sensors.mote.sensors.MoteUpdateSubscriber;

import android.util.Log;

//import edu.cmu.ices.stress.phone.service.logger.Log;
/**
 * This Class has two modes. One connects to the mote subsystem to receive updates for the ECG one connects to the replay server. 
 * @author mahbub
 *
 */
public class  StretchVirtualSensor extends AbstractSensor implements MoteUpdateSubscriber, SensorBusSubscriber {
//public class  StretchVirtualSensor extends AbstractSensor implements SensorBusSubscriber {  //now it should get data from the sensorbus. so it is a subscriber of sensorbus only

	private static final int FRAMERATE = 60;
	/**
	 * duration in seconds.
	 */
	//private static final int WINDOW_DURATION=30;
	private static final int WINDOW_DURATION=60;
	private static final int PVWINDOWSIZE = WINDOW_DURATION*FRAMERATE;
	private static final Boolean PVSCHEDULER = false;
	private static long timestamp_check=0;
	private FileOutputStream fout;
	private PrintStream printStrm;
	private String rawFromMote = "/sdcard/stressLog/rawFromMote.txt";
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

	private StretchRunner runner = new StretchRunner(); 

	private Object lock = new Object();
	/**
	 * internal runner to execute the PVCalculation in a separate thread
	 * @author mahbub
	 *
	 */
	class StretchRunner implements Runnable {
		//private PVCalculation pvCalculation;
		private StretchCalculation stretchCalculation;
		public StretchRunner() {
			stretchCalculation = new StretchCalculation();
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
						Log.d("StretchBefore","BeginTS= "+timestamps[0]+"EndTS= "+timestamps[timestamps.length-1]);
						//test purpose
						//						String raw="";
						//						for(int i=0;i<buffer.length;i++)
						//							raw+=buffer[i]+",";
						//						Log.d("BeforeCallingStretchCalculation","raw= "+raw);
						//						Log.d("BeforeCallingStretchCalculation","raw length= "+buffer.length);

						int[] calculate = stretchCalculation.calculate(buffer, timestamps);
						Log.d("Stretch:Calculate: ","length= "+calculate.length);
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
							}
						}
						/*else if((calculate.length>4*numberOfPeakThreshold))	//too many real peaks, need to adjust the threshold to upper value; 5 is probable offset
						{
							//							Variance var=new Variance(Constants.FEATURE_VAR, false, 0);
							//							if(var.calculate(buffer, timestamps, Constants.SENSOR_RIP)>1)
							//							{
							//Log.d("Threshold"," too many peaks="+peakThreshold);
							peakThreshold=(int)Percentile.evaluate(buffer, quantile);
							//calculate = pvCalculation.calculate(buffer, timestamps);
							//continue;			//test whether it works or not
							//							}
						}*/
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
							Log.d("StretchAfter","BeginTS= "+timestampsNew[0]+"EndTS= "+timestampsNew[timestampsNew.length-1]);
							//							for(int t=0;t<end;t++)
							//							{
							//								System.out.print(calculate[t]+" ");
							//							}
							//							
							//							String ripData="";
							//							for(int i=0;i<calculate.length;i++)
							//							{
							//								ripData+=calculate[i]+",";
							//							}
							//							String checktimestamp="";
							//							for(int i=0;i<timestampsNew.length;i++)
							//							{
							//								checktimestamp+=timestamps[i]+",";
							//							}
							//							Log.d("Stretch", "Stretch value= "+ripData);
							//							Log.d("Stretch","Stretch timestamp= "+checktimestamp);

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
	private Thread StretchThread;
	private StretchVirtualSensor INSTANCE;

	public StretchVirtualSensor(int SensorID) {
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
//		try
//		{
//			fout = new FileOutputStream(rawFromMote);
//			printStrm = new PrintStream(fout);
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//		}
		active = true;
		runner.active = true;
		runner.timestamps = null;
		runner.buffer = null;
		StretchThread = new Thread(runner);
		StretchThread.start();	
		//		if (REPLAY_SENSOR) { 
		//			SensorBus.getInstance().subscribe(this);
		//		} else {
		//			MoteSensorManager.getInstance().registerListener(this);
		//		}
		
		//MoteSensorManager.getInstance().registerListener(this);   //for MoteBusSubscriber
		SensorBus.getInstance().subscribe(this);   //for SEnsorBus subscribing
		// as this depends on the ECG sensor to be active, i need to load it to make sure it's there!
		if (REPLAY_SENSOR) {
			InferrenceService.INSTANCE.fm.activateSensor(Constants.SENSOR_REPLAY_RESP);
		} else {
			InferrenceService.INSTANCE.fm.activateSensor(Constants.SENSOR_RIP);
			//InferrenceService.INSTANCE.fm.activateSensor(Constants.SENSOR_ACCELPHONEZ);
		}

	}

	@Override
	public void deactivate() {
//		try{
//			printStrm.close();
//			fout.close();
//
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//		}

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
			//InferrenceService.INSTANCE.fm.deactivateSensor(Constants.SENSOR_ACCELPHONEZ);	
		}

		active = false;
		synchronized(lock) {
			lock.notify();
			runner.active=false;
			StretchThread=null;
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
		if(SensorID==Constants.SENSOR_ACCELPHONEZ) 
			Log.d("StretchVirtualSensor","Received ACCELPHONEZ from MoteBus");
		if(SensorID==Constants.SENSOR_RIP) 
			Log.d("StretchVirtualSensor","Received RIP from MoteBus");
		
	}
		/*		if(SensorID == Constants.SENSOR_RIP)
		{
			//all for the debugging
//			String str="";
//			if (timestamp_check!= 0) {
//				if(timestamp<timestamp_check)
//				{
//					Log.d("MoteBusDataRepeatation","Data Repeated");
//					//return;
//				}
//			}
//
//			//write to the file
//			for(int i=0;i<data.length;i++)
//				str+=timestamp+","+data[i]+"\n";
//			try
//			{
//				printStrm.print(str);
//			}
//			catch(Exception e)
//			{
//				e.printStackTrace();
//			}
			// long[] timeStamps = new long[data.length];
			// Arrays.fill(timeStamps, 0, data.length, timestamp);
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
			//			Log.d("StretchVirtualSensor", "raw RIP data= "+ripData);
			//			Log.d("StretchVirtualSensor","raw RIP data timestamp= "+checktimestamp);
//			timestamp_check=timestamp;
		}
	}*/

	public void receiveBuffer(int sensorID, int[] data, long[] timestamps,
			int startNewData, int endNewData) {
		if (sensorID==Constants.SENSOR_REPLAY_RESP) {
			addValue(data, timestamps);

		}
//		if(sensorID==Constants.SENSOR_ACCELPHONEZ) 
//			Log.d("StretchVirtualSensor","Received ACCELPHONEZ");
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
			Log.d("StretchVirtualSensor", "raw RIP data for Stretch= "+ripData);
			Log.d("StretchVirtualSensor","raw RIP data timestamp for stretch= "+checktimestamp);
		}
	}
}

