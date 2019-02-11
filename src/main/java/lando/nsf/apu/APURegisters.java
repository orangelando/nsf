package lando.nsf.apu;

import java.util.Objects;

import lando.nsf.apu.dmc.DeltaModulationRegisters;
import lando.nsf.apu.noise.NoiseRegisters;
import lando.nsf.apu.pulse.PulseRegisters;
import lando.nsf.apu.triangle.TriangleRegisters;

public final class APURegisters {
    
    private static final int PULSE_1_STATUS_ADDR     = 0x4000;
    private static final int PULSE_1_SWEEP_ADDR      = 0x4001;
    private static final int PULSE_1_TIMER_LOW_ADDR  = 0x4002;
    private static final int PULSE_1_LEN_TIMER_ADDR  = 0x4003;
    
    private static final int PULSE_2_STATUS_ADDR     = 0x4004;
    private static final int PULSE_2_SWEEP_ADDR      = 0x4005;
    private static final int PULSE_2_TIMER_LOW_ADDR  = 0x4006;
    private static final int PULSE_2_LEN_TIMER_ADDR  = 0x4007;
    
    private static final int TRIANGLE_STATUS_ADDR    = 0x4008;
    private static final int TRIANGLE_UNUSED_ADDR    = 0x4009;
    private static final int TRIANGLE_TIMER_LOW_ADDR = 0x400A;
    private static final int TRIANGLE_LEN_TIMER_ADDR = 0x400B;
    
    private static final int NOISE_STATUS_ADDR       = 0x400C;
    private static final int NOISE_UNUSED_ADDR       = 0x400D;
    private static final int NOISE_LOOP_PERIOD_ADDR  = 0x400E;
    private static final int NOISE_LEN_COUNTER_ADDR  = 0x400F;
    
    private static final int DMC_STATUS_ADDR         = 0x4010;
    private static final int DMC_DIRECT_LOAD_ADDR    = 0x4011;
    private static final int DMC_SAMPLE_ADDR_ADDR    = 0x4012;
    private static final int DMC_SAMPLE_LEN_ADDR     = 0x4013;
    
    private static final int STATUS_ADDR             = 0x4015;
    private static final int FRAME_COUNTER_ADDR      = 0x4017;
    
    private final APU apu;
    private final PulseRegisters pulse1Registers;
    private final PulseRegisters pulse2Registers;
    private final TriangleRegisters triangleRegisters;
    private final NoiseRegisters noiseRegisters;
    private final DeltaModulationRegisters dmcRegisters;
    
    
    public APURegisters(APU apu) {
        this.apu = Objects.requireNonNull(apu);
        this.pulse1Registers = new PulseRegisters(apu.pulse1);
        this.pulse2Registers = new PulseRegisters(apu.pulse2);
        this.triangleRegisters = new TriangleRegisters(apu.triangle);
        this.noiseRegisters = new NoiseRegisters(apu.noise);
        this.dmcRegisters = new DeltaModulationRegisters(apu.dmc);
    }
    
    public boolean isAPURegister(int addr) {
        return (addr >= 0x4000 && addr <= 0x4013) ||
                addr == 0x4015 || 
                addr == 0x4017;
    }
    
    public void write(int addr, int M) {
        
        /*
        if( addr == STATUS_ADDR ) 
            APULog.logWrite("S", addr, M);
        
        if( addr == FRAME_COUNTER_ADDR ) 
            APULog.logWrite("F", addr, M);
        
        if( addr >= PULSE_2_STATUS_ADDR && addr <= PULSE_2_LEN_TIMER_ADDR )
            APULog.logWrite(addr - PULSE_2_STATUS_ADDR, addr, M);
        //*/
        
        switch(addr) {
        
        case PULSE_1_STATUS_ADDR:    pulse1Registers.writeReg1(M); break;
        case PULSE_1_SWEEP_ADDR:     pulse1Registers.writeReg2(M); break;
        case PULSE_1_TIMER_LOW_ADDR: pulse1Registers.writeReg3(M); break;
        case PULSE_1_LEN_TIMER_ADDR: pulse1Registers.writeReg4(M); break;
        
        case PULSE_2_STATUS_ADDR:    pulse2Registers.writeReg1(M); break;
        case PULSE_2_SWEEP_ADDR:     pulse2Registers.writeReg2(M); break;
        case PULSE_2_TIMER_LOW_ADDR: pulse2Registers.writeReg3(M); break;
        case PULSE_2_LEN_TIMER_ADDR: pulse2Registers.writeReg4(M); break;
        
        case TRIANGLE_STATUS_ADDR:    triangleRegisters.writeReg1(M); break;
        case TRIANGLE_UNUSED_ADDR:    triangleRegisters.writeReg2(M); break;
        case TRIANGLE_TIMER_LOW_ADDR: triangleRegisters.writeReg3(M); break;
        case TRIANGLE_LEN_TIMER_ADDR: triangleRegisters.writeReg4(M); break;
        
        case NOISE_STATUS_ADDR:      noiseRegisters.writeReg1(M); break;
        case NOISE_UNUSED_ADDR:      noiseRegisters.writeReg2(M); break;
        case NOISE_LOOP_PERIOD_ADDR: noiseRegisters.writeReg3(M); break;
        case NOISE_LEN_COUNTER_ADDR: noiseRegisters.writeReg4(M); break;
        
        case DMC_STATUS_ADDR:      dmcRegisters.writeReg1(M); break;
        case DMC_DIRECT_LOAD_ADDR: dmcRegisters.writeReg2(M); break;
        case DMC_SAMPLE_ADDR_ADDR: dmcRegisters.writeReg3(M); break;
        case DMC_SAMPLE_LEN_ADDR:  dmcRegisters.writeReg4(M); break;        
        
        case STATUS_ADDR: writeStatusReg(M); break;
            
        case FRAME_COUNTER_ADDR: writeFrameCounterReg(M); break;
        }
    }
    
    public int read(int addr) {

        switch(addr) {
        case STATUS_ADDR: return readStatusReg();        
        }
        
        //is this the right thing to do?
        return 0;
    }
    
    private int readStatusReg() {
        final int status = 
                ((apu.dmc.getIntteruptFlag()            ? 1 : 0)<<7) |
                ((apu.frameSequencer.getInterruptFlag() ? 1 : 0)<<6) |
                ((apu.dmc.getSampleBytesRemaining() > 0 ? 1 : 0)<<4) |
                
                ((apu.noise   .lengthCounter.getCount() > 0 ? 1 : 0)<<3) |
                ((apu.triangle.lengthCounter.getCount() > 0 ? 1 : 0)<<2) |
                ((apu.pulse2  .lengthCounter.getCount() > 0 ? 1 : 0)<<1) |
                ((apu.pulse1  .lengthCounter.getCount() > 0 ? 1 : 0)<<0) ;
        
        apu.frameSequencer.clearInterruptFlag();
        
        return status;
    }

    private void writeStatusReg(int M) {
        
        if( (M & 0b0001_0000) != 0 ) {
            apu.dmc.restartSample();
        } else {
            apu.dmc.clearSampleBytesRemaining();
        }
        
        apu.dmc.clearInterrupt();
        
        apu.noise   .lengthCounter.setDisabled( (M & 0b1000) == 0);
        apu.triangle.lengthCounter.setDisabled( (M & 0b0100) == 0);
        apu.pulse2  .lengthCounter.setDisabled( (M & 0b0010) == 0);
        apu.pulse1  .lengthCounter.setDisabled( (M & 0b0001) == 0);
    }
        
    private void writeFrameCounterReg(int M) {
        
        //reset divider and sequencer
        //mi-- ---- mode, IRQ disable
        boolean mode       = ((M>>7)&1) != 0;
        boolean irqDisable = ((M>>6)&1) != 0;
        
        apu.frameSequencer.resetDividerAndSequencer();
        
        if( ! mode ) {
            apu.frameSequencer.select4StepMode();
        } else {
            apu.frameSequencer.select5StepMode();
            apu.frameSequencer.clockSequencer();
        }
        
        if( ! irqDisable ) {
            apu.frameSequencer.clearIRQDisable();
        } else {
            apu.frameSequencer.setIRQDisable();
        }
    }
}