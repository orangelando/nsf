package lando.nsf.apu;

import java.util.Objects;

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
    
    private static final int DMC_STATUS_ADDR      = 0x4010;
    private static final int DMC_DIRECT_LOAD_ADDR = 0x4011;
    private static final int DMC_SAMPLE_ADDR_ADDR = 0x4012;
    private static final int DMC_SAMPLE_LEN_ADDR  = 0x4013;
    
    private static final int STATUS_ADDR          = 0x4015;
    private static final int FRAME_COUNTER_ADDR   = 0x4017;
    
    private final APU apu;
    
    public APURegisters(APU apu) {
        this.apu = Objects.requireNonNull(apu);
    }
    
    public boolean isAPURegister(int addr) {
        
        return (addr >= 0x4000 && addr <= 0x4013) ||
                addr == 0x4015 || 
                addr == 0x4017;
    }
    
    public void write(int addr, int M) {
        
        switch(addr) {
        
        case STATUS_ADDR: //acts as a control register when written to
        {
            
        }
        break;
            
        case PULSE_1_LEN_TIMER_ADDR:
        {
            apu.pulse1.lengthCounter.reload(M);
        }
        break;
        
        case PULSE_2_LEN_TIMER_ADDR:
        {
            apu.pulse2.lengthCounter.reload(M);
        }
        break;
        
        case TRIANGLE_LEN_TIMER_ADDR:
        {
            apu.triangle.lengthCounter.reload(M);
        }
        break;
        
        case NOISE_LEN_COUNTER_ADDR:
        {
            apu.noise.lengthCounter.reload(M);
        }
        break;
            
        case FRAME_COUNTER_ADDR: 
        {
            //reset divider and sequencer
            //mi-- ---- mode, IRQ disable
            int mode = (M>>7)&1;
            int irqDisable = (M>>6)&1;
            
            apu.frameSequencer.resetDividerAndSequencer();
            
            if( mode == 0 ) {
                apu.frameSequencer.select4StepMode();
            } else {
                apu.frameSequencer.select5StepMode();
                apu.frameSequencer.clockSequencer();
            }
            
            if( irqDisable == 0 ) {
                apu.frameSequencer.clearIRQDisable();
            } else {
                apu.frameSequencer.setIRQDisable();
            }
            
        } break;
        
        }
    }
    
    public int read(int addr) {
        
        switch(addr) {
        
        case STATUS_ADDR:
        {
            apu.frameSequencer.clearIRQDisable();
        }
        break;
        }
        
        throw new IllegalStateException(String.format("%x is not an readable APU register", addr));
    }
}
