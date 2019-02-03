package lando.nsf.apu;

public final class EnvelopeGenerator {

    private boolean restartOnNextClock = false;
    
    public boolean disable = false;
    public boolean loop = false;
    public int dividerPeriod = 1;
    public final int counterPeriod = 15;
    
    private int counter = 0;
    private int divider = 0;
    
    public void restartOnNextClock() {
        restartOnNextClock = true;
    }
    
    public void clock() {
        
        if( restartOnNextClock ) {
            counter = counterPeriod;
            divider = dividerPeriod;
            restartOnNextClock = false;
        } else {
            
            if( --divider < 0 ) {
                divider = dividerPeriod;
                
                if( --counter < 0) {
                    counter = loop ? counterPeriod : 0;
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
