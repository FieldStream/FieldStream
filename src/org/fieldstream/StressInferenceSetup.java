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
package org.fieldstream;

//@author Andrew Raij


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fieldstream.service.IInferrenceService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class StressInferenceSetup extends Activity 
{
	protected static final String TAG = "StressInferenceSetup";

	// inference service access
	public IInferrenceService service;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.sisetup_layout);

		createUI();		
	}

	/* This is called when the app is killed. */
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	/* END ANDROID LIFE CYCLE */

	public void createUI()
	{
		ListView options = (ListView) findViewById(R.id.setup_options_list);
		
		// fill in the options
		List<Map<String, String>> groupData = new ArrayList<Map<String, String>>() {
			{
				add(new HashMap<String, String>() {
					{
						put( "text1",  "Network" );
						put( "text2",  "Select a new bridge" );
					}
				});
				
				add(new HashMap<String, String>() {
					{
						put( "text1",  "Dead Periods" );
						put( "text2",  "Select the time period with no interviews and the time period the participant will sleep" );
					}
				});				
			}
		};
		
		// -- create an adapter, takes care of binding hash objects in our list to actual row views
		SimpleAdapter adapter = new SimpleAdapter( this, groupData, android.R.layout.simple_list_item_2, 
		                                                   new String[] { "text1", "text2" },
		                                                   new int[]{ android.R.id.text1, android.R.id.text2 } );
		options.setAdapter( adapter );		
		
		options.setOnItemClickListener(onClickListener);		
	}

	// Scan Button Handler
	private OnItemClickListener onClickListener = new OnItemClickListener() 
	{
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (position == 0) {
				Intent intent = new Intent(getBaseContext(), NetworkSetup.class);				
				startActivity(intent);
			}
			else if (position == 1) {
				Intent intent = new Intent(getBaseContext(), DeadPeriodSetup.class);
				startActivity(intent);				
			}			
		}
	};

}
