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

//@author Kurt Plarre

package org.fieldstream.service.context.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.fieldstream.Constants;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensor.ContextBus;
import org.fieldstream.service.sensor.ContextSubscriber;


public class AccumulationModel extends ModelCalculation implements ContextSubscriber{
	public final static int N=120;

	private final String tag="AccumulationModel";

	public static double[] EMABuff=new double[N];
	public static double[] DECBuff=new double[N];
	public static int Head=0;
	public static double S=-1;

	public static double lastEMAval=-1;
	public static double lastDECval=-1;
	public static double estimatedEMA=-1;

	public static double alpha=-1;
	public static double beta=-1;
	public static double A11=0;
	public static double A12=0;
	public static double A22=0;
	public static double B1=0;
	public static double B2=0;

	private final static ArrayList<Integer> featureLabels = new ArrayList<Integer>(){
	{add(Constants.getId(Constants.FEATURE_NULL, Constants.SENSOR_BATTERY_LEVEL));}};
		// ==================================

		public AccumulationModel(){
			for(int i=0;i<N;i++){
				EMABuff[i]=-1;
				DECBuff[i]=-1;
			}
			ContextBus.getInstance().subscribe(this);
		}

		// ==================================
		public void setEMA(double EMAval){
			lastEMAval=EMAval;
		}
		public void setDEC(double DECval){
			lastDECval=DECval;
		}
		public void push(){
			EMABuff[Head]=lastEMAval;
			DECBuff[Head]=lastDECval;
			Head=(Head+1)%N;
		}
		// ==================================

		// ==================================
		public void estimateParams(){
			int count=0;
			for(int i=0;i<N-1;i++){
				if(EMABuff[(Head+i)%N]>=0&&EMABuff[(Head+i)%N]>=0&&DECBuff[(Head+i+1)%N]>=0&&DECBuff[(Head+i+1)%N]>=0){
					A11=A11+EMABuff[(Head+i)%N]*EMABuff[(Head+i)%N];
					A12=A12+EMABuff[(Head+i)%N]*DECBuff[(Head+i)%N];
					A22=A22+DECBuff[(Head+i)%N]*DECBuff[(Head+i)%N];
					B1=B1+EMABuff[(Head+i+1)%N]*EMABuff[(Head+i)%N];
					B2=B2+EMABuff[(Head+i+1)%N]*DECBuff[(Head+i+1)%N];
					count++;
				}
			}
			double det=A11*A22-A12*A12;
			double numa=A22*B1-A12*B2;
			double numb=-A12*B1+A11*B2;
			if(det>1e-6&&count>5){
				alpha=numa/det;
				beta=numb/det;
			}
		}
		// ==================================

		// ==================================
		public void estimateEMA(){
			//estimatedEMA=0;
			if(beta>0){
				estimatedEMA=0.5;
				S=0.5;
				for(int i=0;i<N;i++){
					if(EMABuff[(Head+i)%N]>=0&&DECBuff[(Head+i)%N]>=0){
						estimatedEMA=alpha*estimatedEMA+beta*DECBuff[(Head+i)%N];
						S=alpha*S+beta;
					}
				}
				if(S>1e-6||S<-1e-6){
					estimatedEMA=estimatedEMA/S;
				}else{
					estimatedEMA=-1;
				}
			}
		}
		// ==================================

		public synchronized void setLabel(float label) {
			Log.d(tag,"Received EMA "+label);
			setEMA(label);
		}

		public void receiveContext(int modelID, int label, long startTime, long endTime) {
			if(modelID==Constants.MODEL_STRESS){
				Log.d(tag,"Received Stress "+label);
				setDEC(label);
				push();
				estimateParams();
				estimateEMA();
				
				int out = 0;
				if (estimatedEMA >= 0.7)
					out = 1;
				
				ContextBus.getInstance().pushNewContext(getID(), out, startTime, endTime);				
			}
		}

		@Override
		public void computeContext(FeatureSet fs) {
			// TODO Auto-generated method stub

		}

		@Override
		public int getCurrentClassifier() {
			// TODO Auto-generated method stub
			return (int)(estimatedEMA*10000);
		}

		@Override
		public int getID() {
			// TODO Auto-generated method stub
			return Constants.MODEL_ACCUMULATION;
		}

		private final static HashMap<Integer, String> outputDescription = new HashMap<Integer, String>() {
			{
				put(-1,"Not Initialized Yet");
				put(1,"Not Stressed");
				put(2,"Stressed");				
			}
		};
		
		
		@Override
		public HashMap<Integer, String> getOutputDescription() {
			// TODO Auto-generated method stub
			return outputDescription;
		}

		@Override
		public ArrayList<Integer> getUsedFeatures() {
			// TODO Auto-generated method stub
			return featureLabels;
		}
	}


