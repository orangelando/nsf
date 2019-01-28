package lando.nsf.apu.noise;

import lando.nsf.apu.EnvelopeGenerator;
import lando.nsf.apu.LengthCounter;
import lando.nsf.apu.Timer;

public final class NoiseChannel {
    
    private static final int[] TIMER_PERIOD = {
         0x004, 0x008, 0x010, 0x020,
         0x040, 0x060, 0x080, 0x0A0,
         0x0CA, 0x0FE, 0x17C, 0x1FC,
         0x2FA, 0x3F8, 0x7F2, 0xFE4};
    
    int mode = 0;
    int period = 0;
    int shiftRegister = 1; //15-bit 
    
    public Timer timer = new Timer();
    public LengthCounter lengthCounter = new LengthCounter();
    public EnvelopeGenerator envelopeGenerator = new EnvelopeGenerator();
    
    public void clockTimer() {
        
        if( timer.clock() ) {
            int bit0 = shiftRegister & 1;
            int otherBitToCheck = mode == 0 ? 1 : 6;
            int bit1 = (shiftRegister & (1<<otherBitToCheck)) != 0 ? 1 : 0;
            int bit14 = bit0 ^ bit1;
            
            shiftRegister = (shiftRegister>>1)&0x7F_FF | (bit14<<14);
        }
    }
    
    public int getOutput() {
        
        //System.err.println(shiftRegister);
        
        if( (shiftRegister & 1) == 0 ) {
            return 0;
        };
        
        if( ! lengthCounter.isDisabled() && lengthCounter.getCount() == 0 ) {
            return 0;
        }
        
        return envelopeGenerator.getVolume();
    }

    public void setPeriod(int i) {
        timer.setPeriod(TIMER_PERIOD[i]);
    }
}
