package lando.nsf.apu;

import java.util.Objects;

import lando.nsf.cpu.CPU;
import lando.nsf.cpu.IRQSource;

public final class FrameSequencer {
    
    //NES system clock is 21.47727 MHz
    private static final int DIVIDER_PERIOD = 89490;
    
    private final CPU cpu;
    private final PulseChannel pulse1;
    private final PulseChannel pulse2;
    private final TriangleChannel triangle;
    private final NoiseChannel noise;
    private final DeltaModulationChannel dmc;
    
    private int dividerCount = DIVIDER_PERIOD;
    private int step = 0;
    private Runnable sequence = this::clockMode4Sequence;
    private boolean disableInterrupts = true;
    
    public FrameSequencer(
            CPU cpu,
            PulseChannel pulse1,
            PulseChannel pulse2,
            TriangleChannel triangle,
            NoiseChannel noise,
            DeltaModulationChannel dmc
            ) {
        
        this.cpu = Objects.requireNonNull(cpu);
        this.pulse1 = Objects.requireNonNull(pulse1);
        this.pulse2 = Objects.requireNonNull(pulse2);
        this.triangle = Objects.requireNonNull(triangle);
        this.noise = Objects.requireNonNull(noise);
        this.dmc = Objects.requireNonNull(dmc);
    }

    public void select4StepMode() {
        sequence = this::clockMode4Sequence;
        step = step%4;
    }

    public void select5StepMode() {
        sequence = this::clockMode5Sequence;
        clearInterruptFlag();
    }
    
    public void clearIRQDisable() {
        disableInterrupts = false;
    }
    
    public void setIRQDisable() {
        disableInterrupts = true;
    }
    
    /**
     * 
     * The divider generates an output clock rate of just under 240 Hz, and appears to
     * be derived by dividing the 21.47727 MHz system clock by 89490. The sequencer is
     * clocked by the divider's output.
     * 
     */
    public void clockDivider() {
        if( --dividerCount <= 0 ) {
            sequence.run();
            dividerCount = DIVIDER_PERIOD;
        }
    }
    
    public void clockSequencer() {
        sequence.run();
    }
    
    public void resetDividerAndSequencer() {
        step = 0;
        dividerCount = DIVIDER_PERIOD;
    }
    
    private void clockMode4Sequence() {
        
        switch(step++) {
        case 0:
            clearInterruptFlag();
            clockEnvelopesAndTriangleLinearCounter();
            break;
            
        case 1:
            clockLengthCountersAndSweepUnits();
            clockEnvelopesAndTriangleLinearCounter();
            break;
            
        case 2:
            clockEnvelopesAndTriangleLinearCounter();
            break;
            
        case 3:
            setInterruptFlag();
            clockLengthCountersAndSweepUnits();
            clockEnvelopesAndTriangleLinearCounter();
            break;
        }
        
        if(step > 3) {
            step = 0;
        }
    }
    
    private void clockMode5Sequence() {
        
        switch(step++) {
        case 0:
            clockLengthCountersAndSweepUnits();
            clockEnvelopesAndTriangleLinearCounter();
            break;
            
        case 1:
            clockEnvelopesAndTriangleLinearCounter();
            break;
            
        case 2:
            clockLengthCountersAndSweepUnits();
            clockEnvelopesAndTriangleLinearCounter();
            break;
            
        case 3:
            clockEnvelopesAndTriangleLinearCounter();
            break;
            
        case 4:
            break;
        }
        
        if(step > 4) {
            step = 0;
        }
    }
    
    private void setInterruptFlag() {
        if( ! disableInterrupts ) {
            cpu.setIRQ(IRQSource.APU);
        }
    }
    
    private void clearInterruptFlag() {
        cpu.clearIRQ(IRQSource.APU);
    }
    
    private void clockLengthCountersAndSweepUnits() {
        pulse1.lengthCounter.clock();
        pulse2.lengthCounter.clock();
        triangle.lengthCounter.clock();
        noise.lengthCounter.clock();
    }
    
    private void clockEnvelopesAndTriangleLinearCounter() {
        
    }
}
