package lando.nsf.app.towav;

import java.io.OutputStream;

import lando.dsp.SimpleDsp;
import lando.nsf.apu.Divider;
import lando.wav.ShortArray;
import lando.wav.WAVWriter;

/**
 * Outputs a single channel of signed 16-bit PCM samples at 44.1khz
 * 
 * Tries to apply similar filtering that the APU mixer circuit + downstream
 * circuitry applies.
 * 
 */
final class WavConsumer implements APUSampleConsumer {
    
    private static final int WAV_SAMPLES_PER_SEC = 44_100;
    
    private final OutputStream bout;
    private final Divider divider = new Divider(
            NSFRenderer.SYSTEM_CYCLES_PER_SEC/WAV_SAMPLES_PER_SEC);
    
    private float accumulator = 0;
    private ShortArray shorts = new ShortArray();
    private float[] filter;
    
    WavConsumer(OutputStream bout) {
        this.bout = bout;
    }
    
    @Override
    public void init() throws Exception {
        SimpleDsp dsp = new SimpleDsp();
        
        float[] highpass = dsp.createHighPass(WAV_SAMPLES_PER_SEC, 0, 440);
        float[] lowpass  = dsp.createLowPass (WAV_SAMPLES_PER_SEC, 14_000, 36_000);
        
        filter = dsp.convolve(highpass, lowpass);
    }

    @Override
    public void consume(float sample) throws Exception {
        
        accumulator += sample;
        
        if( divider.clock() ) {
            accumulator /= divider.getPeriod();
            
            if( accumulator > 1 ) {
                accumulator = 1;
            }
            
            else if( accumulator < 0 ) {
                accumulator = 0;
            }
            
            shorts.append( (short)(accumulator*65535 - 32768) );
            
            accumulator = 0;
        }
    }

    @Override
    public void finish() throws Exception {
        new WAVWriter(bout).write(shorts);
    }
}
