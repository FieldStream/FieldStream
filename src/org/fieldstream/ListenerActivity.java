//Copyright (c) 2010, University of Memphis
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
// @author Somnath Mitra


package org.fieldstream;

import org.fieldstream.gui.ema.InterviewScheduler;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensors.mote.bluetooth.BluetoothConnectionStates;
import org.fieldstream.service.sensors.mote.bluetooth.BluetoothStateManager;
import org.fieldstream.service.sensors.mote.bluetooth.BluetoothStateSubscriber;
import org.fieldstream.service.sensors.mote.sensors.MoteSensorManager;
import org.fieldstream.service.sensors.mote.sensors.MoteUpdateSubscriber;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ListenerActivity extends Activity 
implements MoteUpdateSubscriber, BluetoothStateSubscriber, Runnable
{
	// EMA stuff
	private Intent stressorTime;
	private Intent emaScheduler;
	
	// Mote and data stuff
	private MoteSensorManager mMoteSensorManager;
	int SensorID;
	int[] data;
	
	//Bluetooth Stuff
	private BluetoothStateManager mBluetoothStateManager;
	private String BridgeAddress = "00:A0:96:28:E0:B6"; //"00:A0:96:1C:AA:80"; // 
	private long StateStartTime;
	private long StateEndTime;
	
	// UI components	
	private TextView deviceAddressTextView;	
	private EditText deviceAddressEditText;	
	private TextView logFileNameTextView;	
	private EditText logFileNameEditText;	
	private Button startButton;	
	private EditText statusEditText;	
	private TextView statusTextView;	
	private String logFileName = "log";	
	private boolean start = false;
	private TextView consoleTextView;
	private EditText consoleEditText;	
	private String statusString;
	private String consoleString;
	
	public void startStuff()
	{
		// Create the mote sensor manager
		mMoteSensorManager =(MoteSensorManager) MoteSensorManager.getInstance();
		mMoteSensorManager.registerListener(this);
		
		// Create the bluetooth state manager
		mBluetoothStateManager = (BluetoothStateManager) BluetoothStateManager.getInstance();
		mBluetoothStateManager.setBridgeAddress(BridgeAddress);
		mBluetoothStateManager.registerListener(this);
		mBluetoothStateManager.startUp();
		start = true;
		
		
		/*// init EMA stuff
		stressorTime = new Intent(getBaseContext(), SetTime.class);
		startActivityForResult(stressorTime, 1);*/
	}
	
	public void stopStuff()
	{
		// unregister the mote sensor manager
		mMoteSensorManager.unregisterListener(this);
		mMoteSensorManager = null;
		
		// destroy the bluetooth connection
		mBluetoothStateManager.unregisterListener(this);
		mBluetoothStateManager.stopDown();
		mBluetoothStateManager = null;
		
		start = false;
		
		/*// ema stop
		stopService(emaScheduler);*/
	}

	public void onReceiveData(int SensorID, int[] data, long[] timeStamps, int lastSampleNumber)
	{		
		String TAG = "ListenerActivity.onReceiveData";
		String msg1 = makeSensorIDString(SensorID);
		if (Log.DEBUG) Log.d(TAG, msg1);
		this.SensorID = SensorID;
		this.data = new int[data.length];
		System.arraycopy(data, 0, this.data, 0, data.length);		
		Thread thread = new Thread(this);
		thread.start();
		return;
	}
	
	private String makeSensorIDString(int SensorID)
	{
		String sensor = "";
		switch(SensorID)
		{
		case(Constants.SENSOR_ACCELCHESTX): 
			sensor = "ACCELCHESTX";
			break;
		
		case(Constants.SENSOR_ACCELCHESTY): 
			sensor = "ACCELCHESTY";
			break;
			
		case(Constants.SENSOR_ACCELCHESTZ): 
			sensor = "ACCELCHESTZ";
			break;
		
		case(Constants.SENSOR_ECK):
			sensor = "ECG";
			break;
		
		case(Constants.SENSOR_GSR):
			sensor = "GSR";
			break;
			
		case(Constants.SENSOR_RIP):
			sensor = "RIP";
			break;
			
		case(Constants.SENSOR_BODY_TEMP):
			sensor = "TEMP";
			break;
			
		default:	break;
		
		}
		return sensor;
	}
	
	public void createUI()
    {
    	deviceAddressTextView = (TextView) findViewById(R.id.deviceAddressTV);
    	deviceAddressEditText = (EditText) findViewById(R.id.deviceAddressET);
    	deviceAddressEditText.setText(BridgeAddress);
    	
    	logFileNameTextView = (TextView) findViewById(R.id.logFileNameTV);
    	logFileNameEditText = (EditText) findViewById(R.id.logFileNameET);
    	logFileNameEditText.setText(logFileName);
    	
    	startButton = (Button) findViewById(R.id.startButton);
    	startButton.setOnClickListener(startButtonOnClickListener);
    	
    	statusTextView = (TextView) findViewById(R.id.statusTV);
    	statusEditText = (EditText) findViewById(R.id.statusET);
    	
    	consoleTextView = (TextView) findViewById(R.id.consoleTV);
    	consoleEditText = (EditText) findViewById(R.id.consoleET);
    	
    
    }
	
	 private OnClickListener startButtonOnClickListener = new OnClickListener() 
	    {
	    	// Scan Button onClick listener
			public void onClick(View v) 
	    	{	
				if(!start)
				{
					BridgeAddress = deviceAddressEditText.getText().toString();
					startStuff();
					startButton.setText("Stop");
				}
				else
				{
					stopStuff();
					startButton.setText("Start");
				}
	    	}
					
		};
		
		 @Override
		    public void onCreate(Bundle savedInstanceState) {
		        super.onCreate(savedInstanceState);
		        setContentView(R.layout.test);
		        createUI();
		    }
		/* 
		 @Override
		 public void onActivityResult(int requestCode, int resultCode, Intent data) {
		   if (resultCode == Activity.RESULT_OK && requestCode == 1) {
		     Bundle extras = data.getExtras();
		     int startHour = extras.getInt(Constants.stressorStart_H);
		     int startMin = extras.getInt(Constants.stressorStart_M);
		     int stopHour = extras.getInt(Constants.stressorStop_H);
		     int stopMin = extras.getInt(Constants.stressorStop_M);
		     
		     emaScheduler = new Intent(getBaseContext(), InterviewScheduler.class);
		     emaScheduler.putExtra(Constants.stressorStart_H, startHour);
		     emaScheduler.putExtra(Constants.stressorStart_M, startMin);
		     emaScheduler.putExtra(Constants.stressorStop_H, stopHour);
		     emaScheduler.putExtra(Constants.stressorStop_M, stopMin);
		     startService(emaScheduler);
		   }
		 }
*/
		public synchronized void run() {
			statusString = makeSensorIDString(SensorID);
			consoleString = "";
			for(int i=0; i < data.length; i++)
				consoleString = consoleString + data[i] + " ";
			String TAG = "ListenerActivity.run";
			if (Log.DEBUG) Log.d(TAG, consoleString);
			handler.sendEmptyMessage(0);
			return;
		}
		
		 public Handler handler = new Handler() 
		 {
			 public void handleMessage(Message msg) 
			 {
				 updateET();
			 }
		 };
		 
		 public synchronized void updateET()
		 {
			 statusEditText.setText(statusString);
			 consoleEditText.setText(consoleString);
		 }

		public void onReceiveBluetoothStateUpdate(int BluetoothState) {
			statusString = "BRIDGE PROBLEM";
			handler.sendEmptyMessage(0);
			return;
		}

		
		
}

