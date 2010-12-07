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

public class RespirationAMP extends AbstractFeature {

	private double respamplitude;


	public RespirationAMP(int ID, boolean checkUpdate, float thresholdInput) {
		super(ID, checkUpdate, thresholdInput);

	}

	@Override
	public double calculate(int[] buffer, long[] timestamps, int sensor) {
		int[] datapks = findpks1(buffer);
        int[] datavalleys = findvalleys(buffer);
        float mpk = mean(datapks);
        float mval =mean(datavalleys);
        respamplitude = mpk-mval;
        return respamplitude;
		
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

	
	private int[] findpks1(int[] tlpbody02) {
		int SIZE = tlpbody02.length;
		int[] pks = new int[SIZE];
		int[] valuepks = new int[SIZE];
		int count = 0;
		for (int j = 2; j < SIZE-2; j++) {
			if (tlpbody02[j - 2] < tlpbody02[j - 1] && tlpbody02[j - 1] < tlpbody02[j]
			                                                                && tlpbody02[j] >= tlpbody02[j + 1]
			                                                                                         && tlpbody02[j + 1] > tlpbody02[j + 2]) {
				pks[count] = j - 1 ;
				valuepks[count] = tlpbody02[j];
				//out1.println(valuepks[count]);
				count++;
			}
		}

		int[] sendpks;
		if (count == 0) {
			sendpks = new int[0];
		}
		else { 
			sendpks = new int[count-1];
			for (int m=0;m<count-1;m++)
				sendpks[m]=valuepks[m];
		}
	    return(sendpks);
	}
	//find valleys
	private int[] findvalleys(int[] tlpbody02) {
		int SIZE = tlpbody02.length;
		int[] pks = new int[SIZE];
		int[] valuepks = new int[SIZE];
		int count = 0;
		for (int j = 2; j < SIZE-2; j++) {
			if (tlpbody02[j - 2] > tlpbody02[j - 1] && tlpbody02[j - 1] > tlpbody02[j]
			                                                                && tlpbody02[j] <= tlpbody02[j + 1]
			                                                                                         && tlpbody02[j + 1] < tlpbody02[j + 2]) {
				pks[count] = j - 1 ;
				valuepks[count] = tlpbody02[j];
				//out1.println(pks[count]);
				count++;
			}
		}

		int[] sendpks;
		if (count == 0) {
			sendpks = new int[0];
		}
		else { 
			sendpks = new int[count-1];
			for (int m=0;m<count-1;m++)
				sendpks[m]=valuepks[m];
		}
	    return(sendpks);
	}
	
}
