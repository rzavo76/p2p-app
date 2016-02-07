import java.text.*;
import java.util.*;
import java.io.*;

public class BTLogger { 
// simpleton logging class that a peer shares among its threads
// writes to a file with the desired message
	private static Calendar cal;
    private static SimpleDateFormat sdf;
    private static BTLogger instance = null;
    private BTLogger() {}
    private void writeToFile(String filename, String logString) {
    	try {
    	File log = new File(filename);
    	if(!log.exists()) { //make log file if it doesn't exist
    		log.createNewFile();
    	}
	    	FileOutputStream toLog = new FileOutputStream(log, true); //append to end of file
	    	toLog.write(logString.getBytes());
	    	toLog.flush();
	    	toLog.close();
    	} catch(IOException ie) {
    		System.err.println("Failed to write to log file.");
    	}
    }
    public static synchronized BTLogger getInstance() {
    	if(instance == null) {
    		instance = new BTLogger();
    		sdf = new SimpleDateFormat("HH:mm:ss"); //format hour, minute, second
    	}
    	return instance;
    }
	public void TCPConnect(String filename, String p1id, String p2id) {
		cal = Calendar.getInstance(); //get time object
		writeToFile(filename, String.format("%s: Peer %s makes a connection to Peer %s.\n", 
			sdf.format(cal.getTime()), p1id, p2id));
	}
}