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

package org.fieldstream.service.sensor.virtual;

//@author Kurt Plarre

import java.util.Arrays;

import org.fieldstream.service.logger.Log;


public class TempQualityCalculation {
	// ===========================================================
	private static final String TAG="TempQualityCalculation";
	
	private static final int BUFFER_LENGTH=6;
	private static double levBuff[];
	private static int levHead;
	private static int dirBuff[];
	private static int dirHead;
	
	public final static int DATA_QUALITY_GOOD = 0;
	public final static int DATA_QUALITY_NOISE = 1;    
	public final static int DATA_QUALITY_BAND_LOOSE = 2;
	public final static int DATA_QUALITY_BAND_OFF = 3;
	
	private static final int LOW_LEVEL=1200;
	private static final int ACCEPTABLE_BAD_SEGMENTS=2;
	private static final int ACCEPTABLE_LOW_SEGMENTS=4;
	// ===========================================================
	
	public TempQualityCalculation(){
		levBuff=new double[BUFFER_LENGTH];
		dirBuff=new int[BUFFER_LENGTH];
		for(int i=0;i<BUFFER_LENGTH;i++){
			levBuff[i]=LOW_LEVEL+200;
			dirBuff[i]=1;
		}
		levHead=0;
		dirHead=0;
	}

	private double computeLevel(double[] x){
		if (Log.DEBUG) Log.d("TempQualityCalculation","computeLevel");			
		int s=0;
		for(int i=0;i<x.length;i++) s+=x[i];
		return s/x.length;
	}
	
	private int computeDir(double[] x){
		if (Log.DEBUG) Log.d("TempQualityCalculation","computeDir");			
		double[] s=new double[x.length];
		double da=0;
		double dd=0;
		for(int i=0;i<x.length;i++) s[i]=x[i];
		Arrays.sort(s);
		for(int i=0;i<x.length;i++){
			da=da+Math.abs(x[i]-s[i]);
			dd=dd+Math.abs(x[i]-s[x.length-i-1]);
		}
		if(dd>da) return 1;else return -1;
	}
	
	// 1 second averages
	private double[] summarize(int[] data){
		if (Log.DEBUG) Log.d("TempQualityCalculation","summarize");			
		int n=(int) Math.floor(data.length/10);
		double[] x=new double[n];
		for(int i=0;i<n;i++){
			int s=0;
			for(int j=10*i;j<10*i+10;j++) s+=data[j];
			x[i]=s/10;
		}
		return x;
	}
	
	public int currentQuality(int[] data){
		if (Log.DEBUG) Log.d("TempQualityCalculation","currentQuality-in");			
		double[] x=summarize(data);
		double level=computeLevel(x);
		levBuff[(levHead++)%levBuff.length]=level;
		int dir=computeDir(x);
		dirBuff[(dirHead++)%dirBuff.length]=dir;
		int count_lev=0;
		int count_bad=0;
		if (Log.DEBUG) Log.d("TempQualityCalculation","count bad segments");			
		for(int i=0;i<BUFFER_LENGTH;i++){
			if(levBuff[i]<LOW_LEVEL){
				count_lev++;
				if(dirBuff[i]==-1) count_bad++;
			}
		}

		//if (Log.DEBUG) Log.d("TempQualityCalculation","levBuff "+levBuff[0]+" "+levBuff[1]+" "+levBuff[2]+" "+levBuff[3]+" "+levBuff[4]+" "+levBuff[5]+" "+levBuff[6]+" "+levBuff[7]+" "+levBuff[8]+" "+levBuff[9]+" ");
		//if (Log.DEBUG) Log.d("TempQualityCalculation","dirBuff "+dirBuff[0]+" "+dirBuff[1]+" "+dirBuff[2]+" "+dirBuff[3]+" "+dirBuff[4]+" "+dirBuff[5]+" "+dirBuff[6]+" "+dirBuff[7]+" "+dirBuff[8]+" "+dirBuff[9]+" ");
		//if (Log.DEBUG) Log.d("TempQualityCalculation","count_lev "+count_lev+" count_bad "+count_bad);

		if(count_lev<=ACCEPTABLE_LOW_SEGMENTS & count_bad<=ACCEPTABLE_BAD_SEGMENTS){
			return DATA_QUALITY_GOOD;
		}else{
			return DATA_QUALITY_BAND_OFF;			
		}
	}
	
}

