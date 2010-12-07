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
/**
 * 
 */

public class HeartRateLomb {

	public static float[][] calculate(int[] Rout) {	
		double[] t = new double[Rout.length];
		double A,Asq,B,C,Csq,D;
		double Ss2wt;
		double Sc2wt;
		float mx,vx;
		int nt;
		int T;
		int nf;
		for (int i=0;i<Rout.length;i++)		
		{
			t[i] = (float) (i+1);
			//		   out.println(t[i]);
		}
		nt = t.length;
		T = nt-1;
		nf = (int) Math.round(0.5*4*1*nt);
		float[] f = new float[nf];
		for (int i=0;i<nf;i++)	 
		{
			f[i] = ((float)(i+ 0.0001))/(T*4);		
		}

		mx = mean(Rout);

		float[] wt=new float[t.length];
		float[] swt=new float[t.length];
		float[] cwt=new float[t.length];
		float[] cwt2=new float[t.length];
		float[] diff=new float[t.length];
		float[] sum=new float[t.length];
		float[] swttau=new float[t.length];
		float[] cwttau = new float[t.length];
		float wtau= 0;
		float swtau= 0;
		float cwtau= 0;
		float[] P= new float[f.length];
		vx = std(Rout,mx); 
		vx=(float) Math.pow(vx,2);
		for (int i=0;i<Rout.length;i++)	
		{
			Rout[i]  = (int) (Rout[i]-mx);
		}

		for (int i=0;i<nf;i++)  
		{
			for (int j=0;j<t.length;j++)
			{
				wt[j]  = (float) (2*Math.PI*f[i]*t[j]);
				swt[j] = (float) Math.sin(wt[j]);
				cwt[j] = (float) Math.cos(wt[j]);
				cwt2[j]= 2*cwt[j];  
				diff[j]=cwt[j]-swt[j];
				sum[j]=cwt[j]+swt[j];
			}
			Ss2wt = MatrixMTI(cwt2,swt);// Row by column to be done     	
			Sc2wt = MatrixMTI(diff,sum); 
			wtau  = (float) (0.5*Math.atan2(Ss2wt,Sc2wt));   	  
			swtau = (float) Math.sin(wtau);
			cwtau = (float) Math.cos(wtau);
			for (int j=0;j<t.length;j++)
			{
				swttau[j] = cwtau*swt[j] - swtau*cwt[j];
				cwttau[j] = cwtau*cwt[j] + swtau*swt[j];
			} 
			A=MatrixMT(Rout,cwttau);
			B=MatrixMTI(cwttau,cwttau);
			C=MatrixMT(Rout,swttau);
			D=MatrixMTI(swttau,swttau);	
			P[i] =(float) ((Math.pow(A, 2))/B + (Math.pow(C, 2))/D);
			P[i]= P[i]/(2*vx);
			//  out.println(P[i]);
		}
		float[][] send =new float[2][f.length];
		System.arraycopy(P,0,send[0],0,f.length);
		System.arraycopy(f,0,send[1],0,f.length);
		return send;
	}

	private static float MatrixMT(int[] rout,float[] array1 )
	{
		float  array2;
		int x1=rout.length;
		array2=0;
		for (int i=0; i<x1;i++) 
			array2 =array2+(rout[i]*array1[i]);     
		return array2;
	}

	private static float MatrixMTI(float[] rout,float[] array1 )
	{
		float  array2;
		int x1=rout.length;
		array2=0;
		for (int i=0; i<x1;i++) 
			array2 =array2+(rout[i]*array1[i]);     
		return array2;
	}

	private static float std(int[] rR2, float meanRR) {
		float store1 = 0;
		for (int i = 0; i < rR2.length; i++) {
			store1 += (rR2[i] - meanRR) * (rR2[i] - meanRR);
		}
		float std1 = (float) Math.sqrt(store1 / rR2.length);
		return std1;
	}

	private static float mean(int[] rR2) {
		int store = 0;
		float mean1;
		for (int i = 0; i < rR2.length; i++) {
			store += rR2[i];
		}
		if (rR2.length != 0) {
			mean1 = store / rR2.length;
		} else {
			mean1 = 0;
		}
		return mean1;
	}	
}
