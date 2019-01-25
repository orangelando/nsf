package lando.nsf.apu;

import java.util.Objects;

public final class PulseRegisters {
    
    private static final int SWEEP_ENABLE_BIT = 0x80;
    private static final int SWEEP_NEGATE_BIT = 0x08;
    
    private static final int ENVELOPE_LOOP_BIT    = 0x20;
    private static final int ENVELOPE_DISABLE_BIT = 0x10;
    
    private final PulseChannel pulse;
    
    public PulseRegisters(PulseChannel pulse) {
        this.pulse = Objects.requireNonNull(pulse);
    }
    
    public void writeReg1(int M) {
        pulse.sequencer.setDuty(M >> 6);
        pulse.envelopeGenerator.loop = (M & ENVELOPE_LOOP_BIT) != 0;
        pulse.envelopeGenerator.disable = (M & ENVELOPE_DISABLE_BIT) != 0;
        pulse.envelopeGenerator.dividerPeriod = (M & 0xF) + 1;
        
        //pulse.lengthCounter.setDisabled(flag);
    }
    
    public void writeReg2(int M) {
        pulse.sweep.enable = (M & SWEEP_ENABLE_BIT) != 0;
        pulse.sweep.negate = (M & SWEEP_NEGATE_BIT) != 0;
        pulse.sweep.divider.setPeriod( ((M>>4) & 7) + 1);
        pulse.sweep.shift = M & 7;
    }
    
    public void writeReg3(int M) {
        pulse.timer.setLow8PeriodBits(M);
    }
    
    public void writeReg4(int M) {
        pulse.lengthCounter.reload(M);
        pulse.envelopeGenerator.restartOnNextClock();
        pulse.timer.setUpper3PeriodBits(M);
        pulse.sequencer.setDuty(0);
        //pulse.envelopeGenerator.
    }
}