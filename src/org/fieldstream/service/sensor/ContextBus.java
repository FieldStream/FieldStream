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
package org.fieldstream.service.sensor;

import java.util.ArrayList;
import java.util.HashMap;

import org.fieldstream.Constants;
import org.fieldstream.service.InferrenceService;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensors.mote.sensors.MoteUpdateSubscriber;

import android.app.Notification;
import android.app.NotificationManager;

/**
 * @author Patrick Blitz
 * @author Andrew Raij
 */

/**
 * Singleton class that relays all Context update to the subscribers
 * @author blitz
 *
 */

public class ContextBus {

	private ArrayList<ContextSubscriber> subscribers;
	private HashMap<Integer,ArrayList<Integer>> lastContexts;
	private static ContextBus INSTANCE;

	private  NotificationManager nm;
	private boolean led=true;
	private Notification notif;

	
	public static ContextBus getInstance() {
		if (INSTANCE==null) {
			INSTANCE = new ContextBus();
		}
		return INSTANCE;
	}

	protected void finalize() {
		Log.d("ContextBus", "Garbage Collected");
	}

	private ContextBus() {
		subscribers = new ArrayList<ContextSubscriber>();
		lastContexts= new HashMap<Integer, ArrayList<Integer>>();
		
		notif = new Notification();
		notif.flags = Notification.FLAG_SHOW_LIGHTS;

	}

	public void subscribe(ContextSubscriber subscriber) {
		if (!subscribers.contains(subscriber)) {
			subscribers.add(subscriber);

		}

	}

	public void unsubscribe(ContextSubscriber subscriber) {
		if (subscribers.contains(subscriber)) {
			subscribers.remove(subscriber); 
		}
	}
	
	public ArrayList<Integer> getPreviousContexts(int modelID) {
		
	if (lastContexts.containsKey(modelID)) 
		return lastContexts.get(modelID);
	else 
		return null;
	} 

	
	
	private void blink() {
        if (led) {
       		notif.ledARGB = 0xFF00FF00;
        	notif.ledOnMS = 1;
        } else {
        	notif.ledARGB = 0x00000000;
        	notif.ledOnMS = 0;
        }
        led=!led;
        notif.ledOffMS = 0;
        if (nm == null) {
        	nm=( NotificationManager ) InferrenceService.INSTANCE.getSystemService( InferrenceService.NOTIFICATION_SERVICE );
        }
        //		nm.notify(1, notif);
	}
	
	public void pushNewContext(int modelID, int label, long startTime, long endTime) {
		if (Log.DEBUG) Log.d("ContextBus", Constants.getModelDescription(modelID) + " = " + label + " " + startTime);		
				
		blink();
		
		if (subscribers!= null) {
			for (int i=0;  i<subscribers.size(); i++) {
				subscribers.get(i).receiveContext(modelID, label, startTime, endTime);
			}
		}
		if (lastContexts.containsKey(modelID)) {
			ArrayList<Integer> lastContextList = lastContexts.get(modelID);
			if (lastContextList.size()>5) {
				lastContextList.remove(0);
			}
			lastContextList.add(label);
			lastContexts.put(modelID, lastContextList);
		} else {
			ArrayList<Integer> lastContextList = new ArrayList<Integer>();
			lastContextList.add(label);
			lastContexts.put(modelID, lastContextList);
		}
	}

}
