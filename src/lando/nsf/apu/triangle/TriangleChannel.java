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
        
        if( timer.getPeriod() <= 2 ) {
            return 7;
        }
                
        return sequencer.getOutput();
    }
}
