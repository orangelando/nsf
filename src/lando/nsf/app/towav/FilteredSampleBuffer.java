package lando.nsf.app.towav;

import org.apache.commons.lang3.Validate;

public final class FilteredSampleBuffer {

    public final float[] samples;
    private final float[] filter;
    
    public int next;
    
    public FilteredSampleBuffer(float[] filter) {
        Validate.isTrue( filter != null && filter.length > 0);
        this.samples = new float[filter.length];
        this.filter = filter;
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
    
    public float filtered() {
        float a = 0;
        
        for(int i = 0; i < filter.length; i++) {
            a += filter[filter.length - 1 - i]*samples[(next + i)%filter.length];
        }
        
        return a;
    }
}
