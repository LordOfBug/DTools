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

package si.matjazcerkvenik.dtools.update;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class is used to compare versions of local .war file and the one on 
 * the server and performs upgrade if user decides to upgrade to newer version.
 * 
 * @author matjaz
 *
 */
public class Update {
	
	public static String lastVersionUrl = "http://www.matjazcerkvenik.si/projects/dtools/getLastVersion.php";
	public static String installScriptUrl = "http://www.matjazcerkvenik.si/projects/download/DTools/$VERSION$/install-script.xml";
	public static String DTOOLS_HOME;
	public static String webappsDir = "/server/apache-tomcat-7.0.57/webapps";
	public static String warFile = "/server/apache-tomcat-7.0.57/webapps/DTools.war";
	public static String repositoryDir = "/repository";
	public static String versionTxt = "/config/version.txt";
	public static String currentVersion;
	public static String lastVersion;
	
	
	private static boolean debugMode = false;
	
	public static boolean errorOccured = false;
	
	public static void main(String[] args) {
		
		Update u = new Update();
		
		// set DTOOLS_HOME
		if (args.length > 0) {
			DTOOLS_HOME = args[0].substring(0, args[0].length() - 4);
		} else {
			DTOOLS_HOME = "/Users/matjaz/Desktop"; // set project dir as home dir
			installScriptUrl = installScriptUrl.replaceAll("install-script.xml", "install-script-test.xml"); // use test script
		}
		println("DTOOLS_HOME=" + DTOOLS_HOME);
		versionTxt = DTOOLS_HOME + versionTxt;
		webappsDir = DTOOLS_HOME + webappsDir; // used only for restore
		warFile = DTOOLS_HOME + warFile; // used only for restore
		repositoryDir = DTOOLS_HOME + repositoryDir; // used only for restore and show repo
		
		// check input arguments if any
		if (args.length > 1) {
			
			if (args[1].equalsIgnoreCase("-h") 
					|| args[0].equalsIgnoreCase("--help") 
					|| args[0].equalsIgnoreCase("help")
					|| args[0].equalsIgnoreCase("?")) {
				u.printHelp();
				System.exit(0);
			} else if (args[1].equalsIgnoreCase("-d")) {
				debugMode = true;
				println("Debug mode: ON");
			} else if (args[1].equalsIgnoreCase("-m")) {
				println("MD5[DTools.war]=" + MD5.getMd5(warFile));
				System.exit(0);
			} else if (args[1].equalsIgnoreCase("-s")) {
				u.showRepository();
				System.exit(0);
			} else if (args[1].equalsIgnoreCase("-v")) {
				u.getCurrentVersionFromTxt();
				u.getLastVersionFromTheServer();
				System.exit(0);
			} else if (args[1].equalsIgnoreCase("-r")) {
				if (args.length > 2) {
					u.restore(args[2]);
				} else {
					println("WARN: Missing version argument");
				}
				System.exit(0);
			} else {
				u.printHelp();
				System.exit(0);
			}
			
		}
		
		
		u.isTomcatRunning();
		
		// get versions
		currentVersion = u.getCurrentVersionFromTxt();
		lastVersion = u.getLastVersionFromTheServer();
		installScriptUrl = installScriptUrl.replace("$VERSION$", lastVersion);
		
		// check versions
		boolean proceed = u.isUpdateRequired(currentVersion, lastVersion);
		if (!proceed) {
			println("DTools is up to date");
			System.exit(0);
		}
		
		// ask user to proceed
		while (proceed) {
			
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Do you want to upgrade now? [y/n]: ");
	        try {
				String s = br.readLine();
				if (s.equalsIgnoreCase("n") || s.equalsIgnoreCase("no")) {
					println("Exit");
					System.exit(0);
				} else if (s.equalsIgnoreCase("y") || s.equalsIgnoreCase("yes")) {
					proceed = false;
				} else {
					println("Choose yes or no!");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// process script
		ScriptProcessor sProc = new ScriptProcessor();
		sProc.loadScript();
		sProc.processScript();
		
		if (errorOccured) {
			println("Update completed with errors. Please run again.");
		} else {
			u.updateVersion(lastVersion);
			println("Update complete: " + lastVersion);
		}
		
		
	}
	
	
	/**
	 * Show CLI help
	 */
	public void printHelp() {
		
		println("DTools update help");
		println("-h\tprint help");
		println("-m\tmd5 checksum");
		println("-d\tdebugger output");
		println("-r x.y.z\trestore version");
		println("-s\tshow respository");
		println("-v\tshow version");
		
	}
	
	
	/**
	 * Check weather Tomcat is running by opening connection to localhost:8080
	 * @return true if Tomcat is running
	 */
	public boolean isTomcatRunning() {
		try {
		    new URL("http://localhost:8080").openConnection().connect();
		    println("WARN: Please stop the server before updating!");
			System.exit(0);
		    return true;
		} catch (IOException e) {
		}
		return false;
	}
	
	
	/**
	 * Read version.txt file
	 * @return current version
	 */
	public String getCurrentVersionFromTxt() {

		String ver = "0.0.0";

		try {

			FileInputStream fis = new FileInputStream(versionTxt);
			DataInputStream dis = new DataInputStream(fis);
			BufferedReader br = new BufferedReader(new InputStreamReader(dis));
			ver = br.readLine().trim();
			dis.close();
			
			println("Current version: " + ver);

		} catch (Exception e) {
			e.printStackTrace();
			println("ERROR: cannot read " + versionTxt);
			System.exit(0);
		}

		return ver;

	}
	
	
	/**
	 * Send request to the mc.si server and read last version. Return 
	 * version in x.y.z format.
	 * @return version
	 */
	public String getLastVersionFromTheServer() {
		
		String lastVersion = "0.0.0";

		try {
			InputStream iStream = new URL(lastVersionUrl).openStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(iStream));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			lastVersion = response.toString().toString();

			// print result
			println("Last version: " + lastVersion);

		} catch (MalformedURLException e) {
			println("ERROR: MalformedURLException: cannot read version from the server");
			System.exit(0);
		} catch (IOException e) {
			println("ERROR: IOException: cannot retrieve data from the server");
			System.exit(0);
		}
		
		return lastVersion;

	}
	
	
	/**
	 * Convert version to integer:<br>
	 * 1. split version string by periods (.)<br>
	 * 2. make every version (major, minor, patch) 3-digit number by adding zeros at the beginning<br>
	 * 3. join the string and convert to integer<br><br>
	 * Example: version 1.20.333 (String) --&gt; 001020333 (String) --&gt; 1020333 (int)<br>
	 * With this principle there are possibilities to support 1000 major, 1000 minor and 1000 patch versions 
	 * for simple comparison of integers. 
	 * @param version
	 * @return version as integer
	 */
	public int convertToInteger(String version) {
		
		String[] tempVer = version.split("\\.");
		String convertString = tempVer[0];
		if (tempVer[1].length() == 1) {
			convertString += "00" + tempVer[1];
		} else if (tempVer[1].length() == 2) {
			convertString += "0" + tempVer[1];
		} else {
			convertString += tempVer[1];
		}
		if (tempVer[2].length() == 1) {
			convertString += "00" + tempVer[2];
		} else if (tempVer[2].length() == 2) {
			convertString += "0" + tempVer[2];
		} else {
			convertString += tempVer[2];
		}
		
		int v = Integer.parseInt(convertString);
		
		return v;
	}
	
	
	/**
	 * Parse the last version and compare it to installed version. Return true if 
	 * last version is higher than current version.
	 * @param currentVersion
	 * @param lastVersion
	 * @return true if update is required
	 */
	public boolean isUpdateRequired(String currentVersion, String lastVersion) {
		
		if (currentVersion.contains("alpha") || currentVersion.contains("beta")) {
			println("Cannot update developer version!");
			System.exit(0);
			return false;
		}
		
		int currVer = convertToInteger(currentVersion);
		int lastVer = convertToInteger(lastVersion);
		
		if (lastVer == currVer) {
			println("DTools is up to date");
			System.exit(0);
			return false;
		}
		
		if (lastVer > currVer) {
			return true;
		}
		
		return false;
		
	}
	
	
	
	/**
	 * Write last version to version.txt file.
	 */
	private void updateVersion(String version) {
		try {
			FileWriter fw = new FileWriter(versionTxt);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(version);
			bw.close();
		} catch (IOException e) {
			println("ERROR: IOException: cannot update version.txt");
		}
	}
	
	/**
	 * Restore selected version from repository. This method is used to downgrade 
	 * DTools to one of previous versions.
	 * @param version
	 */
	public void restore(String version) {
		
		File f = new File(repositoryDir + "/" + version + "/DTools.war");
		if (!f.exists()) {
			println("WARN: version " + version + " not found in repository");
			return;
		}
		
		ScriptProcessor sp = new ScriptProcessor();
		
		// delete work directories
		sp.delete(new File("server/apache-tomcat-7.0.57/work/Catalina/localhost/DTools"));
		sp.delete(new File("server/apache-tomcat-7.0.57/webapps/DTools"));
		
		// move old war to repository
		sp.move(warFile, repositoryDir + "/" + getCurrentVersionFromTxt());
		
		// move selected war to webapps
		sp.move(repositoryDir + "/" + version + "/DTools.war", webappsDir);
		
		updateVersion(version);
		System.out.println("Successfully restored version " + version);
		
	}
	
	public void showRepository() {
		
		println("Repository=" + repositoryDir);
		
		File dir = new File(repositoryDir);
		File[] files = dir.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		
		if (files == null || files.length == 0) {
			println("Repository is empty");
			return;
		}
		
		for (int i = 0; i < files.length; i++) {
			println(files[i].getName());
		}
		
	}
	
	
	public static void print(String s) {
		if (!debugMode) {
			System.out.print(s);
		}
	}
	
	public static void println(String s) {
		System.out.println(s);
	}
	
	public static void debug(String s) {
		if (debugMode) {
			System.out.println(s);
		}
	}
	
}
