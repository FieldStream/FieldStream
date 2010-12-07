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

package org.fieldstream.service;
import org.fieldstream.service.IInferrenceServiceCallback;

// @author Patrick Blitz
// @author Andrew Raij


interface IInferrenceService {
	// writes statements to the GUI log
	void writeLabel(String label);
	void writeEMALog(in int triggerType, in String activeContexts, in int status, in long prompt, in long delayDuration, in String[] delayResponses, in long[] delayResponseTimes, in long start, in String[] responses, in long[] responseTimes);
	// retrieves the currently sensed labels from the specified model
	void logDeadPeriod(in long start, in long end);
	int getCurrentLabel(int model);
	int getActiveModels();
	void activateModel(int model);
	void deactivateModel(int model);
	void activateSensor(int sensor);
	void deactivateSensor(int sensor);
	void setFeatureComputation(boolean state);
	void subscribe(in IInferrenceServiceCallback listener);
	void unsubscribe(in IInferrenceServiceCallback listener);
    int getNumEMAsToday();
    long getLastEMATimestamp();
    double getTotalIncentivesEarned();
    void logResume(long timestamp);
}