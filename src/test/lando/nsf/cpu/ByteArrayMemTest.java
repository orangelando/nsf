package lando.nsf.cpu;

import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

import lando.nsf.cpu.ByteArrayMem;

public class ByteArrayMemTest {

    @Test
    public void test_random_reads_and_writes() {
        
        Random rnd = new Random();
        ByteArrayMem mem = new ByteArrayMem();
        
        for(int i = 0; i < 1000; i++) {
            byte data = (byte)rnd.nextInt();
            int addr = rnd.nextInt() & 0xFFFF;
            
            mem.write(addr, data);
            
            byte data2 = (byte)mem.read(addr);
            
            assertTrue( data + " != " + data2, data == data2 );
        }
    }
}
