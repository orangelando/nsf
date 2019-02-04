package lando.nsf.spectrogram;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.Validate;

import com.meapsoft.FFT;

public final class GenerateSpectrogramApp {

    public static void main(String [] args) throws Exception {
        new GenerateSpectrogramApp().exec();
    }
    
    private void exec() throws Exception {
        PrintStream out = System.err;
        Path path = Paths.get(
                "/Users/oroman/Desktop/nsf-out/out-03.raw"
                );
        
        float[] samples = readAllSamples(path);
        out.println("numSamples: " + samples.length);
        
        long samplesPerSec = 21_477_270; //NES system clock rate
        
        int fftSize = 8192;
        Validate.isTrue( fftSize > 0 && Integer.bitCount(fftSize) == 1);
        
        int numFFTWindows = samples.length/fftSize;
        out.println("numFFTWindows: " + samples.length/fftSize);
        
        int[] pixels = new int[numFFTWindows*fftSize/2];
        
        double fftDuration = (double)fftSize/samplesPerSec;
        out.printf("fftDuration: %10.6f%n", fftDuration);
        
        double[] re = new double[fftSize], im = new double[fftSize];
        
        FFT fft = new FFT(fftSize);
        
        double minDb = -30;
        double maxDb =   0;
        
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        
        for(int i = 0; i < samples.length - fftSize; i += fftSize)  {
            
            Arrays.fill(im, 0);
            
            setSamples(samples, re, i, fftSize);
            
            fft.fft(re, im);
            
            for(int j = 0; j < fftSize/2; j++) {
                
                double u = 2*Math.sqrt(re[j]*re[j] + im[j]*im[j])/fftSize;

                u = 10*Math.log(u)/Math.log(10);
                u = (u - minDb)/(maxDb - minDb);                
                
                if( u < 0 ) u = 0;
                if( u > 1 ) u = 1;
                
                min = Math.min(min, u);
                max = Math.max(max, u);
                
                int r = (int)Math.round(Math.pow(u, 2.00)*255);
                int g = (int)Math.round(Math.pow(u, 1.00)*255);
                int b = (int)Math.round(Math.pow(u, 0.20)*255);
                
                int c = ((r<<16)&0xFF_00_00) | 
                        ((g<< 8)&0x00_FF_00) |
                        ((b<< 0)&0x00_00_FF) ;
                
                int x = i/fftSize;
                int y = j;
                
                pixels[y*numFFTWindows + x] = c;
            }
            
        }
        
        out.println("[" + min + ", " + max + "]");
        
        BufferedImage img = new BufferedImage(numFFTWindows, fftSize/2, BufferedImage.TYPE_INT_RGB);
        
        img.setRGB(0, 0, numFFTWindows, fftSize/2, pixels, 0, numFFTWindows);
        
        ImageIO.write(img, "PNG", Paths.get("/Users/oroman/Desktop/out.png").toFile());
        
        out.println("done");
    }
    
    public static float[] readAllSamples(Path path) throws Exception {
        long fileSize = Files.size(path);
        
        Validate.isTrue( fileSize <= Integer.MAX_VALUE, "File size must be less than 4 GiB");
        Validate.isTrue( fileSize%4 == 0, "File size must be multiple of 4");
                
        int numFloats = (int)fileSize/4;
        float[] samples = new float[numFloats];
        
        try(InputStream in = Files.newInputStream(path);
            InputStream bin = new BufferedInputStream(in)) {
            
            for(int i = 0; i < numFloats; i++) {
                int b0 = bin.read();
                int b1 = bin.read();
                int b2 = bin.read();
                int b3 = bin.read();
                
                int sampleBits = 
                        ((b0<< 0)&0x00_00_00_FF) |
                        ((b1<< 8)&0x00_00_FF_00) |
                        ((b2<<16)&0x00_FF_00_00) |
                        ((b3<<24)&0xFF_00_00_00) ;
                
                samples[i] = Float.intBitsToFloat(sampleBits);
            }
        }
        
        return samples;
    }
    
    private void setSamples(float[] samples, double[] re, int i, int fftSize) {
        for(int j = 0; j < fftSize; j++) {
            int k = i + j;
            
            if( k < samples.length ) {
                re[j] = samples[k]; 
            } else {
                re[j] = 0;
            }
        }
    }
}
