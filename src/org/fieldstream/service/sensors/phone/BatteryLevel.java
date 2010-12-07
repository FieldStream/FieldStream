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

// @author Andrew Raij

package org.fieldstream.service.sensors.phone;

import org.fieldstream.service.InferrenceService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class BatteryLevel {
	
	static BatteryLevel INSTANCE = null;
	
	public int batteryLevel;
	
	BroadcastReceiver batteryLevelReceiver;
	
	public BatteryLevelSensor batteryLevelSensor;
	
	static BatteryLevel getInstance(){
		if(INSTANCE == null)
			INSTANCE = new BatteryLevel();
		return INSTANCE;
	}
	
	public BatteryLevel()
	{
		batteryLevel = -1;
		batteryLevelReceiver = null;
	}
	
	/**
     * Computes the battery level by registering a receiver to the intent triggered 
     * by a battery status/level change.
     */
    public void activate() {
        INSTANCE.batteryLevelReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
//                context.unregisterReceiver(this);
                int rawlevel = intent.getIntExtra("level", -1);
                int scale = intent.getIntExtra("scale", -1);
                int level = -1;
                if (rawlevel >= 0 && scale > 0) {
                    level = (rawlevel * 100) / scale;
                }
                INSTANCE.batteryLevel = level;
                
                batteryLevelSensor.addValue(level, System.currentTimeMillis());
            }
        };
        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        InferrenceService.INSTANCE.registerReceiver(INSTANCE.batteryLevelReceiver, batteryLevelFilter);
        active = true;
    }
    
    public void deactivate() {
    	if(INSTANCE.batteryLevelReceiver != null)
    		 InferrenceService.INSTANCE.unregisterReceiver(INSTANCE.batteryLevelReceiver);
    	
    	active = false;
       }

    
    private boolean active = false;
    public boolean isActive() {
    	return active;
    }
}
