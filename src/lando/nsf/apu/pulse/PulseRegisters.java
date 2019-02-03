package lando.nsf.apu.pulse;

import java.util.Objects;

import lando.nsf.cpu.StringUtils;

public final class PulseRegisters {
    
    private static final int LENGTH_ENABLE_BIT = 0x20;
    
    private static final int SWEEP_ENABLE_BIT = 0x80;
    private static final int SWEEP_NEGATE_BIT = 0x08;
    
    private static final int ENVELOPE_LOOP_BIT    = 0x20;
    private static final int ENVELOPE_DISABLE_BIT = 0x10;
    
    private final PulseChannel pulse;
    
    public PulseRegisters(PulseChannel pulse) {
        this.pulse = Objects.requireNonNull(pulse);
    }
    
    public void writeReg1(int M) {
        
        if( pulse.isSecondChannel ) {
            //String s = StringUtils.toBin8(M);
            //System.err.printf("reg1 %s%n", s.substring(0, 4) + " " + s.substring(4));
        }
        
        //$4000,$4004
        pulse.sequencer.setDuty(M >> 6);
        
        pulse.envelopeGenerator.loop = (M & ENVELOPE_LOOP_BIT) != 0;
        pulse.envelopeGenerator.disable = (M & ENVELOPE_DISABLE_BIT) != 0;
        pulse.envelopeGenerator.dividerPeriod = (M & 0xF) + 1;
        
        pulse.lengthCounter.setDisabled( (M & LENGTH_ENABLE_BIT) != 0 );
    }
    
    public void writeReg2(int M) {        
        //$4001,$4005
        pulse.sweep.enable = (M & SWEEP_ENABLE_BIT) != 0;
        pulse.sweep.negate = (M & SWEEP_NEGATE_BIT) != 0;
        pulse.sweep.divider.setPeriod( ((M>>4) & 7) + 1);
        pulse.sweep.shift = M & 7;
        
        pulse.sweep.resetDividerOnNextClock();
    }
    
    public void writeReg3(int M) {
        //$4002,$4006
        pulse.timer.setLow8PeriodBits(M);
    }
    
    public void writeReg4(int M) {
        
        //$4003,$4007
        pulse.lengthCounter.reload(M);
        pulse.envelopeGenerator.restartOnNextClock();
        pulse.timer.setUpper3PeriodBits(M);
        pulse.sequencer.setDuty(0);
    }
}
