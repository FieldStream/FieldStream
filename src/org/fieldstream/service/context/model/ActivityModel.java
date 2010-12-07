//Copyright (c) 2010, Carnegie Mellon University
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
//    * Neither the name of Carnegie Mellon University nor the names of its contributors may be used to 
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
package org.fieldstream.service.context.model;

/**
 * @author Scott Fisk
 * This class stores the model currently used for activity classification...
 * It is a simple test decision tree for a holster  or hand worn Android.
 */
public class ActivityModel {
	
	
	public static final float[][] dtChest = new float[][] {
		/*
			MCRZ <= 11: 0 (750.0)
			MCRZ > 11
			|   MADZ <= 22.5: 1 (686.0)
			|   MADZ > 22.5: 2 (808.0)
		*/
		// MCRZ
		{0,(float) 11.0,0,-1},
		// MADZ
		{1,(float) 22.5,1,2},
	};

	
	@SuppressWarnings("unused")
	public static final float[][] dtHolst = new float[][] {
		// var
		{0,(float) 004.8202,-1,-2},
		// mean
		{1,(float) 973.2087,3,-3},
		//mad
		{2,(float) 112.2808,-4,5},
		//mean
		{1,(float) 1007.2515,-5,4},
		// rms
		{3,(float) 981.9117,3,1},
		// rms
		{3,(float) 1002.4042,4,2}
	};
	@SuppressWarnings("unused")
	private static final float[][] dtHand = new float[][] {
		// var
		{0,(float) 0.10693,-1,-2},
		// mean
		{1,(float) 9.807366,-3,4},
		//mean
		{1,(float) 10.092329,-4,5},
		//rms
		{3,(float) 9.433453,4,-5},
		// mad
		{2,(float) 0.850103,-6,1},
		// mad
		{2,(float) 0.140919,3,-7},
		// mad
		{2,(float) 0.559277,-8,-9},
		// rms
		{3,(float) 9.656594,-10,4},
		// mean
		{1,(float) 9.469759,3,2},
		// std
		{4,(float) 1.130971,1,-11},
		// mean
		{1,(float) 9.613225,2,3},
		// mad
		{2,(float) 0.671841,-12,-13},
		// std
		{4,(float) 1.159792,4,3},
		// rms
		{3,(float) 10.015569,2,1}
	};

	
	
}
