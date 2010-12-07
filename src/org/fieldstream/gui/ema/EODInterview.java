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

// @author Brian French

package org.fieldstream.gui.ema;

import java.io.Serializable;

import org.fieldstream.R;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class EODInterview extends AbstractInterview {

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

    	//context = this;
    	// configure the layout for this interview type
    	setContentView(R.layout.eod_layout);
        layout = (RelativeLayout) findViewById(R.id.ButtonLayout);
        questionView = (TextView) findViewById(R.id.Question);
        responseView = findViewById(R.id.EditText);
        //responseList.setFocusable(true);
        backButton = (Button) findViewById(R.id.BackButton);
        nextButton = (Button) findViewById(R.id.NextButton);
    	
    	// instantiate content before calling super or else null pointer creating entry
    	content = new EODContent();
    	
    	initResponseView();
        
    	super.onCreate(savedInstanceState);

    }
	
	@Override
	public void clearResponseView() {
		// clear the text
		((EditText)responseView).setText("");
	}

	@Override
	public void initResponseView() {
		
		// initialize the entry array contents
//		int length = content.getNumberQuestions();
//		for (int i=0; i< length; i++) {
//			entry.setResponse(i, "", -1);
//		}
		
		((EditText)responseView).setText("");
		((EditText)responseView).setOnEditorActionListener(new OnEditorActionListener() {

			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				
				if (nextButton.getVisibility() == View.VISIBLE && currQuestion < content.getNumberQuestions(false)) {
					nextButton.performClick();
				}
				return true;
			}
			
		});
		((EditText)responseView).setOnKeyListener(new OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				
				System.out.println("key pressed with focus on editor " + keyCode);
				// ignore keyUp actions and all key events associated with the enter button
				boolean ignore = ignoredKey(keyCode);
				
				if (event.getAction() == KeyEvent.ACTION_DOWN && !ignore) {
					nextButton.setVisibility(View.VISIBLE);
				}
				if (((EditText)responseView).getText().toString().equalsIgnoreCase("") && ignore) {
					nextButton.setVisibility(View.INVISIBLE);
				}
				return false;
			}
			
		});

	}
	
	private boolean ignoredKey(int keyCode) {
		if (keyCode == KeyEvent.KEYCODE_ENTER ||
				keyCode == KeyEvent.KEYCODE_ALT_LEFT ||
				keyCode == KeyEvent.KEYCODE_ALT_RIGHT ||
				keyCode == KeyEvent.KEYCODE_SHIFT_LEFT ||
				keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT ||
				keyCode == KeyEvent.KEYCODE_SEARCH ||
				keyCode == KeyEvent.KEYCODE_MENU ||
				keyCode == KeyEvent.KEYCODE_SPACE ||
				keyCode == KeyEvent.KEYCODE_DEL) {
			return true;
		}
		return false;
	}

	@Override
	public Serializable selectedResponse() {
		
		Serializable response = null;
		String text = ((EditText)responseView).getText().toString();
		if (!text.equals("")) {
			// remove all commas since the log is comma separated
			response = text.replaceAll(",", "");
		}
		
		return response;
	}

	@Override
	public void updateResponseView(String[] res) {
		//res should always be null
		String text = ((EditText)responseView).getText().toString();
		
		// restore last selected item if back option was used
		if (backUsed) {
			backUsed = false;
			// kludge
			String sel = entry.getResponse(currQuestion).toString();
			if (!sel.equalsIgnoreCase("")) {
				((EditText)responseView).setText(sel);
			}
			nextButton.setVisibility(View.VISIBLE);
		}
		
		if (text.equals("")) {
			nextButton.setVisibility(View.INVISIBLE);
		}
		
	}

	@Override
	public void hideResponseView() {
		((EditText)responseView).setVisibility(View.INVISIBLE);
	}

	@Override
	public void showResponseView() {
		((EditText)responseView).setVisibility(View.VISIBLE);
		
	}

}
