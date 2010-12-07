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

package org.fieldstream.service.internet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.fieldstream.service.logger.Log;


public class HTTPRequester extends Thread  {
	
	  public static final long TIME_BETWEEN_PINGS = 30000; // 30 seconds
	    
	  // Constants for method
	  public static final int POST_METHOD = 1;
	  public static final int GET_METHOD = 2;
	  public static final int DEFAULT_METHOD = 1;
	  
	  //Constants for URL
	  public static final String DEFAULT_URL = "http://frodo.cs.memphis.edu:8080/AutoSensePingReceiverServlet";
	  
	  private static HTTPRequester INSTANCE = null;
	  private int method = DEFAULT_METHOD; 
	  private String url = DEFAULT_URL;
	  
	   // Add your data  
	   List<NameValuePair> nameValuePairs; 
	   
	   HttpClient httpclient;  
	   HttpPost httppost; 
	   HttpResponse response;
	   
	   long lastTimeStamp;
	  
	   volatile boolean running;
	   
	  public static HTTPRequester getInstance()
	  {
		  if(INSTANCE == null)
		  {
			  INSTANCE = new HTTPRequester();
		  }
		  return INSTANCE;
	  }
	  
	  public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setMethod(int method) {
		this.method = method;
	}

	public int getMethod() {
		return method;
	}

	public HTTPRequester()
	{
		  nameValuePairs = new ArrayList<NameValuePair>(); 
		  httpclient = new DefaultHttpClient();
		  httppost = new HttpPost(url);  
		  response = null;
	  }

	   public void addData(String name, String value) {  
		   // Create a new HttpClient and Post Header  
		    
		   try {  
			   INSTANCE.nameValuePairs.add(new BasicNameValuePair(name, value));  
		   } 
		   catch(Exception e)
		   {			   
		   }
}   
	   public void postAddedData()
	   {
		    running = true;
		    lastTimeStamp = System.currentTimeMillis();
		    INSTANCE.start();
	   }
	   
	   String TAG = "HTTPRequester";
	   public void run()
	   {
		   while (running) {
			   try{
				   int count = 0;
				   long newTimeStamp = System.currentTimeMillis();
				   if (newTimeStamp - lastTimeStamp > TIME_BETWEEN_PINGS) {
					  lastTimeStamp = newTimeStamp;
					   // Execute HTTP Post Request  
					   Log.d(TAG, "ping start");
					  INSTANCE.response = httpclient.execute(httppost);
					   Log.d(TAG, "ping response received");
					  String s = HttpHelper.request(response);
					  String[] params = s.split(";");
					  for (int i=0; i < params.length; i++) {
						  if (params[i].contains("color"))
							  count++;
					  }
					  
					   Log.d(TAG, "num colors = " + count);
				   }
			   }
			   catch(Exception e)
			   {
				   
			   }
		   }
	   }

	public void shutdown() {
		running = false;
	}

}
