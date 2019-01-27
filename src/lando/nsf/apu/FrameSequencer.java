package lando.nsf.apu;

import java.util.Objects;

import lando.nsf.apu.dmc.DeltaModulationChannel;
import lando.nsf.apu.noise.NoiseChannel;
import lando.nsf.apu.pulse.PulseChannel;
import lando.nsf.apu.triangle.TriangleChannel;
import lando.nsf.cpu.CPU;
import lando.nsf.cpu.IRQSource;

public final class FrameSequencer {

    private final CPU cpu;
    private final PulseChannel pulse1;
    private final PulseChannel pulse2;
    private final TriangleChannel triangle;
    private final NoiseChannel noise;
    private final DeltaModulationChannel dmc;
    
    //NES system clock is 21.47727 MHz
    private final Divider divider = new Divider(89490);
    private int step = 0;
    private Runnable sequence = this::clockMode4Sequence;
    private boolean disableInterrupts = true;
    private boolean interruptFlag = false;
    
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
    
    public void clockDivider() {
        if( divider.clock() ) {
            sequence.run();
        }
    }
    
    public void clockSequencer() {
        sequence.run();
    }
    
    public void resetDividerAndSequencer() {
        step = 0;
        divider.reset();
    }
    
    private void clockMode4Sequence() {
        
        switch(step++) {
        case 0:
            clearInterruptFlag();
            clockEnvelopesAndLinearCounters();
            break;
            
        case 1:
            clockLengthCountersAndSweepUnits();
            clockEnvelopesAndLinearCounters();
            break;
            
        case 2:
            clockEnvelopesAndLinearCounters();
            break;
            
        case 3:
            setInterruptFlag();
            clockLengthCountersAndSweepUnits();
            clockEnvelopesAndLinearCounters();
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
            clockEnvelopesAndLinearCounters();
            break;
            
        case 1:
            clockEnvelopesAndLinearCounters();
            break;
            
        case 2:
            clockLengthCountersAndSweepUnits();
            clockEnvelopesAndLinearCounters();
            break;
            
        case 3:
            clockEnvelopesAndLinearCounters();
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
            interruptFlag = true;
            cpu.setIRQ(IRQSource.APU);
        }
    }
    
    public void clearInterruptFlag() {
        interruptFlag = false;
        cpu.clearIRQ(IRQSource.APU);
    }
    
    public boolean getInterruptFlag() {
        return interruptFlag;
    }
    
    private void clockLengthCountersAndSweepUnits() {
        pulse1.lengthCounter.clock();
        pulse2.lengthCounter.clock();
        triangle.lengthCounter.clock();
        noise.lengthCounter.clock();
        
        pulse1.sweep.clock();
        pulse2.sweep.clock();
    }
    
    private void clockEnvelopesAndLinearCounters() {
        pulse1.envelopeGenerator.clock();
        pulse2.envelopeGenerator.clock();
        //triangle.envelopeGenerator.clock();
        noise.envelopeGenerator.clock();
        
        triangle.linearCounter.clock();
    }
}
