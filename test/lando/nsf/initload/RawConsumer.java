package lando.nsf.initload;

import java.io.OutputStream;
import java.util.Objects;

import lando.nsf.apu.Divider;

/**
 * Outputs raw 32-bit float samples average every N samples.
 */
final class RawConsumer implements SampleConsumer {

    private final Divider audioDivider;
    private final OutputStream bout;
    private float audioAccumulator = 0;
    
    RawConsumer(int audioDividerPeriod, OutputStream bout) {
        this.audioDivider = new Divider( audioDividerPeriod );
        this.bout = Objects.requireNonNull(bout);
    }
    
    @Override
    public void init() throws Exception {
        
    }

    @Override
    public void consume(float apuSample) throws Exception {
        
        audioAccumulator += apuSample;
        
        if( audioDivider.clock() ) {
            
            audioAccumulator /= audioDivider.getPeriod();
        
            int sample = Float.floatToIntBits(audioAccumulator);

            bout.write((sample>> 0)&255);
            bout.write((sample>> 8)&255);
            bout.write((sample>>16)&255);
            bout.write((sample>>24)&255);
            
            audioAccumulator = 0;
        }
    }

    @Override
    public void finish() throws Exception {
        
    }
}
