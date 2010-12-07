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

// @author Mishfaq Ahmed


package org.fieldstream.gui.ema;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;

import org.fieldstream.Constants;
import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;

import android.database.Cursor;
import android.os.Environment;


public class EMATriggerer {

	EMABudgeter budgeter=null;
	HashMap<String, Integer> estimates=null;
	HashMap<String, Integer> numberobservedtoday = null;
	DatabaseLogger dataLogger;
	
	EMATriggerer(EMABudgeter budgeter){
		this.budgeter = budgeter;
		
		///////////// pop from database
		estimates = new HashMap<String, Integer>();
		numberobservedtoday = new HashMap<String, Integer>();
		estimates.put(""+Constants.MODEL_SELF_DRINKING, 1);
		estimates.put(""+Constants.MODEL_SELF_SMOKING, 8);
		//estimates.put(""+Constants.MODEL_STRESS, 4);
		estimates.put(""+Constants.MODEL_ACCUMULATION, 4);
		//	estimates.put(Constants.MODEL_CONVERSATION, 10);
		//estimates.put(Constants., value);
		//this.estimates
		
		// initialize todays observation for all types of event to zero
		numberobservedtoday.put(""+Constants.MODEL_SELF_DRINKING,0);
		numberobservedtoday.put(""+Constants.MODEL_SELF_SMOKING, 0);
		numberobservedtoday.put(""+Constants.MODEL_STRESS,0);
	}
	
	public boolean trigger(int modelID, long currentTimeMillis) {
		Log.d("Triggering","Entering the EMATrigger::trigger for model"+modelID);
		
		numberobservedtoday.put(""+modelID, numberobservedtoday.get(""+modelID)+1);
		int estimation = estimates.get(""+modelID);
		
		String itemDesc = ""+modelID;
		
		int budgetleft = budgeter.getremainingItemBudget(itemDesc);
		float probability = (float)budgetleft/(float)estimation;
		if(probability > 1)
			probability = 1;
		
		if((float)Math.random()<probability){
		Log.d("Triggering with probability",""+probability);
		return true;
		}
//		
		
		
		// check what is the estimated number of events for this type in the current day.
		// check what is the remaining budget for this model.
		// check remaining time of the day.
		// check what is the budget of current day.
		// based on this values calculate the probabilities of returning true.
		
		else
		{
			Log.d("not triggered although the probability was",""+probability);
			return false;
		}
	}
	
	
/*	public void loadEstimatesFromDB() {
		
		dataLogger = null;
		
		// this is for loading and saving to db
		// only works with the db for now
  		File root = Environment.getExternalStorageDirectory();
  		if (Constants.LOGTODB) {
  			dataLogger = DatabaseLogger.getInstance(this); 
  		}
  		else {
  			return;
  		}

		try {
			
			Cursor c = dataLogger.readEMA(0, System.currentTimeMillis());

			if (c!=null) {
				if (c.moveToFirst()) {
					do {
						Calendar cal = Calendar.getInstance();
						cal.set(Calendar.MINUTE, 0);
						cal.set(Calendar.SECOND, 0);
						cal.set(Calendar.MILLISECOND, 0);
						
						int hour=0;
						do {
				    		cal.roll(Calendar.HOUR_OF_DAY, false);
				    		hour = cal.get(Calendar.HOUR_OF_DAY);
				    		if (hour == 23) {
				    			cal.roll(Calendar.DAY_OF_YEAR, false);
				    		}    		
						} while (hour != EMABudgeter.RESET_BUDGET_HOUR);

				    	
						long lastResetTime = cal.getTimeInMillis();
						
//						int trigger = c.getInt(c.getColumnIndex("trigger_type"));
//						if (trigger == EMALogConstants.TYPE_CONTEXT_CHANGE || trigger == EMALogConstants.TYPE_CONTEXT_TIME || trigger == EMALogConstants.TYPE_INTERRUPTED_BY_CONTEXT_CHANGE || trigger == EMALogConstants.TYPE_INTERRUPTED_BY_CONTEXT_TIME) {
							long timestamp = c.getLong(c.getColumnIndex("prompt_timestamp"));
							if (timestamp > lastResetTime) {
								String models = c.getString(c.getColumnIndex("context"));
								
								long now = System.currentTimeMillis();
								String[] modelsArray = models.split(" ");
								if (modelsArray.length == 1) {
									updateBudget("other", now);
								}
								else {
									updateBudget(modelsArray[0], now);
								}
							}	
					} while (c.moveToNext());
				}
				c.close();
			}		
		} catch (Exception e) {
			if (Log.DEBUG) Log.d("StressInferenceIncentiveManager", e.getLocalizedMessage());
		}

		Log.d(TAG, "Loaded budgets from DB");
		
		dataLogger = null;
		DatabaseLogger.releaseInstance(this);
	}
*/
}
