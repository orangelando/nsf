package lando.nsf.apu.triangle;

import lando.nsf.apu.LengthCounter;
import lando.nsf.apu.Timer;

public final class TriangleChannel {

    public final Timer timer = new Timer();
    public final LinearCounter linearCounter = new LinearCounter();
    public final LengthCounter lengthCounter = new LengthCounter();
    public final TriangleSequencer sequencer = new TriangleSequencer();
    
    public void clockTimer() {
        
        if( ! timer.clock() ) {
            return;
        }
        
        if( ! lengthCounter.isDisabled() && lengthCounter.getCount() == 0 ) {
            return;
        }
        
        if( linearCounter.counter == 0 ) {
            return;
        }
        
        sequencer.clock();
    }
    
    public int getOutput() {
        
        /*
         * Blaarg says:
         *   At the lowest two periods ($400B = 0 and $400A = 0 or 1), the resulting
         *   frequency is so high that the DAC effectively outputs a value half way between
         *   7 and 8.
         *   
         * Remember that the timer period is equal to the register values + 1 so 
         * register value 0 or 1 is really timer period 1 or 2.
         */
        if( timer.getPeriod() <= 2 ) {
            return 7;
        }
                
        return sequencer.getOutput();
    }
}
