package lando.nsf.app.towav;

import org.apache.commons.lang3.Validate;

/**
 * FIFO buffer of samples.
 */
public final class SampleRingBuffer {

    private final float[] samples;
    private int start;
    private float total;
    private float prevSample;
    
    public SampleRingBuffer(int size) {
        Validate.isTrue(size > 0);
        this.samples = new float[size];
        this.start = 0;
        this.total = 0;
        this.prevSample = 0;
    }
    
    int size() {
        return samples.length;
    }
    
    public void add(float newSample) {
        float evictedDiffSq = samples[start];
        float newDiffSq = newSample - prevSample;
        
        newDiffSq *= newDiffSq;
        
        samples[start++] = newDiffSq;
        
        if( start == samples.length ) {
            start = 0;
        }
        
        total += newDiffSq - evictedDiffSq;
        prevSample = newSample;
    }
    
    float diffSq() {
        return Math.abs(total/samples.length);
    }
}
