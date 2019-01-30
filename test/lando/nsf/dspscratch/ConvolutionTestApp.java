package lando.nsf.dspscratch;

import java.io.PrintStream;
import java.util.Arrays;

import org.apache.commons.lang3.Validate;

public class ConvolutionTestApp {

    private static PrintStream out = System.err;
    
    public static void main(String [] args) throws Exception {
        
        int[] f = {3, 4, 5, 1};
        int[] g = {6, 7, 8, 9, 10, 11};
        
        convolve(f, g);
        convolve(g, f);
        
        out.println( Arrays.toString( convolve2(f, g) ) );
        out.println( Arrays.toString( convolve2(g, f) ) );
    }
    
    private static int get(int[] a, int i) {
        return i >= 0 && i < a.length ? a[i] : 0;
    }
    
    private static void convolve(int[] f, int[] g) {
        
        out.print("[");
                
        for(int n = -10; n <= 10; n++) {
            int sum = 0;
            
            for(int m = -10; m <= 10; m++) {
                sum += get(f, m)*get(g, n - m);
            }
            
            out.print(sum + ", ");
        }
        
        out.println("]");
        
    }
    
    private static int[] convolve2(int[] f, int[] g) {
        Validate.isTrue(f != null && g != null && f.length > 0 && f.length > 0);
        
        int[] y = new int[f.length + g.length - 1];
        
        for(int n = 0; n < y.length; n++) {
            int a = 0;
            
            for(int m = 0; m < y.length; m++) {
                a += get(f, m)*get(g, n - m);
            }
            
            y[n] = a;
        }
        
        return y;
    }
}
