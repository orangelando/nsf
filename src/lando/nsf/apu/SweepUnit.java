package lando.nsf.apu;

import java.util.Objects;

public final class SweepUnit {

    boolean resetDividerOnNextClock = false;
    boolean enable = false;
    boolean negate = false;
    int shift = 0;
    
    final Divider divider = new Divider();
    final Timer timer;
    final boolean isForSecondPulse;
    
    public SweepUnit(Timer timer, boolean isForSecondPulse) {
        this.timer = Objects.requireNonNull(timer);
        this.isForSecondPulse = isForSecondPulse;
    }
    
    void resetDividerOnNextClock() {
        resetDividerOnNextClock = true;
    }
    
    void clock() {

        if( divider.clock() ) {
            int s = getShifter();

            if( enable && ! isTooMuch(s) && s > 0 ) {
                timer.setLow8PeriodBits(s & 255);
                timer.setUpper3PeriodBits((s >> 8) & 7);
            }
        }
        
        if( resetDividerOnNextClock ) {
            divider.reset();
            resetDividerOnNextClock = false;
        }
    }
    
    int getShifter() {
        int shifter = timer.getPeriod()>>shift;
        
        if( negate ) {
            shifter = ~shifter + (isForSecondPulse ? 1 : 0);
        }
        
        return (shifter + timer.getPeriod()) & 0xFF_FF;
    }
    
    boolean isTooMuch() {
        return isTooMuch(getShifter());
    }
    
    boolean isTooMuch(int shifter) {
        return timer.getPeriod() < 8 || shifter > 0x7FF;
    }
}
