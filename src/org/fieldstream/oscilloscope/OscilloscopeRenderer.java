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

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.fieldstream.service.logger.Log;

import android.graphics.Paint;
import android.opengl.GLSurfaceView.Renderer;

public class OscilloscopeRenderer implements Renderer {

	private static String TAG="OscilloscopeRenderer";
	private ArrayList<SignalRenderer> signalRenderers;
	private int pixelWidth;
	private int pixelHeight;

	int newWidth = 0;	
	
	OscilloscopeRenderer() {
		super();	
		signalRenderers = new ArrayList<SignalRenderer>();
	}
	
	
	
	void addSignal(SignalRenderer sr) {
		signalRenderers.add(sr);
		
		resetWindowWidths();
	}
	
	void removeSignal(SignalRenderer sr) {
		signalRenderers.remove(sr);
		
		resetWindowWidths();
	}
		
	public void onDrawFrame(GL10 gl) {

		// clear the screen
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		// set up the camera
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();

		// apply transforms to each signal model
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		
		
		
		int xPos = 0;

		for (SignalRenderer sr : signalRenderers) {
		    gl.glViewport(xPos, 0, newWidth, pixelHeight);  		    
		    sr.draw(gl);
			xPos += newWidth;
		}
	}


	private void resetWindowWidths() {
	 	float dx = 1/(float)signalRenderers.size();
		newWidth = (int)(dx * pixelWidth);
		
		for (SignalRenderer r : signalRenderers) {
			r.setPixelSize(newWidth, pixelHeight);
		}
	}
	
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		Log.d(TAG, "adusting for screen resolution (" + width + ", " + height + ")");
	     pixelWidth = width;
	     pixelHeight = height;
	    
	     resetWindowWidths();
	}
	
	public void onSurfaceCreated(GL10 gl, EGLConfig glConfig) {
		
		gl.glClearColor(0, 0, 0, 1);

		// this doesn't seem to do anything!
		gl.glLineWidth(4.0f);
		gl.glEnable(GL10.GL_LINE_SMOOTH);
		gl.glHint(GL10.GL_LINE_SMOOTH_HINT, GL10.GL_NICEST);
	}


	

	

	
	
	
}
