package lando.nsf.apu;

public final class Timer {

    private int period = 1;
    private int count = 0;
    
    private int low8 = 0;
    private int upper3 = 0;
    
    public Timer() {
    }
    
    public int getPeriod() {
        return period;
    }
    
    private void setPeriod() {
        period = ( (upper3<<8) | low8 ) + 1;
    }
    
    public int getCount() {
        return count;
    }
    
    public void setLow8PeriodBits(int M) {
        low8 = M & 255;
        setPeriod();
    }
    
    public void setUpper3PeriodBits(int M) {
        upper3 = M & 7;
        setPeriod();
    }
    
    public boolean clock() {
        
        if( --count <= 0 ) {
            count = period;
            return true;
        }
        
        return false;
    }
}
