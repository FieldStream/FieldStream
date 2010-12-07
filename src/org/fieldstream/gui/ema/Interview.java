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

//@author Brian French
//@author Andrew Raij

package org.fieldstream.gui.ema;

import java.io.Serializable;
import java.util.ArrayList;

import org.fieldstream.R;
import org.fieldstream.service.logger.Log;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class Interview extends AbstractInterview {
	
	ArrayAdapter<String> responseListAdapter;
	ArrayList<String> list;

	private ListView listResponseView = null;
    private EditText freeResponseView = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

    	context = this;
    	// configure the layout for this interview type
    	setContentView(R.layout.interview_layout);
        layout = (RelativeLayout) findViewById(R.id.ButtonLayout);
        questionView = (TextView) findViewById(R.id.Question);
        //responseView = (View) findViewById(R.id.EMAResponseList);
        
        
        
        incentiveView = (View) findViewById(R.id.EMAIncentive);
        incentiveCurrentView = (TextView) findViewById(R.id.EMAIncentiveCurrent);
        incentiveTotalView = (TextView) findViewById(R.id.EMAIncentiveTotalEarned);
        
        //responseList.setFocusable(true);
        backButton = (Button) findViewById(R.id.BackButton);
        nextButton = (Button) findViewById(R.id.NextButton);
    	
    	// instantiate content before calling super or else null pointer creating entry
    	content = AutoSenseStudyInterviewContent.getInstance();
    	    	
    	initResponseView();
            	
    	super.onCreate(savedInstanceState);

    }

	private TextWatcher tw = new TextWatcher() {

		public void afterTextChanged(Editable s) {
			int len = freeResponseView.getText().length();
			
			Log.d("incentive", "afterTextChanged called!");
			
			String incentiveID;
			if (state == POST_DELAY) {
				incentiveID = "DELAY" + currQuestion;
			}
			else {
				incentiveID = "" + currQuestion;
			}
			
			if (len == 0) {
				nextButton.setVisibility(View.INVISIBLE);
				if (incentives != null)
					if (incentives.isPerQuestion())
						incentives.markIncentive(incentiveID, false);
				Log.d("AbstractIncentive", "false");
			}
			else if (len >= 1){
				nextButton.setVisibility(View.VISIBLE);
				if (incentives != null)
					if (incentives.isPerQuestion())
						incentives.markIncentive(incentiveID, true);
				Log.d("AbstractIncentive", "true");
			}
			
			updateIncentiveView();
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

			
		}
	};
    
    
    
    public void initResponseView() {
    	// list view
    	ArrayList<String> list = new ArrayList<String>();
		responseListAdapter = new ArrayAdapter<String>(getBaseContext(), R.layout.ema_response_row_layout, list);
        listResponseView = (ListView) findViewById(R.id.EMAResponseList);
		listResponseView.setAdapter(responseListAdapter);
		listResponseView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				
				
				//selectedResponse[currQuestion] = arg2;
//				arg1.setSelected(true);
//				View responsible = responseList.getSelectedView();
//				responsible.setPressed(true);
//				tempRow = arg3;
				System.out.println("response " + arg2 + ", row: " + arg3);

				Log.d("incentive", "onItemClick called!  My caller is " + arg2);
				
				String incentiveID;
				if (state == POST_DELAY) {
					incentiveID = "DELAY" + currQuestion;
				}
				else {
					incentiveID = "" + currQuestion;
				}

				if (listResponseView.getChoiceMode() == ListView.CHOICE_MODE_SINGLE) {
					Log.d("AbstractIncentive", "single - item checked = " + listResponseView.getCheckedItemPosition());
					if (listResponseView.getCheckedItemPosition() == ListView.INVALID_POSITION) {
						if (incentives!=null)
							if (incentives.isPerQuestion())
								incentives.markIncentive(incentiveID, false);
						nextButton.setVisibility(View.INVISIBLE);
						Log.d("AbstractIncentive", "single - false");
					}
					else {
						if (incentives!=null)
							if (incentives.isPerQuestion())
								incentives.markIncentive(incentiveID, true);
						nextButton.setVisibility(View.VISIBLE);
						Log.d("AbstractIncentive", "single = true");
					}
				}
				else if (listResponseView.getChoiceMode() == ListView.CHOICE_MODE_MULTIPLE) {
					int numChecked = 0;
					int numItems = listResponseView.getCount();
					for (int i = 0; i < numItems; i++) {
						if (listResponseView.isItemChecked(i))
							numChecked+=1;
					}
					
					Log.d("AbstractIncentive", "multiple - num items checked = " + numChecked);
					
					if (numChecked == 0) {
						if (incentives!=null)
							if (incentives.isPerQuestion())
								incentives.markIncentive(incentiveID, false);					
						nextButton.setVisibility(View.INVISIBLE);
						Log.d("AbstractIncentive", "multiple - false");
					}
					else {
						if (incentives!=null)
							if (incentives.isPerQuestion())
								incentives.markIncentive(incentiveID, true);
						nextButton.setVisibility(View.VISIBLE);
						Log.d("AbstractIncentive", "multiple - true");
					}					
				}
				updateIncentiveView();
			}			
		});
		
		// freeform view
		freeResponseView = (EditText) findViewById(R.id.EMAFreeResponse);		
		freeResponseView.addTextChangedListener(tw);
    }
    
    public Serializable selectedResponse() {
    	if (responseView == listResponseView) {
	    	SparseBooleanArray selections = listResponseView.getCheckedItemPositions();
	//    	int maxSelection = ((ListView)responseView).getAdapter().getCount() - 1;
	    	//if (Log.DEBUG) Log.d("Interview, selectedResponse()","selection=" + selection + " num=" + numSelections);
	
	    	Integer res = new Integer(0);
	    	for (int i=0; i < selections.size(); i++) {
	    		int key = selections.keyAt(i);
	    		if (selections.get(key)) {
			    	int value = 1;
		    		for (int j=0; j < key; j++)	{   
		    			value *= 2;
		    		}
		    		res += value;
	    		}
	    	}	    	
	    	return res;
    	}
    	else if (responseView == freeResponseView){
    		String res = freeResponseView.getText().toString();
    		return res;
    	}
        	
//    	if (selections >= 0 || responseAdapter.isEmpty()) {
//			res = new Integer(maxSelection - selection);
//		}
    	return null;
    }
    
    public void clearResponseView() {
    	responseListAdapter.clear();
    	listResponseView.clearChoices();
    	
		freeResponseView.removeTextChangedListener(tw);
    	freeResponseView.setText("");
		freeResponseView.addTextChangedListener(tw);
    }
    
    public void updateResponseView(String[] res) {
    	//if (Log.DEBUG) Log.d("updateResponseView", "scrollX: " + ((ListView)responseView).getScrollX() + " scrollY: " + ((ListView)responseView).getScrollY());
    		    	
		if (content.isQuestionFreeResponse(currQuestion)) {
			freeResponseView.setVisibility(View.VISIBLE);
			listResponseView.setVisibility(View.INVISIBLE);
			responseView = freeResponseView;

			InputMethodManager mgr = (InputMethodManager) freeResponseView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			mgr.showSoftInput(freeResponseView, InputMethodManager.SHOW_FORCED);
//			mgr.showSoftInputFromInputMethod(freeResponseView.getWindowToken(), InputMethodManager.SHOW_FORCED);
//			mgr.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,InputMethodManager.HIDE_IMPLICIT_ONLY);
									
			String response = (String) entry.getResponse(currQuestion);
			if (response != null) {
				freeResponseView.removeTextChangedListener(tw);
				freeResponseView.setText("");
				freeResponseView.append(response);

				nextButton.setVisibility(View.VISIBLE);
				freeResponseView.addTextChangedListener(tw);
			}
			else {
				nextButton.setVisibility(View.INVISIBLE);
			}
		}
		else if (res != null) {
			listResponseView.setVisibility(View.VISIBLE);
			freeResponseView.setVisibility(View.INVISIBLE);
			responseView = listResponseView;
			
			InputMethodManager mgr = (InputMethodManager) freeResponseView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			mgr.hideSoftInputFromInputMethod(freeResponseView.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
			
			int len = res.length;
			for (int i=0; i< len; i++) {
				responseListAdapter.add(res[i]);
			}


			((ListView) responseView).setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			if (state == CONDUCTING) {
				if (content.isQuestionMultipleChoice(currQuestion)) {
					((ListView) responseView).setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
				}
			}
			// reset the adapter for the list view so that the scroll postition resets to the top
			((ListView)responseView).setAdapter(responseListAdapter);
			
			Integer response = 0;
			if (state == POST_DELAY) {
				 response = (Integer) entry.getDelayResponse(currQuestion);
			}
			else {
				response = (Integer) entry.getResponse(currQuestion);
			}
			//if (Log.DEBUG) Log.d("updateResponseView", "selection: " + response.intValue() + " max: "+maxSelection);
			if (response != null) {
				int sel = response.intValue();
				int i=0;
				if (!responseListAdapter.isEmpty()) {
					while (sel > 0) {
						// this bit is set, therefore this item should be checked
						((ListView)responseView).setItemChecked(i, (sel & 1) == 1);
						sel = sel >> 1;
						i++;
					}
				}
				nextButton.setVisibility(View.VISIBLE);
			} 
			else {
				nextButton.setVisibility(View.INVISIBLE);
			}			
		} else {
			nextButton.setVisibility(View.VISIBLE);
//			if (backUsed) backUsed = false;
		}
    }
    
    public void hideResponseView() {
    	listResponseView.setVisibility(View.INVISIBLE);
    	freeResponseView.setVisibility(View.INVISIBLE);
    }

	@Override
	public void showResponseView() {
		if (content.isQuestionFreeResponse(currQuestion)) {
			freeResponseView.setVisibility(View.VISIBLE);
			listResponseView.setVisibility(View.INVISIBLE);
		}
		else {
			listResponseView.setVisibility(View.VISIBLE);
			freeResponseView.setVisibility(View.INVISIBLE);			
		}
		
	}

}
