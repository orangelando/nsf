package lando.nsf.initload;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.apache.commons.lang3.Validate;

import lando.nsf.apu.Divider;

public final class NSFRenderer {
    
    //NES system clock rate
    public static final long SYSTEM_CYCLES_PER_SEC = 21_477_270;

    private final PrintStream out = System.out;
    
    //both the NES CPU and APU timers run off the
    //system clock divded by 12 or ~1.79MHz
    private final Divider cpuDivider = new Divider(12);
    
    private final NES nes;
    private final OutputFmt outFmt;
    
    private final long maxSystemCycles;
    private final long maxSilenceCycles;
    private final PeriodTimestampFinder playPeriodFinder;
    
    private long systemCycle;
    private long nextCycleToPlay;
    
    public NSFRenderer(
            NES nes,
            OutputFmt outFmt,
            int maxPlaySecs,
            int maxSilenceSecs) {
        
        Validate.isTrue(maxPlaySecs > 0);
        Validate.isTrue(maxSilenceSecs > 0);
        
        this.nes = Objects.requireNonNull(nes);
        this.outFmt = Objects.requireNonNull(outFmt);
        
        this.maxSystemCycles = SYSTEM_CYCLES_PER_SEC*maxPlaySecs;
        this.maxSilenceCycles = SYSTEM_CYCLES_PER_SEC*maxSilenceSecs;
        this.playPeriodFinder = createPlayPeriodFinder();
    }

    public void render(int trackNum, Path outputPath) throws Exception {
        Validate.isTrue(trackNum >= 1 && trackNum <= nes.nsf.header.totalSongs);
        Validate.notNull(outputPath);
        
        try(OutputStream os = Files.newOutputStream(outputPath);
            BufferedOutputStream bout = new BufferedOutputStream(os)) {
            
            nes.initTune(trackNum - 1);
            
            nes.startInit();
            nes.runRoutine();

            systemCycle = 0;
            cpuDivider.reset();
            nextCycleToPlay = playPeriodFinder.findNextPeriod(0);

            APUSampleConsumer sc = createSampleConsumer(bout);
            
            sc.init();
            
            while(systemCycle < maxSystemCycles) {                
                systemCycle += step(sc);
            }
            
            sc.finish();
        }
    }
    
    private APUSampleConsumer createSampleConsumer(BufferedOutputStream bout) {
        
        switch(outFmt) {
        case system_raw: return new RawConsumer(30, bout);
        case wav_16_441: return new WavConsumer(bout);
        }
        
        throw new IllegalArgumentException("Unknown fmt " + outFmt);
    }

    private PeriodTimestampFinder createPlayPeriodFinder() {
        
        long playPeriodNanos = nes.nsf.getPlayPeriodNanos();
        out.println("playPeriodNanos: " + playPeriodNanos);
        
        long playPeriodSystemCycles = (long)Math.round(
                playPeriodNanos/(1e9/SYSTEM_CYCLES_PER_SEC));   
        
        out.println("playPeriodSystemCycles: " + playPeriodSystemCycles);
        
        return new PeriodTimestampFinder(0, playPeriodSystemCycles);
    }

    /*
     * returns number of cycles that elapsed.
     */
    private int step(APUSampleConsumer sampleConsumer) throws Exception {
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
            
            sampleConsumer.consume(nes.apu.getOutput());
        }
        
        return cycles;
    }
}