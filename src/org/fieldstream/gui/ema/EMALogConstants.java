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

// @author Mishfaq Ahmed
// @author Brian French
// @author Andrew Raij


package org.fieldstream.gui.ema;

import java.util.HashMap;

public class EMALogConstants {

	// ema intent keys
	public static final String PROMPT_TIME = "PROMPT_TIME";
	public static final String DELAY_TIME = "DELAY_TIME";
	public static final String START_TIME = "START_TIME";
	public static final String EMA_STATUS = "EMA_STATUS";
	public static final String RESPONSES = "RESPONSES";
	public static final String RESPONSE_TIMES = "RESPONSE_TIMES";
	public static final String DELAY_RESPONSE_TIMES = "DELAY RESPONSE TIMES";
	public static final String DELAY_RESPONSES = "DELAY RESPONSES";			
	
	// ema status constants
	public static final int COMPLETE = 0;
	public static final int ABANDONED = 1;
	public static final int MISSED = 2;
	
	public final static HashMap<Integer, String> emaStatusDescriptions = new HashMap<Integer, String>() {
		{
			put(COMPLETE, "Completed");
			put(ABANDONED, "Abandoned midway through interview");
			put(MISSED, "Ignored or not noticed by user");
		}
	};			
	
	
	// launch type constants
	public static final int TYPE_PERIODIC = 0;
	public static final int TYPE_CONTEXT_TIME = 1;
	public static final int TYPE_CONTEXT_CHANGE = 2;
	public static final int TYPE_INTERRUPTED_BY_PERIODIC = 3;
	public static final int TYPE_INTERRUPTED_BY_CONTEXT_TIME = 4;
	public static final int TYPE_INTERRUPTED_BY_CONTEXT_CHANGE = 5;
	public static final int TYPE_EOD = 6;
	
	public final static HashMap<Integer, String> emaTriggerDescriptions = new HashMap<Integer, String>() {
		{
			put(TYPE_PERIODIC, "Periodic");
			put(TYPE_CONTEXT_TIME, "Context time");
			put(TYPE_CONTEXT_CHANGE, "Context change");
			put(TYPE_INTERRUPTED_BY_PERIODIC, "Interrupted by periodic");
			put(TYPE_INTERRUPTED_BY_CONTEXT_TIME, "Interrupted by context time");
			put(TYPE_INTERRUPTED_BY_CONTEXT_CHANGE, "Interrupted by context change");
			put(TYPE_EOD, "EOD");
		}
	};	
}
