package lando.nsf.apu.triangle;

import lando.nsf.apu.EnvelopeGenerator;
import lando.nsf.apu.LengthCounter;
import lando.nsf.apu.Timer;

public final class TriangleChannel {

    public final Timer timer = new Timer();
    public final LengthCounter lengthCounter = new LengthCounter();
    public final EnvelopeGenerator envelopeGenerator = new EnvelopeGenerator();
    public final LinearCounter linearCounter = new LinearCounter();
    public final TriangleSequencer sequencer = new TriangleSequencer();
    
    public void clock() {
        
    }
    
    public int getOutput() {
        return sequencer.getOutput();
    }
}
