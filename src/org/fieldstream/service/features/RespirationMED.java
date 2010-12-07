//Copyright (c) 2010, University of Memphis, Ohio State University
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
//    * Neither the names of the University of Memphis and Ohio State University nor the names of its 
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
package org.fieldstream.service.features;

//@author Andrew Raij
//@author Siddharth Shah

import org.fieldstream.service.sensors.api.AbstractFeature;

public class RespirationMED extends AbstractFeature {

	public RespirationMED(int ID, boolean checkUpdate, float thresholdInput) {
		super(ID, checkUpdate, thresholdInput);

	}

	@Override
	public double calculate(int[] buffer, long[] timestamps, int sensor) {
        float mx=mean(buffer);
		return std(buffer,mx);
		
	}

	//function to calculate the mean
	private float mean(int[] rout) {
		int store = 0;
		float mean1;
		for (int i = 0; i < rout.length; i++) {
			store += rout[i];
			
		}
		if (rout.length != 0) {
			mean1 = store / rout.length;
		} else {
			mean1 = 0;
		}
		
		return mean1;
	}

	//function to calculate standard deviation
	private float std(int[] rout, float meanRR) {
		float store1 = 0;
		for (int i = 0; i < rout.length; i++) {
			store1 += (rout[i] - meanRR) * (rout[i] - meanRR);
		}
		float std1 = (float) Math.sqrt(store1 / rout.length);
		return std1;
	}


	
	
}
