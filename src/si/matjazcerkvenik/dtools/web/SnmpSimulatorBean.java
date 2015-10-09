package si.matjazcerkvenik.dtools.web;

import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import si.matjazcerkvenik.dtools.tools.localhost.LocalhostInfo;
import si.matjazcerkvenik.dtools.tools.snmp.SnmpAgent;
import si.matjazcerkvenik.dtools.xml.DAO;

@ManagedBean
@ApplicationScoped
public class SnmpSimulatorBean {
	
	/**
	 * Add new SNMP agent with default settings
	 */
	public void addNewSnmpAgent() {
		
		SnmpAgent a = new SnmpAgent("SnmpAgent" + DAO.getInstance().loadSnmpSimulator().getSnmpAgentsList().size(), 
				LocalhostInfo.getLocalIpAddress(), 6161);
		
		DAO.getInstance().addSnmpAgent(a);
		Growl.addGrowlMessage("Created SNMP agent: " + a.toString(), FacesMessage.SEVERITY_INFO);
		
	}
	
	public void deleteSnmpAgent(SnmpAgent a) {
		a.stop();
		DAO.getInstance().deleteSnmpAgent(a);
		Growl.addGrowlMessage(a.getName() + " deleted", FacesMessage.SEVERITY_INFO);
	}
	
	public List<SnmpAgent> getSnmpAgents() {
		return DAO.getInstance().loadSnmpSimulator().getSnmpAgentsList();
	}
	
	
	public void toggleSnmpAgent(SnmpAgent a) {
		
		if (a.isActive()) {
			boolean b = a.start();
			if (b) {
				Growl.addGrowlMessage("Start agent on port " + a.getLocalPort(), FacesMessage.SEVERITY_INFO);
			} else {
				Growl.addGrowlMessage("Error starting agent", FacesMessage.SEVERITY_WARN);
			}
		} else {
			// already listening
			a.stop();
			Growl.addGrowlMessage("Stop agent", FacesMessage.SEVERITY_INFO);
		}
	}
	
}
