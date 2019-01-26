package lando.nsf.apu.noise;

import lando.nsf.apu.EnvelopeGenerator;
import lando.nsf.apu.LengthCounter;

public final class NoiseChannel {
    
    public LengthCounter lengthCounter = new LengthCounter();
    public EnvelopeGenerator envelopeGenerator = new EnvelopeGenerator();
}
