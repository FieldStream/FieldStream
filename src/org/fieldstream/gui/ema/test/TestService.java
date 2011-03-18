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

package org.fieldstream.gui.ema.test;

import org.fieldstream.service.IInferrenceService;
import org.fieldstream.service.IInferrenceServiceCallback;
import org.fieldstream.service.logger.Log;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;

public class TestService extends Service {

	IInferrenceServiceCallback callback;
	Handler handler;
	LocalActivityManager mgr;
	
	
	int event = 0;
	int[] contexts; // = {"L", "H","H","L","H","L","L","L","H", "L"};
	long[] times; // = {5000, 10000, 20000, 5000, 2000, 10000, 5000, 15000, 30000, 20000};
	
	/* ALL TESTS ASSUME 10 SEC MISSES, 10 SEC CONTEXT TIMERS, 30 SEC PERIOD, 5 SEC GRACE */
	// scheduled interrupted by context time - CHECK
	final int[] context1 = {1};
	final long[] time1 = {15000};
	
	// scheduled interrupted by context change - CHECK
	final int[] context2 = {1, 2};
	final long[] time2 = {2000, 18000 };
	
	// context change in the middle of a scheduled - CHECK
	final int[] context3 = {1,2};
	final long[] time3 = {15000, 18000};

	// context change in the middle of a context time - CHECK
	final int[] context4 = {1, 2};
	final long[] time4 = {2000, 22000};
	
	// context time interrupted by a scheduled - CHECK
	final int[] context5 = {1};
	final long[] time5 = {25000};
	
	// context change at same time as scheduled - CHECK
	final int[] context6 = {1, 2};
	final long[] time6 = {20000, 10000};
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate() {
    	super.onCreate();
        //responses.setTextFilterEnabled(true);
    	contexts = context1;
    	times = time1;
    	
    	handler = new Handler();
    	handler.postDelayed(contextEvent, 10000);
    	if (Log.DEBUG) Log.d("testService", "onCreate");
    	
       
    }
	
	private final IInferrenceService.Stub mBinder = new IInferrenceService.Stub() {
		
		public void writeLabel(String string) throws RemoteException {
			// TO DO Auto-generated method stub
			
		}
		
		public void unsubscribe(IInferrenceServiceCallback listener)
				throws RemoteException {
			// TO DO Auto-generated method stub
			
		}
		
		public void subscribe(IInferrenceServiceCallback listener)
				throws RemoteException {
			callback = listener;
			
		}
		
		public int getActiveModels() throws RemoteException {
			// TO DO Auto-generated method stub
			return 0;
		}
		
		public int getCurrentLabel(int model) throws RemoteException {
			// TO DO Auto-generated method stub
			return 0;
		}
		
		public void deactivateModel(int model) throws RemoteException {
			// TO DO Auto-generated method stub
			
		}
		
		public void activateModel(int model) throws RemoteException {
			// TO DO Auto-generated method stub
			
		}

		public void writeEMALog(int triggerType, String activeContexts, int status, long prompt,
				long delayDuration, String[] delayResponses, long[] delayResponseTimes, long start,
				String[] responses, long[] responseTimes) throws RemoteException {
			// TODO Auto-generated method stub
			if (Log.DEBUG) Log.d("TestService", "Received EMA log entry");
			System.out.println("" + triggerType + ", " + status + ", " + prompt + ", " + start + ", " + delayDuration);
			String s = "delayQuestions:";
			for (int i=0; i<delayResponses.length; i++) {
				s += delayResponses[i] + " @ " + delayResponseTimes[i] + ", ";	
			}
			System.out.println(s);
			
			s="questions:";
			for (int i=0; i<responses.length; i++) {
				s += responses[i] + " @ " + responseTimes[i] + ", ";	
			}
			System.out.println(s);
		}

		public void setFeatureComputation(boolean state) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public long getLastEMATimestamp() throws RemoteException {
			// TODO Auto-generated method stub
			return System.currentTimeMillis() - (15L * 60 * 1000);
		}

		public int getNumEMAsToday() throws RemoteException {
			// TODO Auto-generated method stub
			return 20;
		}

		public void logDeadPeriod(long start, long end) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public double getTotalIncentivesEarned() throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}

		public void logResume(long timestamp) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void activateSensor(int sensor) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void deactivateSensor(int sensor) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		
		public void activateMote(int mote) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		
		public void deactivateMote(int mote) throws RemoteException {
			// TODO Auto-generated method stub
			
		}
	};		
	@Override
	public IBinder onBind(Intent intent) {
		// TO DO Auto-generated method stub
		return mBinder;
	}
	
	private Runnable contextEvent = new Runnable() {
		public void run() {
			//if (Log.DEBUG) Log.d("currentActivity", mgr.getCurrentId());
//			try {
//				if (callback != null) 
//					
//					callback.receiveCallback(contexts[event]);
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			}
//			event ++;
//			
//			if (event < times.length) {
//				handler.postDelayed(this, times[event]);
//			} else {
//				stopSelf();
//			}
		}
	};
	
}
