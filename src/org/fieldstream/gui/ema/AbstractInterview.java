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
// @author Andrew Raij


package org.fieldstream.gui.ema;

import java.io.Serializable;
import java.text.NumberFormat;

import org.fieldstream.Constants;
import org.fieldstream.incentives.EMAIncentiveManager;
import org.fieldstream.service.logger.Log;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnKeyListener;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Vibrator;
import android.os.PowerManager.WakeLock;
import edu.cmu.ices.stress.phone.gui.ema.IInterviewCancellationCallback;
import edu.cmu.ices.stress.phone.gui.ema.ISchedulerService;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;


public abstract class AbstractInterview extends Activity {
	AbstractInterview INSTANCE = null;
	
	// UI controls
	Activity context;
	RelativeLayout layout;
	View responseView;
	TextView questionView;
	Button backButton;
	Button nextButton;
//	ArrayAdapter<String> responseAdapter;
//	ArrayList<String> list;
	AlertDialog startDialog;
	Menu delayMenu;

	// incentive stuff
	boolean showIncentive = true;
	View incentiveView;
	TextView incentiveTotalView;
	TextView incentiveCurrentView;
	
	// interview state
	int currQuestion;
//	Queue<Integer> lastSelected;
//	Queue<Long> lastRow;
//	long tempRow;
	int state;
	//int endFunction = 2; // default behavior is sleep and lock
	boolean backUsed = false;
	int promptTimes;
	Bundle savedState;
	boolean debugMode;
	boolean labMode;
	boolean homeKeyPressed = false;
	int interviewType;
		
	// interview content and data
	IContent content;
	InterviewData entry;
	
	// handles timing events
	Handler handler;
	
	// handles for the two locking services
	KeyguardLock keyguard;
	WakeLock wakelock;
	
	// monitor service
	ISchedulerService schedulerService;
	boolean interviewIsCancelled;
	
	// incentives manager for payment feedback at end of interview
	EMAIncentiveManager incentives = null;
	
	// interview constants
	static final String START_MESSAGE = "Do you want to start the interview now?";
	static final String START_STRING = "Start";
	static final String DELAY_STRING = "Delay 10 Minutes";
	static final long MINUTE_MILLIS = (60 * 1000);
	static final long DELAY_TIME = (10 * MINUTE_MILLIS); //(15 * MINUTE_MILLIS);
	static final long START_TIMEOUT = (5 * MINUTE_MILLIS); //(5 * MINUTE_MILLIS);
	static final long INTERVIEW_TIMEOUT = (5 * MINUTE_MILLIS); //(5 * MINUTE_MILLIS);
	static final long END_TIMEOUT = 10000;
	
	// state constants
	static final int AT_START = 0;
	static final int POST_DELAY = 1;
	static final int DELAYED = 2;
	static final int CONDUCTING = 3;
	static final int TIMED_OUT = 4;
	static final int DONE = 5;
	
	// delay menu constants
	static final int BREAK_DELAY = 0;
	static final int CANCEL = 1;
	
	// phone key constants
	static final int END_LOCKS_PHONE = 2;
	static final int END_DISABLED = 0;
	
	// prompt control
	static final int VOLUME = 100;
	static final int PROMPT_INTERVAL = 500;
	static final int PROMPT_REPEAT = 5;

	
	/* Abstract Methods */
	public abstract void initResponseView();
	public abstract Serializable selectedResponse();
	public abstract void clearResponseView();
	public abstract void updateResponseView(String[] res);
	public abstract void hideResponseView();
	public abstract void showResponseView();
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	INSTANCE = this;
//    	super.onCreate(savedInstanceState);
//
//        // Use an existing ListAdapter that will map an array
//        // of strings to TextViews
//        setListAdapter(new ArrayAdapter<String>(this,
//                android.R.layout.simple_list_item_1, test));
//        getListView().setTextFilterEnabled(true);

        super.onCreate(savedInstanceState);
        
        incentives = InterviewScheduler.getInstance().getIncentiveMangager();
        if (incentives != null) {
	        incentives.reset(false);
        }

    	initInterviewState();
        initUI();
        handler = new Handler();        
        
        long time = System.currentTimeMillis();
        context = this;
//        setContentView(R.layout.interview_layout);
//        
//        questionView = (TextView) findViewById(R.id.Question);
//        responseView = (ListView) findViewById(R.id.List);
//        //responseList.setFocusable(true);
//        backButton = (Button) findViewById(R.id.BackButton);
//        nextButton = (Button) findViewById(R.id.NextButton);
        if (savedInstanceState != null) {
        	// created with some past state, possible recovering from an activity disposed of by
        	// the operating system
        	onRestoreInstanceState(savedInstanceState);
        } 
//        else {
//        	initInterviewState();
//	        initUI();
//	        handler = new Handler();
////	        handler.post(updateView);
////	        handler.post(launchStartDialog);
//	        
//        }
        Intent intent = getIntent();
        debugMode = intent.getBooleanExtra("DEBUG", false);
        labMode = intent.getBooleanExtra("LAB_MODE", false);
        interviewType = intent.getIntExtra("interviewType", 0);
        savedState = intent.getBundleExtra("STATE");
        if (savedState != null) {
        	onRestoreInstanceState(savedState);
        }
        bindSchedulerService();
        //responses.setTextFilterEnabled(true);

        handler.post(updateView);
        
        if (labMode) {
        	entry.setStartTime(time);
        	entry.setPromptTime(time);
        	state = CONDUCTING;
        	//state = AT_START;
        }
        
        
        // handle recovering the timeout behavior when restoring from interruption
        switch(state) {
        case AT_START:
        	long promptTime = entry.getPromptTime();
        	if (promptTime == 0) {
        		// first time through
        		//handler.postDelayed(timeoutInterview, START_TIMEOUT);
        		handler.post(launchStartDialog);
        	} else {
        		// returning from interrupt
        		if (time - promptTime >= START_TIMEOUT) {
        			// timed out
        			handler.post(timeoutInterview);
        		} else {
        			// interrupted in the middle of the start dialog
        			handler.postDelayed(timeoutInterview, START_TIMEOUT - (time - promptTime));
        			handler.post(launchStartDialog);
        		}
        	}
        	break;
        case DELAYED:
        	long startDelay = entry.getDelayStart();
        	long stopDelay = entry.getDelayStop();
        	if (stopDelay == -1) {
        		// still in the delay
        		if (time - startDelay >= DELAY_TIME) {
        			// delay should have timed out already
        			//handler.postDelayed(timeoutInterview, START_TIMEOUT);
        			handler.post(launchStartDialog);
        		} else {
        			long delayLeft = DELAY_TIME - (time - startDelay);
        			//handler.postDelayed(timeoutInterview, START_TIMEOUT + delayLeft);
        			handler.postDelayed(launchStartDialog, delayLeft);
        		}
        	} else {
        		// returning from delay already and back at start prompt
        		if (time - stopDelay >= START_TIMEOUT) {
        			// timed out
        			handler.post(timeoutInterview);
        		} else {
        			// interrupted in the middle of the start dialog
        			handler.postDelayed(timeoutInterview, START_TIMEOUT - (time - stopDelay));
        			handler.post(launchStartDialog);
        		}
        	}
        	break;
        case POST_DELAY:
        	// falls through to conducting
        case CONDUCTING:
        	long startTime = entry.getStartTime();
        	if (time - startTime >= INTERVIEW_TIMEOUT) {
        		// timeout interval for the interview has passed
        		handler.post(timeoutInterview);
        	} else {
        		handler.postDelayed(timeoutInterview, INTERVIEW_TIMEOUT - (time - startTime));
        	}
        	break;
        case TIMED_OUT:
        	handler.post(timeoutInterview);
        	break;
        case DONE:
        	handler.post(timeoutInterview);
        	break;
        }
//        if (state == AT_START || state == DELAYED) {
//        	handler.post(launchStartDialog);
//        }

        
    }
    
    /* This is called immediately after onCreate() when the app is started
	 * or after the app comes back from a pause.
	 */
	@Override
	protected void onResume() {
		//handler.post(updateView);

		android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.END_BUTTON_BEHAVIOR, END_DISABLED);
		keyguard.disableKeyguard();
		wakelock.acquire();
		Log.i("Interview", "onResume");
		super.onResume();
		
	}

	/* This is called when the app goes into the background. */
	@Override
	protected void onPause() {
		Log.i("Interview", "onPause");
		//layout.requestFocus();
		// restore the end button behavior
		android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.END_BUTTON_BEHAVIOR, END_LOCKS_PHONE);
		// reenable the keyguard
		keyguard.reenableKeyguard();
		if (wakelock.isHeld()) {
			wakelock.release();
		}
		
		// make sure the interview is finished and not being interrupted
		if (state != DONE && state != TIMED_OUT && homeKeyPressed) {
			// the interview has been interrupted by some other task
			if (Log.DEBUG) Log.d("onPause", "MSG: interview interrupted!");
			
			if (schedulerService != null) {
				try {
					schedulerService.interviewInterrupted(savedState);
					if (startDialog != null) {
						if (startDialog.isShowing()) {
							startDialog.cancel();
						}
					}
					if (wakelock != null) {
						if (wakelock.isHeld())
							wakelock.release();
					}
					finish();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}

			homeKeyPressed = false;
		}
		
		super.onPause();
	}

	/* This is called when the app is killed. */
	@Override
	protected void onDestroy() {
		incentives = null;
		
		Log.i("Interview", "onDestroy");
		// make sure all the handler events are disposed, even though there shouldn't be any left
		handler.removeCallbacks(timeoutInterview);
		handler.removeCallbacks(launchStartDialog);
		handler.removeCallbacks(updateView);

		unbindSchedulerService();
		super.onDestroy();
		

	}
	
	
	@Override
	// this callback indicates that a user action has caused focus to be lost
	protected void onUserLeaveHint() {
		Log.i("AbstractInterview", "onUserLeaveHint()");

		homeKeyPressed = true;
		super.onUserLeaveHint();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
	  // Save UI state changes to the savedInstanceState.
	  // This bundle will be passed to onCreate if the process is
	  // killed and restarted.
		super.onSaveInstanceState(savedInstanceState);
		Log.i("Interview", "onSaveInstanceState");
		// save recorded data
		entry.onSaveInstanceState(savedInstanceState);
		// save interview state
		savedInstanceState.putInt("currQuestion", currQuestion);
		savedInstanceState.putInt("state", state);
		savedInstanceState.putBoolean("backUsed", backUsed);
		savedInstanceState.putInt("promptTimes", promptTimes);
		savedInstanceState.putBoolean("debugMode", debugMode);
		savedInstanceState.putInt("interviewType", interviewType);
		// etc.
		
		// keep a pointer to the bundle in case I need it later
		savedState = savedInstanceState;
		
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// Restore UI state from the savedInstanceState.
		// This bundle has also been passed to onCreate.
		entry.onRestoreInstanceState(savedInstanceState);
		currQuestion = savedInstanceState.getInt("currQuestion");
		state = savedInstanceState.getInt("state");
		backUsed = savedInstanceState.getBoolean("backUsed");
		promptTimes = savedInstanceState.getInt("promptTimes");
		debugMode = savedInstanceState.getBoolean("debugMode");
		interviewType = savedInstanceState.getInt("interviewType");
		Log.i("Interview", "onRestoreInstanceState");
	}

	/* END ANDROID LIFE CYCLE */

	/* BEGIN INITIALIZATION METHODS */
	
	private void initInterviewState() {
		// init interview state
		currQuestion = 0;
		state = AT_START;
		promptTimes = PROMPT_REPEAT;
		
		interviewIsCancelled = false;
		
		// init interview data
//		content = new InterviewContent();
		entry = new InterviewData(content.getNumberQuestions(false), content.getNumberDelayQuestions());
		
//		lastSelected = new LinkedList<Integer>();
//		lastRow = new LinkedList<Long>();
		
//		// init the keyguard lock manager
		KeyguardManager mgr = (KeyguardManager)this.getSystemService(KEYGUARD_SERVICE);
		keyguard = mgr.newKeyguardLock("EMA_Interview");
		
		PowerManager pm = (PowerManager)this.getSystemService(POWER_SERVICE);
		//wakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "AbstractInterview");
		wakelock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "AbstractInterview");	
	}
	
	//abstract void initInterviewContents();
	
	/*
	 * Adds click event listeners to the next and back buttons
	 */
	private void initUI() {
		// clear question text by default
		questionView.setText("");
//		question.setOnFocusChangeListener(new OnFocusChangeListener() {
//
//			@Override
//			public void onFocusChange(View arg0, boolean arg1) {
//				if (!responseList.hasFocus() && !back.hasFocus() && !next.hasFocus())
//					arg0.requestFocus();
//			}
//			
//		});
		
		// layout stuff
//		layout.setOnFocusChangeListener(new OnFocusChangeListener() {
//
//			public void onFocusChange(View arg0, boolean arg1) {
//				if (!arg1) {
//					layout.requestFocus();
//				}
//			}
//			
//		});
		
		// set up a the response list adapter and click listener
//		ArrayList<String> list = new ArrayList<String>();
//		responseAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_checked, list);
//		responseView.setAdapter(responseAdapter);
//		responseView.setOnItemClickListener(new OnItemClickListener() {
//
//			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
//					long arg3) {
//				//selectedResponse[currQuestion] = arg2;
////				arg1.setSelected(true);
////				View responsible = responseList.getSelectedView();
////				responsible.setPressed(true);
////				tempRow = arg3;
//				System.out.println("response " + arg2);
//				nextButton.setVisibility(View.VISIBLE);
//			}
//			
//		});
		
		
		// set up the next button visibility and click listener
		//next.setVisibility(View.INVISIBLE);
		nextButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Serializable selection = selectedResponse();
										
					if (state == POST_DELAY) {
						try {
							long time = System.currentTimeMillis();
//							int response = Integer.parseInt(selection.toString());
							entry.setDelayResponse(currQuestion, selection, time);

			        		handler.removeCallbacks(timeoutInterview);
			        		handler.postDelayed(timeoutInterview, INTERVIEW_TIMEOUT);
										                
							currQuestion++;
							if (currQuestion >= content.getNumberDelayQuestions()) {
								
								state = CONDUCTING;
								currQuestion = 0;
								entry.setDelayStop(System.currentTimeMillis());
	//			                questionView.setVisibility(View.VISIBLE);
				                promptTimes = PROMPT_REPEAT;				                
				                
				                if (content.hasResponses(currQuestion)) {
				                	if (incentives != null) {
				                		if (incentives.isPerQuestion()) {
				                			String incentiveID = "" + currQuestion;
				                			incentives.requestIncentive(incentiveID);
				                		}
				                	}
				                }
							}
							else {
								if (content.hasDelayResponses(currQuestion)) {
									if (incentives !=null) {
				                		if (incentives.isPerQuestion()) {
											String incentiveID = "DELAY" + currQuestion;
											incentives.requestIncentive(incentiveID);
				                		}
									}
								}
							}
									
			                //handler.removeCallbacks(timeoutInterview);
			                //handler.postAtTime(timeoutInterview, SystemClock.uptimeMillis() + INTERVIEW_TIMEOUT);
			                handler.post(updateView);
			                //handler.postDelayed(launchStartDialog, DELAY_TIME);
			                //if (wakelock.isHeld())
			                //	wakelock.release();
						} catch (NumberFormatException e) {}
					
					} else {
						String amount = "";
						long time = System.currentTimeMillis();
						System.out.println("selected: "+selection +" @ " + time);
						entry.setResponse(currQuestion, selection, time);
						
		        		handler.removeCallbacks(timeoutInterview);
		        		handler.postDelayed(timeoutInterview, INTERVIEW_TIMEOUT);

	//					lastSelected.offer(new Integer(selection));
	//					lastRow.offer(new Long(tempRow));
	//					tempRow = -1;
//						try {
//							// test if the selected response results in skipping any future questions
//							int response = Integer.parseInt(selection.toString());
//							if (response >= 0) {
//								int skip = content.skipCount(currQuestion, response);
//								for (int i=0; i<skip; i++) {
//									currQuestion ++;
//									entry.setResponse(currQuestion, -1, time);
//								}
//							}
//						} catch (NumberFormatException nfe) {
//							// selection is not an integer value
//						}
						currQuestion++;
						
						while (!content.isQuestionActive(currQuestion, entry) && currQuestion < content.getNumberQuestions(false)) {
							currQuestion++;
						}
						
						backButton.setVisibility(View.VISIBLE);
						if (currQuestion >= content.getNumberQuestions(false)) {
							// finished with the interview
							state = DONE;
							//entry.setCompleted();
							entry.setStatus(EMALogConstants.COMPLETE);
							handler.removeCallbacks(timeoutInterview);
							handler.postDelayed(timeoutInterview, END_TIMEOUT);
							
					    	if (incentives != null) {
					    		if (!incentives.isPerQuestion()) {
					    			incentives.markIncentive("__", true);
					    		}
					    	}
						} 
						else {
			                if (content.hasResponses(currQuestion)) {
			                	if (incentives != null) {
			                		if (incentives.isPerQuestion()) {
				                		String incentiveID = "" + currQuestion;
				                		incentives.requestIncentive(incentiveID);
			                		}
			                	}
			                }
						}
					}
				
				handler.post(updateView);
			}
			
		});
		
		// set up the back button visibility and click listener
		//back.setVisibility(View.INVISIBLE);
		backButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (state == CONDUCTING && currQuestion == 0 && entry.getDelayStart() > 0  && content.getNumberDelayQuestions() > 0) {
					state = POST_DELAY;
					currQuestion = content.getNumberDelayQuestions() - 1;
				}
				else if (currQuestion > 0) {
				
					if (state == POST_DELAY) {
						currQuestion--;
					}
					else {
						do  {
							currQuestion--;
						} while (!content.isQuestionActive(currQuestion, entry) && currQuestion > 0);
					}
				}
				backUsed = true;
				//System.out.println("Current Question = " + currQuestion);
				handler.post(updateView);
				
			}
			
		});
		
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
	    return super.onKeyDown(keyCode, event);
	}
	
	/* Creates the menu items */
	public boolean onCreateOptionsMenu(Menu menu) {
	    if (state == DELAYED) {
			menu.add(0, BREAK_DELAY, 0, "Break Delay");
		    menu.add(0, CANCEL, 0, "Cancel");
		    delayMenu = menu;
	    }
	    return true;
	}

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case BREAK_DELAY:
	    	promptTimes = 0;
	    	entry.setDelayStop(System.currentTimeMillis());
	        handler.removeCallbacks(startInterview);
	        handler.post(startInterview);
	        

			entry.setDelayStop(System.currentTimeMillis());
			Log.i("launchStartDialog", "set delay stop time");

			if (delayMenu != null) {
				delayMenu.clear();
				delayMenu.close();
				delayMenu = null;
			}
	        
	        return true;
	    case CANCEL:
//	    	delayMenu = null;
	        return true;
	    }
	    return false;
	}
	
	/* BEGIN RUNNABLES USED FOR INTERIEW TIMING AND CONTROL FLOW */
	
	/*
	 * This runnable is used to run the prompting behavior of the application
	 */
	private Runnable promptUser = new Runnable() {
		public void run() {
			if (Constants.BUZZ) {
				// vibrate and beep to alert the user to a new interview
				Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
				ToneGenerator tone = new ToneGenerator(android.media.AudioManager.STREAM_NOTIFICATION, VOLUME);
	
				vibrator.vibrate(500);
				tone.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP);
			}
			
   			entry.setPromptTime(System.currentTimeMillis());
			//questionView.setVisibility(View.INVISIBLE);
			//questionView.setText("");
			
			// prompt repeat control
			if ((state == AT_START || state == DELAYED) && --promptTimes > 0) {
				handler.postDelayed(this, PROMPT_INTERVAL);
			}
		}
	};
	
	private Runnable startInterview = new Runnable() {
		public void run() {
	        entry.setStartTime(System.currentTimeMillis());
	        currQuestion = 0;
	        
	        if (incentives != null) {
		  	     if (!incentives.isPerQuestion()) {
	    			incentives.requestIncentive("__", entry.getStartTime()-entry.getPromptTime());
		  	     }
	        }	  	   	        
	        
	        if (state == DELAYED) {
	     	   state = POST_DELAY;
	   	 	   displayStartView();
	        } else {
	     	   state = CONDUCTING;
	        }

	//        questionView.setVisibility(View.VISIBLE);
	//        showResponseView();
	 	   //back.setVisibility(View.VISIBLE);
	 	   handler.removeCallbacks(timeoutInterview);
	 	   handler.removeCallbacks(promptUser);
	 	   questionView.setVisibility(View.VISIBLE);
	 	   

	 	   
	 	   handler.postDelayed(updateView, 7000);
	 	   handler.postDelayed(timeoutInterview, INTERVIEW_TIMEOUT);
		}
	};	
	/*
	 * This runnable object is posted to launch the start dialog box
	 */
	private Runnable launchStartDialog = new Runnable() {
		public void run() {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage(START_MESSAGE)
			       .setCancelable(false)
			       .setOnKeyListener(new OnKeyListener() {
			    	   // need to handle the hardware keys here as well as in the Interview activity
						public boolean onKey(DialogInterface dialog, int keyCode,
								KeyEvent keyEvent) {
							if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
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
							}
							// all other key presses are not handled
							return false;
						}
			       })
			       .setPositiveButton(START_STRING, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   handler.post(startInterview);
			           }
			       });

			       builder.setNegativeButton(DELAY_STRING, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			                state = DELAYED;
			                handler.removeCallbacks(promptUser);
			                handler.post(updateView);
			                entry.setDelayStart(System.currentTimeMillis());
//			                questionView.setVisibility(View.VISIBLE);
			                promptTimes = PROMPT_REPEAT;
			                handler.removeCallbacks(timeoutInterview);
			                handler.postDelayed(startInterview, DELAY_TIME);
			                if (wakelock.isHeld())
			                	wakelock.release();
			           }
			       });

			startDialog = builder.create();

			questionView.setVisibility(View.INVISIBLE);

			//handler.post(updateView);
			startDialog.show();
			
			if (promptTimes > 0)
				handler.postDelayed(promptUser, 500);
			handler.postDelayed(timeoutInterview, START_TIMEOUT);
			if (!wakelock.isHeld())
				wakelock.acquire();
		}
	};

	private static int TEXT_SIZE = 17;
	private static int HIGHLIGHTED_TEXT_SIZE = 19;

	
	
	protected void updateIncentiveView() {
		if (incentives == null) {
			incentiveView.setVisibility(View.INVISIBLE);
			incentiveTotalView.setText("");
			incentiveCurrentView.setText("");
			return;
		}

		incentiveTotalView.setText("Total: " + NumberFormat.getCurrencyInstance().format(incentives.getTotalEarned()));
		switch (state) {
		case AT_START:
			incentiveCurrentView.setText("");
			break;
		case CONDUCTING:
	        if (incentives.isIncentiveVisible()) {
	        	incentiveView.setVisibility(View.VISIBLE);

				if (content.hasResponses(currQuestion)) 
					incentiveCurrentView.setText("This Question: " + NumberFormat.getCurrencyInstance().format(incentives.getCurrentIncentive()) + " ");										
				
				if (incentives.incentiveEarned("" + currQuestion)) {
					incentiveTotalView.setTextSize(HIGHLIGHTED_TEXT_SIZE);
	//				incentiveCurrentView.setTextSize(HIGHLIGHTED_TEXT_SIZE);
					incentiveTotalView.setBackgroundColor(0xFF00FF00);
					incentiveCurrentView.setBackgroundColor(0xFF00FF00);
					incentiveView.setBackgroundColor(0xFF00FF00);
				}
				else {
					incentiveTotalView.setTextSize(TEXT_SIZE);
	//				incentiveCurrentView.setTextSize(TEXT_SIZE);
					incentiveTotalView.setBackgroundColor(0xFFFFFFFF);
					incentiveCurrentView.setBackgroundColor(0xFFFFFFFF);
					incentiveView.setBackgroundColor(0xFFFFFFFF);
				}
	        } 
	        else { 
	        	incentiveView.setVisibility(View.INVISIBLE);
	        }
			
			break;
		case POST_DELAY:
	        if (incentives.isIncentiveVisible()) {
	        	incentiveView.setVisibility(View.VISIBLE);

				if (content.hasDelayResponses(currQuestion)) 
					incentiveCurrentView.setText("This Question: " + NumberFormat.getCurrencyInstance().format(incentives.getCurrentIncentive()) + " ");								
				
				if (incentives.incentiveEarned("DELAY" + currQuestion)) {
					incentiveTotalView.setTextSize(HIGHLIGHTED_TEXT_SIZE);
		//			incentiveCurrentView.setTextSize(HIGHLIGHTED_TEXT_SIZE);
					incentiveTotalView.setBackgroundColor(0xFF00FF00);
					incentiveCurrentView.setBackgroundColor(0xFF00FF00);
					incentiveView.setBackgroundColor(0xFF00FF00);
				}
				else {
					incentiveTotalView.setTextSize(TEXT_SIZE);
	//				incentiveCurrentView.setTextSize(TEXT_SIZE);
					incentiveTotalView.setBackgroundColor(0xFFFFFFFF);
					incentiveCurrentView.setBackgroundColor(0xFFFFFFFF);
					incentiveView.setBackgroundColor(0xFFFFFFFF);
				}
	        }
	        else {
	        	incentiveView.setVisibility(View.INVISIBLE);
	        }
			break;
		case DELAYED:
        	incentiveView.setVisibility(View.INVISIBLE);
			break;
		case TIMED_OUT:
        	incentiveView.setVisibility(View.INVISIBLE);
			break;
		case DONE:
        	incentiveView.setVisibility(View.INVISIBLE);
			break;
		default:
			//shouldn't get here

		}		
	}
	
	public void displayStartView() {		
		questionView.setVisibility(View.VISIBLE);
		
		if (incentives != null) {		
			String award="???\n\nWe'll tell you what you earned after the interview.";
			if (incentives.isIncentiveVisible()) {
				award = "" + NumberFormat.getCurrencyInstance().format(incentives.getCurrentIncentive());
			}
			if (incentives.isPerQuestion()) {
				questionView.setText("\n\n\nEach question you answer in this interview will earn you " + award + ".");
			}
			else {
				questionView.setText("Please wait...questions will appear shortly...");	
			}
		
		}
		else {
			questionView.setText("Please wait...questions will appear shortly...");
		}
		
		hideResponseView();
		backButton.setVisibility(View.INVISIBLE);
		nextButton.setVisibility(View.INVISIBLE);				
	}
	
	/*
	 * This runnable object is posted to update the display after back or next is clicked
	 */
	private Runnable updateView = new Runnable() {
		public void run() {
			// clear current UI state
//			responseAdapter.clear();
//			responseView.clearChoices();
			clearResponseView();
				
			// update display according to the current state
			switch (state) {
			case AT_START:
				displayStartView();
				break;
			case CONDUCTING:
				questionView.setText(content.getQuestion(currQuestion));
		        //ArrayList<String> list = new ArrayList<String>();
				String[] res = content.getResponses(currQuestion);

				updateResponseView(res);
//				if (res != null) {
//					//String[] res = responses[mapping[currQuestion]];
//					int len = res.length;
//					for (int i=0; i< len; i++) {
//						responseAdapter.add(res[i]);
//					}
//					
////					if (backUsed) {
////						backUsed = false;
////						int sel = lastSelected.remove().intValue();
////						long row = lastRow.remove().longValue();
////						if (!responseAdapter.isEmpty()) {
////							View last = responseList.getChildAt(sel);
////							responseList.performItemClick(last, sel, row);
////						} else {
////							
////						}
////					}
//					nextButton.setVisibility(View.INVISIBLE);
//				} else {
//					
//					nextButton.setVisibility(View.VISIBLE);
//				}
				// set visibility of view elements
				questionView.setVisibility(View.VISIBLE);
				
				// if we had delay questions previously
				if (currQuestion > 0 || entry.getDelayStart() > 0) {
					backButton.setVisibility(View.VISIBLE);
				} else {
					backButton.setVisibility(View.INVISIBLE);
				}
				if (currQuestion >= content.getNumberQuestions(false)) {
					hideResponseView();
				} else {
					showResponseView();	
				}
				break;
			case POST_DELAY:
				
				questionView.setText(content.getDelayQuestion(currQuestion));
				updateResponseView(content.getDelayResponses(currQuestion));
				questionView.setVisibility(View.VISIBLE);
				if (currQuestion > 0) {
					backButton.setVisibility(View.VISIBLE);
				} else {
					backButton.setVisibility(View.INVISIBLE);
				}
				if (currQuestion >= content.getNumberDelayQuestions()) {
					hideResponseView();
				} else {
					showResponseView();	
				}
				break;
			case DELAYED:
				questionView.setVisibility(View.VISIBLE);
				questionView.setText("Interview is delayed. Press the menu button for options.");
				hideResponseView();
				backButton.setVisibility(View.INVISIBLE);
				nextButton.setVisibility(View.INVISIBLE);
				break;
			case TIMED_OUT:
				String interviewTotal;
				String questionAmount;
				String total;
				if (incentives != null) {
					interviewTotal = "" + NumberFormat.getCurrencyInstance().format(incentives.getCurrentInterviewTotal());
					questionAmount = "" + NumberFormat.getCurrencyInstance().format(incentives.getCurrentIncentive());
					total = "" + NumberFormat.getCurrencyInstance().format(incentives.getTotalEarned());
					
					if (incentives.isPerQuestion()) {
						questionView.setText("The interview has timed out.\n\n\nYou earned " 
								+ questionAmount + " per question for a total of "+ interviewTotal + " for this interview.\n\n" +
										"Overall, you have earned " + total + " for all of your interviews. Thank You!");
					}
					else {
						questionView.setText("The interview has timed out.");					
					}
				}
				else {
					questionView.setText("The interview has timed out.");					
				}
				hideResponseView();
				backButton.setVisibility(View.INVISIBLE);
				nextButton.setVisibility(View.INVISIBLE);
				break;
			case DONE:
				if (incentives!=null) {
					interviewTotal = "" + NumberFormat.getCurrencyInstance().format(incentives.getCurrentInterviewTotal());
					questionAmount = "" + NumberFormat.getCurrencyInstance().format(incentives.getCurrentIncentive());
					total = "" + NumberFormat.getCurrencyInstance().format(incentives.getTotalEarned());
					
					if (incentives.isPerQuestion()) {
					
						questionView.setText("Interview completed.\n\n\nYou earned " 
								+ questionAmount + " per question for a total of "+ interviewTotal + " for this interview.\n\n" +
										"Overall, you have earned " + total + " for all of your interviews.\n\nThank You!");				
					}
					else {
						questionView.setText("Interview completed.\n\n\nYou earned " 
								+ interviewTotal + " for this interview.\n\n" +
										"Overall, you have earned " + total + " for all of your interviews.\n\nThank You!");				
					}
					
					hideResponseView();
				}
				else {
					questionView.setText("Interview Completed.  Thank you!");
				}
				
				backButton.setVisibility(View.INVISIBLE);
				nextButton.setVisibility(View.INVISIBLE);
				break;
			default:
				//shouldn't get here
				questionView.setText("Unknown state reached!!!!!!");
			}
			
			updateIncentiveView();
		}  
	};
	
	/*
	 * This runnable handles all the timeout events for the interview
	 */
	private Runnable timeoutInterview = new Runnable() {
		public void run() {
			handler.removeCallbacks(timeoutInterview);
			switch (state) {
			case AT_START:
			case DELAYED:
				startDialog.cancel();
				//entry.setMissed();
				entry.setStatus(EMALogConstants.MISSED);
			case POST_DELAY:
				questionView.setVisibility(View.VISIBLE);
			case CONDUCTING:
				state = TIMED_OUT;
				handler.post(promptUser);
				handler.post(updateView);
				handler.postDelayed(timeoutInterview, END_TIMEOUT);
				break;
			case TIMED_OUT:
				if (currQuestion > 0)
					//entry.setAbandoned();
					entry.setStatus(EMALogConstants.ABANDONED);
			case DONE:
			default:
				// handle recording the log entry and destroying the activity
//				Intent result = new Intent();
//				String logEntry = entry.getLogEntry();
				Intent result = entry.getLogEntry();
				if (Log.DEBUG) Log.d("timeoutInterview", "retrieved entry data");
				//result.putExtra("RESULT", logEntry);
				result.putExtra("interviewType", interviewType);
				//setResult(RESULT_OK, result);
				if (labMode) {
					setResult(RESULT_OK, result);
				} else {
					if (schedulerService != null) {
						try {
							schedulerService.interviewCompleted(result);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				}
				if (wakelock.isHeld())
					wakelock.release();
				// final finish the interview activity
				finish();
				break;	
			}
		}
	};
   
		
	/* END RUNNABLES USED FOR INTERIEW TIMING AND CONTROL FLOW */
	
	/* BIND TO THE MONITOR SERVICE TO KEEP ME AWAKE */
	
	private void bindSchedulerService() {
		Intent intent = new Intent(getBaseContext(), InterviewScheduler.class);
		//intent.setClassName("edu.cmu.ices.stress.phone.monitor", "edu.cmu.ices.stress.phone.monitor.MonitorService");
		bindService(intent, schedulerConnection, 0);
		if (Log.DEBUG) Log.d("bindSchedulerService", "Bound to the scheduler service");
	}
	
	private void unbindSchedulerService() {
		if (schedulerService != null) {
			try {
				schedulerService.unregisterCallback(canceller);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		// unbind the scheduler service
		unbindService(schedulerConnection);
		if (Log.DEBUG) Log.d("unbindSchedulerService", "Unbound the scheduler service");
			
		
	}
	
	/* 
	 * Connection to the scheduler service 
	 */	
	private ServiceConnection schedulerConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName name, IBinder service) {
			schedulerService = ISchedulerService.Stub.asInterface(service);
			if (schedulerService != null) {
			try {
				schedulerService.registerCallback(canceller);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			if (Log.DEBUG) Log.d("AbstractInterview", "Connected to the scheduler service");
			} else {
				if (Log.DEBUG) Log.d("AbstractInterview", "Failed to connect to the scheduler service");
			}
			
		}

		public void onServiceDisconnected(ComponentName name) {
			
			schedulerService = null;
			if (Log.DEBUG) Log.d("AbstractInterview", "Disconnected from scheduler service");
		}
		
	};
	
	private IInterviewCancellationCallback.Stub canceller = new IInterviewCancellationCallback.Stub() {
		
		public void cancelInterview() throws RemoteException {
			
			// TODO integrate cancellation check in the activity destruction process
			interviewIsCancelled = true;
			if (wakelock.isHeld())
				wakelock.release();
			finish();
			
		}
	};
}
