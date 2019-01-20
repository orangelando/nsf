package lando.nsf.initload;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TestPlaybackWithSchedulerApp {
    
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
                        
        PlayStatsList statsList = new PlayStatsList(1000);
        
        long playPeriodNanos = nes.nsf.getPlayPeriodNanos();
        out.println("playPeriodNanos: " + playPeriodNanos);
        
        long schedPeriodNanos = 1_000_000; //1ms
        
        /**
         * Using an empty while loop to wait for the next time to play
         * is super accurate but takes up 100% CPU. 
         * 
         * Using just the scheduler for a 60.0024hz playback rates resulted
         * in +- 15-20% variance in frequency.
         * 
         * Instead we use the scheduler to run at 1000hz and use an
         * empty while loop when we are "close enough" to the next
         * play time. This gives great accuracy and 10% CPU.
         * 
         */
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        PeriodTimestampFinder periodFinder = new PeriodTimestampFinder(
                System.nanoTime(),
                playPeriodNanos);
                
        scheduler.scheduleAtFixedRate(
                () -> {
                    maybeExecPlayRoutine(
                            periodFinder, schedPeriodNanos, statsList, nes);      
                }, 
                0, 
                schedPeriodNanos, 
                TimeUnit.NANOSECONDS);
        
        while( ! scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
            scheduler.shutdownNow();
        }
                
        reportStats(out, statsList);
        
        out.println("done");
    }
    
    private static void maybeExecPlayRoutine(
            PeriodTimestampFinder periodFinder,
            long schedPeriodNanos,
            PlayStatsList statsList,
            NES nes
            ) {
        
        long nextTimeToPlay = periodFinder.findNextPeriod(
                System.nanoTime());
        
        //too long to wait!
        if( nextTimeToPlay - System.nanoTime() > schedPeriodNanos*2) {
            return;
        }
        
        //close enough to wait!
        long timesWaited = 0;
        
        while( System.nanoTime() < nextTimeToPlay ) {
            ++timesWaited;
        }
            
        long startPlay = System.nanoTime();
        nes.startPlay();
        nes.runRoutine();
        long endPlay = System.nanoTime();
        
        PlayStats stats = statsList.next();
        
        stats.playTimestamp = startPlay;
        stats.execElapsedTime = endPlay - startPlay;
        stats.numInstrsExec = nes.numInstrs.get();
        stats.timesWaited = timesWaited;
        
        if( statsList.isFull() ) {
            throw new IllegalStateException("gone too far!");
        };
    }
    
    private static void reportStats(PrintStream out, PlayStatsList statsList) {
        
        for(int i = 1; i < Math.min(120, statsList.size()); i++) {
            
            PlayStats curr = statsList.get(i), 
                      prev = statsList.get(i - 1);
            
            double hz = 1e9/(curr.playTimestamp - prev.playTimestamp);
            
            out.printf("%10.4fhz, %8dus, %5d, %10.1fmips %d times waited%n", 
                    hz, 
                    curr.execElapsedTime/1_000, 
                    curr.numInstrsExec,
                    toMIPS(curr.numInstrsExec, curr.execElapsedTime),
                    curr.timesWaited);
        }
    }

    private static double toMIPS(long numInstrs, long numNanos) {
        return numInstrs/(numNanos/1e9)/1e6;
    }
}
