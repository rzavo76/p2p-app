package com.cnt5106.p2p;

import com.cnt5106.p2p.models.Peer;
import org.junit.Test;
import org.junit.Assert;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;

public class FileParserTest
{
    FileParser fileParser = FileParser.getInstance();
    @Test
    public void parsesConfigFile()
    {
        try
        {
            Path p = FileSystems.getDefault().getPath("src", "test", "java", "com", "cnt5106", "p2p", "Common.cfg");
            fileParser.parseConfigFile(p);
            Assert.assertEquals(2, fileParser.getNumPreferredNeighbors());
            Assert.assertEquals(5, fileParser.getUnchokeInterval());
            Assert.assertEquals(15, fileParser.getOptUnchokeInterval());
            Assert.assertEquals("TheFile.dat", fileParser.getFileName());
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
        ArrayList<Peer> targetPeers = new ArrayList<>();
        targetPeers.add(new Peer(1001, "lin114-00.cise.ufl.edu", 6008, true));
        targetPeers.add(new Peer(1002, "lin114-01.cise.ufl.edu", 6008, false));
        targetPeers.add(new Peer(1003, "lin114-02.cise.ufl.edu", 6008, false));
        targetPeers.add(new Peer(1004, "lin114-03.cise.ufl.edu", 6008, false));
        targetPeers.add(new Peer(1005, "lin114-04.cise.ufl.edu", 6008, false));
        targetPeers.add(new Peer(1006, "lin114-05.cise.ufl.edu", 6008, false));
        try
        {
            Path path = FileSystems.getDefault().getPath("src", "test", "java", "com", "cnt5106", "p2p", "PeerInfo.cfg");
            ArrayList<Peer> peers = fileParser.getPeersFromFile(path);
            for (Peer p : peers)
            {
                // Remove comparing peer from the front of the array list
                Peer pComp = targetPeers.remove(0);
                Assert.assertEquals(pComp.getPID(), p.getPID());
                Assert.assertEquals(pComp.getHostName(), p.getHostName());
                Assert.assertEquals(pComp.getLPort(), p.getLPort());
                Assert.assertEquals(pComp.getHasFile(), p.getHasFile());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
