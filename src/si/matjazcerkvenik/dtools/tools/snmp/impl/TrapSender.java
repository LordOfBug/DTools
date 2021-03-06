/* 
 * Copyright (C) 2015 Matjaz Cerkvenik
 * 
 * DTools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DTools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with DTools. If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package si.matjazcerkvenik.dtools.tools.snmp.impl;

import java.io.IOException;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import si.matjazcerkvenik.dtools.context.DToolsContext;
import si.matjazcerkvenik.dtools.tools.snmp.SnmpTrap;
import si.matjazcerkvenik.simplelogger.SimpleLogger;

public class TrapSender {
	
	private SimpleLogger logger;
	
	private String localIp;
	private int localPort;
	private Snmp snmp;
	
	private int counter = 0;
	
	public TrapSender() {
		logger = DToolsContext.getInstance().getLogger();
	}
	
	public TrapSender(String localIp, int localPort) {
		this.localIp = localIp;
		this.localPort  = localPort;
		logger = DToolsContext.getInstance().getLogger();
	}
	
	

	/**
	 * Start SNMP agent
	 * @param localIp
	 * @param localPort
	 */
	public boolean start() {
		
		boolean result = false;
		
		try {
			// Create Transport Mapping
			@SuppressWarnings("rawtypes")
			TransportMapping transport = new DefaultUdpTransportMapping();
			transport.listen();
			
			// Send the PDU
			snmp = new Snmp(transport);
			
			logger.info("SnmpTrapSender.start(): agent started on port " + localPort);
			
			result = true;
			
		} catch (IOException e) {
			logger.error("SnmpTrapSender.start(): IOException", e);
		}
		
		return result;
		
	}
	
	/**
	 * Stop SNMP agent
	 */
	public void stop() {
		try {
			snmp.close();
		} catch (IOException e) {
			logger.error("SnmpTrapSender.stop(): IOException", e);
		}
		logger.info("SnmpTrapSender.stop(): stop agent");
	}
	
	/**
	 * Create PDU and send the SNMP trap to manager at selected ip (hostname) and port.
	 * @param destIp
	 * @param destPort
	 * @param trap
	 */
	public void sendTrap(String destIp, int destPort, SnmpTrap trap) {
		
		// Create Target
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString(trap.getCommunity()));
		target.setAddress(new UdpAddress(destIp + "/" + destPort));
		target.setRetries(2);
		target.setTimeout(5000);
		
		if (trap.getVersion().equals("v1")) {
			
			target.setVersion(SnmpConstants.version1);
			
			// Create PDU for V1
			PDUv1 pdu = new PDUv1();
			pdu.setType(PDU.V1TRAP);
			pdu.setEnterprise(new OID(trap.getEnterpriseOid()));
//			pdu.setGenericTrap(PDUv1.ENTERPRISE_SPECIFIC);
			pdu.setGenericTrap(trap.getGenericTrap());
			pdu.setSpecificTrap(trap.getSpecificTrap());
			pdu.setAgentAddress(new IpAddress(trap.getSourceIp()));
			
			for (int i = 0; i < trap.getVarbind().size(); i++) {
				pdu.add(trap.getVarbind().get(i).getSnmp4jVarBind());
			}
			
			sendTrapV1(pdu, target);
						
		}
		
		if (trap.getVersion().equals("v2c")) {
			
			target.setVersion(SnmpConstants.version2c);
			
			// Create PDU for V2
			PDU pdu = new PDU();
			pdu.setType(PDU.NOTIFICATION);
			pdu.setRequestID(new Integer32(counter++));
			
			// variable binding for Enterprise Specific objects
			for (int i = 0; i < trap.getVarbind().size(); i++) {
				pdu.add(trap.getVarbind().get(i).getSnmp4jVarBind());
			}
			
			sendTrapV2C(pdu, target);
			
		}
		
		
		
	}

	/**
	 * This methods sends the V1 trap
	 */
	private void sendTrapV1(PDUv1 pdu, CommunityTarget target) {
		try {
			snmp.send(pdu, target);
			logger.info("SnmpTrapSender.sendTrapV1(): PDU = " + pdu.toString());
		} catch (Exception e) {
			logger.error("SnmpTrapSender.sendTrapV1(): Error sending V1 trap to " + target.getAddress(), e);
		}
	}
	
	/**
	 * This methods sends the V2C trap
	 */
	private void sendTrapV2C(PDU pdu, CommunityTarget target) {
		try {
			snmp.send(pdu, target);
			logger.info("SnmpTrapSender.sendTrapV2C(): PDU = " + pdu.toString());
		} catch (Exception e) {
			logger.error("SnmpTrapSender.sendTrapV2C(): Error sending V2C trap to " + target.getAddress(), e);
		}
	}

}
