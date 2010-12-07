﻿//Copyright (c) 2010, University of Memphis, Carnegie Mellon University
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
//@author Kurt Plarre


import java.util.Arrays;

import org.fieldstream.Constants;
import org.fieldstream.service.InferrenceService;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensor.SensorBus;
import org.fieldstream.service.sensor.SensorBusSubscriber;
import org.fieldstream.service.sensors.api.AbstractSensor;
import org.fieldstream.service.sensors.mote.MoteSensorManager;
import org.fieldstream.service.sensors.mote.MoteUpdateSubscriber;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;


public class TempQualityVirtualSensor extends AbstractSensor implements MoteUpdateSubscriber, SensorBusSubscriber {
	private static final String TAG = "TempQualityVirtualSensor";
	private static final Boolean REPLAY_SENSOR = false;
	private TempQualityRunner runner = new TempQualityRunner(); 

	private static NotificationManager nm;
	private static Notification notif;

	public static int currentQuality=0;

	private Object lock = new Object();
	class TempQualityRunner implements Runnable {
		TempQualityCalculation calculateQuality;
		public TempQualityRunner(){
			if(Log.DEBUG) Log.d(TAG,"Runner: initialized");
			calculateQuality=new TempQualityCalculation();
		}
		public boolean active=true;
		public int[] buffer;
		public long[] timestamps;
		public int startNewData;
		public int endData;
		
		public void run(){
			while (active){
				synchronized (lock){
					if (buffer!=null){
						if(Log.DEBUG) Log.d(TAG,"Runner: buffer.length="+buffer.length+" timestamps.length="+timestamps.length);
						if(Log.DEBUG) Log.d(TAG,"Runner: buffer="+buffer[0]+" "+buffer[1]+" "+buffer[2]+" "+buffer[3]+" "+buffer[4]+" "+buffer[5]);
						// =================================================
						int[] quality=new int[1];
						quality[0]=calculateQuality.currentQuality(buffer);
						currentQuality=quality[0];
						long[] newTimeStamp=new long[1];
						newTimeStamp[0]=timestamps[timestamps.length-1];

						if(Log.DEBUG) Log.d(TAG,"Runner(): sending "+quality[0]+" "+newTimeStamp[0]);

						sendBufferReal(quality,newTimeStamp, 0, 1);
						if(Log.DEBUG) Log.d(TAG,"Runner: sent");
						// =================================================
						if(nm == null) nm=( NotificationManager ) InferrenceService.INSTANCE.getSystemService( InferrenceService.NOTIFICATION_SERVICE );
						if((RipQualityVirtualSensor.currentQuality==0)&(EckQualityVirtualSensor.currentQuality==0)){
							if(notif==null) notif=new Notification();
							if(currentQuality==3){
								if(Log.DEBUG) Log.d(TAG,"Runner: blinking LED");
								notif.ledARGB = 0xFF0000FF;
								notif.ledOnMS = 300;
								notif.ledOffMS = 300;
								notif.flags |= Notification.FLAG_SHOW_LIGHTS;
								//long[] vibrate = {0,100,300,500};
								//notif.vibrate = vibrate;
								nm.notify(1, notif);
							}else if(currentQuality==2){
								if(Log.DEBUG) Log.d(TAG,"Runner: blinking LED");
								notif.ledARGB = 0xFF0000FF;
								notif.ledOnMS = 1000; 
								notif.ledOffMS = 1000; 
								notif.flags |= Notification.FLAG_SHOW_LIGHTS;
								nm.notify(1, notif);
							}else{
								if(Log.DEBUG) Log.d(TAG,"Runner: LED OFF");
								notif.ledARGB = 0xFFFFFF00;
								notif.ledOnMS = 1; 
								notif.ledOffMS = 0; 
								notif.flags |= Notification.FLAG_SHOW_LIGHTS;
								nm.notify(1, notif);
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
	
	private Thread RipThread;
	private TempQualityVirtualSensor INSTANCE;

	private static final int SAMPLES_PER_SECOND=10;
	private static final int WINDOW_SIZE_IN_SECONDS=3;
	private static final int PVWINDOWSIZE = SAMPLES_PER_SECOND*WINDOW_SIZE_IN_SECONDS;
	private static final Boolean PVSCHEDULER = false;
	public TempQualityVirtualSensor(int SensorID) {
		super(SensorID);
		if (Log.DEBUG) Log.d(TAG, "activate");
		INSTANCE = this;
		initalize(PVSCHEDULER,PVWINDOWSIZE, PVWINDOWSIZE);	
		notif=new Notification();
	}

	@Override
	public void activate() {
		Log.d(TAG, "activate(): activated");
		active = true;
		runner.active=true;
		if (Log.DEBUG) Log.d(TAG, "activate");
		RipThread = new Thread(runner);
		RipThread.start();
		//SensorBus.getInstance().subscribe(this);
		MoteSensorManager.getInstance().registerListener(this);
		if (REPLAY_SENSOR) {
			InferrenceService.INSTANCE.fm.activateSensor(Constants.SENSOR_REPLAY_TEMP);
		} else {
			InferrenceService.INSTANCE.fm.activateSensor(Constants.SENSOR_BODY_TEMP);
		}		
	}

	@Override
	public void deactivate() {
		//SensorBus.getInstance().unsubscribe(this);
		MoteSensorManager.getInstance().unregisterListener(this);
		if (REPLAY_SENSOR) {
			InferrenceService.INSTANCE.fm.deactivateSensor(Constants.SENSOR_REPLAY_TEMP);
		} else {
			InferrenceService.INSTANCE.fm.deactivateSensor(Constants.SENSOR_BODY_TEMP);	
		}
		active = false;
		synchronized (lock) {
			runner.active=false;
			RipThread=null;
			lock.notify();
		}		
	}

	protected void calculate(int[] toSendSamples, long[] timestamps) {
		if (active) {
			synchronized (lock) {
				if(Log.DEBUG) Log.d(TAG, "calculate()");
				runner.buffer=toSendSamples;
				runner.timestamps=timestamps;
				lock.notify();	
			}
		}
	}

	@Override
	protected void sendBuffer(int[] toSendSamples, long[] toSendTimestamps,
			int startNewData, int endNewData) {
		//		super.sendBuffer(toSendSamples, toSendTimestamps, startNewData, endNewData);
		if (active) {
			synchronized (lock) {
				if(Log.DEBUG) Log.d(TAG, "sendBuffer() "+" "+toSendSamples.length+" "+toSendTimestamps.length+" "+startNewData+" "+endNewData);
				runner.buffer=toSendSamples;
				runner.timestamps=toSendTimestamps;
				runner.startNewData=startNewData;
				runner.endData=endNewData;
				lock.notify();	
			}
		}		
	}
	
	protected void sendBufferReal(int[] toSendSamples, long[] toSendTimestamps, int startNewData, int endNewData) {
		//if(Log.DEBUG) Log.d(TAG,"sendBufferReal: sending "+toSendSamples.length+" "+toSendTimestamps.length+" "+startNewData+" "+endNewData);
		super.sendBuffer(toSendSamples, toSendTimestamps, startNewData, endNewData);
		if(Log.DEBUG) Log.d(TAG,"sendBufferReal(): sent");
	}

	public void onReceiveData(int SensorID, int[] data, long[] timeStamps) {
		//if(Log.DEBUG) Log.d(TAG, "onReceiveData(): got a buffer from " + SensorID+" "+data.length+" "+timestamp);
		if(SensorID == Constants.SENSOR_BODY_TEMP)
		{
			double avg=0;
			for(int i=0;i<data.length;i++) avg+=data[i];
			avg=avg/data.length;
			if(Log.DEBUG) Log.d(TAG, "onReceiveData(): got a buffer from " + SensorID+" Npoints="+data.length+" avg="+avg);
			//long[] timeStamps = new long[data.length];
			//Arrays.fill(timeStamps, 0, data.length, timestamp);
			addValue(data, timeStamps);	
		}
	}
	
	public void receiveBuffer(int sensorID, int[] data, long[] timestamps, int startNewData, int endNewData) {
		//if(Log.DEBUG) Log.d(TAG, "receiveBuffer(): got a buffer from " + sensorID+" "+data.length+" "+timestamps.length+" "+startNewData+" "+endNewData);
		if (sensorID == Constants.SENSOR_BODY_TEMP) {
			//if(Log.DEBUG) Log.d(TAG, "receiveBuffer(): got a buffer from " + sensorID+" "+data.length+" "+timestamps.length+" "+startNewData+" "+endNewData);
			//addValue(data, timestamps);
		}
	}
}
