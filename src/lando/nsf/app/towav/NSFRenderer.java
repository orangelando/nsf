package lando.nsf.app.towav;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.Validate;

import lando.nsf.apu.Divider;

public final class NSFRenderer {
    
    //NES system clock rate
    public static final int SYSTEM_CYCLES_PER_SEC = 21_477_270;
    
    private final PrintStream out = System.out;
    
    //both the NES CPU and APU timers run off the
    //system clock divided by 12 or ~1.79MHz
    private final Divider cpuDivider = new Divider(12);
    
    //~239.996hz
    private final Divider frameSequencerDivider = new Divider(89490);
        
    private final NES nes;
    private final OutputFmt outFmt;
    
    private final long maxSystemCycles;
    private final long fadeOutStartCycle;
    private final boolean disableFadeOut;
    
    private final PeriodTimestampFinder playPeriodFinder;
    private final SilenceDetector silenceDetector;
    
    private boolean splitChannels = false;
    private boolean disableBandPass = false;
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
        this.fadeOutStartCycle = this.maxSystemCycles - SYSTEM_CYCLES_PER_SEC; //1 second fade out
        this.disableFadeOut = this.fadeOutStartCycle <= SYSTEM_CYCLES_PER_SEC; //do not fade out if max play is <= 1 second.
        this.silenceDetector = new SilenceDetector(SYSTEM_CYCLES_PER_SEC*maxSilenceSecs);
        this.playPeriodFinder = createPlayPeriodFinder();
    }
    
    public void splitChannels() {
        splitChannels = true;
    }
    
    public void disableBandPass() {
        disableBandPass = true;
    }


    public void render(int trackNum, Path outputPath) throws Exception {
        Validate.isTrue(trackNum >= 1 && trackNum <= nes.nsf.header.totalSongs);
        Validate.notNull(outputPath);
        
        try(APUSamplers samplers = new APUSamplers(outputPath, outFmt, disableBandPass)) {
            
            samplers.setupSamplers(nes.apu, splitChannels);
            
            nes.initTune(trackNum - 1);
            nes.execInit();

            systemCycle = 0;
            cpuDivider.reset();
            silenceDetector.reset();
            nextCycleToPlay = playPeriodFinder.findNextPeriod(0);
            
            for(APUSamplePipe sampler: samplers.getSamplers()) {
                sampler.sampleConsumer.init();
            }
            
            while(systemCycle < maxSystemCycles) {                
                systemCycle += step(samplers.getSamplers());
                
                if( silenceDetector.wasSilenceDetected() ) {
                    break;
                }
            }
            
            for(APUSamplePipe sampler: samplers.getSamplers()) {
                sampler.sampleConsumer.finish();
            }   
        }        
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
    private int step(List<APUSamplePipe> samplers) throws Exception {
        int cycles;
        
        if( systemCycle >= nextCycleToPlay ) {
            nextCycleToPlay = playPeriodFinder.findNextPeriod(systemCycle + 1);
            
            nes.execPlay();
            
            cycles = nes.numCycles.get();                
        } else {
            cycles = 1;
        }
        
        for(int i = 0; i < cycles; i++) {
            
            if( cpuDivider.clock() ) {
                nes.apu.clockChannelTimers();
            }
            
            if( frameSequencerDivider.clock() ) {
                nes.apu.clockFrameSequencer();
            }
            
            float sample = nes.apu.mixerOutput();

            silenceDetector.addSample(sample);

            float scale;
            
            if( ! disableFadeOut && systemCycle >= fadeOutStartCycle ) {
                scale = 1f - (float)(systemCycle - fadeOutStartCycle)/(maxSystemCycles - fadeOutStartCycle);
            } else {
                scale = 1f;
            }
            
            for(APUSamplePipe sampler: samplers) {
                sampler.sample(scale);
            }
        }
        
        return cycles;
    }
}
