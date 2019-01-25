package lando.nsf.apu;

public final class Divider {

    private int period = 1;
    private int count = 0;
    
    public Divider() {
        
    }
    
    public int getPeriod() {
        return period;
    }
    
    public void reset() {
        count = period;
    }
    
    public void setPeriod(int p) {
        if( p < 1 ) {
            throw new IllegalArgumentException("period must be > 0");
        }
        
        period = p;
    }
    
    public boolean clock() {
        
        if( --count <= 0 ) {
            count = period;
            return true;
        }
        
        return false;
    }
}
