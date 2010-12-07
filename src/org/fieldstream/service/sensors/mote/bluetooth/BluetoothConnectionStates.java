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
//@author Andrew Raij


package org.fieldstream.service.sensors.mote.bluetooth;


/*
 * This class abstracts bluetooth states and modes.
 * <br> Modes represent local states, such as CONNECT / DISCONNECT
 * <br> phases of the power cycle state.
 * <p> The Bluetooth connection can be in different states such as
 * <br> DUTY CYCLE - The phone and the radio turn on their bt radios
 *      after a fixed interval to transmit data
 * <br> NO DUTY CYCLE (CONTINOUS) 
 * <br> OUT OF RANGE - The bluetooth bridge node is out of range
 * <br> DEAD TIME - The participant has explicitly asked to switch off 
 *      data collection
 */
public class BluetoothConnectionStates {
	
	public static final String BT_DISCONNECT_STRING = "BTDISCONNECT";
	
	
	public static final int BT_MODE_CONNECT = 1;
	public static final int BT_MODE_DISCONNECT = 2;
	public static final int BT_DEFAULT_MODE = BT_MODE_CONNECT;
	
	public static final int BT_STATE_BRIDGE_PROBLEM = 3;
	public static final int BT_STATE_DEAD_TIME = 4;
	public static final int BT_STATE_DUTY_CYCLE = 5;
	public static final int BT_STATE_NO_DUTY_CYCLE = 6;
	public static final int BT_STATE_BRIDGE_DISCONNECTED = 7;
	public static final int BT_STATE_PERIODIC_SCANNING = 8;
	public static final int BT_DEFAULT_STATE = BT_STATE_NO_DUTY_CYCLE;
	
	public static final long BT_DUTY_CYCLE_PERIOD_DEFAULT_MILLIS = 1000;
	
	public static final long BT_TIMEOUT_DEFAULT_MILLIS = 60000;
	
	public static final long BT_SCAN_PERIOD_DEFAULT_MILLIS = 30000;
	
	public static long BT_DUTY_CYCLE_PERIOD_MILLIS = BT_DUTY_CYCLE_PERIOD_DEFAULT_MILLIS;

	public static final long BT_CHECK_TIME_DEFAULT = 1000;
		
	public static long getBT_DUTY_CYCLE_PERIOD_MILLIS() {
		return BT_DUTY_CYCLE_PERIOD_MILLIS;
	}

	public static void setBT_DUTY_CYCLE_PERIOD_MILLIS(long bTDUTYCYCLEPERIODMILLIS) {
		BT_DUTY_CYCLE_PERIOD_MILLIS = bTDUTYCYCLEPERIODMILLIS;
	}
	
	public static int CURRENT_BT_STATE;

	public static int getCURRENT_BT_STATE() {
		return CURRENT_BT_STATE;
	}

	public static void setCURRENT_BT_STATE(int cURRENTBTSTATE) {
		CURRENT_BT_STATE = cURRENTBTSTATE;
	}
	
	public static long CURRENT_BT_STATE_START_TIME;
	
	public static long getCURRENT_BT_STATE_START_TIME() {
		return CURRENT_BT_STATE_START_TIME;
	}

	public static void setCURRENT_BT_STATE_START_TIME(long cURRENTBTSTATESTARTTIME) {
		CURRENT_BT_STATE_START_TIME = cURRENTBTSTATESTARTTIME;
	}
	
	public static long CURRENT_BT_STATE_END_TIME;
	
	public static long getCURRENT_BT_STATE_END_TIME() {
		return CURRENT_BT_STATE_END_TIME;
	}

	public static void setCURRENT_BT_STATE_END_TIME(long cURRENTBTSTATEENDTIME) {
		CURRENT_BT_STATE_END_TIME = cURRENTBTSTATEENDTIME;
	}

	public static int CURRENT_BT_MODE;

	public static int getCURRENT_BT_MODE() {
		return CURRENT_BT_MODE;
	}

	public static void setCURRENT_BT_MODE(int cURRENTBTMODE) {
		CURRENT_BT_MODE = cURRENTBTMODE;
	}
	
	public static long CURRENT_BT_MODE_START_TIME;
		
	public static long getCURRENT_BT_MODE_START_TIME() {
		return CURRENT_BT_MODE_START_TIME;
	}

	public static void setCURRENT_BT_MODE_START_TIME(long cURRENTBTMODESTARTTIME) {
		CURRENT_BT_MODE_START_TIME = cURRENTBTMODESTARTTIME;
	}
	
	public static long CURRENT_BT_MODE_END_TIME;
	public static long getCURRENT_BT_MODE_END_TIME() {
		return CURRENT_BT_MODE_END_TIME;
	}

	public static void setCURRENT_BT_MODE_END_TIME(long cURRENTBTMODEENDTIME) {
		CURRENT_BT_MODE_END_TIME = cURRENTBTMODEENDTIME;
	}
	

	public static long BT_TIMEOUT = BT_TIMEOUT_DEFAULT_MILLIS;

	public static long getBT_TIMEOUT() {
		return BT_TIMEOUT;
	}

	public static void setBT_TIMEOUT(long bTTIMEOUT) {
		BT_TIMEOUT = bTTIMEOUT;
	}
	
	public static long BT_CHECK_TIME = BT_CHECK_TIME_DEFAULT;

	public static long getBT_CHECK_TIME() {
		return BT_CHECK_TIME;
	}

	public static void setBT_CHECK_TIME(long bTCHECKTIME) {
		BT_CHECK_TIME = bTCHECKTIME;
	}
	
	public static long BT_SCAN_PERIOD = BT_SCAN_PERIOD_DEFAULT_MILLIS;

	public static long getBT_SCAN_PERIOD() {
		return BT_SCAN_PERIOD;
	}

	public static void setBT_SCAN_PERIOD(long bTSCANPERIOD) {
		BT_SCAN_PERIOD = bTSCANPERIOD;
	}
	
}
