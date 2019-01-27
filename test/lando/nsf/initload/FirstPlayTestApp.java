package lando.nsf.initload;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;

import lando.nsf.apu.Divider;

/**
 * Outputs a raw stream of 16bit PCM samples at the system clock
 * rate of 21.47727 MHz.
 * 
 */
public final class FirstPlayTestApp {

    public static void main(String [] args) throws Exception {
        new FirstPlayTestApp().exec();
    }
    
    private void exec() throws Exception {
        
        PrintStream out = System.err;
        
        long systemCyclesPerSec = 21_477_270;
        long playtimeSecs = 30;
        long numSystemCycles = systemCyclesPerSec*playtimeSecs;
        
        out.println("numSystemCycles: " + numSystemCycles);
        
        long audioSamplesPerSec = 44_100;
        
        int audioDividerPeriod = (int)(systemCyclesPerSec/audioSamplesPerSec);
        out.println("audioDividerPeriod: " + audioDividerPeriod);
        
        Divider audioDivider = new Divider( audioDividerPeriod );
        float audioAccumulator = 0;
        
        Divider cpuDivider = new Divider(12);
        
        Path outPath = Paths.get(System.getProperty("user.home"), "Desktop", "out.raw");
        
        Path nsfDir = Paths.get("/Users/oroman/Desktop/stuff2/NSF-06-01-2011");
        Path path = nsfDir.resolve(
              //"d/Donkey Kong (1983)(Ikegami Tsushinki)(Nintendo R&D1)(Nintendo).nsf"
              "s/Super Mario Bros. 2 [Yume Koujou - Doki Doki Panic] [Super Mario USA] (1987)(Nintendo EAD)(Nintendo).nsf"
                );
        
        NES nes = NES.buildForPathNoMemMonitor(path);
        
        out.println("num-songs: " + nes.nsf.header.totalSongs);
        
        int songIndex = 1;
        nes.initTune(songIndex);
        
        nes.startInit();
        nes.runRoutine();
        
        long playPeriodNanos = nes.nsf.getPlayPeriodNanos();
        out.println("playPeriodNanos: " + playPeriodNanos);

        long playPeriodSystemCycles = (long)Math.round(
                playPeriodNanos/(1e9/systemCyclesPerSec));   
        
        out.println("playPeriodSystemCycles: " + playPeriodSystemCycles);
        
        PeriodTimestampFinder playPeriodFinder = new PeriodTimestampFinder(
                0, playPeriodSystemCycles);

        long nextCycleToPlay = playPeriodFinder.findNextPeriod(
                0);
        
        Instant start = Instant.now();        
        
        try(OutputStream os = Files.newOutputStream(outPath);
            BufferedOutputStream bout = new BufferedOutputStream(os)) {
            
            long systemCycle = 0;
            
            while(systemCycle < numSystemCycles) {                
                int cycles;
                
                if( systemCycle >= nextCycleToPlay ) {
                    nextCycleToPlay = playPeriodFinder.findNextPeriod(systemCycle + 1);
                    
                    nes.startPlay();
                    nes.runRoutine();
                    
                    cycles = nes.numCycles.get();                
                } else {
                    cycles = 1;
                }
                
                for(int i = 0; i < cycles; i++) {
                    
                    if( cpuDivider.clock() ) {
                        nes.apu.clockChannelTimers();
                    }
                    
                    nes.apu.clockFrameSequencer();
        
                    audioAccumulator += nes.apu.getOutput();
                    
                    if( audioDivider.clock() ) {

                        audioAccumulator /= audioDivider.getPeriod();
                        
                        int sample = Float.floatToIntBits(audioAccumulator);
                        
                        //little endian
                        bout.write((sample>> 0)&255);
                        bout.write((sample>> 8)&255);
                        bout.write((sample>>16)&255);
                        bout.write((sample>>24)&255);
                        
                        audioAccumulator = 0;
                    }
                }
                
                systemCycle += cycles;
            }
        }
        
        Instant end = Instant.now();
        
        out.println("duration: " + Duration.between(start, end));
        out.println("done");
    }
}
