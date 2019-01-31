package lando.nsf.app.towav;

import org.apache.commons.lang3.Validate;

/**
 * FIFO buffer of samples.
 */
public final class SampleRingBuffer {

    public final float[] samples;
    public int next;
    
    public SampleRingBuffer(int size) {
        Validate.isTrue(size > 0);
        this.samples = new float[size];
        this.next = 0;
    }
    
    public int size() {
        return samples.length;
    }
    
    public void add(float newSample) {        
        samples[next++] = newSample;
        
        if( next == samples.length ) {
            next = 0;
        }
    }
}
