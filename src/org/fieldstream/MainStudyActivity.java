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
package org.fieldstream;

//@author Mishfaq Ahmed
//@author Patrick Blitz
//@author Somnath Mitra
//@author Andrew Raij


import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.fieldstream.gui.ema.InterviewScheduler;
import org.fieldstream.oscilloscope.ECGRIPOscilloscopeActivity;
import org.fieldstream.oscilloscope.OscilloscopeActivity;
import org.fieldstream.service.IInferrenceService;
import org.fieldstream.service.IInferrenceServiceCallback;
import org.fieldstream.service.InferrenceService;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.logger.TopExceptionHandler;
import org.fieldstream.service.sensors.mote.bluetooth.BluetoothStateSubscriber;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.PowerManager.WakeLock;

import android.text.format.Time;


import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainStudyActivity extends Activity implements BluetoothStateSubscriber {
	// EMA stuff
	private Intent emaScheduler;
	private Intent oscopeIntent;
	private Intent ecgripDemoIntent;
	
	// exit dialog stuff
	private Dialog keypad = null;
	private boolean keypadVisible = false;
	private ImageButton one;
	private ImageButton two;
	private ImageButton three;
	private ImageButton four;
	private ImageButton five;
	private ImageButton six;
	private ImageButton seven;
	private ImageButton eight;
	private ImageButton nine;
	private ImageButton zero;
	
	private Button selfReportButton;
	private Intent selfReportIntent;
	//test
	private final static String EXIT_CODE = "7556";
	private final static String OSCOPE_CODE ="6557";
	private final static String ECGRIP_DEMO_CODE ="3366";
	private String keypadCode = "";

	// inference service access
	public IInferrenceService service;
	private Intent inferenceServiceIntent;
	private IInferrenceService inferenceService;

	// state variables
	private boolean start;
	private String currentDeviceAddress = null;

	// wake lock to make sure main looper events execute
	WakeLock wakelock;
	boolean mDoDestroy = true;

	/* END USER INTERFACE DECLARATIONS */

	private IInferrenceServiceCallback inferenceCallback = new IInferrenceServiceCallback.Stub() {

		public void receiveCallback(int modelID, int value, long startTime,
				long endTime) throws RemoteException {

		}
	};
	
	
	private TextView label;
//	private TextView subjectlabel;
	private TextView endofdaylabel;
	private TextView stressorlabel;
	private TextView incentives;
	@SuppressWarnings("unused")
	private int result;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
		
		//
		//		if (Constants.DEBUG)
		//			Debug
		//					.startMethodTracing("stressInferenceFramework-MainStudyActivity");
		requestWindowFeature(Window.FEATURE_NO_TITLE);  
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
				WindowManager.LayoutParams.FLAG_FULLSCREEN);  

		setContentView(R.layout.test_layout);

		start = false;

		// grab a partial wake lock to make sure the main threads looper
		// continues running
		PowerManager pm = (PowerManager) this.getSystemService(POWER_SERVICE);
		wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
		"MainStudyActivity");
		wakelock.acquire();
//		subjectlabel=(TextView)findViewById(R.id.subjectlabel);
		stressorlabel=(TextView)findViewById(R.id.stressorlabel);
		endofdaylabel=(TextView)findViewById(R.id.eodlabel);
		label=(TextView)findViewById(R.id.Label);
		incentives=(TextView)findViewById(R.id.incentives);
		
		if (InterviewScheduler.INCENTIVE_SCHEME == InterviewScheduler.NO_INCENTIVE_SCHEME) 
			incentives.setVisibility(View.INVISIBLE);

		// read config in!
		readConfig();
		keypad = createKeypad();
		initKeypad(keypad);

		// for launching oscilloscope activity
		oscopeIntent = new Intent(getBaseContext(), OscilloscopeActivity.class);		
		ecgripDemoIntent = new Intent(getBaseContext(), ECGRIPOscilloscopeActivity.class);
		//for self reporting event
		
		selfReportButton =(Button) findViewById(R.id.selfReportButton);
		selfReportButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//System.out.println("he wants to report some event!");
				//choice c = new choice();
				//c.startActivity(intent);
			//	startActivity((Intent)c);
				selfReportIntent = new Intent(getBaseContext(),SelfReportEventActivity.class);
				startActivity(selfReportIntent);
			
			}
			
		});
		
	}

	void loadNetworkConfig() {
		File root = Environment.getExternalStorageDirectory();
		try {
			File dir = new File(root+"/"+Constants.CONFIG_DIR);
			dir.mkdirs();

			File setupFile = new File(dir, Constants.NETWORK_CONFIG_FILENAME);
			if (!setupFile.exists())
				return;
			
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document dom = builder.parse(setupFile);
            
            Element xmlroot = dom.getDocumentElement();		
            
            NodeList nodeList = xmlroot.getElementsByTagName("bridge");
            Node node = nodeList.item(0);
            currentDeviceAddress = node.getFirstChild().getNodeValue();
            Constants.moteAddress = currentDeviceAddress;

            ArrayList<Integer> sensorMotes = new ArrayList<Integer>();
            nodeList = xmlroot.getElementsByTagName("sensor_mote");
            for (int i=0; i < nodeList.getLength(); i++) {
            	node = nodeList.item(i);
            	sensorMotes.add(Integer.parseInt(node.getNodeValue()));            	
            }	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.d("MainStudyActivity",e.getMessage());
			e.printStackTrace();
		}
	}	

	long quietStart, quietEnd;
	long offStart, offEnd;
	void loadDeadPeriods() {
		File root = Environment.getExternalStorageDirectory();

		File dir = new File(root+"/"+Constants.CONFIG_DIR);
		dir.mkdirs();

		File setupFile = new File(dir, Constants.DEAD_PERIOD_CONFIG_FILENAME);
		if (!setupFile.exists())
			return;
		
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Document dom = null;
		try {
			dom = builder.parse(setupFile);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        Element xmlroot = dom.getDocumentElement();		
        
        NodeList nodeList = xmlroot.getElementsByTagName("period");
        for (int i=0; i < nodeList.getLength(); i++) {
        	Node node = nodeList.item(i);
        	NamedNodeMap map = node.getAttributes();
        	if (map != null) {
        		Node type = map.getNamedItem("type");
        		Node start = map.getNamedItem("start");
        		Node end = map.getNamedItem("end");

        		if (type != null && start != null && end != null) {
        			if (type.getNodeValue().equalsIgnoreCase("quiet")) {
        				quietStart = Long.parseLong(start.getNodeValue());
        				quietEnd = Long.parseLong(end.getNodeValue());            				
        			}
        			else if (type.getNodeValue().contentEquals("off")) {
        				offStart = Long.parseLong(start.getNodeValue());
        				offEnd = Long.parseLong(end.getNodeValue());            				            				
        			}         			
        		}            		
        	}
        }	
	}	
	
	
	private void readConfig() {
		loadNetworkConfig();
		loadDeadPeriods();
				
		emaScheduler = new Intent(getBaseContext(),InterviewScheduler.class);
		emaScheduler.putExtra(Constants.quietStart, quietStart);
		emaScheduler.putExtra(Constants.quietEnd, quietEnd);
		emaScheduler.putExtra(Constants.sleepStart, offStart);
		emaScheduler.putExtra(Constants.sleepEnd, offEnd);

		switch(InterviewScheduler.INCENTIVE_SCHEME) {
			case InterviewScheduler.UNIFORM_INCENTIVE_SCHEME:
				label.setText("u\n");
				break;
			case InterviewScheduler.VARIABLE_INCENTIVE_SCHEME:
				label.setText("v\n");
				break;
			case InterviewScheduler.HIDDEN_INCENTIVE_SCHEME:
				label.setText("h\n");
				break;
			case InterviewScheduler.NO_INCENTIVE_SCHEME:
			case InterviewScheduler.UNIFORM_AND_BONUS_INCENTIVE_SCHEME:			
				label.setText("\n");
				break;				
			default:
				label.setText("error");
				
		}
				
		stressorlabel.setText("Interview break from "+ makeTimeString(quietStart) + " until "+ makeTimeString(quietEnd) + "\n");
		endofdaylabel.setText("Data collection ends at "+makeTimeString(offStart)+", begins again at " + makeTimeString(offEnd) + "\n");

		startStuff();
		startService(emaScheduler);

		// callback to disable stress inference when the time is right!
		Log.i("onActivityResult", "started the scheduler service");
		CharSequence text = "Configuration successfully loaded";
		//label.setText("Running the study");
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(getApplicationContext(), text, duration);
		toast.show();
	}

	/* This is called when the app is killed. */
	@Override
	protected void onDestroy() {
		if (mDoDestroy) {
			stopStuff();
			if (Constants.WRITETRACE)
				Debug.stopMethodTracing();

			// release the main activity wake lock so the processor can sleep
			if (wakelock.isHeld()) {
				wakelock.release();
			}

			
			
		} else {

		}

		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		
		updateIncentivesDisplay();
		
		if (inferenceService != null)
			try {
				inferenceService.logResume(System.currentTimeMillis());
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	private void updateIncentivesDisplay() {
		if (InterviewScheduler.INCENTIVE_SCHEME == InterviewScheduler.NO_INCENTIVE_SCHEME) {
			incentives.setText("");
			return;
		}
			
		double total = 0.0;
		try {
			if (inferenceService != null)
				total = inferenceService.getTotalIncentivesEarned();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String text=NumberFormat.getCurrencyInstance().format(total);
		
		incentives.setText("So far, you've earned:\n\n" + text);
	}
	
	/* END ANDROID LIFE CYCLE */

	public void startStuff() {
		if (start)
			stopStuff();

		initService();
		
		// Create the mote sensor manager
		// MoteSensorManager.getInstance();
		
		// Create the bluetooth state manager
//		btStateManager = (BluetoothStateManager) BluetoothStateManager.getInstance();
//		btStateManager.setBridgeAddress(currentDeviceAddress);
//		btStateManager.startUp();

		start = true;
	}

	public void stopStuff() {
		if (start) {
			if (currentDeviceAddress != "No Device") {
				
				// destroy the bluetooth connection
//				btStateManager.unregisterListener(this);
//				btStateManager.stopDown();
//				btStateManager.kill();
//				btStateManager = null;
			}
//			label.setText("Stopped the study");
			start = false;

			// ema stop
			stopService(emaScheduler);
			try {
				this.disconnect();
			} catch (RemoteException e) {
				Log.w("MainStudyActivity.stopStuff()", e.getMessage());
			}
		}
	}

	//	public void createUI() {
	//		// scan Button
	//		scanButton = (Button) findViewById(R.id.scan_btdevice);
	//		scanButton.setOnClickListener(scanButtonOnClickListener);
	//
	//		// deviceSpinner
	//		deviceSpinner = (Spinner) findViewById(R.id.btdevice_spinner);
	//		deviceArrayAdapter = ArrayAdapter.createFromResource(this,
	//				R.array.device_spinner_list,
	//				android.R.layout.simple_spinner_item);
	//		deviceArrayAdapter
	//				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	//		deviceSpinner.setAdapter(deviceArrayAdapter);
	//		deviceSpinner
	//				.setOnItemSelectedListener(deviceSpinnerOnItemSelectedListener);
	//
	//		// start button
	//		startButton = (Button) findViewById(R.id.start_feature);
	//		startButton.setOnClickListener(startButtonOnClickListener);
	//
	//	}

	// Scan Button Handler
	//	private OnClickListener scanButtonOnClickListener = new OnClickListener() {
	//		// Scan Button onClick listener
	//
	//		public void onClick(View v) {
	//			pd = ProgressDialog.show(thisApp, "Scanning...",
	//					"Searching nearby bluetooth devices...");
	//			BluetoothScan.getInstance().registerListener(scanSubscriber);
	//			BluetoothScan.getInstance().scan();
	//		}
	//	};
	//
	//	private OnClickListener startButtonOnClickListener = new OnClickListener() {
	//		// Scan Button onClick listener
	//		public void onClick(View v) {
	//			if (!start) {
	//				// Start the service
	//				scanButton.setClickable(false);
	//				startStuff();
	//				startButton.setText("Stop");
	//
	//			} else {
	//				stopStuff();
	//				scanButton.setClickable(true);
	//				startButton.setText("Start");
	//			}
	//
	//		}
	//
	//	};

	//	// Device Spinner Handler
	//	@SuppressWarnings("unchecked")
	//	private Spinner.OnItemSelectedListener deviceSpinnerOnItemSelectedListener = new Spinner.OnItemSelectedListener() {
	//
	//		public void onItemSelected(AdapterView parent, View v, int position,
	//				long id) {
	//			currentDeviceAddress = parent.getSelectedItem().toString();
	//			String TAG = "deviceSpinnerOnItemSelectedListener";
	//			if (Log.DEBUG) Log.d(TAG, currentDeviceAddress);
	//		}
	//
	//		public void onNothingSelected(AdapterView parent) {
	//
	//		}
	//	};

	public String currentSubject;

	// subject chooser

	protected void connect() {

		// bindService(inferenceServiceIntent, service, 0);
		// service.subscribe(this);

	}

	public Object onRetainNonConfigurationInstance() {
		mDoDestroy = false;
		return new Object();
	}


	protected void disconnect() throws RemoteException {
		unbindService(inferenceConnection);
		stopService(inferenceServiceIntent);
		inferenceServiceIntent = null;
		inferenceConnection = null;
		inferenceCallback =  null;
	}

	private void initService() {

		// inferenceServiceIntent = new Intent();
		inferenceServiceIntent = new Intent(getBaseContext(),
				InferrenceService.class);
		// conn = new InferrenceServiceConnection();
		// Intent i = new Intent();
		// inferenceServiceIntent.setClassName( "edu.cmu.ices.stress.phone",
		//	// "edu.cmu.ices.stress.phone.service.InferrenceService" );
		startService(inferenceServiceIntent);
		bindService(inferenceServiceIntent, inferenceConnection, 0);

		Log.i("FeatureActivity", "bindService()");

	}

	private ServiceConnection inferenceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName name, IBinder service) {

			inferenceService = IInferrenceService.Stub.asInterface(service);
			Log.i("inferenceConnection", "Connected to the inference service");

			updateIncentivesDisplay();
			
			try {
				inferenceService.subscribe(inferenceCallback);
				Log.i("inferenceConnection",
				"Subscribed to the inference service callback");
				// inferenceService.activateModel(Constants.MODEL_STRESS);
				//				inferenceService.activateModel(Constants.MODEL_DATAQUALITY);
				//				inferenceService.activateModel(Constants.MODEL_CONVERSATION);
				//				inferenceService.activateModel(Constants.MODEL_ACTIVITY);
			
				
				inferenceService.logDeadPeriod(offStart,offEnd);
				
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		public void onServiceDisconnected(ComponentName name) {

			inferenceService = null;
			Log.i("inferenceConnection", "Disconnected from inference service");
//			label.setText("Stopped the study");
		}
	};
	//
	//	@Override
	//	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	//		if (resultCode == Activity.RESULT_OK && requestCode == 1) {
	//			Bundle extras = data.getExtras();
	//			int startHour = extras.getInt(Constants.stressorStart_H);
	//			int startMin = extras.getInt(Constants.stressorStart_M);
	//			int stopHour = extras.getInt(Constants.stressorStop_H);
	//			int stopMin = extras.getInt(Constants.stressorStop_M);
	//			int eodHour = extras.getInt(Constants.eod_H);
	//			int eodMin = extras.getInt(Constants.eod_M);
	//			int sodHour = extras.getInt(Constants.sod_H);
	//			int sodMin = extras.getInt(Constants.sod_M);
	//
	//			emaScheduler = new Intent(getBaseContext(),
	//					InterviewScheduler.class);
	//			// scheduler.putExtra("DEBUG", debugMode);
	//			emaScheduler.putExtra(Constants.stressorStart_H, startHour);
	//			emaScheduler.putExtra(Constants.stressorStart_M, startMin);
	//			emaScheduler.putExtra(Constants.stressorStop_H, stopHour);
	//			emaScheduler.putExtra(Constants.stressorStop_M, stopMin);
	//			emaScheduler.putExtra(Constants.eod_H, eodHour);
	//			emaScheduler.putExtra(Constants.eod_M, eodMin);
	//			emaScheduler.putExtra(Constants.sod_H, sodHour);
	//			emaScheduler.putExtra(Constants.sod_M, sodMin);
	//			startService(emaScheduler);
	//			// callback to disable stress inference when the time is right!
	//			if (Log.DEBUG) Log.d("onActivityResult", "started the scheduler service");
	//		}
	//	}

	public IBinder asBinder() {
		// TODO Auto-generated method stub
		return null;
	}

	public void onReceiveBluetoothStateUpdate(int BluetoothState) {
		// TODO Auto-generated method stub

	}

	//	@SuppressWarnings("unchecked")
	//	public void onReceiveBluetoothScanResults(ArrayList<String> devices) {
	//		pd.dismiss();
	//
	//		deviceArray = new ArrayList();
	//		for (String device : devices) {
	//			if (device.startsWith("00:A0")) {
	//				deviceArray.add(device);
	//			}
	//		}
	//
	//		// update spinner content
	//		deviceArrayAdapter = new ArrayAdapter(thisApp,
	//				android.R.layout.simple_spinner_item, deviceArray);
	//		deviceArrayAdapter
	//				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	//		deviceSpinner.setAdapter(deviceArrayAdapter);
	//		BluetoothScan.getInstance().unregisterListener(scanSubscriber);
	//		BluetoothScan.getInstance().destroy();
	//	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// disable the back button while in the interview
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_CALL) {
			// disable the call button
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_CAMERA) {
			// disable camera
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_POWER) {
			// don't know if this can be disabled here
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			// disable search
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (keypadVisible == false) {
				//keypad = createKeypad();
				//initKeypad(keypad);
				keypadCode = "";
				keypadVisible = true;
				keypad.show();
			} else {
				keypad.hide();
				keypadVisible = false;
			}

			//finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}



	private Dialog createKeypad() {
		Dialog dialog = new Dialog(this);

		dialog.setContentView(R.layout.keypad_layout);
		dialog.setTitle("Enter exit key, or press back");
		dialog.setOnKeyListener(new OnKeyListener() {

			public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
				// TODO Auto-generated method stub
				if (arg2.getKeyCode() == KeyEvent.KEYCODE_CALL) {
					return true;
				}
				if (arg2.getKeyCode() == KeyEvent.KEYCODE_CAMERA) {
					return true;
				}
				if (arg2.getKeyCode() == KeyEvent.KEYCODE_BACK) {
					if (keypadVisible) {
						keypad.hide();
						keypadVisible = false;
					}
					return true;
				}
				return false;
			}

		});

		return dialog;
	}

	private void initKeypad(Dialog dialog) {
		one = (ImageButton) dialog.findViewById(R.id.one);
		two = (ImageButton) dialog.findViewById(R.id.two);
		three = (ImageButton) dialog.findViewById(R.id.three);
		four = (ImageButton) dialog.findViewById(R.id.four);
		five = (ImageButton) dialog.findViewById(R.id.five);
		six = (ImageButton) dialog.findViewById(R.id.six);
		seven = (ImageButton) dialog.findViewById(R.id.seven);
		eight = (ImageButton) dialog.findViewById(R.id.eight);
		nine = (ImageButton) dialog.findViewById(R.id.nine);
		zero = (ImageButton) dialog.findViewById(R.id.zero);

		// set up key handlers
		one.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				keypadCode += "1";
				checkKeypadCodes();
			}

		});
		two.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				keypadCode += "2";
				checkKeypadCodes();
			}

		});
		three.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				keypadCode += "3";
				checkKeypadCodes();
			}

		});
		four.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				keypadCode += "4";
				checkKeypadCodes();
			}

		});
		five.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				keypadCode += "5";
				checkKeypadCodes();
			}

		});
		six.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				keypadCode += "6";
				checkKeypadCodes();
			}

		});
		seven.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				keypadCode += "7";
				checkKeypadCodes();
			}

		});
		eight.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				keypadCode += "8";
				checkKeypadCodes();
			}

		});
		nine.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				keypadCode += "9";
				checkKeypadCodes();
			}

		});
		zero.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				keypadCode += "0";
				checkKeypadCodes();
			}

		});

	}





	private void checkKeypadCodes() {
		
		
		if (keypadCode.length() == EXIT_CODE.length() 
				|| keypadCode.length() == OSCOPE_CODE.length()
				|| keypadCode.length() == ECGRIP_DEMO_CODE.length()) {
			if (keypadCode.equalsIgnoreCase(EXIT_CODE)) {		
				keypad.hide();
				keypadVisible = false;
				keypad.dismiss();
				keypad = null;
				finish();
			} else if (keypadCode.equalsIgnoreCase(OSCOPE_CODE)) {
				keypad.hide();
				keypadVisible = false;
				this.startActivity(oscopeIntent);    			
			} else if (keypadCode.equalsIgnoreCase(ECGRIP_DEMO_CODE)) {
				keypad.hide();
				keypadVisible = false;
				this.startActivity(ecgripDemoIntent);    				
			} else {
				keypad.hide();
				keypadVisible = false;
			}
		}
	}

	private static int format(int c) {
		if (c == 0) c = 12;

		else if (c > 12) c-=12;

		return c;
	}

	private static String pad(int c) {
		if (c >= 10)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
	}    

	private static String makeTimeString(long time) {
		Time t = new Time();
		t.set(time);
		return t.format("%D %I:%M %p");
		//	return format(t.hour) + ":" + pad(t.minute) + (t.hour >=12 ? "pm":"am");
		
	}
	
	
	
}
