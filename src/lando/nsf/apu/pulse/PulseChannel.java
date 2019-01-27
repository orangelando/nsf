package lando.nsf.apu.pulse;

import lando.nsf.apu.Divider;
import lando.nsf.apu.EnvelopeGenerator;
import lando.nsf.apu.LengthCounter;
import lando.nsf.apu.Timer;

public final class PulseChannel {
    

    public final LengthCounter lengthCounter = new LengthCounter();
    public final EnvelopeGenerator envelopeGenerator = new EnvelopeGenerator();
    public final PulseSequencer sequencer = new PulseSequencer();
    public final Divider divider = new Divider(2);
    public final SweepUnit sweep;
    
    final Timer timer = new Timer();
    
    public PulseChannel(boolean isSecondChannel) {
        this.sweep = new SweepUnit(timer, isSecondChannel);
    }
    
    public void clockTimer() {
        
        if( timer.clock() ) {
            if( divider.clock() ) {
                sequencer.clock();
            }
        }
    }
    
    public int getOutput() {
        
        /*
        System.err.println(
                "timer: " + timer.getCount() + ", " + timer.getPeriod() + ", " +
                "sweep: " + sweep.getShifter() + ", " + sweep.isTooMuch() + ", " + 
                "sequencer: " + sequencer.getOutput() + ", " + 
                "lengthCounter: " + lengthCounter.getCount() + ", " + lengthCounter.isDisabled() + ", " + 
                "envelopeGenerator: " + envelopeGenerator.getVolume());
                */
              
        
        if( sweep.isTooMuch() ) {
            return 0;
        }
        
        if( sequencer.getOutput() == 0 ) {
            return 0;
        }
        
        if( ! lengthCounter.isDisabled() && lengthCounter.getCount() == 0 ) {
            return 0;
        }
              
        return envelopeGenerator.getVolume();
    }
}
