package lando.nsf.apu;

import java.util.ArrayList;
import java.util.List;

public final class APUOutputRangeTestApp {

    public static void main(String [] args) throws Exception {
        
        int[] pulse1Vals   = {0, 15};
        int[] pulse2Vals   = {0, 15};
        int[] triangleVals = {0, 15};
        int[] noiseVals    = {0, 15};
        int[] dmcVals      = {0, 127};
        
        List<Double> vals = new ArrayList<>();
        
        for(int pulse1: pulse1Vals) {
            for(int pulse2: pulse2Vals) {
                for(int triangle: triangleVals) {
                    for(int noise: noiseVals) {
                        for(int dmc: dmcVals) {
                            double pulseOut =  95.88/( 8128.0/(pulse1 + pulse2) + 100.0 );
                            double tndOut   = 159.79/( 1f/(triangle/8227.0 + noise/12241.0 + dmc/22638.0) + 100f );
                            double out      = pulseOut + tndOut;
                            
                            vals.add(out);
                        }
                    }
                }
            }
        }
        
        vals.sort((a, b) -> Double.compare(a,  b));
        

        vals.forEach(d -> {
            System.err.printf("%10.3f%n", d);
        });
    }
}
