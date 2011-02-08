package org.fieldstream.service.sensor.virtual;

import org.fieldstream.Constants;
import org.fieldstream.service.InferrenceService;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensor.SensorBus;
import org.fieldstream.service.sensor.SensorBusSubscriber;
import org.fieldstream.service.sensors.api.AbstractSensor;

import android.app.Notification;
import android.app.NotificationManager;

//public class MovementDetection extends AbstractSensor implements MoteUpdateSubscriber, SensorBusSubscriber {
public class AccelCommutingVirtualSensor extends AbstractSensor implements SensorBusSubscriber {
	// =====================================================================
	private static final String TAG = "AccelCommutingVirtualSensor";
	// =====================================================================

	// =====================================================================
	private static final Boolean REPLAY_SENSOR = false;
	private MovementDetectionRunner runner = new MovementDetectionRunner(); 
	private static int[] decBuff={0,0,0,0,0};
	private static int decHead=0;
	private static int currentDecision=0;
	private static NotificationManager nm;
	private static Notification notif;
	// =====================================================================

	private Object lock = new Object();
	class MovementDetectionRunner implements Runnable {
		AccelCommutingCalculation calculateDecision;
		public MovementDetectionRunner(){
			if(Log.DEBUG) Log.d(TAG,"Runner: initialized");
			calculateDecision=new AccelCommutingCalculation();
		}
		public boolean active=true;
		public int[] buffer;
		public long[] timestamps;
		public int startNewData;
		public int endData;

		// =====================================================================
		// 	RUNNER
		// =====================================================================
		public void run(){
			while (active){
				synchronized (lock){
					if (buffer!=null){
						// =====================================================================
						if(Log.DEBUG) Log.d(TAG,"Runner: buffer.length="+buffer.length+" timestamps.length="+timestamps.length);
		
						// =====================================================================
						// OBTAIN CURRENT DECISION
						// =====================================================================
						currentDecision=calculateDecision.calculate(buffer);
						if(Log.DEBUG) Log.d(TAG,"currentDecision="+currentDecision);
						int[] decision=new int[1];
						long[] newTimeStamp=new long[1];
						newTimeStamp[0]=timestamps[timestamps.length-1];

						// =====================================================================
						// PUT DECISION ON THE SENSOR BUS
						// =====================================================================
						if(decHead==decBuff.length){ 
							decHead=0;
							int sum=0;
							for(int i=0;i<decBuff.length;i++) sum+=decBuff[i];
							if(2*sum>=decBuff.length){
								decision[0]=1;
							}else{
								decision[0]=0;
							}
							sendBufferReal(decision,newTimeStamp,0,1);
							if(Log.DEBUG) Log.d(TAG,"Runner: sent");
						}else{
							decBuff[decHead]=currentDecision;
							decHead++;
						}

						// =====================================================================
						// SHOW OUTPUT ON LED (Blue=NO MOVEMENT, Orange=MOVEMENT)
						// =====================================================================
						if(Log.DEBUG){	
							if(nm == null) nm=( NotificationManager ) InferrenceService.INSTANCE.getSystemService(InferrenceService.NOTIFICATION_SERVICE);
							if(notif==null) notif=new Notification();
							if(currentDecision==0){
								if(Log.DEBUG) Log.d(TAG,"Runner: blinking LED: Blue");
								notif.ledARGB = 0xFF0000FF;
								notif.ledOnMS = 500;
								notif.ledOffMS = 500;
								notif.flags |= Notification.FLAG_SHOW_LIGHTS;
								nm.notify(1, notif);
							}else if(currentDecision==1){
								if(Log.DEBUG) Log.d(TAG,"Runner: blinking LED: Orange");
								notif.ledARGB = 0xFFFFFF00;
								notif.ledOnMS = 500; 
								notif.ledOffMS = 500; 
								notif.flags |= Notification.FLAG_SHOW_LIGHTS;
								nm.notify(1,notif);
							}
						}
						// =====================================================================
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
	}

	private Thread MovDetectThread;
	private AccelCommutingVirtualSensor INSTANCE;

	// =====================================================================
	// SET WINDOW SIZE
	// =====================================================================
	private static final int SAMPLES_PER_SECOND=100;
	private static final int WINDOW_SIZE_IN_SECONDS=12;
	private static final int PVWINDOWSIZE = SAMPLES_PER_SECOND*WINDOW_SIZE_IN_SECONDS;
	private static final Boolean PVSCHEDULER = false;
	// =====================================================================
	// CONSTRUCTOR
	// =====================================================================
	public AccelCommutingVirtualSensor(int SensorID){
		super(SensorID);
		if (Log.DEBUG) Log.d(TAG, "activate");
		INSTANCE = this;
		initalize(PVSCHEDULER,PVWINDOWSIZE, PVWINDOWSIZE);	
	}

	// =====================================================================
	// ACTIVATE (SUBSCRIBE TO SENSOR UPDATES)
	// =====================================================================
	@Override
	public void activate() {
		Log.d(TAG, "activate(): activated");
		active = true;
		runner.active=true;
		if (Log.DEBUG) Log.d(TAG, "activate");
		MovDetectThread = new Thread(runner);
		MovDetectThread.start();
		SensorBus.getInstance().subscribe(this);
		//MoteSensorManager.getInstance().registerListener(this);
		if (REPLAY_SENSOR) {
			// InferrenceService.INSTANCE.fm.activateSensor(Constants.SENSOR_REPLAY_TEMP);
		} else {
			InferrenceService.INSTANCE.fm.activateSensor(Constants.SENSOR_ACCELPHONEMAG);
		}		
	}

	// =====================================================================
	// DEACTIVATE (UNSUSCRIBE FROM SENSOR UPDATES)
	// =====================================================================
	@Override
	public void deactivate() {
		SensorBus.getInstance().unsubscribe(this);
		//MoteSensorManager.getInstance().unregisterListener(this);
		if (REPLAY_SENSOR) {
			//InferrenceService.INSTANCE.fm.deactivateSensor(Constants.SENSOR_REPLAY_TEMP);
		} else {
			InferrenceService.INSTANCE.fm.deactivateSensor(Constants.SENSOR_ACCELPHONEMAG);
		}
		active = false;
		synchronized (lock) {
			runner.active=false;
			MovDetectThread=null;
			lock.notify();
		}		
	}

	// =====================================================================
	// CALCULATE
	// =====================================================================
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
		if(Log.DEBUG) Log.d(TAG,"sendBuffer");
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
		if(Log.DEBUG) Log.d(TAG,"sendBufferReal");
		//if(Log.DEBUG) Log.d(TAG,"sendBufferReal: sending "+toSendSamples.length+" "+toSendTimestamps.length+" "+startNewData+" "+endNewData);
		super.sendBuffer(toSendSamples, toSendTimestamps, startNewData, endNewData);
		//SensorBus.getInstance().receiveBuffer(ID, toSendSamples, toSendTimestamps, startNewData, endNewData);
		if(Log.DEBUG) Log.d(TAG,"sendBufferReal(): sent");
	}

	// =====================================================================
	// RECEIVE DATA FROM MOTE BUS
	// =====================================================================
	//public void onReceiveData(int SensorID, int[] data, long[] timeStamps) {
	//if(Log.DEBUG) Log.d(TAG, "onReceiveData(): got a buffer from " + SensorID+" "+data.length+" "+timestamp);
	//if(SensorID == Constants.SENSOR_ACCELPHONEMAG){
	//if(Log.DEBUG) Log.d(TAG, "onReceiveData(): got a buffer from " + SensorID+" Npoints="+data.length);
	//long[] timeStamps = new long[data.length];
	//Arrays.fill(timeStamps, 0, data.length, timestamp);
	//	addValue(data, timeStamps);	
	//}
	//}

	// =====================================================================
	// RECEIVE DATA FROM SENSOR BUS
	// =====================================================================
	public void receiveBuffer(int sensorID, int[] data, long[] timestamps, int startNewData, int endNewData) {
		// if(Log.DEBUG) Log.d(TAG, "receiveBuffer(): got a buffer from " + sensorID+" "+data.length+" "+timestamps.length+" "+startNewData+" "+endNewData);
		if(Log.DEBUG) Log.d(TAG,"Received Sensor ID="+sensorID);
		if(sensorID == Constants.SENSOR_ACCELPHONEMAG){
			if(Log.DEBUG) Log.d(TAG,"Buffer length="+data.length);
			//if(Log.DEBUG) Log.d(TAG, "receiveBuffer(): got a buffer from " + sensorID+" "+data.length+" "+timestamps.length+" "+startNewData+" "+endNewData);
			addValue(data, timestamps);
		}
	}
}


