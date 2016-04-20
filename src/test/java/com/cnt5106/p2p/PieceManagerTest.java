package com.cnt5106.p2p;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Dylan Richardson on 4/20/2016.
 *
 * Tests to make sure the file is parsed as expected
 */
public class PieceManagerTest {

    private static final int FILE_SIZE = 8859; // bytes
    private static final int PIECE_SIZE = 30;
    private static final int PIECE_COUNT = 296;
    private static final int PID = 1000;
    private static final String FILE_NAME = "declaration_of_independence.dat";
    private static byte[] data;

    @Before
    public void initialize()
    {
        Path path = Paths.get(FILE_NAME);
        try {
            data = Files.readAllBytes(path);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void splitFileInto30AndConcatenateIntoOriginal()
    {
        try {
            PieceManager.setInstance(PIECE_COUNT, FILE_SIZE, PIECE_SIZE, PID, FILE_NAME);
            PieceManager pm = PieceManager.getInstance();
            pm.splitFile();
            pm.mergePieces();
            byte[] newData = Files.readAllBytes(Paths.get(FILE_NAME));
            Assert.assertArrayEquals(data, newData);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
