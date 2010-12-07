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
//@author Nathan Stohs

import java.util.Arrays;

import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensors.api.AbstractFeature;


public class HeartRate extends AbstractFeature {
	
	public HeartRate(int ID, boolean checkUpdate, float thresholdInput) {
		super(ID, checkUpdate, thresholdInput);
	}


	@Override
	public double calculate(int[] databuffer, long[] timestamps, int sensor) {
		return calcHR(databuffer);
	}
	
	public static double calcHR(int[] rrIntervals) {
		double meanRR;
		double medianRRout;
		double RRstd;
		final int numout = 6;
		double HR=0;
		meanRR = Mean.calcMean(rrIntervals, -1);	
		RRstd = std(rrIntervals, meanRR);
		if (RRstd > 4) {
			int[] RRout = new int[rrIntervals.length-numout];
			RRout = grubtest(rrIntervals,meanRR,RRstd);
			if (RRout.length!=0)
				medianRRout = RRout[(RRout.length) / 2];
			else 
				medianRRout=0;
			if (medianRRout!=0)
				HR = 3600 / meanRR;
			else
				HR=0;
		} else {
			int[] RRout = new int[rrIntervals.length];
			System.arraycopy(rrIntervals, 0, RRout, 0, rrIntervals.length);
			Arrays.sort(RRout);
			medianRRout = RRout[(RRout.length)/ 2];
			if (medianRRout!=0)
				HR = 3600 / medianRRout;
			else
				HR=0;			
		}	

		return HR;		
	}
	
	private static double std(int[] array, double meanRR) {
		double store1 = 0;
		for (int i = 0; i < array.length; i++) {
			store1 += (array[i] - meanRR) * (array[i] - meanRR);
		}
		double std1 = (float) Math.sqrt(store1 / array.length);
		return std1;
	}
	
	private static int[] grubtest(int[]rr, double meanRR, double Rstd)
   	{
		int numout = 6;
   		int[] grub= new int[rr.length];
   		int[] result = null;
   		for (int i=0;i<grub.length;i++)
   		{
   			grub[i]=(int) Math.abs(rr[i]-meanRR/Rstd);
   		}
   		for (int i=0;i<numout;i++)
   		{   
   			float maxvalue=grub[0];
   			int pos=0;
   			for(int j=0;j<grub.length;j++){  
   				if(grub[j] > maxvalue){  
   			        maxvalue = grub[j];
   			        pos=j;
   			    }  
   			}
   			result = new int[grub.length - 1];
   			System.arraycopy(grub, 0, result, 0, pos);
   			if (grub.length != pos) {
   			    System.arraycopy(grub, pos + 1, result, pos, grub.length - pos - 1);
   			}
   		}
   		return result;
   	}

	
	
}
