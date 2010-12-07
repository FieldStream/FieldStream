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

//@author Mishfaq Ahmed

package org.fieldstream.service.context.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.fieldstream.Constants;
import org.fieldstream.service.context.model.ModelCalculation.FeatureSet;
import org.fieldstream.service.sensor.ContextBus;


public class SelfSmokingModel extends ModelCalculation{

	@Override
	public int getCurrentClassifier() {
		return 0;
	}

	@Override
	public int getID() {
		// TODO Auto-generated method stub
		return Constants.MODEL_SELF_SMOKING;
	}


	@Override
	public ArrayList<Integer> getUsedFeatures() {

		return new ArrayList<Integer>();
	}

	private final static HashMap<Integer, String> outputDescription = new HashMap<Integer, String>() {
		{
			put(1, "Smoking Event Started");
		}
	};
	
	public HashMap<Integer, String> getOutputDescription() {
		return outputDescription;
	}


	
//	public HashMap<Integer, String> getOutputDescription() {
//		return outputDescription;
//	}

	
	public void computeContext(FeatureSet fs) {
		// TODO Auto-generated method stub
		//ContextBus.getInstance().pushNewContext(Constants.MODEL_TEST, 0, fs.getBeginTime(), fs.getEndTime());		
	}
	
}
