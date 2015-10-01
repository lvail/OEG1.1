package server;

import java.io.*;
import java.util.Properties;

public class SavedInformation {
	Properties savedInfo;

	/** Static method that creates and loads a Properties object
	 * from a file if it exists
	 * @param The name of the file to be loaded
	 * @return A new Properties object to be used to store state information.
	 */
	public static Properties getProperties(String fileName) {
		// create new Properties object
		Properties savedInfo = new Properties();
		FileInputStream in;
		
		fileName = "oeg_" + fileName + ".conf";
		
		File file = new File(fileName);
		//if file exists, load information into savedInfo
		if (file.exists()) {
			try {
				in = new FileInputStream(file);
				savedInfo.load(in);
				in.close();
				savedInfo.setProperty("loaded", "true");
			} catch (Exception e) {
				e.printStackTrace();
			}
		//else set property to false
		} else {
			savedInfo.setProperty("loaded", "false");
		}
		
		//return the Properties object
		return savedInfo;
	}

	/** Static method that saves the contents of a Properties
	 *  object to the file that is specified by the name key
	 * @param property The property object being saved.
	 */
	public static void saveProperties(Properties property) {
		FileOutputStream outFile;
		String fileName = "oeg_" + property.getProperty("name") + ".conf";
		try {
			outFile = new FileOutputStream(fileName);
			//saves the contents of property to out file
			property.store(outFile, "03G 1S 4W3S0M3");
			outFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
