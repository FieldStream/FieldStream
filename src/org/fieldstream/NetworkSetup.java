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

// @author Somnath Mitra
// @author Andrew Raij


package org.fieldstream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.fieldstream.service.logger.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import backport.android.bluetooth.BluetoothAdapter;
import backport.android.bluetooth.BluetoothDevice;
import edu.cmu.ices.stress.phone.R;
import edu.cmu.ices.stress.phone.service.IInferrenceService;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class NetworkSetup extends Activity {
		
	protected static final String TAG = "StressInferenceSetup";

	// inference service access
	public IInferrenceService service;

	// Adapters
	private ProgressDialog pd;
	private ArrayList<CharSequence> 	deviceArray;
	private ArrayAdapter<CharSequence> deviceArrayAdapter;


	//UI components
	private Button 		scanButton;
	private Spinner  	deviceSpinner;

	//state variables
	private final Activity thisApp = this;
	private String currentDeviceAddress = null;

	private boolean configChanged = false;
	
	BluetoothAdapter btAdapter;

	/* END USER INTERFACE DECLARATIONS */

    private static final int REQUEST_ENABLE_BT = 2;
    private static final int BRIDGE_NOT_FOUND_DIALOG_ID = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.network_setup_layout);

		initUI();		
		
		// check on the bluetooth adapter
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter == null) {
			// bluetooth not supported on this device...
			if (Log.DEBUG) Log.d(TAG, "Bluetooth not supported on this device.");
			
			Toast.makeText(getApplicationContext(), "Network Setup Error: Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
			finish();
			
		}
		else {
			// ask the user to enable bluetooth if its not enabled already
			if (!btAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			} else {
				scanButton.setVisibility(View.VISIBLE);
			}
		}
		
		deviceArray = new ArrayList<CharSequence>(); 
		
//		loadConfigFromFile();    // until we add pairing, don't bother with loading config from file
	}


	@Override
	public void onPause() {
		super.onPause();
		if (configChanged) {
			writeConfigToFile();
			configChanged = false;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (currentDeviceAddress == null) {
			loadConfigFromFile();
		}
	}
	
	
	void loadConfigFromFile() {
		File root = Environment.getExternalStorageDirectory();
		try {
			File dir = new File(root+"/"+Constants.CONFIG_DIR);
			dir.mkdirs();

			File setupFile = new File(dir, Constants.NETWORK_CONFIG_FILENAME);
			if (!setupFile.exists())
				return;
			
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document dom = builder.parse(setupFile);
            
            Element xmlroot = dom.getDocumentElement();		
            
            NodeList nodeList = xmlroot.getElementsByTagName("bridge");
            for (int i=0; i < nodeList.getLength(); i++) {
            	Element element = (Element)nodeList.item(i);
            	Log.d("NetworkSetup", "tag name " + i + ": " + element.getNodeName() + "/" + element.getTagName());
            	currentDeviceAddress = element.getFirstChild().getNodeValue();
            	if (currentDeviceAddress == null)
            		Log.d("NetworkSetup", "device: null");
            	else
            		Log.d("NetworkSetup", "device: " +currentDeviceAddress);
            }            
            fillSpinner(currentDeviceAddress);

            ArrayList<Integer> sensorMotes = new ArrayList<Integer>();
            nodeList = xmlroot.getElementsByTagName("sensor_mote");
            for (int i=0; i < nodeList.getLength(); i++) {
            	Node node = nodeList.item(i);
            	sensorMotes.add(Integer.parseInt(node.getNodeValue()));            	
            }
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.d("SETUP",e.getMessage());
			e.printStackTrace();
		}
	}
	
	void writeConfigToFile() {
		try {			
			ArrayList<Integer> sensorMotes = new ArrayList<Integer>();
			
			File root = Environment.getExternalStorageDirectory();
			File dir = new File(root+"/"+Constants.CONFIG_DIR);
			dir.mkdirs();
			File setupFile = new File(dir, Constants.NETWORK_CONFIG_FILENAME);
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(setupFile));
			writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
			writer.write("<fieldstream>\n");
			writer.write("\t<network>\n");
			writer.write("\t\t<bridge>");   writer.write(currentDeviceAddress);   writer.write("</bridge>\n");
            for (Integer i : sensorMotes) {
            	writer.write("\t\t<sensor_mote>");   writer.write(String.valueOf(i));   writer.write("</sensor_mote>\n");
            }		
			writer.write("\t</network>\n");
			writer.write("</fieldstream>");
			
			writer.close();
			
			Toast.makeText(getApplicationContext(), "Network Setup Saved Successfully", Toast.LENGTH_SHORT).show();
			
		}
		catch(Exception e) {
			// TODO Auto-generated catch block
			Log.d("SETUP",e.getMessage());
			e.printStackTrace();			

			Toast.makeText(getApplicationContext(), "Error Saving Network Setup", Toast.LENGTH_SHORT).show();			
		}   
	}
	

	public void initUI()
	{
		//scan Button
		scanButton = (Button) findViewById(R.id.scan_btdevice);
		scanButton.setOnClickListener(scanButtonOnClickListener);
		scanButton.setVisibility(View.INVISIBLE);

		// deviceSpinner
		deviceSpinner = (Spinner) findViewById(R.id.btdevice_spinner);
		deviceArrayAdapter = ArrayAdapter.createFromResource(this, R.array.device_spinner_list, android.R.layout.simple_spinner_item);
		deviceArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		deviceSpinner.setAdapter(deviceArrayAdapter);        
		deviceSpinner.setOnItemSelectedListener(deviceSpinnerOnItemSelectedListener);
	}

	// Scan Button Handler
	private OnClickListener scanButtonOnClickListener = new OnClickListener() 
	{
		// Scan Button onClick listener

		public void onClick(View v) {
			pd = ProgressDialog.show(thisApp, "Scanning...", "Searching nearby bluetooth devices...");
			
			// Register the BroadcastReceiver
			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
			registerReceiver(deviceFoundReceiver, filter); // Don't forget to unregister during onDestroy

			filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
			registerReceiver(scanEndedReceiver, filter); // Don't forget to unregister during onDestroy

			deviceArray.clear();

//			Set<BluetoothDevice> alreadyPaired = btAdapter.getBondedDevices();
//			Iterator<BluetoothDevice> iter = alreadyPaired.iterator();
//			while (iter.hasNext()) {
//				String address = iter.next().getAddress();
//				if (address.startsWith("00:A0"))  
//					deviceArray.add(address);
//			}
	
			btAdapter.startDiscovery();
		}

	};




	// Device Spinner Handler
	@SuppressWarnings("unchecked")
	private Spinner.OnItemSelectedListener deviceSpinnerOnItemSelectedListener = new Spinner.OnItemSelectedListener() 
	{

		public void onItemSelected(AdapterView parent, View v, int position, long id)
		{
			currentDeviceAddress = parent.getSelectedItem().toString();
			String TAG = "deviceSpinnerOnItemSelectedListener";
			if (Log.DEBUG) Log.d(TAG, currentDeviceAddress);			
		}

		public void onNothingSelected(AdapterView parent) 
		{ 

		}
	};

	public  String currentSubject="Generic";
	// subject chooser

	
	// Create a BroadcastReceiver for finding a bt device
	private final BroadcastReceiver deviceFoundReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        // When discovery finds a device
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	            // Get the BluetoothDevice object from the Intent
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            // Add the name and address to an array adapter to show in a ListView
				if (device.getAddress().startsWith("00:A0")) {
					deviceArray.add(device.getAddress());
		        }
	        }
	    }
	};

	private void fillSpinner(String device) {
		ArrayList<CharSequence> devices = new ArrayList<CharSequence>();
		devices.add(device);
		fillSpinner(devices);
	}
	
	private void fillSpinner(ArrayList<CharSequence> devices) {
		deviceArrayAdapter = new ArrayAdapter<CharSequence>(thisApp, android.R.layout.simple_spinner_item, devices);
		deviceArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		deviceSpinner.setAdapter(deviceArrayAdapter);
	}
	
	// Create a BroadcastReceiver for catching when scanning ends
	private final BroadcastReceiver scanEndedReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        // When discovery finds a device
	        if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
	        	thisApp.unregisterReceiver(deviceFoundReceiver);
	        	thisApp.unregisterReceiver(scanEndedReceiver);

	        	if (deviceArray.size() == 0) {
	        		showDialog(BRIDGE_NOT_FOUND_DIALOG_ID);
	        	}	        	
	        	else {
	        		fillSpinner(deviceArray);
					configChanged = true;
	        	}
	        	
	    		pd.dismiss();
	        }
	    }
	};
	
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		if (id == BRIDGE_NOT_FOUND_DIALOG_ID) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Could not find a Bridge to connect to.  Check to make sure the bridge is on and try scanning again.")
			       .setCancelable(true)
			       .setNegativeButton("OK", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			           }
			       });
			dialog = builder.create();
		}
		return dialog;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == Activity.RESULT_OK ) {
				if (Log.DEBUG) Log.d(TAG, "User Enabled Bluetooth");
				scanButton.setVisibility(View.VISIBLE);
			}
			else {
				if (Log.DEBUG) Log.d(TAG, "Bluetooth not enabled by user (or an error occurred)");
				scanButton.setVisibility(View.INVISIBLE);
			}
		}
	}

	
}
