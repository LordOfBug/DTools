package si.matjazcerkvenik.dtools.tools.snmp;

import java.io.IOException;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

// http://www.jayway.com/2010/05/21/introduction-to-snmp4j/

// snmpget -v 2c -c public 192.168.1.100:6161 -O en 1.3.6.1.2.1.2.2.1.6.1
// snmpwalk -v 2c -c public 192.168.1.100:6161 -O en 1.3.6.1.2.1

public class SimpleSnmpManager {
	
	private Snmp snmp = null;
	private String address = null;

	public SimpleSnmpManager(String address, String port) {
		this.address = "udp:" + address + "/" + port;
		try {
			start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public static void main(String[] args) throws IOException {
		
//		SimpleSnmpManager client = new SimpleSnmpManager("udp:127.0.0.1/6161"); // localhost
//		SimpleSnmpManager client = new SimpleSnmpManager("udp:192.168.1.100/6161"); // iMac
		SimpleSnmpManager client = new SimpleSnmpManager("192.168.1.110", "161"); // CentOS (first start snmpd)
		String sysDescr = client.getAsString(".1.3.6.1.2.1.1.1.0");
		System.out.println(sysDescr);
		String ifDesc1 = client.getAsString(".1.3.6.1.2.1.2.2.1");
		System.out.println(ifDesc1);
		String ifDesc2 = client.getAsString(".1.3.6.1.2.1.2.2.1.6.1");
		System.out.println(ifDesc2);
		String ifDesc3 = client.getAsString(".1.3.6.1.2.1.2.2.1");
		System.out.println(ifDesc3);
		
	}
	
	
	

	private void start() throws IOException {
		TransportMapping<?> transport = new DefaultUdpTransportMapping();
		snmp = new Snmp(transport);
		// Do not forget this line!
		transport.listen();
	}
	
	public String getAsString(String o) {
		return getAsString(new OID(o));
		
	}

	public String getAsString(OID oid) {
		try {
			ResponseEvent event = get(new OID[] { oid });
			return event.getResponse().get(0).getVariable().toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "snmp get failed";
	}

	private ResponseEvent get(OID oids[]) throws IOException {
		PDU pdu = new PDU();
		for (OID oid : oids) {
			pdu.add(new VariableBinding(oid));
		}
		pdu.setType(PDU.GET);
		ResponseEvent event = snmp.send(pdu, getTarget(), null);
		if (event != null) {
			return event;
		}
		throw new RuntimeException("GET timed out");
	}

	private Target getTarget() {
		Address targetAddress = GenericAddress.parse(address);
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString("public"));
		target.setAddress(targetAddress);
		target.setRetries(2);
		target.setTimeout(5000);
		target.setVersion(SnmpConstants.version2c);
		return target;
	}

}
