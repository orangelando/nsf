package lando.nsf;

import org.apache.commons.lang3.Validate;

public final class IntRange {

    public static IntRange of(int min, int max) {
        return new IntRange(min, max);
    }
    
    private final int inclusiveMin;
    private final int exclusiveMax;
    
    private IntRange(int min, int max) {
        Validate.isTrue(min < max);
        
        this.inclusiveMin = min;
        this.exclusiveMax = max;
    }
    
    public int getInclusiveMin() {
        return inclusiveMin;
    }
    
    public int getExclusiveMax() {
        return exclusiveMax;
    }
    
    public boolean overlaps(IntRange that) {
        Validate.notNull(that);
        
        return Math.max(this.inclusiveMin, that.inclusiveMin) < 
               Math.min(this.exclusiveMax, that.exclusiveMax);
    }
}
