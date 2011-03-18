package org.fieldstream.service.sensors.mote;

import java.util.HashMap;

import org.fieldstream.Constants;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensors.api.AbstractMote;
import org.fieldstream.service.sensors.mote.sensors.MoteSensorManager;
import org.fieldstream.service.sensors.mote.tinyos.TOSOscopeIntPacket;

public class GenericAutoSenseMote extends AbstractMote implements MoteReceiverInterface {
		
	/*
	 * 
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
	 *  column 3= number of packets lost
	 */
	private int[][] packetLoss;
	private long[] timeStamps;
	
	private int receivedPacketCounter;
	
	private final static int WRAP_AROUND_THRESHOLD = 65536;
	
	private final static int COLUMN_SENSOR_MOTE_CHANNEL = 0;
	private final static int COLUMN_LAST_RECEIVED_SAMPLE_NUMBER = 1;
	private final static int COLUMN_NEXT_EXPECTED_SAMPLE_NUMBER = 2;
	private final static int COLUMN_NO_OF_PACKETS_LOST = 3;
		
	private final static int SAMPLES_IN_PACKET = 50;
	private final static int PACKET_NOT_RECEIVED = -999;
		
	public GenericAutoSenseMote(int moteType)
	{
		super(moteType);
		initialize();
	}
	
	public void initialize() {
//		buffer = new TOSOscopeIntPacket[bufferLength];
		switch(MoteType)
		{
		case Constants.MOTE_TYPE_AUTOSENSE_1_ECG: 
			NumberOfMoteSensors = Constants.NO_OF_MOTE_SENSORS_AUTOSENSE_1_ECG;
			break;
			
		case Constants.MOTE_TYPE_AUTOSENSE_1_RIP:
			NumberOfMoteSensors = Constants.NO_OF_MOTE_SENSORS_AUTOSENSE_1_RIP;
			
		case Constants.MOTE_TYPE_AUTOSENSE_2_ECG_RIP:
			NumberOfMoteSensors = Constants.NO_OF_MOTE_SENSORS_AUTOSENSE_2_ECG_RIP;
			
		case Constants.MOTE_TYPE_AUTOSENSE_2_ALCOHOL:
			NumberOfMoteSensors = Constants.NO_OF_MOTE_SENSORS_AUTOSENSE_2_ALCOHOL;
		
		}
		
		// init the packet loss matrix
		packetLoss = new int[NumberOfMoteSensors][4];
		
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
			packetLoss[i][COLUMN_NO_OF_PACKETS_LOST] = 0;
			
			timeStamps[i] = PACKET_NOT_RECEIVED;
		}
	
		receivedPacketCounter = 0;
		
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

	@Override
	public void onReceiveMotePacket(TOSOscopeIntPacket toip) 
	{
		Log.d("GenericAutoSenseMote", "onReceiveMotePacket Called");
		synchronized(INSTANCE)
		{
			Log.d("GenericAutoSenseMote", "onReceiveMotePacket Executing after a wait");
			
			// mote id 
			int moteID = toip.getMoteID();
			
			// use the mapping rule
			// default mapping rule for motes is modulo division by 10
			boolean myPacket = (moteID%ChannelToSensorMapping.getMappingRuleBase() == ChannelToSensorMapping.mappingRule(MoteType));
			
			if(myPacket)
			{ 
				// this packet actually belongs to this mote 
				// so start processing
				
				//channel id 
				int chanID = toip.getChan();
				
				//last sample number
				int lastSampleNumber = toip.getLastSample();
				
				// the data
				int[] data = toip.getData();
				
				// create a timestamps array;
				long[] timestamps;
				
				int index = sensorIndex.get(chanID);
				
				// get the sensor id
				int sensorID = sensorMap[index][1];
				
				// get the previously stored last Sample Number for this channel
				int lastReceivedSampleNumber = packetLoss[index][COLUMN_LAST_RECEIVED_SAMPLE_NUMBER];
				
				// get the next expected Sample Number for this channel
				int nextExpectedSampleNumber = packetLoss[index][COLUMN_NEXT_EXPECTED_SAMPLE_NUMBER];
				
				// no packet yet received
				if(receivedPacketCounter == 0)
				{
				packetLoss[index][COLUMN_LAST_RECEIVED_SAMPLE_NUMBER] = lastSampleNumber;
				packetLoss[index][COLUMN_NEXT_EXPECTED_SAMPLE_NUMBER] = lastSampleNumber + SAMPLES_IN_PACKET;
				packetLoss[index][COLUMN_NO_OF_PACKETS_LOST] = 0;
				
				// start time stamping
				for(int i=0; i < NumberOfMoteSensors; i++)
					timeStamps[i] = System.currentTimeMillis();
				
				// fill the timestamps array
				timestamps = TimeStamping.timestampCalculator(timeStamps[index] , sensorID);
				timeStamps[index] = timestamps[timestamps.length - 1];
				
				
				// send the packet to the mote sensor manager
				MoteSensorManager.getInstance().updateSensor(data, sensorID, timestamps);
				
				// update the received packet counter
				receivedPacketCounter++;
				
				// return here
				return;
				
				}
				
				// this is not the first packet
				else if(receivedPacketCounter > 0)
				{
					// is there a packet loss
					int numberOfPacketsLost = packetLoss(lastSampleNumber, lastReceivedSampleNumber, nextExpectedSampleNumber);
									
					if(lastSampleNumber == nextExpectedSampleNumber )
					{
						// no packetLoss and no wrap around
						packetLoss[index][COLUMN_LAST_RECEIVED_SAMPLE_NUMBER] = lastSampleNumber;
						packetLoss[index][COLUMN_NEXT_EXPECTED_SAMPLE_NUMBER] = lastSampleNumber + SAMPLES_IN_PACKET;
						packetLoss[index][COLUMN_NO_OF_PACKETS_LOST] = 0;
						
						// time stamp the packet
						timestamps = TimeStamping.timestampCalculator(timeStamps[index], sensorID);
						timeStamps[index] = timestamps[timestamps.length - 1];
						
						// send the packet to the mote sensor manager
						MoteSensorManager.getInstance().updateSensor(data, sensorID, timestamps);
						
						// update the received packet counter
						receivedPacketCounter++;
											
						// return here
						return;
						
					}
					
					else if (wrapAround(nextExpectedSampleNumber))
					{
						// a wraparound is expected
						// so reset this channels packetloss 
						if(lastSampleNumber < nextExpectedSampleNumber)
						{
							// the wrap around happened
							packetLoss[index][COLUMN_LAST_RECEIVED_SAMPLE_NUMBER] = lastSampleNumber;
							packetLoss[index][COLUMN_NEXT_EXPECTED_SAMPLE_NUMBER] = lastSampleNumber + SAMPLES_IN_PACKET;
							packetLoss[index][COLUMN_NO_OF_PACKETS_LOST] = 0;
							
							// time stamp the packet
							timestamps = TimeStamping.timestampCalculator(timeStamps[index], sensorID);
							timeStamps[index] = timestamps[timestamps.length - 1];
							
							// send the packet to the mote sensor manager
							MoteSensorManager.getInstance().updateSensor(data, sensorID, timestamps);
							
							// update the received packet counter
							receivedPacketCounter++;
													
							// return here
							return;
						
						}					
					}
					else if(lastSampleNumber < nextExpectedSampleNumber)
					{
						// the wrap around clause failed
						// this is a wierd
						// out of sequence packet
						
						// don't add this packet
						
						return;
					}
					
					// end else if s here 
					
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
							
							// send the packet to the mote sensor manager
							MoteSensorManager.getInstance().updateSensor(nullData, sensorID, timestamps);
							
							// update the received packet counter
							receivedPacketCounter++;
											
						}
						
						// added the null packets
						// now add the packet that was received now
						//update the time stamps
						timestamps = TimeStamping.timestampCalculator(timeStamps[index], sensorID);
						timeStamps[index] = timestamps[timestamps.length - 1];
						
						// send the packet to the mote sensor manager
						MoteSensorManager.getInstance().updateSensor(data, sensorID, timestamps);
					
						
						// return here
						return;
						
					} // if	packet loss
				
				} // else if wrap around
			} // if my packet
		}// end sunchronized
	} // method end

	private boolean wrapAround(int nextExpectedSampleNumber) 
	{
		boolean mWrapAround = false;
		
		if(nextExpectedSampleNumber > WRAP_AROUND_THRESHOLD)
			mWrapAround = true;
		
		return mWrapAround;
	}
	
	private int packetLoss(int lastSampleNumber, int lastReceivedSampleNumber, int nextExpectedSampleNumber)
	{
		int packetsLost = -1;
		
		if(lastSampleNumber == nextExpectedSampleNumber)
		{
			//  correct case = no packets lost
			packetsLost = 0;
		}
		else if(lastSampleNumber > nextExpectedSampleNumber)
		{
			// packets were lost
			packetsLost = (lastReceivedSampleNumber - lastSampleNumber) % 50;
		}
		else
		{
			// out of order
			packetsLost = -2;
		}
		
		return packetsLost;
	}

	@Override
	public void finalize() {
		// TODO Auto-generated method stub
		
	}

	@Override
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
				int lastSampleNumber = packetLoss[index][COLUMN_LAST_RECEIVED_SAMPLE_NUMBER];
				toip = TOSOscopeIntPacket.generateNullPacket( missingToken, moteID, chanID, lastSampleNumber );
				onReceiveMotePacket(toip);
			}
		}
		return;
	}
	
		
}
