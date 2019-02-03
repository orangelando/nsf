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

import lando.nsf.apu.APU;

final class APUSamplers implements AutoCloseable {

    private final PrintStream out = System.out;
    
    private final Path origPath;
    private final OutputFmt outFmt;
    private final boolean disableBandPass;
    
    private final ChannelNameAdder nameAdder = new ChannelNameAdder();
    private final List<OutputStream> streams = new ArrayList<>();
    private final List<APUSamplePipe> samplers = new ArrayList<>();
    
    APUSamplers(Path origPath, OutputFmt outFmt, boolean disableBandPass) {
        this.origPath = Objects.requireNonNull(origPath);
        this.outFmt = Objects.requireNonNull(outFmt);
        this.disableBandPass = disableBandPass;
    }
    
    List<APUSamplePipe> getSamplers() {
        return samplers;
    }
    
    void setupSamplers(APU apu, boolean splitChannels) throws Exception {
        
        Validate.notNull(apu);
        
        if( ! splitChannels ) {
            maybeAddSampler(apu::mixerOutput);
        } else {
            maybeAddSampler("p1",    apu.isPulse1Enabled(),   apu::pulse1Output);
            maybeAddSampler("p2",    apu.isPulse2Enabled(),   apu::pulse2Output);
            maybeAddSampler("tri",   apu.isTriangleEnabled(), apu::triangleOutput);
            maybeAddSampler("noise", apu.isNoiseEnabled(),    apu::noiseOutput);
            maybeAddSampler("dmc",   apu.isDmcEnabled(),      apu::dmcOutput);
        }
    }
    
    private void maybeAddSampler(APUSampleSupplier supplier) throws Exception {
        maybeAddSampler(null, true, supplier);
    }
    
    private void maybeAddSampler(
            String channelName,
            boolean enabled,
            APUSampleSupplier supplier) throws Exception {
        
        if( enabled ) {
            Path outputPath = channelName != null ? 
                    nameAdder.addChannelName(origPath, channelName) :
                    origPath;
            
            out.println("    opening " + outputPath);
            
            OutputStream os = Files.newOutputStream(outputPath);
            BufferedOutputStream bout = new BufferedOutputStream(os);
            APUSampleConsumer consumer = createSampleConsumer(bout);
    
            samplers.add(new APUSamplePipe(supplier, consumer));
        }
    }
    
    private APUSampleConsumer createSampleConsumer(BufferedOutputStream bout) {
        
        switch(outFmt) {
        case system_raw: return new RawConsumer(12, bout);
        case wav: return new WavConsumer(bout, disableBandPass);
        }
        
        throw new IllegalArgumentException("Unknown fmt " + outFmt);
    }

    @Override
    public void close() throws Exception {
        Exception lastException = null;
        
        for(int i = streams.size() - 1; i >= 0; i--) {
            OutputStream s = streams.get(i);
            
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
}
