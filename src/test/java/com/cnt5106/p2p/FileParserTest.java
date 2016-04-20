package com.cnt5106.p2p;

import com.cnt5106.p2p.models.RemotePeerInfo;
import org.junit.Test;
import org.junit.Assert;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;

public class FileParserTest
{
    private final static String FILE_NAME   = "TheFile.dat";
    private final static String TEST_DIR    = "src/test/java/com/cnt5106/p2p/";
    FileParser fileParser = FileParser.getInstance();
    @Test
    public void parsesConfigFile()
    {
        try
        {
            Path p = FileSystems.getDefault().getPath(TEST_DIR + "Common.cfg");
            fileParser.parseConfigFile(p);
            Assert.assertEquals(2, fileParser.getNumPreferredNeighbors());
            Assert.assertEquals(5, fileParser.getUnchokeInterval());
            Assert.assertEquals(15, fileParser.getOptUnchokeInterval());
            Assert.assertEquals(FILE_NAME, fileParser.getFileName());
            Assert.assertEquals(10000232, fileParser.getFileSize());
            Assert.assertEquals(32768, fileParser.getPieceSize());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


    }

    @Test
    public void parsesPeerFile()
    {
        ArrayList<RemotePeerInfo> targetPeers = new ArrayList<>();
        targetPeers.add(new RemotePeerInfo(1001, "lin114-00.cise.ufl.edu", 6008, true));
        targetPeers.add(new RemotePeerInfo(1002, "lin114-01.cise.ufl.edu", 6008, false));
        targetPeers.add(new RemotePeerInfo(1003, "lin114-02.cise.ufl.edu", 6008, false));
        targetPeers.add(new RemotePeerInfo(1004, "lin114-03.cise.ufl.edu", 6008, false));
        targetPeers.add(new RemotePeerInfo(1005, "lin114-04.cise.ufl.edu", 6008, false));
        targetPeers.add(new RemotePeerInfo(1006, "lin114-05.cise.ufl.edu", 6008, false));
        try
        {
            Path path = FileSystems.getDefault().getPath(TEST_DIR + "PeerInfo.cfg");
            ArrayList<RemotePeerInfo> peers = fileParser.getPeersFromFile(path);
            for (RemotePeerInfo p : peers)
            {
                // Remove comparing peer from the front of the array list
                RemotePeerInfo pComp = targetPeers.remove(0);
                Assert.assertEquals(pComp.peerId, p.peerId);
                Assert.assertEquals(pComp.peerAddress, p.peerAddress);
                Assert.assertEquals(pComp.peerPort, p.peerPort);
                Assert.assertEquals(pComp.hasFile, p.hasFile);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
