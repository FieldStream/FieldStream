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

//@author Patrick Blitz
//@author Andrew Raij
//@author Siddharth Shah
//@author Nathan Stohs

import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensors.api.AbstractFeature;

public class RespirationRate extends AbstractFeature {

	float resprate;
	float respamplitude;
	float stdresp;
	
	public RespirationRate(int ID, boolean checkUpdate, float thresholdInput) {
		super(ID, checkUpdate, thresholdInput);

	}

	@Override  // TODO  VERY SLOW - REIMPLEMENT
	public double calculate(int[] buffer, long[] timestamps, int sensor) {
//		resprate=lomb(buffer);
//		resprate=nativelomb(buffer, buffer.length);

		// assuming it receives an array of inhalation durations, then resp rate is
		resprate = 0;
		String tstamp="";
		for (int i=0; i < timestamps.length; i++) {
			tstamp += timestamps[i] + " ";
		}
		
		Log.d("RespRate", tstamp);
		
		if (timestamps.length > 1) {
			float duration = (float)(timestamps[timestamps.length-1] - timestamps[0]);
			duration = ((duration / 1000) / 60);
			Log.d("RespRate", "Number of breaths in the Window = " + buffer.length);
			Log.d("RespRate", "Duration of Window (minutes) = " + duration);
			if (duration != 0) {
				resprate = buffer.length / duration;			
			}
			Log.d("RespRate", "resp rate (breaths/min) = " + resprate);
		}
		
		return resprate;
	}
	private float lomb(int[] Rout) {
		float[] t = new float[Rout.length];
		float A,B,C,D;
		float Ss2wt;
		float Sc2wt;
		float mx,vx;
		int nt;
		int T;
		int nf;
		
		float valuef=0;
		
//		valuef=nativelomb(Rout, Rout.length);
		
//		return valuef;
		
	   for (int i=0;i<Rout.length;i++)		
	   {
		   t[i] = (float) (i+0.0001);
	   }
		nt = t.length;
		T = nt-1;
		nf = (int) Math.round(0.5*4*1*nt);
		float[] f = new float[nf];
		for (int i=0;i<nf;i++)	 
		{
			f[i] = ((float)(i+ 0.0001))/(T);
			
		}
	   	nf = f.length;
		mx = mean(Rout);
		float[] wt=new float[t.length];
		float[] swt=new float[t.length];
		float[] cwt=new float[t.length];
		float[] cwt2=new float[t.length];
		float[] diff=new float[t.length];
		float[] sum=new float[t.length];
		float[] cwttau2=new float[t.length];
		float[] swttau2=new float[t.length];
		float[] swttau=new float[t.length];
		float[] cwttau = new float[t.length];
		float[] wtau= new float[f.length];
		float[] swtau= new float[f.length];
		float[] cwtau= new float[f.length];
		float[] P= new float[f.length];
		vx = std(Rout,mx); 
		
		vx=(float) Math.pow(vx,2);
		for (int i=0;i<Rout.length;i++)	
		{
		Rout[i]  = (int) (Rout[i]-mx);
		}
		
		
	for (int i=0;i<50;i++)  
	{
		for (int j=0;j<t.length;j++)
		{
		    wt[j]  = (float) (2*3.142*f[i]*t[j]);
		    swt[j] = (float) Math.sin(wt[j]);
		    cwt[j] = (float) Math.cos(wt[j]);
		    cwt2[j]= 2*cwt[j];
		}
		    Ss2wt = MatrixMT(cwt2,swt);// Row by column to be done     
		    for (int j=0;j<t.length;j++)
		    {
		       diff[j]=cwt[j]-swt[j];
		       sum[j]=cwt[j]+swt[j];
		    }
		    Sc2wt = MatrixMT(diff,sum); 
		    wtau[i]  = (float) (0.5*Math.atan2(Ss2wt,Sc2wt));   
		    swtau[i] = (float) Math.sin(wtau[i]); 
		    cwtau[i] = (float) Math.cos(wtau[i]);
		    for (int j=0;j<t.length;j++)
		    {
		    	swttau[j] = swt[j]*cwtau[i] - cwt[j]*swtau[i];
		    	cwttau[j] = cwt[j]*cwtau[i] + swt[j]*swtau[i];
		    	cwttau2[j]=(float) Math.pow(cwttau[j], 2);
		    	swttau2[j]=(float) Math.pow(swttau[j],2);		
			} 
		    
		    A=MatrixMTI(Rout,cwttau2);
		    B=MatrixMT(cwttau,cwttau);
		    C=MatrixMTI(Rout,swttau2);
		    D=MatrixMT(swttau,swttau);	
		  	P[i] =(float) ((Math.pow(A, 2))/B + (Math.pow(C, 2))/D);
		    P[i]= P[i]/(2*vx);
		    
					}
	int index=0;

	index=maxindex(P);
	valuef= (f[index]*100*32);

	return valuef;
	
		
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
	//To calculate matrix multiplication.
	public static float MatrixMT(float[] cwt2,float[] array1 )
	{
		float  array2;
		int x1=cwt2.length;
		array2=0;
		for (int i=0; i<x1-1;i++) 
	        array2 =array2+(cwt2[i]*array1[i]);     
	return array2;
	}
	//To calculate matrix multiplication with the inverse
	public static float MatrixMTI(int[] rout,float[] array1 )
	{
		float  array2;
		int x1=rout.length;
		array2=0;
		for (int i=0; i<x1-1;i++) 
	        array2 =array2+(rout[i]*array1[i]);     
	return array2;
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


	private static int maxindex(float[] t) {
	    float maximum = t[0];   // start with the first value
	    int index=0;
	    for (int i=1; i<t.length/2; i++) {
	        if (t[i] > maximum) {
	            maximum = t[i];// new maximum
	            index=i;
	        }
	    }
	    return index;

	}// end of max index
	
 	public native float nativelomb(int[] jbuf, int len);
    static 
    {
        System.loadLibrary("feature");
    }

}
