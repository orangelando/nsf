package lando.nsf.dspscratch;

import static lando.nsf.spectrogram.GenerateSpectrogramApp.readAllSamples;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import org.apache.commons.lang3.Validate;

import lando.dsp.SimpleDsp;
import lando.nsf.app.towav.SampleRingBuffer;

public class FixedPointTestApp {

    public static void main(String [] args) throws Exception {
        PrintStream out = System.err;
        
        out.println("started");
        
        int samplesPerSec = 44_100;
        
        float[] samples = readAllSamples(
                Paths.get("/Users/oroman/Desktop/test.raw"));
        
        out.println("loaded");
        
        printStats(samples);
        
        out.printf("read %d samples or %s%n", 
                samples.length, 
                Duration.ofMillis(1000L*samples.length/samplesPerSec));
        
        filter(samplesPerSec, samples);
        
        out.println("filtered");
        
        printStats(samples);
        
        short[] pcm = new short[samples.length];
        
        for(int i = 0; i < samples.length; i++) {
            pcm[i] = (short)(samples[i]*32767);
        }
        
        out.println("converted...");
        
        writeAllSamples(
                pcm, 
                Paths.get("/Users/oroman/Desktop/filtered.raw"));
        
        out.println("done");
    }
    
    private static void printStats(float[] samples) {
        float max = Float.MIN_VALUE;
        float min = Float.MAX_VALUE;
        
        for(float s: samples) {
            max = Float.max(max, s);
            min = Float.min(min, s);
        }
        
        System.err.println("min: " + min + ", max: " + max);
    }
    
    private static void filter(int samplesPerSec, float[] x) {
        Validate.isTrue(samplesPerSec > 0);
        Validate.isTrue(x != null && x.length > 0);
        
        // https://tomroelandts.com/articles/how-to-create-a-simple-low-pass-filter
        
        SimpleDsp dsp = new SimpleDsp();
        
        float[] highpass = dsp.createHighPass(samplesPerSec, 0, 440);
        float[] lowpass  = dsp.createLowPass(samplesPerSec, 14_000, 36_000);
        float[] filter = dsp.convolve(highpass, lowpass);
        
        //float[] filter = dsp.createLowPass(samplesPerSec,  14_000, 36_000);
        SampleRingBuffer buf = new SampleRingBuffer(filter);
        
        System.err.println("filter: " + filter.length);
        
        float[] y = new float[x.length];
                        
        for(int i = 0; i < x.length; i++) {
            
            if( i % 100000 == 0 ) {
                //System.err.println(i);
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
    
    
    public static void writeAllSamples(short[] samples, Path p) throws Exception {
        try(OutputStream os = Files.newOutputStream(p);
            BufferedOutputStream bos = new BufferedOutputStream(os)) {
            
            for(short s: samples) {
                bos.write( (s>> 0) & 255 );
                bos.write( (s>> 8) & 255 );
            }
        }
    }
}
