package lando.dsp;

import static java.lang.Math.PI;
import static java.lang.Math.ceil;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import org.apache.commons.lang3.Validate;

/**
 * https://tomroelandts.com/articles/how-to-create-a-simple-low-pass-filter
 */
public final class SimpleDsp {
    
    public double sinc(double x) {
        if( x == 0.0 ) {
            return 1.0;
        }
        
        double u = PI*x;
        
        return sin(u)/u;
    }
    
    /**
     * pre: sum(x) > 0
     */
    public void normalize(float[] x) {
        double sum = 0;
        
        for(int n = 0; n < x.length; n++) {
            sum += x[n];
        }
        
        for(int n = 0; n < x.length; n++) {
            x[n] /= sum;
        }
        
    }
    
    public float[] createBlackmanWindow(int N) {
        Validate.isTrue(N > 0);
        
        float[] window  = new float[N];
        
        for(int n = 0; n < N; n++) {
            window[n] = (float)(0.42 
                    - 0.50*cos(2 * PI * n / (N - 1)) 
                    + 0.08*cos(4 * PI * n / (N - 1))
                    );
        }
        
        return window;
    }
    
    public float[] createSinc(int N, float fc) {
        float[] impulse = new float[N];
        
        for(int n = 0; n < N; n++) {
            impulse[n] = (float)sinc(2 * fc * (n - (N - 1) / 2));
        }
        
        return impulse;
    }
    
    public void multiply(float[] filter, float[] scale) {
        Validate.isTrue(filter != null && scale != null && filter.length == scale.length);
        
        for(int n = 0; n < filter.length; n++) {
            filter[n] *= scale[n];
        }
    }
    
    public float[] createLowPass(int samplesPerSec, float cutoffFreqStart, float cutoffFreqEnd) {
        Validate.isTrue(samplesPerSec > 0);
        Validate.isTrue(cutoffFreqStart >= 0);
        Validate.isTrue(cutoffFreqEnd > cutoffFreqStart);
        
        float fc = cutoffFreqStart/samplesPerSec;
        float b  = (cutoffFreqEnd - cutoffFreqStart)/samplesPerSec;
        
        Validate.isTrue( fc >= 0f && fc <= 1.0f );
        Validate.isTrue( b  >  0f && b  <= 0.5f );
        
        int N = (int)(ceil(4f / b));
        
        N += N%2==0 ? 1 : 0; //make odd
        
        Validate.isTrue(N > 0);
        
        float[] filter = createSinc(N, fc);
        float[] window = createBlackmanWindow(N);
        
        multiply(filter, window);
        normalize(filter);

        return filter;
    }
    
    public float[] createHighPass(int samplesPerSec, float cutoffFreqStart, float cutoffFreqEnd) {
        float[] filter = createLowPass(samplesPerSec, cutoffFreqStart, cutoffFreqEnd);
        Validate.isTrue(filter.length%2 ==1);
        
        for(int n = 0; n < filter.length; n++) {
            filter[n] *= -1;
        }
        
        filter[filter.length/2] += 1;
        
        return filter;
    }
    
    public float get(float[] a, int i) {
        return i >= 0 && i < a.length ? a[i] : 0;
    }
    
    public float[] convolve(float[] f, float[] g) {
        Validate.isTrue(f != null && g != null && f.length > 0 && f.length > 0);
        
        float[] y = new float[f.length + g.length - 1];
        
        for(int n = 0; n < y.length; n++) {
            float a = 0;
            
            for(int m = 0; m < y.length; m++) {
                a += get(f, m)*get(g, n - m);
            }
            
            y[n] = a;
        }
        
        return y;
    }
}
