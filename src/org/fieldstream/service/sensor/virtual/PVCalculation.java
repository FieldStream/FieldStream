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

//@author Amin Ahsan Ali
//@author Mahbub Rahman


import java.util.ArrayList;
public class PVCalculation {

	
	public static boolean isPeak=false;							
	/**
	 * Last index of previous window; next window peak/valley index should be added with this. 
	 * this is the connection between two window
	 */
	public static int lastPointIndexOfPrevWindow=0;

	public static int globalIndex=0;

	/**
	 * calculates the real peak valleys in two steps: a) find local max min from the given samples
	 * and b) then find real peaks and valleys
	 * It depends on a threshold value. all the real peaks will be above the threshold value.
	 * adaptive threshold is calculated according to the output of real peak value.
	 * whoever is using this function should also implement adaptive threshold value code. (e.g RealPeakValleyVirtualSensor.java)
	 * @param data
	 * @param timestamp
	 * @return real peaks and valleys as an integer array
	 * @author Mahbub
	 */
	public int[]calculate(int[] data, long[] timestamp)
	{
		int localMaxMin[]=getAllPeaknValley(data);
		int realPeakValley[]=getRealPeaknValley(localMaxMin);
		return realPeakValley;
	}
	/**
	 * calculates peaks and valleys (false + real) from the data buffer
	 * @param buffer
	 * @return list of tuple containing (valleyIndex, valley, peakIndex, peak). so if any method wants to use this method, it should read all the four values together.
	 * @author Mahbub
	 */
	public int[] getAllPeaknValley(int[] data)					//consider the timestamp issues. because it is important
	{

		int prev_value1=0;
		int curr_value=0;
		boolean isStarting=true;
		int length=data.length;
		ArrayList<Integer> list=new ArrayList<Integer>();

		try {
			for(int i=0;i<length;){
				int line;
				if(isStarting && (i < length-1))
				{
					isStarting=false;
					prev_value1=data[i++];
					globalIndex++;
					curr_value=data[i++];
					globalIndex++;
					//skipping up to the first increasing sequence
					while((prev_value1>=curr_value)&& (i < length))
					{
						prev_value1=curr_value;
						line=data[i++];
						globalIndex++;
						curr_value=line;
					}
					list=addToTheList(list, globalIndex-1, prev_value1);		//prev_value1 is the current valley
					continue;
				}
				if(curr_value>prev_value1 )			//this means the sequence is increasing
				{
					while((prev_value1<=curr_value)&& (i < length))
					{
						prev_value1=curr_value;
						line=data[i++];
						globalIndex++;
						curr_value=line;
					}
					list=addToTheList(list,globalIndex-1, prev_value1);		//prev_value1 is the current valley
				}else //if(Integer.parseInt(curr_value)<Integer.parseInt(prev_value1))
				{
					while((prev_value1>=curr_value)&& (i < length))
					{
						prev_value1=curr_value;
						line=data[i++];
						globalIndex++;
						curr_value=line;
					}
					if(i!=length)
						list=addToTheList(list,globalIndex-1, prev_value1);		//prev_value1 is the current valley
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//converting the ArrayList to array
		int peakValleys[]=new int[list.size()];
		for(int i=0;i<list.size();i++)
		{
			peakValleys[i]=list.get(i).intValue();
		}
		return peakValleys;
	}
	public ArrayList<Integer> addToTheList(ArrayList<Integer> list,int anchorIndex, int anchorValue)
	{
		Integer val=new Integer(anchorValue);
		Integer ind=new Integer(anchorIndex);
		list.add(ind);
		list.add(val);
		return list;
	}
	/**
	 * calculates real peaks and valleys from the data buffer
	 * @param data
	 * @return list of tuple containing (valleyIndex, valley, peakIndex, peak). so if any method wants to use this method, it should read all the four values together.
	 * @author Mahbub
	 */
	public int[] getRealPeaknValley(int[] data)			//check whether it is multiple of four....if not then discard the last part which does not fit to
	{

		boolean isStarting=true;
		ArrayList<Integer> list=new ArrayList<Integer>();

		int prev1_valleyIndex=0;
		int prev1_valley=0;		
		int prev1_peakIndex=0;
		int prev1_peak=0;
		int current_valleyIndex=0;
		int current_valley=0;		
		int current_peakIndex=0;
		int current_peak=0;
		int valleyAnchor=0;
		int valleyAnchorIndex=0;
		int realPeak=0;
		int realPeakIndex=0;
		int realValley=0;
		int realValleyIndex=0;


		//I have to consider four values together to calculate the real peaks and valleys
		int i=0;
		int size=data.length;
		outer:
		for(;i<size;)
		{
			if(isStarting)		//check ...it should be equal or greater
			{
				//find the first real valley
				isStarting=false;
				if((size-i)<4)
				{
					i+=4;
					continue outer;
				}
				prev1_valleyIndex=data[i];
				prev1_valley=data[i+1];
				prev1_peakIndex=data[i+2];
				prev1_peak=data[i+3];
				valleyAnchor=prev1_valley;
				valleyAnchorIndex=prev1_valleyIndex;
				if(prev1_peak>=RealPeakValleyVirtualSensor.peakThreshold)
				{
					realPeak=prev1_peak;
					realPeakIndex=prev1_peakIndex;
					realValley=valleyAnchor;
					realValleyIndex=valleyAnchorIndex;
					list=addToTheList(list, realValleyIndex, realValley);
					list=addToTheList(list, realPeakIndex, realPeak);
				}
				i+=4;
				if((size-i)<4)
				{
					i+=4;
					continue outer;
				}
				current_valleyIndex=data[i];
				current_valley=data[i+1];		
				current_peakIndex=data[i+2];
				current_peak=data[i+3];
				i+=4;
			}

			if(current_peak>prev1_peak)			//this means the sequence is increasing
			{
				while(prev1_peak<=current_peak)		//this is increasing trend
				{
					if((current_peak>=RealPeakValleyVirtualSensor.peakThreshold) && realPeak!=0)				//then the previous valleyAnchor is real valley, check the peak to previous real peak against duration threshold
					{
						//then real valley update, inhalation period, exhalation period, IE ratio.
						if((current_peakIndex- realPeakIndex)>=RealPeakValleyVirtualSensor.durationThreshold)
						{
							realValley=current_valley;//realValley=Integer.parseInt(curr_value[1]);

							realValleyIndex=current_valleyIndex;//realValleyIndex=Integer.parseInt(curr_value[0]);

							realPeak=current_peak;//realPeak=Integer.parseInt(curr_value[3]);
							realPeakIndex=current_peakIndex;//realPeakIndex=Integer.parseInt(curr_value[2]);
							list=addToTheList(list, realValleyIndex, realValley);
							list=addToTheList(list, realPeakIndex, realPeak);
						}
						else		//pick the higher peak as the real peak.
						{
							//inhalationPeriod=realPeakIndex-realValleyIndex;
						}
					}
					prev1_valleyIndex=current_valleyIndex;
					prev1_valley=current_valley;
					prev1_peakIndex=current_peakIndex;
					prev1_peak=current_peak;
					if((size-i)<4)
					{
						i+=4;
						continue outer;
					}
					current_valleyIndex=data[i];				//line=dis.readLine();
					current_valley=data[i+1];		
					current_peakIndex=data[i+2];
					current_peak=data[i+3];
					i+=4;										//curr_value=line.split(" ");
				}

				if(prev1_peak>=RealPeakValleyVirtualSensor.peakThreshold)				//then the previous valleyAnchor is real valley, check the peak to previous real peak against duration threshold
				{
					if(realPeak==-1)
					{
						realPeak=prev1_peak;//realPeak=Integer.parseInt(prev_value1[3]);
						realPeakIndex=prev1_peakIndex;//realPeakIndex=Integer.parseInt(prev_value1[2]);
						realValley=valleyAnchor;
						realValleyIndex=valleyAnchorIndex;
						list=addToTheList(list, realValleyIndex, realValley);
						list=addToTheList(list, realPeakIndex, realPeak);
					}
					else
					{
						if((prev1_peakIndex- realPeakIndex)>=RealPeakValleyVirtualSensor.durationThreshold)		//then real valley update, inhalation period, exhalation period, IE ratio.
						{
							realValley=valleyAnchor;
							realValleyIndex=valleyAnchorIndex;

							realPeak=prev1_peak;//realPeak=Integer.parseInt(prev_value1[3]);
							realPeakIndex=prev1_peakIndex;//realPeakIndex=Integer.parseInt(prev_value1[2]);
							list=addToTheList(list, realValleyIndex, realValley);
							list=addToTheList(list, realPeakIndex, realPeak);
						}
						else
						{
						}
					}
				}
			}else
			{
				while(prev1_peak>=current_peak)		//this is decreasing trend
				{
					if((current_peak>=RealPeakValleyVirtualSensor.peakThreshold) && ((current_peakIndex- realPeakIndex)>=RealPeakValleyVirtualSensor.durationThreshold) && realPeak!=0)				//then the previous valleyAnchor is real valley, check the peak to previous real peak against duration threshold
					{
						realValley=current_valley;
						realValleyIndex=current_valleyIndex;

						realPeak=current_peak;
						realPeakIndex=current_peakIndex;
						list=addToTheList(list, realValleyIndex, realValley);
						list=addToTheList(list, realPeakIndex, realPeak);
					}
					prev1_valleyIndex=current_valleyIndex;			//prev_value1=curr_value;
					prev1_valley=current_valley;
					prev1_peakIndex=current_peakIndex;
					prev1_peak=current_peak;
					
					if((size-i)<4)
					{
						i+=4;
						continue outer;
					}
					
					current_valleyIndex=data[i];				//line=dis.readLine();
					current_valley=data[i+1];		
					current_peakIndex=data[i+2];
					current_peak=data[i+3];
					i+=4;										//curr_value=line.split(" ");
				}
				valleyAnchor=current_valley;//valleyAnchor=Integer.parseInt(curr_value[1]);
				valleyAnchorIndex=current_valleyIndex;//valleyAnchorIndex=Integer.parseInt(curr_value[0]);
			}
		}
		//converting the ArrayList to array
		int realPeakValleys[]=new int[list.size()];
		for(int j=0;j<list.size();j++)
		{
			realPeakValleys[j]=list.get(j).intValue();
		}
		return realPeakValleys;
	}
}
