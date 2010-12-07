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

/**
 * 
 */

//@author Andrew Raij

package org.fieldstream.incentives;

import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;

import org.fieldstream.Constants;
import org.fieldstream.service.InferrenceService;
import org.fieldstream.service.logger.AbstractLogger;
import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.logger.TextFileLogger;

import edu.cmu.ices.stress.phone.service.IInferrenceService;

import android.database.Cursor;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;

/**
 * @author Andrew Raij
 * TODO add case where award is recomputed randomly for each question
 */
public class EMAIncentiveManager extends AbstractIncentivesManager {
	boolean loggingEnabled = true;
	boolean perQuestion = false;
	int numQuestionsAnswered = 0;
	
	boolean variable = false;
	private float[] variableDistribution;
	private BigDecimal[] variableAmounts;
	
	BigDecimal uniformAmount = new BigDecimal(1);
	BigDecimal currentIncentive = new BigDecimal(0);
	BigDecimal bonusAmount = new BigDecimal(0);
	long bonusTime = 0;
	
	boolean incentiveVisible = true;
	boolean loaded = false;
	
	int numQuestions = 1;

	
	BigDecimal currentInterviewTotal = new BigDecimal(0); 
	
	DatabaseLogger dataLogger;

	
	
	
	public EMAIncentiveManager() {
		dataLogger = null;
		
  		if (Constants.LOGTODB) {
  			dataLogger = DatabaseLogger.getInstance(this); 
  		}
	}
	
	public void setBonusTime(long time) {
		bonusTime = time;
	}
	
	public void setBonusAmount(BigDecimal amount) {
		bonusAmount = amount;
	}
			
	public void setVariable(float[] distribution, BigDecimal[] amounts) {
		this.variable = true;
		variableDistribution = distribution;
		variableAmounts = amounts;
		
		recomputeIncentives();
	}
		
	public void setUniform(BigDecimal amount) {
		this.variable = false;
		uniformAmount = amount;
		
		recomputeIncentives();
	}
	
	public void setPerQuestion(boolean perQuestion) {
		this.perQuestion = perQuestion;
		
		recomputeIncentives();
	}
	
	public void setNumQuestions(int numQuestions) {
		this.numQuestions = numQuestions;
		
		recomputeIncentives();
	}
	
	public BigDecimal requestIncentive(String id, long timeToFinish) {
		if (!hasIncentive(id)) {
			if (timeToFinish < bonusTime) {
				setIncentive(id, currentIncentive.add(bonusAmount));	
				Log.d("EMAIncentiveManager", "requestIncentive: $1.25");
			}
			else {
				setIncentive(id, currentIncentive);
				Log.d("EMAIncentiveManager", "requestIncentive: $1.00");				
			}
		}
		
		return getIncentive(id);
	}
	
	public BigDecimal markIncentive(String id, boolean earned) {
		Boolean alreadyEarned = this.incentiveEarned(id);

		BigDecimal amount = super.markIncentive(id, earned);
		currentInterviewTotal = currentInterviewTotal.add(amount);
		
		Log.d("EMAIncentiveManager", "markIncentive: " + NumberFormat.getCurrencyInstance().format(currentInterviewTotal));
		
		if (alreadyEarned == null)
			return amount;
		
		if (earned) {
			if (!alreadyEarned) {

				if (perQuestion)
					numQuestionsAnswered++;
				else 
					numQuestionsAnswered+=numQuestions;
				
				if (loggingEnabled) {
					long timestamp = System.currentTimeMillis();
					dataLogger.logIncentiveEarned(getID(), id, timestamp, amount.floatValue(), getTotalEarned().floatValue());
				}
			}
		}
		else {
			if (alreadyEarned) {

				if (perQuestion)
					numQuestionsAnswered--;
				else 
					numQuestionsAnswered-=numQuestions;
				
				if (loggingEnabled) {
					long timestamp = System.currentTimeMillis();
					dataLogger.logIncentiveEarned(getID(), id, timestamp, amount.floatValue(), getTotalEarned().floatValue());
				}
			}			
		}
		
		return amount;
	}

	public int getID() {
		int id = AbstractIncentivesManager.INCENTIVE_NONE;
		
		id |= perQuestion ? AbstractIncentivesManager.INCENTIVE_EMA_PER_QUESTION : AbstractIncentivesManager.INCENTIVE_NONE;
		id |= variable ? AbstractIncentivesManager.INCENTIVE_EMA_VARIABLE : AbstractIncentivesManager.INCENTIVE_NONE;
		id |= incentiveVisible ? AbstractIncentivesManager.INCENTIVE_EMA_VISIBLE : AbstractIncentivesManager.INCENTIVE_NONE;
		id |= bonusTime > 0 ? AbstractIncentivesManager.INCENTIVE_EMA_TIME_BONUS : AbstractIncentivesManager.INCENTIVE_NONE;		
		
		return id;
	}
	
	public void reset(boolean resetTotal) {
		super.reset(resetTotal);
		if (resetTotal)
			loaded = false;		
		
		currentInterviewTotal = new BigDecimal(0);
		
		if (variable) {
			recomputeIncentives();
		}
	}
		
	private void recomputeIncentives() {
		currentInterviewTotal = new BigDecimal(0);
		
		BigDecimal currentInterviewAmount = new BigDecimal(0);
		
		if (variable) {
			float value = (float)Math.random();
			float total = 0;
			for (int i=0; i < variableDistribution.length; i++) {
				if (value >= total && value < total + variableDistribution[i] ) {
					currentInterviewAmount = variableAmounts[i];
					break;
				}
				
				total += variableDistribution[i];
			}
		}
		else {
			currentInterviewAmount = uniformAmount;
		}
		
		if (perQuestion) {
			currentIncentive = currentInterviewAmount.divide(new BigDecimal(numQuestions), 2, BigDecimal.ROUND_HALF_EVEN);
		}
		else {
			currentIncentive = currentInterviewAmount;
		}
	}
	
	public BigDecimal getCurrentIncentive() {		
		return currentIncentive;
	}

	public BigDecimal getCurrentInterviewTotal() {
		return currentInterviewTotal;
	}
	
	public int getNumQuestionsAnswered() {
		return numQuestionsAnswered;
	}
	
	public void setIncentiveVisible(boolean visible) {
		this.incentiveVisible = visible;
	}
	
	public boolean isIncentiveVisible() {
		return incentiveVisible;
	}

	public boolean isPerQuestion() {
		return perQuestion;
	}
	
	public String getIncentiveDesc() {
		return EMAIncentiveManager.getIncentiveDesc(getID());
	}
	
	public static String getIncentiveDesc(int id) {
		String summary = "Incentives earned for completing EMAs.  Incentives ";
		
		summary += (INCENTIVE_EMA_PER_QUESTION & id) > 0 ? "are earned per question, " : "are earned per questionnaire, ";
		summary += (INCENTIVE_EMA_VARIABLE & id) > 0 ? "are of variable amounts, and" : "are of uniform amounts, and ";
		summary += (INCENTIVE_EMA_VISIBLE & id) > 0 ? "are shown to the user before the incentive is awarded." : "are hidden from the user until the incentive is awarded.";
		summary += (INCENTIVE_EMA_TIME_BONUS & id) > 0 ? "include a time bonus." : "";		
		
		return summary;
	}

	@Override
	public void loadIncentivesEarned() {
		if (loaded)
			return;
		
		if (dataLogger == null)
			return;

		loggingEnabled = false;
		
		if (Log.DEBUG) Log.d("incentive", "total=" + this.getTotalEarned());
		
		try {
			
			Cursor c = dataLogger.readIncentivesData(getID());
			Log.d("EMAIncentive", "got the cursor");

			if (c!=null) {
				Log.d("EMAIncentive", "cursor is not null");
				if (c.moveToFirst()) {
					do {
						Log.d("EMAIncentive", "cursor pos = " + c.getPosition());

						BigDecimal amount = new BigDecimal(c.getFloat(c.getColumnIndex("amount"))).setScale(2, BigDecimal.ROUND_HALF_EVEN);
						String comment = c.getString(c.getColumnIndex("comment"));
						setIncentive(comment, amount);
						this.markIncentive(comment, true);
						Log.d("EMAIncentive", "Loaded " + comment + " -- $" + amount);
					} while (c.moveToNext());
				}
				c.close();
			}		
		} catch (Exception e) {
			if (Log.DEBUG) Log.d("StressInferenceIncentiveManager", e.getLocalizedMessage());
		}
		
		loaded = true;
		loggingEnabled = true;
		

	}

	@Override
	public String status() {
		return "";
	}
	
	public void cleanup() {
		if (dataLogger != null) {
			dataLogger = null;
			DatabaseLogger.releaseInstance(this);
		}
	}
	
	protected void finalize() {
		cleanup();
	}

	@Override
	public BigDecimal requestIncentive(String id) {
		return requestIncentive(id, 0);
	}
	
}
