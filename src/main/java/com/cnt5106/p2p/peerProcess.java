package com.cnt5106.p2p;

/**
 * Created by Dylan Richardson on 3/10/2016.
 *
 * A model class for containing information about peers
 */

import java.io.IOException;
import java.util.*;
import java.lang.*;

public class peerProcess {


    public static void main(String[] args) {
        // Read PeerInfo.cfg to match arg[0] to pID and hostname, lport, hasfile
        // Create two thread arrays to connect with other peers
        // Connect to previously made connections
        int pID = Integer.valueOf(args[0]);
        BTLogger.getInstance().setPid(pID);
        try 
        {
            ThreadManager.getInstance().createThreads(pID);
        }
        catch(Exception e) {
            try {
                BTLogger.getInstance().writeToLog(Arrays.toString(e.getStackTrace()));
            }
            catch (IOException ioe) {
                e.printStackTrace();
            }
        }
    }
}
