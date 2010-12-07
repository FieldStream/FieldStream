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


//@author Andrew Raij

package org.fieldstream.incentives;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;

import org.fieldstream.Constants;
import org.fieldstream.service.InferrenceService;
import org.fieldstream.service.logger.AbstractLogger;
import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.logger.TextFileLogger;

import android.os.Environment;


/**
 *  Abstract class which keeps track of $ incentives earned and the reasons incentives earned.  
 *  Subclasses should do the dirty work of deciding when an incentive is earned, and then use newIncentiveEarned()
 *  to report the incentive.
 *  
 */				


public abstract class AbstractIncentivesManager {
	// for creating incentive IDs
	public static final int INCENTIVE_NONE = 0;
	public static final int INCENTIVE_TIME_WEARING_SENSORS = 1;
	public static final int INCENTIVE_EMA_PER_QUESTION = 2;
	public static final int INCENTIVE_EMA_VARIABLE = 4;
	public static final int INCENTIVE_EMA_VISIBLE = 8;
	public static final int INCENTIVE_EMA_TIME_BONUS = 16;
	


	
	
	static public String getIncentiveDesc(int id) {
		String summary;
		if ((INCENTIVE_TIME_WEARING_SENSORS & id) > 0) {
			summary = "Incentives earned for each minute wearing sensors";
		}
		else {
			summary = EMAIncentiveManager.getIncentiveDesc(id);
		}
				
		return summary;

	}
	
	
	
	private HashMap<String, BigDecimal> incentiveMap = new HashMap<String, BigDecimal>();
	private HashMap<String, Boolean> incentiveEarnedMap = new HashMap<String, Boolean>();
	
	private BigDecimal totalEarned;	
	
	protected AbstractIncentivesManager() {				
		totalEarned = new BigDecimal("0.0").setScale(2, BigDecimal.ROUND_HALF_EVEN);
	}
	
	public abstract int getID();
	
	public BigDecimal getTotalEarned() {
		return totalEarned;
	}
		
	public Boolean incentiveEarned(String id) {
		 
		if (incentiveEarnedMap.containsKey(id)) {
			return incentiveEarnedMap.get(id);
		}
		
		return false;
	}
		
	public abstract String status();

	void setIncentive(String id, BigDecimal amount) {
		incentiveMap.put(id, amount.setScale(2, BigDecimal.ROUND_HALF_EVEN));
		incentiveEarnedMap.put(id, false);
	}

	BigDecimal getIncentive(String id) {
		BigDecimal f = incentiveMap.get(id);
		if (f == null)
			return new BigDecimal("0.0"); 

		return f;
	}

	boolean hasIncentive(String id) {
		return incentiveMap.containsKey(id);
	}
	
	abstract public BigDecimal requestIncentive(String id);

	
	BigDecimal markIncentive(String id, boolean earned) {
		BigDecimal amount = new BigDecimal("0.0");
		if (!incentiveMap.containsKey(id) || !incentiveEarnedMap.containsKey(id))
			return amount;

		Boolean alreadyEarned = incentiveEarned(id);
				
		if (earned) {
			if (!alreadyEarned) {
				incentiveEarnedMap.put(id, true);
				amount = incentiveMap.get(id);
				totalEarned = totalEarned.add(amount);			
				Log.d("AbstractIncentive", "Earned $" + amount + " for " + id + " - num keys = " + incentiveEarnedMap.keySet().size());
			}
			else {
				Log.d("AbstractIncentive", "Already earned incentive for " + id + " - num keys = " + incentiveEarnedMap.keySet().size());
			}
		}
		else {
			if (alreadyEarned) {
				incentiveEarnedMap.put(id, false);
				amount = incentiveMap.get(id).negate();
				totalEarned = totalEarned.add(amount);
				
				Log.d("AbstractIncentive", "Lost $" + amount + " for " + id + " - num keys = " + incentiveEarnedMap.keySet().size());
			}			
			else {
				Log.d("AbstractIncentive", "Already lost incentive for " + id + "(or it was never added)" + " - num keys = " + incentiveEarnedMap.keySet().size());
			}
		}

//		String str = "";
//		for (String s : incentiveEarnedMap.keySet()) {
//			str += s + " ";
//		}
//		Log.d("AbstractIncentive", str);
		
		return amount;
	}
	
	// only works for a sqlite logger
	protected abstract void loadIncentivesEarned();
	
	protected void reset(boolean resetTotal) {
		if (resetTotal)
			totalEarned = new BigDecimal("0.0").setScale(2, BigDecimal.ROUND_HALF_EVEN);
		
		incentiveMap.clear();
		incentiveEarnedMap.clear();
	}
}
