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

//@author Mahbub Rahman
//@author Amin Ahsan Ali
package org.fieldstream.service.context.model;

import android.util.Log;

/*
 * For testing conversation module
 * mahbub
 */

public class ConversationPrediction {

	public final static int SPEAKING = 1;
	public final static int QUIET = 0;
	public final static int SMOKING = 2;


	public int getLabel(double features,double features2,double features3, double features4, double features5,
			double features6,double features7, double features8, double features9)
	{

		if(features4 <= 142.8)
			if(features7 <= 0.904083)
				if(features <= 44.4) return QUIET;
				else
					if(features9 <= 0.643939)
						if(features9 <= 0.61017)
							if(features5 <= 38.649708) return QUIET;
							else
								if(features8 <= 0.237643) return SPEAKING;
								else
									if(features4 <= 117.6) return QUIET;
									else
										if(features4 <= 124.8) return SPEAKING;
										else
											if(features4 <= 130.4) return QUIET;
											else
												if(features9 <= 0.555556) return SPEAKING;
												else return QUIET;
						else return SPEAKING;
					else
						if(features3 <= 83)
							if(features8 <= 0.354074) return QUIET;
							else
								if(features <= 83.2)
									if(features5 <= 48.61276)
										if(features5 <= 34.760612) return SPEAKING;
										else return QUIET;
									else return SPEAKING;
								else return QUIET;
						else
							if(features3 <= 100)
								if(features9 <= 0.686747) return QUIET;
								else return SPEAKING;
							else return QUIET;
			else return QUIET;
		else return SPEAKING;
	}
	public int getLabel_20100511(double mean_inhalation,double median_inhalation,double stdev_exhalation,double median_ieratio)
	{
		if(mean_inhalation <= 81.2)
			if(median_inhalation <= 61) return SPEAKING;
			else
				if(stdev_exhalation <= 39.62575) return QUIET;
				else return SPEAKING;
		else
			if(median_ieratio <= 0.776119)
				if(stdev_exhalation <= 26.87378) return QUIET;
				else return SPEAKING;
			else return QUIET;

	}
	//AdaboostM1 decision tree which gives almost 95% of accuracy
	public int getLablelSpeakingSmokingSilent(double median_ieratio,double secondbest_stretch,double median_stretch,double median_exhalation,double median_bduration,double mean_ieratio,double secondbest_inhalation,double mean_firstDiff,double median_firstDiff,double secondbest_exhalation,double mean_exhalation,double median_inhalation )
	{
		Log.d("DecisionTreeParam", "stretch_secondbest 14399 = "+secondbest_stretch+" stretch_median 11899 = "+median_stretch+" inhalation_median 11895 = "+median_inhalation+" inhalation_secondbest 14395 = "+secondbest_inhalation+" exhalation_mean 11296 = "+mean_exhalation+" exhalation_median 11896 = "+median_exhalation+" exhalation_secondbest 14396 = "+secondbest_exhalation+
				" ieratio_mean 11297 = "+mean_ieratio+" ieratio_median 11897 = "+median_ieratio+" bduration_median 11890 = "+median_bduration+" firstDiff_mean 11289 = "+mean_firstDiff+" median_firstDiff 11889 = "+median_firstDiff);
		if(median_ieratio <= 0.705357)
			if(secondbest_stretch <= 431)
				if(median_exhalation <= 100) return SPEAKING;
				else
					if(median_stretch <= 379)
						if(secondbest_inhalation <= 83) return SMOKING;
						else return QUIET;
					else return QUIET;
			else
				if(median_bduration <= 5) return SMOKING;
				else 
					if(mean_ieratio <= 0.672582)
						if(secondbest_inhalation <= 87)
							if(secondbest_inhalation <= 81)
								if(median_exhalation <= 167) return SPEAKING;
								else 
									if(mean_firstDiff <= 59.4) return SMOKING;
									else
										if(median_firstDiff <= 153) return SPEAKING;
										else return SMOKING;
							else
								if(median_bduration <= 18) return SMOKING;
								else
									if(secondbest_exhalation <= 265) return SPEAKING;
									else return SMOKING;
						else return SPEAKING;
					else 
						if(median_stretch <= 654) return SMOKING;
						else return SPEAKING;
		else
			if(mean_exhalation <= 162.2)
				if(median_inhalation <= 66) return SPEAKING;
				else return QUIET;
			else return SMOKING;
	}
	//here for training the tree, sliding window for 600 samples are used...trained using array version of the offline processing
	public int getLablelSpeakingSmokingSilentFromArrayVersion(double percentile_inhal, double std_inhal, double mean_exhal, double mean_ie,  
			double meadian_ie, double mean_bd,double secondBest_bd, double std_strch)
	{
		if(mean_exhal <= 126.125)
		   if(std_inhal <= 44.47134)
		      if(mean_bd <= 0.222222) return SPEAKING;
		      else return QUIET;
		   else return SMOKING;
		else
		   if(std_strch <= 315.59674)
		      if(mean_ie <= 0.665843)
		         if(percentile_inhal <= 50)
		            if(mean_exhal <= 132.4) return SMOKING;
		            else 
		               if(meadian_ie <= 0.2226) return SMOKING;
		               else return SPEAKING;
		         else
		            if(meadian_ie <= 0.4888) return SMOKING;
		            else return SPEAKING;
		      else return QUIET;
		   else 
		      if(secondBest_bd <= 20)
		         if(mean_bd <= 1.666667) return SPEAKING;
		         else return SMOKING;
		      else return SPEAKING;

	}
}
