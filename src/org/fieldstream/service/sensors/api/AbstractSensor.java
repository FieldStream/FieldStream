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

// @author Amin Ahsan Ali
// @author Patrick Blitz
// @author Mahbub Rahman
// @author Andrew Raij

package org.fieldstream.service.sensors.api;

import org.fieldstream.Constants;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensor.SensorBus;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

public abstract class AbstractSensor {
	private static final int SCHEDULER_SIZE_FACTOR = 400;
	private static final String TAG = "AbstractSensor";
	/**
	 * the ID of this sensor, as defined in {@link Constants}
	 */
	public int ID;
	/**
	 * The Frame Rate of this sensor, if available
	 */
	public int frameRate;
	/**
	 * a ring buffer for all the values sensed in a window. In case of
	 * scheduler==true, this will be much bigger than the smallest window
	 */
	protected int[] buffer;
	/**
	 * timestamps array, same behavior and size as buffer array
	 */
	protected long[] timestamps;
	/**
	 * current start value of the ring buffer
	 */
	protected int startIndex;
	/**
	 * current index of the ring buffer, in case it's much bigger than the
	 * window size
	 */
	protected int bufferIndex;
	/**
	 * depending on @scheduler, this variable holds either the milliseconds
	 * until the scheduler will become active to clear the buffer or the number
	 * of samples until the buffer is cleared
	 */
	protected int windowSize;
	/**
	 * In case of Sliding windows, the amount of steps/samples the window is
	 * shifted each time. It also determines after how many samples a shift is
	 * carried out. Setting this to anything lower than 1 shift a seconds is at
	 * your own risk! Normally, values of 1-2 seconds (for 10Hz this would be
	 * 20) is sensible.
	 */
	protected int slidingWindowStep;
	/**
	 * defines if the buffer should be emptied by a scheduler (true) or by just
	 * hitting a size limit
	 */
	protected boolean scheduler = false;

	protected boolean active = false;

	private AbstractSensor INSTANCE;  
	
	
	private Thread thread = new Thread() {
		public void run() {
			  try {
			    // preparing a looper on current thread
			    // the current thread is being detected implicitly
			    Looper.prepare();
			 
			    // now, the handler will automatically bind to the
			    // Looper that is attached to the current thread
			    // You don't need to specify the Looper explicitly
			    handler = new Handler();
			    
				handler.postDelayed(scheduledRun, windowSize);
			    
			    // After the following line the thread will start
			    // running the message loop and will not normally
			    // exit the loop unless a problem happens or you
			    // quit() the looper (see below)
			    Looper.loop();
			  } catch (Throwable t) {
			    Log.e(TAG, "halted due to an error");
			  }
		}
	};
	
	/**
	 * the runnable that is used to reset the buffer whenever the timer triggers
	 * it
	 */
	private Runnable scheduledRun = new Runnable() {
		public void run() {
			synchronized(INSTANCE) {   // makes sure there is no buffer copying when the buffer is being updated.

				if (bufferIndex == startIndex) {
					Log.d(TAG, "Empty buffer being sent by " + ID);
				}
				
				
//				if (bufferIndex != startIndex) {
					int[] tmpBuffer = new int[bufferIndex];
					long[] tmpTimestamps = new long[bufferIndex];
					if (Log.DEBUG) Log.d(TAG, " active Runnable: " + this.toString());
					
					System.arraycopy(buffer, 0, tmpBuffer, 0, bufferIndex);
					System.arraycopy(timestamps, 0, tmpTimestamps, 0, bufferIndex);
		
					sendBuffer(tmpBuffer, tmpTimestamps,0,bufferIndex);
					bufferIndex = 0;
//				}
//				else {
//					Log.d(TAG, "Scheduled Buffer Empty - not sending");
//				}

					
				if (active) {
					// handler.removeCallbacks(this);
					// handler.postDelayed(this, 3000);
					handler.postAtTime(this, SystemClock.uptimeMillis() + windowSize);
				}
			}
		}
	};

	private long lastTime;
	protected Handler handler;

	/**
	 * this can be called if the other parameters are not yet known!
	 * 
	 * @param SensorID
	 */
	public AbstractSensor(int SensorID) {
		ID = SensorID;
		INSTANCE = this;
		
		Log.d("AbstractSensor - " + Constants.getSensorDescription(ID), "Created");
	}

	/**
	 * Constructor - should not be used anymore, as a call to in
	 * 
	 * @param SensorID
	 *            The ID of this Sensor, defined in
	 *            {@link org.fieldstream.Constants}
	 * @param windowLength
	 *            the length of a window
	 */

	@Deprecated
	protected AbstractSensor(int SensorID, Boolean scheduler, int windowLength,
			int slidingWindowStep) {
		ID = SensorID;
		initalize(scheduler, windowLength, slidingWindowStep);

	}

	/**
	 * Initializes the Sensor
	 * @param scheduler true if this should be running with a scheduler (every x seconds)
	 * @param windowLength the length of samples in one window / block
	 * @param slidingWindowStep the number of samples to step between sliding windows
	 */
	protected void initalize(Boolean scheduler, int windowLength,
			int slidingWindowStep) {

		this.scheduler = scheduler;
		this.slidingWindowStep = slidingWindowStep;
		if (scheduler) {
			// windowLength/1000:=No. of Seconds
			int length = (windowLength / 1000) * SCHEDULER_SIZE_FACTOR;
			buffer = new int[length];
			timestamps = new long[length];
		} else {
			buffer = new int[windowLength];
			timestamps = new long[windowLength];
		}
		windowSize = windowLength;
		if (this.scheduler) {
			setUpTimer();
		}
	}

	protected void finalize() {
		Log.d("AbstractSensor - " + Constants.getSensorDescription(ID), "Garbage Collected");
	}
	
	/**
	 * forwards a window of data to the FeatureCalculation class and also logs the new window. 
	 * @param toSendSamples window of samples to be processed as new samples
	 * @param toSendTimestamps timestamps for the samples to be processed
	 * @param startNewData startindex of the new samples in this window in the case of overlapping windows. For nonoverlaping windows, this will always be 0 
	 * @param endNewData stopindex of new samples in this window in the case of overlapping windows. For non-overlapping windows, this will always be the window size.
	 */
	protected void sendBuffer(int[] toSendSamples, long[] toSendTimestamps,int startNewData, int endNewData) {
		long currentTime = SystemClock.uptimeMillis();
		//		Log.d(TAG, "Send out Buffer, length:"
		//				+ Integer.toString(toSendSamples.length) + " at time "
		//				+ (currentTime - lastTime) + " with " + lastTime
		//				+ " and current " + currentTime);
		setLastTime(currentTime);

		// send it for feature calculation 
		// TODO reimplement FeatureCalculation as SensorBusSubscriber
		// FeatureCalculation.INSTANCE.receiveBuffer(ID, toSendSamples,toSendTimestamps);
		//		long middletime = SystemClock.uptimeMillis();

		// send it for any subscribers
		SensorBus.getInstance().receiveBuffer(ID, toSendSamples, toSendTimestamps, startNewData, endNewData);
		if (Log.DEBUG) Log.d(TAG,"time from the beginnging of sendBuffer: "+(SystemClock.uptimeMillis()-currentTime)  );
		if (Log.DEBUG) Log.d(TAG,"SensorID: "+ID );
		
	}

	

	
	protected void setUpTimer() {		
		if (scheduler) {
			thread.start();
		}
	}

	/**
	 * new raw sensors values that should be added to the current buffer.
	 * Synchronized so that only one can be active at any time
	 * 
	 * @param input
	 *            a single new Value for thi   s sensor
	 */
	public synchronized void addValue(int input, long timestamp) {
		timestamps[bufferIndex] = timestamp;
		buffer[bufferIndex++] = input;

		bufferIndex = bufferIndex % windowSize;
		// as we use sliding window, the buffer will be advanced
		if (!scheduler && bufferIndex == startIndex) {
			resetBuffer();
		}

	}

	private synchronized void resetBuffer() {
		// if this holds true, we need to empty the buffer!
		int[] tmpBuffer = new int[windowSize];
		long[] tmpTimestamps = new long[windowSize];

		// TODO not very robust, might well crash if the length and buffer index
		// do not align perfectly
		// syntax of arraycopy fromObj, int srcPos, Object dest, int destPos,
		// int lengt
		System.arraycopy(buffer, startIndex, tmpBuffer, 0, windowSize
				- startIndex);
		System.arraycopy(timestamps, startIndex, tmpTimestamps, 0, windowSize
				- startIndex);
		//		Log.d(TAG, ((Integer) startIndex).toString()
		//				+ " : startIndex bufferIndex:"
		//				+ ((Integer) bufferIndex).toString());
		if (startIndex != 0) {
			// this means we have to copy from the startIndex until the end of
			// the buffer, and from 0 until the end
			System.arraycopy(buffer, 0, tmpBuffer, windowSize - startIndex - 1,
					startIndex - 1);
			System.arraycopy(timestamps, 0, tmpTimestamps, windowSize
					- startIndex - 1, startIndex - 1);
		}
		sendBuffer(tmpBuffer, tmpTimestamps,windowSize-slidingWindowStep, windowSize);
		// we hit a sliding window, advancing the startIndex the window!
		startIndex = (startIndex + slidingWindowStep) % windowSize;

	}

	// TODO confirm this code works when we start using sliding windows again
	// TODO make sure this works with the scheduler too.
	private synchronized void addValueSlidingWindows(int[] input, long timestamps[]) {
		assert(true);  // we should not end up here, since we're 
					   // not using sliding windows right now
		
		int copied = 0;				
		// while the input left to copy will overflow the windowStep
		while (input.length - copied >= slidingWindowStep) {
			int canAdd = slidingWindowStep;
			// if the input left to copy will overflow the buffer
			if (bufferIndex + canAdd > windowSize) {
				// copy until the end of the buffer
				canAdd = windowSize - bufferIndex;
				System.arraycopy(input, copied, buffer, bufferIndex, canAdd);
				System.arraycopy(timestamps, copied, this.timestamps, bufferIndex, canAdd);
				copied += canAdd;
				canAdd = slidingWindowStep - windowSize + bufferIndex;
				bufferIndex = 0;				
			}

			// copy whatever else we can add from the input to the window. 
			// this will complete the window
			System.arraycopy(input, copied, buffer, bufferIndex, canAdd);
			System.arraycopy(timestamps, copied, this.timestamps, bufferIndex, canAdd);
			copied += canAdd;

			// reset and send the window, unless we're doing scheduled resets
			// in that case, the scheduler will decide when to send the data
			//if (!scheduler)
				resetBuffer();
		}
		
		// if there is something left to copy
		if (input.length < slidingWindowStep) {
			int canAdd = input.length;
			// if copying will overflow the buffer
			if (bufferIndex + canAdd > windowSize) {
				// copy until the end of the buffer
				canAdd = windowSize - bufferIndex;
				System.arraycopy(input, 0, buffer, bufferIndex, canAdd);
				System.arraycopy(timestamps, 0, this.timestamps, bufferIndex, canAdd);
				copied += canAdd;
				canAdd = slidingWindowStep - windowSize + bufferIndex;
				bufferIndex = 0;
			}

			// copy whatever is left in the input to the window. 
			System.arraycopy(input, copied, buffer, bufferIndex, canAdd);
			System.arraycopy(timestamps, copied, this.timestamps, bufferIndex, canAdd);
			bufferIndex += canAdd;
		}

	}

	// TODO make sure this works with the scheduler too.
	private synchronized void addValueStaticWindows(int[] input, long timestamps[]) {
		int copied = 0;
		// while the input left to copy will overflow the window
		while (input.length - copied >= windowSize - bufferIndex) {
//			Log.d("AbstractSensor", "bufferIndex = " + bufferIndex);
			// copy until we fill the window
			int canAdd = windowSize - bufferIndex;
			System.arraycopy(input, copied, buffer, bufferIndex, canAdd);
			System.arraycopy(timestamps, copied, this.timestamps, bufferIndex, canAdd);							
			bufferIndex = 0;
//			Log.d("AbstractSensor", "Buffer filled");
			copied += canAdd;
			
			// reset and send the window, unless we're doing scheduled resets
			// in that case, the scheduler will decide when to send the data
			//if (!scheduler)
				resetBuffer();
		}
		
		if (input.length - copied < windowSize-bufferIndex) {
			// copy anything that's left 
//			Log.d("AbstractSensor", "bufferIndex = " + bufferIndex);
			int canAdd = input.length-copied;
			System.arraycopy(input, copied, buffer, bufferIndex, canAdd);
			System.arraycopy(timestamps, copied, this.timestamps, bufferIndex, canAdd);
			bufferIndex += canAdd;
//			Log.d("AbstractSensor", "bufferIndex = " + bufferIndex);
		}
	}

	/**
	 * new raw sensors values that should be added to the current buffer.
	 * 
	 * @param input
	 *            a array of new values
	 */
	public synchronized void addValue(int[] input, long timestamps[]) {
		if (slidingWindowStep == windowSize) {
			// static windows
			addValueStaticWindows(input, timestamps);
		}
		else {   
			// sliding windows
			addValueSlidingWindows(input, timestamps);
		}
	}
	
//	public synchronized void addValue(int[] input, long timestamps[]) {
//		// added by andrew - comment out
//		//		if (this.ID == Constants.SENSOR_VIRTUAL_INHALATION) {
//		//			String buf="RPVInput =";
//		//			for (int i=0; i<input.length; i++) {
//		//				buf += " " + input[i];
//		//			}
//		//			Log.d("InhalationBuffer", buf);
//		//		}		
//
//		long starttime = SystemClock.uptimeMillis();
//		// if (!scheduler)
//		Log.d("AbstractSensor","startIndex "+startIndex + " bufferIndex "+bufferIndex );
//		int canAdd;
//		if ((startIndex== 0 &&  bufferIndex + input.length>windowSize) || (startIndex> 0 && bufferIndex + input.length > startIndex)) {
//			// in this case, we can not add the complete array, it would overrun
//			// the buffer!
//			// the max amount we can add
//			if (startIndex==0) { 
//				canAdd = windowSize - bufferIndex;
//			} else {
//				if (bufferIndex>startIndex) {
//					bufferIndex=startIndex;
//				}
//				canAdd = startIndex - bufferIndex;
//			}
//
//		} else {
//			canAdd = input.length;
//		}
//
//		//		Log.d(TAG,"bufferIndex: "+bufferIndex +" canAdd:" + canAdd + "size of input : "+ input.length + " startindex "+ startIndex );
//		System.arraycopy(input, 0, buffer, bufferIndex, canAdd);
//		System.arraycopy(timestamps, 0, this.timestamps, bufferIndex, canAdd);		
//		bufferIndex = (bufferIndex+canAdd)%windowSize;
//		// added by andrew - comment out
//		//		if (this.ID == Constants.SENSOR_VIRTUAL_INHALATION) {
//		//			String buf="InhalationBuffer =";
//		//			for (int i=0; i<bufferIndex; i++) {
//		//				buf += " " + buffer[i];
//		//			}
//		//			Log.d("InhalationBuffer", buf);
//		//		}
//
//		if (!scheduler && canAdd != input.length || startIndex==bufferIndex) {
//
//			// in this case, we know we have to reset the array!
//			resetBuffer();
//			// added by andrew - comment out
//			//			if (this.ID == Constants.SENSOR_VIRTUAL_INHALATION) {
//			//				Log.d("InhalationBuffer", "buffer sent and cleared");				
//			//			}
//
//			Log.d("AbstractSensor","input length "+input.length+" canAdd "+canAdd + " bufferIndex "+bufferIndex+ " WindowSize "+windowSize   );
//			if ((input.length-canAdd)>windowSize) {
//				// there is more data left in the input buffer than we can fit in the window
//				System.arraycopy(input, canAdd, buffer, bufferIndex, windowSize);
//				System.arraycopy(timestamps, canAdd, this.timestamps, bufferIndex, windowSize);				
//				bufferIndex=(bufferIndex+windowSize)%windowSize;
//			}else {
//				// we can fit all the data in the input buffer in the window
//				// AND 
//				if (((input.length-canAdd)+bufferIndex)<=windowSize) {
//					System.arraycopy(input, canAdd, buffer, bufferIndex, input.length-canAdd);
//					System.arraycopy(timestamps, canAdd, this.timestamps, bufferIndex, input.length-canAdd);
//
//				} else {
//					// TODO: this causes an error and seems wrong, fix it!
//					System.arraycopy(input, canAdd, buffer, bufferIndex, windowSize-bufferIndex);
//					System.arraycopy(timestamps, canAdd, this.timestamps, bufferIndex, windowSize-bufferIndex);					
//					// no clue really, 
//				}
//				bufferIndex=(bufferIndex+(input.length-canAdd))%windowSize;
//			}
//
//			// added by andrew - comment out
//			//			if (this.ID == Constants.SENSOR_VIRTUAL_INHALATION) {
//			//				String buf="InhalationBuffer =";
//			//				for (int i=0; i<bufferIndex; i++) {
//			//					buf += " " + buffer[i];
//			//				}
//			//				Log.d("InhalationBuffer", buf);
//			//			}
//
//			//			if (bufferIndex==startIndex) 
//			//				bufferIndex++;
//		}
//
//		// }
//
//		// throw new IllegalArgumentException("Not Yet Implemented");
//		//		Log.d(TAG,"time it took to reset " + (SystemClock.uptimeMillis()-starttime)+ " and time until the middle was reached: "+(middletime-starttime));
//	}

	/**
	 * activate this sensor
	 */
	public abstract void activate();

	/**
	 * deactivate this sensors
	 */
	public abstract void deactivate();

	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}

	public long getLastTime() {
		return lastTime;
	}
}
