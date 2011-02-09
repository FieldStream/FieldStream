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

//@author Somnath Mitra

package org.fieldstream.service.sensors.phone;


import java.io.File;

import org.fieldstream.Constants;
import org.fieldstream.service.InferrenceService;
import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.SimpleFileLogger;
import org.fieldstream.service.logger.TextFileLogger;
import org.fieldstream.service.sensors.api.AbstractSensor;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Environment;

/**
 * Wrapper class that manages the communication with the phone gps. has the actual sensors (instances of {@link AbstractSensor}) as inner classes.
 * <br> Observe the odd 'N' in LocatioN to avoid conflict with android.location.Location </br>
 * @author mitra
 *
 */

public class LocatioN {
	
	// This MULTIPLIER CAUSES loss of GPS Precision
	// as Java does not allow very large numbers for constants
	public static long MULTIPLIER = 10000000;
	
	public static final int SPEED_MULTIPLIER = 100;
	
	protected LocationSensor locationLatitude ;
	protected LocationSensor locationLongitude ;
	protected LocationSensor locationSpeed ;
			
	private LocationManager mLocationManager;	
	private long MINTIME = 1000;
	private float MINDISTANCE = 0.0f;
	
	protected int active = 0;
	static LocatioN INSTANCE;
	
	static LocatioN getInstance() {
		if (INSTANCE==null) {
			INSTANCE = new LocatioN();
		} 
		return INSTANCE;
	}
	protected LocatioN() 
	{
		
	}
	
	public void activateSensor() {
		mLocationManager = (LocationManager) InferrenceService.INSTANCE.getSystemService(Context.LOCATION_SERVICE);
		// Register the gps based location service
		mLocationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER,
				MINTIME,
				MINDISTANCE,
				this.mLocationListeners[0]);
	/*	// Register the network based location service
		mLocationManager.requestLocationUpdates (
		 LocationManager.NETWORK_PROVIDER,
		 1000,
		 0F,
		 this.mLocationListeners[1]);*/
	}

	public void deactivateSensor() 
	{	
		if (this.mLocationManager != null)
		{
			for (int i = 0; i < this.mLocationListeners.length; i++)
			{
				try
				{
				this.mLocationManager.removeUpdates(mLocationListeners[i]);
				}
				catch (Exception ex)
				{
				
				}
			}	
		}
	}
	
	// Adding the following 3 methods to be more precise in some applications
	public double getCurrentLatitude()
	{
		double latitude = -1.0f;
		if( this.mLocationListeners[0].mValid != false)
			latitude = this.mLocationListeners[0].getLastLatitude();
		return latitude;		
	} 
	
	public double getCurrentLongitude()
	{
		double longitude = -1.0f;
		if( this.mLocationListeners[0].mValid != false)
			longitude = this.mLocationListeners[0].getLastLongitude();
		return longitude;		
	} 
	
	public double getCurrentSpeed ()
	{
		double speed = -1.0f;
		if( this.mLocationListeners[0].mValid != false)
			speed = this.mLocationListeners[0].getLastSpeed();
		return speed;
	}

	LocationListener [] mLocationListeners = new LocationListener[]
  	{
  		new LocationListener(LocationManager.GPS_PROVIDER),
  		new LocationListener(LocationManager.NETWORK_PROVIDER)
  	};
	
	private class LocationListener implements android.location.LocationListener
	{
		Location mLastLocation;
		boolean mValid = false;
		String mProvider;
		SimpleFileLogger locationLog;

  				
		public LocationListener(String provider)
		{
		mProvider = provider;
		mLastLocation = new Location(mProvider);
		
  		File root = Environment.getExternalStorageDirectory();
  		String locationLogFileName = root + "/" + Constants.LOG_DIR + "/LOCATION_LOG";
		
		locationLog = new SimpleFileLogger(locationLogFileName+Long.toString(System.currentTimeMillis()));
		}
		
		public void logLocation(Location newLocation)
		{
			// clear the location string 
			String locationString = "";
			// get the lat
			locationString +=  Double.toString(newLocation.getLatitude()) + ",";
			// get the long
			locationString +=  Double.toString(newLocation.getLongitude()) + ",";
			// get the altitude
			locationString +=  Double.toString(newLocation.getAltitude()) + ",";
			// get the bearing
			locationString +=  Double.toString(newLocation.getBearing()) + ",";
			// get the speed
			locationString +=  Double.toString(newLocation.getSpeed()) + ",";
			// get the accuracy
			locationString +=  Double.toString(newLocation.getAccuracy()) + ",";
			// get the provider
			locationString +=  newLocation.getProvider() + ",";
			// get the time
			locationString +=  Long.toString(newLocation.getTime()) + ",";
			// append a newline
			locationString += "\n";
			// log to file
			locationLog.log(locationString);	
		}
		
		
		
		public double getLastLatitude()
		{
			double lastLatitude = -2.0f;
			lastLatitude = mLastLocation.getLatitude();
			return lastLatitude;
		}
		
		public double getLastLongitude()
		{
			double lastLongitude = -2.0f;
			lastLongitude = mLastLocation.getLongitude();
			return lastLongitude;
		}
		
		public double getLastSpeed()
		{
			double lastSpeed = -2.0f;
			lastSpeed = mLastLocation.getSpeed();
			return lastSpeed;
		}
		
		public void onLocationChanged(Location newLocation)
		{
			if (newLocation.getLatitude() == 0.0 && newLocation.getLongitude() == 0.0)
			{
			// Hack to filter out 0.0,0.0 locations
			return;
			}
			if (newLocation != null)
			{
				if(Constants.GPS_LOGGING)
					logLocation(newLocation); 
				long timeStamp = newLocation.getTime();
				if(locationLatitude != null)
					locationLatitude.addValue( (int) (newLocation.getLatitude() * MULTIPLIER) , timeStamp);
				if(locationLongitude != null)
					locationLongitude.addValue((int)(newLocation.getLongitude() * MULTIPLIER), timeStamp);
				if(locationSpeed != null)
					locationSpeed.addValue((int)(newLocation.getSpeed()* SPEED_MULTIPLIER ) , timeStamp);
			}
			mLastLocation.set(newLocation);
			mValid = true;
		}
		
		public void onProviderEnabled(String provider) 
		{
			
		}
		
		public void onProviderDisabled(String provider)
		{
			mValid = false;
		}
		
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
			if (status == LocationProvider.OUT_OF_SERVICE)
			{
			mValid = false;
			}
		}
		
		
	};
		
		
		
}
