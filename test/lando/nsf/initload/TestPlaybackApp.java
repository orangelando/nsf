package lando.nsf.initload;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class TestPlaybackApp {

    public static void main(String [] args) throws Exception {
                
        PrintStream out = System.err;
        
        Path path = Paths.get(
                "/Users/oroman/Desktop/stuff2/NSF-06-01-2011/d/Donkey Kong (1983)(Ikegami Tsushinki)(Nintendo R&D1)(Nintendo).nsf");
        
        NES nes = NES.buildForPathNoMemMonitor(path);
        
        out.println("num-songs: " + nes.nsf.header.totalSongs);
        
        int songIndex = 5;
        nes.initTune(songIndex);
        
        nes.startInit();
        
        long startTime = System.nanoTime();
        nes.runRoutine();
        long elapsedTime = System.nanoTime() - startTime;

        out.printf(
                "executed %d instructions in %d ns or %f MIPS %n", 
                nes.numInstrs.get(), 
                elapsedTime, 
                toMIPS(nes.numInstrs.get(), elapsedTime));
                
        int numTimestamps = 1000;
        AtomicInteger currTimestamp = new AtomicInteger(0);
        
        long[] playTimestamps = new long[numTimestamps];
        long[] execElapsedTimes = new long[numTimestamps];
        long[] numInstrsExecCounts = new long[numTimestamps];
        
        long playPeriodNanos = nes.nsf.getPlayPeriodNanos();
        out.println("playPeriodNanos: " + playPeriodNanos);

        //using the ScheduledExecutorService had the actual playback rate vary
        //from 49 to 73 hz. doing it this way takes up 100% CPU but is super
        //accurate!
        
        boolean done = false;
        long nextTimeToPlay = System.nanoTime();
        
        while( ! done ) {
            long now = System.nanoTime();
            
            if( now >= nextTimeToPlay) {
                nextTimeToPlay = System.nanoTime() + playPeriodNanos;
                
                nes.startPlay();
                nes.runRoutine();
                
                long endPlay = System.nanoTime();
                
                int ix = currTimestamp.getAndIncrement()%numTimestamps;
                playTimestamps[ix] = now;
                execElapsedTimes[ix] = endPlay - now;
                numInstrsExecCounts[ix] = nes.numInstrs.get();
                
                if( currTimestamp.get() >= numTimestamps ) {
                    done = true;
                };                            
            }            
        }
        
        for(int i = 1; i < Math.min(120, currTimestamp.get()); i++) {
            double hz = 1e9/(playTimestamps[i] - playTimestamps[i - 1]);
            
            out.printf("%10.4fhz, %8dus, %5d, %10.4fMIPS%n", 
                    hz, 
                    execElapsedTimes[i]/1_000, 
                    numInstrsExecCounts[i],
                    toMIPS(numInstrsExecCounts[i], execElapsedTimes[i]));
        }
        
        out.println("done");
    }
    
    private static double toMIPS(long numInstrs, long numNanos) {
        return numInstrs/(numNanos/1e9)/1e6;
    }
}
