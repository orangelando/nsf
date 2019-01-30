package lando.nsf.app.towav;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SampleRingBufferTest {

    private static final float EPS = 0.001f;
    
    @Test
    public void test() {
        SampleRingBuffer buf = new SampleRingBuffer(3);
        
        buf.add(10);
        buf.add( 8);
        buf.add(20);
        buf.add(21);
        buf.add( 4);
        
        double diffSq = (sq(20 - 8) + sq(21 - 20) + sq(4 - 21))/3.0;

        assertEquals("Case 3", diffSq, buf.diffSq(), EPS);
    }
    
    private static double sq(double d) {
        return d*d;
    }
}
