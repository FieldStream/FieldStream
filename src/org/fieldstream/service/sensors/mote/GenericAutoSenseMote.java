package org.fieldstream.service.sensors.mote;

import java.util.HashMap;

import org.fieldstream.Constants;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensors.api.AbstractMote;
import org.fieldstream.service.sensors.mote.sensors.MoteSensorManager;
import org.fieldstream.service.sensors.mote.tinyos.TOSOscopeIntPacket;

public class GenericAutoSenseMote extends AbstractMote implements MoteReceiverInterface {
	
	/*
	 * the standard log tag
	 */
	public String TAG = "GenericAutoSenseMote";
	
	/*
	 * The number of physical sensors on this mote
	 */
	public int NumberOfMoteSensors;
	
		
	/*
	 * This variable is a flag to check
	 * if a reset happened or not
	 */
	protected boolean resetHappened;
	
	
	/*
	 * This variable is a flag to check 
	 * if a mote is disconnected or not
	 */
	protected boolean disconnectHappened;
		
	// sensor maps are the channel numbers on the motes
	// and their corresponding sensor numbers on the phone
	// programmer should know these and load these 
	public int[][] sensorMap;
	
	public String bridgeAddress;
	
//	private TOSOscopeIntPacket[] buffer;
//	
//	private int bufferLength = 20;
//	private int bufferIndex = -1;
//	
	private static HashMap<Integer, Integer> sensorIndex;
	private int sensorCount;
	
	/*
	 * packetLoss is a matrix of
	 *  number of sensors X 4
	 *  where column 0 = sensor 
	 *  where column 1 = lastReceivedSampleNumber
	 *  column 2 = next Expected Sample Number
	 *  column 3 = number of packets lost
	 *  column 4 = number of packets received for this sensor
	 */
	private int[][] packetLoss;
	private long[] timeStamps;
	
	private int receivedPacketCounter;
	
	private final static int WRAP_AROUND_THRESHOLD = 65536;
	
	private final static int WRAP_AROUND_HAPPENED = -2;
	private final static int RESET_HAPPENED = -3;
	// private final static int OUT_OF_ORDER = -4;
	
	private final static int NUMBER_OF_COLUMNS = 5;
	private final static int COLUMN_SENSOR_MOTE_CHANNEL = 0;
	private final static int COLUMN_LAST_RECEIVED_SAMPLE_NUMBER = 1;
	private final static int COLUMN_NEXT_EXPECTED_SAMPLE_NUMBER = 2;
	private final static int COLUMN_NO_OF_PACKETS_LOST = 3;
	private final static int COLUMN_NUMBER_OF_PACKETS_RECEIVED = 4;
	
		
	private final static int SAMPLES_IN_PACKET = 50;
	private final static int PACKET_NOT_RECEIVED = 0;
		
	public GenericAutoSenseMote(int moteType)
	{
		super(moteType);
		initialize();
	}
	
	public void initialize() {
		
		try {
	//		buffer = new TOSOscopeIntPacket[bufferLength];
			switch(MoteType)
			{
			case Constants.MOTE_TYPE_AUTOSENSE_1_ECG: 
				NumberOfMoteSensors = Constants.NO_OF_MOTE_SENSORS_AUTOSENSE_1_ECG;
				break;
				
			case Constants.MOTE_TYPE_AUTOSENSE_1_RIP:
				NumberOfMoteSensors = Constants.NO_OF_MOTE_SENSORS_AUTOSENSE_1_RIP;
				break;
				
			case Constants.MOTE_TYPE_AUTOSENSE_2_ECG_RIP:
				NumberOfMoteSensors = Constants.NO_OF_MOTE_SENSORS_AUTOSENSE_2_ECG_RIP;
				break;
				
			case Constants.MOTE_TYPE_AUTOSENSE_2_ALCOHOL:
				NumberOfMoteSensors = Constants.NO_OF_MOTE_SENSORS_AUTOSENSE_2_ALCOHOL;
				break;
			
			}
			
			// init the packet loss matrix
			packetLoss = new int[NumberOfMoteSensors][NUMBER_OF_COLUMNS];
			
			//init the timestamp matrix
			timeStamps = new long[NumberOfMoteSensors];
			
			// init the sensormap
			sensorMap = ChannelToSensorMapping.getChannelToSensorMap(MoteType);
			
			// create the hashmap to remember which sensor is in which index		
			sensorIndex = new HashMap<Integer, Integer>();
			sensorCount = 0;
			
			for(int i=0;i < NumberOfMoteSensors; i++)
			{
				sensorIndex.put(sensorMap[i][0], sensorCount++ );
				packetLoss[i][COLUMN_SENSOR_MOTE_CHANNEL] = sensorMap[i][0];
				packetLoss[i][COLUMN_LAST_RECEIVED_SAMPLE_NUMBER] = PACKET_NOT_RECEIVED;
				packetLoss[i][COLUMN_NEXT_EXPECTED_SAMPLE_NUMBER] = PACKET_NOT_RECEIVED;
				packetLoss[i][COLUMN_NO_OF_PACKETS_LOST] = PACKET_NOT_RECEIVED;
				
				timeStamps[i] = PACKET_NOT_RECEIVED;
			}
		
			receivedPacketCounter = 0;
			if(Log.DEBUG)
			{
				Log.d(TAG+".initialize()",Constants.getMoteDescription(MoteType)+"initialized" );
			}
		} // end try
		catch(Exception e)
		{
			
		}
		
	}
	
	public void activate() {
		MoteDeviceManager.getInstance().subsribe(this);
				
	}

	@Override
	public void deactivate() {
		MoteDeviceManager.getInstance().unsubscribe(this);
	}

	@Override
	public void sendCommand(int command) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendCommand(String command) {
		// TODO Auto-generated method stub
		
	}

	//@Override
	public void onReceiveMotePacket(TOSOscopeIntPacket toip) 
	{
		Log.d("GenericAutoSenseMote", "onReceiveMotePacket Called");
		synchronized(INSTANCE)
		{
			
			// mote id 
			int moteID = toip.getMoteID();
			
			//channel id 
			int chanID = toip.getChan();
									
			
			boolean myPacket = false;
			
			if(Constants.CURRENT_SENSOR_SUITE == Constants.SENSOR_SUITE_AUTOSENSE_1)
			{
				myPacket = (moteID%ChannelToSensorMapping.getMappingRuleBase() == ChannelToSensorMapping.mappingRule(MoteType));
			}
			else if(Constants.CURRENT_SENSOR_SUITE == Constants.SENSOR_SUITE_AUTOSENSE_2)
			{
				// use the channel number to decide
				int moteType = ChannelToSensorMapping.getMoteTypeAutosense2(chanID);
				if(moteType == MoteType)
					myPacket = true;
				
			}
			if(myPacket)
			{ 
				
				// this packet actually belongs to this mote 
				// so start processing
				
				//last sample number
				int lastSampleNumber = toip.getLastSample();
				
				// the data
				int[] data = toip.getData();
				
				// create a timestamps array;
				long[] timestamps;
				
				int index = sensorIndex.get(chanID);
				
				// get the sensor id
				int sensorID = sensorMap[index][1];
				
				if(Log.DEBUG)
				{
					Log.d(TAG+".onReceiveMotePacket()","chanID = "+Integer.toString(chanID) + " " + Constants.getMoteSensorDescriptions(MoteType, chanID) );
					Log.d(TAG+".onReceiveMotePacket()","sensorID = "+Integer.toString(sensorID) + " " + Constants.getSensorDescription(sensorID) );
				}
				
				// get the previously stored last Sample Number for this channel
				int lastReceivedSampleNumber = packetLoss[index][COLUMN_LAST_RECEIVED_SAMPLE_NUMBER];
				
				// get the next expected Sample Number for this channel
				int nextExpectedSampleNumber = packetLoss[index][COLUMN_NEXT_EXPECTED_SAMPLE_NUMBER];
				
				// get the total number of Packet received for this sensor
				int numberOfPacketsReceived = packetLoss[index][COLUMN_NUMBER_OF_PACKETS_RECEIVED];
				
				if(Log.DEBUG)
				{
					Log.d(TAG+".onReceiveMotePacket()", "lastReceivedSampleNumber = "+Integer.toString(lastReceivedSampleNumber));
					Log.d(TAG+".onReceiveMotePacket()","nextExpectedSampleNumber = "+Integer.toString(nextExpectedSampleNumber));
				}
				
				// no packet yet received
				if(receivedPacketCounter == 0)
				{
				//set the mote id
				MoteID = moteID;
				
//				packetLoss[index][COLUMN_LAST_RECEIVED_SAMPLE_NUMBER] = lastSampleNumber;
//				packetLoss[index][COLUMN_NEXT_EXPECTED_SAMPLE_NUMBER] = lastSampleNumber + SAMPLES_IN_PACKET;
//				packetLoss[index][COLUMN_NO_OF_PACKETS_LOST] = 0;
//				
				// start time stamping
				for(int i=0; i < NumberOfMoteSensors; i++)
					timeStamps[i] = System.currentTimeMillis();
				
				// update the received packet counter
				receivedPacketCounter++;
				
				}
				
				if(numberOfPacketsReceived == 0)
				{
					// Init the packet loss matrix for this particular sensor
					packetLoss[index][COLUMN_LAST_RECEIVED_SAMPLE_NUMBER] = lastSampleNumber;
					packetLoss[index][COLUMN_NEXT_EXPECTED_SAMPLE_NUMBER] = lastSampleNumber + SAMPLES_IN_PACKET;
					packetLoss[index][COLUMN_NO_OF_PACKETS_LOST] = 0;
					packetLoss[index][COLUMN_NUMBER_OF_PACKETS_RECEIVED]++;
					
					// fill the timestamps array
					timestamps = TimeStamping.timestampCalculator(timeStamps[index] , sensorID);
					timeStamps[index] = timestamps[timestamps.length - 1];
				
					if(Log.DEBUG)
					{
						Log.d(TAG+".norpc0", " TimeStamp for Channel "+Integer.toString(chanID)+" = " + Long.toString(timeStamps[index]));
					}
								
					// send the packet to the mote sensor manager
					MoteSensorManager.getInstance().updateSensor(data, sensorID, timestamps, lastSampleNumber);
							
					// return here
					return;
				
				}
				
				// this is not the first packet for this sensor
				else if(numberOfPacketsReceived > 0)
				{
					// is there a packet loss
					int numberOfPacketsLost = packetLoss(lastSampleNumber, lastReceivedSampleNumber, nextExpectedSampleNumber);
					
					if(Log.DEBUG)
						Log.d(TAG+".norpcgt0","Chan "+chanID+" "+Integer.toString(numberOfPacketsLost)+" packets lost");
					
					// just check for packet loss now
					if(numberOfPacketsLost > 0)
					{
						// update the channels packet loss matrix
						packetLoss[index][COLUMN_NO_OF_PACKETS_LOST] = packetLoss[index][COLUMN_NO_OF_PACKETS_LOST] + numberOfPacketsLost;
						packetLoss[index][COLUMN_LAST_RECEIVED_SAMPLE_NUMBER] = lastSampleNumber;
						packetLoss[index][COLUMN_NEXT_EXPECTED_SAMPLE_NUMBER] = lastSampleNumber + SAMPLES_IN_PACKET;
																	
						// request null packets for this sensor
						int NullPacketToken = PacketLoss.getMissingToken(sensorID);
						for(int i=0;i < numberOfPacketsLost; i++)
						{
							int[] nullData = TOSOscopeIntPacket.generateNullData(NullPacketToken);
							
							//update the time stamps
							timestamps = TimeStamping.timestampCalculator(timeStamps[index], sensorID);
							timeStamps[index] = timestamps[timestamps.length - 1];
							
							if(Log.DEBUG)
								Log.d(TAG+".packetLost ","generating null packet for channel = "+chanID);
							
							// send the packet to the mote sensor manager
							MoteSensorManager.getInstance().updateSensor(nullData, sensorID, timestamps, lastSampleNumber);
																	
						}
						
						// added the null packets
						// now add the packet that was received now
						//update the time stamps
						timestamps = TimeStamping.timestampCalculator(timeStamps[index], sensorID);
						timeStamps[index] = timestamps[timestamps.length - 1];
						
						if(Log.DEBUG)
						{
							Log.d(TAG+".packetLost", " TimeStamp for Channel "+Integer.toString(chanID)+" = " + Long.toString(timeStamps[index]));
						}
						
						// update the received packet counter
						receivedPacketCounter++;
						
						// send the packet to the mote sensor manager
						MoteSensorManager.getInstance().updateSensor(data, sensorID, timestamps, lastSampleNumber);
										
						// return here
						return;
						
					} // if	packet loss
					
					else if(numberOfPacketsLost == 0 )
					{
						// no packetLoss and no wrap around
						packetLoss[index][COLUMN_LAST_RECEIVED_SAMPLE_NUMBER] = lastSampleNumber;
						packetLoss[index][COLUMN_NEXT_EXPECTED_SAMPLE_NUMBER] = lastSampleNumber + SAMPLES_IN_PACKET;
						// packetLoss[index][COLUMN_NO_OF_PACKETS_LOST] = 0;
						packetLoss[index][COLUMN_NUMBER_OF_PACKETS_RECEIVED]++;
						
						// time stamp the packet
						timestamps = TimeStamping.timestampCalculator(timeStamps[index], sensorID);
						timeStamps[index] = timestamps[timestamps.length - 1];
						
						if(Log.DEBUG)
						{
							Log.d(TAG+".noPacketLost", "TimeStamp for Channel "+Integer.toString(chanID)+" = " + Long.toString(timeStamps[index]));
						}
											
						// update the received packet counter
						receivedPacketCounter++;
						
						// send the packet to the mote sensor manager
						MoteSensorManager.getInstance().updateSensor(data, sensorID, timestamps, lastSampleNumber);
															
						// return here
						return;
						
					}
					
					else if (numberOfPacketsLost == WRAP_AROUND_HAPPENED )
					{
						// a wraparound is expected
						// so reset this channels packetloss 
						if(lastSampleNumber < nextExpectedSampleNumber)
						{
							// the wrap around happened
							packetLoss[index][COLUMN_LAST_RECEIVED_SAMPLE_NUMBER] = lastSampleNumber;
							packetLoss[index][COLUMN_NEXT_EXPECTED_SAMPLE_NUMBER] = lastSampleNumber + SAMPLES_IN_PACKET;
							//packetLoss[index][COLUMN_NO_OF_PACKETS_LOST] = 0;
							packetLoss[index][COLUMN_NUMBER_OF_PACKETS_RECEIVED]++;
							
							// time stamp the packet
							timestamps = TimeStamping.timestampCalculator(timeStamps[index], sensorID);
							timeStamps[index] = timestamps[timestamps.length - 1];
							
							// update the received packet counter
							receivedPacketCounter++;
							
							if(Log.DEBUG)
								Log.d(TAG+".wrapAround","Channel "+chanID+" lastRecvdSampleNo = "+lastReceivedSampleNumber+" lastSampleNo = "+lastSampleNumber);
							
							// send the packet to the mote sensor manager
							MoteSensorManager.getInstance().updateSensor(data, sensorID, timestamps, lastSampleNumber);
																	
							// return here
							return;
						
						}					
					}
					else if(numberOfPacketsLost == RESET_HAPPENED )
					{
						// the wrap around clause failed
						// chances are the mote must have reset for some reason
						
						
						packetLoss[index][COLUMN_LAST_RECEIVED_SAMPLE_NUMBER] = lastSampleNumber;
						packetLoss[index][COLUMN_NEXT_EXPECTED_SAMPLE_NUMBER] = lastSampleNumber + SAMPLES_IN_PACKET;
						packetLoss[index][COLUMN_NO_OF_PACKETS_LOST] = 0;
						
						// can't estimate how many packets we lost here easily 
						// so just reset everything
						// ideally we should estimate the number of packets lost here
						// by the last timestamp and the time expired
						
						// start time stamping
						for(int i=0; i < NumberOfMoteSensors; i++)
							timeStamps[i] = System.currentTimeMillis();
						
						// fill the timestamps array
						timestamps = TimeStamping.timestampCalculator(timeStamps[index] , sensorID);
						timeStamps[index] = timestamps[timestamps.length - 1];
												
						// update the received packet counter
						receivedPacketCounter++;
						
						if(Log.DEBUG)
							Log.d(TAG+".reset","Channel "+chanID+" lastRecvdSampleNo = "+lastReceivedSampleNumber+" lastSampleNo = "+lastSampleNumber);
						
						// send the packet to the mote sensor manager
						MoteSensorManager.getInstance().updateSensor(data, sensorID, timestamps, lastSampleNumber);
									
						return;
					}
					
					// end else if s here 
					
					
				
				} // else if wrap around
			} // if my packet
		}// end synchronized
	} // method end

	
	
	private int packetLoss(int lastSampleNumber, int lastReceivedSampleNumber, int nextExpectedSampleNumber)
	{
		int packetsLost = -1;
		
		if(nextExpectedSampleNumber <= WRAP_AROUND_THRESHOLD)
		{		
			if(lastSampleNumber == nextExpectedSampleNumber)
			{
				//  correct case = no packets lost
				packetsLost = 0;
			}
			else if(lastSampleNumber > nextExpectedSampleNumber && lastReceivedSampleNumber > lastSampleNumber)
			{
				// packets were lost
				packetsLost = (lastReceivedSampleNumber - lastSampleNumber) % 50;
			}
			else if(lastSampleNumber < lastReceivedSampleNumber)
			{
				// out of order
				// reset must have happened
				packetsLost = RESET_HAPPENED;
			}
		}
		else
		{
				packetsLost = WRAP_AROUND_HAPPENED;
		}
		
		if(Log.DEBUG)
			Log.d("GASM.packetLoss","packetLost = "+packetsLost);
		
		return packetsLost;
	}

	@Override
	public void finalize() {
		// TODO Auto-generated method stub
		
	}

	//@Override
	public void onNullPacketRequest(int moteType, int SensorID,
			int numberOfPackets) {
		if(moteType == MoteType)
		{
			for(int i=0; i < numberOfPackets; i++)
			{
				TOSOscopeIntPacket toip;
				int missingToken = PacketLoss.getMissingToken(SensorID);
				int moteID = moteType;
				int chanID = ChannelToSensorMapping.mapPhoneSensorToMoteChannel(SensorID);
				int index = sensorIndex.get(chanID);
				int lastSampleNumber = packetLoss[index][COLUMN_NEXT_EXPECTED_SAMPLE_NUMBER];
				toip = TOSOscopeIntPacket.generateNullPacket( missingToken, moteID, chanID, lastSampleNumber );
				if(Log.DEBUG)
				{
					Log.d("GenericAutoSenseMote.onNullPacketRequest","generating null packet for sensor "+SensorID+" with null token "+missingToken);
				}
				onReceiveMotePacket(toip);
			}
		}
		return;
	}
	
		
}
