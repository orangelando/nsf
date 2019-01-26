package lando.nsf.apu.pulse;

import lando.nsf.apu.EnvelopeGenerator;
import lando.nsf.apu.LengthCounter;
import lando.nsf.apu.Timer;

public final class PulseChannel {
    

    public final LengthCounter lengthCounter = new LengthCounter();
    public final EnvelopeGenerator envelopeGenerator = new EnvelopeGenerator();
    public final PulseSequencer sequencer = new PulseSequencer();
    public final Timer timer = new Timer();
    public final SweepUnit sweep;
    
    public PulseChannel(boolean isSecondChannel) {
        this.sweep = new SweepUnit(timer, isSecondChannel);
    }
    
    public int getOutput() {
        
        if( sweep.isTooMuch() ) {
            return 0;
        }
        
        return 0;
    }
}
