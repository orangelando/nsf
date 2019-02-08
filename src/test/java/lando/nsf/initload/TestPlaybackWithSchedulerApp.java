package lando.nsf.initload;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lando.nsf.app.towav.NES;
import lando.nsf.app.towav.PeriodTimestampFinder;
import lando.nsf.cpu.Memory;
import lando.nsf.gui.StringUtils;

public class TestPlaybackWithSchedulerApp {
    
    public static void main(String [] args) throws Exception {
        
        PrintStream out = System.out;
        
        Path path = Paths.get(args[0]);
        int songIndex = Integer.parseInt(args[1]) - 1;
        int playSecs = Integer.parseInt(args[2]);
        
        APUCaptureMem apuMem = new APUCaptureMem();
        NES nes = NES.buildForPath(path, (mem) -> {
            apuMem.setMemory(mem);
            return apuMem;
        });
        
        out.println("num-songs: " + nes.nsf.header.totalSongs);
        
        nes.initTune(songIndex);
        
        apuMem.startCapturing(System.nanoTime());
        nes.startInit();
        nes.runRoutine();

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
                
        long playStart = System.nanoTime();
        
        scheduler.scheduleAtFixedRate(
                () -> {
                    maybeExecPlayRoutine(
                            playStart, 
                            periodFinder, schedPeriodNanos, statsList, nes, apuMem);      
                }, 
                0, 
                schedPeriodNanos, 
                TimeUnit.NANOSECONDS);
        
        while( ! scheduler.awaitTermination(playSecs, TimeUnit.SECONDS)) {
            scheduler.shutdownNow();
        }
        
        long playStop = System.nanoTime();
        
        out.printf("total time played: %s%n", 
                Duration.ofNanos(playStop - playStart));
                
        reportStats(out, statsList);
        reportWrites(out, apuMem);
        
        out.println("done");
    }

    private static void maybeExecPlayRoutine(
            long initStart, 
            PeriodTimestampFinder periodFinder,
            long schedPeriodNanos,
            PlayStatsList statsList,
            NES nes, 
            APUCaptureMem apuMem
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
        //System.out.println("_____________________________________________________________________");
        //System.out.printf("t:%.3f%n", (startPlay - initStart)/1e9);
        //apuMem.startCapturing(startPlay);
        nes.startPlay();
        nes.runRoutine();
        long endPlay = System.nanoTime();
        
        PlayStats stats = statsList.next();
        
        stats.playTimestamp = startPlay;
        stats.execElapsedTime = endPlay - startPlay;
        stats.numInstrsExec = nes.numInstrs.get();
        stats.numCycles = nes.numCycles.get();
        stats.timesWaited = timesWaited;
        
        if( statsList.isFull() ) {
            throw new IllegalStateException("gone too far!");
        };
    }
    
    private static void reportStats(PrintStream out, PlayStatsList statsList) {
        
        for(int i = 1; i < Math.min(180, statsList.size()); i++) {
            
            PlayStats curr = statsList.get(i), 
                      prev = statsList.get(i - 1);
            
            double hz = 1e9/(curr.playTimestamp - prev.playTimestamp);
            
            out.printf("%10.4fhz, %8dus, %5d, %10.1fmips %8d times waited, %5d cycles%n", 
                    hz, 
                    curr.execElapsedTime/1_000, 
                    curr.numInstrsExec,
                    toMIPS(curr.numInstrsExec, curr.execElapsedTime),
                    curr.timesWaited,
                    curr.numCycles);
        }
    }
    
    private static void reportWrites(PrintStream out, APUCaptureMem apuMem) {
        out.println("num frames: " + apuMem.apuWrites.size());
        
        if( apuMem.apuWrites.isEmpty()) {
            return;
        }
        
        long firstTime = apuMem.apuWrites.get(0).nanoTime;
        
        for(Writes writes: apuMem.apuWrites) {
            long time = writes.nanoTime - firstTime;
            out.println();
            out.printf("%.3f%n", time/1e9);
            
            for(Write write: writes.writes) {
                String d = StringUtils.toBin8(write.data);
                
                d = d.substring(0, 4) + " " + d.substring(4);
                
                out.printf("    %4x: %8s%n", write.addr, d);
            }
        }
    }

    private static double toMIPS(long numInstrs, long numNanos) {
        return numInstrs/(numNanos/1e9)/1e6;
    }
    
    private static final class Write {
        final int addr;
        final int data;
        
        Write(int addr, int data) {
            this.addr = addr;
            this.data = data;
        }
    }
    
    private static final class Writes {
        final long nanoTime;
        final List<Write> writes = new ArrayList<>();
        
        Writes(long nanoTime) {
            this.nanoTime = nanoTime;
        }
    }
    
    private static final class APUCaptureMem implements Memory {
        final List<Writes> apuWrites = new ArrayList<>();
        Memory mem = null;
        Writes currWrites = null;
        
        void setMemory(Memory mem) {
            this.mem = mem;
        }
        
        void startCapturing(long nanoTime) {
            currWrites = new Writes(nanoTime);
            
            apuWrites.add(currWrites);
        }

        @Override
        public int read(int addr) {
            return mem.read(addr);
        }

        @Override
        public void write(int addr, int data) {
            
            if( addr >= 0x4000 && addr <= 0x4017 ) {
                currWrites.writes.add(new Write(addr, data));
            }
            
            mem.write(addr, data);
        }
    }

}
