package lando.nsf.apu.triangle;

import java.util.Objects;

public final class TriangleRegisters {
    
    private static final int COUNTER_CONTROL_BIT = 0x80;
    
    private final TriangleChannel triangle;
    
    public TriangleRegisters(TriangleChannel triangle) {
        this.triangle = Objects.requireNonNull(triangle);
    }
    
    public void writeReg1(int M) {
        //$4008
        boolean counterControl = (M & COUNTER_CONTROL_BIT) != 0;
        
        triangle.lengthCounter.setDisabled(! counterControl);
        
        triangle.linearCounter.control = counterControl;
        triangle.linearCounter.setReload(M);
    }
    
    public void writeReg2(int M) {
        //$4009
        //unused!
    }
    
    public void writeReg3(int M) {
        //$400A
        triangle.timer.setLow8PeriodBits(M);
    }
    
    public void writeReg4(int M) {
        //$400B
        triangle.timer.setUpper3PeriodBits(M);
        
        triangle.lengthCounter.reload(M);
        triangle.linearCounter.reload(M);
        triangle.linearCounter.halt = true;
    }

}
