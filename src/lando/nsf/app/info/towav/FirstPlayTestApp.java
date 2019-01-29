package lando.nsf.app.info.towav;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import lando.nsf.NSFHeader;

public final class FirstPlayTestApp {
    
    private static final String TRACK_NUM_OPT = "-trackNum";
    private static final String ALL_TRACKS_OPT = "-allTracks";

    @Option(name="-nsfFile", required=true)
    private String nsfFile;
    
    @Option(name="-outFile", required=true)
    private String outFile;
    
    @Option(name=TRACK_NUM_OPT, required=false, forbids={ALL_TRACKS_OPT})
    private Integer trackNum;
    
    @Option(name=ALL_TRACKS_OPT, required=false, forbids={TRACK_NUM_OPT})
    private Boolean allTracks;
    
    @Option(name="-disableChannels", required=false, usage="Turn off channels: 1=pulse1, 2=pulse2, T=triangle, N=noise, D=dmc")
    private String disableChannels;
    
    @Option(name="-maxPlaySecs", required=false, usage="Max track length")
    private int maxPlaySecs=30;
    
    @Option(name="-maxSilenceSecs", required=false, usage="Tracks stops after silence")
    private int maxSilenceSecs=3;
    
    @Option(name="-outFmt", required=false, usage="system_raw(21.47727mhz, 32bit float, raw apu output), wav_16_441(44.1khz, signed 16bit pcm wav file)")
    private OutputFmt outFmt = OutputFmt.wav_16_441;
    
    public static void main(String [] args) throws Exception {
        
        FirstPlayTestApp app = new FirstPlayTestApp();
        CmdLineParser cmdParser = new CmdLineParser(app);
        
        try {
            cmdParser.parseArgument(args);
            
            if( app.trackNum == null && app.allTracks == null ) {
                throw new CmdLineException(
                        cmdParser, 
                        new IllegalArgumentException(
                                "Single track-num or all-tracks must be selected."));
            }
            
        } catch(CmdLineException e) {
            System.err.println(e.getMessage());
            cmdParser.printUsage(System.err);
            System.exit(1);
        }
        
        app.exec();
    }
    
    private void exec() throws Exception {
        
        PrintStream out = System.out;
        
        Path path = Paths.get(nsfFile);
        
        if( ! Files.exists(path) ) {
            System.err.println(path + " does not exist");
            return;
        }
        
        NES nes = NES.buildForPathNoMemMonitor(path);
        
        printNSFInfo(out, nes.nsf.header);
        
        List<TrackArg> tracksToRender = buildTracksToRender(nes);
        
        if( disableChannels != null ) {
            disableSelectedChannels(nes);
        }
        
        NSFRenderer renderer = new NSFRenderer(nes, outFmt, maxPlaySecs, maxSilenceSecs);
        
        out.println("tracks-to-render: " + tracksToRender.size());
        
        for(TrackArg track: tracksToRender) {
            out.println();
            out.println("rendering track " + track.trackNum + " to " + track.outputPath);
            
            Instant start = Instant.now();
            
            renderer.render(track.trackNum, track.outputPath);
            
            Instant end = Instant.now();

            out.println("    rendered in " + Duration.between(start, end).toString().toLowerCase());
        }
        
        out.println("done");
    }

    private void printNSFInfo(PrintStream out, NSFHeader header) {
        
        out.printf("Song Name         : %s%n", header.songName);
        out.printf("Artist Name       : %s%n", header.artistName);
        out.printf("Copyright         : %s%n", header.copyrightHolder);
        out.printf("Total Songs       : %s%n", header.totalSongs);
        out.printf("Starting Song     : %s%n", header.startingSong);
        out.printf("Init Banks        : %s%n", Arrays.toString(header.bankswitchInitValues));
        out.printf("NTSC/PAL          : %s%n", header.getNtscPalMode());
        out.printf("Extra Sound Chips : %s%n", header.getSupportedExtraSoundChips());
        
        if( ! header.getSupportedExtraSoundChips().isEmpty() ) {
            out.println("WARNING: extra sound chips not supported!");
        }
    }

    private List<TrackArg> buildTracksToRender(NES nes) {
        
        Path outPath = Paths.get(outFile);
        List<TrackArg> tracksToRender = new ArrayList<>();
        
        if( trackNum != null ) {
            
            if( trackNum < 1 || trackNum > nes.nsf.header.totalSongs ) {
                throw new IllegalArgumentException("Invalid track num");
            }
            
            tracksToRender.add(new TrackArg(trackNum, outPath));
        }
        
        else if( allTracks != null ) {
            TrackNumAdder adder = new TrackNumAdder();
            
            for(int t = 1; t <= nes.nsf.header.totalSongs; t++) {
                Path trackOutPath = adder.addTrackNum(outPath, t);
                TrackArg arg = new TrackArg(t, trackOutPath);
                
                tracksToRender.add(arg);
            }
        }
        
        return tracksToRender;
    }

    private void disableSelectedChannels(NES nes) {

        String d = disableChannels.toLowerCase();
        
        if( d.contains("1") ) {
            nes.apu.setPulse1Enabled(false);
        }
        
        if( d.contains("2") ) {
            nes.apu.setPulse2Enabled(false);
        }
        
        if( d.contains("t") ) {
            nes.apu.setTriangleEnabled(false);
        }
        
        if( d.contains("n") ) {
            nes.apu.setNoiseEnabled(false);
        }
        
        if( d.contains("d") ) {
            nes.apu.setDmcEnabled(false);
        }
    }
}

final class TrackArg {
    final int trackNum;
    final Path outputPath;
    
    TrackArg(int trackNum, Path outputPath) {
        this.trackNum = trackNum;
        this.outputPath = outputPath;
    }
}
