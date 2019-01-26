package lando.nsf.apu.noise;

import java.util.Objects;

public final class NoiseRegisters {
    
    private final NoiseChannel noise;
    
    public NoiseRegisters(NoiseChannel noise) {
        this.noise = Objects.requireNonNull(noise);
    }
    
    public void writeReg1(int M) {
        
    }
    
    public void writeReg2(int M) {
        
    }
    
    public void writeReg3(int M) {
        
    }
    
    public void writeReg4(int M) {
        noise.lengthCounter.reload(M);
        noise.envelopeGenerator.restartOnNextClock();
    }

}
