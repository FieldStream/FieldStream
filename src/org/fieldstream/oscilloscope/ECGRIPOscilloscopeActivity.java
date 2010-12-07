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

import org.fieldstream.Constants;
import org.fieldstream.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Spinner;
import android.opengl.GLSurfaceView;

public class ECGRIPOscilloscopeActivity extends Activity {
	
	private static String TAG = "OscilloscopeActivity";
	
	private OscilloscopeRenderer renderer;
	private GLSurfaceView oscopeView;
	
	WakeLock wakelock;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);  
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
		                                 WindowManager.LayoutParams.FLAG_FULLSCREEN);  

		setContentView(R.layout.oscilloscope_layout);

		PowerManager pm = (PowerManager)this.getSystemService(POWER_SERVICE);
		wakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "OscilloscopeActivity");
		//wakelock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "OscilloscopeActivity");
		
		initSpinner();
		initRenderer();		
	}

	// don't need the spinner
	private void initSpinner() {
		Spinner signalSpinner = (Spinner) findViewById(R.id.OscilloscopeSpinner);
		signalSpinner.setVisibility(View.GONE);
	}
	
	private void initRenderer() {
		oscopeView = (GLSurfaceView) findViewById(R.id.OscilloscopeView);	

		renderer = new OscilloscopeRenderer();

		// add the ecg signal
		SignalRenderer sr = new SignalRenderer();
		sr.setColor(1, 0, 0, 1);
		sr.setSignal(Constants.SENSOR_ECK);
		sr.setSignalLabel(Constants.getId(Constants.FEATURE_HR, Constants.SENSOR_VIRTUAL_RR));
		renderer.addSignal(sr);

		// add the rip signal
		sr = new SignalRenderer();
		sr.setColor(0, 0, 1, 1);
		sr.setSignal(Constants.SENSOR_RIP);
		sr.setSignalLabel(Constants.getId(Constants.FEATURE_RESP_RATE, Constants.SENSOR_VIRTUAL_INHALATION));
		renderer.addSignal(sr);
		
		oscopeView.setRenderer(renderer);	
	}
	
	
    @Override
    protected void onPause() { 
        super.onPause();        
    	wakelock.release();
        oscopeView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
		wakelock.acquire();
		oscopeView.onResume();
    }


	
//	/* This is called when the app is killed. */
//	@Override
//	protected void onDestroy() {
//		super.onDestroy();
//	}
	
	/* END ANDROID LIFE CYCLE */

	
}
