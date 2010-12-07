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

//@author Patrick Blitz
//@author Kurt Plarre
//@author Mahbub Rahman

package org.fieldstream.service.sensor.replay;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.fieldstream.R;
import org.fieldstream.service.InferrenceService;


public class TestSensorStorage {
	/**
	 * the GSR sensors id (internal, for addressing the correct raw file)
	 */
	public static final int GSR =  6;
	public static final int ECG =  2;
	public static final int RESP =  1;
	public static final int TEMP =  7;
	private static TestSensorStorage INSTANCE;
	private BufferedReader respirationReader;
	private BufferedReader gsrReader;
	private BufferedReader tempReader;
	private BufferedReader ecgReader;
	
	public TestSensorStorage() {
			respirationReader = new BufferedReader(new InputStreamReader(InferrenceService.INSTANCE.getResources().openRawResource(R.raw.sensor1)));
			ecgReader = new BufferedReader(new InputStreamReader(InferrenceService.INSTANCE.getResources().openRawResource(R.raw.sensor2)));
			tempReader = new BufferedReader(new InputStreamReader(InferrenceService.INSTANCE.getResources().openRawResource(R.raw.sensor7)));
			gsrReader = new BufferedReader(new InputStreamReader(InferrenceService.INSTANCE.getResources().openRawResource(R.raw.sensor6)));
			

			
	}

	public BufferedReader getReader(int sensorType) {
		switch (sensorType) {
		case GSR:
			return gsrReader;
		case TEMP:
			return tempReader;
		case RESP:
			return respirationReader;
		case ECG:
			return ecgReader;
		default:
			break;
		}
		
		return respirationReader;
	}
	
	public static TestSensorStorage getInstance() {
		if (INSTANCE==null)
			INSTANCE= new TestSensorStorage();
		return INSTANCE;
	}
	
	
}
