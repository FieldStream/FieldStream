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

import org.fieldstream.service.features.Percentile;
import org.fieldstream.service.logger.Log;

public class RPVCalculationNew {

	
	public static boolean isPeak=false;							
	/**
	 * Last index of previous window; next window peak/valley index should be added with this. 
	 * this is the connection between two window
	 */
	public static int lastPointIndexOfPrevWindow=0;

	public int globalIndex=0;

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
		globalIndex=0;
		String raw="";
		Log.d("RPVRaw","raw length= "+data.length);
		int len=data.length/3;
//		for(int k=0;k<3;k++)
//		{
//			for(int i=k*len/3;i<(len/3)*(k+1);i++)
//				raw+=data[i]+",";
//			Log.d("RPVRaw","raw:"+k+" = "+raw);
//			raw="";
//		}
		RealPeakValleyVirtualSensor.peakThreshold=(int)Percentile.evaluate(data, 70.0F);;
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

		int prev1_valleyIndex=-1;
		int prev1_valley=-1;		
		int prev1_peakIndex=-1;
		int prev1_peak=-1;
		int current_valleyIndex=-1;
		int current_valley=-1;		
		int current_peakIndex=-1;
		int current_peak=-1;
		int valleyAnchor=-1;
		int valleyAnchorIndex=-1;
		int realPeak=-1;
		int realPeakIndex=-1;
		int realValley=-1;
		int realValleyIndex=-1;
		
		//int valleyAnchorIndex=-1;
		//int valleyAnchor=-1;
		int valleyAnchorIndex1=-1;
		int valleyAnchor1=-1;
		int peakAnchor=-1;
		int peakAnchorIndex=-1;


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
//				if(prev1_peak>=RealPeakValleyVirtualSensor.peakThreshold)
//				{
//					realPeak=prev1_peak;
//					realPeakIndex=prev1_peakIndex;
//					realValley=valleyAnchor;
//					realValleyIndex=valleyAnchorIndex;
//					list=addToTheList(list, realValleyIndex, realValley);
//					list=addToTheList(list, realPeakIndex, realPeak);
//				}
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
					if((current_peak>=RealPeakValleyVirtualSensor.peakThreshold) /*&& realPeak!=0*/)				//then the previous valleyAnchor is real valley, check the peak to previous real peak against duration threshold
					{
						//then real valley update, inhalation period, exhalation period, IE ratio.
						if(peakAnchorIndex!=-1||(current_peakIndex - realPeakIndex)>=RealPeakValleyVirtualSensor.durationThreshold || realPeak==-1)
						{
							if((peakAnchorIndex!=-1)&&((current_peakIndex- peakAnchorIndex)>=RealPeakValleyVirtualSensor.durationThreshold)&& (realPeakIndex!=peakAnchorIndex)&& peakAnchorIndex>valleyAnchorIndex)
							{
								realPeak=peakAnchor;
								realPeakIndex=peakAnchorIndex;
								if(valleyAnchor<RealPeakValleyVirtualSensor.peakThreshold || valleyAnchorIndex<valleyAnchorIndex1)
								{
									realValley=valleyAnchor;
									realValleyIndex=valleyAnchorIndex;
								}
								else
								{
									realValley=valleyAnchor1;					//this is a previous valley candidate
									realValleyIndex=valleyAnchorIndex1;
								}
								peakAnchor=current_peak;
								peakAnchorIndex=current_peakIndex;
								if(realPeak!=-1)
								{
									list=addToTheList(list, realValleyIndex, realValley);
									list=addToTheList(list, realPeakIndex, realPeak);
								}
							}
							peakAnchor=current_peak;
							peakAnchorIndex=current_peakIndex;
							if(current_valley<RealPeakValleyVirtualSensor.peakThreshold || realValleyIndex==prev1_peakIndex)
							{
								valleyAnchor=current_valley;
								valleyAnchorIndex=current_valleyIndex;
							}
							else
							{
//								valleyAnchor=prev1_valley;
//								valleyAnchorIndex=prev1_valleyIndex;
								if(prev1_valley<RealPeakValleyVirtualSensor.peakThreshold)
								{
									valleyAnchor=prev1_valley;
									valleyAnchorIndex=prev1_valleyIndex;
								}else
								{
									int m=getValleyAnchorIndexBelowThreshold(data, i+1, realPeak);
									if(m==0)
									{
										valleyAnchor=prev1_valley;
										valleyAnchorIndex=prev1_valleyIndex;
									}
									else if(data[i]<peakAnchorIndex)
									{
										valleyAnchor=data[m];
										valleyAnchorIndex=data[m-1];
									}
								}
							}
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
				if(realPeakIndex<peakAnchorIndex && realPeakIndex!=-1)
				{
					
					realPeak=peakAnchor;
					realPeakIndex=peakAnchorIndex;
					
					if(valleyAnchor<RealPeakValleyVirtualSensor.peakThreshold ||valleyAnchorIndex1<realPeakIndex || valleyAnchorIndex<valleyAnchorIndex1 || (valleyAnchorIndex1<realValleyIndex && valleyAnchorIndex>realValleyIndex))
					{
						realValley=valleyAnchor;
						realValleyIndex=valleyAnchorIndex;
					}
					else
					{
						realValley=valleyAnchor1;					//this is a previous valley candidate
						realValleyIndex=valleyAnchorIndex1;
					}
					list=addToTheList(list, realValleyIndex, realValley);
					list=addToTheList(list, realPeakIndex, realPeak);
				}
				else if(current_peak>=RealPeakValleyVirtualSensor.peakThreshold)
				{
					if(realPeakIndex==-1 && realPeakIndex<peakAnchorIndex)
					{
						//check this thing
						realPeak=peakAnchor;
						realPeakIndex=peakAnchorIndex;
						realValley=valleyAnchor;
						realValleyIndex=valleyAnchorIndex;
						list=addToTheList(list, realValleyIndex, realValley);
						list=addToTheList(list, realPeakIndex, realPeak);
					}
					if((current_peakIndex - realPeakIndex)>=RealPeakValleyVirtualSensor.durationThreshold)
					{
						peakAnchor=current_peak;
						peakAnchorIndex=current_peakIndex;
						if(current_valley<RealPeakValleyVirtualSensor.peakThreshold || realValleyIndex==prev1_peakIndex)
						{
							valleyAnchor=current_valley;
							valleyAnchorIndex=current_valleyIndex;
						}
						else
						{
//													valleyAnchor=prev1_valley;
//													valleyAnchorIndex=prev1_valleyIndex;
							if(prev1_valley<RealPeakValleyVirtualSensor.peakThreshold)
							{
								valleyAnchor=prev1_valley;
								valleyAnchorIndex=prev1_valleyIndex;
							}else
							{
								int m=getValleyAnchorIndexBelowThreshold(data, i+1, realPeak);
								if(m==0)
								{
									valleyAnchor=prev1_valley;
									valleyAnchorIndex=prev1_valleyIndex;
								}
								else if(data[i]<peakAnchorIndex)
								{
									valleyAnchor=data[m];
									valleyAnchorIndex=data[m-1];
								}
							}
						}
					}
				}
			}else
			{
				while(prev1_peak>=current_peak)		//this is decreasing trend
				{
					if((current_peak>=RealPeakValleyVirtualSensor.peakThreshold) && ((current_peakIndex - realPeakIndex)>=RealPeakValleyVirtualSensor.durationThreshold) && realPeak!=-1)				//then the previous valleyAnchor is real valley, check the peak to previous real peak against duration threshold
					{
						if(realPeakIndex<peakAnchorIndex && realPeakIndex!=-1 && ((current_peakIndex - peakAnchorIndex)>=RealPeakValleyVirtualSensor.durationThreshold))
						{
							realPeak=peakAnchor;
							realPeakIndex=peakAnchorIndex;
							if(valleyAnchor<RealPeakValleyVirtualSensor.peakThreshold)
							{
								realValley=valleyAnchor;
								realValleyIndex=valleyAnchorIndex;
							}
							else
							{
								realValley=valleyAnchor1;					//this is a previous valley candidate
								realValleyIndex=valleyAnchorIndex1;
							}					
							list=addToTheList(list, realValleyIndex, realValley);
							list=addToTheList(list, realPeakIndex, realPeak);
						}
						peakAnchor=current_peak;
						peakAnchorIndex=current_peakIndex;
						if(current_valley<RealPeakValleyVirtualSensor.peakThreshold || realValleyIndex==prev1_valleyIndex)
						{
							valleyAnchor=current_valley;
							valleyAnchorIndex=current_valleyIndex;
						}
						else
						{
//							valleyAnchor=prev1_valley;
//							valleyAnchorIndex=prev1_valleyIndex;
							if(prev1_valley<RealPeakValleyVirtualSensor.peakThreshold)
							{
								valleyAnchor=prev1_valley;
								valleyAnchorIndex=prev1_valleyIndex;
							}else
							{
								int m=getValleyAnchorIndexBelowThreshold(data, i+1, realPeak);
								if(m==0)
								{
									valleyAnchor=prev1_valley;
									valleyAnchorIndex=prev1_valleyIndex;
								}
								else if(data[i]<peakAnchorIndex)
								{
									valleyAnchor=data[m];
									valleyAnchorIndex=data[m-1];
								}
							}
						}
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
				valleyAnchor1=current_valley;
				valleyAnchorIndex1=current_valleyIndex;
			}
		}
		ArrayList<Integer> list1=postProcessing(list);
		//converting the ArrayList to array
		int realPeakValleys[]=new int[list1.size()];
		for(int j=0;j<list1.size();j++)
		{
			realPeakValleys[j]=list1.get(j).intValue();
		}
		return realPeakValleys;
	}
	/**
	 * @param prevRealPeakIndex prev real peak location in local min max array array
	 * @param data local min max array in [index+valley ,index+peak] format
	 * @param startIndex the starting point of the back tracking to search the valley anchor
	 * @return valleyAnchor Index
	 */
			
	public int getValleyAnchorIndexBelowThreshold(int data[],int startIndex, int prevRealPeak)
	{
		int prevRealPeakIndex=0;
		if(prevRealPeak==-1)
			return 0;
		if(startIndex>=data.length)
			return 0;
		for(int j=startIndex;j>0;j=j-2)
		{
			if(data[j]==prevRealPeak)
			{
				prevRealPeakIndex=j;
				break;
			}
		}
		for(int i=startIndex;i>0;i=i-4)
		{
			if(data[i]<RealPeakValleyVirtualSensor.peakThreshold && i>prevRealPeakIndex)
				return i;
		}
		return 0;
	}
	/**
	 * eliminates if peaks are detected wrongly
	 * @param list
	 * @return
	 */
	public ArrayList<Integer> postProcessing(ArrayList<Integer> list)
	{
		if(list.size()<8) //at least two peaks required for this processing
			return list;
		int size=list.size();
		for(int i=2;i<size-4;i+=4)
		{
			if(list.get(i+4).intValue()-list.get(i).intValue()<RealPeakValleyVirtualSensor.durationThreshold)
			{
				size-=4;
				if(list.get(i+5).intValue()>list.get(i+1).intValue())
				{
					list.remove(i);
					list.remove(i);
					list.remove(i);
					list.remove(i);
				}
				else
				{
					list.remove(i+2);
					list.remove(i+2);
					list.remove(i+2);
					list.remove(i+2);
				}
			}
		}
		//for negetive inhalations.....//need to thought carefully later
		if(list.size()>=8)
		{
			for(int i=0;i<list.size()-4;i+=4)
			{
				if(list.get(i).intValue()==list.get(i+4).intValue()||list.get(i).intValue()<0) //for corrupted data, index might be -1 so we need to remove this things
				{
					list.remove(i);
					list.remove(i);
					list.remove(i);
					list.remove(i);
				}
			}
		}
		return list;
	}
}
