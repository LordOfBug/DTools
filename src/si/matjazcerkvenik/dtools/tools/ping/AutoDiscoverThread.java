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

package si.matjazcerkvenik.dtools.tools.ping;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import si.matjazcerkvenik.dtools.context.DProps;
import si.matjazcerkvenik.dtools.context.DToolsContext;
import si.matjazcerkvenik.dtools.io.DAO;
import si.matjazcerkvenik.dtools.xml.Node;

public class AutoDiscoverThread extends Thread {
	
	private ExecutorService executor;
	
	private int[] fromIpArray;
	private int[] toIpArray;
	
	/** Number of currently active workers */
	private int activeWorkersCount = 0;
	
	/** Total counter */
	private int totalCount = 0;
	
	/** Number of discovered nodes */
	private int discoveredNodesCount = 0;
	
	private boolean running = false;
	
	public AutoDiscoverThread() {
				
		int poolSize = DProps.getPropertyInt(DProps.AUTO_DISCOVERY_THREAD_POOL_SIZE);
		executor = Executors.newFixedThreadPool(poolSize);
	}
	
	@Override
	public void run() {
		
		DToolsContext.getInstance().getLogger().info("AutoDiscoverThread:: started");
		
		running = true;
		activeWorkersCount = 0;
		totalCount = 0;
		discoveredNodesCount = 0;
		
		while (!(fromIpArray[0] == toIpArray[0] && fromIpArray[1] == toIpArray[1] 
				&& fromIpArray[2] == toIpArray[2] && fromIpArray[3] == toIpArray[3])) {
			
			String ipAddr = fromIpArray[0] + "." + fromIpArray[1] + "."
					+ fromIpArray[2] + "." + fromIpArray[3];
			
			if (!ipExists(ipAddr)) {
				Runnable worker = new AutoDiscoverWorker(totalCount, ipAddr, this);
				try {
					executor.execute(worker);
					activeWorkersCount++;
					DToolsContext.getInstance().getLogger().info("AutoDiscoverThread:: ping " + ipAddr);
				} catch (RejectedExecutionException e) {
					DToolsContext.getInstance().getLogger().warn("AutoDiscoverThread:: terminated");
					return;
				}
			}
			totalCount++;
			
			fromIpArray[3]++;
			if (fromIpArray[3] == 256) {
				fromIpArray[3] = 0;
				fromIpArray[2]++;
			}
			
			if (fromIpArray[2] == 256) {
				fromIpArray[2] = 0;
				fromIpArray[1]++;
			}
			
			if (fromIpArray[1] == 256) {
				fromIpArray[1] = 0;
				fromIpArray[0]++;
			}
			
			try {
				Thread.sleep(determineDelay());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
				
		while (activeWorkersCount > 0) {
//			System.out.println("count: " + count);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
			
		}
		
		executor.shutdown();
		executor = null;
		running = false;
		
		DToolsContext.getInstance().getLogger().info("AutoDiscoverThread:: finished");
		
	}
	
	public void startAutoDiscover(String startIp, String endIp) {
		
		fromIpArray = convertIpAddressToIntArray(startIp);
		toIpArray = convertIpAddressToIntArray(endIp);
		
		this.start();
		
	}
	
	public static void main(String[] args) {
		String sip = "192.168.0.15";
		String eip = "192.168.5.20";
		
		AutoDiscoverThread adtp = new AutoDiscoverThread();
		
		long sipInt = adtp.getIpAsInt(sip);
		long eipInt = adtp.getIpAsInt(eip);
		
		if ((eipInt - sipInt) < 0) {
			System.out.println("Invalid range: " + (eipInt - sipInt));
			return;
		}
		
//		System.out.println("Number of IPs to scan: " + adtp.getNumberOfIpsInRange());
		
		adtp.startAutoDiscover(sip, eip);
	}
	
	public void stopAutoDiscover() {
		executor.shutdownNow();
		while (!executor.isTerminated()) {
			// wait
		}
		DToolsContext.getInstance().getLogger().info("AutoDiscoverThread:: stopped");
		running = false;
	}
	
//	public boolean isTerminated() {
//		return executor.isShutdown();
//	}
	
	public boolean isRunning() {
		return running;
	}

	/**
	 * Convert String IP address (x.x.x.x) to array of four integers ([x, x, x, x]).
	 * @param ip
	 * @return array
	 */
	private int[] convertIpAddressToIntArray(String ip) {
		
		int[] ipInt = new int[4];
		String[] temp = ip.split("\\.");
		
		for (int i = 0; i < temp.length; i++) {
			ipInt[i] = Integer.parseInt(temp[i]);
		}
		
		return ipInt;
		
	}
	
	
	
	private long getIpAsInt(String ip) {
		
		String[] temp = ip.split("\\.");
		
		if (temp[1].length() == 1) {
			temp[1] = "00" + temp[1];
		} else if (temp[1].length() == 2) {
			temp[1] = "0" + temp[1];
		}
		if (temp[2].length() == 1) {
			temp[2] = "00" + temp[2];
		} else if (temp[2].length() == 2) {
			temp[2] = "0" + temp[2];
		}
		if (temp[3].length() == 1) {
			temp[3] = "00" + temp[3];
		} else if (temp[3].length() == 2) {
			temp[3] = "0" + temp[3];
		}
		
		return Long.parseLong(temp[0] + temp[1] + temp[2] + temp[3]);
	}
	
	public boolean ipExists(String ip) {
		for (int i = 0; i < DAO.getInstance().loadNetworkNodes().getNodesList().size(); i++) {
			if (DAO.getInstance().loadNetworkNodes().getNodesList().get(i).getHostname().equals(ip)) {
				return true;
			}
		}
		return false;
	}
	
	public synchronized void storeNode(Node n) {
		DAO.getInstance().addNode(n);
		discoveredNodesCount++;
		DToolsContext.getInstance().getLogger().info("AutoDiscoverThread:addNode(): " + n.getHostname());
	}
	
	public synchronized void decreaseCount() {
		activeWorkersCount--;
	}
	
	public int determineDelay() {
		int delay = 30;
		delay = 5 * activeWorkersCount;
//		System.out.println("delay " + delay);
		return delay;
	}

	public int getActiveWorkersCount() {
		return activeWorkersCount;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public int getDiscoveredNodesCount() {
		return discoveredNodesCount;
	}
	
	
	
}
