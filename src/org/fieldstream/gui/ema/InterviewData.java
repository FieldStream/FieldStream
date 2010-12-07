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

//@author Brian French
//@author Andrew Raij

package org.fieldstream.gui.ema;

import java.io.Serializable;

import org.fieldstream.service.logger.Log;


import android.content.Intent;
import android.os.Bundle;
import android.text.format.Time;


public class InterviewData {

	// interview entry state
	Serializable[] selectedResponse;
	Serializable[] selectedDelayResponse;
	long[] responseTime;
	long[] delayResponseTime;
	long promptTime;
	long startTime;
	long delayStart;
	long delayStop;
	int status;
//	boolean missed;
//	boolean abandoned;
//	boolean completed;
	
	final String dateFormat = "%Y/%m/%d,%I:%M:%S,%p";
	
	public InterviewData(int responseLength, int delayResponseLength) {
		selectedResponse = new Serializable[responseLength];
		responseTime = new long[responseLength];
		for (int i=0; i<responseLength; i++) {
			selectedResponse[i] = null;
			responseTime[i] = -1;
		}

		selectedDelayResponse = new Serializable[delayResponseLength];
		delayResponseTime = new long[delayResponseLength];
		for (int i=0; i<delayResponseLength; i++) {
			selectedDelayResponse[i] = null;
			delayResponseTime[i] = -1;
		}
		
		promptTime = 0;
		startTime = -1;
		delayStart = -1;
		delayStop = -1;
		status = -1;
//		missed = false;
//		abandoned = false;
//		completed = false;
	}
	
	public Bundle onSaveInstanceState(Bundle state) {
		int length = selectedResponse.length;
		state.putInt("selectedResponseLength", length);
		for(int i=0; i<length; i++) {
			if (selectedResponse[i] != null) {
				state.putSerializable("selectedResponse"+i, selectedResponse[i]);
			}
		}

		length = selectedDelayResponse.length;
		state.putInt("selectedDelayResponseLength", length);
		for(int i=0; i<length; i++) {
			if (selectedDelayResponse[i] != null) {
				state.putSerializable("selectedDelayResponse"+i, selectedDelayResponse[i]);
			}
		}
				
		state.putLongArray("responseTime", responseTime);
		state.putLongArray("delayResponseTime", delayResponseTime);
		state.putLong("promptTime", promptTime);
		state.putLong("startTime", startTime);
		state.putLong("delayStart", delayStart);
		state.putLong("delayStop", delayStop);
		state.putInt("status", status);
//		state.putBoolean("missed", missed);
//		state.putBoolean("abandoned", abandoned);
//		state.putBoolean("completed", completed);
		return state;
	}
	
	public void onRestoreInstanceState(Bundle state) {
		int length = state.getInt("selectedResponseLength");
		selectedResponse = new Serializable[length];
		for(int i=0; i<length; i++) {
			if (state.containsKey("selectedResponse"+i)) {
				selectedResponse[i] = state.getSerializable("selectedResponse"+i);
			}
		}
		
		length = state.getInt("selectedDelayResponseLength");
		selectedDelayResponse = new Serializable[length];
		for(int i=0; i<length; i++) {
			if (state.containsKey("selectedDelayResponse"+i)) {
				selectedDelayResponse[i] = state.getSerializable("selectedDelayResponse"+i);
			}
		}
		
		delayResponseTime = state.getLongArray("delayResponseTime");
		responseTime = state.getLongArray("responseTime");
		promptTime = state.getLong("promptTime");
		startTime = state.getLong("startTime");
		delayStart = state.getLong("delayStart");
		delayStop = state.getLong("delayStop");
		status = state.getInt("status");
//		missed = state.getBoolean("missed");
//		abandoned = state.getBoolean("abandoned");
//		completed = state.getBoolean("completed");
	}
	
	public void setPromptTime(long time) {
		if (promptTime == 0) {
			promptTime = time;
		}
	}
	public long getPromptTime() {
		return promptTime;
	}
	
	public void setStartTime(long time) {
		startTime = time;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public void setDelayStart(long time) {
		delayStart = time;
	}
	
	public long getDelayStart() {
		return delayStart;
	}
	
	public void setDelayStop(long time) {
		if (delayStop == -1)
			delayStop = time;
	}
	
	public long getDelayStop() {
		return delayStop;
	}
	
	public void setDelayResponse(int index, Serializable response, long timestamp) {
		selectedDelayResponse[index] = response;
		delayResponseTime[index] = timestamp;
	}
	
	public Serializable getDelayResponse(int index) {
		return selectedDelayResponse[index];
	}
	
	public void setStatus(int s) {
		status = s;
	}
//	public void setMissed() {
//		missed = true;
//		abandoned = false;
//		completed = false;
//	}
//	
//	public void setAbandoned() {
//		missed = false;
//		abandoned = true;
//		completed = false;
//	}
//	
//	public void setCompleted() {
//		missed = false;
//		abandoned = false;
//		completed = true;
//	}
	
	public void setResponse(int index, Serializable value, long time) {
		Log.d("InterviewData", "index = " + index);
//		if (index < selectedResponse.length) {
			selectedResponse[index] = value;
			responseTime[index] = time;
//		}
	}
	
	public Serializable getResponse(int index) {
		
		return selectedResponse[index];
	}

	public Intent getLogEntry() {
		
		Intent result = new Intent();
		result.putExtra(EMALogConstants.PROMPT_TIME, promptTime);
		result.putExtra(EMALogConstants.DELAY_TIME, delayStop - delayStart);
		result.putExtra(EMALogConstants.START_TIME, startTime);
		result.putExtra(EMALogConstants.EMA_STATUS, status);
		//result.putExtra(EMALogConstants.RESPONSES, selectedResponse);
		result.putExtra(EMALogConstants.RESPONSE_TIMES, responseTime);
		result.putExtra(EMALogConstants.DELAY_RESPONSE_TIMES, delayResponseTime);
		//state.putInt("selectedResponseLength", length);
		for(int i=0; i<responseTime.length; i++) {
			if (selectedResponse[i] != null) {
				result.putExtra(EMALogConstants.RESPONSES+i, selectedResponse[i]);
				//state.putSerializable("selectedResponse"+i, selectedResponse[i]);
			}
		}
		for(int i=0; i<delayResponseTime.length; i++) {
			if (selectedDelayResponse[i] != null) {
				result.putExtra(EMALogConstants.DELAY_RESPONSES+i, selectedDelayResponse[i]);
				//state.putSerializable("selectedResponse"+i, selectedResponse[i]);
			}
		}

		
		return result;
//		StringBuilder sb = new StringBuilder();
//		Time prompt = new Time();
//		prompt.set(promptTime);
//		sb.append(prompt.format(dateFormat));
//		sb.append(","+promptTime);
//		long delay = delayStop - delayStart;
//		sb.append(","+delay);
//		sb.append(","+delayReason);
//		long start;
//		if (startTime == -1) {
//			start = startTime;
//		} else {
//			start = startTime - promptTime;
//		}
//		sb.append(","+start);
//		if (missed)
//			sb.append(",missed");
//		if (abandoned)
//			sb.append(",abandoned");
//		if (completed)
//			sb.append(",completed");
//		int length = selectedResponse.length;
//		for (int i=0; i < length; i++) {
//			long offset;
//			if (i == 0) {
//				offset = responseTime[i] - startTime;
//				
//			} else {
//				offset = responseTime[i] - responseTime[i-1];
//				if (offset < 0) offset = 0;
//			}
//			sb.append(","+selectedResponse[i]+","+offset);
//		}
//		return sb.toString();
	}

}

