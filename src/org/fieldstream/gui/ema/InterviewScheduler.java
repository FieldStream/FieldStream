
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

//@author Mishfaq Ahmed
//@author Brian French
//@author Andrew Raij


package org.fieldstream.gui.ema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.fieldstream.Constants;
import org.fieldstream.gui.ema.test.TestService;
import org.fieldstream.incentives.EMAIncentiveManager;
import org.fieldstream.service.IInferrenceService;
import org.fieldstream.service.IInferrenceServiceCallback;
import org.fieldstream.service.InferrenceService;
import org.fieldstream.service.context.model.ActivityCalculation;
import org.fieldstream.service.context.model.CommutingCalculation;
import org.fieldstream.service.context.model.ConversationDetectionModel;
import org.fieldstream.service.context.model.ConversationPrediction;
import org.fieldstream.service.context.model.DataQualityCalculation;
import org.fieldstream.service.logger.Log;

import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.format.Time;
import android.view.View;
import android.widget.Toast;

public class InterviewScheduler extends Service {
	
	EMAIncentiveManager incentives = null;
	static InterviewScheduler INSTANCE = null;
	
	// interview activity
	// Activity context;
	Intent interviewIntent;
	Intent eodIntent;
	Intent inferenceServiceIntent;

	// keyguard lock
	// KeyguardLock keyguard;

	// stress inference service
	IInferrenceService inferenceService;
	IInterviewCancellationCallback interviewCallback;
	// IMonitorService monitorService;
	
		
	// scheduling variables
	Handler scheduler;
	long lastInterviewTime;
	long nextPeriodicTime;
	long nextContextTime;
	// boolean interviewScheduled;
	boolean interviewRunning;
	long endOfDay;
	long startOfDay;
	boolean atEOD;
	long stressorStartTime;
	long stressorStopTime;
	boolean launchEOD;
	// boolean eodReached;
	// boolean stressorPassed;
	// boolean stressorReached;
	int launchType;
	boolean debugMode;
	boolean schedulerPaused;

	// context received
	String activeModelsString;
//	int lastContextTrigger;
//	int lastContextTriggerValue;
	long lastContextChangeTime;
	boolean withinGracePeriod;	
	
	static public IContent getContent() {
		IContent content = AutoSenseStudyInterviewContent.getInstance();
		return content;
	}

	public EMAIncentiveManager getIncentiveMangager() {
		return incentives;
	}	
	

	
	// scheduler constants
	static final long MINUTE_MILLIS = 60L * 1000L;
	static final long HOUR_MILLIS = 60L * 60L * 1000L;
	static final long PERIODIC_INTERVAL = (55L * MINUTE_MILLIS);
	static final long CONTEXT_TIME_THRESHOLD = (60L * MINUTE_MILLIS);
	static final long GRACE_PERIOD = (33L * MINUTE_MILLIS);

	private static final int MAX_DAILY_EMA = 20;
		
//	static final long PERIODIC_INTERVAL = 120000;// every 2 minutes
//	static final long CONTEXT_TIME_THRESHOLD = 35000; // 15 seconds
//	static final long GRACE_PERIOD = 60000; // 20 seconds
	static final int EOD_HOUR = 21;
	static final boolean HAS_EOD_INTERVIEW = false;
	

	// rescheduling type constants
	static final int PERIODIC = 0;
	static final int CONTEXT_TIME = 1;
	static final int CONTEXT_CHANGE = 2;

	// interview type constants
//	static final int TYPE_PERIODIC = 0;
//	static final int TYPE_CONTEXT_TIME = 1;
//	static final int TYPE_CONTEXT_CHANGE = 2;
//	static final int TYPE_INTERRUPTED_BY_PERIODIC = 3;
//	static final int TYPE_INTERRUPTED_BY_CONTEXT_TIME = 4;
//	static final int TYPE_INTERRUPTED_BY_CONTEXT_CHANGE = 5;
//	static final int TYPE_EOD = 6;

	// sub activity constants
	static final int INTERVIEW_REQUEST = 1;
	static final int EOD_REQUEST = 2;

	// ema log constants
	static final String[] INTERVIEW_HEADER = { "TYPE", "DATE", "TIME", "AM/PM",
			"RAW TIMESTAMP", "DELAY", "DELAY REASON", "START OFFSET", "STATUS" };

	// final String INTERVIEW_PACKAGE = "edu.cmu.ices.stress.phone.gui.ema";
	// final String INTERVIEW_CLASS =
	// "edu.cmu.ices.stress.phone.gui.ema.Interview";

	// public InterviewScheduler() {
	// initialize();
	// }

	/* START ANDROID LIFE CYCLE */

	/** Called when the activity is first created. */
	@Override
	public void onCreate() { // (Bundle savedInstanceState) {
		super.onCreate();// (savedInstanceState);
		if (Log.DEBUG) Log.d("InterviewScheduler", "onCreate");
		// setContentView(R.layout.scheduler_layout);
		// debugMode = getIntent().getBooleanExtra("DEBUG", false);

		// initialize();
		// responses.setTextFilterEnabled(true);
		// bindServices();

		INSTANCE = this;
	}

	@Override
	// TODO THIS FUNCTION IS DEPRECATED AFTER API 4. SHOULD BE CHANGED TO
	// ONSTARTCOMMAND IF API 5 OR ABOVE IS USED.
	public void onStart(Intent intent, int startId) {

		// the intent contains potential configuration information
		if (intent != null) {
			debugMode = intent.getBooleanExtra("DEBUG", false);
			if (debugMode) {
				if (Log.DEBUG) Log.d("onStart", "debug mode enabled");
			} else {
				if (Log.DEBUG) Log.d("onStart", "debug mode disabled");
			}

			// any other configuration parameters?
			stressorStartTime = intent.getLongExtra(Constants.quietStart, 0);
			stressorStopTime = intent.getLongExtra(Constants.quietEnd, 0);
			endOfDay = intent.getLongExtra(Constants.sleepStart, 0);
			startOfDay = intent.getLongExtra(Constants.sleepEnd, 0);

			if (Log.DEBUG) Log.d("InterviewScheduler:onStart", "stessorStartTime "+stressorStartTime);
			if (Log.DEBUG) Log.d("InterviewScheduler:onStart", "stessorStopTime " +stressorStopTime);
		} else {
			debugMode = false;
			stressorStartTime = 0;
			stressorStopTime = 0;
			endOfDay = 0;
			startOfDay = 0;
		}

		initialize();

		bindServices();

		initIncentiveManager();

		initEMABudget();
		
		initEMATriggerer(this.budgeter);
		// set up pause timers
		initSchedulerPauseEvents();

		super.onStart(intent, startId);
		
		// notify user of start with a beep and a toast
		Time stressor = new Time();
		stressor.set(stressorStartTime);
		long stressorLen = (stressorStopTime - stressorStartTime) / 1000 / 60;
		Time eod = new Time();
		eod.set(endOfDay);
		long eodLen = (startOfDay - endOfDay) / 1000 / 60;
		ToneGenerator tone = new ToneGenerator(android.media.AudioManager.STREAM_NOTIFICATION, 100);
		tone.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP);
		Toast t = Toast.makeText(getBaseContext(), "EMAService: stressor: " +stressor.hour + ":" + stressor.minute + "-" + stressorLen + ", eod: " + eod.hour + ":" + eod.minute + "-" + eodLen, Toast.LENGTH_LONG);
		t.show();
	}

	/* This is called when the app is killed. */
	@Override
	public void onDestroy() {

		if (incentives != null) {
			incentives.cleanup();
			incentives = null;
		}
		
		Log.i("InterviewScheduler", "onDestroy");
		// unbind the inference service since we are being destroyed
		unbindServices();

		// remove all callbacks to make sure the service is stopped completely
		scheduler.removeCallbacks(launchScheduledInterview);
		scheduler.removeCallbacks(launchContextTimeInterview);
		scheduler.removeCallbacks(launchContextChangeInterview);
		scheduler.removeCallbacks(gracePeriodOver);
		scheduler.removeCallbacks(launchEODInterview);
		scheduler.removeCallbacks(restartScheduler);
		scheduler.removeCallbacks(STRESSOR);
		scheduler.removeCallbacks(EOD);

		if (interviewCallback != null && interviewRunning) {
			try {
				interviewCallback.cancelInterview();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		super.onDestroy();
	}

	/* END ANDROID LIFE CYCLE */

	@SuppressWarnings("all")
	private void initialize() {
		// create the interview intent now and keep it around since it won't
		// change
		// context = this;
		// interviewIntent = new Intent();
		// interviewIntent.setClassName(INTERVIEW_PACKAGE, INTERVIEW_CLASS);
		
		interviewIntent = new Intent(getBaseContext(), Interview.class);
		interviewIntent.putExtra("interviewType", INTERVIEW_REQUEST);
		interviewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		interviewIntent.putExtra("DEBUG", debugMode);

		eodIntent = new Intent(getBaseContext(), EODInterview.class);
		eodIntent.putExtra("interviewType", EOD_REQUEST);
		eodIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		eodIntent.putExtra("DEBUG", debugMode);

		 if (debugMode) {
			 inferenceServiceIntent = new Intent(getBaseContext(), TestService.class);
		 } else {
			 inferenceServiceIntent = new Intent(getBaseContext(), InferrenceService.class);
		 }

		// init scheduling vars
		scheduler = new Handler();
		lastInterviewTime = 0;
		// nextPeriodicTime = 0;
		// interviewScheduled = false;
		interviewRunning = false;
		launchType = -1;
//		Time eod = new Time();
//		eod.setToNow();
		// set end values

		if (endOfDay == 0 && EOD_HOUR > 0) {
			//eod.set(0, 0, EOD_HOUR, eod.monthDay, eod.month, eod.year);
			endOfDay = formatTime(EOD_HOUR, 0); //eod.normalize(false);
		}
//		} else {
//			endOfDay = 0;
//		}
		launchEOD = false;
		// eodReached = false;
		atEOD = false;

		// stressorTime = 0;
		// stressorPassed = false;
		// stressorReached = false;

		schedulerPaused = false;

		// init context
//		lastContextTrigger = -1;
//		lastContextTriggerValue = -1;
		activeModelsString = "";
		lastContextChangeTime = 0;
		nextContextTime = 0;
		withinGracePeriod = false;

		// init the keyguard lock manager
		// KeyguardManager mgr =
		// (KeyguardManager)this.getSystemService(KEYGUARD_SERVICE);
		// keyguard = mgr.newKeyguardLock("InterviewScheduler");

		// set up the first periodic interview
		// reschedule(PERIODIC);
		// scheduler.postDelayed(launchScheduledInterview, SCHEDULE_INTERVAL);
		// initialize the first scheduled interview
		
		// MOVED TO SCHEDULEFIRSTINTERVIEW method which is called on connection with the inference service
//		nextPeriodicTime = SystemClock.uptimeMillis() + (1L*MINUTE_MILLIS);
//		// hardcoded the first EMA to one minute in the future!
//		scheduler.postAtTime(launchScheduledInterview, nextPeriodicTime);
		if (Log.DEBUG) {
		Time debugtime = new Time();
		debugtime.set(nextPeriodicTime);
		 Log.d("Constructor","set first interview for "+nextPeriodicTime+" with periodic  "+PERIODIC_INTERVAL +" at real time "+debugtime.format2445() );
		}
		// add the column header to the ema log
		addLogHeader();

		// initialize the context buffer
		initContextBuffer();
		
		if (Log.DEBUG) Log.d("Constructor", "Initialized scheduler");
		
	}
	
	
	private void scheduleFirstInterview() {
		if (inferenceService != null) {
			try {
				int numEMAs = inferenceService.getNumEMAsToday();
				long lastEMA = inferenceService.getLastEMATimestamp();
				long timeSinceLast = System.currentTimeMillis() - lastEMA;
				if (numEMAs < MAX_DAILY_EMA) {
					// schedule an interview
					if (timeSinceLast > GRACE_PERIOD) {
						nextPeriodicTime = SystemClock.uptimeMillis() + (1L*MINUTE_MILLIS);
						// hardcoded the first EMA to one minute in the future!
						//for testing conversation.....mahbub
						scheduler.postAtTime(launchScheduledInterview, nextPeriodicTime);
						
						Log.d("ScheduleFirstInterview", "scheduled immediately");
					} 
//					else {
//						long diff = GRACE_PERIOD - timeSinceLast;
//						nextPeriodicTime = SystemClock.uptimeMillis() + diff;
//						scheduler.postAtTime(launchScheduledInterview, nextPeriodicTime);
//						Log.d("ScheduleFirstInterview", "scheduled in " + diff + " ms");
//					}
				} else {
					// all interviews for today are done, disable all interviews
					scheduler.removeCallbacks(launchScheduledInterview);
					scheduler.removeCallbacks(launchContextTimeInterview);
					scheduler.removeCallbacks(launchContextChangeInterview);
					schedulerPaused = true;
					Log.d("ScheduleFirstInterview", "No more EMAs can be scheduled for the day");
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private long formatTime(int hour, int minute) {
		Time now = new Time();
		Time time = new Time();
		now.setToNow();
		// set end values

		time.set(0, minute, hour, now.monthDay, now.month, now.year);
		return time.toMillis(false);// - now.toMillis(false);
//		if (EOD_HOUR > 0) {
//			eod.set(0, 0, EOD_HOUR, eod.monthDay, eod.month, eod.year);
//			endOfDay = eod.normalize(false);
//		} else {
//			endOfDay = 0;
//		}
	}

	private void initSchedulerPauseEvents() {

		long curr = System.currentTimeMillis();
		long diff;

		// set up the stressor disable timer
		if (stressorStartTime != 0) {
			diff = stressorStartTime - curr;
			if (diff > 0) {
				scheduler.postAtTime(STRESSOR, SystemClock.uptimeMillis()
						+ diff);
				if (Log.DEBUG) Log.d("initSchedulerPause","Posted Stressor schedule at now +"+diff);
			} else if (curr < this.stressorStopTime){
				scheduler.post(STRESSOR);
				if (Log.DEBUG) Log.d("initSchedulerPause","Posted Stressor now");
			}
			else {
				// we're past the stressor period
			}
		}

		// set up the end of day disable timer
		if (endOfDay != 0) {
			diff = endOfDay - curr;
			if (diff > 0) {
				scheduler.postAtTime(EOD, SystemClock.uptimeMillis() + diff);
				if (Log.DEBUG) Log.d("initSchedulerPause","Posted EOD schedule at now "+diff);
			} else if (curr < startOfDay){
				scheduler.post(EOD);
				if (Log.DEBUG) Log.d("initSchedulerPause","Posted EOD now");
			}
			else {
				// we're past the start of the new day 
			}

		}
		// set up the start of day resume event
//		if (startOfDay != 0) {
//			diff = startOfDay - curr;
//			if (diff > 0) { //always should be since its tomorrow if ever
//				scheduler.postAtTime(SOD, SystemClock.uptimeMillis() + diff);
//			}
//		}
	}

	/*
	 * Adds a header to the EMA log
	 */
	private void addLogHeader() {
//		StringBuilder sb = new StringBuilder();
//		int length = INTERVIEW_HEADER.length;
//		sb.append(INTERVIEW_HEADER[0]);
//		for (int i = 1; i < length; i++) {
//			sb.append(",").append(INTERVIEW_HEADER[i]);
//		}
//		length = InterviewContent.questions.length;
//		for (int i = 0; i < length; i++) {
//			sb.append(",Q").append(i).append(",Q").append(i).append("_time");
//		}
//		String header = sb.toString();
//		if (Log.DEBUG) Log.d("addLogHeader", header);
//		
//		try {
//			inferenceService.writeLabel(header);
//		} catch (RemoteException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	private void addEODLogHeader() {
		StringBuilder sb = new StringBuilder();
		int length = INTERVIEW_HEADER.length;
		sb.append(INTERVIEW_HEADER[0]);
		for (int i = 1; i < length; i++) {
			sb.append(",").append(INTERVIEW_HEADER[i]);
		}
		length = EODContent.questions.length;
		for (int i = 0; i < length; i++) {
			sb.append(",Q").append(i).append(",Q").append(i).append("_time");
		}
		String header = sb.toString();
		if (Log.DEBUG) Log.d("addEODLogHeader", header);
		
		try {
			inferenceService.writeLabel(header);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* BEGIN INTERPROCESS COMMUNICATION WITH INFERRENCE SERVICE */

	private void bindServices() {
		bindService(inferenceServiceIntent, inferenceConnection, 0);
		// bindService(new Intent(getBaseContext(), MonitorService.class),
		// monitorConnection, 0);
		if (Log.DEBUG) Log.d("bindServices", "Bound to the services");
	}

	private void unbindServices() {
		if (inferenceService != null) {
			try {
				inferenceService.unsubscribe(inferenceCallback);
				//stopService(inferenceServiceIntent);
				if (Log.DEBUG) Log.d("unbindInferenceService",
						"Unsubscribed the service callback");
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		unbindService(inferenceConnection);
		// unbindService(monitorConnection);
		if (Log.DEBUG) Log.d("unbindServices", "Services unbound");

	}

	/*
	 * Connection to the inference service
	 */
	private ServiceConnection inferenceConnection = new ServiceConnection() {


		
		public void onServiceConnected(ComponentName name, IBinder service) {

			inferenceService = IInferrenceService.Stub.asInterface(service);
			if (Log.DEBUG) Log.d("inferenceConnection", "Connected to the inference service");

			try {
				inferenceService.subscribe(inferenceCallback);				
				
				if (Log.DEBUG) Log.d("inferenceConnection",
						"Subscribed to the inference service callback");
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			
			 scheduleFirstInterview();

		}

		public void onServiceDisconnected(ComponentName name) {			
			
			inferenceService = null;
			if (Log.DEBUG) Log.d("inferenceConnection", "Disconnected from inference service");
		}

	};

	/*
	 * IInferrenceServiceCallback implementation
	 */
	private IInferrenceServiceCallback inferenceCallback = new IInferrenceServiceCallback.Stub() {

		public void receiveCallback(int modelID, int value, long start, long end) throws RemoteException {

			processContext(modelID, value); // (((Integer)value).toString());

		}
	};

	/* END INTERPROCESS COMMUNICATION WITH INFERRENCE SERVICE */

	private void enterGracePeriod(long gracePeriodTime) {
		// disable context change interviews and set the grace period time event
		withinGracePeriod = true;
		scheduler.removeCallbacks(gracePeriodOver);
		if (Log.DEBUG) Log.d("enterGrace", "removingCallbacks");
		// if (gracePeriodTime < endOfDay) {
		 scheduler.postAtTime(gracePeriodOver, gracePeriodTime);
		// } else {
		// setEOD = true;
		// }
	}

	private long interruptScheduled(Runnable interview, long interviewTime,
			long gracePeriodTime, long nextTime) {
		long nextInterview = 0;

		if (interviewTime > 0 && interviewTime < gracePeriodTime) {
			if (nextTime > 0) {
				nextInterview = nextTime;
			} else {
				nextInterview = gracePeriodTime;
			}
			// reschedule context timer and disable context interviews for grace
			// period
			scheduler.removeCallbacks(interview);
			if (Log.DEBUG) Log.d("interruptScheduled", "removingCallbacks");
			// scheduler.postAtTime(gracePeriodOver, gracePeriodTime);
			// if (nextInterview < endOfDay) {
			// if (nextInterview < stressorTime || stressorPassed) {
			 scheduler.postAtTime(interview, nextInterview);
			// } else {
			// stressorReached = true;
			// if (Log.DEBUG) Log.d("interuptScheduled", "reached the stressor time");
			// }
			// } else {
			// eodReached = true;
			// }
		}

		return nextInterview;
	}

	/*
	 * All interview rescheduling happens here so that they are properly
	 * synchronized
	 */
	private void reschedule(int type, boolean typeIsRunning) {
		// long lastContextTime;
		// boolean setEOD = false;
		long next;

		long gracePeriodTime = lastInterviewTime + GRACE_PERIOD;
		if (typeIsRunning) {
			// this call to reschedule was made by the currently running
			// interview process
			// so the grace period should be entered
			// gracePeriodTime = lastInterviewTime + GRACE_PERIOD;
			enterGracePeriod(gracePeriodTime);
		}

		switch (type) {

		case PERIODIC:
			// check context timed for conflicts
			// gracePeriodTime = lastInterviewTime + GRACE_PERIOD;
			if (typeIsRunning) { // (launchType == TYPE_PERIODIC) {
			// // this call to reschedule was made by the currently running
			// interview process
			// gracePeriodTime = lastInterviewTime + GRACE_PERIOD;
			// enterGracePeriod(gracePeriodTime);
				// nextContextTime = lastContextChangeTime +
				// CONTEXT_TIME_THRESHOLD;
				 next = interruptScheduled(launchContextTimeInterview,
					nextContextTime, gracePeriodTime, -1);
				// if (nextContextTime != 0 && nextContextTime <=
				// gracePeriodTime) {
				// nextContextTime = gracePeriodTime;
				// // reschedule context timer and disable context interviews
				// for grace period
				// scheduler.removeCallbacks(launchContextTimeInterview);
				// //scheduler.postAtTime(gracePeriodOver, gracePeriodTime);
				// scheduler.postAtTime(launchContextTimeInterview,
				// nextContextTime);
				if (next > 0) {
					nextContextTime = next;
					launchType = EMALogConstants.TYPE_INTERRUPTED_BY_PERIODIC;
					Log.i("reschedule",
							"context time interview rescheduled by periodic interview: "
									+ gracePeriodTime);
				} else {
					// set the launch type
					launchType = EMALogConstants.TYPE_PERIODIC;
					Log.i("rescheduled",
							"no interview schedules interrupted by periodic");
				}
			}

			// remove any scheduled periodic interviews
			scheduler.removeCallbacks(launchScheduledInterview);
			if (Log.DEBUG) Log.d("reschedule", "removingCallbacks");
			// reschedule the periodic interview
			nextPeriodicTime = nextPeriodicTime + PERIODIC_INTERVAL;
			// if (nextPeriodicTime < endOfDay) {
			// if (nextPeriodicTime < stressorTime || stressorPassed) {
			scheduler.postAtTime(launchScheduledInterview, nextPeriodicTime);   //EDITED BY ANDREW TO TURN OFF SCHEDULED INTERVIEWS
			Log.i("reschedule", "periodic type " + nextPeriodicTime);
			// } else {
			// stressorReached = true;
			// if (Log.DEBUG) Log.d("rescheduler",
			// "reached the stressor time, disable all interviews");
			// }
			// } else {
			// eodReached = true;
			// if (Log.DEBUG) Log.d("reschedule", "end of day set");
			// }
			// interviewScheduled = true;
			break;

		case CONTEXT_TIME: // falls through to context change
		// // check period interview for conflicts
		// //gracePeriodTime = lastInterviewTime + GRACE_PERIOD;
		// // if (launchType == TYPE_CONTEXT_TIME) {
		// // gracePeriodTime = lastInterviewTime + GRACE_PERIOD;
		// // enterGracePeriod(gracePeriodTime);
		// if (typeIsRunning) {
		// next = lastInterviewTime + PERIODIC_INTERVAL;
		// next = interruptScheduled(launchScheduledInterview, nextPeriodicTime,
		// gracePeriodTime, next);
		// // if (nextPeriodicTime <= gracePeriodTime) {
		// // // reschedule the periodic interview
		// // scheduler.removeCallbacks(launchScheduledInterview);
		// // //scheduler.postAtTime(gracePeriodOver, gracePeriodTime);
		// // nextPeriodicTime = lastInterviewTime + PERIODIC_INTERVAL;
		// // if (nextPeriodicTime < endOfDay) {
		// // scheduler.postAtTime(launchScheduledInterview, nextPeriodicTime);
		// // } else {
		// // setEOD = true;
		// // }
		// if (next > 0) {
		// nextPeriodicTime = next;
		// launchType = TYPE_INTERRUPTED_BY_CONTEXT_TIME;
		// if (Log.DEBUG) Log.d("reschedule",
		// "periodic rescheduled by context time interview: " +
		// nextPeriodicTime);
		// } else {
		// launchType = TYPE_CONTEXT_TIME;
		// if (Log.DEBUG) Log.d("reschedule",
		// "no schedules interrupted by context time interview");
		// }
		// }
		// // remove last scheduled context interview
		// scheduler.removeCallbacks(launchContextTimeInterview);
		//				
		// // reschedule the context time based interview
		// nextContextTime = nextContextTime + CONTEXT_TIME_THRESHOLD;
		// // if (nextContextTime < endOfDay) {
		// // if (nextContextTime < stressorTime || stressorPassed) {
		// scheduler.postAtTime(launchContextTimeInterview, nextContextTime);
		// // } else {
		// // stressorReached = true;
		// // if (Log.DEBUG) Log.d("rescheduler",
		// "reached the stressor time, disable all interviews");
		// // }
		// // } else {
		// // eodReached = true;
		// // }
		// if (Log.DEBUG) Log.d("reschedule", "context time type " + nextContextTime);
		// break;

		case CONTEXT_CHANGE:
			// check period interview for conflicts
			// check period interview for conflicts
			// gracePeriodTime = lastInterviewTime + GRACE_PERIOD;
			// if (launchType == TYPE_CONTEXT_CHANGE) {
			// gracePeriodTime = lastInterviewTime + GRACE_PERIOD;
			// enterGracePeriod(gracePeriodTime);
			if (typeIsRunning) {
				next = lastInterviewTime + PERIODIC_INTERVAL;
				 next = interruptScheduled(launchScheduledInterview,
						nextPeriodicTime, gracePeriodTime, next);
				// if (nextPeriodicTime <= gracePeriodTime) {
				// // reschedule the periodic interview
				// scheduler.removeCallbacks(launchScheduledInterview);
				// //scheduler.postAtTime(gracePeriodOver, gracePeriodTime);
				// nextPeriodicTime = lastInterviewTime + PERIODIC_INTERVAL;
				// if (nextPeriodicTime < endOfDay) {
				// scheduler.postAtTime(launchScheduledInterview,
				// nextPeriodicTime);
				// } else {
				// setEOD = true;
				// }
				if (next > 0) {
					nextPeriodicTime = next;
					launchType = (type == CONTEXT_TIME) ? EMALogConstants.TYPE_INTERRUPTED_BY_CONTEXT_TIME
							: EMALogConstants.TYPE_INTERRUPTED_BY_CONTEXT_CHANGE;
					if (Log.DEBUG) Log.d("reschedule",
							"periodic interrupted by context interview: "
									+ nextPeriodicTime);
				} else {
					launchType = (type == CONTEXT_TIME) ? EMALogConstants.TYPE_CONTEXT_TIME
							: EMALogConstants.TYPE_CONTEXT_CHANGE;
					
					if (Log.DEBUG) Log.d("reschedule",
							"no schedules interrupted by context interview");
				}
			}

			// remove last scheduled context interview
			scheduler.removeCallbacks(launchContextTimeInterview);
			if (Log.DEBUG) Log.d("enterGrace", "removingCallbacks");
			// reschedule the context time based interview
			nextContextTime = nextContextTime + CONTEXT_TIME_THRESHOLD;
			// if (nextContextTime < endOfDay) {
			// if (nextContextTime < stressorTime || stressorPassed) {
			scheduler.postAtTime(launchContextTimeInterview, nextContextTime);
			// } else {
			// stressorReached = true;
			// if (Log.DEBUG) Log.d("rescheduler",
			// "reached the stressor time, disable all interviews");
			// }
			// } else {
			// eodReached = true;
			// }
			if (Log.DEBUG) Log.d("reschedule", "context type: " + type + "rescheduled for: "
					+ nextContextTime);
			break;
		}
		// }

	}
	
	// BEGIN CONTEXT CHANGE HANDLING CODE
	
	static final int CONTEXT_BUFFER_SIZE = 5;
	static final int MAJORITY = ((CONTEXT_BUFFER_SIZE >> 1) + 1);
	HashMap<Integer, int[]> contextBuffers;
	HashMap<Integer, Integer> contextBufferPtrs;
	
	private void initContextBuffer() {
		contextBuffers = new HashMap<Integer, int[]>(); 
		contextBufferPtrs = new HashMap<Integer, Integer>();
	}
	
	/*
	 * This method buffers context values in a ring buffer
	 * It returns the context value if all the buffered values are
	 * equal and -1 if not all of the context values are equal
	 */
	private int bufferContext(int modelID, int value) {
		int[] buffer;
		int index;
		if (!contextBuffers.containsKey(modelID)) {
			buffer = new int[CONTEXT_BUFFER_SIZE];
			Arrays.fill(buffer, -1);
			contextBuffers.put(modelID, buffer);
			index = 0;
			contextBufferPtrs.put(modelID, index);
		}
		else {
			buffer = contextBuffers.get(modelID);
			index = contextBufferPtrs.get(modelID);
		}
				
		buffer[index] = value;
		index++;
		
		if (index == CONTEXT_BUFFER_SIZE) {
			index = 0;
		}
		contextBufferPtrs.put(modelID, index);
		
		// figure out current context
		return majority(buffer);
	}
	
	// MJRTY linear time majority function, from Boyer and Moore
	private int majority(int[] contexts) {
		int maj = -1, cnt = 0;
		for (int i=0; i<contexts.length; i++) { 
			// buffer hasn't been filled yet
			if (contexts[i] == -1) {
				return -1;
			}
			if (cnt == 0) {
				maj = contexts[i];
				cnt = 1;
			} else {
				if (contexts[i] == maj) {
					cnt ++;
				} else {
					cnt --;
				}
			}
		}
		return maj;		
	}

	// initialize this
	// majorityContext[modelID] is -1 if the context buffer hasn't been filled yet or doesn't exist, otherwise
	// majorityContext is the majority context out of the last CONTEXT_BUFFER_SIZE context values		
	HashMap<Integer, Integer> majorityContext = null;
	
	/*
	 * Process context information and schedules context triggered interviews
	 * accordingly
	 */
	private synchronized void processContext(int modelID, int value) {
		Log.d("processContext", "calling process context on " + modelID + " = " + value);
		int numEMA = 0;
		try {
			numEMA = inferenceService.getNumEMAsToday();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		////////// For handling self report ///////////
		if(modelID == Constants.MODEL_SELF_DRINKING || modelID == Constants.MODEL_SELF_SMOKING)
			{
			Log.d("gonna self report", "gonna self report");			
			
			if( numEMA<MAX_DAILY_EMA && triggerer.trigger(modelID,System.currentTimeMillis())){
				
				scheduler.post(launchContextChangeInterview);
			}
			return;
			}
		//////////////////////////////////////////////
		
		if (majorityContext == null)
			majorityContext = new HashMap<Integer, Integer>();

		String foundModelID="";
		
		int maj = bufferContext(modelID, value);
		if (maj != -1) {
			int oldMaj = -1;
			if (majorityContext.containsKey(modelID)) {
				oldMaj = majorityContext.get(modelID);
			}			
			
			if (oldMaj != maj || oldMaj == -1) {
				majorityContext.put(modelID, maj);

				String id = modelID + ":" + maj;
				if (Log.DEBUG) Log.d("processContext", "checking budget for context " + id);
				if (budgeter.checkBudget(id)) {	
					if (Log.DEBUG) Log.d("processContext", "budget for context " + id + " available - triggering");
					
					foundModelID = id;
					
					// context change event
					nextContextTime = SystemClock.uptimeMillis();// .elapsedRealtime();
					// if (!contextDisabled) {
					// if (Log.DEBUG) Log.d("processContext", "Context changed triggered interview");
					
					if(triggerer.trigger(modelID,System.currentTimeMillis())){
						
						scheduler.post(launchContextChangeInterview);
					}
					// } else {
					// if (Log.DEBUG) Log.d("processContext", "Context changed with in grace period");
					// reschedule(CONTEXT_CHANGE);
					// if (Log.DEBUG) Log.d("processContext", "rescheduled context time event");
					// }
				}					
			}
		}

			
			
			
		Log.d("processContext", "majority of " + modelID + " = " + maj);

		if (interviewRunning)
			return;
		
//		if (majorityContext.size() != activeModels.size()) {
//			Log.d("processContext", "majority cache not initialized yet - waiting on " + (activeModels.size() - majorityContext.size()) + " more contexts");
//			return;
//		}
//		else
//			Log.d("processContext", "majority cache fully initialized - will now start triggering");
		
		activeModelsString = "";
		
		int i=0;
		for (Integer model : majorityContext.keySet()) {
			int modelValue = majorityContext.get(model);
			String id = model + ":" + modelValue;

			activeModelsString += id; 
			if (i!=majorityContext.size() - 1)
				activeModelsString += ",";
			i++;
		}
		
//		if (foundModelID.length() == 0) {
//			// if we got here, then none of the budgeted contexts are active
//			// or more accurately, one of all other possibilities is active
//			// we check the budget for this "other" case and trigger if we're within the budget
//			
//			if (Log.DEBUG) Log.d("processContext", "checking budget for other contexts");
//			if (budgeter.checkBudget("other")) {
//				
//				nextContextTime = SystemClock.uptimeMillis();// .elapsedRealtime();
//				// if (!contextDisabled) {
//				// if (Log.DEBUG) Log.d("processContext", "Context changed triggered interview");
//				
//				scheduler.post(launchContextChangeInterview);
//				// } else {
//				// if (Log.DEBUG) Log.d("processContext", "Context changed with in grace period");
//				// reschedule(CONTEXT_CHANGE);
//				// if (Log.DEBUG) Log.d("processContext", "rescheduled context time event");
//				// }				
//				
//				if (Log.DEBUG) Log.d("processContext", "triggering ema based on other contexts");
//			}
//		}
//		else {
		if (foundModelID.length() !=0){
			if (Log.DEBUG) Log.d("processContext", "triggering ema based on " + foundModelID);
			activeModelsString = foundModelID + " " + activeModelsString;
		}
		
		Log.d("processContext", "active models = " + activeModelsString);
		
	}
	// END CONTEXT CHANGE HANDLING CODE

	/* RUNNABLES TO HANDLE SCHEDULING TIMES */

	private Runnable launchScheduledInterview = new Runnable() {
		public void run() {
			boolean iAmRunning = false;
			int numEMAs = 0;
			try {
				numEMAs = inferenceService.getNumEMAsToday();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// reschedule(PERIODIC);
			if (Log.DEBUG) Log.d("launchScheduledInterview",
							"scheduled interview event fired");
			// verify that this is the launching interview in case of race
			// conditions
			// not strictly necessary since the interview activity should be
			// configured as singleInstance
			// check to make sure there was no race on this
			if (!interviewRunning && !withinGracePeriod && numEMAs < MAX_DAILY_EMA) {
				if (Log.DEBUG) Log.d("launchScheduledInterview",
						"No currently running interview");
				interviewRunning = true;
				iAmRunning = true;
				// keyguard.disableKeyguard();
				// interviewScheduled = false;
				lastInterviewTime = SystemClock.uptimeMillis();// .elapsedRealtime();
				launchType = EMALogConstants.TYPE_PERIODIC;
				// startActivityForResult(interviewIntent, INTERVIEW_REQUEST);
				startActivity(interviewIntent);
				if (Log.DEBUG) Log.d("launchScheduledInterview",
								"scheduled interview started");
			}
			 reschedule(PERIODIC, iAmRunning);
			// if (launchType == TYPE_PERIODIC || launchType ==
			// TYPE_INTERRUPTED_BY_PERIODIC) {
			// startActivityForResult(interviewIntent, INTERVIEW_REQUEST);
			// if (Log.DEBUG) Log.d("launchScheduledInterview", "scheduled interview started");
			// }
		}
	};

	private Runnable launchContextTimeInterview = new Runnable() {
		public void run() {
			boolean iAmRunning = false;
			if (Log.DEBUG) Log.d("launchContextTimeInterview",
					"context time interview event fired");
			// verify that this is the launching interview in case of race
			// conditions
			// not strictly necessary since the interview activity should be
			// configured as singleInstance
			if (!interviewRunning && !withinGracePeriod) {
				if (Log.DEBUG) Log.d("launchContextTimeInterview",
						"No currently running interview");
				interviewRunning = true;
				iAmRunning = true;
				// keyguard.disableKeyguard();
				// interviewScheduled = false;
				lastInterviewTime = SystemClock.uptimeMillis();// .elapsedRealtime();
				launchType = EMALogConstants.TYPE_CONTEXT_TIME;
				// startActivityForResult(interviewIntent, INTERVIEW_REQUEST);
				startActivity(interviewIntent);
				if (Log.DEBUG) Log.d("launchContextTimeInterview",
						"context time interview started");
			}
			reschedule(CONTEXT_TIME, iAmRunning);
			// if (launchType == TYPE_CONTEXT_TIME || launchType ==
			// TYPE_INTERRUPTED_BY_CONTEXT_TIME) {
			// startActivityForResult(interviewIntent, INTERVIEW_REQUEST);
			// if (Log.DEBUG) Log.d("launchContextTimeInterview",
			// "context time interview started");
			// }
		}
	};

	private Runnable launchContextChangeInterview = new Runnable() {
		public void run() {
			boolean iAmRunning = false;
			if (Log.DEBUG) Log.d("launchContextChangeInterview",
					"context change interview event fired");

			// check to make sure the scheduler is not in a disabled state
			if (!schedulerPaused) {
				// verify that this is the launching interview incase of race
				// conditions
				// not strictly necessary since the interview activity should be
				// configured as singleInstance
				if (!interviewRunning) { // && !withinGracePeriod) {
					if (Log.DEBUG) Log.d("launchContextChangeInterview",
							"No currently running interview");
					interviewRunning = true;
					iAmRunning = true;
					// keyguard.disableKeyguard();
					// interviewScheduled = false;
					lastInterviewTime = SystemClock.uptimeMillis();// .elapsedRealtime();
					launchType = EMALogConstants.TYPE_CONTEXT_CHANGE;
					// startActivityForResult(interviewIntent,
					// INTERVIEW_REQUEST);
					startActivity(interviewIntent);
					if (Log.DEBUG) Log.d("launchContextChangeInterview",
							"context change interview started");
				}
				reschedule(CONTEXT_CHANGE, iAmRunning);
				// if (launchType == TYPE_CONTEXT_CHANGE || launchType ==
				// TYPE_INTERRUPTED_BY_CONTEXT_CHANGE) {
				// startActivityForResult(interviewIntent, INTERVIEW_REQUEST);
				// if (Log.DEBUG) Log.d("launchContextChangeInterview",
				// "context change interview started");
				// }
			}
		}
	};

	private Runnable gracePeriodOver = new Runnable() {
		public void run() {
			withinGracePeriod = false;
			if (Log.DEBUG) Log.d("gracePeriodOver",
					"context change interviews have been reenabled");
		}
	};

	private Runnable launchEODInterview = new Runnable() {
		public void run() {
			if (interviewRunning) {
				launchEOD = true;
			} else {
				// startActivityForResult(eodIntent, EOD_REQUEST);
				// keyguard.disableKeyguard();
				startActivity(eodIntent);
				launchType = EMALogConstants.TYPE_EOD;
			}
		}
	};

	private Runnable STRESSOR = new Runnable() {
		public void run() {
			// disable all interview runnables
			scheduler.removeCallbacks(launchScheduledInterview);
			scheduler.removeCallbacks(launchContextTimeInterview);
			scheduler.removeCallbacks(launchContextChangeInterview);
			if (Log.DEBUG) Log.d("stressor runnable", "removingCallbacks");
			// scheduler.removeCallbacks(gracePeriodOver);
			schedulerPaused = true;
			if (Log.DEBUG) Log.d("STRESSOR", "removed all interview callbacks");
			

			try {
				inferenceService.setFeatureComputation(false);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// post the restart scheduler runnable 
			long restartTime = stressorStopTime - System.currentTimeMillis();
			scheduler.postAtTime(restartScheduler, SystemClock.uptimeMillis() + restartTime);
					//+ (3 * HOUR_MILLIS));
			if (Log.DEBUG) Log.d("STRESSOR", "Posted restart runnable for: " + restartTime + " ms");
		}
	};

	private Runnable EOD = new Runnable() {
		public void run() {
			// disable all interview runnables
			scheduler.removeCallbacks(launchScheduledInterview);
			scheduler.removeCallbacks(launchContextTimeInterview);
			scheduler.removeCallbacks(launchContextChangeInterview);
			// scheduler.removeCallbacks(gracePeriodOver);
			schedulerPaused = true;
			
			if (Log.DEBUG) Log.d("EOD", "removed all interview callbacks for the day");

			// post the restart scheduler runnable for next day if needed
			atEOD = true;
			if (startOfDay > 0) {
				if (Log.DEBUG) Log.d("InterviewScheduler","End Of Day");
				long diff = startOfDay - System.currentTimeMillis();
				if (Log.DEBUG) Log.d("InterviewScheduler","scheduler for SOD in "+diff);
				if (diff > 0) {
					
					scheduler.postAtTime(restartScheduler, SystemClock.uptimeMillis() + diff);
					Time t = new Time();
					t.set(startOfDay);
					if (Log.DEBUG) Log.d("Posted SOD restart scheduler event", ""+t.year+" "+t.month+" "+t.monthDay+" "+t.hour+" "+t.minute+" "+t.second);
				} else {
					// already passed the start time?
					scheduler.post(restartScheduler);
					if (Log.DEBUG) Log.d("Posted SOD", "immediate - already passed the start time");
				}
			}
			// set up the end of day interview
			if (HAS_EOD_INTERVIEW) {
				long timeTilEOD = endOfDay = System.currentTimeMillis();
				if (timeTilEOD > 300000 && !interviewRunning) {
					scheduler.postAtTime(launchEODInterview, SystemClock
							.uptimeMillis()
							+ timeTilEOD);
				} else {
					// launch eod interview upon completion of the currently
					// running one
					launchEOD = true;
				}
			}
		}

	};

	private Runnable restartScheduler = new Runnable() {
		public void run() {
			
			if (Log.DEBUG) Log.d("restartScheduler", "ran");
			schedulerPaused = false;
			nextPeriodicTime = SystemClock.uptimeMillis();
			try {
				inferenceService.setFeatureComputation(true);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (atEOD) {
				atEOD = false;
				// TODO this should probably launch a reminder activity and not
				// just an interview?
				// TODO this should repost the EOD event to the scheduler?
				if (Log.DEBUG) Log.d("restartScheduler", "posted periodic interview");
				scheduler.post(launchScheduledInterview);
				
				if (Log.DEBUG) Log.d("InterviewScheduler","Start Of Day");
//				scheduler.postAtTime(launchScheduledInterview, SystemClock
//						.uptimeMillis()
//						+ PERIODIC_INTERVAL);
			} else {
				// returning from the STRESSOR disable, so launch an interview
				// immediately
				// TODO: this event should be removed if the user initiates
				// interviews immediately after
				// the stressor like they are supposed to
				if (Log.DEBUG) Log.d("restartScheduler", "posted periodic interview");
				scheduler.post(launchScheduledInterview);
			}
		}
	};

	/* BEGIN IMPLEMENT THE SCHEDULER SERVICE INTERFACE */
	private ISchedulerService.Stub schedulerService = new ISchedulerService.Stub() {

		public void interviewInterrupted(Bundle state) throws RemoteException {

			// keyguard.reenableKeyguard();
			int type = state.getInt("interviewType");
			Intent restartInterview = new Intent(getBaseContext(),
					Interview.class);
			if (type == EOD_REQUEST) {
				restartInterview = new Intent(getBaseContext(),
						EODInterview.class);
			}
			restartInterview.putExtra("STATE", state);
			restartInterview.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(restartInterview);

		}

		public void interviewCompleted(Intent result) throws RemoteException {

			long promptTime, delayTime, startTime;
			String[] delayResponses;
			long[] responseTimes;
			long[] delayResponseTimes;
			int emaStatus;
			String[] interviewResponses;
			String[] eodResponses;
			
			int requestCode = result.getIntExtra("interviewType", -1);
			//String interviewResult, logEntry;
			
			// extract the common log data between interview types
			if (requestCode == INTERVIEW_REQUEST || requestCode == EOD_REQUEST) {
				promptTime = result.getLongExtra(EMALogConstants.PROMPT_TIME, -1);
				delayTime = result.getLongExtra(EMALogConstants.DELAY_TIME, -1);
				startTime = result.getLongExtra(EMALogConstants.START_TIME, -1);
				emaStatus = result.getIntExtra(EMALogConstants.EMA_STATUS, -1);
				delayResponseTimes = result.getLongArrayExtra(EMALogConstants.DELAY_RESPONSE_TIMES);
				responseTimes = result.getLongArrayExtra(EMALogConstants.RESPONSE_TIMES);
				interviewResponses = null;
				delayResponses = new String[delayResponseTimes.length];
				for (int i=0; i<delayResponseTimes.length; i++) {
					Serializable response = result.getSerializableExtra(EMALogConstants.DELAY_RESPONSES+i);
					if (response == null) {
						delayResponses[i] = "";
					}
					else {
						try {
							delayResponses[i] = (String)response;
						} catch(ClassCastException e) {
							delayResponses[i] = "" + (Integer)response;
						}						
					}
				}
				
				//}
				//StringBuilder sb;
				Log.i("onActivityResult",
						"Scheduler received result from Interview");
				// retrieve and format the result form the intent object
				switch (requestCode) {
				case INTERVIEW_REQUEST:
					// if (resultCode == Activity.RESULT_CANCELED) {
					// // something bad happened
					// Log.e("onActivityResult", "Interview failed!!");
					//					
					// } else {
					// received a result from the interview
					Log.i("onActivityResult", "Interview returned correctly");
					interviewResponses = new String[responseTimes.length];
					for (int i=0; i<responseTimes.length; i++) {
						Serializable response = result.getSerializableExtra(EMALogConstants.RESPONSES+i);
						if (response == null) {
							interviewResponses[i] = "";
						}
						else {
							try {
								interviewResponses[i] = (String)response;
							} catch(ClassCastException e) {
								interviewResponses[i] = "" + (Integer)response;
							}
						}						
					}
					
	//				interviewResult = result.getStringExtra("RESULT");
	//				sb = new StringBuilder();
	//				sb.append(launchType).append(",").append(interviewResult);
	//				logEntry = sb.toString();
	//				if (Log.DEBUG) Log.d("onActivityResult", logEntry);
					// }
					break;
				case EOD_REQUEST:
					// if (resultCode == Activity.RESULT_CANCELED) {
					// // badness
					// Log.e("onActivityResult", "EOD interview failed!");
					//					
					// } else {
					addEODLogHeader();
					// recieved a result from the interview
					if (Log.DEBUG) Log.d("onActivityResult", "EOD interview returned correctly");
					eodResponses = new String[responseTimes.length];
					for (int i=0; i<responseTimes.length; i++) {
						String response = (String)result.getSerializableExtra(EMALogConstants.RESPONSES+i);
						if (response != null) {
							eodResponses[i] = response;
						} else {
							eodResponses[i] = "";
						}
					}

					//eodResponses = (String[])result.getSerializableExtra(EMALogConstants.RESPONSES);
	//				interviewResult = result.getStringExtra("RESULT");
	//				sb = new StringBuilder();
	//				sb.append(TYPE_EOD).append(",").append(interviewResult);
	//				logEntry = sb.toString();
	//				if (Log.DEBUG) Log.d("onActivityResult", logEntry);
					// }
	
					break;
				default:
					//logEn	try = null;
				}

			// log the entry
//			if (logEntry != null) {
				try {
					//inferenceService.writeLabel("");
					if (requestCode == INTERVIEW_REQUEST && inferenceService != null) {
						inferenceService.writeEMALog(launchType, activeModelsString, emaStatus, promptTime, delayTime, delayResponses, delayResponseTimes, startTime, interviewResponses, responseTimes);						
					}
					
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (launchEOD) {
				launchEOD = false;
				startActivity(eodIntent);
				launchType = EMALogConstants.TYPE_EOD;
			} else {

				// reset state variables
				String[] id = activeModelsString.split(" ");
				if (id.length == 2) {
					budgeter.updateBudget(id[0]);
				}
				else {
					budgeter.updateBudget("other");
				}
				interviewRunning = false;
				// keyguard.reenableKeyguard();
				launchType = -1;
			}

		}

		public void registerCallback(IInterviewCancellationCallback interview)
				throws RemoteException {

			interviewCallback = interview;

		}

		public void unregisterCallback(IInterviewCancellationCallback interview)
				throws RemoteException {

			if (interviewCallback.equals(interview))
				interviewCallback = null;
		}

		public void reenableInterviews() throws RemoteException {
			schedulerPaused = false;
			scheduler.removeCallbacks(restartScheduler);
			scheduler.post(launchScheduledInterview);
			Log.i("reenableInterviews", "removingCallbacks");
			// TODO anything I am forgetting?
		}		
	};

	/* END IMPLEMENT THE SCHEDULER SERVICE INTERFACE */

	@Override
	public IBinder onBind(Intent arg0) {

		return schedulerService;
	}

	public static InterviewScheduler getInstance() {
		return INSTANCE;
	}

	private EMABudgeter budgeter = null;
	private EMATriggerer triggerer = null;
	
	private void initEMABudget() {
		budgeter = new EMABudgeter();
		
		budgeter.setTotalBudget(MAX_DAILY_EMA);
		budgeter.setMinTimeBeforeNext(GRACE_PERIOD);
		
		// speaking, smoking, walking, commuting, other
		budgeter.addOrUpdateItem("" + Constants.MODEL_CONVERSATION + ":" + ConversationPrediction.SPEAKING, 2);		
		budgeter.addOrUpdateItem("" + Constants.MODEL_CONVERSATION + ":" + ConversationPrediction.SMOKING, 2);
		budgeter.addOrUpdateItem("" + Constants.MODEL_ACTIVITY + ":" + ActivityCalculation.WALK, 2);
		budgeter.addOrUpdateItem("" + Constants.MODEL_ACCUMULATION, 4);
		budgeter.addOrUpdateItem("" + Constants.MODEL_GPSCOMMUTING + ":" + CommutingCalculation.COMMUTING, 2);
		budgeter.addOrUpdateItem("" + Constants.MODEL_SELF_DRINKING,1);
		budgeter.addOrUpdateItem("" + Constants.MODEL_SELF_SMOKING, 4);
		
		budgeter.addOrUpdateItem("other", 16);
		
		budgeter.loadChargesFromDB();
				
	}
	
	
	private void initEMATriggerer(EMABudgeter budgeter){
		triggerer = new EMATriggerer(budgeter);
		
	}
		
	public static final int NO_INCENTIVE_SCHEME = 3;
	public static final int UNIFORM_INCENTIVE_SCHEME = 0;
	public static final int VARIABLE_INCENTIVE_SCHEME = 1;
	public static final int HIDDEN_INCENTIVE_SCHEME = 2;
	public static final int UNIFORM_AND_BONUS_INCENTIVE_SCHEME = 4;	

	public static int INCENTIVE_SCHEME = UNIFORM_AND_BONUS_INCENTIVE_SCHEME;
		
	private void initIncentiveManager() {
		if (INCENTIVE_SCHEME == NO_INCENTIVE_SCHEME) {
			incentives = null;
			return;
		}
					
		incentives = new EMAIncentiveManager();
		incentives.setNumQuestions(getContent().getNumberQuestions(true));
		
		if (INCENTIVE_SCHEME != UNIFORM_AND_BONUS_INCENTIVE_SCHEME)
			incentives.setPerQuestion(true);
		else
			incentives.setPerQuestion(false);

		// on average, this distribution leads to EMAs worth ~$1.10, a little high compared to the uniform max of $1.04
		// produces an expected value of ~$1.04
//		float[] distribution = {
//				0.281818182f,      62/220
//				0.213636364f,      47/220
//				0.172727273f,      38/220
//				0.122727273f,      27/220
//				0.068181818f,      15/220
//				0.040909091f,       9/220
//				0.031818182f,       7/220
//				0.027272727f,       6/220
//				0.018181818f,       4/220
//				0.013636364f,       3/220
//				0.009090909f};      2/220

		// on average, this distribution leads to EMAs 
		// worth $1.05, much closer to the uniform max of $1.04
		// also happens to produce an expected value of $1.00 (exactly)
		float[] distribution = {
				0.304545455f,    // 67/220
				0.236363636f,    // 52/220
				0.163636364f,    // 36/220
				0.109090909f,    // 24/220
				0.054545455f,    // 12/220
				0.036363636f,    //  8/220
				0.031818182f,    //  7/220
				0.022727273f,    //  5/220
				0.018181818f,    //  4/220
				0.013636364f,    //  3/220
				0.009090909f     //  2/220
		};		
		
		BigDecimal[] amounts ={
				new BigDecimal("0.50").setScale(2, BigDecimal.ROUND_HALF_EVEN),
				new BigDecimal("0.75").setScale(2, BigDecimal.ROUND_HALF_EVEN),
				new BigDecimal("1.00").setScale(2, BigDecimal.ROUND_HALF_EVEN),
				new BigDecimal("1.25").setScale(2, BigDecimal.ROUND_HALF_EVEN),
				new BigDecimal("1.50").setScale(2, BigDecimal.ROUND_HALF_EVEN),
				new BigDecimal("1.75").setScale(2, BigDecimal.ROUND_HALF_EVEN),
				new BigDecimal("2.00").setScale(2, BigDecimal.ROUND_HALF_EVEN),
				new BigDecimal("2.25").setScale(2, BigDecimal.ROUND_HALF_EVEN),
				new BigDecimal("2.50").setScale(2, BigDecimal.ROUND_HALF_EVEN),
				new BigDecimal("2.75").setScale(2, BigDecimal.ROUND_HALF_EVEN),
				new BigDecimal("3.00").setScale(2, BigDecimal.ROUND_HALF_EVEN)};
		
		
		if (INCENTIVE_SCHEME == UNIFORM_AND_BONUS_INCENTIVE_SCHEME) {		
			incentives.setUniform(new BigDecimal(1));
			incentives.setIncentiveVisible(true);		
			incentives.setBonusTime(300000);
			incentives.setBonusAmount(new BigDecimal(1).divide(new BigDecimal(4)));
		}
		else if (INCENTIVE_SCHEME == UNIFORM_INCENTIVE_SCHEME) {
			incentives.setUniform(new BigDecimal(1));
			incentives.setIncentiveVisible(true);
		}
		else if (INCENTIVE_SCHEME == VARIABLE_INCENTIVE_SCHEME) {			
			incentives.setVariable(distribution, amounts);
			incentives.setIncentiveVisible(true);			
		}
		else if (INCENTIVE_SCHEME == HIDDEN_INCENTIVE_SCHEME) {
			incentives.setVariable(distribution, amounts);
			incentives.setIncentiveVisible(false);
		}
	
		incentives.loadIncentivesEarned();
	}
	

}
