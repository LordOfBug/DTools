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

import java.io.Serializable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import si.matjazcerkvenik.dtools.context.DProps;
import si.matjazcerkvenik.dtools.context.DToolsContext;
import si.matjazcerkvenik.dtools.tools.NetworkLocation;

/**
 * This class schedules periodic ping requests (monitoring).
 * 
 * @author matjaz
 *
 */
public class PingScheduler implements Serializable {
	
	private static final long serialVersionUID = 2461535231866502644L;
	
	private ScheduledExecutorService scheduledThreadPool = null;
	private boolean isRunning = false;
	
	private NetworkLocation location;
	
	public PingScheduler(NetworkLocation location) {
		this.location = location;
	}
	
	public void startPingScheduler() {
		
		isRunning = true;
		
		int threadPoolSize = DProps.getPropertyInt(DProps.NETWORK_MONITORING_PING_POOL_SIZE);
		int interval = DProps.getPropertyInt(DProps.NETWORK_MONITORING_PING_INTERVAL);
		
		DToolsContext.getInstance().getLogger().info("PingScheduler:: monitoring started loc=" 
				+ location.getLocationName() + ", p=" + threadPoolSize + ", i=" + interval
				+ ", t=" + System.currentTimeMillis());
		
		if (scheduledThreadPool == null) {
			scheduledThreadPool = Executors.newScheduledThreadPool(threadPoolSize);
		}
				
		for (int i = 0; i < location.getNetworkNodes().getNodesList().size(); i++) {
			scheduledThreadPool.scheduleWithFixedDelay(location.getNetworkNodes().getNodesList().get(i), 0, interval, TimeUnit.SECONDS);
		}
		
	}
	
	public void stopPingScheduler() {
		scheduledThreadPool.shutdown();
		while (!scheduledThreadPool.isTerminated()) {
			
		}
		scheduledThreadPool = null;
		isRunning = false;
		DToolsContext.getInstance().getLogger().info("PingScheduler:: monitoring stopped loc=" 
				+ location.getLocationName() + ", t=" + System.currentTimeMillis());
	}

	public boolean isRunning() {
		return isRunning;
	}
	
}
