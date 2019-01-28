package lando.nsf.initload;

import java.io.OutputStream;

/**
 * Outputs a single channele of signed 16-bit PCM samples at 44.1khz
 */
final class WavConsumer implements SampleConsumer {
    
    WavConsumer(OutputStream bout) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public void init() throws Exception {
        throw new UnsupportedOperationException("Not implemented yet");
        //output WAV header
    }

    @Override
    public void consume(float sample) throws Exception {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void finish() throws Exception {
        throw new UnsupportedOperationException("Not implemented yet");
    }

}
