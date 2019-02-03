package lando.nsf.app.towav;

import org.apache.commons.lang3.Validate;

final class TotalingSampleBuffer {

    public final float[] vals;
    public int next;
    public float sum;
    
    public TotalingSampleBuffer(int size) {
        Validate.isTrue(size > 0);
        this.vals = new float[size];
        this.next = 0;
        this.sum = 0.0f;
    }
    
    public int size() {
        return vals.length;
    }
    
    public void add(float newVal) {
        float evictedVal = vals[next];
        
        vals[next++] = newVal;
        
        if( next == vals.length ) {
            next = 0;
        }
        
        sum += newVal - evictedVal;
    }
    
    public float computeAverage() {
        return sum/vals.length;
    }
}
