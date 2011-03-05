package org.fieldstream.service.sensors.mote;

import org.fieldstream.service.sensors.mote.tinyos.TOSOscopeIntPacket;

public interface MoteReceiverInterface {
	public void onReceiveMotePacket(TOSOscopeIntPacket toip);
	public void onNullPacketRequest(int moteType, int SensorID, int numberOfPackets);
}
