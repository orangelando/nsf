package lando.nsf.apu;

import java.util.Objects;

public final class TriangleRegisters {
    
    private final TriangleChannel triangle;
    
    public TriangleRegisters(TriangleChannel triangle) {
        this.triangle = Objects.requireNonNull(triangle);
    }
    
    public void writeReg1(int M) {
        
    }
    
    public void writeReg2(int M) {
        
    }
    
    public void writeReg3(int M) {
        
    }
    
    public void writeReg4(int M) {
        triangle.lengthCounter.reload(M);
        triangle.envelopeGenerator.restartOnNextClock();
    }

}
