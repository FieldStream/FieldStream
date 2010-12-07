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

//@author Patrick Blitz

package org.fieldstream.service.sensors.zephyr;

import java.util.ArrayList;

public class ZephyrManager {

	
	private static ZephyrManager INSTANCE;
	private ArrayList<ZephyrUpdateSubscriber> subscribers;
	private int i;
	
	private  ZephyrManager() {
		subscribers = new ArrayList<ZephyrUpdateSubscriber>();
		i = 0;
	}
	
	public static ZephyrManager getINSTANCE() {
		if (INSTANCE==null) 
			INSTANCE=new ZephyrManager();
		return INSTANCE;
	}
	
	
	public void register(ZephyrUpdateSubscriber subscriber) {
		if (!subscribers.contains(subscriber)) 
			subscribers.add(subscriber);
	}
	
	public void unregister(ZephyrUpdateSubscriber subscriber) {
		if (subscribers.contains(subscriber)) 
			subscribers.remove(subscriber);
	}
	
	public void newSensorData(int sensorID, int data) {
		for (i=0;i<subscribers.size();i++) {
			subscribers.get(i).receiveNewData(sensorID, data);
		}	
	}
	
	
}
