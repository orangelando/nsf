package lando.nsf.initload;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import lando.nsf.core6502.CPU;

public final class TestPlaybackApp {

    public static void main(String [] args) throws Exception {
        
        Path path = Paths.get(
                "/Users/oroman/Desktop/stuff2/NSF-06-01-2011/d/Donkey Kong (1983)(Ikegami Tsushinki)(Nintendo R&D1)(Nintendo).nsf");
        
        NES nes = NES.buildForPath(path);
        AtomicInteger numInstrs = new AtomicInteger(0);

        int songIndex = 1;
        
        nes.initTune(songIndex);
         
        int stopAddr = CPU.RESET_VECTOR_ADDR;
        
        nes.startInit(stopAddr);
        numInstrs.set(0);
        long startTime = System.nanoTime();
        
        while( nes.cpu.PC != stopAddr) {
            nes.monitoringMem.clearReadsAndWrites();
            nes.cpu.step();
            numInstrs.incrementAndGet();
        }
        
        long elapsedTime = System.nanoTime() - startTime;

        System.err.printf(
                "executed %d instructions in %d ns or %f MIPS %n", 
                numInstrs.get(), 
                elapsedTime, 
                toMIPS(numInstrs.get(), elapsedTime));
                
        int numTimestamps = 1_000;
        AtomicInteger currTimestamp = new AtomicInteger(0);
        
        long[] playTimestamps = new long[numTimestamps];
        long[] execElapsedTimes = new long[numTimestamps];
        long[] numInstrsExecCounts = new long[numTimestamps];
        
        long playPeriodNanos = nes.nsf.getPlayPeriodNanos();
        System.err.println("playPeriodNanos: " + playPeriodNanos);

        //using the ScheduledExecutorService had the actual playback rate vary
        //from 49 to 73 hz. doing it this way takes up 100% CPU but is super
        //accurate!
        
        boolean done = false;
        long nextTimeToPlay = System.nanoTime();
        
        while( ! done ) {
            long now = System.nanoTime();
            
            if( now >= nextTimeToPlay) {
                nextTimeToPlay = System.nanoTime() + playPeriodNanos;
                numInstrs.set(0);

                nes.startPlay(stopAddr);
                
                while( nes.cpu.PC != stopAddr) {
                    nes.monitoringMem.clearReadsAndWrites();
                    nes.cpu.step();
                    numInstrs.incrementAndGet();
                }
                
                long endPlay = System.nanoTime();
                int ix = currTimestamp.getAndIncrement()%numTimestamps;
                playTimestamps[ix] = now;
                execElapsedTimes[ix] = endPlay - now;
                numInstrsExecCounts[ix] = numInstrs.get();
                
                if( currTimestamp.get() >= numTimestamps ) {
                    done = true;
                };                            
            }            
        }
        
        for(int i = 1; i < Math.min(120, currTimestamp.get()); i++) {
            double hz = 1e9/(playTimestamps[i] - playTimestamps[i - 1]);
            
            System.err.printf("%10.4fhz, %dus, %d, %10.4fMIPS%n", 
                    hz, 
                    execElapsedTimes[i]/1_000, 
                    numInstrsExecCounts[i],
                    toMIPS(numInstrsExecCounts[i], execElapsedTimes[i]));
        }
        
        System.err.println("done");
    }
    
    public static double toMIPS(long numInstrs, long numNanos) {
        return numInstrs/(numNanos/1e9)/1e6;
    }
}
