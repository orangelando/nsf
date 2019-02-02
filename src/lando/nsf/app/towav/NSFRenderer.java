package lando.nsf.app.towav;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.Validate;

import lando.nsf.apu.Divider;

public final class NSFRenderer {
    
    //NES system clock rate
    public static final int SYSTEM_CYCLES_PER_SEC = 21_477_270;
    
    private final PrintStream out = System.out;
    
    //both the NES CPU and APU timers run off the
    //system clock divded by 12 or ~1.79MHz
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

    public void render(int trackNum, Path outputPath) throws Exception {
        Validate.isTrue(trackNum >= 1 && trackNum <= nes.nsf.header.totalSongs);
        Validate.notNull(outputPath);
        
        List<OutputStream> streams = new ArrayList<>();
        List<APUSamplePipe> samplers = new ArrayList<>();
        
        try {
            setupSamplers(outputPath, streams, samplers);
            
            nes.initTune(trackNum - 1);
            
            nes.startInit();
            nes.runRoutine();

            systemCycle = 0;
            cpuDivider.reset();
            silenceDetector.reset();
            nextCycleToPlay = playPeriodFinder.findNextPeriod(0);
            
            for(APUSamplePipe sampler: samplers) {
                sampler.sampleConsumer.init();
            }
            
            while(systemCycle < maxSystemCycles) {                
                systemCycle += step(samplers);
                
                if( silenceDetector.wasSilenceDetected() ) {
                    break;
                }
            }
            
            for(APUSamplePipe sampler: samplers) {
                sampler.sampleConsumer.finish();
            }
                
        } finally {
            closeAll(streams);
        }        
    }
    
    private void addSampler(
            Path outputPath,
            APUSampleSupplier supplier,
            List<OutputStream> streamsToClose, 
            List<APUSamplePipe> samplers) throws Exception {
        
        out.println("    opening " + outputPath);
        
        OutputStream os = Files.newOutputStream(outputPath);
        BufferedOutputStream bout = new BufferedOutputStream(os);
        APUSampleConsumer consumer = createSampleConsumer(bout);

        samplers.add(new APUSamplePipe(supplier, consumer));
    }
    
    private void setupSamplers(Path outputPath, List<OutputStream> streams, List<APUSamplePipe> samplers) throws Exception {
        
        if( ! splitChannels ) {
            addSampler(
                    outputPath, 
                    nes.apu::mixerOutput, 
                    streams, 
                    samplers);
        } else {
            ChannelNameAdder nameAdder = new ChannelNameAdder();
            
            if( nes.apu.isPulse1Enabled() ) {
                addSampler(
                        nameAdder.addChannelName(outputPath, "p1"),
                        nes.apu::pulse1Output,
                        streams,
                        samplers);
            }
            
            if( nes.apu.isPulse2Enabled() ) {
                addSampler(
                        nameAdder.addChannelName(outputPath, "p2"),
                        nes.apu::pulse2Output,
                        streams,
                        samplers);
            }
            
            if( nes.apu.isTriangleEnabled() ) {
                addSampler(
                        nameAdder.addChannelName(outputPath, "tri"),
                        nes.apu::triangleOutput,
                        streams,
                        samplers);
            }
            
            if( nes.apu.isNoiseEnabled() ) {
                addSampler(
                        nameAdder.addChannelName(outputPath, "noise"),
                        nes.apu::noiseOutput,
                        streams,
                        samplers);
            }
            
            if( nes.apu.isDmcEnabled() ) {
                addSampler(
                        nameAdder.addChannelName(outputPath, "dmc"),
                        nes.apu::dmcOutput,
                        streams,
                        samplers);
            }
        }
    }
    
    private void closeAll( List<OutputStream> streamsToClose) throws Exception {
        Exception lastException = null;
        
        for(int i = streamsToClose.size() - 1; i >= 0; i--) {
            OutputStream s = streamsToClose.get(i);
            
            try {
                s.close();
            } catch(Exception e) {
                lastException = e;
            }
        }
        
        if( lastException != null ) {
            throw lastException;
        }
    }
    
    private APUSampleConsumer createSampleConsumer(BufferedOutputStream bout) {
        
        switch(outFmt) {
        case system_raw: return new RawConsumer(1, bout);
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
    private int step(List<APUSamplePipe> samplers) throws Exception {
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
