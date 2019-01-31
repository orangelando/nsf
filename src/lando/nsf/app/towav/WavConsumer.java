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
    private SampleRingBuffer samples;
    
    WavConsumer(OutputStream bout) {
        this.bout = bout;
    }
    
    @Override
    public void init() throws Exception {
        SimpleDsp dsp = new SimpleDsp();
        
        float[] highpass = dsp.createHighPass(WAV_SAMPLES_PER_SEC, 0, 440);
        float[] lowpass  = dsp.createLowPass (WAV_SAMPLES_PER_SEC, 14_000, 26_000);
        
        filter = dsp.convolve(highpass, lowpass);
                
        samples = new SampleRingBuffer(filter.length);
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
            
            samples.add(accumulator);
            
            shorts.append( (short)(filtered()*32767) );
            
            accumulator = 0;
        }
    }
    
    private float filtered() {
       
        float a = 0;
        
        for(int i = 0; i < filter.length; i++) {
            a += filter[filter.length - 1 - i]*samples.samples[(samples.next + i)%filter.length];
        }
        
        return a;
    }

    @Override
    public void finish() throws Exception {
        new WAVWriter(bout).write(shorts);
    }
}
