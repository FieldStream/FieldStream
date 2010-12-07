//Copyright (c) 2010, University of Memphis, Carnegie Mellon University
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
//    * Neither the names of the University of Memphis and Carnegie Mellon University nor the names of its 
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

// @author Patrick Blitz
// @author Brian French
// @author Andrew Raij


package org.fieldstream.service.logger;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.fieldstream.service.logger.Log;

import android.os.Handler;
import android.os.HandlerThread;

public class TextFileLogger extends AbstractLogger {
	
	String tag;
	String data;
	private BufferedWriter out;
	private Handler myhandel;
	
	public TextFileLogger(String DIRNAME, boolean automaticLogging){
		super(automaticLogging);
		tag="STRESS";
		mythread = new HandlerThread("LogService."+tag);
		mythread.start();
		myhandel = new Handler(mythread.getLooper());
		
		try {

			if ((new File(DIRNAME)).canWrite()) {
				String FILENAME = "log_" + tag + ".txt";
				
				File dir = new File(DIRNAME);
				dir.mkdirs();
				File acclog = new File(DIRNAME, FILENAME);
				FileWriter writer = new FileWriter(acclog, true);

					out = new BufferedWriter(writer, 8192);
				logRunner = new LogRunner(out);
			}
		} catch (IOException e) {
			Log.e("LoggerService", "Could not write file "
					+ e.getMessage());
		}
	}
	public void close() {
		try {
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		myhandel = null;
		mythread.getLooper().quit();
		mythread.stop();
		mythread = null;
		
		super.close();
	}
	private HandlerThread mythread;
	private LogRunner logRunner;
	
	class LogRunner implements Runnable {
		private BufferedWriter out;
		public LogRunner(BufferedWriter out) {
			this.out = out;
		}
		public int[] data;
		public int SensorID;
		public String logString;
		public long lastTimestamp;
		public int startNewData;
		public int endNewData;
		
		public void run() {
			if (SensorID!=0) {
				StringBuilder string = new StringBuilder("Sensors"+SensorID+";");
				int start, end;
				if (startNewData == endNewData) {
					start = 0;
					end = data.length;
				}
				else {
					start = startNewData;
					end = endNewData;
				}
				
				for (int i=start;i<end;i++) {
					string.append(data[i]).append(";");		
				}
				string.append(";@").append(lastTimestamp);
				logString=string.toString();
				SensorID=0;
			}
		
			try{
				out.write(logString + "\n");
			}
			catch (IOException e) {
				Log.e("LoggerService ", "Could not write file "
						+ e.getMessage());
			}
		}
	};
	
	public void logData(String data){
		//TODO faster way?
		
		logRunner.logString = data;
		
		if (myhandel!=null) {
		myhandel.post(logRunner);
		}
//		= data;
//		Th.start();
	}
	public void logFeatureData(int featureID, long timestampBegin, long timestampEnd, double value) {
		logData("Feature"+featureID+";"+value+"@"+timestampBegin + "-" + timestampEnd);
		
	}
	public void logModelData(int modelID, int label, long startTime, long endTime) {
		logData("Model"+modelID+";"+label+"@"+startTime + "-" + endTime);
		
	}
	public void logSensorData(int sensorID, long[] timestamps, int[] buffer, int startNewData, int endNewData) {
		// TODO Auto-generated method stub
		LogRunner logRunner2 = new LogRunner(out);
		//logRunner.logString = data;
		logRunner2.data=buffer;
		logRunner2.SensorID=sensorID;
		logRunner2.lastTimestamp = timestamps[timestamps.length-1];
		if (myhandel!=null) {
			myhandel.post(logRunner2);
		}
//		String result = "Sensors"+sensorID+";";

//		logData(string.toString());
	}

	@Override
	public void logEMA(int triggerType, String emaContext, int status, long prompt,
			long delayDuration, String[] delayResponses,
			long[] delayResponseTimes, long start, String[] responses,
			long[] responseTimes) {

		String logString;
		logString = "EMA;" + "triggerType=" + triggerType + ", status=" + status + ", activeContexts=" + emaContext +
		  			", promptTime=" + prompt + ", delayDuration=" + delayDuration + 
		  			", delayResponses=" + delayResponses.toString() + ", delayResponseTimes=" + responseTimes.toString() + ", startTime=" + start + 
		  			"responses=" + responses.toString() + ", responseTimes=" + responseTimes.toString();
		
		logData(logString);
	}
	
	@Override
	public void logUIData(String data) {
		logData(data.concat(";"+System.currentTimeMillis()));		
	}

	@Override
	public void logPerformance(int location, long timestamp, String logString) {
		//never does anything, as this is only useful for DB logging		
	}

	@Override
	public void logIncentiveEarned(int incentiveID, String comment, long timestamp, float amount, float total) {
		String logString;
		logString = "Incentive;" + "timestamp=" + timestamp + ", id=" + incentiveID + ", comment=" + comment + ", lastEarned=" + amount + 
		  			", totalEarned=" + total;
		
		logData(logString);
	}
	@Override
	public void logDeadPeriod(long start, long end) {
		String logString;
		logString = "DeadPeriod;" + "start=" + start + ", end=" + end;
		
		logData(logString);
	}
	@Override
	public double getTotalIncentivesEarned() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void logResume(long timestamp) {
		String logString;
		logString = "Resume;" + "timestamp=" + timestamp;
		
		logData(logString);
	}

	@Override
	public void logAnything(String name, String value, long timestamp) {
		// TODO Auto-generated method stub
		String logString;
		logString = name + ";" + "timestamp=" + timestamp + ", entry=" + value;
		
		logData(logString);
		
	}	
}
