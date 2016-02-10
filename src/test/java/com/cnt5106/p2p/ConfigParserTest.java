package com.cnt5106.p2p;

import org.junit.Test;
import org.junit.Assert;

public class ConfigParserTest
{
    @Test
    public void parsesConfigFile()
    {
        try
        {
            ConfigParser.parseConfigFile("src/test/java/com/cnt5106/p2p/Common.cfg");
            Assert.assertEquals(2, ConfigParser.getNumPreferredNeighbors());
            Assert.assertEquals(5, ConfigParser.getUnchokeInterval());
            Assert.assertEquals(15, ConfigParser.getOptUnchokeInterval());
            Assert.assertEquals("TheFile.dat", ConfigParser.getFileName());
            Assert.assertEquals(10000232, ConfigParser.getFileSize());
            Assert.assertEquals(32768, ConfigParser.getPieceSize());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


    }
}
