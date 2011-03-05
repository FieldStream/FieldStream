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
// @author Andrew Raij


package org.fieldstream;





import java.text.DecimalFormat;
import java.util.ArrayList;

import org.fieldstream.service.IInferrenceService;
import org.fieldstream.service.IInferrenceServiceCallback;
import org.fieldstream.service.InferrenceService;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensor.FeatureBus;
import org.fieldstream.service.sensor.FeatureBusSubscriber;
import org.fieldstream.service.sensor.SensorBus;
import org.fieldstream.service.sensor.SensorBusSubscriber;
import org.fieldstream.service.sensors.mote.bluetooth.BluetoothStateManager;
import org.fieldstream.service.sensors.mote.bluetooth.BluetoothStateSubscriber;
import org.fieldstream.service.sensors.mote.sensors.MoteSensorManager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class FeatureActivity extends Activity
implements 
FeatureBusSubscriber, 
SensorBusSubscriber, 
Runnable, 
BluetoothStateSubscriber

{

	// note that there is no guarantee these features are active!
	// a model that users these features must be activated
	private static ArrayList<Integer> supportedFeatures = new ArrayList<Integer>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			add(Constants.getId(Constants.FEATURE_MEAN, Constants.SENSOR_ACCELCHESTZ));
			add(Constants.getId(Constants.FEATURE_MEAN, Constants.SENSOR_BODY_TEMP));
			add(Constants.getId(Constants.FEATURE_VAR, Constants.SENSOR_ACCELCHESTZ));
			add(Constants.getId(Constants.FEATURE_VAR, Constants.SENSOR_BODY_TEMP));
			add(Constants.getId(Constants.FEATURE_HR, Constants.SENSOR_VIRTUAL_RR));
		}
	};
	
	
	// inference service access
	public IInferrenceService service;
	private Intent inferenceServiceIntent;
	private IInferrenceService inferenceService;	
	
	
	//Other framework components
	private MoteSensorManager mMoteSensorManager;
	
	//Bluetooth Stuff
	private BluetoothStateManager mBluetoothStateManager;
	private String BridgeAddress = "00:A0:96:28:E0:B6"; //"00:A0:96:1C:AA:80"; // 

	Boolean featureUpdated;
	Boolean sensorUpdated;
	
	// Adapters
	private ProgressDialog pd;
	@SuppressWarnings("unchecked")
	private ArrayList 	deviceArray;
	@SuppressWarnings("unchecked")
	private ArrayAdapter deviceArrayAdapter;
	@SuppressWarnings("unchecked")
	private ArrayAdapter<String> featureArrayAdapter;
	
	private int numSampleBuffers;
	
	//UI components
	private Button 		scanButton;
	private Spinner  	deviceSpinner;
		
	private TextView 	featureSelectTextView;
	private Spinner 	featureSpinner;
	
	private Button		startButton;
	private TextView 	featureConsoleTextView;
	private EditText	featureConsoleEditText;
	private TextView 	sensorConsoleTextView;
	private EditText	sensorConsoleEditText;
	
	private DecimalFormat myFormatter = new DecimalFormat("###.##");
	
	//state variables
	private boolean start;
	private final Activity thisApp = this;
	private String currentDeviceAddress = null;
	private int currentFeature = 0;
	private int currentSensor = 0;
	private int currentFeatureSensor = 0;
	private String featureConsoleString;	
	private String sensorConsoleString;
	int[] sensorData = null;
	double featureData;
	
	int numFeaturesSeen;
	
	
	
   // END USER INTERFACE DECLARATIONS 

	private IInferrenceServiceCallback inferenceCallback = new IInferrenceServiceCallback.Stub() {
		
		public void receiveCallback(int modelID, int value, long startTime, long endTime) throws RemoteException {
			
//			processContext(value); //(((Integer)value).toString());
			if (Log.DEBUG) Log.d("receivedCallback", "context value received: " + value);
		}
	};
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Constants.WRITETRACE) 
			Debug.startMethodTracing("stressInferenceFramework-FeatureActivity");
					
		numSampleBuffers = 0;
		sensorUpdated = false;
		featureUpdated = false;
		super.onCreate(savedInstanceState);
        setContentView(R.layout.features);
        createUI();
        
        numFeaturesSeen = 0;
        
        start = false;
       
    }

	// This is called when the app is killed. 
	@Override
	protected void onDestroy() {
		stopStuff();
		if (Constants.WRITETRACE) 
			Debug.stopMethodTracing();
		super.onDestroy();
	}
	// END ANDROID LIFE CYCLE 
	
	
	public void startStuff()
	{
		if (start)
			stopStuff();

		if (currentDeviceAddress != "No Device") {
			BridgeAddress = currentDeviceAddress; // "00:0A:3A:84:97:AA"; //

			// Create the mote sensor manager
			mMoteSensorManager = (MoteSensorManager) MoteSensorManager
					.getInstance();

			// Create the bluetooth state manager
			mBluetoothStateManager = (BluetoothStateManager) BluetoothStateManager
					.getInstance();
			mBluetoothStateManager.setBridgeAddress(BridgeAddress);
			mBluetoothStateManager.registerListener(this);
			mBluetoothStateManager.startUp();

			// init EMA stuff
		//	stressorTime = new Intent(getBaseContext(), SetTime.class);
		//	startActivityForResult(stressorTime, 1);
		}
		
		initService();
		
		SensorBus.getInstance().subscribe(this);
		FeatureBus.getInstance().subscribe(this);
		
		start = true;
		if (Log.DEBUG) Log.d("startStuff","service init done");
	}
	
	public void stopStuff()
	{
		if (start) {
			if (currentDeviceAddress != "No Device") {
				// unregister the mote sensor manager
				mMoteSensorManager = null;

				// destroy the bluetooth connection
				mBluetoothStateManager.unregisterListener(this);
				mBluetoothStateManager.stopDown();
				mBluetoothStateManager.kill();
				mBluetoothStateManager = null;
			}
			start = false;
			
			SensorBus.getInstance().unsubscribe(this);
			FeatureBus.getInstance().unsubscribe(this);
		}		
	}
	
	public void createUI()
	{
		//scan Button
		scanButton = (Button) findViewById(R.id.scan_btdevice);
		scanButton.setOnClickListener(scanButtonOnClickListener);
		
		 // deviceSpinner
		deviceSpinner = (Spinner) findViewById(R.id.btdevice_spinner);
		deviceArrayAdapter = ArrayAdapter.createFromResource(this, R.array.device_spinner_list, android.R.layout.simple_spinner_item);
        deviceArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deviceSpinner.setAdapter(deviceArrayAdapter);        
        deviceSpinner.setOnItemSelectedListener(deviceSpinnerOnItemSelectedListener);
          
        //"Select a feature" text label
		featureSelectTextView = (TextView) findViewById(R.id.select_feature);
		
		//feature spinner
		featureSpinner = (Spinner) findViewById(R.id.feature_spinner);
		featureArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);		
//		featureArrayAdapter = ArrayAdapter.createFromResource(this, R.array.feature_spinner_list, android.R.layout.simple_spinner_item);
        featureArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        featureSpinner.setAdapter(featureArrayAdapter);        
        
        for (int i = 0; i < supportedFeatures.size(); i++) {
        	
    		int feature = Constants.parseFeatureId(supportedFeatures.get(i));
    		int sensor = Constants.parseSensorId(supportedFeatures.get(i));

        	if (i==0) {
        		currentFeature = feature;
        		currentSensor = sensor;
        	}    		
    		
    		String featureDesc = Constants.getFeatureDescription(feature);
    		String sensorDesc = Constants.getSensorDescription(sensor);	    			

        	featureArrayAdapter.add(featureDesc + " " + sensorDesc);
        }
        
        featureSpinner.setOnItemSelectedListener(featureSpinnerOnItemSelectedListener);
             
        //start button
		startButton = (Button) findViewById(R.id.start_feature);
		startButton.setOnClickListener(startButtonOnClickListener);
		
		// feature console label 
		featureConsoleTextView = (TextView) findViewById(R.id.feature_values);
		
		// feature console window
		featureConsoleEditText = (EditText) findViewById(R.id.feature_values_console);
		
		// sensor console label
		sensorConsoleTextView = (TextView) findViewById(R.id.sensor_values);
		
		// sensor console window
		sensorConsoleEditText = (EditText) findViewById(R.id.sensor_values_console);
	}
    
//	private BluetoothScanSubscriber scanSubscriber = this;
	// Scan Button Handler
	 private OnClickListener scanButtonOnClickListener = new OnClickListener() 
	    {
	    	// Scan Button onClick listener
		 @SuppressWarnings("unchecked")
		public void onClick(View v) 
	    	{
			 pd = ProgressDialog.show(thisApp, "Scanning...", "Searching nearby bluetooth devices...");
//	    	 BluetoothScan.getInstance().registerListener(scanSubscriber);
//	    	 BluetoothScan.getInstance().scan();

	    	}
	    };
	    
	    private OnClickListener startButtonOnClickListener = new OnClickListener() 
	    {
	    	// Scan Button onClick listener
			public void onClick(View v) 
	    	{	
				if(!start)
				{
					//Start the service
					scanButton.setClickable(false);
					startStuff();
				   	startButton.setText("Stop");
					
			     }
				else
				{
					stopStuff();
					scanButton.setClickable(true);
					startButton.setText("Start");
				}
					
			}
			
	    };
	
	 // Device Spinner Handler
	    @SuppressWarnings("unchecked")
    private Spinner.OnItemSelectedListener deviceSpinnerOnItemSelectedListener = new Spinner.OnItemSelectedListener() 
    {
    	
		public void onItemSelected(AdapterView parent, View v, int position, long id)
    	{
    		currentDeviceAddress = parent.getSelectedItem().toString();
    		String TAG = "deviceSpinnerOnItemSelectedListener";
    		if (Log.DEBUG) Log.d(TAG, currentDeviceAddress);
    	}
    	
    	public void onNothingSelected(AdapterView parent) 
    	{ 
    		
    	}
	};
	
	 // Feature Spinner Handler
	  @SuppressWarnings("unchecked")
    private Spinner.OnItemSelectedListener featureSpinnerOnItemSelectedListener = new Spinner.OnItemSelectedListener() 
    {
    	public void onItemSelected(AdapterView parent, View v, int position, long id)
    	{
			int newFeatureSensor = supportedFeatures.get(position);    		
			if (currentFeatureSensor != newFeatureSensor)   { 		
				currentFeatureSensor = newFeatureSensor;
	    		featureConsoleString = "";
	    		sensorConsoleString = "";

	    		currentFeature = Constants.parseFeatureId(currentFeatureSensor);
	    		currentSensor = Constants.parseSensorId(currentFeatureSensor);
	    		
	    		String featureDesc = Constants.getFeatureDescription(currentFeature);
	    		String sensorDesc = Constants.getSensorDescription(currentSensor);	    			
    			featureConsoleTextView.setText(featureDesc + " " + sensorDesc);
    			sensorConsoleTextView.setText(sensorDesc);
    			
    			updateET();
			}    		

    		
    	}
    	
    	public void onNothingSelected(AdapterView parent) 
    	{ 
    		
    	}
	};
			 
	 public void run() {
		if (sensorUpdated) {
			sensorConsoleString = "";
			for(int i=0; i < sensorData.length/100; i++)
				sensorConsoleString = sensorConsoleString + sensorData[i] + " ";
			sensorUpdated = false;
		}
		
		if (featureUpdated) {
			if (featureConsoleString.length() > 256) 
				featureConsoleString = "";
			String output = myFormatter.format(this.featureData);
			featureConsoleString = output; // + "\n" + featureConsoleString;
			featureUpdated = false;
		}
				
		 handler.sendEmptyMessageDelayed(0, 1000);
	 }
	 
	 
	 public Handler handler = new Handler() {
		 public void handleMessage(Message msg) {
			 updateET();
		 }
	 };
	 
	 public void updateET() {
		 sensorConsoleEditText.setText(sensorConsoleString);
		 featureConsoleEditText.setText(featureConsoleString);
	 }
	 
	public void receiveUpdate(int featureID, double result, long beginTime,
			long endTime) {

		if (featureID == currentFeatureSensor) {

			this.featureData = result;
			featureUpdated = true;
			Thread thread = new Thread(this);
			thread.start();

			numFeaturesSeen++;	
		}
	

	}		
		

	public void receiveBuffer(int sensorID, int[] data, long[] timestamps,
			int startNewData, int endNewData) {
		
		if (sensorID == currentSensor) {
			sensorUpdated = true;
			this.sensorData = null;
			this.sensorData = new int[data.length];
			System.arraycopy(data, 0, this.sensorData, 0, data.length);		
			
			Thread thread = new Thread(this);
			thread.start();
		}

		numSampleBuffers++;

	}		
		

	
	
	
	
	

protected void disconnect() throws RemoteException {
	unbindService(inferenceConnection);
	stopService(inferenceServiceIntent);
	finish();
}
private void initService() {

	    inferenceServiceIntent = new Intent(getBaseContext(), InferrenceService.class);
	    startService(inferenceServiceIntent);
	    bindService(inferenceServiceIntent, inferenceConnection,0);
	    
		if (Log.DEBUG) Log.d( "FeatureActivity", "bindService()" );

}
	
private ServiceConnection inferenceConnection = new ServiceConnection() {

	

	public void onServiceConnected(ComponentName name, IBinder service) {
		
		inferenceService = IInferrenceService.Stub.asInterface(service);
		if (Log.DEBUG) Log.d("inferenceConnection", "Connected to the inference service");
		
		try {
			inferenceService.subscribe(inferenceCallback);
			if (Log.DEBUG) Log.d("inferenceConnection", "Subscribed to the inference service callback");
			inferenceService.activateModel(Constants.MODEL_TEST);
			inferenceService.activateModel(Constants.MODEL_STRESS_OLD);
			inferenceService.activateModel(Constants.MODEL_ACTIVITY);
			inferenceService.activateModel(Constants.MODEL_CONVERSATION);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void onServiceDisconnected(ComponentName name) {
		
		inferenceService = null;
		if (Log.DEBUG) Log.d("inferenceConnection", "Disconnected from inference service");
	}
};


	public IBinder asBinder() {
		// TODO Auto-generated method stub
		return null;
	}

	public void onReceiveBluetoothStateUpdate(int BluetoothState) {
		// TODO Auto-generated method stub
		
	}

		@SuppressWarnings("unchecked")
		public void onReceiveBluetoothScanResults(ArrayList<String> devices) {
			pd.dismiss();
			
			deviceArray = new ArrayList(); 
	        for (String device: devices) {
	        	if (device.startsWith("00:A0")) {
	        		deviceArray.add(device);
	        	}
	        }
			
			// update spinner content
			deviceArrayAdapter = new ArrayAdapter(thisApp, android.R.layout.simple_spinner_item, deviceArray);
	        deviceArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	   		deviceSpinner.setAdapter(deviceArrayAdapter);
//	    	BluetoothScan.getInstance().unregisterListener(scanSubscriber);
//			BluetoothScan.getInstance().destroy();
		}

}
