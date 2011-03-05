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

// @author Smriti Kumar
// @author Andrew Raij


package org.fieldstream;

// DInitial setup
// Dscreen always on
// Dturn on sensors (but nothing else)
// Drequest ... once every X minutes
// Dlog to db
// Way to mark start of video sessions
// mark end of video sessions on quit
// add oscilloscope back in
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensors.mote.bluetooth.BluetoothStateSubscriber;
import org.fieldstream.service.sensors.mote.sensors.MoteSensorManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.Color;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.Vibrator;
import android.os.PowerManager.WakeLock;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class hsslider extends Activity implements BluetoothStateSubscriber {
	
	SeekBar hSeekBar, sSeekBar  ;
	int i;
	TextView hProgressText, sProgressText;
    TextView hTrackingText, sTrackingText;
	TextView texth, texts;
//	TextView  texti;
	TextView text1,text2,text3,text4,text5,text6;
	TextView text7,text8,text9,text10,text11,text12;
	TextView l1,l2,l3,l4,l5,l6,l7,l8;
	RelativeLayout mScreen;
	boolean first = true;
	
	// keypad stuff
	private Intent oscopeIntent;
	private Intent ecgripDemoIntent;	
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

	/* END USER INTERFACE DECLARATIONS */

	private IInferrenceServiceCallback inferenceCallback = new IInferrenceServiceCallback.Stub() {

		public void receiveCallback(int modelID, int value, long startTime,
				long endTime) throws RemoteException {

		}
	};
	
	
	// wake lock to make sure main looper events execute
	WakeLock wakelock;
	boolean mDoDestroy = true;	
	
	DatabaseLogger db;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);  
        setContentView(R.layout.hsslider);
  //      String str = (String) getLastNonConfigurationInstance();

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
				WindowManager.LayoutParams.FLAG_FULLSCREEN);  
        
		start = false;

		// grab a partial wake lock to make sure the main threads looper
		// continues running
		PowerManager pm = (PowerManager) this.getSystemService(POWER_SERVICE);
		wakelock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
		"hsslider");
		wakelock.acquire();
        
        mScreen = (RelativeLayout) findViewById(R.id.myScreen);
//        mScreen.setBackgroundColor(Color.GRAY);
        hSeekBar = (SeekBar)findViewById(R.id.HappySlider);
        hProgressText = (TextView)findViewById(R.id.progress1);
        hTrackingText = (TextView)findViewById(R.id.tracking1);
        
        sSeekBar = (SeekBar)findViewById(R.id.SadSlider);
        sProgressText = (TextView)findViewById(R.id.progress2);
        sTrackingText = (TextView)findViewById(R.id.tracking2);
       
        
        hSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
       sSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
           
 //       texti=(TextView) null;
        texth=(TextView)findViewById(R.id.Happy);
//        texth.setTextColor(Color.BLUE);
        text1=(TextView)findViewById(R.id.YES1);
//        text1.setTextColor(Color.BLUE);
        text2=(TextView)findViewById(R.id.YES2);
//        text2.setTextColor(Color.BLUE);
        text3=(TextView)findViewById(R.id.yes1);
//        text3.setTextColor(Color.BLUE);
        text4=(TextView)findViewById(R.id.no1);
//        text4.setTextColor(Color.BLUE);
        text5=(TextView)findViewById(R.id.NO1);
//        text5.setTextColor(Color.BLUE);
        text6=(TextView)findViewById(R.id.NO2);
//        text6.setTextColor(Color.BLUE);
        
        texts=(TextView)findViewById(R.id.Sad);
//        texts.setTextColor(Color.BLUE);
        text7=(TextView)findViewById(R.id.YES3);
//        text7.setTextColor(Color.BLUE);
       
            text8=(TextView)findViewById(R.id.YES4);
//            text8.setTextColor(Color.BLUE);

            text9=(TextView)findViewById(R.id.yes2);
//            text9.setTextColor(Color.BLUE);
            text10=(TextView)findViewById(R.id.no2);
//            text10.setTextColor(Color.BLUE);
            text11=(TextView)findViewById(R.id.NO3);
//            text11.setTextColor(Color.BLUE);
            text12=(TextView)findViewById(R.id.NO4);
//            text12.setTextColor(Color.BLUE);
        l1=(TextView)findViewById(R.id.line1);
        l1.setTextColor(Color.BLACK);
        l2=(TextView)findViewById(R.id.line2);
        l2.setTextColor(Color.BLACK);
        l3=(TextView)findViewById(R.id.line3);
        l3.setTextColor(Color.BLACK);
        l4=(TextView)findViewById(R.id.line4);
        l4.setTextColor(Color.BLACK);
        l5=(TextView)findViewById(R.id.line5);
        l5.setTextColor(Color.BLACK);
        l6=(TextView)findViewById(R.id.line6);
        l6.setTextColor(Color.BLACK);
        l7=(TextView)findViewById(R.id.line7);
        l7.setTextColor(Color.BLACK);
        l8=(TextView)findViewById(R.id.line8);
        l8.setTextColor(Color.BLACK);
        
       
		// read config in!
		readConfig();
		keypad = createKeypad();
		initKeypad(keypad);           

		oscopeIntent = new Intent(getBaseContext(), OscilloscopeActivity.class);		
		ecgripDemoIntent = new Intent(getBaseContext(), ECGRIPOscilloscopeActivity.class);				
		
		db = DatabaseLogger.getInstance(this);

		showDialog(0);
    }
    
    
    protected Dialog onCreateDialog(int id) {
        final CharSequence[] items = {"Practice", "Neutral", "Happy", "Sad", "Smoking"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a Session");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
            	if (item == 0) {
                	db.logAnything("session", "start practice", System.currentTimeMillis());            		
            	}
            	else if (item == 1) {
                	db.logAnything("session", "start neutral video", System.currentTimeMillis());            		
            	}
            	else if (item == 2) {
                	db.logAnything("session", "start amusement video", System.currentTimeMillis());            		
            	}
            	else if (item == 3){
                	db.logAnything("session", "start sadness video", System.currentTimeMillis());            		
            	}
            	else if (item == 4){
                	db.logAnything("session", "start smoking", System.currentTimeMillis());            		
            	}            	
        		handler.postDelayed(triggerRequest, 10000);		
            }
        });
        AlertDialog alert = builder.create();        
        
        return alert;
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

	
	
	private void readConfig() {
		loadNetworkConfig();
				
		startStuff();

		CharSequence text = "Configuration successfully loaded";
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
			
	        handler.removeCallbacks(triggerRequest);
		} else {

		}
		
    	db.logAnything("session", "end", System.currentTimeMillis());
		
		DatabaseLogger.releaseInstance(this);
		db = null;
		
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		
		if (inferenceService != null)
			try {
				inferenceService.logResume(System.currentTimeMillis());
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
    

	public void startStuff() {
		if (start)
			stopStuff();

		initService();
		
		// Create the mote sensor manager
		MoteSensorManager.getInstance();
		
		start = true;
	}

	public void stopStuff() {
		if (start) {
			start = false;

			try {
				this.disconnect();
			} catch (RemoteException e) {
				Log.w("MainStudyActivity.stopStuff()", e.getMessage());
			}
		}
	}
	
	protected void connect() {

		// bindService(inferenceServiceIntent, service, 0);
		// service.subscribe(this);

	}

	public Object onRetainNonConfigurationInstance() {
		mDoDestroy = false;
		return new Object();
	}


	protected void disconnect() throws RemoteException {
		inferenceService.deactivateSensor(Constants.SENSOR_ACCELCHESTX);
		inferenceService.deactivateSensor(Constants.SENSOR_ACCELCHESTY);				
		inferenceService.deactivateSensor(Constants.SENSOR_ACCELCHESTZ);
		inferenceService.deactivateSensor(Constants.SENSOR_ACCELPHONEX);				
		inferenceService.deactivateSensor(Constants.SENSOR_ACCELPHONEY);				
		inferenceService.deactivateSensor(Constants.SENSOR_ACCELPHONEZ);
		inferenceService.deactivateSensor(Constants.SENSOR_AMBIENT_TEMP);				
		inferenceService.deactivateSensor(Constants.SENSOR_BODY_TEMP);				
		inferenceService.deactivateSensor(Constants.SENSOR_BATTERY_LEVEL);				
		inferenceService.deactivateSensor(Constants.SENSOR_ECK);				
		inferenceService.deactivateSensor(Constants.SENSOR_GSR);								
		inferenceService.deactivateSensor(Constants.SENSOR_RIP);

		inferenceService.unsubscribe(inferenceCallback);		
		
		unbindService(inferenceConnection);
		stopService(inferenceServiceIntent);
		inferenceServiceIntent = null;
		inferenceConnection = null;
		inferenceCallback =  null;
		
		Log.d("hsslider", "disconnected from service");
	}

	private void initService() {

		// inferenceServiceIntent = new Intent();
		inferenceServiceIntent = new Intent(getBaseContext(),
				InferrenceService.class);
		startService(inferenceServiceIntent);
		bindService(inferenceServiceIntent, inferenceConnection, 0);

		Log.i("FeatureActivity", "bindService()");
	}


	private ServiceConnection inferenceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName name, IBinder service) {

			inferenceService = IInferrenceService.Stub.asInterface(service);
			Log.i("inferenceConnection", "Connected to the inference service");

			try {
				inferenceService.subscribe(inferenceCallback);
				
				inferenceService.activateSensor(Constants.SENSOR_ACCELCHESTX);
				inferenceService.activateSensor(Constants.SENSOR_ACCELCHESTY);				
				inferenceService.activateSensor(Constants.SENSOR_ACCELCHESTZ);
				inferenceService.activateSensor(Constants.SENSOR_ACCELPHONEX);				
				inferenceService.activateSensor(Constants.SENSOR_ACCELPHONEY);				
				inferenceService.activateSensor(Constants.SENSOR_ACCELPHONEZ);
				inferenceService.activateSensor(Constants.SENSOR_AMBIENT_TEMP);				
				inferenceService.activateSensor(Constants.SENSOR_BODY_TEMP);				
				inferenceService.activateSensor(Constants.SENSOR_BATTERY_LEVEL);				
				inferenceService.activateSensor(Constants.SENSOR_ECK);				
				inferenceService.activateSensor(Constants.SENSOR_GSR);								
				inferenceService.activateSensor(Constants.SENSOR_RIP);
				
				Log.i("inferenceConnection",
				"Subscribed to the inference service callback");
				// inferenceService.activateModel(Constants.MODEL_STRESS);
				//				inferenceService.activateModel(Constants.MODEL_DATAQUALITY);
				//				inferenceService.activateModel(Constants.MODEL_CONVERSATION);
				//				inferenceService.activateModel(Constants.MODEL_ACTIVITY);
				inferenceService.activateModel(Constants.MODEL_ACCELCOMMUTING);
			
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
	
	public IBinder asBinder() {
		// TODO Auto-generated method stub
		return null;
	}

	public void onReceiveBluetoothStateUpdate(int BluetoothState) {
		// TODO Auto-generated method stub

	}

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

	
	
    private SeekBar.OnSeekBarChangeListener seekBarChangeListener
    = new SeekBar.OnSeekBarChangeListener()
    {

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
//    	if (fromTouch) {
//	        handler.removeCallbacks(triggerRequest);
//	        handler.postDelayed(triggerRequest, TIME_UNTIL_REQUEST_LONG_MS);    	
//	    	
//	    	calculatevalue(seekBar);
//	    	
////	    	hProgressText.setText(progessvalue1 + " " + 
////	                getString(R.string.seekbar_from_touch) + "=" + "True");
////	    	hProgressText.setTextColor(Color.BLUE);
////	    	 sProgressText.setText(progessvalue2 + " " + 
////	                 getString(R.string.seekbar_from_touch) + "=" + "True") ;
////	    	 sProgressText.setTextColor(Color.BLUE);
//    	}      
    }
       
    public void onStartTrackingTouch(SeekBar seekBar) {
//        hTrackingText.setText(getString(R.string.seekbar_tracking_on));
//        sTrackingText.setText(getString(R.string.seekbar_tracking_on));
        
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
//        hTrackingText.setText(getString(R.string.seekbar_tracking_off));
//        sTrackingText.setText(getString(R.string.seekbar_tracking_off));       
        
		first = false;
    	
        handler.removeCallbacks(triggerRequest);
        handler.postDelayed(triggerRequest, TIME_UNTIL_REQUEST_LONG_MS);    	    	
    	calculatevalue(seekBar);
        
    }
    
    
};


private static long TIME_UNTIL_REQUEST_LONG_MS = 2 * 60 * 1000;
private static long TIME_UNTIL_REQUEST_SHORT_MS = 30 * 1000;

private Handler handler = new Handler();
private Runnable triggerRequest = new Runnable() {
	   public void run() {	     
       		db.logAnything("buzz", "buzz", System.currentTimeMillis());            		
       	
			// vibrate and beep to alert the user to a new interview
			Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
			ToneGenerator tone = new ToneGenerator(android.media.AudioManager.STREAM_NOTIFICATION, 100);

			vibrator.vibrate(1000);
			tone.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP);

			String text = "";
			if (first) {
				text = "Please rate your emotions now.";		
				first = false;
			}
			else {
				text = "Have your emotions changed?";
			}
			
			Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
			toast.show();
			
		    handler.postDelayed(triggerRequest, TIME_UNTIL_REQUEST_LONG_MS);			
	   }
	};

    private void calculatevalue(SeekBar seekBar)
    {    
    	Log.d("hsslider","calculatevalue()");
    	int progessvalue=seekBar.getProgress();
    	
    	
    	 if(progessvalue<=12) 
		     	  progessvalue=0;
    	     	 
         if(progessvalue>12 & progessvalue<=30) 
          progessvalue=20; 	
        
         
         if(progessvalue>30 & progessvalue<=50)  
          progessvalue=40;
         
         
         if(progessvalue>50 & progessvalue<=70) 
          progessvalue=60;
         
         
         if(progessvalue>70 & progessvalue<=90) 
         	progessvalue=80;
        
         if(progessvalue>90) 
          progessvalue=100;
                 
         seekBar.setProgress(progessvalue);
         

	    // scale from 0 = NO!!! to 5 = YES!!!
        if (seekBar == this.hSeekBar)
        	db.logAnything("lab_self_report_happy", Integer.toString(progessvalue/20), System.currentTimeMillis());             
        else 
        	db.logAnything("lab_self_report_sad", Integer.toString(progessvalue/20), System.currentTimeMillis());    	        	
        
    }
    


}
