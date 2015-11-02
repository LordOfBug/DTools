package si.matjazcerkvenik.dtools.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.xml.bind.annotation.XmlTransient;

import si.matjazcerkvenik.dtools.tools.snmp.SnmpAgent;
import si.matjazcerkvenik.dtools.tools.snmp.SnmpSimulator;
import si.matjazcerkvenik.dtools.tools.snmp.SnmpTable;
import si.matjazcerkvenik.dtools.xml.DAO;

@ManagedBean
@ViewScoped
public class SnmpTableBean implements Serializable {
	
	private static final long serialVersionUID = 1388947034491411923L;
	
	private SnmpAgent agent;
	private SnmpTable table;
	
	@PostConstruct
	public void init() {
		
		Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		if (requestParameterMap.containsKey("agent")) {
			String name = requestParameterMap.get("agent");
			SnmpSimulator sim = DAO.getInstance().loadSnmpSimulator();
			for (SnmpAgent a : sim.getSnmpAgentsList()) {
				if (a.getName().equals(name)) {
					agent = a;
					break;
				}
			}
		}
		if (requestParameterMap.containsKey("snmpTableName")) {
			String name = requestParameterMap.get("snmpTableName");
			for (SnmpTable t : agent.getSnmpTablesList()) {
				if (t.getName().equals(name)) {
					table = t;
					table.setAgent(agent);
					break;
				}
			}
		}
		
		populateColumns();
		
	}

	public SnmpAgent getAgent() {
		return agent;
	}

	public void setAgent(SnmpAgent agent) {
		this.agent = agent;
	}

	public SnmpTable getTable() {
		return table;
	}

	public void setTable(SnmpTable table) {
		this.table = table;
	}
	
	
	
	
	
	private List<String> columns = new ArrayList<String>();
	private List<Map<String, Object>> rows = new ArrayList<Map<String,Object>>();
	
	
	public void populateColumns() {
		
		for (int i = 0; i < table.getMetadata().getColumnsMetaList().size(); i++) {
			columns.add(table.getMetadata().getColumnsMetaList().get(i).getName());
		}
		
		for (int i = 0; i < table.getRowsList().size(); i++) {
			
			Map<String, Object> m = new HashMap<String, Object>();
			for (int j = 0; j < table.getMetadata().getColumnsMetaList().size(); j++) {
				m.put(table.getMetadata().getColumnsMetaList().get(j).getName(), table.getRowsList().get(i).getValuesList().get(j));
			}
			rows.add(m);
			
		}
		
	}

	public List<String> getColumns() {
		return columns;
	}

	@XmlTransient
	public void setColumns(List<String> columns) {
		this.columns = columns;
	}

	public List<Map<String, Object>> getRows() {
		return rows;
	}

	@XmlTransient
	public void setRows(List<Map<String, Object>> rows) {
		this.rows = rows;
	}

}