package si.matjazcerkvenik.dtools.xml;

import javax.xml.bind.annotation.XmlElement;

public class SnmpClient {

	private String name;
	private String hostname;
	private int port;
	private String snmpVersion;
	private String community;
	private boolean favorite = false;

	public String getName() {
		return name;
	}

	@XmlElement
	public void setName(String name) {
		this.name = name;
	}

	public String getHostname() {
		return hostname;
	}

	@XmlElement
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return port;
	}

	@XmlElement
	public void setPort(int port) {
		this.port = port;
	}

	public String getSnmpVersion() {
		return snmpVersion;
	}

	public void setSnmpVersion(String snmpVersion) {
		this.snmpVersion = snmpVersion;
	}

	public String getCommunity() {
		return community;
	}

	@XmlElement
	public void setCommunity(String community) {
		this.community = community;
	}
	
	public boolean isFavorite() {
		return favorite;
	}

	@XmlElement
	public void setFavorite(boolean favorite) {
		this.favorite = favorite;
	}
	
	public String toUrlString() {
		return "SNMP-" + snmpVersion + " [" + community + "] " + hostname + ":" + port;
	}

}
