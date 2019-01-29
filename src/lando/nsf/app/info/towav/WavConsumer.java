package lando.nsf.app.info.towav;

import java.io.OutputStream;

import lando.nsf.apu.Divider;
import lando.wav.ShortArray;
import lando.wav.WAVWriter;

/**
 * Outputs a single channel of signed 16-bit PCM samples at 44.1khz
 */
final class WavConsumer implements APUSampleConsumer {
    
    private final OutputStream bout;
    private final Divider divider = new Divider(
            (int)NSFRenderer.SYSTEM_CYCLES_PER_SEC/44_100);
    
    private float accumulator = 0;
    private ShortArray shorts = new ShortArray();
    
    WavConsumer(OutputStream bout) {
        this.bout = bout;
    }
    
    @Override
    public void init() throws Exception {
    }

    @Override
    public void consume(float sample) throws Exception {
        
        accumulator += sample;
        
        if( divider.clock() ) {
            accumulator /= divider.getPeriod();
            
            accumulator = Math.min(1, accumulator);
            accumulator = Math.max(0, accumulator);
            
            shorts.append( (short)(accumulator*65535 - 32768) );
            
            accumulator = 0;
        }
    }

    @Override
    public void finish() throws Exception {
        new WAVWriter(bout).write(shorts);
    }
}
