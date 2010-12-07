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
// @author Andrew Raij


package org.fieldstream.gui.ema;

import java.io.File;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;

import org.fieldstream.Constants;
import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;

import android.database.Cursor;
import android.os.Environment;

public class EMABudgeter {

	private static String TAG = "EMABudgeter";
	
	public static int RESET_BUDGET_HOUR = 4;  // 4am
	
	long timeToReset;
		
	// overall budgeting
	int totalBudget;
	int remainingBudget;
	long minTimeBeforeNext;
	long lastTime;
	
	// per-item budgeting
	HashMap<String, Integer> totalItemBudget;
	HashMap<String, Integer> remainingItemBudget;

	DatabaseLogger dataLogger;
	
	EMABudgeter() {
		remainingItemBudget = new HashMap<String, Integer>();
		totalItemBudget = new HashMap<String, Integer>();
		totalBudget = 0;
		remainingBudget = 0;
		minTimeBeforeNext = 0;
		lastTime = 0;
		timeToReset = 0;
	}
	
	void setTotalBudget(int totalBudget) {
		Log.d(TAG, "Total EMA budget set to " + totalBudget);
		this.totalBudget = totalBudget;
		remainingBudget = totalBudget;
	}

	void setMinTimeBeforeNext(long minTimeBeforeNext) {
		Log.d(TAG, "At least " + minTimeBeforeNext + "ms before next EMA");
		this.minTimeBeforeNext = minTimeBeforeNext;
	}
	
	void addOrUpdateItem(String itemDesc, int budget) {
		Log.d(TAG, "Adding " + itemDesc + " with budget of " + budget);
		this.totalItemBudget.put(itemDesc, budget);
		this.remainingItemBudget.put(itemDesc, budget);
	}
	
	void removeItem(String itemDesc) {
		Log.d(TAG, "Removing " + itemDesc);

		remainingItemBudget.remove(itemDesc);
		totalItemBudget.remove(itemDesc);
	}
	
	void updateBudget(String itemDesc) {
		updateBudget(itemDesc, System.currentTimeMillis());
	}
	
	void updateBudget(String itemDesc, long timestamp) {
		if (timeToReset == 0 || timeToReset <= timestamp) {
			Log.d(TAG, "Resetting budgeter");
			reset();
		}
		
		remainingItemBudget.put(itemDesc, remainingItemBudget.get(itemDesc) - 1);
		remainingBudget--;
		lastTime = timestamp;
		
		Log.d(TAG, "Updating budget for " + itemDesc + " to " + remainingItemBudget.get(itemDesc));
		Log.d(TAG, "Updating total remaining budget to " + remainingBudget);
	}
	
	boolean checkBudget(String itemDesc) {
		long now = System.currentTimeMillis(); 
		if (timeToReset == 0 || timeToReset <= now) {
			Log.d(TAG, "Resetting budgeter");
			reset();
		}

		int itemBudget = 0;
		if (remainingItemBudget.containsKey(itemDesc)) {
			itemBudget = remainingItemBudget.get(itemDesc);
		}
		
		boolean pass = itemBudget > 0 && now > lastTime + minTimeBeforeNext && remainingBudget > 0;
		
		if (pass)
			Log.d(TAG, "budget check passed - can trigger " + itemDesc + " EMA");
		else 
			Log.d(TAG, "budget check failed - cannot trigger " + itemDesc + " EMA");
		
		return pass;
	}
	
	int getremainingItemBudget(String itemDesc){
		int itemBudget = 0;
		if(remainingItemBudget.containsKey(itemDesc))
			itemBudget = remainingItemBudget.get(itemDesc);
		
		return itemBudget;
	}
	
	void removeAllItems() {
		Log.d(TAG, "removing all items from budget");
		
		remainingItemBudget.clear();
		totalItemBudget.clear();
	}
	
	void resetBudgets() {
		Log.d(TAG, "reseting budgets to initial amounts");
		remainingItemBudget.clear();
		Set<String> keys = totalItemBudget.keySet();
		for (String key : keys) {
			remainingItemBudget.put(key, totalItemBudget.get(key));
		}
		
		remainingBudget = totalBudget;
	}
	
	void resetMinTimes() {
		Log.d(TAG, "reset time since last EMA");
		lastTime = 0;
	}
	
	void reset() {
		Log.d(TAG, "reset EMABudgeter");
		
		resetMinTimes();
		resetBudgets();
		
		// reset the budgets at 4am
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		int hour=0;
		do {
    		cal.roll(Calendar.HOUR_OF_DAY, true);
    		hour = cal.get(Calendar.HOUR_OF_DAY);
    		if (hour == 0) {
    			cal.roll(Calendar.DAY_OF_YEAR, true);
    		}    		
		} while (hour != EMABudgeter.RESET_BUDGET_HOUR);

		
		timeToReset = cal.getTimeInMillis();
				
		String s = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(cal.getTime());
		Log.d(TAG, "timeToReset = " + s);
	}
	
	public void loadChargesFromDB() {
		
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
	
	
}
