package lando.nsf.apu;

public final class EnvelopeGenerator {

    private boolean restartOnNextClock = false;
    
    public boolean disable = false;
    public boolean loop = false;
    public int dividerPeriod = 1;
    
    private int counter = 0;
    private int divider = 0;
    
    public void restartOnNextClock() {
        restartOnNextClock = true;
    }
    
    public void clock() {
        
        if( restartOnNextClock ) {
            counter = 15;
            divider = dividerPeriod;
            restartOnNextClock = false;
        } else {
            
            if( --divider <= 0 ) {
                if( loop && counter == 0 ) {
                    counter = 15;
                } else if( counter > 0 ) {
                    --counter;
                }
            }
        }
        
    }
    
    public int getVolume() {
        
        if( disable ) {
            return dividerPeriod - 1;
        }
        
        return counter;
    }
}
