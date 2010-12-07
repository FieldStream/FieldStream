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

// @author Brian French

package org.fieldstream.gui.ema.test;

import java.util.List;

import org.fieldstream.Constants;
import org.fieldstream.gui.ema.InterviewScheduler;
import org.fieldstream.service.logger.Log;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.cmu.ices.stress.phone.R;

public class InterviewScheduler_TEST extends Activity {

	//testService service;
	//InterviewScheduler sched;
	Button exit;
	Intent scheduler;
	Intent service;
	boolean debugMode = true;
	private HandlerThread inferrenceServiceThread;
	private Handler handler;
	
	ActivityManager mgr;
	Handler mgrH;
	
	WakeLock wakelock;
	boolean stopped = false;
	
	  class startThread implements Runnable {

			public void run() {
				startService(service);
				
			}
	    	
	    }
	
	/* START ANDROID LIFE CYCLE */
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	inferrenceServiceThread = new HandlerThread("InferrenceService");
    	inferrenceServiceThread.start();
    	service = new Intent(getBaseContext(), TestService.class);//(this, InferrenceService.class);
    	handler = new Handler(inferrenceServiceThread.getLooper());
    	handler.post(new startThread());
    	
//    	service = new Intent(getBaseContext(), TestService.class);
    	startService(service);
      
        setContentView(R.layout.schedulertest_layout);
        exit = (Button) findViewById(R.id.ExitButton);
    	//sched = new InterviewScheduler();
        // moved to onActivityResult below
        scheduler = new Intent(this, InterviewScheduler.class);
        scheduler.putExtra("DEBUG", debugMode);
        
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//        Intent stressor = new Intent(getBaseContext(), SetTime.class);
//        startActivityForResult(stressor, 1);
        //startActivityForResult(scheduler, 1);
        startService(scheduler);
        //responses.setTextFilterEnabled(true);
        initButton();
        
        PowerManager pm = (PowerManager)this.getSystemService(POWER_SERVICE);
		//wakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "AbstractInterview");
		wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SchedulerTest");
		wakelock.acquire();
		
		mgr = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE);
//		List<RunningTaskInfo> info = mgr.getRunningTasks(1);
//		String currentTask = info.get(0).topActivity.getShortClassName();
//		if (Log.DEBUG) Log.d("mgrRun", info.get(0).topActivity.getShortClassName());
		mgrH = new Handler();
		mgrH.postDelayed(mgrRun, 10000);
    }
    
    @Override
	 public void onActivityResult(int requestCode, int resultCode, Intent data) {
	   if (resultCode == Activity.RESULT_OK && requestCode == 1) {
	     Bundle extras = data.getExtras();
	     long startHour = extras.getInt(Constants.quietStart);
//	     int startMin = extras.getInt(Constants.stressorStart_M);
	     long stopHour = extras.getInt(Constants.quietEnd);
//	     int stopMin = extras.getInt(Constants.stressorStop_M);
	     long eodHour = extras.getInt(Constants.sleepStart);
//	     int eodMin = extras.getInt(Constants.eod_M);
	     long sodHour = extras.getInt(Constants.sleepEnd);
//	     int sodMin = extras.getInt(Constants.sod_M);
	     
	     scheduler = new Intent(getBaseContext(), InterviewScheduler.class);
	     scheduler.putExtra("DEBUG", debugMode);
	     scheduler.putExtra(Constants.quietStart, startHour);
//	     scheduler.putExtra(Constants.stressorStart_M, startMin);
	     scheduler.putExtra(Constants.quietEnd, stopHour);
//	     scheduler.putExtra(Constants.stressorStop_M, stopMin);
	     scheduler.putExtra(Constants.sleepStart, eodHour);
//	     scheduler.putExtra(Constants.eod_M, eodMin);
	     scheduler.putExtra(Constants.sleepEnd, sodHour);
//	     scheduler.putExtra(Constants.sod_M, sodMin);
	     startService(scheduler);
	     if (Log.DEBUG) Log.d("onActivityResult", "started the scheduler service");
	   }
	 }
    
    /* This is called immediately after onCreate() when the app is started
	 * or after the app comes back from a pause.
	 */
	@Override
	protected void onResume() {
		//handler.post(updateView);
		Log.i("InterviewScheduler", "onResume");
		super.onResume();
		
	}

	/* This is called when the app goes into the background. */
	@Override
	protected void onPause() {
		Log.i("InterviewScheduler", "onPause");
		
		super.onPause();
	}

	/* This is called when the app is killed. */
	@Override
	protected void onDestroy() {
		
		Log.i("InterviewScheduler", "onDestroy");

		if (wakelock.isHeld()) {
			wakelock.release();
		}
		
		super.onDestroy();
	}
	/* END ANDROID LIFE CYCLE */
	
	private Runnable mgrRun = new Runnable() {
		public void run() {
			List<RunningTaskInfo> info = mgr.getRunningTasks(1);
			if (Log.DEBUG) Log.d("mgrRun", info.get(0).topActivity.getShortClassName());
			if (!stopped) {
				mgrH.postDelayed(this, 10000);
			}
		}
	};

	private void initButton() {
		exit.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				//finishActivity(1);
				stopService(scheduler);
				stopService(service);
				inferrenceServiceThread.getLooper().quit();
				inferrenceServiceThread=null;
				stopped = true;
				finish();
			}
			
		});
	}
}
