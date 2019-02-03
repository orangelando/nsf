package lando.nsf.dspscratch;

import static java.lang.Math.PI;
import static java.lang.Math.ceil;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static lando.nsf.spectrogram.GenerateSpectrogramApp.readAllSamples;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import org.apache.commons.lang3.Validate;

import lando.dsp.SimpleDsp;
import lando.nsf.app.towav.FilteredSampleBuffer;

public class FilterTestApp {

    public static void main(String [] args) throws Exception {
        PrintStream out = System.err;
        int samplesPerSec = 44_100;
        
        float[] samples = readAllSamples(
                Paths.get("/Users/oroman/Desktop/test.raw"));
        
        out.printf("read %d samples or %s%n", 
                samples.length, 
                Duration.ofMillis(1000L*samples.length/samplesPerSec));
        
        filter(samplesPerSec, samples);
        
        out.println("filtered");
        
        writeAllSamples(
                samples, 
                Paths.get("/Users/oroman/Desktop/filtered.raw"));
        
        out.println("done");
    }
    
    private static void filter(int samplesPerSec, float[] x) {
        Validate.isTrue(samplesPerSec > 0);
        Validate.isTrue(x != null && x.length > 0);
        
        // https://tomroelandts.com/articles/how-to-create-a-simple-low-pass-filter
        
        SimpleDsp dsp = new SimpleDsp();
        
        //float[] highpass = dsp.createHighPass(samplesPerSec, 0, 440);
        //float[] lowpass  = dsp.createLowPass(samplesPerSec, 14_000, 36_000);
        //float[] filter = dsp.convolve(highpass, lowpass);
        
        float[] filter = dsp.createLowPass(samplesPerSec,  14_000, 36_000);
        FilteredSampleBuffer buf = new FilteredSampleBuffer(filter);
        
        System.err.println("filter: " + filter.length);
        
        float[] y = new float[x.length];
                        
        for(int i = 0; i < x.length; i++) {
            
            if( i % 100000 == 0 ) {
                System.err.println(i);
            }
            
            buf.add(x[i]);
            
            float a = 0;
            
            for(int n = 0; n < filter.length; n++) {
                
                a += filter[filter.length - 1 - n]*buf.samples[(buf.next + n)%filter.length];

                /*
                if( i - n >= 0 ) {
                    a += x[i - n]*filter[n];
                }
                */
            }
            
            y[i] = a;
            
        }
        
        System.arraycopy(y, 0, x, 0, x.length);
    }
    
    public static void writeAllSamples(float[] samples, Path p) throws Exception {
        try(OutputStream os = Files.newOutputStream(p);
            BufferedOutputStream bos = new BufferedOutputStream(os)) {
            
            for(float s: samples) {
                int i = Float.floatToIntBits(s);
                
                bos.write( (i>> 0) & 255 );
                bos.write( (i>> 8) & 255 );
                bos.write( (i>>16) & 255 );
                bos.write( (i>>24) & 255 );
            }
        }
    }
}

final class AvgBuf {

    private final float[] samples;
    private int start;
    private float total;
    //private float prevSample;
    
    AvgBuf(int size) {
        Validate.isTrue(size > 0);
        this.samples = new float[size];
        this.start = 0;
        this.total = 0;
        //this.prevSample = 0;
    }
    
    int size() {
        return samples.length;
    }
    
    public void add(float newSample) {
        float evictedSample = samples[start];
                
        samples[start++] = newSample;
        
        if( start == samples.length ) {
            start = 0;
        }
        
        total += newSample - evictedSample;
        //prevSample = newSample;
    }
    
    float avg() {
        return total/samples.length;
    }
}
