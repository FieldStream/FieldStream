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

// @author Andrew Raij

/**
 * 
 */
package org.fieldstream;


import org.fieldstream.service.IInferrenceService;
import org.fieldstream.service.IInferrenceServiceCallback;
import org.fieldstream.service.InferrenceService;
import org.fieldstream.service.context.model.DataQualityCalculation;
import org.fieldstream.service.logger.Log;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.app.KeyguardManager.KeyguardLock;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Vibrator;
import android.os.PowerManager.WakeLock;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author Andrew Raij
 * TODO add picture of correct wearing.
 */
public class FixBandProblemActivity extends Activity   {

	private static final String[] eckWizardStrings = {
		"Did you take the electrodes off?  If you didn't, we think there could be a problem with them.  Let's try a few things to see if we can fix them.\n\nSelect next when you are ready.",
		"If the electrodes are not sticking well to your chest, disconnect the black cords from the electrodes and throw the electrodes away.  Clean the place where the electrodes were using the adhesive swabs we gave you. \n\nGet two new electrodes, peel their adhesives, and stick them on the same places on your skin where the old ones were.\n\nSelect Next if the electrodes are attached properly to your chest.",
		"Now, check the two black cords extending from one of the pouches your are wearing.  One will have a + sign on it, and the other will have a - sign.  Make sure the cord with the - sign is connected to the electrode on your left, and the cord with the + sign is connected to the other electrode.\n\nYou should hear a snap when they are attached properly.  Select Next if the cords are attached to the electrodes.",
	};
	
	private static final String[] ripWizardStrings = {
		"Did you take the blue band off?  If you didn't, we think there could be a problem with it.\n\nLet's try a few things to see if we can fix it.\n\nSelect next when you are ready.",
		"The blue band is worn under your shirt, directly against your skin.  It has a buckle that snaps the band together and holds it to your chest.  To the immediate left and right of the buckle, you should see two black holes. Make sure these holes face down (towards your feet) and the buckle is in the center of your chest. The band should go about your upper chest, just below your armpits.\n\nSelect Next when the blue band is on your chest and positioned correctly.",
		"Now tighten the band so that it does not slip or move around.\n\nSelect Next when the band is secured tightly around your chest.",
		"Now, find the cord which has two long connectors on the end of it, and plug each connector into a hole on the bottom of the buckle.  It does not matter which connector goes into which hole.\n\nSelect Next when the connectors are plugged in to the blue band.",
	};	

	private static final String[] endWizardStrings = {
		"It looks like the steps you took fixed the problem.  Thanks for your help!\n\nThis screen will disappear shortly.",
		"It appears the problem is still not fixed.  Select Next to try the instructions again.",
		"The problem isn't fixed yet, but maybe we'll try again later.  Thanks for your help!\n\nThis screen will disappear shortly."
	};
	
	// UI elements
	private TextView textView;
	private Button nextButton;
	private Button backButton;
	private ProgressDialog pd;
	
	// booleans indicating which wizards are active
	private Boolean eckWizardActive;
	private Boolean ripWizardActive;
	private Boolean endWizardActive;
	
	// which screen of the wizard is active
	int currentScreen;
	
	// while on the end screen of the wizard, the system will do MAX_PASSES before 
	// evaluating the data quality again to decide if we should repeat the wizard
	private int pass;
	private final static int MAX_PASSES = 2;
		
	// if poor data quality continues, the wizard will be 
	// repeated until MAX_TIMES_WIZARD_REPEATED.
	private int numTimesWizardRepeated;
	private final static int MAX_TIMES_WIZARD_REPEATED = 1;

	private KeyguardLock keyguard;
	private WakeLock wakelock;
	private static final Boolean debugMode = false;
	private static final int VOLUME = 100;
	
	
	private int currentDataQuality;
	
	// inference service access
	public IInferrenceService service;
	private Intent inferenceServiceIntent;
	private IInferrenceService inferenceService;	
	
	private IInferrenceServiceCallback inferenceCallback = new IInferrenceServiceCallback.Stub() {
		
		public void receiveCallback(int modelID, int value, long startTime, long endTime) throws RemoteException {
			if (modelID == Constants.MODEL_DATAQUALITY) {
				currentDataQuality = value;
				startUpdateThread();
			}
		}
	};	
	
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
					
		super.onCreate(savedInstanceState);
        setContentView(R.layout.fix_band_problem_layout);
        createUI();
        
		eckWizardActive = false;
		ripWizardActive = false;
		endWizardActive = false;
		pass = 0;
		numTimesWizardRepeated = 0;
	
		initService();

		KeyguardManager mgr = (KeyguardManager)this.getSystemService(KEYGUARD_SERVICE);
		keyguard = mgr.newKeyguardLock("FixBandProblemActivity");
			
		PowerManager pm = (PowerManager)this.getSystemService(POWER_SERVICE);
		//wakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "AbstractInterview");
		wakelock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "FixBandProblemActivity");		

		if (Constants.BUZZ) {
	    	// vibrate and beep to alert the user they have to fix the bands
	    	Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
	    	//ToneGenerator tone = new ToneGenerator(android.media.AudioManager.STREAM_NOTIFICATION, VOLUME);
	    	vibrator.vibrate(500);
	    	try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	vibrator.vibrate(500);
	    	try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	vibrator.vibrate(500);
		}    	

		Bundle extras = getIntent().getExtras();
		currentDataQuality = extras.getInt("edu.cmu.ices.stress.phone.FixBandProblemActivity.currentDataQuality"); 
//		currentDataQuality = DataQualityCalculation.DATA_QUALITY_BAND_OFF + 10 * DataQualityCalculation.DATA_QUALITY_BAND_OFF;    // for testing
		processDQUpdate();		
		
		if (Log.DEBUG) Log.d("FixBandProblemActivity", "Done with initialization");
    }

	// override the keydown event listener to capture keypresses
	// disables all keys except HOME, END and POWER. END is handled by
	// changing the system settings for end button behavior, home and 
	// power need to be researched...
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	        // disable the back button while in the interview
	    	if (!debugMode)
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
	    if (keyCode == KeyEvent.KEYCODE_HOME) {
	    	// disable home
	    	return true;
	    }	    
	    return super.onKeyDown(keyCode, event);
	}
		
	/* This is called when the app is killed. */
	@Override
	protected void onDestroy() {
		if (wakelock != null) {
			if (wakelock.isHeld()) {
				wakelock.release();
			}
		}
		
		super.onDestroy();
	}
	/* END ANDROID LIFE CYCLE */
	
	private void createUI() {
		textView = (TextView) findViewById(R.id.Instructions);

		nextButton = (Button) findViewById(R.id.FixBandNextButton);
		nextButton.setOnClickListener(nextButtonOnClickListener);
		
		backButton = (Button) findViewById(R.id.FixBandBackButton);
	    backButton.setOnClickListener(backButtonOnClickListener);
	}

	 private OnClickListener nextButtonOnClickListener = new OnClickListener() {
		public void onClick(View v) {
			if (eckWizardActive) {
	    		nextScreenECKWizard();
	    	}
	    	else if (ripWizardActive) {
	    		nextScreenRIPWizard();
	    	}
	    }
	 };

	 private OnClickListener backButtonOnClickListener = new OnClickListener() {
			public void onClick(View v) {
		    	if (eckWizardActive) {
		    		prevScreenECKWizard();
		    	}
		    	else if (ripWizardActive) {
		    		prevScreenRIPWizard();
		    	}
		    }
		 };	
	
	private void startECKWizard() {
		if (Log.DEBUG) Log.d("FixBandProblemActivity", "Start ECK Wizard");
		eckWizardActive = true;
		currentScreen = 0;
		this.backButton.setVisibility(View.INVISIBLE);
		this.nextButton.setVisibility(View.VISIBLE);
		textView.setText(eckWizardStrings[currentScreen]);
		
		
	}

	private void nextScreenECKWizard() {
		if (Log.DEBUG) Log.d("FixBandProblemActivity", "Next Screen ECK Wizard");		
		currentScreen++;		
		this.backButton.setVisibility(View.VISIBLE);
		if (currentScreen < eckWizardStrings.length)
			textView.setText(eckWizardStrings[currentScreen]);
		else {
			endWizardActive = true;
    		textView.setText("");
			pd = ProgressDialog.show(this, "Checking...", "Please wait while we check the health of the system...");
			this.nextButton.setVisibility(View.INVISIBLE);
			this.backButton.setVisibility(View.INVISIBLE);
		}		
	}
	
	private void prevScreenECKWizard() {	
		if (Log.DEBUG) Log.d("FixBandProblemActivity", "Prev Screen ECK Wizard");
		// currentScreen != 0 && currentScreen != eckWizardStrings.length && endWizardActive == false
		currentScreen--;
		this.nextButton.setVisibility(View.VISIBLE);
		if (currentScreen == 0) 
			this.backButton.setVisibility(View.INVISIBLE);

		if (currentScreen < eckWizardStrings.length)
			textView.setText(eckWizardStrings[currentScreen]);
	}
	
	
	private void startRIPWizard() {
		if (Log.DEBUG) Log.d("FixBandProblemActivity", "Start RIP Wizard");		
		ripWizardActive = true;
		currentScreen = 0;
		this.backButton.setVisibility(View.INVISIBLE);
		this.nextButton.setVisibility(View.VISIBLE);
		textView.setText(ripWizardStrings[currentScreen]);
		
	}

	
	private void nextScreenRIPWizard() {
		if (Log.DEBUG) Log.d("FixBandProblemActivity", "Next Screen RIP Wizard");		
		currentScreen++;		
		this.backButton.setVisibility(View.VISIBLE);
		if (currentScreen < ripWizardStrings.length)
			textView.setText(ripWizardStrings[currentScreen]);
		else {
			endWizardActive = true;
    		textView.setText("");
			pd = ProgressDialog.show(this, "Checking...", "Please wait while we check the health of the system...");			
    		this.nextButton.setVisibility(View.INVISIBLE);
			this.backButton.setVisibility(View.INVISIBLE);			
		}		
	}
	
	private void prevScreenRIPWizard() {	
		if (Log.DEBUG) Log.d("FixBandProblemActivity", "Prev Screen RIP Wizard");		
		currentScreen--;
		this.nextButton.setVisibility(View.VISIBLE);
		if (currentScreen == 0) 
			this.backButton.setVisibility(View.INVISIBLE);

		if (currentScreen < ripWizardStrings.length)
			textView.setText(ripWizardStrings[currentScreen]);
	}	
	
	private void doEndWizardPass(Boolean dataGood) {
		if (Log.DEBUG) Log.d("FixBandProblemActivity", "Doing end wizard pass");		
//		pass++;
//		if (pass == MAX_PASSES) {
			if (pd!=null) {
				if (pd.isShowing())
					pd.dismiss();
			}
			
			if (dataGood) {
				if (Log.DEBUG) Log.d("FixBandProblemActivity", "Data GOOD");		
				textView.setText(endWizardStrings[0]);
				numTimesWizardRepeated=0;
				eckWizardActive = false;
				ripWizardActive = false;

				try {
					disconnect();
				} catch (RemoteException e) {
					Log.d("FixBanProblemActivity", "something bad happeneding with the service connection");
					e.printStackTrace();
				}
								
				handler.postDelayed(shutdown, 10000);
			}
			else if (numTimesWizardRepeated < MAX_TIMES_WIZARD_REPEATED){
				if (Log.DEBUG) Log.d("FixBandProblemActivity", "Done with 1 pass, starting another");		
				textView.setText(endWizardStrings[1]);
				numTimesWizardRepeated++;
				currentScreen = 0;
				
				this.nextButton.setVisibility(View.VISIBLE);
				this.backButton.setVisibility(View.INVISIBLE);			
				

			}
			else {
				if (Log.DEBUG) Log.d("FixBandProblemActivity", "Data BAD.");						
				textView.setText(endWizardStrings[2]);
				numTimesWizardRepeated=0;
				
//				this.nextButton.setVisibility(View.VISIBLE);
//				this.backButton.setVisibility(View.INVISIBLE);										

				try {
					disconnect();
				} catch (RemoteException e) {
					Log.d("FixBanProblemActivity", "something bad happeneding with the service connection");
					e.printStackTrace();
				}
				
				
				handler.postDelayed(shutdown, 10000);
			}

			endWizardActive = false;
			currentScreen = 0;
//			pass = 0;

//		}
	}
	
	public String status() {
		int eckDataQuality = currentDataQuality % 10;
		int ripDataQuality = currentDataQuality / 10;
		
		String eckString ="";
		if (eckDataQuality == DataQualityCalculation.DATA_QUALITY_GOOD) {
			eckString = "ECG BAND = GOOD DATA QUALITY";
		}
		else if (eckDataQuality == DataQualityCalculation.DATA_QUALITY_NOISE) {
			eckString = "ECG BAND = NOISY";
		}
		else if (eckDataQuality == DataQualityCalculation.DATA_QUALITY_SENSOR_LOOSE) {
			eckString = "ECG BAND = LOOSE";
		}
		else if (eckDataQuality == DataQualityCalculation.DATA_QUALITY_SENSOR_OFF) {
			eckString = "ECG BAND = OFF";
		}

		String ripString="";
		if (ripDataQuality == DataQualityCalculation.DATA_QUALITY_GOOD) {
			ripString = "RIP BAND = GOOD DATA QUALITY";
		}
		else if (ripDataQuality == DataQualityCalculation.DATA_QUALITY_NOISE) {
			ripString = "RIP BAND = NOISY";
		}
		else if (ripDataQuality == DataQualityCalculation.DATA_QUALITY_SENSOR_LOOSE) {
			ripString = "RIP BAND = LOOSE";		
		}
		else if (ripDataQuality == DataQualityCalculation.DATA_QUALITY_SENSOR_OFF) {
			ripString = "RIP BAND = OFF";
		}

		return eckString + "\n\n" + ripString;
	}
	
	public void processDQUpdate() {
		if (Log.DEBUG) Log.d("FixBandProblemActivity", "Processing Data Quality Update");
		int eckDataQuality = currentDataQuality % 10;
		int ripDataQuality = currentDataQuality / 10;
		
		if (eckWizardActive) {
			if (endWizardActive) {
				doEndWizardPass(eckDataQuality == DataQualityCalculation.DATA_QUALITY_GOOD);
			}
		}
		else if (ripWizardActive) {
			if (endWizardActive) {
				doEndWizardPass(ripDataQuality == DataQualityCalculation.DATA_QUALITY_GOOD);					
			}
		}
		else if (eckDataQuality > 0 && eckDataQuality != DataQualityCalculation.DATA_QUALITY_NOISE) {
			startECKWizard();
		}
		else if (ripDataQuality > 0 && ripDataQuality != DataQualityCalculation.DATA_QUALITY_NOISE) {
			startRIPWizard();
		}
//		else {
////			textView.setText("this should never appear!");
//		}
	}
	
	protected void disconnect() throws RemoteException {
		unbindService(inferenceConnection);
	}
	
	private void initService() {
	
	//		  inferenceServiceIntent = new Intent();
				inferenceServiceIntent = new Intent(getBaseContext(), InferrenceService.class);
	//	    conn = new InferrenceServiceConnection();
	//	    Intent i = new Intent();
	//	    inferenceServiceIntent.setClassName( "edu.cmu.ices.stress.phone", "edu.cmu.ices.stress.phone.service.InferrenceService" );
		    startService(inferenceServiceIntent);
		    bindService(inferenceServiceIntent, inferenceConnection,0);
		    
			if (Log.DEBUG) Log.d( "FixBandProblemActivity", "bindService()" );
	
	}
		
	private ServiceConnection inferenceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			
			inferenceService = IInferrenceService.Stub.asInterface(service);
			if (Log.DEBUG) Log.d("FixBandProblemActivity", "Connected to the inference service");
			
			try {
				inferenceService.subscribe(inferenceCallback);
				if (Log.DEBUG) Log.d("FixBandProblemActivity", "Subscribed to the inference service callback");
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	
		public void onServiceDisconnected(ComponentName name) {
			
			inferenceService = null;
			if (Log.DEBUG) Log.d("FixBandProblemActivity", "Disconnected from inference service");
		}
	};	
	
    // Need handler for callbacks to the UI thread
    final Handler handler = new Handler();

    // Create runnable for posting
    final Runnable updateResults = new Runnable() {
        public void run() {        
       		processDQUpdate();
        }
    };
	
    protected void startUpdateThread() {
        // Fire off a thread to do some work that we shouldn't do directly in the UI thread
        Thread t = new Thread() {
            public void run() {
            	handler.post(updateResults);
            }
        };
        t.start();
    }
	
	private Runnable shutdown = new Runnable() {
		public void run() {			
			finish();			
		}
	};
	
    
    
}
