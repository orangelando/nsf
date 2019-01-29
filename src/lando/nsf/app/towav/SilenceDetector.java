package lando.nsf.app.towav;

import lando.nsf.apu.Divider;

final class SilenceDetector {

    private final Divider divider;
    private boolean silenceDetected;
    
    private float prevSample;
    private float sumDiffSq;
    
    SilenceDetector(int numCycles) {
        this.divider = new Divider(numCycles);
    }
    
    void addSample(float sample) {
        float diff = sample - prevSample;
        
        sumDiffSq += diff*diff;
        
        prevSample = sample;
        
        if( divider.clock() ) {
            float avgDiffSq = sumDiffSq/divider.getPeriod();
            
            if( avgDiffSq < 1e-9f ) {
                silenceDetected = true;
            }
            
            sumDiffSq = 0;
        }
    }
    
    boolean wasSilenceDetected() {
        return silenceDetected;
    }

    public void reset() {
        divider.reset();
        prevSample = 0f;
        sumDiffSq = 0f;
        silenceDetected = false;
    }
}
