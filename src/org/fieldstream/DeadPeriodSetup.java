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


// @author Patrick Blitz
// @author Andrew Raij


package org.fieldstream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.fieldstream.service.logger.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.cmu.ices.stress.phone.R;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Time;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


public class DeadPeriodSetup extends Activity {
	private Button quietStartTimeButton, quietStartDateButton, quietEndTimeButton, quietEndDateButton, sleepingStartDateButton, sleepingStartTimeButton, sleepingEndDateButton, sleepingEndTimeButton;
	private Calendar quietStartTime, quietEndTime, sleepingStartTime, sleepingEndTime;   
	
	boolean settingsChanged = false;
	
	// DIALOG IDs
	static final int QUIET_START_TIME_DIALOG_ID = 0;
	static final int QUIET_END_TIME_DIALOG_ID = 1;
	static final int SLEEPING_START_TIME_DIALOG_ID = 2;
	static final int SLEEPING_END_TIME_DIALOG_ID = 3;
	static final int QUIET_START_DATE_DIALOG_ID = 4;
	static final int QUIET_END_DATE_DIALOG_ID = 5;
	static final int SLEEPING_START_DATE_DIALOG_ID = 6;
	static final int SLEEPING_END_DATE_DIALOG_ID = 7;

	
	
	static final long HOUR_MILLIS = 60L * 60L * 1000L;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.dead_period_setup_layout);
	    	    
	    // capture our View elements
	    quietStartDateButton = (Button) findViewById(R.id.NoInterviewsStartDateButton);	    
	    quietStartTimeButton = (Button) findViewById(R.id.NoInterviewsStartTimeButton);
	    quietEndDateButton = (Button) findViewById(R.id.NoInterviewsEndDateButton);	    
	    quietEndTimeButton = (Button) findViewById(R.id.NoInterviewsEndTimeButton);
	    sleepingStartDateButton = (Button) findViewById(R.id.SleepingStartDateButton);	    
	    sleepingStartTimeButton = (Button) findViewById(R.id.SleepingStartTimeButton);
	    sleepingEndDateButton = (Button) findViewById(R.id.SleepingEndDateButton);	    
	    sleepingEndTimeButton = (Button) findViewById(R.id.SleepingEndTimeButton);
	    
	    // add a click listener to the button
	    quietStartTimeButton.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	            showDialog(QUIET_START_TIME_DIALOG_ID);
	        }
	    });
	    quietEndTimeButton.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	            showDialog(QUIET_END_TIME_DIALOG_ID);
	        }
	    });
	    sleepingStartTimeButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				showDialog(SLEEPING_START_TIME_DIALOG_ID);
			}
		});
	    sleepingEndTimeButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				showDialog(SLEEPING_END_TIME_DIALOG_ID);
			}
		});
	    // add a click listener to the button
	    quietStartDateButton.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	            showDialog(QUIET_START_DATE_DIALOG_ID);
	        }
	    });
	    quietEndDateButton.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	            showDialog(QUIET_END_DATE_DIALOG_ID);
	        }
	    });
	    sleepingStartDateButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				showDialog(SLEEPING_START_DATE_DIALOG_ID);
			}
		});
	    sleepingEndDateButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				showDialog(SLEEPING_END_DATE_DIALOG_ID);
			}
		});

	}
	
	void loadDeadPeriods() {
	    Calendar now = Calendar.getInstance();
	    quietStartTime = (Calendar) now.clone();
	    quietEndTime = (Calendar) now.clone();
	    sleepingStartTime = (Calendar) now.clone();
	    sleepingEndTime = (Calendar) now.clone();
	    sleepingEndTime.add(Calendar.DAY_OF_YEAR, 1);
		
		
		File root = Environment.getExternalStorageDirectory();

		File dir = new File(root+"/"+Constants.CONFIG_DIR);
		dir.mkdirs();

		File setupFile = new File(dir, Constants.DEAD_PERIOD_CONFIG_FILENAME);
		if (!setupFile.exists())
			return;
		
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Document dom = null;
		try {
			dom = builder.parse(setupFile);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        Element xmlroot = dom.getDocumentElement();		
        
        NodeList nodeList = xmlroot.getElementsByTagName("period");
        for (int i=0; i < nodeList.getLength(); i++) {
        	Node node = nodeList.item(i);
        	NamedNodeMap map = node.getAttributes();
        	if (map != null) {
        		Node type = map.getNamedItem("type");
        		Node start = map.getNamedItem("start");
        		Node end = map.getNamedItem("end");

        		if (type != null && start != null && end != null) {
        			if (type.getNodeValue().equalsIgnoreCase("quiet")) {
        				long quietStart = Long.parseLong(start.getNodeValue());
        				long quietEnd = Long.parseLong(end.getNodeValue());
        				
        				if (now.getTimeInMillis() < quietEnd) {
        					quietStartTime.setTimeInMillis(quietStart);
        					quietEndTime.setTimeInMillis(quietEnd);
        				}
        			}
        			else if (type.getNodeValue().contentEquals("off")) {
        				long offStart = Long.parseLong(start.getNodeValue());
        				long offEnd = Long.parseLong(end.getNodeValue());            				            				
        				
        				if (now.getTimeInMillis() < offEnd) {
        					sleepingStartTime.setTimeInMillis(offStart);
        					sleepingEndTime.setTimeInMillis(offEnd);
        				}        				
        			}         			
        		}            		
        	}
        }
	}	
	
    @Override
    public void onPause() {
    	super.onPause();
    	
    	if (settingsChanged) {   	
			long quietStartTimeUnix = quietStartTime.getTimeInMillis();
			long quietEndTimeUnix = quietEndTime.getTimeInMillis();
			long sleepingStartTimeUnix = sleepingStartTime.getTimeInMillis();
			long sleepingEndTimeUnix = sleepingEndTime.getTimeInMillis();
						
			writeConfigToFile(quietStartTimeUnix, quietEndTimeUnix, sleepingStartTimeUnix, sleepingEndTimeUnix);
			settingsChanged = false;
    	}
    }	

    @Override
    public void onResume() {
    	super.onResume();
    	
	    loadDeadPeriods();
	    
	    // display the current date
	    updateDisplay();    	
    }
    
	void writeConfigToFile(long quietStart, long quietEnd, long offStart, long offEnd) {
		try {			
			File root = Environment.getExternalStorageDirectory();
			File dir = new File(root+"/"+Constants.CONFIG_DIR);
			dir.mkdirs();
			File setupFile = new File(dir, Constants.DEAD_PERIOD_CONFIG_FILENAME);
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(setupFile));
			writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
			writer.write("<fieldstream>\n");
			writer.write("\t<deadperiods>");   
			writer.write("\t\t<period type=\"quiet\" start=\"" + quietStart + "\" end=\"" + quietEnd + "\" />" );
			writer.write("\t\t<period type=\"off\" start=\"" + offStart + "\" end=\"" + offEnd + "\" />" );			
			writer.write("\t</deadperiods>\n");
			writer.write("</fieldstream>");
			
			writer.close();
			
			Toast.makeText(getApplicationContext(), "Dead Periods Saved Successfully", Toast.LENGTH_SHORT).show();			
		}
		catch(Exception e) {
			// TODO Auto-generated catch block
			Log.d("SETUP",e.getMessage());
			e.printStackTrace();			

			Toast.makeText(getApplicationContext(), "Error Saving Network Setup", Toast.LENGTH_SHORT).show();			
		}   		
	}
        
	
	@Override
	protected Dialog onCreateDialog(int id) {
		
		switch (id) {

	    case QUIET_START_TIME_DIALOG_ID:
	        return new TimePickerDialog(this,
	        		onQuietStartTimeSetListener, quietStartTime.get(Calendar.HOUR_OF_DAY), quietStartTime.get(Calendar.MINUTE), false);
	    	
	    case QUIET_END_TIME_DIALOG_ID:
	        return new TimePickerDialog(this,
	        		onQuietEndTimeSetListener, quietEndTime.get(Calendar.HOUR_OF_DAY), quietEndTime.get(Calendar.MINUTE), false);
	    
	    case SLEEPING_START_TIME_DIALOG_ID:
	    	return new TimePickerDialog(this,
	    			onSleepingStartTimeSetListener, sleepingStartTime.get(Calendar.HOUR_OF_DAY), sleepingStartTime.get(Calendar.MINUTE), false);

	    case SLEEPING_END_TIME_DIALOG_ID:
	    	return new TimePickerDialog(this,
	    			onSleepingEndTimeSetListener, sleepingEndTime.get(Calendar.HOUR_OF_DAY), sleepingEndTime.get(Calendar.MINUTE), false);
	    
	    case QUIET_START_DATE_DIALOG_ID:
	        return new DatePickerDialog(this,
	        		onQuietStartDateSetListener, quietStartTime.get(Calendar.YEAR), quietStartTime.get(Calendar.MONTH), quietStartTime.get(Calendar.DAY_OF_MONTH));
	       
	    case QUIET_END_DATE_DIALOG_ID:
	        return new DatePickerDialog(this,
	        		onQuietEndDateSetListener, quietEndTime.get(Calendar.YEAR), quietEndTime.get(Calendar.MONTH), quietEndTime.get(Calendar.DAY_OF_MONTH));
	    
	    case SLEEPING_START_DATE_DIALOG_ID:
	    	return new DatePickerDialog(this,
	    			onSleepingStartDateSetListener, sleepingStartTime.get(Calendar.YEAR), sleepingStartTime.get(Calendar.MONTH), sleepingStartTime.get(Calendar.DAY_OF_MONTH));
	
	    case SLEEPING_END_DATE_DIALOG_ID:
	    	return new DatePickerDialog(this,
	    			onSleepingEndDateSetListener, sleepingEndTime.get(Calendar.YEAR), sleepingEndTime.get(Calendar.MONTH), sleepingEndTime.get(Calendar.DAY_OF_MONTH));
	    }		
	
	    return null;
	}
	
	// updates the time we display in the TextView
	private void updateDisplay() {	
		DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT);
		
	    quietStartTimeButton.setText(timeFormatter.format(quietStartTime.getTime()));
	    quietEndTimeButton.setText(timeFormatter.format(quietEndTime.getTime()));
	    sleepingStartTimeButton.setText(timeFormatter.format(sleepingStartTime.getTime()));
	    sleepingEndTimeButton.setText(timeFormatter.format(sleepingEndTime.getTime()));
	    
	    quietStartDateButton.setText(getDateText(quietStartTime));
	    quietEndDateButton.setText(getDateText(quietEndTime));	    
	    sleepingStartDateButton.setText(getDateText(sleepingStartTime));
	    sleepingEndDateButton.setText(getDateText(sleepingEndTime));	    
	}
	
	private String getDateText(Calendar cal) {
		Calendar today = Calendar.getInstance();
		
		int years = cal.get(Calendar.YEAR) - today.get(Calendar.YEAR);
		int days = cal.get(Calendar.DAY_OF_YEAR) - today.get(Calendar.DAY_OF_YEAR);
		
		if (years == 0) {
			if (days == 0)
				return "Today";
			else if (days == 1)
				return "Tomorrow";
			else if (days > 1 && days < 7) 
				return new SimpleDateFormat("EEEE").format(cal.getTime());
			else 
				return DateFormat.getDateInstance(DateFormat.MEDIUM).format(cal.getTime());
		}
		else if (years == 1) {
			days = days + today.getActualMaximum(Calendar.DAY_OF_YEAR);
			if (days == 0) 
				return "Tomorrow";
			else if (days > 0 && days < 6) 
				return new SimpleDateFormat("EEEE").format(cal.getTime());
			else {
				return DateFormat.getDateInstance(DateFormat.MEDIUM).format(cal.getTime());
			}
		}
		else {
			return DateFormat.getDateInstance(DateFormat.MEDIUM).format(cal.getTime());
		}
	}
	
	// the callback received when the user "sets" the time in the dialog
	private TimePickerDialog.OnTimeSetListener onQuietStartTimeSetListener =
	    new TimePickerDialog.OnTimeSetListener() {
	        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
	        	quietStartTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
	        	quietStartTime.set(Calendar.MINUTE, minute);
	        	settingsChanged = true;
	            updateDisplay();
	        }
	    };
	private TimePickerDialog.OnTimeSetListener onQuietEndTimeSetListener =
	    new TimePickerDialog.OnTimeSetListener() {
	        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
	        	quietEndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
	        	quietEndTime.set(Calendar.MINUTE, minute);
	        	settingsChanged = true;
	            updateDisplay();
	        }
	    };
	// the callback received when the user "sets" the time in the dialog
	private TimePickerDialog.OnTimeSetListener onSleepingStartTimeSetListener =
	    new TimePickerDialog.OnTimeSetListener() {
	        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
	        	sleepingStartTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
	        	sleepingStartTime.set(Calendar.MINUTE, minute);
	        	settingsChanged = true;
	            updateDisplay();
	        }
	    };
	private TimePickerDialog.OnTimeSetListener onSleepingEndTimeSetListener =
	    new TimePickerDialog.OnTimeSetListener() {
	        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
	        	sleepingEndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
	        	sleepingEndTime.set(Calendar.MINUTE, minute);
	        	settingsChanged = true;
	            updateDisplay();
	        }
	    };
	    
	    
    private DatePickerDialog.OnDateSetListener onQuietStartDateSetListener = 
	    new DatePickerDialog.OnDateSetListener() {
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				quietStartTime.set(Calendar.YEAR, year);
				quietStartTime.set(Calendar.MONTH, monthOfYear);
				quietStartTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				settingsChanged = true;
				updateDisplay();
			}
	};
    private DatePickerDialog.OnDateSetListener onQuietEndDateSetListener = 
	    new DatePickerDialog.OnDateSetListener() {
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				quietEndTime.set(Calendar.YEAR, year);
				quietEndTime.set(Calendar.MONTH, monthOfYear);
				quietEndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				settingsChanged = true;
				updateDisplay();
			}
	};
    private DatePickerDialog.OnDateSetListener onSleepingStartDateSetListener = 
	    new DatePickerDialog.OnDateSetListener() {
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				sleepingStartTime.set(Calendar.YEAR, year);
				sleepingStartTime.set(Calendar.MONTH, monthOfYear);
				sleepingStartTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				settingsChanged = true;
				updateDisplay();
			}
	};
    private DatePickerDialog.OnDateSetListener onSleepingEndDateSetListener = 
	    new DatePickerDialog.OnDateSetListener() {
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				sleepingEndTime.set(Calendar.YEAR, year);
				sleepingEndTime.set(Calendar.MONTH, monthOfYear);
				sleepingEndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				settingsChanged = true;
				updateDisplay();
			}
	};
}
