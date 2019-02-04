package lando.nsf.apu.pulse;

import lando.nsf.apu.Divider;
import lando.nsf.apu.EnvelopeGenerator;
import lando.nsf.apu.LengthCounter;
import lando.nsf.apu.Timer;

public final class PulseChannel {

    public final boolean isSecondChannel;
    public final LengthCounter lengthCounter = new LengthCounter();
    public final EnvelopeGenerator envelopeGenerator = new EnvelopeGenerator();
    public final PulseSequencer sequencer = new PulseSequencer();
    public final Divider divider = new Divider(2);
    public final SweepUnit sweep;
    
    final Timer timer = new Timer();
    
    public PulseChannel(boolean isSecondChannel) {
        this.sweep = new SweepUnit(timer, isSecondChannel);
        this.isSecondChannel = isSecondChannel;
    }
    
    public void clockTimer() {
        
        if( timer.clock() ) {
            if( divider.clock() ) {
                sequencer.clock();
            }
        }
    }
    
    public int getOutput() {
        
        if( sweep.isTooMuch() ) {
            return 0;
        }
        
        if( lengthCounter.shouldSilenceChannel() ) {
            return 0;
        }
        
        if( sequencer.getOutput() == 0 ) {
            return 0;
        }
                      
        return envelopeGenerator.getVolume();
    }
}
