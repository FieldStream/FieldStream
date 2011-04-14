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

//@author Andrew Raij

package org.fieldstream.oscilloscope;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;
import java.util.Arrays;

import javax.microedition.khronos.opengles.GL10;

import org.fieldstream.Constants;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensor.FeatureBus;
import org.fieldstream.service.sensor.FeatureBusSubscriber;
import org.fieldstream.service.sensors.mote.sensors.MoteSensorManager;
import org.fieldstream.service.sensors.mote.sensors.MoteUpdateSubscriber;

import android.graphics.Paint;


public class SignalRenderer implements MoteUpdateSubscriber, FeatureBusSubscriber{

	private static String TAG = "SignalRenderer";
	
	float red=0.0f, green=1.0f, blue=0.0f, alpha=1.0f;
	
    private float min = 100000, max = -100000;
    private int numSamplesAdded;
    
    private static final int VERTEX_BUFFER_SIZE = 512;

    // maintain two rotating vertex buffers
    // when one is full, we can immediately start recording samples
    // without throwing away existing samples (which will still need to be drawn)
	FloatBuffer firstVertexBuffer = null;
	FloatBuffer secondVertexBuffer = null;
	private FloatBuffer vertexBuffers[] = {null, null};

	Boolean started = false;
	
	private int signalID = -1;

	// stuff for drawing labels
	private int labelSignalID = -1;
	private LabelMaker labelMaker = null;
	private int labelID = -1;
	Paint paint = null;
	boolean labelChanged = true;
	double labelValue = Double.NaN;   
	
	int width = 0, height = 0;
	
	
	SignalRenderer() {
		paint = new Paint();
        paint.setTextSize(32);
        paint.setAntiAlias(true);
        paint.setARGB(255, 255, 255, 255);
//        paint.setTextAlign(Paint.Align.CENTER);
	}
	
	public void draw(GL10 gl) {
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		
		drawLabel(gl);
		
		drawSignal(gl);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPopMatrix();
		
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
	}
	
	public void drawLabel(GL10 gl) {
		if (labelChanged) {
			updateLabel(gl);
			labelChanged = false;
		}

        labelMaker.beginDrawing(gl, width, height);
        labelMaker.draw(gl, labelMaker.getHeight(labelID) + 32, 0, labelID, 90);                
        labelMaker.endDrawing(gl);
	}

	
	
	public void drawSignal(GL10 gl) {

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPushMatrix();
		setProjectionMatrix(gl);

		synchronized(lock) {			
			
			gl.glColor4f(red, green, blue, alpha);
			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);		
			
			// draw the first buffer
			if (firstVertexBuffer.position() != 0) {
				int savedPosition = firstVertexBuffer.position();
				firstVertexBuffer.position(0);
				
				gl.glVertexPointer(3, GL10.GL_FLOAT, 0, firstVertexBuffer); 

				gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, savedPosition / 3);
				gl.glFinish();
				firstVertexBuffer.position(savedPosition);
			}	
			
			// draw the second buffer
			if (secondVertexBuffer.position() != 0) {
				int savedPosition = secondVertexBuffer.position();
				secondVertexBuffer.position(0);
				
				gl.glVertexPointer(3, GL10.GL_FLOAT, 0, secondVertexBuffer); 
				gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, savedPosition / 3);
				gl.glFinish();
				
				secondVertexBuffer.position(savedPosition);
			}
			
			gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		}
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPopMatrix();
		
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
	}

	
	private void setProjectionMatrix(GL10 gl) {		
		// setup camera
		gl.glLoadIdentity();

		// use the screen dimension with more pixels for the time axis
		float bottom, top, right, left;

		bottom = 0;
		if (numSamplesAdded > VERTEX_BUFFER_SIZE) {
			bottom = numSamplesAdded - VERTEX_BUFFER_SIZE;
		}

		top = bottom + VERTEX_BUFFER_SIZE;
		right = min > 0 ? min * .95f : -1;
		left = max > 0 ? max * 1.05f : 1;
		
//		else {
//			left = 0;
//			if (numSamplesAdded > pixelWidth) {
//				left = numSamplesAdded - pixelWidth;
//			}
//	
//			right = left + pixelWidth;
//			top = min > 0 ? min * .95f : -1;
//			bottom = max > 0 ? max * 1.05f : 1;			
//		}
		
		//Log.d(TAG, "camera params: " + left + ", " + right + ", " + bottom +  ", " + top);
		gl.glOrthof(left, right, bottom, top, 0.0f, 10.0f);		
	}
	
	
	void updateLabel(GL10 gl) {
		if (labelMaker != null) {
			labelMaker.shutdown(gl);
		}		

        String text;
        if (Constants.isSensor(labelSignalID)) {
            text = Constants.getSensorDescription(labelSignalID) + ": ";
        	text += Double.isNaN(labelValue) ? "NA" : labelValue;       	
        }
        else {        	
			text = Constants.getFeatureDescription(Constants.parseFeatureId(labelSignalID)) + ": ";
			text += Double.isNaN(labelValue) ? "NA" : new DecimalFormat("#0.0").format(labelValue);
        }
		
        int height = roundUpPower2((int) paint.getFontSpacing());
        final float interDigitGaps = (text.length() - 1) * 1.0f;
        int width = roundUpPower2((int) (interDigitGaps + paint.measureText(text)));
        
        labelMaker = new LabelMaker(true, width, height);
        labelMaker.initialize(gl);
      
        labelMaker.beginAdding(gl);
        labelID = labelMaker.add(gl, text, paint);
        labelMaker.endAdding(gl);
        
		Log.d(TAG, "updated label to " + labelValue);
	}
	
    private int roundUpPower2(int x) {
        x = x - 1;
        x = x | (x >> 1);
        x = x | (x >> 2);
        x = x | (x >> 4);
        x = x | (x >> 8);
        x = x | (x >>16);
        return x + 1;
    }

	
	public synchronized void setSignal(int id) {
		stop();
		this.signalID = id;
		
		if (Constants.isSensor(id)) {
			Log.d(TAG, "Rendering sensor " + id + ": " + Constants.getSensorDescription(id));
		}
		else {

			Log.d(TAG, "Rendering feature " + id + ": " + Constants.getFeatureDescription(Constants.parseFeatureId(id)));
		}
		
		reset();
		start();
	}

	public synchronized void setSignalLabel(int id) {
		this.labelSignalID = id;
		labelChanged = true;
		labelValue = Double.NaN;
	}
	
	
	public void setColor(float red, float green, float blue, float alpha) {

		if (paint !=null) {
			int a = (int)(alpha * 255);
			int r = (int)(red * 255);
			int g = (int)(green * 255);
			int b = (int)(blue * 255);
			paint.setARGB(a,r,g,b);
		}
		
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
		
		labelChanged = true;
	}		
	
	private void stop() {
		if (!started)
			return;
		
		MoteSensorManager.getInstance().unregisterListener(this);
		FeatureBus.getInstance().unsubscribe(this);
		
		synchronized(lock) {
			runner.active=false;
			dataCopyThread=null;
			lock.notify();
		}		
		
		started = false;
	}
	
	private void start() {
		if (started)
			return;

		MoteSensorManager.getInstance().registerListener(this);
		FeatureBus.getInstance().subscribe(this);
		
		synchronized(lock) {
			runner.active = true;
			dataCopyThread = new Thread(runner);
			dataCopyThread.start();
		}
	
		started = true;
	}
	
	private void reset() {				
		synchronized (lock) {
			Log.d(TAG, "resetting buffers");
			runner.sensorBuffer = null;
			runner.featureBuffer = null;
			min =  100000;     // sensor values should never be near this range
			max = -100000;
			Log.d(TAG, " reset -- min = " + min + ", max = " + max);
			
			numSamplesAdded = 0;

    		// 4 bytes in a float * 3 elements per vertex
    		// set the number of vertices to the screen size 
			int bufferSize = 4 * 3 * VERTEX_BUFFER_SIZE;
			Log.d(TAG, "buffer size set to " + bufferSize);
			
		    for (int i=0; i < vertexBuffers.length; i++) {
		    	if (vertexBuffers[i] == null) {	    		
		    		ByteBuffer vbb = ByteBuffer.allocateDirect(bufferSize);
		    		vbb.order(ByteOrder.nativeOrder());	    
					Log.d(TAG, "initialized byte buffer of size " + vbb.capacity());
		    		vertexBuffers[i] = vbb.asFloatBuffer();
					Log.d(TAG, "initialized float buffer of size " + vertexBuffers[i].capacity());
		    	}
		    	vertexBuffers[i].clear();
		    }
		    
		    firstVertexBuffer = vertexBuffers[0];
		    secondVertexBuffer = vertexBuffers[1];		    
		}
	}

	
	public synchronized void onReceiveData(int sensorID, int[] data, long[] timestamp, int lastSampleNumber) {
		if (!started)
			return; 

		if (this.labelSignalID == sensorID) {
			this.labelValue = data[0];
			this.labelChanged = true;
		}		
		
		if (this.signalID == sensorID) {
			synchronized (lock) {
				runner.sensorBuffer=data;
				lock.notify();	
			}			
		}
	
	}

	
	private final static int FEATURE_BUFFER_SIZE = 50;
	public synchronized void receiveUpdate(int featureID, double result, long beginTime,
			long endTime) {
		if (!started)
			return;

		
		if (this.labelSignalID == featureID) {
			this.labelValue = result;
			this.labelChanged = true;
		}
		
		if (this.signalID == featureID) {
			synchronized (lock) {
				runner.featureBuffer=new float[FEATURE_BUFFER_SIZE];
				Arrays.fill(runner.featureBuffer, (float)result);
				lock.notify();	
			}			
		}		
	}
		
	
	private DataCopyRunner runner = new DataCopyRunner(); 
	private Thread dataCopyThread;	
	private Object lock = new Object();

	class DataCopyRunner implements Runnable {

		public boolean active=true;
		public int[] sensorBuffer = null;
		public float[] featureBuffer = null;
		
		public void run() {
			while (active) {
				synchronized (lock) {
					if ((sensorBuffer!=null || featureBuffer != null) && firstVertexBuffer != null && secondVertexBuffer != null ) {
						updateMaxMin();
						addBuffer();
						sensorBuffer = null;
						featureBuffer = null;
					}

					try {
						if (active)
							lock.wait();
					} 
					catch (InterruptedException e) {

					}
				}
			}
		}
		
		private synchronized void updateMaxMin() {
			if (sensorBuffer != null) {
				for (int i=0; i < sensorBuffer.length; i++) {
					
					if (sensorBuffer[i] >= 4095) 
						continue;
					
					if (sensorBuffer[i] < min)
						min = sensorBuffer[i];
					
					if (sensorBuffer[i] > max) 
						max = sensorBuffer[i];					
				}
			}
			else {
				for (int i=0; i < featureBuffer.length; i++) {
					if (featureBuffer[i] < min)
						min = featureBuffer[i];
					
					if (featureBuffer[i] > max) 
						max = featureBuffer[i];					
				}				
			}
			Log.d(TAG, "min = " + min + ", max = " + max);
		}
		
		private void addBuffer() {
			assert(sensorBuffer != null || featureBuffer != null);
			int length = sensorBuffer != null ? sensorBuffer.length : featureBuffer.length;
			assert(length < firstVertexBuffer.capacity() && length < secondVertexBuffer.capacity());
						
			int start = 0;
			int end = length;
			
			start += tryCopyToBuffer(firstVertexBuffer, start, end);
			if (start != end) {
				start += tryCopyToBuffer(secondVertexBuffer, start, end);
				if (start != end) {
					Log.d(TAG, "swapped first and second vertex buffers");
					FloatBuffer temp = firstVertexBuffer;
					firstVertexBuffer = secondVertexBuffer;
					secondVertexBuffer = temp;
					secondVertexBuffer.clear();
					
					start += tryCopyToBuffer(secondVertexBuffer, start, end);
				}
			}
			
			Log.d(TAG, "num samples = " + numSamplesAdded);
		}

		private int tryCopyToBuffer(FloatBuffer floatBuffer, int start, int end) {
			if (sensorBuffer != null) {
				return tryCopyToBufferInt(floatBuffer, start, end);
			}
			else {
				return tryCopyToBufferFloat(floatBuffer, start, end);
			}	
		}
		
		private int tryCopyToBufferInt(FloatBuffer floatBuffer, int start, int end) {
			int i = start;
			while (floatBuffer.position() < floatBuffer.capacity() && i < end) {
				// this should only happen after we reset the buffer
				floatBuffer.put(sensorBuffer[i]);
				floatBuffer.put(numSamplesAdded);
				floatBuffer.put(-1);
				
				i++;
				numSamplesAdded++;
			}
						
			int numCopied = i - start;
			return numCopied;
		}		
		
		
		private int tryCopyToBufferFloat(FloatBuffer floatBuffer, int start, int end) {
			int i = start;
			while (floatBuffer.position() < floatBuffer.capacity() && i < end) {
				// this should only happen after we reset the buffer
				floatBuffer.put(this.featureBuffer[i]);
				floatBuffer.put(numSamplesAdded);
				floatBuffer.put(-1);
				
				i++;
				numSamplesAdded++;
			}
						
			int numCopied = i - start;
			return numCopied;
		}		

		
	}

	public void setPixelSize(int pixelWidth, int pixelHeight) {
		width = pixelWidth;
		height = pixelHeight;	
		labelChanged = true;
	}

	
}
