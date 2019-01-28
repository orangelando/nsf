package lando.nsf.apu.noise;

import java.util.Objects;

public final class NoiseRegisters {
    
    private static final int LOOP_BIT = 0x80;
    
    private static final int ENVELOPE_LOOP_BIT    = 0x20;
    private static final int ENVELOPE_DISABLE_BIT = 0x10;

    
    private final NoiseChannel noise;
    
    public NoiseRegisters(NoiseChannel noise) {
        this.noise = Objects.requireNonNull(noise);
    }
    
    public void writeReg1(int M) {
        //$400C
        noise.envelopeGenerator.loop          = (M & ENVELOPE_LOOP_BIT) != 0;
        noise.envelopeGenerator.disable       = (M & ENVELOPE_DISABLE_BIT) != 0;
        noise.envelopeGenerator.dividerPeriod = (M & 0xF) + 1;
    }
    
    public void writeReg2(int M) {
        //$400D
    }
    
    public void writeReg3(int M) {
        //$400E
        noise.mode = (M & LOOP_BIT) != 0 ? 1 : 0;
        noise.setPeriod(M & 0xF);
    }
    
    public void writeReg4(int M) {
        //$400F
        noise.lengthCounter.reload(M);
        noise.envelopeGenerator.restartOnNextClock();
    }

}
