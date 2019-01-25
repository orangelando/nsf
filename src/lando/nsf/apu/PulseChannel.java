package lando.nsf.apu;

public final class PulseChannel {
    

    final LengthCounter lengthCounter = new LengthCounter();
    final EnvelopeGenerator envelopeGenerator = new EnvelopeGenerator();
    final PulseSequencer sequencer = new PulseSequencer();
    final Timer timer = new Timer();
    final SweepUnit sweep;
    
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
